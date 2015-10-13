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
package de.dimaki.refuel.updater.control;

import java.util.Comparator;

/**
 *
 * @author Dino Tsoumakis
 */
public class VersionComparator implements Comparator<String> {

    private static final String VERSION_SEPARATOR = "\\.";
    private static final String QUALIFIER_SEPARATOR = "-";

    @Override
    public int compare(String localVersion, String remoteVersion) {

        String local = null;
        String qualifierLocal = null;
        String remote = null;
        String qualifierRemote = null;


        int qualifierIndexLocal = localVersion.indexOf(QUALIFIER_SEPARATOR);
        int qualifierIndexRemote = remoteVersion.indexOf(QUALIFIER_SEPARATOR);

        if (qualifierIndexLocal > 0) {
            qualifierLocal = localVersion.substring( qualifierIndexLocal + 1 );
            local = localVersion.substring(0, qualifierIndexLocal);
        } else {
            local = localVersion;
        }

        if (qualifierIndexRemote > 0) {
            qualifierRemote = remoteVersion.substring(qualifierIndexRemote + 1);
            remote = remoteVersion.substring(0, qualifierIndexRemote);
        } else {
            remote = remoteVersion;
        }

        String[] p1 = local.split(VERSION_SEPARATOR);
        String[] p2 = remote.split(VERSION_SEPARATOR);

        int n = Math.min(p1.length, p2.length);
        for (int i = 0; i < n; i++) {
            try {
                int a1 = Integer.parseInt(p1[i]);
                int a2 = Integer.parseInt(p2[i]);
                if (a1 < a2) {
                    return -1;
                } else if (a1 > a2) {
                    return 1;
                }
            } catch (NumberFormatException nfe) {
                // Handle NaN errors
            }
        }

        // different length
        if (Math.max(p1.length, p2.length) > n) {
            if (p1.length > p2.length) {
                return 1;
            } else if (p1.length < p2.length) {
                return -1;
            }
        }

        if (qualifierLocal != null) {
            if (qualifierRemote == null || qualifierRemote.isEmpty()) {
                // Version without qualifiers always win
                return -1;
            }
        } else {
            if (qualifierRemote != null) {
                // Versions without qualifiery always win
                return 1;
            }
        }

        return 0;
    }
}
