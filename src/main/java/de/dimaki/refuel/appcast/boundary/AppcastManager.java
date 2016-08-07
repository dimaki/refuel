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
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Dino Tsoumakis
 */
public class AppcastManager {
    public static final String MANIFEST_APPCAST_VERSION = "Appcast-Version";
    public static final String MANIFEST_APPCAST_URL = "Appcast-Url";
    public static final int DEFAULT_CONNECT_TIMEOUT = 8000;
    public static final int DEFAULT_READ_TIMEOUT = 8000;

    //Client client;
    Unmarshaller unmarshaller;
    // Trust all certs
    boolean trustAllCerts = false;
    // Verify Hostname
    boolean verifyHostname = true;

    public AppcastManager() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Appcast.class);
        unmarshaller = jc.createUnmarshaller();
    }

    /**
     * Fetch an appcast from the given URL
     *
     * @param url The update URL
     * @return The fetched appcast content
     * @throws AppcastException in case of an error
     */
    public Appcast fetch(final URL url) throws AppcastException {
        return fetch(url, null, DEFAULT_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Fetch an appcast from the given URL
     *
     * @param url The update URL
     * @param proxy proxy data
     * @param connectTimeout the connect timeout in milliseconds
     * @param readTimeout the read timeout in milliseconds
     * @return The fetched appcast content
     * @throws AppcastException in case of an error
     */
    public Appcast fetch(final URL url, Proxy proxy, int connectTimeout, int readTimeout) throws AppcastException {
        Appcast appcast = null;
        URLConnection conn = null;
        try {
            if (proxy == null) {
                conn = url.openConnection();
            } else {
                conn = url.openConnection(proxy);
            }
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            // init SSL
            if ((trustAllCerts || !verifyHostname) && conn instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConn = (HttpsURLConnection)conn;
                if (trustAllCerts) {
                    SSLContext sslContext = createSslContext();
                    httpsConn.setSSLSocketFactory(sslContext.getSocketFactory());
                }
                if (!verifyHostname) {
                    httpsConn.setHostnameVerifier(new TrustAllHostnameVerifier());
                }
                conn = httpsConn;
            }
            conn.connect();
            appcast = (Appcast)unmarshaller.unmarshal(conn.getInputStream());
        } catch (JAXBException jbe) {
            throw new AppcastException("Could not read appcast from URL", url, 404, jbe.getMessage());
        } catch (SocketTimeoutException ste) {
            throw new AppcastException("Timeout reading appcast from URL", url, 408, (ste.getCause() != null) ? ste.getCause().getMessage() : ste.getMessage());
        } catch (UnknownHostException uhe) {
            throw new AppcastException("Unknown Host", url, 404, uhe.getMessage());
        } catch (IOException ex) {
            throw new AppcastException("Could not establish connection to URL", url, 403, ex.getMessage());
        } catch (GeneralSecurityException ex) {
            throw new AppcastException("Could not initialize SSL context", url, 500, ex.getMessage());
        }
        // Got a valid response
        return appcast;
    }

    /**
     * Get the latest appcast version string from the given url
     *
     * @param url The appcast url
     * @return The version string
     * @throws AppcastException in case of an error
     */
    public String getLatestVersion(final URL url) throws AppcastException {
        return getLatestVersion(url, null, DEFAULT_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the latest appcast version string from the given url
     *
     * @param url The appcast url
     * @param proxy proxy data
     * @param connectTimeout the connect timeout in milliseconds
     * @param readTimeout the read timeout in milliseconds
     * @return The version string
     * @throws AppcastException in case of an error
     */
    public String getLatestVersion(final URL url, Proxy proxy, int connectTimeout, int readTimeout) throws AppcastException {
        String version = null;
        Appcast appcast = fetch(url, proxy, connectTimeout, readTimeout);
        if (appcast != null) {
            version = appcast.getLatestVersion();
        }
        return version;
    }

    /**
     * Download the file from the given URL to the specified target
     *
     * @param appcast The appcast content
     * @param targetDir The target download dir (update directory)
     * @return Path to the downloaded update file
     * @throws IOException in case of an error
     */
    public Path download(Appcast appcast, Path targetDir) throws IOException, Exception {
        Path downloaded = null;
        Enclosure enclosure = appcast.getLatestEnclosure();
        if (enclosure != null) {
            String url = enclosure.getUrl();
            if (url != null && !url.isEmpty()) {
                URL enclosureUrl = new URL(url);
                String targetName = url.substring( url.lastIndexOf('/')+1, url.length() );
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

    public boolean isTrustAllCerts() {
        return trustAllCerts;
    }

    /**
     * Set option to trust all SSL certificates
     * @param trustAllCerts true to trust all SSL certificates, false otherwise (default)
     */
    public void setTrustAllCerts(boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    public boolean isVerifyHostname() {
        return verifyHostname;
    }

    /**
     * Set option to verify hostname
     * @param verifyHostname true to verify hostnames in SSL sessions (default), false to disable hostname verification
     */
    public void setVerifyHostname(boolean verifyHostname) {
        this.verifyHostname = verifyHostname;
    }

    private SSLContext createSslContext() throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        TrustManager[] trustAll = new TrustManager[] {new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        sslContext.init(null, trustAll, new SecureRandom());
        return sslContext;
    }

    /**
     * Inner class to trust all hostnames
     */
    public class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
