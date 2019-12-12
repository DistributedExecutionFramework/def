package at.enfilo.def.common.util.environment;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionMatcherTest {

    @Test
    public void matchVersion() {
        assertTrue(VersionMatcher.matchVersion("1.8", null));
        assertTrue(VersionMatcher.matchVersion("1.8", ""));
        assertTrue(VersionMatcher.matchVersion(null, ""));
        assertTrue(VersionMatcher.matchVersion(null, null));
        assertTrue(VersionMatcher.matchVersion("", ""));
        assertFalse(VersionMatcher.matchVersion(null, "1.8"));
        assertFalse(VersionMatcher.matchVersion("", "1.8"));

        assertTrue(VersionMatcher.matchVersion("1.8.181", "1.8.181"));
        assertTrue(VersionMatcher.matchVersion("1.8.181", "1.8"));
        assertFalse(VersionMatcher.matchVersion("1.8.181", "1.8.182"));
        assertFalse(VersionMatcher.matchVersion("1.8.181", "1.9"));
        assertFalse(VersionMatcher.matchVersion("1.8.181", "1.8.181.1"));

        assertTrue(VersionMatcher.matchVersion("1.9", ">1.9"));
        assertTrue(VersionMatcher.matchVersion("1.10", ">1.9"));
        assertTrue(VersionMatcher.matchVersion("1.9", ">1"));
        assertTrue(VersionMatcher.matchVersion("1.9.1.1", ">1.9.1"));
        assertTrue(VersionMatcher.matchVersion("1.10", ">1.9.1"));
        assertFalse(VersionMatcher.matchVersion("1.8", ">1.9"));
        assertFalse(VersionMatcher.matchVersion("0.9", ">1"));
        assertFalse(VersionMatcher.matchVersion("1", ">1.9.1"));
        assertFalse(VersionMatcher.matchVersion("1.9", ">1.9.1"));

        assertTrue(VersionMatcher.matchVersion("1.9", "<1.9"));
        assertTrue(VersionMatcher.matchVersion("1.8", "<1.9"));
        assertTrue(VersionMatcher.matchVersion("0.9", "<1"));
        assertTrue(VersionMatcher.matchVersion("1", "<1.9.1"));
        assertTrue(VersionMatcher.matchVersion("1.9", "<1.9.1"));
        assertFalse(VersionMatcher.matchVersion("1.10", "<1.9"));
        assertFalse(VersionMatcher.matchVersion("1.9", "<1"));
        assertFalse(VersionMatcher.matchVersion("1.9.1.1", "<1.9.1"));
        assertFalse(VersionMatcher.matchVersion("1.10", "<1.9.1"));

        assertTrue(VersionMatcher.matchVersion("1.8", "1.8-1.9"));
        assertTrue(VersionMatcher.matchVersion("1.9", "1.8-1.9"));
        assertTrue(VersionMatcher.matchVersion("1.5", "1.1.2-2"));
        assertTrue(VersionMatcher.matchVersion("1.1", "1-2"));
        assertFalse(VersionMatcher.matchVersion("1.10", "1.8-1.9"));
        assertFalse(VersionMatcher.matchVersion("2.2", "1-2"));
        assertFalse(VersionMatcher.matchVersion("0.9", "1-2"));
        assertFalse(VersionMatcher.matchVersion("1", "1.1.2-2"));
        assertFalse(VersionMatcher.matchVersion("0.9", "1.1.2-2"));

        assertTrue(VersionMatcher.matchVersion("1.7", "1.7,1.8,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.8", "1.7,1.8,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.9", "1.7,1.8,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.7.1", "1.7,1.8,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.9", "<1.7,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.7", "<1.7,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.4.1", "<1.7,1.9"));
        assertTrue(VersionMatcher.matchVersion("1.4", "<1.7,>1.9"));
        assertTrue(VersionMatcher.matchVersion("2.0", "<1.7,>1.9"));
        assertTrue(VersionMatcher.matchVersion("2.1.1", "1.4-1.9,>2.1"));
        assertTrue(VersionMatcher.matchVersion("1.5", "1.4-1.9,>2.1"));
        assertFalse(VersionMatcher.matchVersion("2.0", "1.7,1.8,1.9"));
        assertFalse(VersionMatcher.matchVersion("1", "1.7,1.8,1.9"));
        assertFalse(VersionMatcher.matchVersion("1.8", "<1.7,1.9"));
        assertFalse(VersionMatcher.matchVersion("2.0", "<1.7,1.9"));
        assertFalse(VersionMatcher.matchVersion("1.8", "<1.7,>1.9"));
        assertFalse(VersionMatcher.matchVersion("1.3", "1.4-1.9,>2.1"));
        assertFalse(VersionMatcher.matchVersion("2.0", "1.4-1.9,>2.1"));
    }

    @Test
    public void matchExact() {
        assertTrue(VersionMatcher.matchExact("1.8.181", "1.8.181"));
        assertTrue(VersionMatcher.matchExact("1.8.181", "1.8"));
        assertFalse(VersionMatcher.matchExact("1.8.181", "1.8.182"));
        assertFalse(VersionMatcher.matchExact("1.8.181", "1.9"));
        assertFalse(VersionMatcher.matchExact("1.8.181", "1.8.181.1"));
    }

    @Test
    public void matchHigher() {
        assertTrue(VersionMatcher.matchHigher("1.9", "1.9"));
        assertTrue(VersionMatcher.matchHigher("1.10", "1.9"));
        assertTrue(VersionMatcher.matchHigher("1.9", "1"));
        assertTrue(VersionMatcher.matchHigher("1.9.1.1", "1.9.1"));
        assertTrue(VersionMatcher.matchHigher("1.10", "1.9.1"));
        assertFalse(VersionMatcher.matchHigher("1.8", "1.9"));
        assertFalse(VersionMatcher.matchHigher("0.9", "1"));
        assertFalse(VersionMatcher.matchHigher("1", "1.9.1"));
        assertFalse(VersionMatcher.matchHigher("1.9", "1.9.1"));
    }

    @Test
    public void matchLower() {
        assertTrue(VersionMatcher.matchLower("1.9", "1.9"));
        assertTrue(VersionMatcher.matchLower("1.8", "1.9"));
        assertTrue(VersionMatcher.matchLower("0.9", "1"));
        assertTrue(VersionMatcher.matchLower("1", "1.9.1"));
        assertTrue(VersionMatcher.matchLower("1.9", "1.9.1"));
        assertFalse(VersionMatcher.matchLower("1.10", "1.9"));
        assertFalse(VersionMatcher.matchLower("1.9", "1"));
        assertFalse(VersionMatcher.matchLower("1.9.1.1", "1.9.1"));
        assertFalse(VersionMatcher.matchLower("1.10", "1.9.1"));
    }

    @Test
    public void matchInterval() {
        assertTrue(VersionMatcher.matchVersion("1.8", "1.8-1.9"));
        assertTrue(VersionMatcher.matchVersion("1.9", "1.8-1.9"));
        assertTrue(VersionMatcher.matchVersion("1.5", "1.1.2-2"));
        assertTrue(VersionMatcher.matchVersion("1.1", "1-2"));
        assertFalse(VersionMatcher.matchVersion("1.10", "1.8-1.9"));
        assertFalse(VersionMatcher.matchVersion("2.2", "1-2"));
        assertFalse(VersionMatcher.matchVersion("0.9", "1-2"));
        assertFalse(VersionMatcher.matchVersion("1", "1.1.2-2"));
        assertFalse(VersionMatcher.matchVersion("0.9", "1.1.2-2"));
    }
}

