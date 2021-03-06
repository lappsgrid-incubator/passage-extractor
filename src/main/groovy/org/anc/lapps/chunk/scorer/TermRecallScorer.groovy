package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window

/**
 * Created by krim on 8/14/2016.
 */
class TermRecallScorer implements WindowScorerI {
    @Override
    double scoreWindow(Window window, Window document) {
        double score = (double) window.totalContains() / (double) document.totalContains()
        window.score = score
        return score
    }
}
