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

import java.nio.file.Path;

/**
 * Configuration of the app factory application.
 *
 * @author koster
 *
 */
public class FactoryConfig {

	private String absoluteProjectRootDirectory;

	private String relativeSourceRootDirectory;

	private String relativeResourcesRootDirectory;

	private String controllerPackageName;

	public String getControllerPackageName() {
		return controllerPackageName;
	}

	public void setControllerPackageName(final String controllerPackageName) {
		this.controllerPackageName = controllerPackageName;
	}

	public String getAbsoluteSourceRootDirectory() {
		return Path.of(getAbsoluteProjectRootDirectory(), relativeSourceRootDirectory.split("\\/\\\\")).toAbsolutePath()
				.toString();
	}

	public String getAbsoluteResourcesRootDirectory() {
		return Path.of(getAbsoluteProjectRootDirectory(), relativeResourcesRootDirectory.split("\\/\\\\"))
				.toAbsolutePath().toString();
	}

	public String getAbsoluteControllerDirectory() {
		return Path.of(getAbsoluteSourceRootDirectory(), controllerPackageName.split("\\.")).toAbsolutePath()
				.toString();
	}

	public String getAbsoluteProjectRootDirectory() {
		return absoluteProjectRootDirectory;
	}

	public void setAbsoluteProjectRootDirectory(final String absoluteProjectRootDirectory) {
		this.absoluteProjectRootDirectory = absoluteProjectRootDirectory;
	}

	public String getRelativeSourceRootDirectory() {
		return relativeSourceRootDirectory;
	}

	public void setRelativeSourceRootDirectory(final String relativeSourceRootDirectory) {
		this.relativeSourceRootDirectory = relativeSourceRootDirectory;
	}

	public String getRelativeResourcesRootDirectory() {
		return relativeResourcesRootDirectory;
	}

	public void setRelativeResourcesRootDirectory(final String relativeResourcesRootDirectory) {
		this.relativeResourcesRootDirectory = relativeResourcesRootDirectory;
	}
}
