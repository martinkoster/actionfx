/*
 * Copyright (c) 2021 Martin Koster
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.actionfx.appfactory.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link MainAppFactoryConfig}.
 *
 * @author koster
 */
class MainAppFactoryConfigTest {

    @Test
    void testFolder() throws IOException {
        // GIVEN
        final Path tmpFolder = Files.createTempDirectory("projectRoot");
        final MainAppFactoryConfig config = new MainAppFactoryConfig();
        final String rootFolder = tmpFolder.toAbsolutePath().toString();
        config.setAbsoluteProjectRootDirectory(rootFolder);
        config.setName("test-project");
        config.setGroupId("com.github.martinkoster");
        config.setRootPackageName("com.github.testproject");

        config.setUseSpring(false);

        // WHEN and THEN
        assertThat(config.getAbsoluteProjectRootDirectory(), equalTo(rootFolder));
        assertThat(config.getControllerPackageName(), equalTo("com.github.testproject.controller"));
        assertThat(config.getAbsoluteSourceRootDirectory(),
                equalTo(Path.of(rootFolder, "src", "main", "java").toString()));
        assertThat(config.getAbsoluteResourceRootDirectory(),
                equalTo(Path.of(rootFolder, "src", "main", "resources").toString()));
        assertThat(config.getAbsoluteFxmlRootDirectory(),
                equalTo(Path.of(rootFolder, "src", "main", "resources", "fxml").toString()));
        assertThat(config.getAbsoluteMainControllerDirectory(), equalTo(Path
                .of(config.getAbsoluteSourceRootDirectory(), "com", "github", "testproject", "controller").toString()));
    }

}
