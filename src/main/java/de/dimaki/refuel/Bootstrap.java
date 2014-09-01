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
package de.dimaki.refuel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 *
 * @author Dino Tsoumakis
 */
public class Bootstrap {

    private ClassLoader defaultClassLoader;
    private URLClassLoader appClassLoader;
    private Class<?> appClazz;
    private Object appInstance;

    /**
     * Update the application file with the content from the update directory.
     * @param applicationFile The application file to be updated
     * @param updateDir The update directory holding the new file versions
     * @param removeUpdateFiles If true, the updated files are deleted from the update directory
     * @return The path to the newly updated application file, or null if there was no update performed
     * @throws IOException In case of an error
     */
    public static Path update(Path applicationFile, Path updateDir, boolean removeUpdateFiles) throws IOException {
        Path newApplicationJar = null;
        // Extract the application name from the application file
        String applicationName = applicationFile.getFileName().toString();
        // Strip the extension
        applicationName = applicationName.substring(0, applicationName.lastIndexOf("."));

        ArrayList<Path> updateFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(updateDir, applicationName + "*.{jar,JAR,war,WAR,rar,RAR,ear,EAR}")) {
            for (Path entry : stream) {
                updateFiles.add(entry);
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }

        if (!updateFiles.isEmpty()) {
            Path updateFile = updateFiles.get(0);
            newApplicationJar = Files.copy(updateFile, applicationFile, StandardCopyOption.REPLACE_EXISTING);
            if (removeUpdateFiles) {
                Files.delete(updateFile);
            }
        }

        return newApplicationJar;
    }

    /**
     * Start the application
     * @param applicationFile The application file (e.g. a JAR)
     * @param applicationClass The application class (e.g. "de.dimaki.jartest.JarTest")
     * @param methodName The method to be called (e.g. "start")
     * @throws IOException In case of an error
     */
    public void startApp(Path applicationFile, String applicationClass, String methodName) throws IOException {
        try {
            System.out.println("Loading : " + applicationFile.toAbsolutePath().toString());
            this.defaultClassLoader = Thread.currentThread().getContextClassLoader();
            this.appClassLoader = URLClassLoader.newInstance(new URL[]{applicationFile.toUri().toURL()}, this.defaultClassLoader);
            Thread.currentThread().setContextClassLoader(this.appClassLoader);
            Thread.getAllStackTraces().keySet().stream().forEach((thread) -> {
                thread.setContextClassLoader(this.appClassLoader);
            });
            this.appClazz = this.appClassLoader.loadClass(applicationClass);
            final Method method = this.appClazz.getMethod(methodName);
            this.appInstance = this.appClazz.newInstance();
            method.invoke(this.appInstance);
        } catch (final Exception ex) {
            throw new IOException(ex);
        }
    }
}
