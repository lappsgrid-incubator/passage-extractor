package org.anc.lapps.chunk

import org.anc.lapps.chunk.window.Window
import org.anc.lapps.chunk.window.extraction.AbstractWindowExtraction
import org.anc.lapps.chunk.window.extraction.AnnTypeBasedWindow
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class WindowExtractorService extends AbstractWindowService {

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

        File keytermFile = new File((String) params.keyword)
        if (!keytermFile.exists()) {
            return error(NO_KEYWORD_FILE).asPrettyJson()
        }

        List<String> keyterms = readKeyterms(keytermFile)
        Container container = new Container(data.payload)

        AbstractWindowExtraction extractor = new AnnTypeBasedWindow(params)
        List<Window> candidateWindows = extractor.extract(container, keyterms)

        if (candidateWindows == null || candidateWindows.size() == 0) {
            Container result = new Container()
            result.text = container.text
            result.metadata = container.metadata
            View view = result.newView()
            String annotationType = params.annotation
            view.addContains(annotationType, WindowExtractorService.class.name, annotationType)
            return new Data(Discriminators.Uri.LIF, result).asJson()
        }

        View resultView = container.newView()
        resultView.addContains(WINDOW, WindowExtractorService.class.name, WINDOW)
        resultView.metadata.keyterms = keyterms
        resultView.metadata.sizelimit = params.sizelimit ?: "unlimited"
        resultView.metadata.numlimit = params.numlimit ?: "unlimited"
        resultView.metadata.matchlimit = params.matchlimit ?: "unlimited"

        int id = 0
        candidateWindows.each { Window window ->
            resultView.addAnnotation(window.toAnnotation("window-${++id}"))
        }

        return new Data(Uri.LIF, container).asPrettyJson()
    }

    List<String> readKeyterms(File keytermFile) {
        return keytermFile.readLines()
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
                    .name(WindowExtractorService.class.name)
                    .version(Version.getVersion())
                    .description('Extracts all segments (chunks) that contain the given string(s).')
                    .requireEncoding('UTF-8')
                    .requireFormat(Uri.LIF)
                    .produceEncoding('UTF-8')
                    .produceFormat(Uri.LIF)
                    .build()
            metadata = Serializer.toPrettyJson(md)
        }
        return metadata
    }
}
