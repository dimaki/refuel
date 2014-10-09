/*
 * Copyright 2014 Dino Tsoumakis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dimaki.refuel.appcast.boundary;

import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.appcast.entity.Enclosure;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastManager {

    private static final Logger logger = Logger.getLogger(AppcastManager.class.getName());
    public static final String MANIFEST_APPCAST_VERSION = "Appcast-Version";
    public static final String MANIFEST_APPCAST_URL = "Appcast-Url";

    Client client;

    public AppcastManager() {
        // Try to read username and password for authentication
        client = ClientBuilder.newClient(new ClientConfig());
    }

    /**
     * Fetch an appcast from the given URL
     *
     * @param url
     * @return
     * @throws AppcastException
     */
    public Appcast fetch(String url) throws AppcastException {
        WebTarget appcastResource = client.target(url);
        Response response = appcastResource.request(MediaType.APPLICATION_XML_TYPE).get(Response.class);
        if (response == null) {
            logger.log(Level.SEVERE, "Could not fetch appcast from URL ''{0}''", url);
            throw new AppcastException("Could not fetch appcast from URL", url, 500, null);
        }
        if (response.getStatus() != Status.OK.getStatusCode()) {
            logger.log(Level.SEVERE, "Could not fetch appcast from URL ''{0}'': {1} {2}", new Object[]{url, response.getStatus(), response.getStatusInfo()});
            throw new AppcastException("Could not fetch appcast from URL", url, response.getStatus(), response.getStatusInfo().getReasonPhrase());
        }
        // Got a valid response
        return response.readEntity(Appcast.class);
    }

    /**
     * Get the latest appcast version string from the given url
     *
     * @param url The appcast url
     * @return The version string
     * @throws AppcastException in case of an error
     */
    public String getLatestVersion(String url) throws AppcastException {
        String version = null;
        Appcast appcast = fetch(url);
        if (appcast != null) {
            version = appcast.getLatestVersion();
        }
        return version;
    }

    /**
     * Download the file from the given URL to the specified target
     *
     * @param appcast
     * @param targetDir
     * @return
     * @throws IOException
     */
    public Path download(Appcast appcast, Path targetDir) throws IOException, Exception {
        Path downloaded = null;
        Enclosure enclosure = appcast.getLatestEnclosure();
        if (enclosure != null) {
            String url = enclosure.getUrl();
            if (url != null && !url.isEmpty()) {
                URL enclosureUrl = new URL(url);
//                String targetName = Paths.get(enclosureUrl.toURI()).normalize().getFileName().toString();
                String targetName = FilenameUtils.getName(enclosureUrl.toString());
                long length = enclosure.getLength();

                File tmpFile = null;
                ReadableByteChannel rbc = null;
                FileOutputStream fos = null;
                try {
                    tmpFile = File.createTempFile("ac-", ".part");
                    rbc = Channels.newChannel(enclosureUrl.openStream());
                    fos = new FileOutputStream(tmpFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                    // Verify if file is ok
                    // Check size
                    if (length > 0) {
                        long size = Files.size(tmpFile.toPath());
                        if (length != size) {
                            throw new Exception("Downloaded file has wrong size! Expected: " + length + " -- Actual: " + size);
                        }
                    }

                    // Check DSA Signature
                    // TODO

                    // Copy file to target dir
                    downloaded = Files.copy(tmpFile.toPath(), targetDir.resolve(targetName), StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    try { if (fos != null) fos.close(); } catch (IOException e) { /*  ignore */ }
                    try { if (rbc != null) rbc.close(); } catch (IOException e) { /*  ignore */ }
                    if (tmpFile != null) {
                        Files.deleteIfExists(tmpFile.toPath());
                    }
                }
            }
        }

        return downloaded;
    }
}
