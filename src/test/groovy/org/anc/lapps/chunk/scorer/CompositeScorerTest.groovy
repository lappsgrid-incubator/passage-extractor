package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window
import org.junit.*

import static org.junit.Assert.*;

/**
 * Created by krim on 8/17/2016.
 */
class CompositeScorerTest {

    CompositeScorer scorer

    @Before
    void setUp() {
        scorer = new CompositeScorer("brevity: 0.10, offset: 0.1, matchrecall: 0.40, termrecall: 0.4")
    }

    @Test
    void canUseLambdasFromParametrizedString() {
        println scorer.toString()
    }

    @Ignore
    @Test
    void canManuallyAddScorers() {
        WindowScorerI brevity = { window, document -> return 0.5d }
        WindowScorerI offset = { window, document -> return 0.7d }
        WindowScorerI recall = { window, document -> return 0.1d }

        CompositeScorer scorer = new CompositeScorer()
        scorer.add(brevity, 0.33)
        scorer.add(offset, 0.33)
        scorer.add(recall, 0.34)

        double score = scorer.scoreWindow((Window)null, (Window)null)
        assertEquals(0.43d, score, 0.0001)
        println "Score is $score"
        println scorer.toString()
    }
}
