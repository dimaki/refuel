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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/**
 *
 * @author Dino Tsoumakis
 */
public class BootstrapTest {

    @Test
    public void testUpdate() {
        Path appJar = Paths.get("target/test-classes/jartest.jar");
        Path updateDir = Paths.get("target/test-classes/update");

        try {
            Path newApp = Bootstrap.update(appJar, updateDir, false);
            assertNotNull(newApp);
            assertEquals(appJar.toString(), newApp.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

}
