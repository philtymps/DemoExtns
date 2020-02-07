/**
  * YantraItem.java
  *
  **/

// PACKAGE
package com.custom.yantra.items;

import java.util.*;
import	org.w3c.dom.*;

import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class YantraItem implements Serializable
{
    public YantraItem(Object oItems)
    {
		m_sQtyAvailable = "0";
		m_sItemKey = "";
		m_sProductLine = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sItemShortDesc = "";
		m_sItemGroupCode = "";
		m_sUOM = "";
		m_sQtyAvailable = "";
		m_sQtyOnHand = "";
		m_sOrganizationCode = "";
		m_oItems = (YantraItemList)oItems;
		m_vecAdditionalAttributes = new Vector<YantraAdditionalAttribute>();
		m_vecNodes = new Vector<YantraNode>();
    }

	public	String	getItemKey () { return m_sItemKey; }
	public	void	setItemKey (String sItemKey) { m_sItemKey = sItemKey; }
	public	String	getItemID () { return m_sItemID; }
	public	void	setItemID (String sItemID) { m_sItemID = sItemID; }
	public	String	getItemShortDesc ()	{ return m_sItemShortDesc; }
	public	void	setItemShortDesc (String sItemShortDesc) { m_sItemShortDesc = sItemShortDesc; }
	public	String	getItemGroupCode ()	{ return m_sItemGroupCode; }
	public	void	setItemGroupCode (String sItemGroupCode) { m_sItemGroupCode = sItemGroupCode; }
	public	String	getOrganizationCode () { return m_sOrganizationCode; }
	public	void	setOrganizationCode (String sOrganizationCode) { m_sOrganizationCode = sOrganizationCode; }
	public	String	getProductClass () { return m_sProductClass; }
	public	void	setProductClass (String sProductClass) { m_sProductClass = sProductClass; }
	public	String	getUOM () { return m_sUOM; }
	public	void	setUOM (String sUOM) { m_sUOM = sUOM; }
	public	String	getProductLine ()	{ return m_sProductLine; }
	public	void	setProductLine (String sProductLine) { m_sProductLine = sProductLine; }
	public	String	getQtyOnHand () { return m_sQtyOnHand; }
	public	void	setQtyOnHand (String sQtyOnHand) { m_sQtyOnHand = sQtyOnHand; }
	public	void	setQtyAvailable (String sQtyAvailable) { m_sQtyAvailable = sQtyAvailable; }
	public	String	getQtyAvailable () { return m_sQtyAvailable.length()==0 ? "0" : m_sQtyAvailable; }
	public	void	setDateAvailable (String sDateAvailable) { m_sDateAvailable = sDateAvailable; }
	public	String	getDateAvailable () { return m_sDateAvailable; }

	public	YantraItemList	getItems () { return m_oItems; }
	public	int				getShipNodeCount () { return m_vecNodes.size(); }
	public	YantraNode		getShipNode (int iNode) { return (YantraNode)m_vecNodes.elementAt (iNode); }

	public	void Reset ()
	{
		m_sItemKey = "";
		m_sQtyAvailable = "0";
		m_sProductLine = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sOrganizationCode = "";
		m_sUOM = "";
		m_sItemShortDesc = "";
		m_sQtyAvailable = "";
		m_sQtyOnHand = "";
		m_vecAdditionalAttributes.clear ();
		m_vecNodes.clear();
	}


	public	String	getAdditionalAttribute (String sName)
	{
		String	sValue = "";
		for (int iAtt = 0; iAtt < m_vecAdditionalAttributes.size(); iAtt++)
		{
			YantraItem.YantraAdditionalAttribute oAttr = (YantraItem.YantraAdditionalAttribute)m_vecAdditionalAttributes.elementAt(iAtt);

			// if name matches one requested
			if (oAttr.getName().equalsIgnoreCase (sName))
			{
				sValue = oAttr.getValue();
				break;
			}
		}
		return sValue;
	}

	public	void setAdditionalAttribute (String sName, String sValue)
	{
		YantraItem.YantraAdditionalAttribute	oAttr = new YantraItem.YantraAdditionalAttribute ();
		oAttr.setName (sName);
		oAttr.setValue (sValue);
		m_vecAdditionalAttributes.add (oAttr);
	}

/*
	public	String	getItemAttribute (String sElement, String sAttribute)
	{
		String	sValue = "";

		// because we don't cache all item attributes in the YantraItem object
		// this method allows you to retrieve those attribute values that are
		// less commonly accessed.  For example, Inventory attributes are
		// not cached in the YantraItem, but using this method, you can still
		// access these item values by calling this method.  For example:
		// getItemAttribute (iEle, "<InventoryParameters>", "LeadTime")
		// will return Inventory Lead time for this item.

	  try {
	  	YFCElement	eleItems = 
		YCMItemListOutputDoc oItemList = new YCMItemListOutputDoc (getItems().getItemList());

		if (oItemList.getItemList() != null)
		{
			Enumeration oItems = oItemList.getItemList().getItemList();

			// if at least one order line
			for (int iNextEle = 0; oItems.hasMoreElements(); iNextEle++)
			{
				// get the first/next order line from output XML
				YCMItemListOutputDoc.Item oItem = (YCMItemListOutputDoc.Item)oItems.nextElement();

				// if on the right element
				if (oItem.getPrimaryInformation().getAttribute ("ItemKey").equals (getItemKey()))
				{
					YFSXMLParser inXml = new YFSXMLParser();
					Element eleRoot = inXml.createRootElement ("Root", null);
					inXml.getDocument().appendChild((Element)oItem);

					String sElementXml = YFSXMLUtil.getXMLField (YFSXMLUtil.getXMLString (inXml.getDocument()), sElement);
					sValue = YFSXMLUtil.getAttrValue (sElementXml, sAttribute);
					break;
				}
			}
		}
	  } catch (Exception e) {
	  }
		return sValue;
	}
*/

	public	String	getStoreAvailability (String sZipCode, String sCountry) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFSXMLParser inXml = new YFSXMLParser();
				
		Hashtable<String, String>	htPromise = new Hashtable<String, String>();
		htPromise.put ("OrganizationCode", getItems().getOrganizationCode ());
		htPromise.put ("Store", getItems().getShipNode());
		htPromise.put ("IgnoreMinNotificationTime", "N");
		htPromise.put ("IgnorePromised", "N");
		htPromise.put ("IgnoreUnpromised", "Y");
		Element elePromiseIn = inXml.createRootElement ("Promise", htPromise);

		Hashtable<String, String> htShipToAddress = new Hashtable<String, String>();
		htShipToAddress.put ("Country", sCountry);
		htShipToAddress.put ("ZipCode", sZipCode);
		inXml.createChild (elePromiseIn, "ShipToAddress", htShipToAddress);

		Hashtable<String, String> htFulfillmentTypes = new Hashtable<String, String>();
		htFulfillmentTypes.put ("ExpeditedTransferPickupFulfillmentType", "");
		htFulfillmentTypes.put ("ShipToHomeFulfillmentType", "");
		htFulfillmentTypes.put ("SpecialOrderFulfillmentType", "");
		htFulfillmentTypes.put ("StdTransferPickupFulfillmentType", "");
		inXml.createChild (elePromiseIn, "FulfillmentTypes", htFulfillmentTypes);
		
		// set up PromiseLine element
		Hashtable<String, String> htPromiseLine = new Hashtable<String, String>();
		htPromiseLine.put ("ItemID", getItemID());
		htPromiseLine.put ("UnitOfMeasure", getUOM());
		htPromiseLine.put ("RequiredQty", "1");
		htPromiseLine.put ("ProductClass", getProductClass());

		// add child element
		Element	elePromiseLines = inXml.createChild (elePromiseIn, "PromiseLines", null);
		inXml.createChild (elePromiseLines, "PromiseLine", htPromiseLine);

		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getStoreAvailability() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		Document docOut = api.getStoreAvailability (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getStoreAvailability() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (docOut));
		}
		YFCDocument	docPromise = YFCDocument.getDocumentFor (docOut);
		YFCElement	elePromise = docPromise.getDocumentElement ();
		
		YFCElement eleInventorySupplies	= elePromise.getChildElement ("InventorySupplies");
		YFCElement eleItems = eleInventorySupplies.getChildElement ("Items");
		YFCElement eleItem = eleItems.getChildElement ("Item");
		YFCElement eleNodes = eleItem.getChildElement ("Nodes");
		long	lAvailable = 0;
		for (Iterator<?> iNodes = eleNodes.getChildren(); iNodes.hasNext(); )
		{
			YFCElement	eleNode = (YFCElement)iNodes.next();
			YFCElement	eleSupplies = eleNode.getChildElement ("Supplies");
			
			for (Iterator<?> iSupplies = eleSupplies.getChildren(); iSupplies.hasNext(); )
			{
				YFCElement	eleSupply = (YFCElement)iSupplies.next();
				YantraNode	oNode = new YantraNode (this);
				oNode.setShipNode (eleNode.getAttribute ("Node"));
				oNode.setQtyAvailable (eleSupply.getAttribute ("AvailableQty"));
				oNode.setQtyOnHand (eleSupply.getAttribute ("AvailableQty"));
				lAvailable = lAvailable + Long.parseLong (oNode.getQtyAvailable ());
			}
		}
		setQtyAvailable (Long.toString (lAvailable));
		return docPromise.getString();
	}
	
	public	String	getATP (String sSupplyType) throws Exception
	{
		return getATP (sSupplyType, null);
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	public	String	getATP (String sSupplyType, String sRequiredQty) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();

		YFCDocument	docGetATP = YFCDocument.createDocument ("GetATP");
		YFCElement	eleGetATP = docGetATP.getDocumentElement ();

		// set up search criteria (by Status)
		Hashtable<?, ?> htGetATP = new Hashtable<Object, Object>();
		eleGetATP.setAttribute ("ItemID", getItemID());
		eleGetATP.setAttribute ("UnitOfMeasure", getUOM());
		eleGetATP.setAttribute ("OrganizationCode", getItems().getOrganizationCode());
		eleGetATP.setAttribute ("ProductClass", getProductClass());
		if (sRequiredQty != null)
			eleGetATP.setAttribute ("RequiredQty", sRequiredQty);
			
		if (getItems().getDistributionRuleId ().length() > 0)
		{
			eleGetATP.setAttribute ("ConsiderAllNodes", "N");
			eleGetATP.setAttribute ("DistributionRuleId", getItems().getDistributionRuleId());
		}
		else if (getItems().getShipNode().length() > 0)
		{
			eleGetATP.setAttribute ("ConsiderAllNodes", "N");
			eleGetATP.setAttribute ("ShipNode", getItems().getShipNode());
		}
		else
			eleGetATP.setAttribute ("ConsiderAllNodes", "Y");

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item
		// information
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getATP() API is: ");
			System.out.println (docGetATP.getString());
		}
		docGetATP = YFCDocument.getDocumentFor (api.getATP (env, docGetATP.getDocument()));
		YFCElement eleInventoryInformation = docGetATP.getDocumentElement ();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getATP() API is: ");
			System.out.println (docGetATP.getString());
		}

		// get item information returned
		YFCElement	eleItem = eleInventoryInformation.getChildElement ("Item");
		
		// get quantity avaialable for sale today
		setQtyAvailable (eleItem.getAttribute("AvailableToSell"));
		setDateAvailable (new YFCDate(true).getString (YFCLocale.getDefaultLocale(), false));
		YFCElement eleInventoryCalculator = eleItem.getChildElement ("InventoryCalculator");

		// if availability of item is in the future, inventory calculator element will show availability dates
		if (eleInventoryCalculator != null)
		{
			Iterator<?>	iInventory = eleInventoryCalculator.getChildren();
			while (iInventory.hasNext())
			{
				YFCElement	eleInventory = (YFCElement)iInventory.next();
				setDateAvailable (eleInventory.getAttribute ("Date"));
			}
		}
		// if specific supply type requested
		if (sSupplyType != null)
		{
			YFCElement	eleCurrentInventory = eleItem.getChildElement ("CurrentInventory");
			YFCElement	eleSupplies = eleCurrentInventory.getChildElement ("Supplies");
			Iterator<?>	iSupplies = eleSupplies.getChildren ();
			
			while (iSupplies.hasNext ())
			{
				YFCElement	eleSupply = (YFCElement)iSupplies.next();

				if (eleSupply.getAttribute("SupplyType").equals(sSupplyType))
				{
					setQtyOnHand (eleSupply.getAttribute("Quantity"));
					break;
				}
			}
		}
		else
		{
			YFCElement	eleInventoryTotals = eleItem.getChildElement ("InventoryTotals");
			YFCElement	eleSupplies = eleInventoryTotals.getChildElement ("Supplies");
			setQtyOnHand (eleSupplies.getAttribute ("TotalSupply"));
		}
		return docGetATP.getString();
	}

	@SuppressWarnings("unused")
	public String	findInventory (String sRequiredQty) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi 			api = YFSUtil.getYIFApi();
		YFCDocument		docPromise = YFCDocument.createDocument ("Promise");
		YFCElement		elePromise = docPromise.getDocumentElement ();
		
		elePromise.setAttribute ("OrganizationCode", getItems().getOrganizationCode ());
		elePromise.setAttribute ("CheckInventory", "Y");
		elePromise.setAttribute ("OptimizationType", "01"); //Optimize by date
		elePromise.setAttribute ("MaximumRecords", "1");
		YFCElement	elePromiseLines = elePromise.createChild ("PromiseLines");
		YFCElement	elePromiseLine = elePromiseLines.createChild ("PromiseLine");
		elePromiseLine.setAttribute ("ItemID", getItemID());
		elePromiseLine.setAttribute ("UnitOfMeasure", getUOM());
		elePromiseLine.setAttribute ("ProductClass", getProductClass());
		if (sRequiredQty != null)
			elePromiseLine.setAttribute ("RequiredQty", sRequiredQty);
			
			
		///TO DO - FINISH
		return null;
	}
	
	public String	getItemDetails () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi 			api = YFSUtil.getYIFApi();
		YFCDocument		docItem = YFCDocument.createDocument ("Item");
		YFCElement		eleItem = docItem.getDocumentElement ();
		
		if (YFCObject.isVoid (getItemKey()))
		{
			eleItem.setAttribute ("OrganizationCode", getItems().getOrganizationCode ());
			eleItem.setAttribute ("ItemID", getItemID());
			eleItem.setAttribute ("UnitOfMeasure", getUOM());
			//eleItem.setAttribute ("ProductClass", getProductClass());
		}
		else
			eleItem.setAttribute ("ItemKey", getItemKey());
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getItemDetails() API is: ");
			System.out.println (docItem.getString());
		}
		docItem = YFCDocument.getDocumentFor (api.getItemDetails (env, docItem.getDocument()));
		String	sItemDetails = docItem.getString();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output From getItemDetails() API is: ");
			System.out.println (sItemDetails);
		}
		eleItem = docItem.getDocumentElement();
		
		setItemID (eleItem.getAttribute("ItemID"));
		setItemKey (eleItem.getAttribute ("ItemKey"));
		setOrganizationCode (eleItem.getAttribute("OrganizationCode"));
		setItemGroupCode (eleItem.getAttribute ("ItemGroupCode"));
		setUOM (eleItem.getAttribute ("UnitOfMeasure"));
		
		YFCElement	elePrimaryInformation = eleItem.getChildElement("PrimaryInformation");		
		setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));
		setProductLine(elePrimaryInformation.getAttribute ("ProductLine"));
		setProductClass (elePrimaryInformation.getAttribute ("DefaultProductClass"));
		loadAdditionalAttributes (eleItem);
		
		return sItemDetails;
	}

	public	void adjustInventory (int iQty) throws Exception
	{
		adjustInventoryBy (Integer.toString (iQty-Integer.parseInt(getQtyOnHand())));
	}

	public	void adjustInventory (String sQty) throws Exception
	{
		adjustInventoryBy (Integer.toString (Integer.parseInt(sQty)-Integer.parseInt(getQtyOnHand())));
	}
	
	public	void adjustInventoryBy (String sQty) throws Exception
	{
		adjustInventoryBy (Integer.parseInt (sQty));
	}

	public	void adjustInventoryBy (int iValue) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		YFSXMLParser inXml = new YFSXMLParser();
		Element eleItems = inXml.createRootElement("Items", null);

		Hashtable<String, String> htItem = new Hashtable<String, String>();
		htItem.put ("ItemID", getItemID());
		htItem.put ("UnitOfMeasure", getUOM());
		htItem.put ("ProductClass", getProductClass());
		htItem.put ("ShipNode", getItems().getShipNode());
		htItem.put ("AdjustmentType", "ADJUSTMENT");
		htItem.put ("Quantity", Integer.toString (iValue));
		htItem.put ("SupplyType", "ONHAND");
		inXml.createChild(eleItems, "Item", htItem);

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item
		// information
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to adjustInventory() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		api.adjustInventory (env, inXml.getDocument());
		return;
	}


	@SuppressWarnings("unused")
	public	void	getSupplyDetails (String sSupplyType) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		YFCDocument	docSupplyDetails = YFCDocument.createDocument ("getSupplyDetails");
		YFCElement	eleSupplyDetails = docSupplyDetails.getDocumentElement();
		
		// set up search criteria (by Status)
		Hashtable<?, ?> htGetSupplyDetails = new Hashtable<Object, Object>();
		eleSupplyDetails.setAttribute ("ItemID", getItemID());
		eleSupplyDetails.setAttribute ("UnitOfMeasure", getUOM());
		eleSupplyDetails.setAttribute ("OrganizationCode", getItems().getOrganizationCode());
		eleSupplyDetails.setAttribute ("ProductClass", getProductClass());

		// if distribution rule in play
		if (getItems().getDistributionRuleId().length() > 0)
		{
			eleSupplyDetails.setAttribute ("ConsiderAllNodes", "N");
			eleSupplyDetails.setAttribute ("DistributionRuleId", getItems().getDistributionRuleId());
		}
		// if specific ship node in play
		else if (getItems().getShipNode().length() > 0)
		{
			eleSupplyDetails.setAttribute ("ConsiderAllNodes", "N");
			eleSupplyDetails.setAttribute ("ShipNode", getItems().getShipNode());
		}
		else
			eleSupplyDetails.setAttribute ("ConsiderAllNodes", "Y");


		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getSupplyDetails() API is: ");
			System.out.println (docSupplyDetails.getString());
		}
		docSupplyDetails = YFCDocument.getDocumentFor (api.getSupplyDetails (env, docSupplyDetails.getDocument()));
		YFCElement eleItem = docSupplyDetails.getDocumentElement ();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getSupplyDetails() API is: ");
			System.out.println (docSupplyDetails.getString());
		}
		YFCElement	eleShipNodes = eleItem.getChildElement ("ShipNodes");
		
		// if ship nodes present
		if (eleShipNodes != null)
		{
			Iterator<?> iShipNodes = eleShipNodes.getChildren ();

			while (iShipNodes.hasNext ())
			{
				YFCElement eleShipNode = (YFCElement)iShipNodes.next();
				YantraNode	oNode = new YantraNode (this);
				oNode.setQtyAvailable (eleShipNode.getAttribute ("AvailableQty"));
				oNode.setShipNode (eleShipNode.getAttribute ("ShipNode"));

				// now get supply in requested supply type
				YFCElement	eleSupplies = eleShipNode.getChildElement ("Supplies");
				Iterator<?>	iSupplies = eleSupplies.getChildren();
				
				while (iSupplies.hasNext ())
				{
					YFCElement	eleSupply = (YFCElement)iSupplies.next();

					if (eleSupply.getAttribute("SupplyType").equals(sSupplyType))
					{
						oNode.setQtyOnHand (eleSupply.getAttribute("TotalQuantity"));
						break;
					}
				}
				m_vecNodes.add(oNode);
			}
		}
	}

	// protected members
	protected	void	loadAdditionalAttributes (YFCElement eleItem)
	{
		YFCElement	eleAdditionalAttributeList = eleItem.getChildElement ("AdditionalAttributeList");
		if (eleAdditionalAttributeList != null)
		{
			Iterator<?> iAdditionalAttributeList = eleAdditionalAttributeList.getChildren();

			if (YFSUtil.getDebug())
			{
				if (iAdditionalAttributeList.hasNext())
					System.out.println ("Additional Attributes on Item "+getItemID()+ " found");
			}

			while (iAdditionalAttributeList.hasNext())
			{
				YFCElement	eleAdditionalAttribute = (YFCElement)iAdditionalAttributeList.next();
				setAdditionalAttribute (eleAdditionalAttribute.getAttribute ("Name"), eleAdditionalAttribute.getAttribute ("Value"));
				if (YFSUtil.getDebug())
				{
					System.out.println ("Attribute Name = "+eleAdditionalAttribute.getAttribute ("Name") + " Value = "+ eleAdditionalAttribute.getAttribute ("Value"));
				}
			}
		}
		return;
	}

	public		class	YantraNode implements Serializable
	{
		public	YantraNode (YantraItem oItem)
		{
			m_sQtyOnHand = "";
			m_sQtyAvailable="";
			m_sShipNode = "";
			m_oItem = oItem;
		}

		public	String		getShipNode() { return m_sShipNode; }
		public	void		setShipNode (String sShipNode) { m_sShipNode = sShipNode; }
		public	void		setQtyOnHand (String sQtyOnHand) { m_sQtyOnHand = sQtyOnHand; }
		public	String		getQtyOnHand () { return m_sQtyOnHand.length()==0 ? "0" : m_sQtyOnHand; }
		public	void		setQtyAvailable (String sQtyAvailable) { m_sQtyAvailable = sQtyAvailable; }
		public	String		getQtyAvailable () { return m_sQtyAvailable.length()==0 ? "0" : m_sQtyAvailable; }
		public	YantraItem	getItem() { return m_oItem; }

		protected	String		m_sQtyOnHand;
		protected	String		m_sQtyAvailable;
		protected	String		m_sShipNode;
		protected	YantraItem	m_oItem;
	}

	protected	class	YantraAdditionalAttribute implements Serializable
	{
		public	String	getName () { return m_sName; }
		public	void	setName (String sName) { m_sName = sName; }
		public	String	getValue () { return m_sValue; }
		public	void	setValue (String sValue) { m_sValue = sValue; }

		protected	String	m_sName;
		protected	String	m_sValue;
	}

	// protected member variables
	protected	String			m_sItemKey;
	protected	String			m_sItemID;
	protected	String			m_sUOM;
	protected	String			m_sItemShortDesc;
	protected	String			m_sItemGroupCode;
	protected	String			m_sProductLine;
	protected	String			m_sOrganizationCode;
	protected	String			m_sProductClass;
	protected	String			m_sQtyAvailable;
	protected	String			m_sQtyOnHand;
	protected	String			m_sDateAvailable;
	protected	Vector<YantraAdditionalAttribute>			m_vecAdditionalAttributes;
	protected	Vector<YantraNode>			m_vecNodes;
	protected	YantraItemList	m_oItems;
}

