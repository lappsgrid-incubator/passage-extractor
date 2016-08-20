package org.anc.lapps.chunk.window.extraction

import org.anc.lapps.chunk.window.Window
import org.lappsgrid.serialization.lif.Container

/**
 * Created by krim on 8/16/2016.
 */
class MatchBasedWindow extends AbstractWindowExtraction {

    MatchBasedWindow(Map params) {
        super(params)
    }

    @Override
    List<Window> extract(Container payload, List<String> keyterms) {
        return null
    }
}
