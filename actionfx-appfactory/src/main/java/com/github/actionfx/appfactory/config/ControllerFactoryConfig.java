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
public class ControllerFactoryConfig {

    private String absoluteProjectRootDirectory;

    private String absoluteFxmlFilePath;

    private String relativeJavaSourceDirectory;

    private String relativeFXMLResourcesDirectory;

    private String controllerPackageName;

    public ControllerFactoryConfig() {
        // set defaults
        relativeJavaSourceDirectory = "src/main/java";
        relativeFXMLResourcesDirectory = "src/main/resources/fxml";
    }

    public String getControllerPackageName() {
        return controllerPackageName;
    }

    public void setControllerPackageName(final String controllerPackageName) {
        this.controllerPackageName = controllerPackageName;
    }

    public String getAbsoluteJavaSourceDirectory() {
        return replaceBackslash(Path.of(getAbsoluteProjectRootDirectory(), relativeJavaSourceDirectory.split("\\/\\\\"))
                .toAbsolutePath().toString());
    }

    public String getAbsoluteFXMLResourcesDirectory() {
        return replaceBackslash(
                Path.of(getAbsoluteProjectRootDirectory(), relativeFXMLResourcesDirectory.split("\\/\\\\"))
                        .toAbsolutePath().toString());
    }

    public String getAbsoluteControllerDirectory() {
        return replaceBackslash(Path.of(getAbsoluteJavaSourceDirectory(), controllerPackageName.split("\\."))
                .toAbsolutePath().toString());
    }

    public String getAbsoluteProjectRootDirectory() {
        return absoluteProjectRootDirectory;
    }

    public void setAbsoluteProjectRootDirectory(final String absoluteProjectRootDirectory) {
        this.absoluteProjectRootDirectory = replaceBackslash(absoluteProjectRootDirectory);
    }

    public String getAbsoluteFxmlFilePath() {
        return absoluteFxmlFilePath;
    }

    public void setAbsoluteFxmlFilePath(final String absoluteFxmlFilePath) {
        this.absoluteFxmlFilePath = replaceBackslash(absoluteFxmlFilePath);
    }

    public String getRelativeJavaSourceDirectory() {
        return relativeJavaSourceDirectory;
    }

    public void setRelativeJavaSourceDirectory(final String relativeJavaSourceDirectory) {
        this.relativeJavaSourceDirectory = replaceBackslash(relativeJavaSourceDirectory);
    }

    public String getRelativeFXMLResourcesDirectory() {
        return relativeFXMLResourcesDirectory;
    }

    public void setRelativeFXMLResourcesDirectory(final String relativeFXMLResourcesDirectory) {
        this.relativeFXMLResourcesDirectory = replaceBackslash(relativeFXMLResourcesDirectory);
    }

    private static String replaceBackslash(final String value) {
        return value.replace('\\', '/');
    }
}
