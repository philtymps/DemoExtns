/**
  * YantraOrder.java
  *
  **/

// PACKAGE
package com.custom.yantra.orders;

import	java.util.*;
import  java.io.Serializable;
import	org.w3c.dom.*;

import com.custom.yantra.util.*;
import com.custom.yantra.customer.*;

import com.yantra.yfc.dom.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.util.*;
import java.math.BigDecimal;


@SuppressWarnings("serial")
public class YantraOrder implements Serializable
{
    public YantraOrder()
    {
		m_vecOrderLines = new Vector<Object> ();
		m_oCustomer = new Customer();
		m_sSellerOrgCode = "";
		m_sBuyerOrgCode = "";
		m_sEnterpriseCode = "DEFAULT";
		m_sOrderHeaderKey = "";
		m_sOrderType = "";
		m_sOrderNo = "";
		m_sOrderDate = "";
		m_sRequestDate = "";
		m_sStatus = "";
		m_sStatusCode = "";
		m_bIsDraft = true;
		m_htModsAllowed = new Hashtable<String, String>();
		m_sDocumentType = "";
		m_sCarrierServiceCode = "";
		m_sSourcingClassification = "";
		m_sSourcingRule="";
    }
	
	public void addOrderLine (Object oLine)
	{
		m_vecOrderLines.addElement (oLine);
	}

	public void addOrderLine (Object oLine, YFCElement eleNewOrderLine)
	{
		addOrderLine (oLine);
	}
	
	public String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public void		setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public String	getEnterpriseCode () { return m_sEnterpriseCode; }
	public void		setEnterpriseCode (String sEnterpriseCode) { m_sEnterpriseCode = sEnterpriseCode; }
	public String	getBuyerOrgCode () { return m_sBuyerOrgCode; }
	public void		setBuyerOrgCode (String sBuyerOrgCode) { m_sBuyerOrgCode = sBuyerOrgCode; }
	public String	getSellerOrgCode () { return m_sSellerOrgCode; }
	public void		setSellerOrgCode (String sSellerOrgCode) { m_sSellerOrgCode = sSellerOrgCode; }
	public String	getOrderNo () { return m_sOrderNo; }
	public void		setOrderNo (String sOrderNo) { m_sOrderNo = sOrderNo; }
	public String	getDocumentType () { return m_sDocumentType; }
	public void		setDocumentType (String sDocumentType) { m_sDocumentType = sDocumentType; }
	public String	getOrderType () { return m_sOrderType; }
	public void		setOrderType (String sOrderType) { m_sOrderType = sOrderType; }
	public String	getOrderName () { return m_sOrderName; }
	public void		setOrderName (String sOrderName) { m_sOrderName = sOrderName; }
	public String	getOrderDate () { return m_sOrderDate; }
	public void		setOrderDate (String sOrderDate) { m_sOrderDate = sOrderDate; }
	public String	getOrderStatus () { return m_sStatus; }
	public void		setOrderStatus (String sStatus) { m_sStatus = sStatus; }
	public String	getOrderStatusCode () { return m_sStatusCode; }
	public void		setOrderStatusCode (String sStatusCode) { m_sStatusCode = sStatusCode; }
	public String	getRequestDate () { return m_sRequestDate; }
	public void		setRequestDate (String sRequestDate) { m_sRequestDate = sRequestDate; }
	public Customer getCustomer () { return m_oCustomer; }
	public void	 	setCustomer (Customer oCustomer) { m_oCustomer = oCustomer; }
	public String	getIsDraft () { return m_bIsDraft ? "Y" : "N"; }
	public void		setIsDraft (String sDraft) { m_bIsDraft = sDraft.equalsIgnoreCase ("Y"); }
	public String	getCarrierServiceCode () { return m_sCarrierServiceCode; }
	public void		setCarrierServiceCode (String sCarrierServiceCode) { m_sCarrierServiceCode = sCarrierServiceCode; }
	public String	getSourcingClassification() { return m_sSourcingClassification; }
	public void		setSourcingClassification(String sSourcingClassification) { m_sSourcingClassification = sSourcingClassification; }
	public String	getSourcingRule() { return m_sSourcingRule; }
	public void		setSourcingRule (String sSourcingRule) { m_sSourcingRule = sSourcingRule; }

	public void	 	setDraft (boolean bIsDraft) { m_bIsDraft = bIsDraft; }
	public boolean	isDraft () { return m_bIsDraft; }


	public	Object	createNewOrderLine () {	return (Object)new YantraOrderLine(this); }

	@SuppressWarnings("deprecation")
	public	String	getOrderDetails () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YFCDocument	docOrderDetails;
		
