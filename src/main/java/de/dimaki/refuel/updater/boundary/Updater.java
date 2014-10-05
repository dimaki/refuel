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
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dino Tsoumakis
 */
public class Updater {

    private static final Logger logger = Logger.getLogger(Updater.class.getName());

    AppcastManager appcastManager;

    public Updater() {
        appcastManager = new AppcastManager();
    }

    /**
     * Get the update status of the application specified.
     *
     * @param localVersion The local version string, e.g. "2.0.1344"
     * @param updateUrl The update URL (Appcast URL)
     * @return The application status or 'null' if the status could not be evaluated
     */
    public ApplicationStatus getApplicationStatus(String localVersion, String updateUrl) {
        ApplicationStatus status = ApplicationStatus.UNKNOWN;
        if (localVersion != null) {
            if (!localVersion.isEmpty()) {
                if (updateUrl != null && !updateUrl.isEmpty()) {
                    // Fetch remote version
                    String remoteVersion = null;
                    try {
                        logger.log(Level.FINE, "Fetching appcast from update URL ''{0}''...", updateUrl);
                        Appcast appcast = appcastManager.fetch(updateUrl);
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
                        logger.log(Level.WARNING, "{0} ''{1}'': {2} {3}", new Object[]{aex.getMessage(), aex.getUrl(), aex.getStatus(), aex.getStatusInfo()});
                        status = ApplicationStatus.FAILURE;
                        status.setInfo(aex.getMessage() + " '" + aex.getUrl() + "': " + aex.getStatus() + " " + aex.getStatusInfo());
                    } catch (Exception ex) {
                        // Seems the be a network problem (e.g. no internet connection)
                        // Just log it, status should be unknown
                        logger.log(Level.WARNING, "Could not connect to update server: {0}", ex.toString());
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
     * @param appcast
     * @param targetDir
     * @return Updated files
     * @throws Exception
     */
    public Set<Path> update(Appcast appcast, Path targetDir) throws Exception {
        Set<Path> files;
        if (appcast == null) {
            throw new IllegalArgumentException("Appcast cannot be null!");
        }
        logger.log(Level.FINE, "Updating application ''{0}''...", appcast.getTitle());

        // Download the update and verfiy it
        Path downloaded = appcastManager.download(appcast, targetDir);
        if (downloaded == null) {
            throw new Exception("Could not download update package for application '" + appcast.getTitle() + "'!");
        }
        logger.log(Level.FINE, "Downloaded update package ''{0}''", downloaded);

        // Unzip the update if required
        files = ZipHandler.unzip(downloaded, targetDir, true);
        logger.log(Level.FINE, "Extracted files: {0}", files);

        return files;
    }

}
