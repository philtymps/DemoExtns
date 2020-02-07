/**
  * YantraOrderList.java
  *
  **/

// PACKAGE
package com.custom.yantra.orders;

import org.w3c.dom.*;
import java.util.*;
import java.io.Serializable;
import com.custom.yantra.util.*;

import com.yantra.yfc.dom.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;


@SuppressWarnings("serial")
public class YantraOrderList implements Serializable
{
    public YantraOrderList()
    {
		m_vecOrderList = new Vector<YantraOrderListLine> ();
    }
	
	public void addOrder (YantraOrderListLine oOrder)
	{
		m_vecOrderList.addElement (oOrder);
	}
	
	public String getOrderList(String sBuyerOrgCode) throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htOrder = new Hashtable<String, String>();
		htOrder.put("BuyerOrganizationCode", sBuyerOrgCode);
		htOrder.put("BuyerOrganizationCodeQryType" , "EQ");
		return getOrderList (htOrder);
	}
	
	public String getOrderList (Hashtable<String, String> htSearchCriteria) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Order", htSearchCriteria);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getOrderList() API: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		YIFApi api = YFSUtil.getYIFApi();
		return getOrderList (api.getOrderList (env, inXml.getDocument()));	
	}

	protected	String	getOrderList (Document docOrder) throws Exception
	{
		// now parse through the XML output document and load Yantra Order
		Reset ();
		YFCDocument	docOrderList = YFCDocument.getDocumentFor (docOrder);
		YFCElement	eleOrderList = docOrderList.getDocumentElement ();
		m_sOrderList = docOrderList.getString();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getOrderList() API: ");
			System.out.println (m_sOrderList);
		}

		Iterator<?>	iOrderList = eleOrderList.getChildren();		

		// if at least one order line
		while (iOrderList.hasNext())
		{
			// get the first/next order line from output XML
			YFCElement eleOrder = (YFCElement)iOrderList.next();
			
			// create order line saving relevant details
			YantraOrderListLine	yfsOrder = createNewOrderListLine ();
			yfsOrder.setOrderHeaderKey (eleOrder.getAttribute ("OrderHeaderKey"));
			yfsOrder.setOrderNo (eleOrder.getAttribute ("OrderNo"));
			yfsOrder.setStatus (eleOrder.getAttribute ("Status"));			
			yfsOrder.setCustomerPONo (eleOrder.getAttribute ("CustomerPONo"));
			yfsOrder.setCustomerEMailID (eleOrder.getAttribute ("CustomerEMailID"));
			yfsOrder.setCustomerFirstName (eleOrder.getAttribute ("CustomerFirstName"));
			yfsOrder.setCustomerLastName (eleOrder.getAttribute ("CustomerLastName"));
			yfsOrder.setCustomerPhoneNo (eleOrder.getAttribute ("CustomerPhoneNo"));
			yfsOrder.setCustomerZipCode (eleOrder.getAttribute ("CustomerZipCode"));
			yfsOrder.setSearchCriteria1 (eleOrder.getAttribute ("SearchCriteria1"));
			yfsOrder.setSearchCriteria2 (eleOrder.getAttribute ("SearchCriteria2"));
			yfsOrder.setOrderDate(eleOrder.getAttribute ("OrderDate"));
			yfsOrder.setRequestedDate (eleOrder.getAttribute("ReqDeliveryDate"));
			YFCElement	eleOverallTotals = eleOrder.getChildElement ("OverallTotals");
			if (eleOverallTotals != null)
				yfsOrder.setGrandTotal (eleOverallTotals.getAttribute ("GrandTotal"));
			addOrder (yfsOrder);
		}
		return m_sOrderList;
	}	
	
	public	void 	Reset () 
	{
		for (int iLine = 0; iLine < getOrderListCount(); iLine++)
			getOrderListLine(iLine).Reset();	
		m_vecOrderList.clear ();
		m_sOrderList = null;
	}
	
	public	int		getOrderListCount () { return m_vecOrderList.size(); }
	public	YantraOrderListLine	getOrderListLine (int iEle) { return (YantraOrderListLine)m_vecOrderList.elementAt (iEle); }
	public	YantraOrderListLine	createNewOrderListLine () 	{ return new YantraOrderListLine (); }
	
	protected Vector<YantraOrderListLine>	m_vecOrderList;
	protected String	m_sOrderList;
}
