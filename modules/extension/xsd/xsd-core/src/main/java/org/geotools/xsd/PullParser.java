/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2012-2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xsd;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.geotools.xsd.impl.ElementHandler;
import org.geotools.xsd.impl.NodeImpl;
import org.geotools.xsd.impl.ParserHandler;
import org.geotools.xsd.impl.ParserHandler.ContextCustomizer;
import org.xml.sax.SAXException;

/**
 * XML pull parser capable of streaming.
 *
 * <p>Similar in nature to {@link StreamingParser} but based on XPP pull parsing rather than SAX.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class PullParser {

    PullParserHandler handler;
    XMLStreamReader pp;

    Attributes atts = new Attributes();

    public PullParser(Configuration config, InputStream input, QName element) {
        this(config, input, new ElementPullParserHandler(element, config));
    }

    public PullParser(Configuration config, InputStream input, Class type) {
        this(config, input, new TypePullParserHandler(type, config));
    }

    public PullParser(Configuration config, InputStream input, Object... handlerSpecs) {
        this(config, input, new OrPullParserHandler(config, handlerSpecs));
    }

    public PullParser(Configuration config, InputStream input, PullParserHandler handler) {
        this.handler = handler;
        pp = createPullParser(input);
    }

    public void setContextCustomizer(ContextCustomizer contextCustomizer) {
        handler.setContextCustomizer(contextCustomizer);
    }

    /** Sets if the parsing should be strict or not */
    public void setStrict(boolean strict) {
        this.handler.setStrict(strict);
    }

    /** Changes the URIHandler for this parser */
    public void setURIHandler(URIHandler uriHandler) {
        this.handler.getURIHandlers().clear();
        this.handler.getURIHandlers().add(uriHandler);
    }

    public Object parse() throws XMLStreamException, IOException, SAXException {
        if (handler.getLogger() == null) {
            handler.startDocument();
        }

        while (pp.hasNext()) {
            int e = pp.next();

            switch (e) {
                case XMLStreamReader.START_ELEMENT:
                    int count = pp.getNamespaceCount();
                    for (int i = 0; i < count; i++) {
                        String pre = pp.getNamespacePrefix(i);
                        handler.startPrefixMapping(pre != null ? pre : "", pp.getNamespaceURI(i));
                    }

                    handler.startElement(pp.getNamespaceURI(), pp.getLocalName(), str(pp.getName()), atts);

                    break;

                case XMLStreamReader.CHARACTERS:
                    char[] chars = pp.getTextCharacters();
                    handler.characters(chars, pp.getTextStart(), pp.getTextLength());
                    break;

                case XMLStreamReader.END_ELEMENT:
                    handler.endElement(pp.getNamespaceURI(), pp.getLocalName(), str(pp.getName()));

                    count = pp.getNamespaceCount();
                    // undeclare them in reverse order
                    for (int i = count - 1; i >= 0; i--) {
                        handler.endPrefixMapping(pp.getNamespacePrefix(i));
                    }

                    // check whether to break out
                    if (handler.getObject() != null) {
                        return handler.getObject();
                    }

                    break;

                case XMLStreamReader.END_DOCUMENT:
                    handler.endDocument();

                    break;
            }
        }

        return null;
    }

    QName qName(String prefix, String name, XMLStreamReader pp2) {
        if (prefix != null) {
            return new QName(pp.getNamespaceURI(prefix), name, prefix);
        } else {
            return new QName(name);
        }
    }

    XMLStreamReader createPullParser(InputStream input) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        // disable DTDs
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        // disable external entities
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try {
            return factory.createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error creating pull parser", e);
        }
    }

    String str(QName qName) {
        return qName.getPrefix() != null ? qName.getPrefix() + ":" + qName.getLocalPart() : qName.getLocalPart();
    }

    class Attributes implements org.xml.sax.Attributes {

        @Override
        public int getLength() {
            return pp.getAttributeCount();
        }

        @Override
        public String getURI(int index) {
            return pp.getAttributeNamespace(index);
        }

        @Override
        public String getLocalName(int index) {
            return pp.getAttributeLocalName(index);
        }

        @Override
        public String getQName(int index) {
            final String prefix = pp.getAttributePrefix(index);
            if (prefix != null) {
                return prefix + ':' + pp.getAttributeName(index);
            } else {
                return str(pp.getAttributeName(index));
            }
        }

        @Override
        public String getType(int index) {
            return pp.getAttributeType(index);
        }

        @Override
        public String getValue(int index) {
            return pp.getAttributeValue(index);
        }

        @Override
        public int getIndex(String uri, String localName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeNamespace(i).equals(uri)
                        && pp.getAttributeLocalName(i).equals(localName)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getIndex(String qName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if ((pp.getAttributePrefix(i) + ":" + pp.getAttributeLocalName(i)).equals(qName)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public String getType(String uri, String localName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if (pp.getAttributeNamespace(i).equals(uri)
                        && pp.getAttributeLocalName(i).equals(localName)) {
                    return pp.getAttributeType(i);
                }
            }
            return null;
        }

        @Override
        public String getType(String qName) {
            for (int i = 0; i < pp.getAttributeCount(); i++) {
                if ((pp.getAttributePrefix(i) + ":" + pp.getAttributeName(i)).equals(qName)) {
                    return pp.getAttributeType(i);
                }
            }
            return null;
        }

        @Override
        public String getValue(String uri, String localName) {
            return pp.getAttributeValue(uri, localName);
        }

        @Override
        public String getValue(String qName) {
            return pp.getAttributeValue(null, qName);
        }
    }

    abstract static class PullParserHandler extends ParserHandler {

        PullParser parser;
        Object object;

        public PullParserHandler(Configuration config) {
            super(config);
        }

        @Override
        protected void endElementInternal(ElementHandler handler) {
            object = null;
            if (stop(handler)) {
                object = handler.getParseNode().getValue();

                // remove this node from parse tree
                if (handler.getParentHandler() instanceof ElementHandler) {
                    ElementHandler parent = (ElementHandler) handler.getParentHandler();
                    ((NodeImpl) parent.getParseNode()).removeChild(handler.getParseNode());
                }
            }
        }

        public Object getObject() {
            return object;
        }

        protected abstract boolean stop(ElementHandler handler);
    }

    static class TypePullParserHandler extends PullParserHandler {
        Class type;

        public TypePullParserHandler(Class type, Configuration config) {
            super(config);
            this.type = type;
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            return type.isInstance(handler.getParseNode().getValue());
        }
    }

    static class ElementPullParserHandler extends PullParserHandler {
        QName element;

        public ElementPullParserHandler(QName element, Configuration config) {
            super(config);
            this.element = element;
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            boolean equal = false;
            if (element.getNamespaceURI() != null) {
                equal = element.getNamespaceURI().equals(handler.getComponent().getNamespace());
            } else {
                equal = handler.getComponent().getNamespace() == null;
            }
            return equal && element.getLocalPart().equals(handler.getComponent().getName());
        }
    }

    static class ElementIgnoringNamespacePullParserHandler extends ElementPullParserHandler {
        public ElementIgnoringNamespacePullParserHandler(QName element, Configuration config) {
            super(element, config);
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            return element.getLocalPart().equals(handler.getComponent().getName());
        }
    }

    // aggregate the other handlers, and stop if any of them want to stop
    static class OrPullParserHandler extends PullParserHandler {
        private final Collection<PullParserHandler> parserHandlers;

        public OrPullParserHandler(Configuration config, Object... handlerSpecs) {
            super(config);
            Collection<PullParserHandler> handlers = new ArrayList<>(handlerSpecs.length);
            for (Object spec : handlerSpecs) {
                if (spec instanceof Class) {
                    handlers.add(new TypePullParserHandler((Class<?>) spec, config));
                } else if (spec instanceof QName) {
                    // TODO ignoring the namespace
                    handlers.add(new ElementIgnoringNamespacePullParserHandler((QName) spec, config));
                } else if (spec instanceof PullParserHandler) {
                    handlers.add((PullParserHandler) spec);
                } else {
                    throw new IllegalArgumentException(
                            "Unknown element: " + spec.toString() + " of type: " + spec.getClass());
                }
            }
            parserHandlers = Collections.unmodifiableCollection(handlers);
        }

        @Override
        protected boolean stop(ElementHandler handler) {
            for (PullParserHandler pph : parserHandlers) {
                if (pph.stop(handler)) {
                    return true;
                }
            }
            return false;
        }
    }
}
