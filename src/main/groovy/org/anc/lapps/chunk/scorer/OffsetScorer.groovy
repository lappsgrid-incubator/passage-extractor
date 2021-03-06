package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window

/**
 * Created by krim on 8/16/2016.
 */
class OffsetScorer implements WindowScorerI {
    @Override
    double scoreWindow(Window window, Window document) {
        double score =  ( (double) document.length() - (double) window.start ) /
                (double) document.length()
        window.score = score
        return score
    }
}
