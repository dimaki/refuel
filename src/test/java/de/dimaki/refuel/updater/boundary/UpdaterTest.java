package de.dimaki.refuel.updater.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import de.dimaki.refuel.appcast.boundary.AppcastManager;
import de.dimaki.refuel.appcast.boundary.AppcastManagerTest;
import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.appcast.entity.Channel;
import de.dimaki.refuel.appcast.entity.Enclosure;
import de.dimaki.refuel.appcast.entity.Item;
import de.dimaki.refuel.updater.entity.ApplicationStatus;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Dino Tsoumakis
 */
public class UpdaterTest {

    Updater updater;

    @Before
    public void init() throws IOException, AppcastException, Exception {
        updater = new Updater();
        AppcastManager appcastManagerMock = mock(AppcastManager.class);
        Appcast appcast = new Appcast();
        Channel c = new Channel();
        Item i = new Item();
        Enclosure e = new Enclosure();
        e.setVersion("2.0.4711");
        i.setEnclosure(e);
        List<Item> items = new ArrayList<>();
        items.add(i);
        c.setItems(items);
        appcast.setChannel(c);

        when(appcastManagerMock.fetch(any(URL.class), any(Proxy.class), anyInt(), anyInt())).thenReturn(appcast);
        when(appcastManagerMock.getLatestVersion(any(URL.class), any(Proxy.class), anyInt(), anyInt())).thenReturn("2.0.4711");
        when(appcastManagerMock.download(any(Appcast.class), any(Path.class))).thenCallRealMethod();
        updater.appcastManager = appcastManagerMock;
    }

    @Test
    public void testGetApplicationStatus() {
        try {
            ApplicationStatus applicationStatus = updater.getApplicationStatus("2.0.1044", new URL("http://TESTURL"));
            assertEquals(ApplicationStatus.UPDATE_AVAILABLE, applicationStatus);
            assertEquals("2.0.4711", applicationStatus.getInfo());
            assertNotNull(applicationStatus.getUpdateTime());

            applicationStatus = updater.getApplicationStatus("2.0.4711", new URL("http://TESTURL"));
            assertEquals(ApplicationStatus.OK, applicationStatus);
            assertNotNull(applicationStatus.getUpdateTime());

            applicationStatus = updater.getApplicationStatus("2.2.4711", new URL("http://TESTURL"));
            assertEquals(ApplicationStatus.OK, applicationStatus);
            assertNotNull(applicationStatus.getUpdateTime());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }
    }

    @Test
    public void testGetApplicationStatusFailure() {
        try {
            Updater cut = new Updater();
            cut.appcastManager = new AppcastManager();
            ApplicationStatus applicationStatus = cut.getApplicationStatus("2.0.1044",
                    new URL("http://thisisanotexistingdomainnamethatproducesanerror.com/error"));
            assertEquals(ApplicationStatus.FAILURE, applicationStatus);
            assertNotNull(applicationStatus.getInfo());
            System.out.println("Got Info: " + applicationStatus.getInfo());
        } catch (MalformedURLException | JAXBException ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testUpdate() throws Exception {
        try {
            AppcastManagerTest test = new AppcastManagerTest();
            Appcast appcast = test.getAppcast();
            appcast.getChannel().getItems().get(0).getEnclosure().setUrl(getClass().getResource("/jartest.zip").toURI().toURL().toString());
            appcast.getChannel().getItems().get(0).getEnclosure().setLength(1505);
            Path createdTempDirectory = null;
            try {
                createdTempDirectory = Files.createTempDirectory("ac-");
            } catch (IOException ex) {
                fail(ex.toString());
            }
            try {
                updater.update(appcast, createdTempDirectory);
            } finally {
                try { Files.deleteIfExists(createdTempDirectory); } catch (Exception e) { /* Ignore */ }
            }
        } catch (Exception exception) {
            fail("Could not update: " + exception.toString());
        }
    }

}
