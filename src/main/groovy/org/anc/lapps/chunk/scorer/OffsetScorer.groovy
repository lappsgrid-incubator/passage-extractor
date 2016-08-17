package org.anc.lapps.chunk.scorer

/**
 * Created by krim on 8/16/2016.
 */
class OffsetScorer implements WindowScorerI {
    @Override
    double scoreWindow(int begin, int end, int matchesFound, int totalMatches, int keytermsFound, int totalKeyterms, int textSize) {
        return ( (double)textSize - (double)begin ) / (double)textSize;
    }
}
