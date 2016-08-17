package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.WindowsExtractor.Window

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
    public double scoreWindow(Window window, Window document);

}
