package org.anc.lapps.chunk.window

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by krim on 8/17/2016.
 */
public class WindowTest {

    Window window;
    Window document;

    @Before
    void setUp() throws Exception {
        window = new Window(10, 19, 'ab' * 5, 'oid')
        document = new Window(0, 99, 'a' * 100, 'odoc')
    }

    @After
    void tearDown() throws Exception {
        window = null
        document = null
    }

    @Test
    public void length() throws Exception {
        assertEquals(10, window.length())
    }

    @Test
    public void matchesSingle() throws Exception {
        assertEquals(5, window.matches('a'))

    }

    @Test
    public void matchesSingleWithLimit() throws Exception {
        window.keytermMatchLimit = 3
        assertEquals(3, window.matches('a'))
    }

    @Test
    public void matchesList() throws Exception {
        assertEquals(10, window.matches(['a', 'b']))

    }

    @Test
    public void totalMatches() throws Exception {
        window = new Window(10, 19, 'ab' * 5, 'oid', ['a', 'b'])
        assertEquals(10, window.totalMatches())

    }

    @Test
    public void totalContains() throws Exception {
        window = new Window(10, 19, 'ab' * 5, 'oid', ['a', 'b'])
        assertEquals(2, window.totalContains())
    }

    @Test
    public void containsSingle() throws Exception {
        assertEquals(1, window.contains('a'))
    }

    @Test
    public void containsList() throws Exception {
        assertEquals(2, window.contains(['a', 'b']))
    }

}