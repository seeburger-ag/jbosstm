/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.adapters;

import java.util.ArrayList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.arjuna.webservices.logging.WSCLogger;


/**
 * XMLStreamReader for reading from a DOM tree.
 * @author kevin
 * 
 */
public class DOMXMLStreamReader implements XMLStreamReader
{
    /**
     * The empty location.
     */
    private static final Location EMPTY_LOCATION = new EmptyLocation() ;
    
    /**
     * The header element.
     */
    private final Element headerElement ;
    /**
     * The current node.
     */
    private Node currentNode ;
    /**
     * The current type ;
     */
    private int type ;
    /**
     * The namespace context.
     */
    private NamespaceContextImpl namespaceContext ;
    /**
     * The attributes for the current element.
     */
    private Attr[] attributes ;
    
    /**
     * Construct the DOM XMLStreamReader.
     * @param headerElement The header element.
     */
    public DOMXMLStreamReader(final Element headerElement)
    {
        this.headerElement = headerElement ;
        startElement(headerElement) ;
    }

    /**
     * Get the next parsing event.
     * @return The integer code corresponding to the parse event.
     */
    public int next()
        throws XMLStreamException
    {
        // just returns start, end, characters, CData
        if ((currentNode == headerElement) && (type == END_ELEMENT))
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_1()) ;
        }
        else if (type == START_ELEMENT)
        {
            if (currentNode.hasChildNodes())
            {
                final Node child = currentNode.getFirstChild() ;
                processChild(child) ;
            }
            else
            {
                type = END_ELEMENT ;
            }
            return type ;
        }
        else if ((type == CHARACTERS) || (type == CDATA) || (type == END_ELEMENT))
        {
            if (type == END_ELEMENT)
            {
                namespaceContext = namespaceContext.getParent() ;
            }
            final Node sibling = currentNode.getNextSibling() ;
            if (sibling != null)
            {
                processChild(sibling) ;
            }
            else
            {
                type = END_ELEMENT ;
                currentNode = currentNode.getParentNode() ;
            }
        }
        else
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_3(Integer.toString(type))) ;
        }
        return type ;
    }

    /**
     * Get the next tag event.
     * @return The integer code corresponding to the parse event.
     */
    public int nextTag()
        throws XMLStreamException
    {
        final int nextType = next() ;
        if ((nextType != START_ELEMENT) && (nextType != END_ELEMENT))
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_4(Integer.toString(nextType))) ;
        }
        return nextType ;
    }

    /**
     * Get the current event type.
     * @param The current event type.
     */
    public int getEventType()
    {
        return type ;
    }

    /**
     * Returns true if there is another parsing event.
     * @return true if there is another parsing event, false otherwise.
     */
    public boolean hasNext()
        throws XMLStreamException
    {
        return ((currentNode != headerElement) || (type != END_ELEMENT)) ;
    }

    /**
     * Does the current event have a qualified name?
     * @return true if a qualified name, false otherwise.
     */
    public boolean hasName()
    {
        return ((type == START_ELEMENT) || (type == END_ELEMENT)) ;
    }

    /**
     * Returns the qualified name of the start/end element event.
     * @return the tag qualified name.
     */
    public QName getName()
    {
        if (hasName())
        {
            return qualifiedName(currentNode) ;
        }
        return null ;
    }

    /**
     * Returns the local name of the start/end element or entity reference event.
     * @return the local name.
     */
    public String getLocalName()
    {
        return (hasName() ? ((Element)currentNode).getLocalName() : null) ;
    }

    /**
     * Get the prefix of the current event or null.
     * @return the prefix.
     */
    public String getPrefix()
    {
        return (hasName() ? ((Element)currentNode).getPrefix() : null) ;
    }

    /**
     * Get the namespace URI of the current event.
     * @return the namespace URI.
     */
    public String getNamespaceURI()
    {
        return (hasName() ? ((Element)currentNode).getNamespaceURI() : null) ;
    }

    /**
     * Test the current event type.
     * @param type The event type.
     * @param namespaceURI The namespace URI.
     * @param localName The local name.
     */
    public void require(final int type, final String namespaceURI, final String localName)
        throws XMLStreamException
    {
        if ((type != this.type) || (hasName() && !(testEquals(namespaceURI, getNamespaceURI()) && testEquals(localName, getLocalName()))))
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_5()) ;
        }
    }

    /**
     * Is the current event a start tag?
     * @return true if a start tag, false otherwise.
     */
    public boolean isStartElement()
    {
        return (type == START_ELEMENT) ;
    }

    /**
     * Is the current event an end tag?
     * @return true if an end tag, false otherwise.
     */
    public boolean isEndElement()
    {
        return (type == END_ELEMENT) ;
    }

    /**
     * Is the current event a character event?
     * @return true if a character event, false otherwise.
     */
    public boolean isCharacters()
    {
        return ((type == CHARACTERS) || (type == CDATA)) ;
    }

    /**
     * Is the current event a whitespace event?
     * @return true if a whitespace event, false otherwise.
     */
    public boolean isWhiteSpace()
    {
        return false ;
    }

    /**
     * Get the number of attributes on the element.
     * @return the number of attributes.
     */
    public int getAttributeCount()
    {
        if (hasName())
        {
            return (attributes == null ? 0 : attributes.length) ;
        }
        return 0 ;
    }

    /**
     * Get the qualified name of the specified attribute.
     * @param index The attribute index.
     * @return The attribute qualified name.
     */
    public QName getAttributeName(final int index)
    {
        final Node node = getAttribute(index) ;
        return (node == null ? null : qualifiedName(node)) ;
    }

    /**
     * Get the namespace of the specified attribute.
     * @param index The attribute index.
     * @return The attribute namespace.
     */
    public String getAttributeNamespace(final int index)
    {
        final Node node = getAttribute(index) ;
        return (node == null ? null : node.getNamespaceURI()) ;
    }

    /**
     * Get the local name of the specified attribute.
     * @param index The attribute index.
     * @return The attribute local name.
     */
    public String getAttributeLocalName(final int index)
    {
        final Node node = getAttribute(index) ;
        if (node != null)
        {
            final String localName = node.getLocalName() ;
            return (localName == null ? node.getNodeName() : localName) ;
        }
        return null ;
    }

    /**
     * Get the prefix of the specified attribute.
     * @param index The attribute index.
     * @return The attribute prefix.
     */
    public String getAttributePrefix(final int index)
    {
        final Node node = getAttribute(index) ;
        return (node == null ? null : node.getPrefix()) ;
    }

    /**
     * Get the type of the specified attribute.
     * @param index The attribute index.
     * @return The attribute type.
     */
    public String getAttributeType(final int index)
    {
        return null ;
    }

    /**
     * Get the value of the specified attribute.
     * @param index The attribute index.
     * @return The attribute value.
     */
    public String getAttributeValue(final int index)
    {
        final Node node = getAttribute(index) ;
        return (node == null ? null : node.getNodeValue()) ;
    }

    /**
     * Get the value of the specified attribute.
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName The local name of the attribute.
     * @return The attribute value.
     */
    public String getAttributeValue(final String namespaceURI, final String localName)
    {
        if (hasName())
        {
            final Element currentElement = (Element)currentNode ;
            final Attr attr = currentElement.getAttributeNodeNS(namespaceURI, localName) ;
            return attr.getValue() ;
        }
        return null ;
    }
    
    /**
     * Was this attribute created by default?
     * @return true if created by default, false otherwise.
     */
    public boolean isAttributeSpecified(final int index)
    {
        return false ;
    }

    /**
     * Get the number of namespaces declared on this star/end element.
     */
    public int getNamespaceCount()
    {
        return namespaceContext.getNamespaceCount() ;
    }

    /**
     * Get the prefix for the specified namespace declaration.
     * @param index The namespace declaration index.
     */
    public String getNamespacePrefix(final int index)
    {
        return namespaceContext.getPrefix(index) ;
    }

    /**
     * Get the URI for the specified namespace declaration.
     * @param index The namespace declaration index.
     */
    public String getNamespaceURI(final int index)
    {
        return namespaceContext.getNamespaceURI(index) ;
    }

    /**
     * Get the URI for the namespace prefix.
     * @param prefix The namespace prefix.
     */
    public String getNamespaceURI(final String prefix)
    {
        return namespaceContext.getNamespaceURI(prefix) ;
    }

    /**
     * Does the current event have text?
     * @return true if the current event has text, false otherwise.
     */
    public boolean hasText()
    {
        return ((type == CHARACTERS) || (type == CDATA)) ;
    }

    /**
     * Get the current event as a string.
     * @return The current event as a string.
     */
    public String getText()
    {
        if (hasText())
        {
            return ((Text)currentNode).getData() ;
        }
        return null ;
    }

    /**
     * Get the characters from the current event.
     * @return The characters from the current event.
     */
    public char[] getTextCharacters()
    {
        if (hasText())
        {
            return ((Text)currentNode).getData().toCharArray() ;
        }
        return null ;
    }

    /**
     * Get the characters from the current event.
     * @param sourceStart The start index of the source.
     * @param target The target array.
     * @param targetStart The start index of the target.
     * @param length The maximum length of the target.
     * @return The number of characters copied.
     */
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length)
            throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_6()) ;
    }

    /**
     * Get the index of the first character.
     * @return the index of the first character.
     */
    public int getTextStart()
    {
        return 0 ;
    }

    /**
     * Get the length of the text.
     * @return the length of the text.
     */
    public int getTextLength()
    {
        if (hasText())
        {
            return ((Text)currentNode).getData().length() ;
        }
        return 0 ;
    }

    /**
     * Get the content of a text only element.
     * @return The text content.
     */
    public String getElementText()
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_7()) ;
    }

    /**
     * Get the current namespace context.
     * @return the namespace context.
     */
    public NamespaceContext getNamespaceContext()
    {
        return namespaceContext ;
    }

    /**
     * Return the input encoding.
     * @return the input encoding or null.
     */
    public String getEncoding()
    {
        return null ;
    }

    /**
     * Get the location.
     * @return the location.
     */
    public Location getLocation()
    {
        return EMPTY_LOCATION ;
    }

    /**
     * Get the XML version.
     * @return the XML version or null if not declared.
     */
    public String getVersion()
    {
        return null ;
    }

    /**
     * Get the standalone from the XML declaration.
     * @return true if standalone, false otherwise.
     */
    public boolean isStandalone()
    {
        return true ;
    }

    /**
     * Was standalone set in the document?
     * @return true if set, false otherwise.
     */
    public boolean standaloneSet()
    {
        return false ;
    }

    /**
     * Return the character encoding.
     * @return the character encoding or null.
     */
    public String getCharacterEncodingScheme()
    {
        return null ;
    }

    /**
     * Get the target of a processing instruction.
     * @return the processing instruction target.
     */
    public String getPITarget()
    {
        return null ;
    }

    /**
     * Get the data of a processing instruction.
     * @return the processing instruction data.
     */
    public String getPIData()
    {
        return null ;
    }

    /**
     * Get the value of the specified property.
     * @param name The name of the property.
     * @return the property value.
     */
    public Object getProperty(final String name)
        throws IllegalArgumentException
    {
        return null ;
    }

    /**
     * Close the stream.
     */
    public void close()
        throws XMLStreamException
    {
        // Do nothing
    }
    
    /**
     * Process the child node.
     * @param child The child node.
     * @throws XMLStreamException for unsupported node types.
     */
    private void processChild(final Node child)
        throws XMLStreamException
    {
        if (child instanceof CDATASection)
        {
            currentNode = child ;
            type = CDATA ;
        }
        else if (child instanceof Text)
        {
            currentNode = child ;
            type = CHARACTERS ;
        }
        else if (child instanceof Element)
        {
            startElement(child) ;
        }
        else
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_DOMXMLStreamReader_2(child.getClass().getName())) ;
        }
    }
    
    /**
     * Start an element.
     * @param element The new element.
     */
    private void startElement(final Node element)
    {
        type = START_ELEMENT ;
        currentNode = element ;
        namespaceContext = new NamespaceContextImpl(namespaceContext) ;
        
        final NamedNodeMap attrMap = ((Element)element).getAttributes() ;
        final int numAttributes = (attrMap == null ? 0 : attrMap.getLength()) ;
        attributes = null ;
        if (numAttributes > 0)
        {
            final ArrayList attributeList = new ArrayList() ;
            
            for(int count = 0 ; count < numAttributes ; count++)
            {
                final Node attr = attrMap.item(count) ;
                final QName attrName = qualifiedName(attr) ;
                
                final String prefix = attrName.getPrefix() ;
                final String localName = attrName.getLocalPart() ;
                if ("xmlns".equals(prefix))
                {
                    namespaceContext.setPrefix(localName, attr.getNodeValue()) ;
                }
                else if ((prefix == null) && "xmlns".equals(localName))
                {
                    namespaceContext.setDefaultNamespace(attr.getNodeValue()) ;
                }
                else
                {
                    attributeList.add(attr) ;
                }
            }
            
            if (attributeList.size() > 0)
            {
                attributes = (Attr[]) attributeList.toArray(new Attr[attributeList.size()]) ;
            }
        }
    }
    
    /**
     * Test object references for equality.
     * @param lhs The first object.
     * @param rhs The second object.
     * @return true if equals or both null, false otherwise,
     */
    private boolean testEquals(final Object lhs, final Object rhs)
    {
        if (lhs == null)
        {
            return (rhs == null) ;
        }
        return (lhs.equals(rhs)) ;
    }
    
    /**
     * Get the attribute with the specified index.
     * @param index The attribute index.
     * @return The attribute node or null if the index is invalid.
     */
    private Node getAttribute(final int index)
    {
        return (attributes != null ? attributes[index] : null) ;
    }
    
    /**
     * Get the qualified name of the node.
     * @param node The node.
     * @return The qualified name.
     */
    private QName qualifiedName(final Node node)
    {
        final String localName = node.getLocalName() ;
        if (localName == null)
        {
            return new QName(node.getNodeName()) ;
        }
        final String prefix = node.getPrefix() ;
        final String namespaceURI = node.getNamespaceURI() ;
        if (prefix == null)
        {
            return new QName(namespaceURI, localName) ;
        }
        return new QName(namespaceURI, localName, prefix) ;
    }
}
