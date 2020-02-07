/**
  * ATTMapperUtils.java
  *
  **/

// PACKAGE
package com.custom.att.xmlmapper;

import java.util.*;
import com.custom.yantra.util.*;
import org.w3c.dom.*;
import com.custom.yantra.xmlmapper.*;
import com.yantra.yfc.dom.*;

public class ATTMapperUtils extends YantraMapperUtils
{
    public ATTMapperUtils()
    {
		super();
    }

	public static void setDefaultOrgCode (String sDefaultOrgCode)
	{
		strDefaultOrgCode =	sDefaultOrgCode;
	}
	
	public	static String getDefaultOrgCode ()
	{
		return strDefaultOrgCode;
	}
	
	public static Document	removeSOAPEnvelope(Document docIn, String sServiceName) throws Exception
	{
		String			sDoc  = YFSXMLUtil.getXMLString (docIn);
		return removeSOAPEnvelope (sDoc, sServiceName);
//		return removeSOAPEnvelope (YFCDocument.getDocumentFor (docIn));
	}
	
	public static Document	removeSOAPEnvelope(String sDoc, String sServiceName) throws Exception
	{
/*
		String			sBody = YFSXMLUtil.getXMLField (sDoc, "<soap:Body>");
		YFSXMLParser	docNew = null;
		
		if (sBody != null && sBody.length() > 0)
		{
			if (YFSUtil.getDebug())
			{
				System.out.println ("SOAP Message Received:");
				System.out.println (sDoc);
				System.out.println ("\r\n");
				System.out.println ("\r\nStripping SOAP Envelope-Resulting Document:");
				System.out.println (sBody);
				System.out.println ("\r\n");
			}
			docNew = new YFSXMLParser (strXMLHeader+sBody, false);
		}
		return docNew.getDocument();	
*/
		YFCDocument docSoap = YFCDocument.getDocumentFor (sDoc);
		return removeSOAPEnvelope (docSoap, sServiceName);
	}
	
	@SuppressWarnings("rawtypes")
	public static Document	removeSOAPEnvelope(YFCDocument docIn, String sServiceName) throws Exception
	{
		YFCElement	eleSoap = docIn.getDocumentElement();
		YFCElement	eleBody = (YFCElement)eleSoap.getChildElement ("soap:Body");
		String		sBody;
		if (eleBody != null)
		{
			Iterator	iBody = eleBody.getChildren();
//			YFCElement	eleHeader = (YFCElement)iBody.next();
			YFCElement	eleVaxb = (YFCElement)iBody.next();
			sBody = eleVaxb.getString();
			sBody = "<vaxb>" + YFSXMLUtil.getXMLField (sBody, "<"+createVaxb(sServiceName)+">") + "</vaxb>";
			if (YFSUtil.getDebug())
			{
				System.out.println ("SOAP Message Received:");
				System.out.println (eleBody.getString());
				System.out.println ("\r\n");
				System.out.println ("\r\nStripping SOAP Envelope-Resulting Document:");
				System.out.println (sBody);
				System.out.println ("\r\n");
			}
		}
		else
			sBody = "<Empty/>";
		YFCDocument	docNew = YFCDocument.getDocumentFor (sBody);
		return docNew.getDocument();
	}

	public static Document addSOAPEnvelope (Document docOut, String sServiceName) throws Exception
	{
		String	sDoc  = YFSXMLUtil.getXMLString (docOut);
		String	sBody = sDoc.substring (sDoc.indexOf ("?>")+2);
		YFSXMLParser	docNew = null;
		String			vaxb = createVaxb (sServiceName);
						
		if (sBody != null && sBody.length() > 0)
		{
			int	 i1 = sBody.indexOf ("<vaxb")+1;
			sBody = sBody.substring (0, i1) + vaxb + sBody.substring (i1+4);
			int	 i2 = sBody.indexOf ("</vaxb")+2;
			sBody = sBody.substring (0, i2) + vaxb + sBody.substring (i2+4);
			docNew = new YFSXMLParser (strATTSOAPEnvelope+strATTSOAPHeader+sBody+strATTSOAPFooter, false);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Adding SOAP Envelope-Resulting Message");
				System.out.println (YFSXMLUtil.getXMLString (docNew.getDocument()));
			}
		}
		return docNew.getDocument();
	}

	private static String createVaxb(String sServiceName)
	{
		return "vaxb:com.sbc.eia.idl.rm.RmFacadePackage._"+sServiceName+"BISMsg";
	}
	
	private static String	strATTSOAPEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:embus=\"urn:soap.embus.sbc.com\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:vaxb=\"urn:RmFacadePackage.rm.idl.eia.sbc.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ RM.xsd urn:RmFacadePackage.rm.idl.eia.sbc.com Body.xsd urn:soap.embus.sbc.com Header.xsd \"><soap:Body>";
	private static String	strATTSOAPHeader = 
	"<soap:Header>"+"\n"+
     "<embus:MessageHeader>"+"\n"+
      "<embus:MessageTag>embus:MessageTag</embus:MessageTag>"+"\n"+
      "<embus:ApplicationID>embus:ApplicationID</embus:ApplicationID>"+"\n"+
      "<embus:MessageID>embus:MessageID</embus:MessageID>"+"\n"+
      "<embus:CorrelationID>embus:CorrelationID</embus:CorrelationID>"+"\n"+
      "<embus:ConversationKey>embus:ConversationKey</embus:ConversationKey>"+"\n"+
      "<embus:LoggingKey>embus:LoggingKey</embus:LoggingKey>"+"\n"+
      "<embus:ResponseMessageExpiration>0</embus:ResponseMessageExpiration>"+"\n"+
     "</embus:MessageHeader>"+"\n"+
    "</soap:Header>"+"\n";

	private static String	strATTSOAPFooter = "</soap:Body></soap:Envelope>";
	private	static String	strDefaultOrgCode = "DEFAULT";
}

