/**
  * YFSXMLUtil.java
  *
  **/

package com.custom.yantra.util;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;

public class YFSXMLUtil
{
static DOMParser parser = new DOMParser();

	public static Document getDocument(String inXML, boolean isFile) throws Exception{
		if (inXML == null || inXML.length() == 0)
			return null;

		InputSource   inSource = null;

		if (!isFile)
		{
			StringReader strReader = new StringReader( inXML );
			inSource = new InputSource( strReader );
		}//if isFile == false
		else
		{
			FileReader fileReader = new FileReader( inXML );
			inSource = new InputSource( fileReader );
		}//else

		return getDocument( inSource );

	}//getDocument(String)

	public static Document getDocument(InputSource inSource) throws Exception{
		parser.reset();
		parser.parse( inSource );
		return parser.getDocument();

	}//getDocument(InputSource)

	public static String getXMLString(Document doc) throws Exception {
		if (doc == null)
			return "";
		
		OutputFormat outFmt = new OutputFormat( doc, "UTF-8", true );
		outFmt.setPreserveSpace( true );
		XMLSerializer xmlOP = new XMLSerializer( outFmt );
		StringWriter strWriter = new StringWriter();
		xmlOP.setOutputCharStream( strWriter );
		xmlOP.serialize( doc );
		return encodeSpecialChars (strWriter.toString());

	}//getXMLString(Document)

	public static	boolean hasSpecialChars(String sXMLString)
	{
		for (int i=0; i<sSpecialChars.length(); i++)
		{
			if(sXMLString.indexOf ((int)sSpecialChars.charAt(i)) >= 0)
				return true;
		}
		return false;
	}
	
	public static	String	encodeSpecialChars (String sXMLString) throws Exception
	{	
		if(!hasSpecialChars(sXMLString))
			return sXMLString;
			
		StringBuffer	sNewXMLString = new StringBuffer ();
		
        for(int i = 0; i < sXMLString.length(); i++)
        {
            char ch = sXMLString.charAt(i);
			int  iIdx = sSpecialChars.indexOf(ch);
			if (iIdx >= 0)
                sNewXMLString.append("&#" + (int)ch + ";");
			else
                sNewXMLString.append(ch);
		}
		return sNewXMLString.toString();
	}

	public static void writeToFile(Document doc, String fileName) throws Exception {
		if (doc == null)
			return;

		OutputFormat outFmt = new OutputFormat( doc, "UTF-8", true );
		outFmt.setPreserveSpace( true );
		XMLSerializer xmlOP = new XMLSerializer( outFmt );
		FileWriter fileWriter = new FileWriter( new File(fileName) );
		xmlOP.setOutputCharStream( fileWriter );
		xmlOP.serialize( doc );
		fileWriter.flush();
		fileWriter.close();

	}//writeToFile(Document,fileName)

	public static Element createElement(Document doc, String elementName, Object hashAttribs) {
		return createElement(doc, elementName, hashAttribs, false);
	}//

	private static Element createElement(Document doc, String elementName, Object hashAttribs,
 boolean isTextNode) {
		Element element = doc.createElement( elementName );
		if (hashAttribs == null)
			return element;

		if (hashAttribs instanceof String)
		{
			if (isTextNode)
				element.appendChild( doc.createTextNode( (String)hashAttribs ) );
		}//String
		else if (hashAttribs instanceof Hashtable)
		{
			Enumeration<?> e = ((Hashtable<?, ?>)hashAttribs).keys();
			while ( e.hasMoreElements() )
			{
				String attribName  = (String)e.nextElement();
				String attribValue = (String)((Hashtable<?, ?>)hashAttribs).get(attribName);
				element.setAttribute( attribName, attribValue );
			}//while
		}//Hashtable

		return element;

	}//createElement

	public static Element createTextElement(Document doc, String elementName, String textValue, 
Hashtable<?, ?> hashattribs) {
		Element element = doc.createElement( elementName );
		element.appendChild( doc.createTextNode( textValue ) );
		if (hashattribs == null)
			return element;

		Enumeration<?> e = hashattribs.keys();
		while ( e.hasMoreElements() )
		{
			String aName  = (String)e.nextElement();
			String aValue = (String)hashattribs.get( aName );
			element.setAttribute( aName, aValue );
		}//while

		return element;

	}//createTextElement(Document,String,String,Hashtable)

