package org.anc.lapps.chunk.window.extraction

import org.anc.lapps.chunk.window.Window
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

/**
 * Created by krim on 8/16/2016.
 */
class AnnTypeBasedWindow extends AbstractWindowExtraction {

    int sizeLimit
    int numLimit
    int matchLimit

    AnnTypeBasedWindow(Map params) {
        super(params)
        sizeLimit = params.sizelimit ?: Double.POSITIVE_INFINITY
        numLimit = params.numlimit ?: Double.POSITIVE_INFINITY
        matchLimit =  params.matchlimit ?: Double.POSITIVE_INFINITY

    }

    @Override
    List<Window> extract(Container payload, List<String> keyterms) {
        String annotationType = this.params.annotation

        List<View> views = payload.findViewsThatContain(annotationType)
        if (views == null || views.size() == 0) {
            return Collections.emptyList()
        }

        String text = payload.text
        List<Window> extracted = []

        // Get the last view that contains the annotation type and iterate over each annotation
        // and find each span that contains the keyword.
        View view = views[-1]
        for (Annotation a in view.annotations) {
            if (a.atType == annotationType) {
                int start = (int) a.start
                int end = (int) a.end

                if ((end - start) > sizeLimit) {
                    continue
                }
                String covered = text.substring(start, end)
                Window extraction = new Window(start, end,
                        covered, a.getId(), keyterms, matchLimit)
                if (extraction.totalContains() > 0) {
                    extracted.add(extraction)
                }
                if (extracted.size() >= numLimit) {
                    break
                }
            }
        }
        return extracted
    }
}
