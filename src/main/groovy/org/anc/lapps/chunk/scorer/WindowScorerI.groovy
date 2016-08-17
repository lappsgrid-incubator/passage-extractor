package org.anc.lapps.chunk.scorer

/**
 * Created by krim on 8/14/2016.
 */
interface WindowScorerI {

    /**
     * @param begin : window start offset
     * @param end : window end offset
     * @param matchesFound : # all occurrences of kws in the window
     * @param totalMatches : # all occurrences of kws in the document
     * @param keytermsFound : # occurred kws in the window (single count)
     * @param totalKeyterms : # occurred kws in the document (single count)
     * @param textSize : doc size
     * @return
     */
    public double scoreWindow(int begin,
                              int end,
                              int matchesFound,
                              int totalMatches,
                              int keytermsFound,
                              int totalKeyterms,
                              int textSize );

}