	public static Element createChild( Document doc, Element parentElement, 
String elementName, Object hashAttribs ) {
		Element childElement = createElement( doc, elementName, hashAttribs );
		parentElement.appendChild( childElement );
		return childElement;

	}//createChild(Document,Element,String,Object)

	public	static	String	getXMLField (String strXML, String strOpenTag, String strCloseTag)
	{
		// in case of nested calls that return a null
		if (strXML != null)
		{
			String strTestXML = new String(strXML.toUpperCase());
			int	idx1 = strTestXML.indexOf (strOpenTag.toUpperCase());
			if (strCloseTag == null)
				strCloseTag = "</"+strOpenTag.substring (1, strOpenTag.length()-1);
			int	idx2 = strTestXML.indexOf (strCloseTag.toUpperCase());
			if (idx1 >= 0 && idx2 >= 0)
				return strXML.substring (idx1+strOpenTag.length(), idx2);
		}
		return null;
	}

	public	static	String	getXMLElement (String strXML, String strOpenTag, String strCloseTag)
	{
		// in case of nested calls that return a null
		if (strXML != null)
		{
			String strTestXML = new String(strXML.toUpperCase());
			// look for <Tag>
			int	idx1 = strTestXML.indexOf (strOpenTag.toUpperCase());
			if (idx1 < 0)
				// look for <Tag >
				idx1 = strTestXML.indexOf (strOpenTag.toUpperCase().substring(0,strOpenTag.length()-1));
			if (strCloseTag == null)
				strCloseTag = "</"+strOpenTag.substring (1, strOpenTag.length()-1)+">";
			int	idx2 = strTestXML.indexOf (strCloseTag.toUpperCase());
			if (idx2 < 0 && idx1 >= 0)
			{
				idx2 = strTestXML.indexOf ("/>", idx1);
				strCloseTag = "/>";
			}
			if (idx1 >= 0 && idx2 >= 0)
				return strXML.substring (idx1, idx2+strCloseTag.length());
		}
		return null;
	}

	public	static	String	getXMLElement (String strXML, String strOpenTag)
	{
		return getXMLElement (strXML, strOpenTag, null);
	}
	
	public	static String getXMLField (String strXML, String strInTag, String strOpenTag, String strCloseTag)
	{
		String strOutTag = "</"+strInTag.substring (1, strInTag.length()-1);

		return getXMLField (getXMLField (strXML, strInTag, strOutTag), strOpenTag, strCloseTag);
	}

	public	static	String	getXMLField (String strXML, String strOpenTag)
	{
		return getXMLField (strXML, strOpenTag, null);
	}

	public static String getAttrValue(String strXML, String strAttr)
	{
        int idx1, idx2, idx3;
		String	strRet = "";
		        
        // find Attribute="Value"
        idx1 = strXML.indexOf(strAttr + "=");
	
        if (idx1 > 0)
		{
            idx1 = strXML.indexOf ("=", idx1);
			// find open quote
            idx2 = strXML.indexOf ("\"", idx1);
			// find close quote
            idx3 = strXML.indexOf ("\"", idx2 + 1);
            if (idx3 > idx2)
                strRet = strXML.substring (idx2 + 1, idx3);
		}
		return strRet;
	}

