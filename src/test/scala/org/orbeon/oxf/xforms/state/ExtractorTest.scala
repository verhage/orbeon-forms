/**
 *  Copyright (C) 2016 Orbeon, Inc.
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version
 *  2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms.state

import org.junit.Test
import org.orbeon.oxf.test.DocumentTestBase
import org.orbeon.oxf.util.WhitespaceMatching
import org.orbeon.oxf.xforms.XFormsConstants
import org.orbeon.oxf.xforms.analysis.{Metadata, XFormsAnnotator, XFormsExtractor}
import org.orbeon.oxf.xml.XMLParsing.ParserConfiguration._
import org.orbeon.oxf.xml.{JXQName, _}
import org.orbeon.scaxon.SAXEvents._
import org.scalatest.junit.AssertionsForJUnit
import org.xml.sax.Attributes

import scala.collection.mutable.ListBuffer

class ExtractorTest extends DocumentTestBase with AssertionsForJUnit {

  class Collector extends XMLReceiverAdapter {

    import org.orbeon.scaxon.SAXEvents._

    private var _events = ListBuffer[SAXEvent]()
    def events = _events.result

    override def startDocument()                                                               : Unit = _events += StartDocument
    override def endDocument()                                                                 : Unit = _events += EndDocument
    override def startPrefixMapping(prefix: String, uri: String)                               : Unit = _events += StartPrefixMapping(prefix, uri)
    override def endPrefixMapping(prefix: String)                                              : Unit = _events += EndPrefixMapping(prefix)
    override def startElement(uri: String, localName: String, qName: String, atts: Attributes) : Unit = _events += StartElement(uri, localName, qName, atts)
    override def endElement(uri: String, localName: String, qName: String)                     : Unit = _events += EndElement(uri, localName, qName)
  }

  @Test def issue1897namespacesWithXIncludes(): Unit = {

    val url = "oxf:/org/orbeon/oxf/xforms/state/form-with-include.xhtml"

    val metadata = new Metadata
    val template = AnnotatedTemplate(new SAXStore)

    val extractorCollector = new Collector

    // Filter out properties and other elements which we don't want to test
    val ElementsToExclude = Set("properties", "last-id", "template")
    val extractorOutput = new ElementFilterXMLReceiver(
      extractorCollector,
      (level, uri, localname) ⇒ ! ElementsToExclude(localname)
    )

    XMLParsing.urlToSAX(
      url,
      new WhitespaceXMLReceiver(
        new XFormsAnnotator(
          new SAXStore,
          new XFormsExtractor(
            new WhitespaceXMLReceiver(
              extractorOutput,
              WhitespaceMatching.defaultBasePolicy,
              WhitespaceMatching.basePolicyMatcher
            ),
            metadata,
            template,
            ".",
            XFormsConstants.XXBLScope.inner,
            true,
            false,
            false
          ),
          metadata,
          true
        ),
        WhitespaceMatching.defaultHTMLPolicy,
        WhitespaceMatching.htmlPolicyMatcher
      ),
      XINCLUDE_ONLY,
      false
    )

    import javax.xml.namespace.{QName ⇒ JQName}
    import JXQName._

    val XMLURI    = "http://www.w3.org/XML/1998/namespace"
    val XFormsURI = "http://www.w3.org/2002/xforms"

    val Id     : JQName = "id"
    val XMLBase: JQName = XMLURI → "base"

    val ExpectedEvents: List[SAXEvent] = List(
      StartDocument,
        StartElement("static-state", List("is-html" → "true")),
          StartElement("root", List("id" → "#document")),
            StartPrefixMapping("xh", "http://www.w3.org/1999/xhtml"),
            StartPrefixMapping("xf", "http://www.w3.org/2002/xforms"),
            StartPrefixMapping("xi", "http://www.w3.org/2001/XInclude"),
            StartElement(
              XFormsURI → "model",
              List(Id → "my-model", XMLBase → ".")
            ),
            EndElement(XFormsURI → "model"),
            EndPrefixMapping("xh"),
            EndPrefixMapping("xf"),
            EndPrefixMapping("xi"),
            StartPrefixMapping("xh", "http://www.w3.org/1999/xhtml"),
            StartPrefixMapping("xf", "http://www.w3.org/2002/xforms"),
            StartPrefixMapping("xi", "http://www.w3.org/2001/XInclude"),
            StartElement(
              XFormsURI → "group",
              List(Id → "my-group", XMLBase → ".")
            ),
              StartPrefixMapping("xi", ""),
              StartPrefixMapping("foo", "http://example.org/foo"),
              StartElement(XFormsURI → "var", List("id" → "my-var1", "name" → "lang", "value" → "()")),
              EndElement(XFormsURI → "var"),
              EndPrefixMapping("xi"),
              EndPrefixMapping("foo"),
              // This is what we really want to test
              StartPrefixMapping("xi", ""),
              StartPrefixMapping("foo", "http://example.org/foo"),
              StartElement(
                XFormsURI → "var",
                List("id" → "my-var2", "name" → "v2", "value" → "foo:bar()")
              ),
              EndElement(XFormsURI → "var"),
              EndPrefixMapping("xi"),
              EndPrefixMapping("foo"),
            EndElement(XFormsURI → "group"),
            EndPrefixMapping("xh"),
            EndPrefixMapping("xf"),
            EndPrefixMapping("xi"),
          EndElement("root"),
          // This is output by the extractor around properties
          StartPrefixMapping("xxf", "http://orbeon.org/oxf/xml/xforms"),
          EndPrefixMapping("xxf"),
        EndElement("static-state"),
      EndDocument
    )

    assert(ExpectedEvents === extractorCollector.events)
  }
}