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
package com.github.actionfx.appfactory.factories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.actionfx.appfactory.config.FactoryConfig;
import com.github.actionfx.appfactory.text.TestUtils;

/**
 * JUnit test case for {@link ControllerFactory}.
 *
 * @author koster
 *
 */
class ControllerFactoryTest {

	@Test
	void testProduce() throws IOException {
		// GIVEN
		final FactoryConfig cfg = new FactoryConfig();
		final Path tmpFolder = Files.createTempDirectory("project");
		cfg.setAbsoluteProjectRootDirectory(tmpFolder.toAbsolutePath().toString());
		cfg.setRelativeSourceRootDirectory("src/main/java/");
		cfg.setRelativeResourcesRootDirectory("src/main/resources/");
		cfg.setControllerPackageName("com.github.actionfx.appfactory.controller");
		TestUtils.copyClasspathFile("/fxml/MainView.fxml", tmpFolder);
		final ControllerFactory controllerFactory = new ControllerFactory(cfg);

		// WHEN
		controllerFactory.produce(Path.of(tmpFolder.toString(), "MainView.fxml").toAbsolutePath().toString());

		// THEN
		TestUtils.assertFileExists(cfg.getAbsoluteControllerDirectory() + "/MainController.java");
	}

}
