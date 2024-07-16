package ratismal.drivebackup.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void isAfter() {
        Version v1 = Version.parse("1.2.3");
        Version v4 = Version.parse("4.2.0");
        assertTrue(v4.isAfter(v1));
        assertFalse(v1.isAfter(v4));
    }

    @Test
    void testToString() {
        assertEquals("1.2.3", Version.parse("1.2.3").toString());
    }
}