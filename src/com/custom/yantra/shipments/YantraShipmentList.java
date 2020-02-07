/**
  * YantraShipmentList.java
  *
  **/

// PACKAGE
package com.custom.yantra.shipments;

import org.w3c.dom.*;
import java.util.*;
import java.io.Serializable;
import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;

@SuppressWarnings("serial")
public class YantraShipmentList implements Serializable
{
    public YantraShipmentList()
    {
		m_vecShipmentList = new Vector<YantraShipmentListLine> ();
    }
	
	public void addShipment (YantraShipmentListLine oShipment)
	{
		m_vecShipmentList.addElement (oShipment);
	}
	
	public String getShipmentList(String sBuyerOrgCode) throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htShipment = new Hashtable<String, String>();
		htShipment.put("BuyerOrganizationCode", sBuyerOrgCode);
		htShipment.put("BuyerOrganizationCodeQryType" , "EQ");
		return getShipmentList (htShipment);
	}
	
	public String getShipmentList (Hashtable<String, String> htSearchCriteria) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Shipment", htSearchCriteria);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getShipmentList() API: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = api.getShipmentList (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getShipmentList() API: ");
			System.out.println (YFSXMLUtil.getXMLString (docOutXML));
		}
		return loadShipmentLines (docOutXML);	
	}
	
	public String getShipmentListForOrder(String sOrderHeaderKey) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable<String, String> htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", sOrderHeaderKey);

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Order", htOrder);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for getShipmentListForOrder() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		Document docShipmentList = api.getShipmentListForOrder (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from getShipmentListForOrder() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docShipmentList));
		}		
		// now load the order line list
		return loadShipmentLines (docShipmentList);
	}


	protected	String	loadShipmentLines (Document docShipmentList) throws Exception
	{
		// now parse through the XML output document and load Yantra Order
		Reset ();
		m_sShipmentList = YFSXMLUtil.getXMLString (docShipmentList);
		YFCDocument	docShipments = YFCDocument.getDocumentFor (docShipmentList);
		YFCElement	eleShipments = docShipments.getDocumentElement();
		// if at least one order line
		for (Iterator<?> iShipments = eleShipments.getChildren(); iShipments.hasNext();)
		{
			// get the first/next order line from output XML
			YFCElement	eleShipment = (YFCElement)iShipments.next();
			
			// create order line saving relevant details
			YantraShipmentListLine	yfsShipment = createNewShipmentListLine ();
			yfsShipment.setShipmentKey (eleShipment.getAttribute ("ShipmentKey"));
			yfsShipment.setShipmentNo (eleShipment.getAttribute ("ShipmentNo"));
			yfsShipment.setStatus (eleShipment.getAttribute ("Status"));
			yfsShipment.setDocumentType (eleShipment.getAttribute ("DocumentType"));
			yfsShipment.setShipNode (eleShipment.getAttribute ("ShipNode"));
			yfsShipment.setReceiveNode (eleShipment.getAttribute("ReceivingNode"));

			// load expected/actual shipment and delivery dates
			if (eleShipment.getAttribute ("ActualShipmentDate") != null)
				yfsShipment.setShipmentDate(eleShipment.getAttribute ("ActualShipmentDate"));
			else
				yfsShipment.setShipmentDate(eleShipment.getAttribute ("ExpectedShipmentDate"));
			if (eleShipment.getAttribute ("ActualDeliveryDate") != null)
				yfsShipment.setDeliveryDate(eleShipment.getAttribute ("ActualDeliveryDate"));
			else
				yfsShipment.setDeliveryDate(eleShipment.getAttribute ("ExpectedDeliveryDate"));
				
			// if either shipment date or delivery date are null, set to today's date
			if (yfsShipment.getShipmentDate() == null || yfsShipment.getShipmentDate().length() == 0)
				yfsShipment.setShipmentDate (YFCLocaleUtils.makeXMLDateTime ());
			if (yfsShipment.getDeliveryDate() == null || yfsShipment.getShipmentDate().length() == 0)
				yfsShipment.setDeliveryDate (YFCLocaleUtils.makeXMLDateTime ());
								
			YFCElement eleStatus = eleShipment.getChildElement ("Status");
			if (eleStatus != null)
				yfsShipment.setStatusDescription (eleStatus.getAttribute ("Description"));
			addShipment (yfsShipment);
		}
		return m_sShipmentList;
	}	
	
	public	void 	Reset () 
	{
		for (int iLine = 0; iLine < getShipmentListCount(); iLine++)
			getShipmentListLine(iLine).Reset();	
		m_vecShipmentList.clear ();
		m_sShipmentList = null;
	}
	
	public	int		getShipmentListCount () { return m_vecShipmentList.size(); }
	public	YantraShipmentListLine	getShipmentListLine (int iEle) { return (YantraShipmentListLine)m_vecShipmentList.elementAt (iEle); }
	public	YantraShipmentListLine	createNewShipmentListLine () 	{ return new YantraShipmentListLine (); }
	
	protected Vector<YantraShipmentListLine>	m_vecShipmentList;
	protected String	m_sShipmentList;
}

