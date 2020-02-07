/**
  * YantraItemList.java
  *
  **/

// PACKAGE
package com.custom.yantra.items;

import	java.util.*;
import	org.w3c.dom.*;

import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class YantraItemList implements Serializable
{
    public YantraItemList()
    {
		m_sShipNode = "";
		m_sOrganizationCode = "";
		m_sDistributionRuleId="";
		m_sItemList = "";
		m_vecItemLines = new Vector<YantraItem> ();
		m_vecItemSelected = new Vector<Integer> ();
		m_bConsiderAllNodes = false;
    }
	
	public String	getShipNode ()
	{
		return m_sShipNode;
	}
	
	public	void 	setShipNode(String sShipNode, String sConsiderAllNodes)
	{
		m_sShipNode = sShipNode;
		if (sConsiderAllNodes.equalsIgnoreCase ("Y"))
		{
			m_bConsiderAllNodes = true;
			m_sDistributionRuleId = "";
		}
		else if (sConsiderAllNodes.equalsIgnoreCase ("N"))
		{
			m_bConsiderAllNodes = false;
			m_sDistributionRuleId = "";
		}
		else
		{
			m_sDistributionRuleId = sConsiderAllNodes;
			m_bConsiderAllNodes = false;
		}
	}	

	public	void 	setShipNode(String sShipNode)
	{
		m_sShipNode = sShipNode;
	}	

	public	void 	setDistributionRuleId(String sDistributionRuleId)
	{
		m_sDistributionRuleId = sDistributionRuleId;
	}	

	public	String	getDistributionRuleId()
	{
		return m_sDistributionRuleId;
	}	

	public	void 	setOrganizationCode (String sOrganizationCode)
	{
		m_sOrganizationCode = sOrganizationCode;
	}	

	public	String	getOrganizationCode()
	{
		return m_sOrganizationCode;
	}	

	public void addItem (YantraItem oLine)
	{
		m_vecItemLines.addElement (oLine);
	}
	
	public	void addItemSelected (int iSelected)
	{
		m_vecItemSelected.addElement (new Integer (iSelected));
	}
	
	public	int	getItemSelected (int iSelected)
	{
		return ((Integer)m_vecItemSelected.elementAt (iSelected)).intValue();
	}
	
	public	int	getItemSelectedCount ()
	{
		return m_vecItemSelected.size();
	}
		
	public	YantraItem getItem (int iLine)
	{
		return (YantraItem)m_vecItemLines.elementAt (iLine);
	}
	
	public	int	getItemCount ()
	{
		return m_vecItemLines.size();
	}
	
	public	void Reset ()
	{
		m_sShipNode = "";
		m_sOrganizationCode = "";
		m_sDistributionRuleId="";
		m_sItemList = "";
		ResetSelected();
		for (int iItem = 0; iItem < getItemCount(); iItem++)
			getItem(iItem).Reset();
		m_vecItemLines.clear ();
		m_sItemList = null;
	}
	
	public	void ResetSelected ()
	{
		m_vecItemSelected.clear ();
	}		
	
	public String getItemList (String sItemID, String sShortDescription) throws Exception
	{	
		YFSEnvironment env = YFSUtil.getYFSEnv();
		return (getItemList (env, sItemID, sShortDescription));		
	}


	public String getItemList () throws Exception
	{
		// if no cached item list	
		if (m_sItemList == null)
		{
			YFSEnvironment env = YFSUtil.getYFSEnv();
			getItemList (env, null, null);
		}
		return m_sItemList;
	}
	
	@SuppressWarnings("unused")
	public String getItemList (YFSEnvironment env, String sItemID, String sShortDescription) throws Exception
	{
		YIFApi api = YFSUtil.getYIFApi();		

		Hashtable<String, String> htItem = new Hashtable<String, String>();
		htItem.put("ItemGroupCode", "PROD");
		if (sItemID != null && sItemID.length () > 0)
		{
			htItem.put("ItemID", sItemID);
			htItem.put("ItemIDQryType" , "FLIKE");
		}
		
		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		Element eleItem = inXml.createRootElement("Item", htItem);

		// create OrderLine attributes
		Hashtable<String, String> attPrimaryInformation = new Hashtable<String, String>();
		if (sShortDescription != null && sShortDescription.length() > 0)
		{
			attPrimaryInformation.put("ShortDescription", sShortDescription);
			attPrimaryInformation.put("ShortDescriptionQryType", "LIKE");
		}
		// generate XML for item line Primary Information
		Element elePrimaryInformation = inXml.createChild (eleItem, "PrimaryInformation", attPrimaryInformation);
		elePrimaryInformation.setAttribute ("IsModelItem", "");
		eleItem.setAttribute ("IsForOrdering", "Y");
		
		return getItemList (env, inXml.getDocument());
	}

	public	String	getItemList (YFSEnvironment env, Document inXml) throws Exception
	{
		YIFApi api = YFSUtil.getYIFApi();		

		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getItemList() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml));
		}

		// call the getItemList API
		YFCDocument docItemList = YFCDocument.getDocumentFor (api.getItemList (env, inXml));
		YFCElement	eleItemList = docItemList.getDocumentElement();
				
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getItemList() API is: ");
			System.out.println (docItemList.getString());
		}

		if (docItemList != null)
		{
			Iterator<?>	iItemList = eleItemList.getChildren();
				
			// if at least one order line
			while (iItemList.hasNext())
			{
				// get the first/next order line from output XML
				YFCElement	eleItem = (YFCElement)iItemList.next();
				YFCElement	elePrimaryInformation = eleItem.getChildElement ("PrimaryInformation");
				if (YFCObject.isVoid(elePrimaryInformation.getAttribute ("IsModelItem")) || elePrimaryInformation.getAttribute ("IsModelItem").equals("N"))
				{
					// create order line saving relevant details
					YantraItem	oYantraItem = (YantraItem)createNewItem();
					oYantraItem.setItemKey (eleItem.getAttribute ("ItemKey"));
					oYantraItem.setOrganizationCode (eleItem.getAttribute ("OrganizationCode"));
					oYantraItem.setItemID(eleItem.getAttribute ("ItemID"));
					oYantraItem.setUOM (eleItem.getAttribute("UnitOfMeasure"));
					oYantraItem.setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));
					oYantraItem.setProductClass (elePrimaryInformation.getAttribute ("DefaultProductClass"));
					oYantraItem.setProductLine (elePrimaryInformation.getAttribute ("ProductLine"));
					
					// now load additional (custom) attributes into the item
					oYantraItem.loadAdditionalAttributes (eleItem);
					addItem (oYantraItem);
				}
			}
		}
		return m_sItemList;
	}

