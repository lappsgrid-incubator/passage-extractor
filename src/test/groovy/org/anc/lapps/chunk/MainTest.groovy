package org.anc.lapps.chunk

import org.junit.*
import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
class MainTest {

    PrintStream console
    ByteArrayOutputStream stream

    @Before
    void setup() {
        console = System.out
        stream = new ByteArrayOutputStream()
        System.out = new PrintStream(stream)
    }

    @After
    void teardown() {
        System.out = console
        stream = null
    }

    @Test
    void testVersion() {
        String expected = "Lappsgrid Passage Extractor v${Version.getVersion()}"
        PassageExtractor.main(['-v'] as String[])
        String output = stream.toString()
        assertTrue output.contains(expected)
        assertTrue output.contains('Copyright')
        assertTrue(output.contains('American National Corpus. All rights reserved.'))
        println 'test passes'
    }

    @Test
    void testHelp() {
        PassageExtractor.main(['-h'] as String[])
        String out = stream.toString()
        println out
    }


    void println(String message) {
        console.println(message)
    }
}
