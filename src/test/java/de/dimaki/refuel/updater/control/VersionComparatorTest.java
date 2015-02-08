package de.dimaki.refuel.updater.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Dino Tsoumakis
 */
public class VersionComparatorTest {

    VersionComparator vc = new VersionComparator();

    @Test
    public void testCompareNewRemote() {
        String localVersion = "1.9.1234";
        String remoteVersion = "2.0.4711";
        int compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        localVersion = "1.9.5678";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        // local : 2.0.1234
        localVersion = "2.0.1234";

        remoteVersion = "2.0.4711";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "2.1.777";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "2.1.1234";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "2.1.4711";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "3.0.777";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "3.0.4711";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "2.1.0-SNAPSHOT";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        // local: 2.1.0-SNAPSHOT
        localVersion = "2.1.0-SNAPSHOT";

        remoteVersion = "2.1.0";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);

        remoteVersion = "2.1.1";
        compare = vc.compare(localVersion, remoteVersion);
        assertTrue(localVersion + " < " + remoteVersion, compare < 0);
    }

    @Test
    public void testCompareOldRemote() {
        String localVersion = "2.0.4711";
        String remoteVersion = "1.9.777";
        int compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " > " + remoteVersion, 1, compare);

        remoteVersion = "1.9.7777";
        compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " > " + remoteVersion, 1, compare);

        remoteVersion = "2.0.4710";
        compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " > " + remoteVersion, 1, compare);

        remoteVersion = "2.0.4711-SNAPSHOT";
        compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " > " + remoteVersion, 1, compare);
    }

    @Test
    public void testCompareSameVersion() {
        String localVersion = "2.0.4711";
        String remoteVersion = "2.0.4711";
        int compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " == " + remoteVersion, 0, compare);
    }

    @Test
    public void testCompareDifferentLength() {
        String localVersion = "2.0.4";
        String remoteVersion = "2.0.4.1";
        int compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " < " + remoteVersion, -1, compare);

        localVersion = "2.0.4.1";
        remoteVersion = "2.0.4";
        compare = vc.compare(localVersion, remoteVersion);
        assertEquals(localVersion + " > " + remoteVersion, 1, compare);
    }
}
