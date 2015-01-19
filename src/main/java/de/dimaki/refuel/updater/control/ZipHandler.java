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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dino Tsoumakis
 */
public class ZipHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ZipHandler.class);
    private static final String ROOT_PATH = "/";
    private static final String ZIP_EXTENSION = ".zip";

    /**
     * Hide constructor
     */
    private ZipHandler() {
        super();
    }

    public static Set<Path> unzip(final Path zipFile, final Path targetDir, final boolean deleteAfterUnzip) throws Exception {
        final HashSet<Path> extractedFiles = new HashSet<>();
        if (zipFile != null && zipFile.toString().endsWith(ZIP_EXTENSION)) {
            try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFile, null)) {
                Path root = zipFileSystem.getPath(ROOT_PATH);

                // Process each file within the zip file
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path destFile = Paths.get(targetDir.toString(), file.toString());
                        extractedFiles.add(Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        final Path dirToCreate = Paths.get(targetDir.toString(), dir.toString());
                        if (Files.notExists(dirToCreate)) {
                            Files.createDirectory(dirToCreate);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            if (deleteAfterUnzip) {
                LOG.debug("Deleting zip file ''{}''", zipFile);
                Files.deleteIfExists(zipFile);
            }
        } else {
            LOG.debug("No zip file ''{}''!", zipFile);
            extractedFiles.add(zipFile);
        }
        return extractedFiles;
    }
}
