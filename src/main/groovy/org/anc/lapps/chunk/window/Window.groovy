package org.anc.lapps.chunk.window

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by krim on 8/16/2016.
 * Class representing a "windows" as a candidate for a meaningful passage
 * Need to be specified with 'start', 'end', and 'text' values
 * , as well as keytermMatchLimit, only if necessary
 */
class Window {

    int start // inclusive
    int end // exclusive
    int keytermMatchLimit
    def keytermMatches
    def keytermContains
    String text

    def init() {
        this.keytermMatchLimit = Double.POSITIVE_INFINITY
        this.keytermMatches = [:]
        this.keytermContains = [:]
    }

    Window(start, end, text) {
        init()
        this.start = start
        this.end = end
        this.text = text
    }

    Window(int start, int end, String text, int keytermMatchLimit) {
        this(start, end, text)
        this.keytermMatchLimit = keytermMatchLimit
    }

    Window(int start, int end, String text, List<String> keyterms) {
        this(start, end, text)
        this.matches(keyterms)
        this.contains(keyterms)
    }

    Window(int start, int end, String text, List<String> keyterms, int keytermMatchLimit) {
        this(start, end, text, keytermMatchLimit)
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

}
