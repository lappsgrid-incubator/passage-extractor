package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.WindowsExtractor.Window

/**
 * Created by krim on 8/16/2016.
 */
class OffsetScorer implements WindowScorerI {
    @Override
    double scoreWindow(Window window, Window document) {
        return ( (double) document.length() - (double) window.start ) /
                (double) document.length()
    }
}
