package de.dimaki.refuel.updater.control;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Dino Tsoumakis
 */
public class ZipHandlerTest {

    @Test
    public void testUnzip() {
        Set<Path> unzip = null;
        try {
            URL url = getClass().getResource("/jartest.zip");
            Path zipFile = Paths.get(url.toURI());
            Path dest = Paths.get(System.getProperty("java.io.tmpdir"));
            unzip = ZipHandler.unzip(zipFile, dest, false);
            System.out.println("Unzipped files: " + unzip);

            assertNotNull(unzip);
            Assert.assertTrue(unzip.size() > 0);

            boolean found = false;
            for (Path path : unzip) {
                if (path.endsWith("jartest.jar")) {
                    found = true;
                }
            }
            Assert.assertTrue(found);

        } catch (Exception exception) {
            fail(exception.toString());
        } finally {
            for (Path path : unzip) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException iOException) {
                    // Ignore
                }
            }
        }
    }

}
