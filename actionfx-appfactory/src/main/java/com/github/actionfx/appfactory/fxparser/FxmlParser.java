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
package com.github.actionfx.appfactory.fxparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for FXML documents.
 *
 * @author koster
 *
 */
public class FxmlParser {

	/**
	 * Parses an FXML-document into an instance of {@index FxmlDocument} from the
	 * given {@code inputStream}.
	 *
	 * @param inputStream the input stream to parse the FXML document from
	 * @return the parse {@link FxmlDocument}
	 */
	public FxmlDocument parseFxml(final InputStream inputStream) {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			final SAXParser saxParser = factory.newSAXParser();
			saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			final FxmlHandler fxmlHandler = new FxmlHandler();
			saxParser.parse(inputStream, fxmlHandler);
			return fxmlHandler.getFxmlDocument();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new IllegalStateException("Unable to parse document using SAX parser!", e);
		}
	}

	private static class FxmlHandler extends DefaultHandler {

		private static final String ATTR_FXID = "fx:id";

		private static final String ATTR_ONACTION = "onAction";

		// lookup table: Simple node name -> fully qualified node name
		private final Map<String, String> imports = new TreeMap<>();

		// id -> simple node name
		private final Map<String, String> idNodesMap = new TreeMap<>();

		private FxmlElement rootElement = null;

		private FxmlElement currentElement = null;

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			final FxmlElement newElement = parseToFxmlElement(currentElement, qName, attributes);
			if (rootElement == null) {
				rootElement = newElement;
			}
			currentElement = newElement;

		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			if (currentElement.getParent() != null) {
				currentElement.getParent().getChildren().add(currentElement);
			}
			currentElement = currentElement.getParent();
		}

		/**
		 * Parses the given element {@code name} and its {@code attributes} to an
		 * instance of {@link FxmlDocument}.
		 *
		 * @param name       the element's name
		 * @param attributes the attributes of the element
		 * @return the created instance of {@link FxmlElement}
		 */
		private FxmlElement parseToFxmlElement(final FxmlElement parent, final String name,
				final Attributes attributes) {
			final String fxId = getAttributeValue(ATTR_FXID, attributes, null);
			final String onAction = getAttributeValue(ATTR_ONACTION, attributes, null);
			final FxmlElement fxmlElement = new FxmlElement(parent, name, imports.get(name));
			if (fxId != null) {
				fxmlElement.setId(fxId);
				idNodesMap.put(fxId, name);
			}
			fxmlElement.setOnActionProperty(onAction);
			return fxmlElement;
		}

		/**
		 * Gets the value of the attribute with name {@code attrName}. In case there is
		 * no attribute with that name, then {@code defaultValue} is returned.
		 *
		 * @param attrName     the attribute name to search for
		 * @param attributes   the list of attributes
		 * @param defaultValue the default value to return, in case {@code attrName} is
		 *                     not found in the provided list of {@code attributes}.
		 * @return the retrieved attribute value
		 */
		private String getAttributeValue(final String attrName, final Attributes attributes,
				final String defaultValue) {
			final int idx = attributes.getIndex(attrName);
			if (idx == -1) {
				return defaultValue;
			}
			return attributes.getValue(idx);
		}

		public FxmlDocument getFxmlDocument() {
			final FxmlDocument fxmlDocument = new FxmlDocument(rootElement, imports);
			fxmlDocument.getIdNodesMap().putAll(idNodesMap);
			return fxmlDocument;
		}

		@Override
		public void processingInstruction(final String target, final String data) throws SAXException {
			if ("import".equals(target)) {
				final String simpleClassName = data.substring(data.lastIndexOf('.') + 1);
				imports.put(simpleClassName, data);
			}
		}

	}
}
