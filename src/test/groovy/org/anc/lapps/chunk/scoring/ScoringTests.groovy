package org.anc.lapps.chunk.scoring

import org.anc.lapps.chunk.scorer.BrevityScorer
import org.anc.lapps.chunk.scorer.MatchRecallScorer
import org.anc.lapps.chunk.scorer.OffsetScorer
import org.anc.lapps.chunk.scorer.WindowScorerI
import org.anc.lapps.chunk.window.Window
import org.junit.Test
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.junit.Assert.*
import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class ScoringTests {

    Data score(String name, WindowScorerI scorer, Data data) {
        assertEquals(Uri.LIF, data.discriminator)
        assertNotNull(scorer.class.name)
        println scorer.class.name
        Container container = new Container(data.payload)
        List<View> views = container.findViewsThatContain('Window')
        assertEquals(1, views.size())

        View view = views[-1]
        List<Annotation> annotations = view.annotations
        assertEquals(2, annotations.size())

        Window document = new Window(0, container.text.length(), container.text, view.metadata.keyterms ?: [])

        Map scores = [:]
        annotations.each { Annotation a ->
            List terms = a.features.matches.collect { it.term }
            Window window = new Window((int)a.start, (int)a.end, a.features.text, terms)
            scores[name] = scorer.scoreWindow(window, document)
        }
        container.views[-1].metadata.scores = scores
        return new Data(Uri.LIF, container)
    }

    void score(String name, WindowScorerI scorer) {
        URL url = ScoringTests.getResource('/test1.lif')
        assertNotNull('Resource not found.', url)
        Data data = Serializer.parse(url.text, Data)
        data = score(name, scorer, data)
        println data.asPrettyJson()
    }

    @Test
    void testBrevityScorer() {
        score('brevity', new BrevityScorer())
    }

    @Test
    void testOffsetScorer() {
        score('offset', new OffsetScorer())
    }

    @Test
    void testMatchRecallScorer() {
        score('match', new MatchRecallScorer())
    }

    @Test
    void testTermRecallScorer() {
        score('term', new MatchRecallScorer())
    }
}
