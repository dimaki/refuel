package de.dimaki.refuel.appcast.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.appcast.entity.Channel;
import de.dimaki.refuel.appcast.entity.Enclosure;
import de.dimaki.refuel.appcast.entity.Item;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.junit.Ignore;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastManagerTest {

    AppcastManager manager;

    public AppcastManagerTest() throws JAXBException {
        this.manager = new AppcastManager();
    }

    @Test
    public void testFetch() {
        Appcast appcast = getAppcast();

        assertNotNull(appcast);
        assertEquals("2.0", appcast.getVersion());

        Channel channel = appcast.getChannel();
        assertNotNull(channel);
        assertNotNull(channel.getLink());
        assertEquals("en", channel.getLanguage());

        List<Item> items = channel.getItems();
        assertNotNull(items);
        assertTrue(items.size() > 0);

        Item item = items.get(0);
        assertNotNull(item);
        assertNotNull(item.getTitle());
        assertNotNull(item.getReleaseNotesLink());
        assertTrue(item.getReleaseNotesLink().contains("notes.html"));

        Enclosure enclosure = item.getEnclosure();
        assertNotNull(enclosure);
        assertEquals("2.0.4711", enclosure.getVersion());
        assertTrue(enclosure.getLength() > 0);
        assertNotNull(enclosure.getUrl());
    }

    @Test
    public void testFetchNoConnection() {
        try {
            manager.fetch(new URL("http://thisisanotexistingdomainnamethatproducesanerror.com/error"));
        } catch (AppcastException ex) {
            if (ex.getStatus() != 404) {
                fail(ex.toString());
            }
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void testFetchError() {
        try {
            manager.fetch(new URL("http://dummy.com"));
        } catch (AppcastException ex) {
            // OK
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    @Ignore
    public void testFetchRealHTTP() {
        Appcast appcast = null;
        try {
            appcast = manager.fetch(new URL("http://example.com/test/appcast.xml"));
        } catch (AppcastException | MalformedURLException ex) {
            fail(ex.toString());
        }
        assertNotNull(appcast);
    }

    @Test
    public void testDownload() throws Exception {
        Appcast appcast = getAppcast();
        appcast.getChannel().getItems().get(0).getEnclosure().setUrl(getClass().getResource("/jartest.zip").toURI().toURL().toString());
        Path createdTempDirectory = null;
        try {
            createdTempDirectory = Files.createTempDirectory("ac-");
        } catch (IOException ex) {
            fail(ex.toString());
        }
        Path downloaded = null;
        try {
            downloaded = manager.download(appcast, createdTempDirectory);
            assertNotNull(downloaded);
            System.out.println("Downloaded: " + downloaded);
        } catch (Exception e) {
            fail("Could not download update package: " + e.toString());
        } finally {
            try {
                Files.deleteIfExists(downloaded);
            } catch (Exception exception) {
            }
            try {
                Files.deleteIfExists(createdTempDirectory);
            } catch (IOException iOException) {
            }
        }
    }

    @Test
    public void testDownloadHttpUrl() {
        Appcast appcast = getAppcast();
        appcast.getChannel().getItems().get(0).getEnclosure().setUrl("http://dimaki.de/test/jartest.zip");
        Path createdTempDirectory = null;
        try {
            createdTempDirectory = Files.createTempDirectory("ac-");
        } catch (IOException ex) {
            fail(ex.toString());
        }
        Path downloaded = null;
        try {
            downloaded = manager.download(appcast, createdTempDirectory);
        } catch (FileNotFoundException fnfe) {
            /* That is ok here. We only want to check the URL parsing part */
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            try {
                Files.deleteIfExists(downloaded);
            } catch (Exception exception) {
            }
            try {
                Files.deleteIfExists(createdTempDirectory);
            } catch (IOException iOException) {
            }
        }
    }

    public Appcast getAppcast() {
        Appcast appcast = null;
        try {
            JAXBContext ctx = JAXBContext.newInstance(Appcast.class);
            Unmarshaller um = ctx.createUnmarshaller();
            appcast = (Appcast) um.unmarshal(getClass().getResource("/appcast.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to parse appcast: " + ex);
        }
        return appcast;
    }
}
