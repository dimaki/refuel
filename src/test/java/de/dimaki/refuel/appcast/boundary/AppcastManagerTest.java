package de.dimaki.refuel.appcast.boundary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.appcast.entity.Channel;
import de.dimaki.refuel.appcast.entity.Enclosure;
import de.dimaki.refuel.appcast.entity.Item;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastManagerTest {
    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "8887";

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
            manager.fetch(new URL("http://thisisanotexistingdomainnamethatproducesanerror.com/error"),
                    Proxy.NO_PROXY,
                    AppcastManager.DEFAULT_CONNECT_TIMEOUT,
                    AppcastManager.DEFAULT_READ_TIMEOUT);
        } catch (AppcastException ex) {
            assertEquals(404, ex.getStatus());
            System.out.println("Status Info: " + ex.getStatusInfo());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testFetchWithProxy() {
        String protocolPrefix = "https.";

        // Remember current settings
        String oldProxyHost = System.getProperty(protocolPrefix + "proxyHost", null);
        String oldProxyPort = System.getProperty(protocolPrefix + "proxyPort", null);

        System.setProperty(protocolPrefix + "proxyHost", PROXY_HOST);
        System.setProperty(protocolPrefix + "proxyPort", PROXY_PORT);

        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(8887)
                .start();

        try {
            Appcast appcast = manager.fetch(new URL("https://drive.google.com/uc?export=download&id=0BxjtsbG95NcHX1VxaUpVVjFsN2M"));
            assertNotNull(appcast);
            System.out.println("Got latest online version: " + appcast.getLatestVersion());
        } catch (MalformedURLException | AppcastException ex) {
            fail("Fetching failed: " + ex.toString());
        }

        if (oldProxyHost != null && !oldProxyHost.isEmpty()) {
            System.setProperty(protocolPrefix + "proxyHost", oldProxyHost);
        } else {
            System.clearProperty(protocolPrefix + "proxyHost");
        }
        if (oldProxyPort != null && !oldProxyPort.isEmpty()) {
            System.setProperty(protocolPrefix + "proxyPort", oldProxyHost);
        } else {
            System.clearProperty(protocolPrefix + "proxyPort");
        }

        server.stop();
    }

    @Test
    public void testFetchError() {
        try {
            manager.fetch(new URL("http://dummy.com"),
                    Proxy.NO_PROXY,
                    AppcastManager.DEFAULT_CONNECT_TIMEOUT,
                    AppcastManager.DEFAULT_READ_TIMEOUT);
        } catch (AppcastException ex) {
            assertEquals(404, ex.getStatus());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testFetchInvalidXml() {
        Appcast appcast = null;
        try {
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("User-Agent", "Refuel-Client");
            requestProperties.put("SampleProperty", "SampleValue");
            appcast = manager.fetch(new URL("https://drive.google.com/uc?export=download&id=0BxjtsbG95NcHZGxJTEEwR0VTWUU"),
                    Proxy.NO_PROXY,
                    AppcastManager.DEFAULT_CONNECT_TIMEOUT,
                    AppcastManager.DEFAULT_READ_TIMEOUT,
                    requestProperties);
        } catch (AppcastException ex) {
            assertEquals(404, ex.getStatus());
        } catch (MalformedURLException ex) {
            fail(ex.toString());
        }
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

    @Test
    public void testGetLatestVersion() {
        Appcast appcast = getAppcast();
        String latestVersion = appcast.getLatestVersion();
        System.out.println("Latest Version: " + latestVersion);
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
