/**
  * YFSXMLParser.java
  *
  **/

package com.custom.yantra.util;

import java.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

public class YFSXMLParser
{
	private Document doc;
	private Element  rootElement;

	public YFSXMLParser() throws Exception {
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}//

	public YFSXMLParser( Document inXML ) {
		this.doc = inXML;
	}//

	public YFSXMLParser( String inXML, boolean isFile) throws Exception {
		doc = YFSXMLUtil.getDocument( inXML, isFile );
	}//

	public Document getDocument() {
		return doc;
	}//

	public void reset() {
		doc = null;
		rootElement = null;
	}//

	public Element getRootElement() {
		return doc.getDocumentElement();
	}//

	public String getXMLString( Document doc ) throws Exception {
		return YFSXMLUtil.getXMLString( doc );
	}//

	public void writeToFile( String fileName ) throws Exception {
		YFSXMLUtil.writeToFile( doc, fileName );
	}//

	public Element createRootElement( String elementName, Object hashAttribs ) {
		rootElement = YFSXMLUtil.createElement(doc, elementName, hashAttribs);
		doc.appendChild( rootElement );
		return rootElement;
	}//

	public Element createTextElement( String elementName, String textValue, Hashtable<?, ?> hashAttribs ) {
		return YFSXMLUtil.createTextElement( doc, elementName, textValue, hashAttribs );
	}//

	public Element createElement( String elementName, Object hashAttribs ) {
		Element element = YFSXMLUtil.createElement( doc, elementName, hashAttribs );
		if (rootElement != null)
			rootElement.appendChild( element );

		return element;
	}//

	public Element createChild( Element parentElement, String elementName, Object hashAttribs) {
		return YFSXMLUtil.createChild( doc, parentElement, elementName, hashAttribs );
	}//

	public Element createTextChild( Element parentElement, String elementName, 
String textValue, Hashtable<?, ?> hashattribs ) {
		Element childElement = YFSXMLUtil.createTextElement( doc, elementName, textValue, hashattribs );
		parentElement.appendChild( childElement );
		return childElement;
	}//

	public List<Node> getElements( String elementName ) {
		return getElements( null, elementName );
	}//

	public List<Node> getElements( Element startElement, String elementName ) {
		NodeList nodeList;
		if ( startElement == null )
			nodeList = doc.getElementsByTagName( elementName );
		else
			nodeList = startElement.getElementsByTagName( elementName );

		List<Node> elementList = new ArrayList<Node>();
		for (int count=0; count<nodeList.getLength() ; count++)
			elementList.add( nodeList.item( count ) );

		return elementList;
	}//

};//class YFSXMLParser

