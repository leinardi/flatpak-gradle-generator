/* SourcesListPlugin - a Gradle plugin to generate a list of dependencies
 * Copyright (C) 2023 Jan-Willem Harmannij
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jwharm.sourceslistplugin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Files;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the plugin with a temporary created Gradle build that
 * depends on Jetbrains Annotations. It should generate a
 * sources list that contains the annotations-24.0.1.jar file.
 */
class TestDependencyOutput {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    private File getOutputFile() {
        return new File(projectDir, "output.txt");
    }

    @Test void testDependencyOutput() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
        """
                plugins {
                  id 'io.github.jwharm.sourceslistplugin'
                  id 'application'
                }
                
                repositories {
                  mavenCentral()
                  maven { url 'https://jitpack.io' }
                }
                
                dependencies {
                  compileOnly 'org.junit.jupiter:junit-jupiter:5.9.2'
                }
                
                tasks.sourcesList {
                  outputFile = file('%s')
                  downloadDirectory = 'localRepository'
                }
                """.formatted(getOutputFile().getAbsolutePath()));

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("sourcesList");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        // Verify the result
        String sourcesList = Files.readString(getOutputFile().toPath());

        String expected = """
                [
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-params/5.9.2/junit-jupiter-params-5.9.2.jar",
                  "sha512": "6fd6fb739f9ab7d7d188a96e56c26979ab720f0dd7d9f12bf732bb3b1689ba4f5c327e86b4cfae5e468027ca11019dfcbfff60c0ec924c8bc69389bff03de98c",
                  "dest": "localRepository",
                  "dest-filename": "junit-jupiter-params-5.9.2.jar"
                },
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/5.9.2/junit-jupiter-5.9.2.jar",
                  "sha512": "518967645266167d50416f234eaf324bbf6d701c19a96fe5699824b9078d765146335b1a57a5fdfce7a50e8f489c8a6edd4068cb9acf4acee130d6e7cfa3fb9d",
                  "dest": "localRepository",
                  "dest-filename": "junit-jupiter-5.9.2.jar"
                },
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar",
                  "sha512": "17f77797a260eb2bd1666a90e25efc79a5413afa9df1c1cb6c4cd1949d61c38b241e3bb20956396b5f54d144720303d72a2ac00bc5bf245a260a3c3099e01c74",
                  "dest": "localRepository",
                  "dest-filename": "opentest4j-1.2.0.jar"
                },
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.9.2/junit-platform-commons-1.9.2.jar",
                  "sha512": "dd259a9e2f37588552322c9b4dd37aad4daa2e2ae0c10b79e7e3e128698020b028020d7c7dfa058944b9fafa493f1cf8aaf6d32911292a7d4f01910106bb552b",
                  "dest": "localRepository",
                  "dest-filename": "junit-platform-commons-1.9.2.jar"
                },
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar",
                  "sha512": "d7ccd0e7019f1a997de39d66dc0ad4efe150428fdd7f4c743c93884f1602a3e90135ad34baea96d5b6d925ad6c0c8487c8e78304f0a089a12383d4a62e2c9a61",
                  "dest": "localRepository",
                  "dest-filename": "apiguardian-api-1.1.2.jar"
                },
                {
                  "type": "file",
                  "url": "https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.9.2/junit-jupiter-api-5.9.2.jar",
                  "sha512": "36efb8800c40b359133cfe823723c3d6f34b0d39df91187fb8f7f90339a7d9984a34b4d091c945475afc862f3e5ad5412516c1577656b1aee963fe0f6da0d59e",
                  "dest": "localRepository",
                  "dest-filename": "junit-jupiter-api-5.9.2.jar"
                }
                ]
                """;
        assertEquals(expected, sourcesList);
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
