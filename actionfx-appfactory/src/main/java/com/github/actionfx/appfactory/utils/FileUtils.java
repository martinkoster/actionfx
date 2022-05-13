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
package com.github.actionfx.appfactory.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Util class for handling files and directories.
 *
 * @author koster
 *
 */
public class FileUtils {

    private FileUtils() {
        // can not be instantiated
    }

    /**
     * Creates all missing directories in the given {@code absolutePath}.
     *
     * @param absolutePath
     *            the path to be created in the file system
     */
    public static void createDirectories(final String absolutePath) {
        try {
            Files.createDirectories(Path.of(absolutePath));
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot create directories " + absolutePath + "!", e);

        }
    }

    /**
     * Copies a file from the classpath identified by {@code classpathLocation} to the given {@code absoluteFolderPath}.
     * Please note that it is expected that this folder exists.
     *
     * @param classpathLocation
     *            the classpath location to copy the file from
     * @param absoluteFolderPath
     *            the destination folder as absolute folder path
     */
    public static void copyClasspathFile(final String classpathLocation, final String absoluteFolderPath) {
        copyFile(FileUtils.class.getResourceAsStream(classpathLocation), absoluteFolderPath,
                Path.of(classpathLocation).getFileName().toString());
    }

    /**
     * Copies a file located under {@code absoluteFilePath} to the given {@code absoluteFolderPath}. Please note that it
     * is expected that this folder exists.
     *
     * @param absoluteFilePath
     *            the absolute path to the file
     * @param absoluteFolderPath
     *            the destination folder as absolute folder path
     */
    public static void copyFile(final String absoluteFilePath, final String absoluteFolderPath) {
        try (InputStream inputStream = new FileInputStream(new File(absoluteFilePath))) {
            copyFile(new FileInputStream(new File(absoluteFilePath)), absoluteFolderPath,
                    Path.of(absoluteFilePath).getFileName().toString());
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Can not copy file '" + absoluteFilePath + "' to folder '" + absoluteFolderPath + "'!", e);
        }
    }

    /**
     * Copies data from the supplied {@code inputStream} to a file located in folder {@code absoluteFolderPath} and file
     * name {@code fileName}.
     *
     * @param inputStream
     *            the input stream to copy data from
     * @param absoluteFolderPath
     *            the absolute folder path
     * @param fileName
     *            the file name
     */
    private static void copyFile(final InputStream inputStream, final String absoluteFolderPath,
            final String fileName) {
        try (inputStream) {
            assertNonNullInputStream(inputStream, fileName);
            Files.copy(inputStream, Path.of(absoluteFolderPath, fileName));
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Can not copy file '" + fileName + "' to folder '" + absoluteFolderPath + "'!", e);
        }
    }

    /**
     * Reads the contents of a file from the given {@code absoluteFilePath}.
     *
     * @param absoluteFilePath
     *            the absolute file path to read from
     * @param charset
     *            the character set the file possesses
     * @return the content of the file as string
     */
    public static String readFromFile(final String absoluteFilePath, final Charset charset) {
        try (InputStream inputStream = new FileInputStream(new File(absoluteFilePath))) {
            assertNonNullInputStream(inputStream, absoluteFilePath);
            return readFromInputStream(inputStream, charset);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to read file content from location: " + absoluteFilePath);
        }

    }

    /**
     * Reads the contents of a file from the given {@code classpathLocation}.
     *
     * @param classpathLocation
     *            the classpath location to read from
     * @param charset
     *            the character set the file possesses
     * @return the content of the file as string
     */
    public static String readFromClasspath(final String classpathLocation, final Charset charset) {
        try (InputStream inputStream = FileUtils.class.getResourceAsStream(classpathLocation)) {
            assertNonNullInputStream(inputStream, classpathLocation);
            return readFromInputStream(inputStream, charset);
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Unable to read file content from classpath location: " + classpathLocation);
        }
    }

    /**
     * Reads the contents of a file from the given {@link InputStream}.
     *
     * @param inputStream
     *            the input stream to read from
     * @param charset
     *            the character set the file possesses
     * @return the content of the file as string
     */
    public static String readFromInputStream(final InputStream inputStream, final Charset charset) {
        try {
            return new String(inputStream.readAllBytes(), charset);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to read content from input stream!", e);
        }
    }

    /**
     * Unzips a file from the supplied {@code classpathLocation}, which is expected to contain a valid stream of zipped
     * data.
     *
     * @param classpathLocation
     *            the classpath location to unzip from
     * @param destDir
     *            the destination directory
     */
    public static void unzip(final String classpathLocation, final File destDir) {
        unzip(FileUtils.class.getResourceAsStream(classpathLocation), destDir);
    }

    /**
     * Unzips the supplied {@link InputStream}, which is expected to contain a valid stream of zipped data.
     *
     * @param inputStream
     *            the input stream to unzip from
     * @param destDir
     *            the destination directory
     */
    public static void unzip(final InputStream inputStream, final File destDir) {
        final byte[] buffer = new byte[1024];
        final ZipInputStream zis = new ZipInputStream(inputStream);
        try {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                final File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    unzipDirectory(newFile);
                } else {
                    unzipFile(zis, newFile, buffer);
                }
                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();
            zis.close();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to unzip input stream", e);
        }
    }

    private static void unzipDirectory(final File newFile) throws IOException {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile);
        }
    }

    private static void unzipFile(final ZipInputStream zis, final File newFile, final byte[] buffer)
            throws IOException {
        // fix for Windows-created archives
        final File parent = newFile.getParentFile();
        unzipDirectory(parent);

        // write file content
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    private static File newFile(final File destinationDir, final ZipEntry zipEntry) throws IOException {
        final File destFile = new File(destinationDir, zipEntry.getName());

        final String destDirPath = destinationDir.getCanonicalPath();
        final String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static void assertNonNullInputStream(final InputStream inputStream, final String location) {
        if (inputStream == null) {
            throw new IllegalStateException("Unable to create input stream from location '" + location + "'!");
        }
    }
}
