package de.dimaki.refuel.appcast.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.appcast.entity.Channel;
import de.dimaki.refuel.appcast.entity.Enclosure;
import de.dimaki.refuel.appcast.entity.Item;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastManagerTest {

    AppcastManager manager = new AppcastManager();

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
            manager.fetch("http://thisisanotexistingdomainnamethatproducesanerror.com/error");
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
        Client clientMock = mock(Client.class);
        WebTarget targetMock = mock (WebTarget.class);
        Invocation.Builder b = mock(Invocation.Builder.class);
        when(b.get(any(Class.class))).thenReturn(Response.status(404).build());
        when(targetMock.request(any(MediaType.class))).thenReturn(b);
        when(clientMock.target(any(String.class))).thenReturn(targetMock);
        manager.client = clientMock;
        try {

            manager.fetch("http://dummy.com");
        } catch (AppcastException ex) {
            // OK
        } catch (Exception e) {
            fail(e.toString());
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
