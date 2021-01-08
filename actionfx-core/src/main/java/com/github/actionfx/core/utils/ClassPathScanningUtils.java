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
package com.github.actionfx.core.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * Utils for scanning the classpath.
 * 
 * @author koster
 *
 */
public class ClassPathScanningUtils {

	private ClassPathScanningUtils() {
		// class can not be instantiated
	}

	/**
	 * Scans the classpath under the given {@code packagePattern} for classes that
	 * carry the given annotation of type {@code annotationType}.
	 * 
	 * @param <A>            the annotation type parameter
	 * @param rootPackage    the root package to scan (sub-packages are scanned
	 *                       recursively)
	 * @param annotationType the annotation type
	 * @return the list of classes found
	 */
	public static <A extends Annotation> List<Class<?>> findClassesWithAnnotation(String rootPackage,
			Class<A> annotationType) {
		List<Class<?>> foundClasses = new ArrayList<>();
		try (ScanResult result = new ClassGraph().enableClassInfo().enableAnnotationInfo().acceptPackages(rootPackage)
				.scan()) {

			ClassInfoList classInfos = result.getClassesWithAnnotation(annotationType.getName());
			classInfos.forEach(classInfo -> foundClasses.add(classInfo.loadClass(false)));
		}
		return foundClasses;
	}
}
