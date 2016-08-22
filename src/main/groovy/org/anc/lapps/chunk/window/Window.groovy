package org.anc.lapps.chunk.window

import org.lappsgrid.serialization.lif.Annotation

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.anc.lapps.chunk.WindowExtractorService.WINDOW

/**
 * Created by krim on 8/16/2016.
 * Class representing a "windows" as a candidate for a meaningful passage
 * Need to be specified with 'start', 'end', and 'text' values
 * , as well as keytermMatchLimit, only if necessary
 */
class Window {

    int start // inclusive
    int end   // exclusive
    int keytermMatchLimit
    def keytermMatches
    def keytermContains
    def passages
    def originalAnnotationId
    double score
    String text

    def init() {
        this.keytermMatchLimit = Double.POSITIVE_INFINITY
        this.keytermMatches = [:].withDefault {0}
        this.keytermContains = [:]
        this.passages = []
    }

    Window(start, end, text, originalId) {
        init()
        this.start = start
        this.end = end
        this.text = text
        this.originalAnnotationId = originalId
    }

    Window(int start, int end, String text, String originalId, int keytermMatchLimit) {
        this(start, end, text, originalId)
        this.keytermMatchLimit = keytermMatchLimit
    }

    Window(int start, int end, String text, String originalId, List<String> keyterms) {
        this(start, end, text, originalId)
        this.matches(keyterms)
        this.contains(keyterms)
    }

    Window(int start, int end, String text, String originalId,
           List<String> keyterms, int keytermMatchLimit) {
        this(start, end, text, originalId, keytermMatchLimit)
        this.matches(keyterms)
        this.contains(keyterms)
    }

    def length() {
        end - start + 1
    }

    def matches(String keyterm) {
        if (!keytermMatches.keySet().contains(keyterm)) {
            def matchesFound = 0
            Matcher m = Pattern.compile(keyterm).matcher(text);
            while (m.find() && matchesFound < keytermMatchLimit) {
                this.passages.add(new Passage(term: keyterm, start: m.start(), end: m.end()))
                matchesFound++
            }
            keytermMatches[keyterm] = matchesFound
        }
        return keytermMatches[keyterm]
    }

    def matches(List<String> keyterms) {
        def totalMatches = 0
        keyterms.each {
            keyterm -> totalMatches += matches(keyterm)
        }
        return totalMatches
    }

    def totalMatches() {
        return keytermMatches.values().sum()
    }

    def totalContains() {
        return keytermContains.values().sum()
    }

    def contains(String keyterm) {
        if (!keytermContains.keySet().contains(keyterm)) {
            keytermContains[keyterm] = text.contains(keyterm) ? 1 : 0
       }
        return keytermContains[keyterm]
    }

    def contains(List<String> keyterms) {
        def totalContains = 0
        keyterms.each {
            keyterm -> totalContains += contains(keyterm)
        }
        return totalContains
    }

    Annotation toAnnotation(String id) {
        Annotation window = new Annotation()
        window.id = id
        window.setAtType(WINDOW)
        window.setStart(start)
        window.setEnd(end)
        window.features.matches = this.passages
        window.features.text = this.text
        window.features.id = this.originalAnnotationId
        if (this.score != null && this.score >= 0) window.features.score = this.score
        return window
    }

    class Passage {
        String term
        int start
        int end
    }

}
