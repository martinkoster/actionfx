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
public class MainAppFactoryConfig {

    private String absoluteProjectRootDirectory;

    private String groupId;

    private String name;

    private String rootPackageName;

    private boolean useSpring;

    private boolean createEmptyMainView;

    private boolean useExistingFxmlFile;

    public String getAbsoluteProjectRootDirectory() {
        return absoluteProjectRootDirectory;
    }

    public void setAbsoluteProjectRootDirectory(final String absoluteProjectRootDirectory) {
        this.absoluteProjectRootDirectory = absoluteProjectRootDirectory;
    }

    public String getAbsoluteSourceRootDirectory() {
        return Path.of(getAbsoluteProjectRootDirectory(), "src", "main", "java").toAbsolutePath().toString();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public void setRootPackageName(final String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    public boolean isUseSpring() {
        return useSpring;
    }

    public void setUseSpring(final boolean useSpring) {
        this.useSpring = useSpring;
    }

    public boolean isCreateEmptyMainView() {
        return createEmptyMainView;
    }

    public void setCreateEmptyMainView(final boolean createEmptyMainView) {
        this.createEmptyMainView = createEmptyMainView;
    }

    public boolean isUseExistingFxmlFile() {
        return useExistingFxmlFile;
    }

    public void setUseExistingFxmlFile(final boolean useExistingFxmlFile) {
        this.useExistingFxmlFile = useExistingFxmlFile;
    }
}