		// set up search criteria (by Status)		
		Hashtable<String, String> htOrder = new Hashtable<String, String>();
		htOrder.put("OrderHeaderKey", getOrderHeaderKey());

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Order", htOrder);
		YIFApi api = YFSUtil.getYIFApi();
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to getOrderDetails() API: ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOrderDetails = YFCDocument.getDocumentFor (api.getOrderDetails (env, inXml.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output to getOrderDetails() API: ");
			System.out.println( docOrderDetails.getString ());
		}

		YFCElement eleOrderDetails = docOrderDetails.getDocumentElement ();
		
		// load the order header details
		setEnterpriseCode (eleOrderDetails.getAttribute ("EnterpriseCode"));
		setSellerOrgCode (eleOrderDetails.getAttribute ("SellerOrganizationCode"));
		setBuyerOrgCode (eleOrderDetails.getAttribute ("BuyerOrganizationCode"));
		setOrderNo (eleOrderDetails.getAttribute("OrderNo"));
		setOrderType (eleOrderDetails.getAttribute("OrderType"));
		setOrderName (eleOrderDetails.getAttribute("OrderName"));
		setOrderDate (eleOrderDetails.getAttribute("OrderDate"));
		setOrderStatus (eleOrderDetails.getAttribute ("Status"));
		setOrderStatusCode (eleOrderDetails.getAttribute ("MaxOrderStatus"));
		setDocumentType (eleOrderDetails.getAttribute ("DocumentType"));
		setCarrierServiceCode (eleOrderDetails.getAttribute ("CarrierServiceCode"));
		
		if (eleOrderDetails.getAttribute("ReqDeliveryDate") != null)
			setRequestDate (eleOrderDetails.getAttribute("ReqDeliveryDate"));
		else
			setRequestDate (new YFCDate(true).getString (YFCDate.ISO_DATETIME_FORMAT));
			
		Customer oCustomer = getCustomer();
		if (oCustomer != null)
		{
			YFCElement	eleShipTo = eleOrderDetails.getChildElement ("PersonInfoShipTo");
			if (eleShipTo != null)
			{
				oCustomer.setOrganizationCode (eleOrderDetails.getAttribute ("EnterpriseCode"));
				oCustomer.setBuyerOrganizationCode (eleOrderDetails.getAttribute ("BuyerOrganizationCode"));
				oCustomer.setSTLastName (eleShipTo.getAttribute ("LastName"));
				oCustomer.setSTFirstName (eleShipTo.getAttribute ("FirstName"));	
				oCustomer.setSTCompany (eleShipTo.getAttribute ("Company"));
				oCustomer.setSTStreetAddress (eleShipTo.getAttribute ("AddressLine1"));
				oCustomer.setSTAddress2 (eleShipTo.getAttribute ("AddressLine2"));
				oCustomer.setSTCity (eleShipTo.getAttribute ("City"));
				oCustomer.setSTState (eleShipTo.getAttribute ("State"));
				oCustomer.setSTZip (eleShipTo.getAttribute ("ZipCode"));
				oCustomer.setSTCountry (eleShipTo.getAttribute ("Country"));
				oCustomer.setSTPhone (eleShipTo.getAttribute ("DayPhone"));
				oCustomer.setSTEmail (eleShipTo.getAttribute ("EMailID"));
			}
			YFCElement	eleBillTo = eleOrderDetails.getChildElement ("PersonInfoBillTo");
			if (eleBillTo != null)
			{
				oCustomer.setBTLastName (eleBillTo.getAttribute ("LastName"));
				oCustomer.setBTFirstName (eleBillTo.getAttribute ("FirstName"));
				oCustomer.setBTCompany (eleBillTo.getAttribute ("Company"));
				oCustomer.setBTStreetAddress (eleBillTo.getAttribute ("AddressLine1"));
				oCustomer.setBTAddress2 (eleBillTo.getAttribute ("AddressLine2"));
				oCustomer.setBTCity (eleBillTo.getAttribute ("City"));
				oCustomer.setBTState (eleBillTo.getAttribute ("State"));
				oCustomer.setBTZip (eleBillTo.getAttribute ("ZipCode"));
				oCustomer.setBTCountry (eleBillTo.getAttribute ("Country"));
				oCustomer.setBTPhone (eleBillTo.getAttribute ("DayPhone"));
				oCustomer.setBTEmail (eleBillTo.getAttribute ("EMailID"));
			}
		}
		
		// load the order line details
		YFCElement	eleOrderLines = eleOrderDetails.getChildElement ("OrderLines");
		// if at least one order line
		for (Iterator<?> iOrderLines = eleOrderLines.getChildren (); iOrderLines.hasNext(); )
		{
			// get the first/next order line from output XML
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			
			// create order line saving relevant details
			YantraOrderLine	yfsOrderLine = (YantraOrderLine)createNewOrderLine();
			yfsOrderLine.setOrderHeaderKey (eleOrderLine.getAttribute ("OrderHeaderKey"));
			yfsOrderLine.setOrderLineKey (eleOrderLine.getAttribute ("OrderLineKey"));
			yfsOrderLine.setStatus (eleOrderLine.getAttribute ("Status"));			
			yfsOrderLine.setStatusCode (eleOrderLine.getAttribute ("MaxLineStatus"));			
			yfsOrderLine.setQty (eleOrderLine.getAttribute ("OrderedQty"));
			yfsOrderLine.setItemGroupCode (eleOrderLine.getAttribute ("ItemGroupCode"));
			yfsOrderLine.setShipNode (eleOrderLine.getAttribute ("ShipNode"));
			yfsOrderLine.setReceiveNode (eleOrderLine.getAttribute ("ReceivingNode"));
			yfsOrderLine.setDeliveryMethod (eleOrderLine.getAttribute ("DeliveryMethod"));
			yfsOrderLine.setSegmentType (eleOrderLine.getAttribute ("SegmentType"));
			yfsOrderLine.setSegment (eleOrderLine.getAttribute ("Segment"));
			yfsOrderLine.setCarrierServiceCode (eleOrderLine.getAttribute ("CarrierServiceCode"));
				
			YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
			yfsOrderLine.setItemID(eleItem.getAttribute ("ItemID"));
			yfsOrderLine.setUOM (eleItem.getAttribute("UnitOfMeasure"));
			yfsOrderLine.setItemShortDesc (eleItem.getAttribute ("ItemShortDesc"));
			if (eleItem.getAttribute ("IsReturnable") != null)
				yfsOrderLine.setIsReturnable (eleItem.getAttribute ("IsReturnable").equals("Y"));

			// if delivery or service line
			String	sItemGroupCode = eleOrderLine.getAttribute ("ItemGroupCode");
			if (sItemGroupCode.equalsIgnoreCase ("PS") || sItemGroupCode.equalsIgnoreCase ("DS"))
			{
				// add confirmed appointment details to the description
				YFCDate dtApptStartDate = YFCDate.getYFCDate (eleOrderLine.getAttribute ("PromisedApptStartDate"));
				String	sApptStartDate = dtApptStartDate.getString (YFCLocale.getDefaultLocale(), true);				
				String 	sApptStatus = eleOrderLine.getAttribute ("ApptStatus");
				yfsOrderLine.setStatus (yfsOrderLine.getStatus()+"-"+TranslateStatusText(sApptStatus));
				if (sApptStatus.equalsIgnoreCase("CONFIRMED"))
					yfsOrderLine.setStatus (yfsOrderLine.getStatus()+" for "+sApptStartDate);	
			}
			addOrderLine (yfsOrderLine, eleOrderLine);
		}
		// load modifications allowed
		YFCElement	eleAllowedModifications = eleOrderDetails.getChildElement ("AllowedModifications");
		if (eleAllowedModifications != null)
		{
			for (Iterator<?> iModsAllowed = eleAllowedModifications.getChildren (); iModsAllowed.hasNext(); )
			{
				YFCElement	eleModification = (YFCElement)iModsAllowed.next();
				if (YFSUtil.getDebug())
					System.out.println ("Mod Allowed="+eleModification.getAttribute("ModificationType")+"  ThroughOverride="+eleModification.getAttribute("ThroughOverride"));
				m_htModsAllowed.put (eleModification.getAttribute ("ModificationType"), eleModification.getAttribute ("ThroughOverride"));
			}
		}
		return docOrderDetails.getString();
	}
	
	@SuppressWarnings("unused")
	public void	getFulfillmentOptionsForLines () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YFCDocument	docPromise	= YFCDocument.createDocument ("Promise");
		YFCElement	elePromise = docPromise.getDocumentElement ();
		YFCElement	elePromiseLines = elePromise.createChild ("PromiseLines");
		
		elePromise.setAttribute ("OrganizationCode", getEnterpriseCode());
		elePromise.setAttribute ("IgnoreUnpromised", "N");
		elePromise.setAttribute ("CustomerID", getCustomer().getCustomerID());
		elePromise.setAttribute ("CarrierServiceCode", getCarrierServiceCode());
		elePromise.setAttribute ("SourcingClassification", getSourcingClassification());
		elePromise.setAttribute ("AllocationRuleID", getSourcingRule());
		
		for (int i = 0; i < getOrderLineCount(); i++)
		{
			YFCElement	elePromiseLine = elePromiseLines.createChild ("PromiseLine");
			YantraOrderLine	oOrderLine = getOrderLine (i);
			
			if (YFCObject.isVoid (oOrderLine.getOrderLineKey()))
			{
				elePromiseLine.setAttribute ("LineId", oOrderLine.getTransactionalLineId());
				elePromiseLine.setAttribute ("ItemID", oOrderLine.getItemID ());
				elePromiseLine.setAttribute ("UnitOfMeasure", oOrderLine.getUOM());
				elePromiseLine.setAttribute ("ProductClass", oOrderLine.getProductClass ());
				elePromiseLine.setAttribute ("RequiredQty", oOrderLine.getQty ());
				elePromiseLine.setAttribute ("SegmentType", oOrderLine.getSegmentType());
				elePromiseLine.setAttribute ("Segment", oOrderLine.getSegment ());
				elePromiseLine.setAttribute ("CarrierServiceCode", oOrderLine.getCarrierServiceCode());
				elePromiseLine.setAttribute ("FulfillmentType", oOrderLine.getFulfillmentType());
				YFCElement	eleShipToAddress = elePromiseLine.createChild ("ShipToAddress");
				
				if (YFCObject.isVoid (oOrderLine.getSTZip()))
				{
					Customer	oCustomer = getCustomer();
					eleShipToAddress.setAttribute ("AddressLine1", oCustomer.getSTStreetAddress ());
					eleShipToAddress.setAttribute ("AddressLine2", oCustomer.getSTAddress2 ());
					eleShipToAddress.setAttribute ("City", oCustomer.getSTCity ());
					eleShipToAddress.setAttribute ("State", oCustomer.getSTState ());
					eleShipToAddress.setAttribute ("ZipCode", oCustomer.getSTZip ());
					eleShipToAddress.setAttribute ("Country", oCustomer.getSTCountry());
				}
				else
				{
					eleShipToAddress.setAttribute ("City", oOrderLine.getSTCity ());
					eleShipToAddress.setAttribute ("State", oOrderLine.getSTState ());
					eleShipToAddress.setAttribute ("ZipCode", oOrderLine.getSTZip ());
					eleShipToAddress.setAttribute ("Country", oOrderLine.getSTCountry ());
				}
				if (!YFCObject.isVoid (oOrderLine.getShipNode()))
				{
					YFCElement	eleShipNodes = elePromiseLine.createChild ("ShipNodes");
					YFCElement	eleShipNode = eleShipNodes.createChild ("ShipNode");
					eleShipNode.setAttribute ("Node", oOrderLine.getShipNode ());
				}
			}
			else
			{
				YFCElement	eleOrderLine = elePromiseLine.createChild ("OrderLine");
				eleOrderLine.setAttribute ("OrderLineKey", oOrderLine.getOrderLineKey());			
			}
		}

