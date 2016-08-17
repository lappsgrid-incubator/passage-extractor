package org.anc.lapps.chunk.WindowsExtractor

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by krim on 8/16/2016.
 * Class representing a "windows" as a candidate for a meaningful passage
 */
class Window {

    int start
    int end
    int keytermMatchLimit = Double.POSITIVE_INFINITY
    def keytermMatches = [:]
    def keytermContains = [:]
    String text

    def length() {
        end - start
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
