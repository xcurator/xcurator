/*
 *    Copyright (c) 2013, University of Toronto.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package edu.toronto.cs.xcurator.xml;

import edu.toronto.cs.xcurator.common.NsContext;
import edu.toronto.cs.xcurator.common.XPathFinder;
import edu.toronto.cs.xcurator.common.XmlParser;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ekzhu
 */
public class NsContextTest {

    @Test
    public void getNamespaceURITest() throws SAXException, IOException, ParserConfigurationException {
        Document dataDoc = (new XmlParser()).parse(
                NsContextTest.class.getResourceAsStream(
                        "/secxbrls/data/fb-20121231.xml"), -1);
        NamespaceContext nscontext = new NsContext(dataDoc.getDocumentElement());
        assertTrue("Namespace for prefix xbrli is incorrect or empty",
                nscontext.getNamespaceURI("xbrli").equals("http://www.xbrl.org/2003/instance"));
        assertTrue("Namespace for prefix country is incorrect or empty",
                nscontext.getNamespaceURI("country").equals("http://xbrl.sec.gov/country/2012-01-31"));
        assertTrue("Namespace for prefix dei is incorrect or empty",
                nscontext.getNamespaceURI("dei").equals("http://xbrl.sec.gov/dei/2012-01-31"));
        assertTrue("Namespace for prefix iso4217 is incorrect or empty",
                nscontext.getNamespaceURI("iso4217").equals("http://www.xbrl.org/2003/iso4217"));
        assertTrue("Namespace for prefix us-gaap is incorrect or empty",
                nscontext.getNamespaceURI("us-gaap").equals("http://fasb.org/us-gaap/2012-01-31"));
        assertTrue("Namespace for prefix fb is incorrect or empty",
                nscontext.getNamespaceURI("fb").equals("http://www.facebook.com/20121231"));
    }

    @Test
    public void getPrefixTest() throws SAXException, IOException, ParserConfigurationException {
        Document dataDoc = (new XmlParser()).parse(
                NsContextTest.class.getResourceAsStream(
                        "/secxbrls/data/fb-20121231.xml"), -1);
        NamespaceContext nscontext = new NsContext(dataDoc.getDocumentElement());
        assertTrue("Namespace for prefix xbrli is incorrect or empty",
                nscontext.getPrefix("http://www.xbrl.org/2003/instance").equals("xbrli"));
    }

    @Test(expected = NoSuchElementException.class)
    public void getPrefixesTest() throws SAXException, IOException, ParserConfigurationException {
        Document dataDoc = (new XmlParser()).parse(
                NsContextTest.class.getResourceAsStream(
                        "/secxbrls/data/fb-20121231.xml"), -1);
        NamespaceContext nscontext = new NsContext(dataDoc.getDocumentElement());
        Iterator it = nscontext.getPrefixes("http://www.xbrl.org/2003/instance");
        assertTrue("Namespace for prefix xbrli is incorrect or empty", it.hasNext());
        assertTrue("Namespace for prefix xbrli is incorrect or empty", it.next().equals("xbrli"));
        it.next(); // exception
    }

    @Test
    public void getDefaultNamespaceTest() throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        Document dataDoc = (new XmlParser()).parse(
                NsContextTest.class.getResourceAsStream(
                        "/secxbrls/data/msft-20130630.xml"), -1);
        NsContext nscontext = new NsContext(dataDoc.getDocumentElement());
        assertTrue(nscontext.hasNamespace(XMLConstants.DEFAULT_NS_PREFIX, "http://www.xbrl.org/2003/instance"));
        assertTrue(nscontext.hasNamespace("us-gaap", "http://fasb.org/us-gaap/2013-01-31"));

        // Test if the name space in xpath can be resolved
        XPathFinder xPathFinder = new XPathFinder();
        // For default namespace a : is still needed
        NodeList nl = xPathFinder.getNodesByPath("/:xbrl/us-gaap:NonoperatingIncomeExpense", dataDoc, nscontext);
        assertTrue(nl.getLength() > 0);
    }
}