/*	

	public	String	getItemAttribute (int iEle, String sElement, String sAttribute)
	{
		return getItem(iEle).getItemAttribute (sElement, sAttribute);
	}


	public	String	getOnHandATP (int iEle) throws Exception
	{
		return getATP (iEle, "ONHAND");
	}
	
	public	String	getTotalSupplyATP (int iEle) throws Exception
	{
		return getATP (iEle, null);
	}
		
	public	String	getATP (int iEle) throws Exception
	{
		return getATP (iEle, "ONHAND");
	}
	
	public	String	getATP (int iEle, String sSupplyType) throws Exception
	{
		return (getItem(iEle).getATP(sSupplyType));
	}

	public	void adjustInventory (int iEle, int iQtyToAdjustBy) throws Exception
	{
		adjustInventory (iEle, Integer.toString(Integer.parseInt (getItem(iEle).getQtyOnHand())+iQtyToAdjustBy));
	}
	
	public	void adjustInventory (int iEle, String sQty) throws Exception
	{
		getItem(iEle).adjustInventory (sQty);
	}
	

	public	void adjustSelectedInventory (String sQty[]) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		YFSXMLParser inXml = new YFSXMLParser();
		Element eleItems = inXml.createRootElement("Items", null);

		for (int iEle = 0; iEle < getItemSelectedCount(); iEle++)
		{		
			Hashtable htItem = new Hashtable();
			htItem.put ("ItemID", getItem (getItemSelected(iEle)).getItemID());
			htItem.put ("UnitOfMeasure", getItem(getItemSelected(iEle)).getUOM());
			htItem.put ("ShipNode", getShipNode());
			htItem.put ("AdjustmentType", "ADJUSTMENT");
			htItem.put ("Quantity", Integer.toString (Integer.parseInt(sQty[iEle])-Integer.parseInt(getItem(getItemSelected(iEle)).getQtyOnHand())));
			htItem.put ("SupplyType", "ONHAND");
			inXml.createChild(eleItems, "Item", htItem);
		}
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

	public	void resetInventory (String sQtyOnHand) throws Exception
	{
		String[] sQty = new String[m_vecItemLines.size()];
		for (int iCnt = 0; iCnt < m_vecItemLines.size(); iCnt++)
		{
			// get current availability
			getATP (iCnt);
			
			// add the item to the selection list
			addItemSelected (iCnt);
			sQty[iCnt] = sQtyOnHand;
		}
		// adjust all items
		adjustSelectedInventory (sQty);
	}
*/		
	
	public	Object	createNewItem ()
	{
		YantraItem	oYantraItem = new YantraItem(this);
		
		return oYantraItem;	
	}

	// protected member variables
	protected	String	m_sOrganizationCode;
	protected	Vector<YantraItem>	m_vecItemLines;
	protected	Vector<Integer>	m_vecItemSelected;
	protected	String	m_sShipNode;
	protected	String	m_sDistributionRuleId;
	protected	boolean	m_bConsiderAllNodes;
	protected	String	m_sItemList; // cached item list from getlist API			
}

