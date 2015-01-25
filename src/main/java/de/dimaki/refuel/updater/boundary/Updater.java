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
package de.dimaki.refuel.updater.boundary;

import de.dimaki.refuel.appcast.boundary.AppcastManager;
import de.dimaki.refuel.appcast.control.AppcastException;
import de.dimaki.refuel.appcast.entity.Appcast;
import de.dimaki.refuel.updater.entity.ApplicationStatus;
import de.dimaki.refuel.updater.control.VersionComparator;
import de.dimaki.refuel.updater.control.ZipHandler;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dino Tsoumakis
 */
public class Updater {
    private static final Logger LOG = LoggerFactory.getLogger(Updater.class);
    private static final String UPDATE_SCRIPT_SUFFIX = "-update.js";

    AppcastManager appcastManager;

    public Updater() {
        try {
            appcastManager = new AppcastManager();
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not initialize Appcast Manager!", ex);
        }
    }

    /**
     * Get the update status of the application specified.
     *
     * @param localVersion The local version string, e.g. "2.0.1344"
     * @param updateUrl The update URL (Appcast URL)
     * @return The application status or 'null' if the status could not be evaluated
     */
    public ApplicationStatus getApplicationStatus(String localVersion, final URL updateUrl) {
        return getApplicationStatus(localVersion, updateUrl, null, AppcastManager.DEFAULT_CONNECT_TIMEOUT, AppcastManager.DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the update status of the application specified.
     *
     * @param localVersion The local version string, e.g. "2.0.1344"
     * @param updateUrl The update URL (Appcast URL)
     * @param proxy Proxy data
     * @param connectTimeout The connect timeout in milliseconds
     * @param readTimeout The read timeout in milliseconds
     * @return The application status or 'null' if the status could not be evaluated
     */
    public ApplicationStatus getApplicationStatus(String localVersion, final URL updateUrl, Proxy proxy, int connectTimeout, int readTimeout) {
        ApplicationStatus status = ApplicationStatus.UNKNOWN;
        if (localVersion != null) {
            if (!localVersion.isEmpty()) {
                if (updateUrl != null) {
                    // Fetch remote version
                    String remoteVersion = null;
                    try {
                        LOG.debug("Fetching appcast from update URL ''{}''...", updateUrl);
                        Appcast appcast = appcastManager.fetch(updateUrl, proxy, connectTimeout, readTimeout);
                        if (appcast != null) {
                            remoteVersion = appcast.getLatestVersion();
                        }
                        if (remoteVersion == null) {
                            status = ApplicationStatus.FAILURE;
                            status.setInfo("No version information found");
                        } else {
                            VersionComparator vc = new VersionComparator();
                            int compare = vc.compare(localVersion, remoteVersion);
                            if (compare == 0) {
                                status = ApplicationStatus.OK;
                            } else if (compare == -1) {
                                status = ApplicationStatus.UPDATE_AVAILABLE;
                                status.setInfo(remoteVersion);
                                status.setAppcast(appcast);
                            }
                        }
                    } catch (AppcastException aex) {
                        LOG.warn("{} ''{}'': {} {}", aex.getMessage(), aex.getUrl(), aex.getStatus(), aex.getStatusInfo());
                        status = ApplicationStatus.FAILURE;
                        status.setInfo(aex.getMessage() + " '" + aex.getUrl() + "': " + aex.getStatus() + " " + aex.getStatusInfo());
                    } catch (Exception ex) {
                        // Seems the be a network problem (e.g. no internet connection)
                        // Just log it, status should be unknown
                        LOG.warn("Could not connect to update server: {}", ex.toString());
                    }
                    status.setUpdateTime(new Date());
                }
            }
        } else {
            // No version information about installed application!?
            // Seems to be not installed at all
            status = ApplicationStatus.NOT_INSTALLED;
        }
        return status;
    }

    /**
     * Update with the given appcast information in the specified targetDir
     *
     * @param appcast The appcast content (containing the file to download)
     * @param targetDir The target directory for downloaded update files
     * @return Updated files
     * @throws Exception in case of an error
     */
    public Set<Path> update(Appcast appcast, Path targetDir) throws Exception {
        Set<Path> files;
        if (appcast == null) {
            throw new IllegalArgumentException("Appcast cannot be null!");
        }
        LOG.debug("Updating application ''{}''...", appcast.getTitle());

        // Download the update and verfiy it
        Path downloaded = appcastManager.download(appcast, targetDir);
        if (downloaded == null) {
            throw new Exception("Could not download update package for application '" + appcast.getTitle() + "'!");
        }
        LOG.debug("Downloaded update package ''{}''", downloaded);

        // Unzip the update if required
        files = ZipHandler.unzip(downloaded, targetDir, true);
        LOG.debug("Extracted files: {}", files);

        // Check if there is an update script available and execute it if so
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("targetDir", targetDir);
        files.stream().filter((filePath) -> (filePath.getFileName().toString().endsWith(UPDATE_SCRIPT_SUFFIX))).forEach((filePath) -> {
            executeUpdateScript(filePath, bindings);
        });

        return files;
    }

    protected void executeUpdateScript(Path filePath, Map<String, Object> bindings) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        // Fallback
        if (engine == null) {
            engine = new ScriptEngineManager().getEngineByExtension("js");
        }
        if (engine != null) {
            LOG.info("Executing update script ''{}''...", filePath);
            Bindings b = engine.createBindings();
            b.putAll(bindings);
            engine.setBindings(b, ScriptContext.ENGINE_SCOPE);

            FileReader fr = null;
            try {
                fr = new FileReader(filePath.toFile());
                engine.eval(fr);
            } catch (FileNotFoundException | ScriptException ex) {
                LOG.error("Could not evaluate update script file ''{}''!", filePath, ex);
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }
                } catch (IOException ex) { /* ignore */ }
            }
        }
    }

}