	// (R) and TM characters handled specially
	private static final String	sSpecialChars = "®™–—";

/*
Pusedo logic for other XML Field parsing routines

Public Function getXMLField(ByVal strXML As String, ByVal strOpenTag As String, Optional ByVal strCloseTag As String) As String
        Dim idx1 As Long, idx2 As Long, idx3 As Long

        ' look for "<Tag xxxx>" or <Tag>
        idx1 = InStr(strXML, Left$(strOpenTag, Len(strOpenTag) - 1))
        If (idx1 > 0) Then
            idx3 = InStr(idx1, strXML, ">")
            If (strCloseTag = "") Then
                strCloseTag = "</" + Mid$(strOpenTag, 2, Len(strOpenTag) - 1)
            End If
            idx2 = InStr(strXML, strCloseTag)
            If ((idx1 > 0) And (idx2 > 0) And (idx3 < idx2) And (idx3 > idx1)) Then
                getXMLField = Mid$(strXML, idx3 + 1, idx2 - idx3 - 1)
            End If
        End If
End Function

Public Function getNestedXMLField(ByVal strXML As String, ByVal strInTag As String, ByVal strOpenTag As String, Optional ByVal strCloseTag As String)
        getNestedXMLField = getXMLField(getXMLField(strXML, strInTag), strOpenTag, strCloseTag)
End Function

Public Function getOffsetOfXMLField(ByVal strXML As String, ByVal strOpenTag As String, Optional ByVal strCloseTag As String) As Long
        Dim idx1 As Long, idx2 As Long, idx3 As Long

        getOffsetOfXMLField = 0
        ' look for "<Tag xxxx>" or <Tag>
        idx1 = InStr(strXML, Left$(strOpenTag, Len(strOpenTag) - 1))
        If (idx1 > 0) Then
            idx3 = InStr(idx1, strXML, ">")
            If (strCloseTag = "") Then
                strCloseTag = "</" + Mid$(strOpenTag, 2, Len(strOpenTag) - 1)
            End If
            idx2 = InStr(strXML, strCloseTag)
            If ((idx1 > 0) And (idx2 > 0) And (idx3 < idx2) And (idx3 > idx1)) Then
                getOffsetOfXMLField = idx1
            End If
        End If
End Function

Public Function getLenOfXMLField(ByVal strXML As String, ByVal strOpenTag As String, Optional ByVal strCloseTag As String) As Long
        Dim idx1 As Long, idx2 As Long, idx3 As Long

        getLenOfXMLField = 0
        ' look for "<Tag xxxx>" or <Tag>
        idx1 = InStr(strXML, Left$(strOpenTag, Len(strOpenTag) - 1))
        If (idx1 > 0) Then
            idx3 = InStr(idx1, strXML, ">")
            If (strCloseTag = "") Then
                strCloseTag = "</" + Mid$(strOpenTag, 2, Len(strOpenTag) - 1)
            End If
            idx2 = InStr(strXML, strCloseTag) + Len(strCloseTag)
            If ((idx1 > 0) And (idx2 > 0) And (idx3 < idx2) And (idx3 > idx1)) Then
                getLenOfXMLField = idx2 - idx1
            End If
        End If
End Function

Public Function getAttrOfXMLField(ByVal strXML As String, ByVal strOpenTag As String, Optional ByVal strCloseTag As String) As String
        Dim idx1 As Long, idx2 As Long, idx3 As Long, idx4 As Long

        ' look for "<Tag xxxx>" or <Tag>
        idx1 = InStr(strXML, Left$(strOpenTag, Len(strOpenTag) - 1))
        idx3 = InStr(idx1, strXML, ">")
        idx4 = idx1 + Len(strOpenTag)
        If (strCloseTag = "") Then
            strCloseTag = "</" + Mid$(strOpenTag, 2, Len(strOpenTag) - 1)
        End If
        idx2 = InStr(strXML, strCloseTag) + Len(strCloseTag)
        If ((idx1 > 0) And (idx2 > 0) And (idx3 < idx2) And (idx3 > idx1)) Then
            getAttrOfXMLField = Trim$(Mid$(strXML, idx4, idx3 - idx4))
        Else
            getAttrOfXMLField = ""
        End If
End Function

Public Function getAttrValue(ByVal strXML As String, ByVal strAttr As String)
        Dim idx1 As Long, idx2 As Long, idx3 As Long
        
        ' find Attribute="Value"
        idx1 = InStr(strXML, strAttr & "=")
        If (idx1 > 0) Then
            idx1 = InStr(idx1, strXML, "=")
            idx2 = InStr(idx1, strXML, Chr$(34))
            idx3 = InStr(idx2 + 1, strXML, Chr$(34))
            If (idx3 > idx2) Then
                getAttrValue = Mid$(strXML, idx2 + 1, idx3 - idx2 - 1)
            Else
                getAttrValue = ""
            End If
        Else
            getAttrValue = ""
        End If
End Function
*/



};//class YFSXMLUtil

