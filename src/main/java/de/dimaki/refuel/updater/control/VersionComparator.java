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

    public static final String VERSION_SEPARATOR = "\\.";

    @Override
    public int compare(String localVersion, String remoteVersion) {
        String[] p1 = localVersion.split(VERSION_SEPARATOR);
        String[] p2 = remoteVersion.split(VERSION_SEPARATOR);

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

        return 0;
    }
}
