package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window

/**
 * Created by krim on 8/14/2016.
 */
interface WindowScorerI {

    /**
     * @return
     */
    public double scoreWindow(Window window, Window document);

}