		// if this is an existing order in the system use order values
		if (!YFCObject.isVoid (getOrderNo()) || !YFCObject.isVoid (getOrderHeaderKey()))
		{
			YFCElement	eleEvaluateOrder = elePromise.createChild ("EvaluateOrder");
			if (YFCObject.isVoid(getOrderHeaderKey()))
			{
				eleEvaluateOrder.setAttribute ("OrderNo", getOrderNo());
				eleEvaluateOrder.setAttribute ("DocumentType", getDocumentType());
				eleEvaluateOrder.setAttribute ("EnterpriseCode", getEnterpriseCode());
			}
			else
			{
				eleEvaluateOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
			}
		}
		else
		{
			YFCElement	eleEvaluateOptions = elePromise.createChild ("EvaluateOptions");
			YFCElement	eleEvaluateOption = addEvaluationOption (eleEvaluateOptions, "SHP");
/*
			Customer	oCustomer = getCustomer();
			YFCElement	eleShipToAddress = eleEvaluateOption.createChild ("ShipToAddress");
			eleShipToAddress.setAttribute ("AddressLine1", oCustomer.getSTStreetAddress ());
			eleShipToAddress.setAttribute ("AddressLine2", oCustomer.getSTAddress2 ());
			eleShipToAddress.setAttribute ("City", oCustomer.getSTCity ());
			eleShipToAddress.setAttribute ("State", oCustomer.getSTState ());
			eleShipToAddress.setAttribute ("ZipCode", oCustomer.getSTZip ());
			eleShipToAddress.setAttribute ("Country", oCustomer.getSTCountry());
*/
		}
		
