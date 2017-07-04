package org.anc.lapps.chunk.window.extraction

import org.anc.lapps.chunk.window.Window
import org.lappsgrid.serialization.lif.Container

/**
 * Created by krim on 8/17/2016.
 * This will take a 'Data' (lif) object and pull out first N windows
 * as candidates for passages as user requests, subjects for later computation of scores
 */
abstract class AbstractWindowExtraction {

    Map params  // this is passed when being instantiated
                // includes all the information to optimize extraction and scoring

    AbstractWindowExtraction(Map params) {
        this.params = params
    }

    abstract List<Window> extract(Container payload,
                                  List<String> keyterms)
}
