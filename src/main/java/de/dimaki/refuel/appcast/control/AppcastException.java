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
package de.dimaki.refuel.appcast.control;

import java.net.URL;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastException extends Exception {

    URL url;
    int status;
    String statusInfo;

    public AppcastException(String message, URL url, int status, String statusInfo) {
        super(message);
        this.url = url;
        this.status = status;
        this.statusInfo = statusInfo;
    }

    public URL getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }
}
