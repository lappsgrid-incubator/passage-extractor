package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window

/**
 * Created by krim on 8/14/2016.
 */
class BrevityScorer implements WindowScorerI {
    @Override
    double scoreWindow(Window window, Window document) {
        int windowSize = window.length()
        double score = 1 - ((double) windowSize / (double) document.length());
        window.score = score
        return score
    }
}