		YIFApi api = YFSUtil.getYIFApi();
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to getFulfillmentOptionsForLines() API: ");
			System.out.println( docPromise.getString());
		}
		docPromise = YFCDocument.getDocumentFor (api.getFulfillmentOptionsForLines(env, docPromise.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from getFulfillmentOptionsForLines() API: ");
			System.out.println( docPromise.getString ());
		}
		// parse the output
		elePromise = docPromise.getDocumentElement ();
		elePromiseLines = elePromise.getChildElement ("PromiseLines");
		YFCElement eleEvaluateOptions = elePromise.getChildElement ("EvaluateOptions");
		
		if (elePromiseLines != null && eleEvaluateOptions != null)
		{
			Iterator<?>	iPromiseLines = elePromiseLines.getChildren();
			YFCElement	eleEvaluateOption = getEvaluationOption(eleEvaluateOptions, "SHP");
			
			if (eleEvaluateOption != null)
			{
				int	iOption = eleEvaluateOption.getIntAttribute ("OptionNo");
				while (iPromiseLines.hasNext())
				{
					YFCElement	elePromiseLine = (YFCElement)iPromiseLines.next();
					getFulfillmentOptionsForOrderLine (elePromiseLine, iOption);
				}
			}
		}
		return ;
	}
	
	public boolean	IsModificationAllowed (String sModType)
	{
		boolean bAllowed = false;
		String sModAllowed = (String)m_htModsAllowed.get (sModType);
		if (sModAllowed != null && sModAllowed.length() > 0)
			bAllowed = true;
		if (YFSUtil.getDebug())
			System.out.println ("Mod Requested="+sModType+"  Allowed="+bAllowed);
		return bAllowed;
	}
	
	public	YantraOrderLine getOrderLine (int iLine)
	{
		return (YantraOrderLine)m_vecOrderLines.elementAt (iLine);
	}

	public	Vector<Object>	getOrderLines () { return m_vecOrderLines; }
	public	int		getOrderLineCount () { return m_vecOrderLines.size(); }
	
	public	void Reset () { Reset (null); }
	
	public	void Reset (String sStatus)
	{
		if (sStatus != null && getOrderLineCount() > 0)
		{
			for (int iEle = 0; iEle < getOrderLineCount(); )
			{
				if (getOrderLine (iEle).getStatus().equals (sStatus))
				{
					getOrderLine(iEle).Reset();
					m_vecOrderLines.remove (iEle);
				}
				else
					iEle++;
			}
		}
		else
		{
			for (int iEle = 0; iEle < getOrderLineCount(); iEle++)		
				getOrderLine(iEle).Reset();
			m_vecOrderLines.clear();
		}
		m_oCustomer = null;
		m_oCustomer = new Customer();
		m_htModsAllowed = null;
		m_htModsAllowed = new Hashtable<String, String>();
	}

	public String getOrderLineListForOrder(String sStatus) throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htOrderLines = new Hashtable<String, String>();
		htOrderLines.put("OrderHeaderKey", getOrderHeaderKey());
		htOrderLines.put("Status", sStatus);
		htOrderLines.put("StatusQryType" , "EQ");

		return getOrderLineList (htOrderLines);
	}
			
	public String getOrderLineListForOrder() throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htOrderLines = new Hashtable<String, String>();
		htOrderLines.put("OrderHeaderKey", getOrderHeaderKey());
		return getOrderLineList (htOrderLines);
	}

	public String getOrderLineList(String sStatus) throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htOrderLines = new Hashtable<String, String>();
		htOrderLines.put("Status", sStatus);
		htOrderLines.put("StatusQryType" , "EQ");
		return getOrderLineList (htOrderLines);
	}

	public String getOrderLineList(Hashtable<String, String> htOrderLines) throws Exception
	{
		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("OrderLine", htOrderLines);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getOrderLineList () API:");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		Document docOrderLineList = api.getOrderLineList (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getOrderLineList () API:");
			System.out.println (YFSXMLUtil.getXMLString (docOrderLineList));
		}			
		
		// now load the order line list
		return getOrderLineList (docOrderLineList);	
	}
		
	protected	String getOrderLineList (Document docOrderLineList) throws Exception
	{				
		// now parse through the XML output document and load Yantra Order Lines
		String	sOrderLineList = YFSXMLUtil.getXMLString (docOrderLineList);
		YFCDocument	docOrderLines = YFCDocument.getDocumentFor (docOrderLineList);
		YFCElement	eleOrderLineList = docOrderLines.getDocumentElement ();

		for (Iterator<?> iOrderLines = eleOrderLineList.getChildren(); iOrderLines.hasNext(); )
		{
			// get the first/next order line from output XML
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();			
			
			// create order line saving relevant details
			YantraOrderLine	yfsOrderLine = (YantraOrderLine)createNewOrderLine();
			yfsOrderLine.setOrderHeaderKey (eleOrderLine.getAttribute ("OrderHeaderKey"));
			yfsOrderLine.setOrderLineKey (eleOrderLine.getAttribute ("OrderLineKey"));
			yfsOrderLine.setStatus (eleOrderLine.getAttribute ("Status"));			
			yfsOrderLine.setStatusCode (eleOrderLine.getAttribute ("MaxLineStatus"));			
			yfsOrderLine.setQty (eleOrderLine.getAttribute ("OrderedQty"));
			yfsOrderLine.setSegmentType (eleOrderLine.getAttribute ("SegmentType"));
			yfsOrderLine.setSegment (eleOrderLine.getAttribute ("Segment"));
			
			YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
			if (eleItem != null)
			{
				yfsOrderLine.setItemID(eleItem.getAttribute ("ItemID"));
				yfsOrderLine.setUOM (eleItem.getAttribute("UnitOfMeasure"));
				yfsOrderLine.setItemShortDesc (eleItem.getAttribute ("ItemShortDesc"));
			}
			addOrderLine (yfsOrderLine, eleOrderLine);
		}
		return sOrderLineList;
	}

	public String	cancelOrder () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = null;
		YFSXMLParser inXml = new YFSXMLParser();

		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey());
		htOrder.put ("Action", "CANCEL");

		// generate XML for Order element			
		inXml.createRootElement ("Order", htOrder);
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to changeOrder() API: ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.changeOrder (env, inXml.getDocument());	
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeOrder() API:");
			System.out.println( YFSXMLUtil.getXMLString(docOutXML));
		}
		return YFSXMLUtil.getXMLString (docOutXML);						
	}
	
	public String	cancelOrderLine(int iLine) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = null;
		YFSXMLParser inXml = new YFSXMLParser();

		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey());

		// generate XML for Order element			
		Element eleOrder = inXml.createRootElement ("Order", htOrder);

		// generate XML for OrderLines
		Element eleOrderLines = inXml.createChild (eleOrder, "OrderLines", null);

		// create OrderLine htributes
		Hashtable<String, String> htOrderLine = new Hashtable<String, String>();
		htOrderLine.put("OrderLineKey", getOrderLine(iLine).getOrderLineKey());
		htOrderLine.put("Action", "CANCEL");
		inXml.createChild(eleOrderLines, "OrderLine", htOrderLine);
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to changeOrder() API: ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.changeOrder (env, inXml.getDocument());	
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeOrder() API:");
			System.out.println( YFSXMLUtil.getXMLString(docOutXML));
		}
		return YFSXMLUtil.getXMLString (docOutXML);						
	}
	
	
	public String	createOrder () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFCDocument	docOrder = YFCDocument.createDocument ("Order");
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		
		eleOrder.setAttribute ("EnterpriseCode", getEnterpriseCode());
		eleOrder.setAttribute ("SellerOrganizationCode", getSellerOrgCode());
		eleOrder.setAttribute ("BuyerOrganizationCode", getBuyerOrgCode ());
		eleOrder.setAttribute ("DraftOrderFlag", isDraft () ? "Y" : "N");
		
		// generate XML for OrderLines
		YFCElement eleOrderLines = eleOrder.createChild ("OrderLines");

		// iterate over order lines
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			// create OrderLine htributes
			YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
			
			eleOrderLine.setAttribute ("OrderedQty", getOrderLine(iEle).getQty());
			YFCElement	eleItem = eleOrderLine.createChild ("Item");
			
			eleItem.setAttribute ("ItemID", getOrderLine(iEle).getItemID ());
			eleItem.setAttribute ("UnitOfMeasure", getOrderLine (iEle).getUOM());
			eleItem.setAttribute ("ProductClass", getOrderLine (iEle).getProductClass());
		}

		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to createOrder() API: ");
			System.out.println(docOrder.getString());
		}
		docOrder = YFCDocument.getDocumentFor (api.createOrder (env, docOrder.getDocument()));	
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from createOrder() API:");
			System.out.println( docOrder.getString());
		}
		return docOrder.getString();
	}
	
	public	String	changeOrder () throws Exception
	{
		return changeOrder ((Hashtable<?, ?>)null);
	}

	// Deprecated Method.  Please use new method changeOrderDoc (YFCDocument)
	/** @deprecated <b>Deprecated.</b> <i>use changeOrder(Document) instead</i> */
	public String	changeOrder (Hashtable<?, ?> htOrder) throws Exception
	{
		return changeOrder (htOrder, false, false);
	}
	
	// Deprecated Method.  Please use new method changeOrderAddress (YFCDocument, boolean bShipTo, bBillTo)
	/** @deprecated <b>Deprecated.</b> <i>use changeOrderAddress (Document docOrder, boolean, boolean) instead</i> */
	public	String	changeOrderAddress (Hashtable<?, ?> htOrder, boolean bShipTo, boolean bBillTo) throws Exception
	{
		return changeOrder (htOrder, bShipTo, bBillTo);
	}

	// Deprecated Method.  Please use new method changeOrder (YFCDocument, boolean bShipTo, bBillTo)
	/** @deprecated <b>Deprecated.</b> <i>use changeOrder(Document docOrder, boolean, boolean) instead</i> */
	public	String changeOrder (Hashtable<?, ?> htOrder, boolean bShipTo, boolean bBillTo) throws Exception
	{
		YFCDocument	docOrder = YFCDocument.createDocument ("Order");
		YFCElement	eleOrder = docOrder.getDocumentElement();
		
		if (htOrder != null)
		{
			Enumeration<?> enumKeys = htOrder.keys();
			while (enumKeys.hasMoreElements ())
			{
				String	sKey = (String)enumKeys.nextElement ();
				eleOrder.setAttribute (sKey, (String)htOrder.get(sKey));
			}
		}
		else
		{		
			eleOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		}
		return changeOrder (docOrder.getDocument(), bShipTo, bBillTo);
	}

	// new method for change order
	public	String	changeOrder (Document docOrder, boolean bShipTo, boolean bBillTo) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();

		docOrder = changeOrderDoc (docOrder, bShipTo, bBillTo);
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to changeOrder() API: ");
			System.out.println( YFSXMLUtil.getXMLString(docOrder));
		}
		docOrder = api.changeOrder (env, docOrder);
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeOrder() API:");
			System.out.println( YFSXMLUtil.getXMLString(docOrder));
		}
		return YFSXMLUtil.getXMLString (docOrder);		
	}
	
	public	Document	changeOrderDoc (Document domOrder, boolean bShipTo, boolean bBillTo) throws Exception
	{
		YFCDocument	docOrder;
		YFCElement	eleOrder;

		// set these attributes so as to make changes that are allowed or allowed with override
		if (domOrder == null)
		{
			docOrder = YFCDocument.createDocument ("Order");
			eleOrder = docOrder.getDocumentElement ();
			eleOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		}
		else
		{
			docOrder = YFCDocument.getDocumentFor(domOrder);
			eleOrder = docOrder.getDocumentElement ();
		}
		// set the action and override attributes
		eleOrder.setAttribute ("Action", "MODIFY");
		eleOrder.setAttribute ("Override", "Y");
		
		// if customer information provided 
		if (getCustomer() != null)
		{
			Customer	oCustomer = getCustomer();
			
			// finish filling out address fields
			if (bBillTo)
			{
				YFCElement	eleBillToPersonInfo = eleOrder.createChild ("PersonInfoBillTo");
				eleBillToPersonInfo.setAttribute ("FirstName", oCustomer.getBTFirstName());
				eleBillToPersonInfo.setAttribute ("LastName", oCustomer.getBTLastName());
				eleBillToPersonInfo.setAttribute ("Company", oCustomer.getBTCompany());
				eleBillToPersonInfo.setAttribute ("AddressLine1", oCustomer.getBTStreetAddress());
				eleBillToPersonInfo.setAttribute ("AddressLine2", oCustomer.getBTAddress2());
				eleBillToPersonInfo.setAttribute ("City", oCustomer.getBTCity());
				eleBillToPersonInfo.setAttribute ("State", oCustomer.getBTState());
				eleBillToPersonInfo.setAttribute ("ZipCode", oCustomer.getBTZip());
				eleBillToPersonInfo.setAttribute ("Country", oCustomer.getBTCountry());
				eleBillToPersonInfo.setAttribute ("EMailID", oCustomer.getBTEmail());
				eleBillToPersonInfo.setAttribute ("DayPhone", oCustomer.getBTPhone());
			}
			if (bShipTo)
			{
				// finish filling out address fields
				YFCElement	eleShipToPersonInfo = eleOrder.createChild ("PersonInfoShipTo");
				eleShipToPersonInfo.setAttribute("FirstName", oCustomer.getSTFirstName());
				eleShipToPersonInfo.setAttribute("LastName", oCustomer.getSTLastName());
				eleShipToPersonInfo.setAttribute("Company", oCustomer.getSTCompany());
				eleShipToPersonInfo.setAttribute("AddressLine1", oCustomer.getSTStreetAddress());
				eleShipToPersonInfo.setAttribute("AddressLine2", oCustomer.getSTAddress2());
				eleShipToPersonInfo.setAttribute("City", oCustomer.getSTCity());
				eleShipToPersonInfo.setAttribute("State", oCustomer.getSTState());
				eleShipToPersonInfo.setAttribute("ZipCode", oCustomer.getSTZip());
				eleShipToPersonInfo.setAttribute("Country", oCustomer.getSTCountry());
				eleShipToPersonInfo.setAttribute("EMailID", oCustomer.getSTEmail());
				eleShipToPersonInfo.setAttribute("DayPhone", oCustomer.getSTPhone());
			}
		}
		return (docOrder.getDocument());			
	}

	public String returnOrder (String sReasonCode, String  sLineType) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		
		// generate XML input for createOrder API
		YFCDocument docRetOrder = YFCDocument.createDocument ("Order");
		YFCElement	eleRetOrder = docRetOrder.getDocumentElement();

		eleRetOrder.setAttribute ("EnterpriseCode", getEnterpriseCode());
		eleRetOrder.setAttribute ("SellerOrganizationCode", getSellerOrgCode());
		eleRetOrder.setAttribute ("BuyerOrganizationCode", getBuyerOrgCode ());
		eleRetOrder.setAttribute ("DraftOrderFlag", "N");
		eleRetOrder.setAttribute ("DocumentType", "0003");
		eleRetOrder.setAttribute ("ApplyDefaultTemplate", "Y");
		
		// generate XML for OrderLines
		YFCElement eleRetOrderLines = eleRetOrder.createChild ("OrderLines");
		
		// get the returnable order lines
		getReturnableOrderLines ();
						
		// iterate over order lines and put all returnable lines into the return order
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)		
		{
			YantraOrderLine	oOrderLine = getOrderLine(iEle);
			
			if (oOrderLine.getIsReturnable ())
			{
				// create OrderLines
				YFCElement	eleOrderLine = eleRetOrderLines.createChild("OrderLine");
				YFCElement	eleDerivedFrom = eleOrderLine.createChild ("DerivedFrom");
				YFCElement	eleOrderLineTranQuantity = eleOrderLine.createChild ("OrderLineTranQuantity");
				YFCElement	eleItem = eleOrderLine.createChild ("Item");

				eleItem.setAttribute ("ItemID", oOrderLine.getItemID());
				eleItem.setAttribute ("ProductClass", oOrderLine.getProductClass());

				eleOrderLineTranQuantity.setAttribute ("TransactionalUOM", oOrderLine.getUOM());
				eleOrderLineTranQuantity.setAttribute ("OrderedQty", oOrderLine.getQty());
				eleOrderLine.setAttribute ("LineType", sLineType);
				eleOrderLine.setAttribute ("ReturnReason", sReasonCode);
				eleOrderLine.setAttribute ("ShipNode", oOrderLine.getShipNode());
				eleDerivedFrom.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
				eleDerivedFrom.setAttribute ("OrderLineKey", oOrderLine.getOrderLineKey());
				eleDerivedFrom.setAttribute ("OrderReleaseKey", oOrderLine.getOrderReleaseKey());	
				eleDerivedFrom.setAttribute ("SegmentType", oOrderLine.getSegmentType());			
				eleDerivedFrom.setAttribute ("Segment", oOrderLine.getSegment());			
			}						
		}

		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for createOrder() API is ...");
			System.out.println(docRetOrder.getString());
		}
		docRetOrder = YFCDocument.getDocumentFor (api.createOrder (env, docRetOrder.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from createOrder() API is ...");
			System.out.println(docRetOrder.getString());
		}

		return docRetOrder.getString();
	}

	protected	void getReturnableOrderLines () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();

		// get the returnable order lines
		YFCDocument docOrderStatusList = YFCDocument.createDocument("OrderLineStatus");
		YFCElement	eleOrderStatusList = docOrderStatusList.getDocumentElement();
		eleOrderStatusList.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		eleOrderStatusList.setAttribute ("DocumentType", getDocumentType());
		eleOrderStatusList.setAttribute ("TransactionId", "INCLUDE_IN_RETURN");
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for getOrderStatusList():");
			System.out.println (docOrderStatusList.getString());
		}
		docOrderStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderStatusList.getDocument()));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getOrderStatusList():");
			System.out.println (docOrderStatusList.getString());
		}
		
		eleOrderStatusList = docOrderStatusList.getDocumentElement();

		// iterate over the original order lines from parent order
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			YantraOrderLine	oOrderLine = getOrderLine (iEle);
			oOrderLine.setIsReturnable (false);
			
			// iterate over the status line list
			for (Iterator<?> iOrderLineStatus = eleOrderStatusList.getChildren (); iOrderLineStatus.hasNext(); )
			{		
				YFCElement	eleOrderStatus = (YFCElement)iOrderLineStatus.next();			
				// if line in a pickup status for a return
				if (oOrderLine.getOrderLineKey().equals (eleOrderStatus.getAttribute ("OrderLineKey")))
				{
					// Note TO DO: add logic to determine if item returnable etc and within return window
					oOrderLine.setIsReturnable (true);
					oOrderLine.setOrderReleaseKey (eleOrderStatus.getAttribute ("OrderReleaseKey"));
					if (eleOrderStatus.getChildElement ("Schedule") != null)
						oOrderLine.setShipNode (eleOrderStatus.getChildElement ("Schedule").getAttribute ("ShipNode"));
				}
			}				
		}
		return; // all returnable lines now have IsReturnable set to 'true'
	}

	protected	void getShipableOrderLines (boolean bForPickup) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();

		// get the returnable order lines
		YFCDocument docOrderStatusList = YFCDocument.createDocument("OrderLineStatus");
		YFCElement	eleOrderStatusList = docOrderStatusList.getDocumentElement();
		eleOrderStatusList.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		eleOrderStatusList.setAttribute ("DocumentType", getDocumentType());
		eleOrderStatusList.setAttribute ("TransactionId", "INCLUDE_SHIPMENT");
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for getOrderStatusList():");
			System.out.println (docOrderStatusList.getString());
		}
		docOrderStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderStatusList.getDocument()));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getOrderStatusList():");
			System.out.println (docOrderStatusList.getString());
		}
		
		eleOrderStatusList = docOrderStatusList.getDocumentElement();

		// iterate over the original order lines from parent order
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			YantraOrderLine	oOrderLine = getOrderLine (iEle);
			oOrderLine.setIsShipable (false);
			
			// iterate over the status line list
			for (Iterator<?> iOrderLineStatus = eleOrderStatusList.getChildren (); iOrderLineStatus.hasNext(); )
			{		
				YFCElement	eleOrderStatus = (YFCElement)iOrderLineStatus.next();			
				if (eleOrderStatus.getNodeName().equals("OrderStatus"))
				{
				
					// if line in a pickup status for a return
					boolean bShipable = (bForPickup && oOrderLine.getDeliveryMethod().equals ("PICK") && oOrderLine.getOrderLineKey().equals (eleOrderStatus.getAttribute ("OrderLineKey")) && !YFCObject.isVoid(eleOrderStatus.getAttribute ("OrderReleaseKey")))
						  		     || (!bForPickup && oOrderLine.getOrderLineKey().equals (eleOrderStatus.getAttribute ("OrderLineKey")) && !YFCObject.isVoid(eleOrderStatus.getAttribute ("OrderReleaseKey")));
						  
					if (bShipable)
					{
						// Note TO DO: add logic to determine if item returnable etc and within return window
						oOrderLine.setIsShipable (true);
						if (YFSUtil.getDebug ())
						{
							System.out.print ("OrderLineKey" + oOrderLine.getOrderLineKey () + " Item " + oOrderLine.getItemID()+ "  is Shippable");
						}
						oOrderLine.setOrderReleaseKey (eleOrderStatus.getAttribute ("OrderReleaseKey"));
					}
				}
			}				
		}
		return; // all returnable lines now have IsReturnable set to 'true'
	}

	public String receiveOrder (String sReceiveNode) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFCDocument		docReceipt = YFCDocument.createDocument("Receipt");
		YFCElement		eleReceipt = docReceipt.getDocumentElement ();
		
		eleReceipt.setAttribute ("DocumentType", getDocumentType());
		eleReceipt.setAttribute ("ReceivingNode", sReceiveNode);
		eleReceipt.setAttribute ("NumOfCartons", "1");
		eleReceipt.setAttribute ("NumOfPallets", "0");

		YFCElement	eleShipment = eleReceipt.createChild ("Shipment");		
		eleShipment.setAttribute ("OrderHeaderKey", getOrderHeaderKey());

		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for startReceipt() API is ...");
			System.out.println(docReceipt.getString());
		}
		Document docOutXML = api.startReceipt (env, docReceipt.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from startReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}
		docReceipt = YFCDocument.getDocumentFor (docOutXML);
		String	sReceiptHeaderKey = docReceipt.getDocumentElement().getAttribute ("ReceiptHeaderKey");
		docReceipt = YFCDocument.createDocument ("Receipt");
		eleReceipt = docReceipt.getDocumentElement ();
		eleReceipt.setAttribute ("DocumentType", getDocumentType());
		eleReceipt.setAttribute ("ReceiptHeaderKey", sReceiptHeaderKey);
		eleReceipt.setAttribute ("ReceivingNode", sReceiveNode);

		// now iterate over order lines and add to receipt lines in same quantity
		YFCElement eleReceiptLines = eleReceipt.createChild ("ReceiptLines");

		for (int iOrderLine = 0; iOrderLine < getOrderLineCount(); iOrderLine++)
		{
			YantraOrderLine oOrderLine = getOrderLine (iOrderLine);
			YFCElement	eleReceiptLine = eleReceiptLines.createChild ("ReceiptLine");

			eleReceiptLine.setAttribute("OrderLineKey", oOrderLine.getOrderLineKey());
			eleReceiptLine.setAttribute("Quantity", oOrderLine.getQty());
			eleReceiptLine.setAttribute("DispositionCode", "RESTOCKED");
			
			// add serial & lot numbers
			if (oOrderLine.getLotNumber().length () > 0)
			{
				eleReceiptLine.setAttribute ("LotNumber", oOrderLine.getLotNumber());
			}
			if (oOrderLine.getSerialNo ().length() > 0)
			{
				eleReceiptLine.setAttribute ("SerialNo", oOrderLine.getSerialNo());				
			}
		}		
		
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println("Input for receiveOrder() API is ...");
			System.out.println(docReceipt.getString());
		}
		// receive the shipment
		docOutXML = api.receiveOrder (env, docReceipt.getDocument());
		docReceipt = YFCDocument.getDocumentFor (docOutXML);
		if (YFSUtil.getDebug())
		{
			System.out.println("Output from receiveOrder() API is ...");
			System.out.println(docReceipt.getString());
		}		
		docReceipt = YFCDocument.createDocument ("Receipt");
		eleReceipt = docReceipt.getDocumentElement ();
		eleReceipt.setAttribute ("DocumentType", getDocumentType());
		eleReceipt.setAttribute ("ReceiptHeaderKey", sReceiptHeaderKey);
		eleReceipt.setAttribute ("ReceivingNode", sReceiveNode);
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println("Input for closeReceipt() API is ...");
			System.out.println(docReceipt.getString());
		}
		Document docCloseReceipt = api.closeReceipt (env, docReceipt.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from closeReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docCloseReceipt));
		}			
		return docReceipt.getString();
	}

	public String updateOrderLineStatus (String sDropStatus, String sTransaction) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
				
		// create OrderStatusChange htributes			
		eleOrderStatusChange.setAttribute ("DocumentType", getDocumentType());
		eleOrderStatusChange.setAttribute ("TransactionId",sTransaction);
		eleOrderStatusChange.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		eleOrderStatusChange.setAttribute ("ChangeForAllAvailableQty", "Y");
		eleOrderStatusChange.setAttribute ("BaseDropStatus", sDropStatus);			
						
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input for changeOrderStatus API is ... ");
			System.out.println( docOrderStatusChange.getString());
		}
		docOrderStatusChange = YFCDocument.getDocumentFor (api.changeOrderStatus (env, docOrderStatusChange.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeOrderStatus API is ... ");
			System.out.println( docOrderStatusChange.getString());
		}			
		
		return docOrderStatusChange.getString();
	}
	
	public String scheduleOrder () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFCDocument	docScheduleOrder = YFCDocument.createDocument ("ScheduleOrder");
		YFCElement	eleScheduleOrder = docScheduleOrder.getDocumentElement ();
		eleScheduleOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		eleScheduleOrder.setAttribute ("IgnoreMinNotificationTime", "Y");
		eleScheduleOrder.setAttribute ("IgnoreReleaseDate", "Y");
		eleScheduleOrder.setAttribute ("AllocationRuleID", getSourcingRule());
		
		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for scheduleOrder() API is ...");
			System.out.println( docScheduleOrder.getString());
		}
		docScheduleOrder = YFCDocument.getDocumentFor (api.scheduleOrder (env, docScheduleOrder.getDocument()));		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from scheduleOrder() API is ...");
			System.out.println( docScheduleOrder.getString());
		}
		return docScheduleOrder.getString ();
	}

	@SuppressWarnings("deprecation")
	public String scheduleOrderLines (String sPickupStatus) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFCDocument		docPromise = YFCDocument.createDocument ("Promise");
		YFCElement		elePromise = docPromise.getDocumentElement ();	
		// generate XML for OrderStatusChange element			
		elePromise.setAttribute ("CheckInventory", "Y");
		elePromise.setAttribute ("DocumentType", "0001");
		elePromise.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		elePromise.setAttribute ("IgnoreReleaseDate", "Y");
		elePromise.setAttribute ("AllocationRuleID", getSourcingRule());
		
		// generate XML for OrderLines
		YFCElement elePromiseLines = elePromise.createChild ("PromiseLines");

		// iterate over order lines
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			// create OrderLine htributes
			if (sPickupStatus == null || getOrderLine(iEle).getStatusCode().startsWith (sPickupStatus))
			{
				YFCElement	elePromiseLine = elePromiseLines.createChild("PromiseLine");
				elePromiseLine.setAttribute ("OrderLineKey", getOrderLine(iEle).getOrderLineKey());
				elePromiseLine.setAttribute ("Quantity", getOrderLine(iEle).getQty());
				elePromiseLine.setAttribute ("ShipNode", getOrderLine(iEle).getShipNode());
				elePromiseLine.setAttribute ("DeliveryDate", new YFCDate(false).getString (YFCDate.XML_DATE_FORMAT));						
			}
		}
		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for scheduleOrderLines() API is ...");
			System.out.println( docPromise.getString());
		}
		docPromise = YFCDocument.getDocumentFor (api.scheduleOrderLines (env, docPromise.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from scheduleOrderLines() API is ...");
			System.out.println( docPromise.getString());
		}
		return docPromise.getString();
	}

	public String releaseOrder () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFCDocument	docReleaseOrder = YFCDocument.createDocument ("ReleaseOrder");
		YFCElement	eleReleaseOrder = docReleaseOrder.getDocumentElement ();

		eleReleaseOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey());
		eleReleaseOrder.setAttribute ("IgnoreReleaseDate", "Y");
		eleReleaseOrder.setAttribute ("AllocationRuleID", getSourcingRule());

		// generate XML for OrderStatusChange element			
		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for releaseOrder() API is ...");
			System.out.println( docReleaseOrder.getString());
		}
		docReleaseOrder = YFCDocument.getDocumentFor (api.releaseOrder (env, docReleaseOrder.getDocument()));		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from releaseOrder() API is ...");
			System.out.println( docReleaseOrder.getString());
		}
		return docReleaseOrder.getString();
	}
		
	public String createShipmentsForOrder (boolean bForPickup) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		
		YFCDocument docShipment = YFCDocument.createDocument ("Shipment");
		YFCElement	eleShipment = docShipment.getDocumentElement();
		eleShipment.setAttribute ("EnterpriseCode", getEnterpriseCode());
		eleShipment.setAttribute ("SellerOrganizationCode", getSellerOrgCode());
		eleShipment.setAttribute ("Action", "Create-Modify");
		
		// get the shipable lines and the associated order release key
		getShipableOrderLines (bForPickup);
		
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			YantraOrderLine	oOrderLine = getOrderLine (iEle);

			if (oOrderLine.getIsShipable ())
			{			
				// create a shipment line element			
				YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
				if (eleShipmentLines == null)
					eleShipmentLines = eleShipment.createChild ("ShipmentLines");

				// add the shipment line child element to the shipment lines element
				YFCElement	eleShipmentLine = eleShipmentLines.createChild ("ShipmentLine");
				eleShipmentLine.setAttribute ("Action", "Create");
				eleShipmentLine.setAttribute ("OrderLineKey", oOrderLine.getOrderLineKey());
				eleShipmentLine.setAttribute ("OrderHeaderKey", oOrderLine.getOrderHeaderKey());
				eleShipmentLine.setAttribute ("OrderReleaseKey", oOrderLine.getOrderReleaseKey ());
				eleShipmentLine.setAttribute ("Quantity", oOrderLine.getQty());
				eleShipmentLine.setAttribute ("UnitOfMeasure", oOrderLine.getUOM());
				eleShipmentLine.setAttribute ("SegmentType", oOrderLine.getSegmentType());
				eleShipmentLine.setAttribute ("Segment", oOrderLine.getSegment());
				eleShipment.setAttribute ("ShipNode", oOrderLine.getShipNode());
			}
		}
		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for changeShipment() API is ...");
			System.out.println( docShipment.getString());
		}
		docShipment = YFCDocument.getDocumentFor (api.changeShipment (env, docShipment.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeShipment() API is ...");
			System.out.println( docShipment.getString());
		}
		return docShipment.getString();
	}

	public String shipOrderLines () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFCDocument		docShipment = YFCDocument.createDocument ("Shipment");	
		YFCElement 		eleShipment = docShipment.getDocumentElement ();
		
		// generate XML for OrderStatusChange element			
			
		// generate XML for OrderLines
		YFCElement eleShipmentLines = eleShipment.createChild ("ShipmentLines");

		// iterate over order lines
		for (int iEle = 0; iEle < getOrderLineCount(); iEle++)
		{
			// create OrderLine htributes
			YFCElement	eleShipmentLine = eleShipmentLines.createChild ("ShipmentLine");
			eleShipmentLine.setAttribute ("OrderHeaderKey", getOrderLine(iEle).getOrderHeaderKey());
			eleShipmentLine.setAttribute ("OrderLineKey", getOrderLine(iEle).getOrderLineKey());
			eleShipmentLine.setAttribute ("ItemID", getOrderLine(iEle).getItemID());
			eleShipmentLine.setAttribute ("Quantity", getOrderLine(iEle).getQty());
			eleShipmentLine.setAttribute ("UnitOfMeasure", getOrderLine(iEle).getUOM());
		}
		// debug message
		if (YFSUtil.getDebug ())
		{
			System.out.println( "Input for confirmShipment() API is ...");
			System.out.println( docShipment.getString());
		}
		docShipment = YFCDocument.getDocumentFor (api.confirmShipment (env, docShipment.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from confirmShipment() API is ...");
			System.out.println( docShipment.getString());
		}
		return docShipment.getString();
	}

	protected	String TranslateStatusText (String sStatus)
	{
		if (sStatus.equalsIgnoreCase("NOT_TAKEN"))
			sStatus="Not_Taken";
		else if (sStatus.equalsIgnoreCase ("NOT_TAKEN_WAITING_FOR_SEQ_LINE"))
			sStatus="Sequencing_Wait";
		else if (sStatus.equalsIgnoreCase ("CONFIRMED"))
			sStatus="Confirmed";
		else if (sStatus.equalsIgnoreCase ("REQUIRES_CONFIRMATION"))
			sStatus="Requires_Confirmation";
		else if (sStatus.equalsIgnoreCase ("OVERRIDDEN"))
			sStatus="Confirmed_with_Override";
		return (sStatus);
	}		


	@SuppressWarnings("deprecation")
	protected	void getFulfillmentOptionsForOrderLine (YFCElement elePromiseLine, int iOption)
	{
		YFCElement	eleOrderLine = elePromiseLine.getChildElement ("OrderLine");
		YFCElement	eleOptions = elePromiseLine.getChildElement ("Options");
		int			iCorrespondingOrderLine;
		
		if (eleOrderLine == null || YFCObject.isVoid(eleOrderLine.getAttribute ("OrderLineKey")))
			iCorrespondingOrderLine = getCorrespondingOrderLine (elePromiseLine);
		else
			iCorrespondingOrderLine = getCorrespondingOrderLine (eleOrderLine);
		
		if (iCorrespondingOrderLine >= 0 && eleOptions != null)
		{
			Iterator<?>	iOptions = eleOptions.getChildren ();	
			while (iOptions.hasNext ())
			{
				YFCElement	eleOption = (YFCElement)iOptions.next();

				// find the requested evaulation option
				if (eleOption.getIntAttribute ("OptionNo") == iOption)
				{
					YantraOrderLine	oOrderLine = getOrderLine (iCorrespondingOrderLine);

					if (eleOption.getAttribute ("IsUnavailable").equals ("N"))
					{
						if (!YFCObject.isVoid (eleOption.getAttribute ("ProductAvailableDate")))
						{
							// iterate over assignments looking for assignment with product availabilty of today
							YFCElement	eleAssignments = eleOption.getChildElement ("Assignments");
							if (eleAssignments != null)
							{
								Iterator<?>	iAssignments = eleAssignments.getChildren ();
								YFCDate		dtAvailableDate = eleOption.getDateAttribute("ProductAvailableDate");
								YFCDate		dtToday = new YFCDate (true);
								boolean		bAppendAssignment = false;
								
								dtAvailableDate.removeTimeComponent();	
								// look for availability today
								while (iAssignments.hasNext ())
								{
									YFCElement	eleAssignment = (YFCElement)iAssignments.next ();
									YFCDate		dtAvailDate = eleAssignment.getDateAttribute ("ProductAvailDate");
									
									dtAvailDate.removeTimeComponent ();
									if (dtAvailDate.compareTo (dtToday) == 0)
									{
										if (bAppendAssignment)
										{
											oOrderLine.setAvailableQty (oOrderLine.getAvailableQty () + "/" + eleAssignment.getAttribute ("Quantity"));
											oOrderLine.setShipNode (oOrderLine.getShipNode() + "/" + eleAssignment.getAttribute ("ShipNode"));
										}
										else
										{
											oOrderLine.setAvailableQty (eleAssignment.getAttribute ("Quantity"));		
											oOrderLine.setShipNode (eleAssignment.getAttribute ("ShipNode"));
											bAppendAssignment = true;
										}
										oOrderLine.setAvailableDate(dtAvailDate.getString (YFCLocale.getDefaultLocale(), false));
										oOrderLine.setShipDate (eleAssignment.getDateAttribute ("ShipDate").getString (YFCLocale.getDefaultLocale(), false));
										oOrderLine.setDeliveryDate (eleAssignment.getDateAttribute ("DeliveryDate").getString (YFCLocale.getDefaultLocale(), false));
									}
								}

								// now make a pass for future inventory availability
								iAssignments = eleAssignments.getChildren ();
								while (iAssignments.hasNext ())
								{
									YFCElement	eleAssignment = (YFCElement)iAssignments.next ();
									YFCDate		dtAvailDate = eleAssignment.getDateAttribute ("ProductAvailDate");
									if (dtAvailDate.compareTo (dtToday) > 0)
									{
										if (bAppendAssignment)
										{
											oOrderLine.setAvailableQty (oOrderLine.getAvailableQty () + "/" + eleAssignment.getAttribute ("Quantity"));		
											oOrderLine.setShipNode (oOrderLine.getShipNode() + "/" + eleAssignment.getAttribute ("ShipNode"));
										}
										else
										{
											oOrderLine.setAvailableQty (eleAssignment.getAttribute ("Quantity"));		
											oOrderLine.setShipNode (eleAssignment.getAttribute ("ShipNode"));
											bAppendAssignment = true;
										}
										oOrderLine.setAvailableDate(dtAvailDate.getString (YFCLocale.getDefaultLocale(), false));
										oOrderLine.setShipDate (eleAssignment.getDateAttribute ("ShipDate").getString (YFCLocale.getDefaultLocale(), false));
										oOrderLine.setDeliveryDate (eleAssignment.getDateAttribute ("DeliveryDate").getString (YFCLocale.getDefaultLocale(), false));
									}
								}
								
								if (eleOption.getAttribute ("HasAnyUnavailableQty").equals ("Y"))
								{
									BigDecimal	bdQuantity = new BigDecimal ("0.00");

									// compute shortage
									iAssignments = eleAssignments.getChildren ();
									while (iAssignments.hasNext ())
									{
										YFCElement	eleAssignment = (YFCElement)iAssignments.next ();
										bdQuantity = bdQuantity.add (new BigDecimal (eleAssignment.getAttribute ("Quantity")));
									}
									BigDecimal	bdRequiredQty = new BigDecimal (elePromiseLine.getAttribute ("RequiredQty"));
									BigDecimal	bdShortage = bdRequiredQty.subtract (bdQuantity);
									oOrderLine.setUnAvailableQty (bdShortage.setScale (2).toString());
								}
							}
						}
					}
					else
						oOrderLine.setAvailableDate ("Not_Available");						
				}
			}
		}
		else
		{
			if (YFSUtil.getDebug())
			{
				System.out.println ("Warning: getCorrespondingOrderLine() method - Either OrderLineKey or TransactionalLineId Not Found in Element: \n" + (eleOrderLine==null ? elePromiseLine.getString() : eleOrderLine.getString()));
			}
		}
	}
		
	public	int	getCorrespondingOrderLine (String sOrderLineKey)
	{
		return getCorrespondingOrderLine (sOrderLineKey, null);
	}
	
	public	int	getCorrespondingOrderLine (String sOrderLineKey, String sTransactionalLineId)
	{
		YFCElement	eleOrderOrPromiseLine;
		if (sOrderLineKey != null)
		{
			eleOrderOrPromiseLine  = YFCDocument.createDocument ("OrderLine").getDocumentElement();
			eleOrderOrPromiseLine.setAttribute ("OrderLineKey", sOrderLineKey);
		}
		else
		{
			eleOrderOrPromiseLine = YFCDocument.createDocument ("PromiseLine").getDocumentElement();
			eleOrderOrPromiseLine.setAttribute ("LineId", sTransactionalLineId);
		}
		return getCorrespondingOrderLine (eleOrderOrPromiseLine);
	}
	
	public	int	getCorrespondingOrderLine (YFCElement eleOrderOrPromiseLine)
	{
		boolean	bFound = false;
		int	iCorrespondingOrderLine;
		
		for (iCorrespondingOrderLine = 0; iCorrespondingOrderLine < getOrderLineCount() && !bFound; iCorrespondingOrderLine++)
		{
			YantraOrderLine	oOrderLine = getOrderLine(iCorrespondingOrderLine);
			
			if (!YFCObject.isVoid (eleOrderOrPromiseLine.getAttribute ("OrderLineKey")))
				bFound = eleOrderOrPromiseLine.getAttribute ("OrderLineKey").equals (oOrderLine.getOrderLineKey ());
			else if (!YFCObject.isVoid (eleOrderOrPromiseLine.getAttribute ("LineId")))
				bFound = eleOrderOrPromiseLine.getAttribute ("LineId").equals (oOrderLine.getTransactionalLineId());
		}
		return bFound ? iCorrespondingOrderLine-1 : -1;
	}
	
	protected	YFCElement	getEvaluationOption (YFCElement eleEvaluateOptions, String sDeliveryMethod)
	{
		Iterator<?>	iEvaluateOptions = eleEvaluateOptions.getChildren ();
		while (iEvaluateOptions.hasNext ())
		{
			YFCElement	eleEvaluateOption = (YFCElement)iEvaluateOptions.next();
			if (eleEvaluateOption.getAttribute ("DeliveryMethod").equals(sDeliveryMethod))
				return eleEvaluateOption;
		}
		return null;
	}

	protected	YFCElement addEvaluationOption (YFCElement eleEvaluateOptions, String sDeliveryMethod)
	{
		YFCElement	eleEvaluateOption = eleEvaluateOptions.createChild ("EvaluateOption");
		eleEvaluateOption.setAttribute ("DeliveryMethod", sDeliveryMethod);
		if (sDeliveryMethod.equals ("SHP"))
			eleEvaluateOption.setAttribute ("CarrierServiceCode", getCarrierServiceCode());	
		return eleEvaluateOption;
	}
	

	
	// protected member variables
	protected String	m_sOrderHeaderKey;
	protected String	m_sEnterpriseCode;
	protected String	m_sBuyerOrgCode;
	protected String	m_sSellerOrgCode;
	protected String	m_sDocumentType;
	protected String	m_sOrderNo;
	protected String	m_sOrderType;
	protected String	m_sOrderName;
	protected String	m_sOrderDate;
	protected String	m_sStatus;
	protected String	m_sStatusCode;
	protected String	m_sSourcingClassification;
	protected String	m_sSourcingRule;
	protected String	m_sRequestDate;
	protected String	m_sCarrierServiceCode;
	protected boolean	m_bIsDraft;	
	protected Vector<Object>	m_vecOrderLines;
	protected Customer	m_oCustomer;
	protected Hashtable<String, String>	m_htModsAllowed;
}
