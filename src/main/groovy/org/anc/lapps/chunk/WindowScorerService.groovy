package org.anc.lapps.chunk

import org.anc.lapps.chunk.scorer.CompositeScorer
import org.anc.lapps.chunk.window.Window
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class WindowScorerService extends AbstractWindowService {

    String metadata

    /**
     * Entry point for a Lappsgrid service.
     * <p>
     * Each service on the Lappsgrid will accept {@code org.lappsgrid.serialization.Data} object
     * and return a {@code Data} object with a {@code org.lappsgrid.serialization.lif.Container}
     * payload.
     * <p>
     * Errors and exceptions the occur during processing should be wrapped in a {@code Data}
     * object with the discriminator set to http://vocab.lappsgrid.org/ns/error
     * <p>
     * See <a href="https://lapp.github.io/org.lappsgrid.serialization/index.html?org/lappsgrid/serialization/Data.html>org.lappsgrid.serialization.Data</a><br />
     * See <a href="https://lapp.github.io/org.lappsgrid.serialization/index.html?org/lappsgrid/serialization/lif/Container.html>org.lappsgrid.serialization.lif.Container</a><br />
     *
     *
     * @param input A JSON string representing a Data object
     * @return A JSON string containing a Data object with a Container payload.
     */
    @Override
    String execute(String input) {

        Data data = validateInputJson(input)

        if (Uri.ERROR == data.discriminator) {
            return data.asPrettyJson()
        }

        Map params = data.parameters
        Data paramsValidation = validateParameters(params)
        if (paramsValidation != null && paramsValidation.discriminator == Uri.ERROR) {
            return paramsValidation.asPrettyJson()
        }
        CompositeScorer scorer
        if (params.containsKey("scorers")) {
            scorer = new CompositeScorer((String) params.scorers)
        } else {
            scorer = new CompositeScorer()
            scorer.useDefaultScorers()
        }

        Container container = new Container(data.payload)
        List<View> views = container.findViewsThatContain(WINDOW)

        View view = views[-1]
        List<Annotation> annotations = view.annotations
        View scores = container.newView()
        scores.addContains(SCORES, WindowScorerService.class.name, SCORES)

        List<String> keyterms = view.metadata.keyterms ?: []
        int matchLimit = (int) view.metadata.matchlimit ?: Double.POSITIVE_INFINITY
        Window document = new Window(0, container.text.length(), container.text, "document", keyterms, matchLimit)

        int id = 0
        Best best = new Best()

        annotations.each { Annotation a ->
            Window window = new Window((int) a.start, (int) a.end, a.features.text, a.features.id, keyterms, matchLimit)
            Annotation scoreA = scores.newAnnotation("score-${++id}", "Score", a.start, a.end)
            double score = scorer.scoreWindow(window, document)
            scoreA.features.score = score
            scoreA.features.window = a.id
            if (score > best.score) {
                best.score = score
                best.start = a.start
                best.end = a.end
                best.window = a.id
            }
        }

        //container.views[-1].metadata.scores = scores
        View bestView = container.newView()
        bestView.addContains(BEST, WindowScorerService.class.name, BEST)
        Annotation bestA = bestView.newAnnotation('best-0', BEST, best.start, best.end)
        bestA.features.window = best.window
        bestA.features.score = best.score

        return new Data(Uri.LIF, container).asPrettyJson()
    }

    /**
     * Returns a JSON string containing metadata describing the service. The
     * JSON <em>must</em> conform to the json-schema at
     * <a href="http://vocab.lappsgrid.org/schema/service-schema.json">http://vocab.lappsgrid.org/schema/service-schema.json</a>
     * (processing services) or
     * <a href="http://vocab.lappsgrid.org/schema/datasource-schema.json">http://vocab.lappsgrid.org/schema/datasource-schema.json</a>
     * (datasources).
     *
     */
    @Override
    String getMetadata() {
        if (!metadata) {
            ServiceMetadata md = new ServiceMetadataBuilder()
                    .allow(Uri.ANY)
                    .license(Uri.APACHE2)
                    .vendor("http://www.anc.org")
                    .name(WindowScorerService.class.name)
                    .version(Version.getVersion())
                    .description('Scores relevance of segments pulled by WindowExtractorService')
                    .requireEncoding('UTF-8')
                    .requireFormat(Uri.LIF)
                    .produceEncoding('UTF-8')
                    .produceFormat(Uri.LIF)
                    .build()
            metadata = Serializer.toPrettyJson(md)
        }
        return metadata
    }
       class Best {
            double score = -1
            long start = 0
            long end = 0
            String window
        }

}
