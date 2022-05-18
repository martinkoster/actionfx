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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Configuration for Freemarker templating.
 *
 * @author koster
 *
 */
public class FreemarkerConfiguration {

    private static FreemarkerConfiguration instance = null;

    private final Configuration configuration;

    /**
     * Please use {@link #getInstance()}.
     */
    private FreemarkerConfiguration() {
        configuration = prepareFreemarkerConfiguration();
    }

    /**
     * Gets an instance of {@link FreemarkerConfiguration}.
     *
     * @return the freemarker configuration instance
     */
    public static FreemarkerConfiguration getInstance() {
        if (instance == null) {
            instance = new FreemarkerConfiguration();
        }
        return instance;
    }

    /**
     * Writes the populate template identified with {@code templateName} to the file identified with the absolute path
     * under {@code targetFile}, using the data from the given {@code model}.
     *
     * @param templateName
     *            the name of the template
     * @param model
     *            the data model
     * @param targetFile
     *            the absolute path to the target file
     */
    public void writeTemplate(final String templateName, final Object model, final File targetFile) {
        try (final FileWriter fileWriter = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            writeTemplate(templateName, model, fileWriter);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot write file " + targetFile.getAbsolutePath() + "!", e);
        }
    }

    /**
     * Writes the populate template identified with {@code templateName} to the supplied {@code writer}, using the data
     * from the given {@code model}.
     *
     * @param templateName
     *            the name of the template
     * @param model
     *            the data model
     * @param writer
     *            the writer to write to
     */
    public void writeTemplate(final String templateName, final Object model, final Writer writer) {
        final Template template = getTemplate(templateName);
        try {
            template.process(model, writer);
        } catch (TemplateException | IOException e) {
            throw new IllegalStateException(
                    "Unable to write data model '" + model + "' to template '" + templateName + "'!", e);
        }
    }

    /**
     * Gets a Freemarker template that is identified by {@code name}. The name must be relative to the root template
     * path {@code templates} and must not start with a slash "/".
     *
     * @param name
     *            the template name
     * @return the loaded template
     */
    protected Template getTemplate(final String name) {
        try {
            return configuration.getTemplate(name);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to load Freemarker template from '" + name + "'!", e);
        }
    }

    /**
     * Sets up Freemarker for use.
     *
     * @return the Freemarker configuration
     */
    private Configuration prepareFreemarkerConfiguration() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(FreemarkerConfiguration.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        cfg.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into
        // TemplateException-s:
        cfg.setWrapUncheckedExceptions(true);

        // Do not fall back to higher scopes when reading a null loop variable:
        cfg.setFallbackOnNullLoopVariable(false);

        return cfg;
    }
}
