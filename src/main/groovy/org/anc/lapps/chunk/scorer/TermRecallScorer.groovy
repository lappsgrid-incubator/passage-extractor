package org.anc.lapps.chunk.scorer

/**
 * Created by krim on 8/14/2016.
 */
class TermRecallScorer implements WindowScorerI {
    @Override
    double scoreWindow(int begin, int end, int matchesFound, int totalMatches, int keytermsFound, int totalKeyterms, int textSize) {
        return (double)keytermsFound / (double)totalKeyterms;
    }
}
