package org.anc.lapps.chunk.scoring

import org.anc.lapps.chunk.scorer.CompositeScorer
import org.anc.lapps.chunk.scorer.WindowScorerI
import org.anc.lapps.chunk.window.Window
import org.junit.Test

/**
 * @author Keith Suderman
 */
class CompositeScorerTest {

    @Test
    void exampleTest() {
        WindowScorerI brevity = { window, document -> return 0.5d }
        WindowScorerI offset = { window, document -> return 0.7d }
        WindowScorerI recall = { window, document -> return 0.1d }

        CompositeScorer scorer = new CompositeScorer()
        scorer.add(brevity, 0.33)
        scorer.add(offset, 0.33)
        scorer.add(recall, 0.34)

        double score = scorer.scoreWindow((Window)null, (Window)null)
        println "Score is $score"
    }

}
