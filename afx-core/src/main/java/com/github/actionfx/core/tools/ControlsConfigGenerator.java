/*
 * Copyright (c) 2020 Martin Koster
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
package com.github.actionfx.core.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.actionfx.core.utils.ReflectionUtils;
import com.github.actionfx.core.view.graph.ControlWrapper;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;

/**
 * Helper-class for generating template configuration for class
 * {@link ControlWrapper}. The class accepts a package name to scan for
 * components derived from {@link Control}.
 *
 * @author koster
 *
 */
public class ControlsConfigGenerator {

	private static final String CONFIG_VALUES = "valueProperty=%s\n" //
			+ "valuesObservableList=%s\n" //
			+ "selectionModelProperty=%s\n" //
			+ "selectedValueProperty=%s\n" //
			+ "selectedValuesObservableList=%s";

	public static List<Class<?>> findControls(final String rootPackage) {
		final List<Class<?>> foundClasses = new ArrayList<>();
		try (ScanResult result = new ClassGraph().enableClassInfo().enableAnnotationInfo().acceptPackages(rootPackage)
				.scan()) {

			final ClassInfoList classInfos = result.getSubclasses(Control.class.getCanonicalName());
			classInfos.forEach(classInfo -> foundClasses.add(classInfo.loadClass(false)));
		}
		return foundClasses.stream()
				.filter(clazz -> (clazz.getModifiers() & Modifier.ABSTRACT) == 0 && !Cell.class.isAssignableFrom(clazz))
				.collect(Collectors.toList());
	}

	private static void writeControlsConfigProperties(final String folder, final char[] buffer, final Class<?> clazz,
			final InputStreamReader reader) {
		final String fileName = folder + "/" + clazz.getCanonicalName() + ".properties";
		try (final FileWriter writer = new FileWriter(fileName)) {
			System.out.println("Generating file: " + fileName);
			int numRead;
			while ((numRead = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, numRead);
			}
			writer.write(generateConfigValuesForClass(clazz));
		} catch (final IOException e) {
		}
	}

	private static String generateConfigValuesForClass(final Class<?> clazz) {
		// try to guess some values
		final String valueProperty = checkFields(clazz, "selected", "expandedPane", "value", "root", "progress", "item",
				"text");
		final String valuesObservableList = checkFields(clazz, "items", "values", "tabs", "panes");
		final boolean hasSelectionModel = ReflectionUtils.findField(clazz, "selectionModel") != null;
		final String selectionModelProperty = hasSelectionModel ? "selectionModel" : "";
		final String selectedValueProperty = hasSelectionModel ? "selectionModel.selectedItem" : "";
		final String selectedValuesObservableList = hasSelectionModel ? "selectionModel.selectedItems" : "";
		return String.format(CONFIG_VALUES, valueProperty, valuesObservableList, selectionModelProperty,
				selectedValueProperty, selectedValuesObservableList);

	}

	private static String checkFields(final Class<?> clazz, final String... fieldCandidates) {
		for (final String fieldCandidate : fieldCandidates) {
			if (ReflectionUtils.findField(clazz, fieldCandidate) != null) {
				return fieldCandidate;
			}
		}
		return "";
	}

	public static void main(final String[] argv) {
		if (argv.length != 2) {
			System.out.println("Usage: ControlsConfigGenerator <package-name> <output-folder>");
			System.out.println("Example: java ControlsConfigurator javafx.scene.control /home/user/configs");
			System.exit(-1);
		}
		final String packageName = argv[0];
		final String outputFolder = argv[1];
		final List<Class<?>> controls = findControls(packageName);
		final char[] buffer = new char[1024];
		for (final Class<?> clazz : controls) {
			final String sourceFile = "/" + ControlsConfigGenerator.class.getPackageName().replace('.', '/')
					+ "/template.properties";
			System.out.println("Reading from source file: " + sourceFile);
			try (InputStreamReader reader = new InputStreamReader(
					ControlsConfigGenerator.class.getResourceAsStream(sourceFile), StandardCharsets.UTF_8)) {
				writeControlsConfigProperties(outputFolder, buffer, clazz, reader);
			} catch (final IOException e) {
			}
		}
	}

}
