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

    void score(WindowScorerI scorer) {
        URL url = ScoringTests.getResource('/test1.lif')
        assertNotNull('Resource not found.', url)
        Data data = Serializer.parse(url.text, Data)
        assertEquals(Uri.LIF, data.discriminator)
        Container container = new Container(data.payload)
        List<View> views = container.findViewsThatContain('Window')
        assertEquals(1, views.size())

        View view = views[-1]
        List<Annotation> annotations = view.annotations
        assertEquals(2, annotations.size())

//        List<String> keyterms = view.metadata.keyterms
        Window document = new Window(0, container.text.length(), container.text, view.metadata.keyterms ?: [])

        List<Window> windows = []
        annotations.each { Annotation a ->
            List terms = a.features.matches.collect { it.term }
            windows.add(new Window((int)a.start, (int)a.end, a.features.text, terms))
        }

//        BrevityScorer scorer = new BrevityScorer()
        windows.each { Window window ->
            println scorer.scoreWindow(window, document)
        }
    }

    @Test
    void testBrevityScorer() {
        score(new BrevityScorer())
    }

    @Test
    void testOffsetScorer() {
        score(new OffsetScorer())
    }

    @Test
    void testMatchRecallScorer() {
        score(new MatchRecallScorer())
    }

    @Test
    void testTermRecallScorer() {
        score(new MatchRecallScorer())
    }
}
