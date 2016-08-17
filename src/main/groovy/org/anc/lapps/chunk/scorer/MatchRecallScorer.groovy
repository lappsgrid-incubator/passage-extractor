package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.WindowsExtractor.Window

/**
 * Created by krim on 8/14/2016.
 */
class MatchRecallScorer implements WindowScorerI {
    @Override
    double scoreWindow(Window window, Window document) {
        return (double) window.totalMatches() / (double) document.totalMatches()
    }
}
