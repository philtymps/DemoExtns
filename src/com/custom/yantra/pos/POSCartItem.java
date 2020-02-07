/**
  * POSCartItem.java
  *
  **/

// PACKAGE
package com.custom.yantra.pos;

import	java.util.*;
import	java.math.*;
import  java.io.Serializable;
import	com.custom.yantra.util.*;

import	com.yantra.yfc.dom.*;
import	com.yantra.yfc.util.*;

import	org.w3c.dom.*;

import	com.yantra.yfs.japi.YFSEnvironment;
import  com.yantra.interop.japi.YIFApi;

@SuppressWarnings("serial")
public class POSCartItem implements Serializable
{
    @SuppressWarnings("rawtypes")
	public POSCartItem (POSCart oPOSCart)
    {	
		m_oPOSCart = oPOSCart;
		m_sItemKey = "";
		m_sOrderLineKey = "";
		m_sLineType = "";
		m_sOrgCode = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sUOM = "EA";
		m_sItemShortDesc = "";
		m_sQty = "0";
		m_sSvcQty = "0";
		m_sPrice = "0.00";
		m_sUnitPrice = "0.00";
		m_sTotalPrice = "0.00";
		m_sShipping = "0.00";
		m_sDiscount = "0.00";
		m_bIsKit = false;
		m_bHasServices = false;
		m_sTransactionalLineId = "";
		m_sPrimeLineNo = "";
		m_sRequestedDate = "";
		m_sRequiresProdAssociation = "N";
		m_vecComponents = new Vector();
    }
	
	public	String	getOrderLineKey () { return m_sOrderLineKey; }
	public	void	setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
	public	String	getLineType () { return m_sLineType; }
	public	void	setLineType (String sLineType) { m_sLineType = sLineType; }
	public	String	getItemKey () { return m_sItemKey; }
	public	void	setItemKey (String sItemKey) { m_sItemKey = sItemKey; }
	public	String	getItemID () { return m_sItemID; }
	public	void	setItemID (String sItemID) { m_sItemID = sItemID; }
	public	String	getProductClass () { return m_sProductClass; }
	public	void	setProductClass (String sProductClass) { m_sProductClass = sProductClass; }
	public	String	getItemGroupCode () { return m_sItemGroupCode; }
	public	void	setItemGroupCode (String sItemGroupCode) { m_sItemGroupCode = sItemGroupCode; }
	public	String	getOrgCode () { return m_sOrgCode; }
	public	void	setOrgCode (String sOrgCode) { m_sOrgCode = sOrgCode; }
	public	String	getOEM () { return m_sOEM; }
	public	void	setOEM (String sOEM) { m_sOEM = sOEM; }
	public	String	getQty () { return m_sQty; }
	public	void	setQty (String sQty) { m_sQty = sQty; }
	public	String	getSvcQty () { return m_sSvcQty; }
	public	void	setSvcQty (String sSvcQty) { m_sSvcQty = sSvcQty; }
	public	String	getUOM () { if (m_sUOM != null) return m_sUOM; else return new String(); }
	public	void	setUOM (String sUOM) { if (sUOM !=null ) m_sUOM = sUOM; }
	public	String	getItemShortDesc ()	{ return m_sItemShortDesc; }
	public	void	setItemShortDesc (String sItemShortDesc) { m_sItemShortDesc = sItemShortDesc; }
	public	String	getPrice () { return m_sPrice; }
	public	void	setPrice (String sPrice) { m_sPrice = sPrice; }
	public	String	getUnitPrice () { return m_sUnitPrice; }
	public	void	setUnitPrice (String sUnitPrice) { m_sUnitPrice = sUnitPrice; }
	public	String	getTotalPrice () { return m_sTotalPrice; }
	public	void	setTotalPrice (String sTotalPrice) { m_sTotalPrice = sTotalPrice; }
	public	String	getShipping () { return m_sShipping; }
	public	void	setShipping (String sShipping) { m_sShipping = sShipping; }
	public	String	getDiscount () { return m_sDiscount; }
	public	void	setDiscount (String sDiscount) { m_sDiscount = sDiscount; }
	public	boolean	getIsKit () { return m_bIsKit; }
	public	void	setIsKit (boolean bIsKit) { m_bIsKit = bIsKit; }
	public	boolean getHasServices ()	{ return m_bHasServices; }
	public	void	setHasServices (boolean bHasServices) { m_bHasServices = bHasServices; }
	public	String	getPrimeLineNo ()	{ return m_sPrimeLineNo; }
	public	void	setPrimeLineNo (String sPrimeLineNo) { m_sPrimeLineNo = sPrimeLineNo; }
	public	String	getRequiresProdAssociation ()	{ return m_sRequiresProdAssociation; }
	public	void	setRequiresProdAssociation (String sRequiresProdAssociation) { m_sRequiresProdAssociation = sRequiresProdAssociation; }
	public	String	getTransactionalLineId ()	{ return m_sTransactionalLineId; }
	public	void	setTransactionalLineId (String sTransactionalLineId) { m_sTransactionalLineId = sTransactionalLineId; }
	public	String	getRequestedDate() { return m_sRequestedDate; }
	public	void	setRequestedDate (String sRequestedDate) { m_sRequestedDate = sRequestedDate; }
	
	// read-only getters
	public	boolean			getIsService () { return (getItemGroupCode().equals("PS") || getItemGroupCode().equals("DS")); }
	public	boolean			getIsDeliveryService () { return m_sItemGroupCode.equals("DS"); }
	public	boolean			getIsProductService () { return m_sItemGroupCode.equals("PS"); }
	public	POSCart	getPOSCart () { return m_oPOSCart; }
				
	public void	Reset ()
	{
		m_sItemKey = "";
		m_sOrderLineKey = "";
		m_sLineType = "";
		m_sOrgCode = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sUOM = "EA";
		m_sItemShortDesc = "";
		m_sQty = "0";
		m_sSvcQty = "0";
		m_sPrice = "0.00";
		m_sUnitPrice = "0.00";
		m_sTotalPrice = "0.00";
		m_sShipping = "0.00";
		m_sDiscount = "0.00";
		m_bIsKit = false;
		m_bHasServices = false;
		m_sTransactionalLineId = "";
		m_sPrimeLineNo = "";
		m_sRequestedDate = "";
		m_sRequiresProdAssociation = "N";

		for (int iComponent = 0; iComponent < getComponentCount(); iComponent++)
			getComponent (iComponent).Reset();
			
		m_oPOSCart = null;		
		m_vecComponents.clear();
	}
	
	public	void loadShippingAndDiscountsForItem() throws Exception
	{
		return;
	}

	@SuppressWarnings({ "unused", "rawtypes", "deprecation" })
	public String loadItem() throws Exception
	{
		// load pricing, discount and shipping charges for package item
		loadShippingAndDiscountsForItem();
		
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable htItem = new Hashtable();
		YFCDocument	docItem = YFCDocument.getDocumentFor ("Item");
		YFCElement	eleItem = docItem.getDocumentElement ();
		
		eleItem.setAttribute ("ItemID", getItemID());
		eleItem.setAttribute ("UnitOfMeasure" , getUOM());
		eleItem.setAttribute ("ProductClass", getProductClass());
		eleItem.setAttribute ("OrganizationCode", getPOSCart().getOrganizationCode());

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getItemDeatils() for Item");
			System.out.println (docItem.getString ());
		}

		// call get item details API for package item
		docItem = YFCDocument.getDocumentFor (api.getItemDetails (env, docItem.getDocument ()));
		eleItem = docItem.getDocumentElement ();
		
		String	sItemDetails = docItem.getString();

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getItemDetails() for Item");
			System.out.println (sItemDetails);
		}
		
		// set item properties into cart item
		YFCElement	elePrimaryInformation = eleItem.getChildElement ("PrimaryInformation");
		setItemKey (eleItem.getAttribute ("ItemKey"));
		setItemGroupCode (eleItem.getAttribute ("ItemGroupCode"));
		setOrgCode (eleItem.getAttribute ("OrganizationCode"));
		setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));

		// if caller is relying on Yantra to do pricing of lines		
		if (!getPOSCart().getIsLinesPriced())
		{
			// now get the price from the item
			YFCDocument	docComputePriceForItem = YFCDocument.createDocument ("ComputePriceForItem");
			YFCElement	eleComputePriceForItem = docComputePriceForItem.getDocumentElement ();
			
			eleComputePriceForItem.setAttribute ("OrganizationCode", getPOSCart().getOrganizationCode());
			eleComputePriceForItem.setAttribute ("Currency", YFCLocale.getDefaultLocale ().getCurrency());
			eleComputePriceForItem.setAttribute ("PriceProgramName", getPOSCart().getPriceProgram ());
			eleComputePriceForItem.setAttribute ("PricingDate", getRequestedDate());
			eleComputePriceForItem.setAttribute ("ItemID", getItemID());
			eleComputePriceForItem.setAttribute ("ItemGroupCode", getItemGroupCode());
			eleComputePriceForItem.setAttribute ("Uom", getUOM());
		
			if (getIsService())
			{
				if (getSvcQty().length() == 0 || getSvcQty().equals("0"))
					setSvcQty (getQty());
				eleComputePriceForItem.setAttribute ("Quantity", getSvcQty());
			}
			else
			{
				eleComputePriceForItem.setAttribute ("ProductClass", getProductClass());
				eleComputePriceForItem.setAttribute ("Quantity", getQty());
			}		
		
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to computePriceForItem() for Item");
				System.out.println (docComputePriceForItem.getString());
			}

			docComputePriceForItem = YFCDocument.getDocumentFor (api.computePriceForItem (env, docComputePriceForItem.getDocument()));		
			eleComputePriceForItem = docComputePriceForItem.getDocumentElement ();
					
			// parse output and add price to item
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from computePriceForItem() for Item");
				System.out.println (docComputePriceForItem.getString());
			}
		
			setUnitPrice (eleComputePriceForItem.getAttribute ("UnitPrice"));
			BigDecimal	bdPrice = new BigDecimal (getUnitPrice());
			bdPrice.multiply (new BigDecimal(getIsService () ? getSvcQty() : getQty()));
			bdPrice.setScale (2);
			setPrice (bdPrice.toString());
		}
		loadComponents (eleItem);
		return sItemDetails;
	}
		
	@SuppressWarnings("rawtypes")
	private	void	loadComponents (YFCElement eleItemDetails) throws Exception
	{
		int	iComponentsCount = 0;
		int iServicesCount = 0;
		
		// now parse through the XML output document and load Yantra Order
		YFCElement eleComponents = eleItemDetails.getChildElement ("Components");
		
		// assume we don't have a kit
		setIsKit (false);

		// assume we have no associated services for this item
		setHasServices (false);

		// if we have kit components
		if (eleComponents != null)
		{				
			Iterator	iComponents = eleComponents.getChildren ();

			// if kit items exist
			if (iComponents != null && iComponents.hasNext())
			{
				// set kit flag
				setIsKit (true);
								
				// iterate over components
				while (iComponents.hasNext())
				{
					// get the first/next component from output XML
					YFCElement eleComponent = (YFCElement)iComponents.next ();
			
					// create order line saving relevant details
					POSCartItem.Component	oscComponent = createNewComponent ();
					oscComponent.setItemKey (eleComponent.getAttribute ("ComponentItemKey"));
					oscComponent.setItemID (eleComponent.getAttribute ("ComponentItemId"));
					oscComponent.setOrgCode (eleComponent.getAttribute ("ComponentOrganizationCode"));
					oscComponent.setQty (eleComponent.getAttribute ("KitQuantity"));
					oscComponent.setUOM (eleComponent.getAttribute ("ComponentUnitOfMeasure"));
					oscComponent.loadComponentDetails ();
					addComponent (oscComponent);
					iComponentsCount++;
				}
			}
		}
		if (YFSUtil.getDebug())
		{
			if (getIsKit())
				System.out.println ("Loaded "+iComponentsCount+" Components for Item "+getItemID());
			else
				System.out.println ("No Components Configured for Item "+getItemID());
		}
		
		// now look for associated service/delivery components
		YFCElement	eleServices = eleItemDetails.getChildElement ("ItemServiceAssocList");
		
		
		// if the associated service list is not null
		if (eleServices != null)
		{
			Iterator iServices = eleServices.getChildren ();
		
			if (iServices != null && iServices.hasNext())
			{
				setHasServices (true);
								
				// if at least one order line
				while (iServices.hasNext())
				{
					// get the first/next order line from output XML
					YFCElement eleService = (YFCElement)iServices.next ();
			
					// create order line saving relevant details
					POSCartItem.Component	oscService = createNewComponent ();
					oscService.setItemID (eleService.getAttribute ("ServiceItemId"));
					oscService.setOrgCode (eleService.getAttribute ("ServiceOrganizationCode"));
					oscService.setQty (eleService.getAttribute ("ProductQuantity"));
					oscService.setSvcQty (eleService.getAttribute ("ServiceQuantity"));
					oscService.setUOM (eleService.getAttribute ("ServiceUOM"));
					oscService.loadComponentDetails ();
					addComponent (oscService);
					iServicesCount++;
				}
			}
		}
		if (YFSUtil.getDebug())
		{
			if (getHasServices())
				System.out.println ("Loaded "+iServicesCount + " Services for Item "+getItemID());
			else
				System.out.println ("No Associated Services Configured for Item "+getItemID());
		}
	}
	
	public	Component createNewComponent ()
	{
		return new Component (this);
	}

	@SuppressWarnings("unchecked")
	public	void	addComponent (Component oComponent)
	{
		m_vecComponents.add (oComponent);
		if (oComponent.getIsService())
		{
			setHasServices(true);
			if (YFSUtil.getDebug())
				System.out.println ("Added Service Component to Item: "+getItemID());
		}
		else
		{
			if (YFSUtil.getDebug())
				System.out.println ("Added Product Component to Item: "+getItemID());
		}
	}
		
	public	int			getComponentCount () { return m_vecComponents.size(); }
	public	Component	getComponent (int iComponent) { return (Component)m_vecComponents.elementAt (iComponent); }

	public	class	Component implements Serializable
	{
		public	Component (POSCartItem oPOSCartItem)
		{
			m_oPOSCartItem = oPOSCartItem;
			m_sSvcQty = "0";
			m_sItemKey = "";
			m_sOrderLineKey = "";
			m_sLineType = "";
			m_sOrgCode = "";
			m_sItemID = "";
			m_sProductClass = "";
			m_sItemGroupCode = "";
			m_sUOM = "";
			m_sItemShortDesc = "";
			m_sQty = "0";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
			m_sRequestedDate = "";
			m_sPrimeLineNo = "";
			m_sTransactionalLineId = "";
			m_sRequiresProdAssociation = "N";
		}

		public	void	Reset ()
		{
			m_sSvcQty = "0";
			m_sItemKey = "";
			m_sOrderLineKey = "";
			m_sLineType = "";
			m_sOrgCode = "";
			m_sItemID = "";
			m_sProductClass = "";
			m_sItemGroupCode = "";
			m_sUOM = "";
			m_sItemShortDesc = "";
			m_sQty = "0";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
			m_sRequestedDate = "";
			m_sPrimeLineNo = "";
			m_sTransactionalLineId = "";
			m_sRequiresProdAssociation = "N";
			m_oPOSCartItem = null;
			if (m_oComponentSchedule != null)
				m_oComponentSchedule.Reset();
			m_oComponentSchedule = null;
		}

		private void loadComponentDetails() throws Exception
		{
			return;
/*		
			YFSEnvironment env = YFSUtil.getYFSEnv();
			YIFApi api = YFSUtil.getYIFApi();

			// get component's details (description and price)
			Hashtable htItem = new Hashtable();
			if (getItemKey().length() == 0)
			{
				htItem.put ("ItemID", getItemID());
				htItem.put ("OrganizationCode", getPOSCartItem().getPOSCart().getOrganizationCode());
				htItem.put ("UnitOfMeasure", getUOM());
			}
			else
			{
				htItem.put("ItemKey", getItemKey());
			}

			// prepare to call getItemDetails()
			YFSXMLParser inXml = new YFSXMLParser();
			inXml.createRootElement("Item", htItem);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getItemDetails() for Component");
				System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
			}
			Document docItemDetails = api.getItemDetails (env, inXml.getDocument());
		
			// now parse through the XML output document and load description
			String	sItemDetails = YFSXMLUtil.getXMLString (docItemDetails);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getItemDetails() for Component");
				System.out.println (sItemDetails);
			}

			YCMItemDetailsOutputDoc  oItemDetails = new YCMItemDetailsOutputDoc (sItemDetails);
			setItemGroupCode (oItemDetails.getItem().getAttribute ("ItemGroupCode"));
			setItemKey (oItemDetails.getItem().getAttribute ("ItemKey"));
			setItemID (oItemDetails.getItem().getAttribute ("ItemID"));
			setUOM (oItemDetails.getItem().getAttribute ("UnitOfMeasure"));
			setItemShortDesc (oItemDetails.getItem().getPrimaryInformation ().getAttribute ("ShortDescription"));
			setRequiresProdAssociation(oItemDetails.getItem().getPrimaryInformation().getAttribute("RequiresProdAssociation"));

			// if it's not a service		
			if (!getIsService())
				setProductClass (oItemDetails.getItem().getPrimaryInformation().getAttribute ("DefaultProductClass"));
		
			// if caller is relying on Yantra to do pricing of lines		
			if (!getPOSCartItem().getPOSCart().getIsLinesPriced())
			{
				// now get the price from the item
				Hashtable	htComputePriceForItem = new Hashtable();
				htComputePriceForItem.put ("OrganizationCode", getPOSCartItem().getPOSCart().getOrganizationCode());
				htComputePriceForItem.put ("Currency", YFCLocale.getDefaultLocale().getCurrency());
				htComputePriceForItem.put ("PriceProgramName", getPOSCartItem().getPOSCart().getPriceProgram ());
				htComputePriceForItem.put ("PricingDate", getRequestedDate());
				htComputePriceForItem.put ("ItemID", getItemID());
				htComputePriceForItem.put ("ItemGroupCode", getItemGroupCode());
				htComputePriceForItem.put ("Uom", getUOM());
				if (getIsService())
				{
					if (getSvcQty().length() == 0 || getSvcQty().equals("0"))
						setSvcQty (getQty());
					htComputePriceForItem.put ("Quantity", getSvcQty());
				}
				else
				{
					htComputePriceForItem.put ("ProductClass", getProductClass());
					htComputePriceForItem.put ("Quantity", getQty());
				}		
				// call Compute Price for Item API
				inXml = new YFSXMLParser();
				Element	eleComputePriceForItem = inXml.createRootElement("ComputePriceForItem", htComputePriceForItem);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to computePriceForItem() for Component");
					System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
				}
				Document docComputePriceForItem = api.computePriceForItem (env, inXml.getDocument());		

				// parse output and add price to item
				String	sComputePriceForItem = YFSXMLUtil.getXMLString (docComputePriceForItem);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from computePriceForItem() for Component");
					System.out.println (sComputePriceForItem);
				}
				YFSComputePriceForItemOutputDoc	oComputePriceForItem = new YFSComputePriceForItemOutputDoc(sComputePriceForItem);		
				setUnitPrice (oComputePriceForItem.getComputePriceForItem().getAttribute ("UnitPrice"));		
				BigDecimal	bdPrice = new BigDecimal (getUnitPrice());
				bdPrice.multiply (new BigDecimal(getIsService () ? getSvcQty() : getQty()));
				bdPrice.setScale (2);
				setPrice (bdPrice.toString());
			}
*/
		}

		public	ComponentSchedule createNewComponentSchedule ()
		{
			return (m_oComponentSchedule = new ComponentSchedule (this));
		}
		
		public	String				getOrderLineKey () { return m_sOrderLineKey; }
		public	void				setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
		public	String				getLineType () { return m_sLineType; }
		public	void				setLineType (String sLineType) { m_sLineType = sLineType; }
		public	String				getItemKey () { return m_sItemKey; }
		public	void				setItemKey (String sItemKey) { m_sItemKey = sItemKey; }
		public	String				getOrgCode () { return m_sOrgCode; }
		public	void				setOrgCode (String sOrgCode) { m_sOrgCode = sOrgCode; }
		public	String				getItemID () { return m_sItemID; }
		public	void				setItemID (String sItemID) { m_sItemID = sItemID; }
		public	String				getProductClass () { return m_sProductClass; }
		public	void				setProductClass (String sProductClass) { m_sProductClass = sProductClass; }
		public	String				getItemGroupCode () { return m_sItemGroupCode; }
		public	void				setItemGroupCode (String sItemGroupCode) { m_sItemGroupCode = sItemGroupCode; }
		public	String				getQty () { return m_sQty; }
		public	void				setQty (String sQty) { m_sQty = sQty; }
		public	String				getSvcQty () { return m_sSvcQty; }
		public	void				setSvcQty (String sSvcQty) { m_sSvcQty = sSvcQty; }
		public	String				getUOM () { if (m_sUOM != null) return m_sUOM; else return new String(); }
		public	void				setUOM (String sUOM) { if (sUOM !=null ) m_sUOM = sUOM; }
		public	String				getItemShortDesc ()	{ return m_sItemShortDesc; }
		public	void				setItemShortDesc (String sItemShortDesc) { m_sItemShortDesc = sItemShortDesc; }
		public	String				getPrice () { return m_sPrice; }
		public	void				setPrice (String sPrice) { m_sPrice = sPrice; }
		public	String				getUnitPrice () { return m_sUnitPrice; }
		public	void				setUnitPrice (String sUnitPrice) { m_sUnitPrice = sUnitPrice; }
		public	String				getPrimeLineNo ()	{ return m_sPrimeLineNo; }
		public	void				setPrimeLineNo (String sPrimeLineNo) { m_sPrimeLineNo = sPrimeLineNo; }
		public	String				getTransactionalLineId ()	{ return m_sTransactionalLineId; }
		public	void				setTransactionalLineId (String sTransactionalLineId) { m_sTransactionalLineId = sTransactionalLineId; }
		public	POSCartItem	getPOSCartItem () { return m_oPOSCartItem; }
		public	void				setPOSCartItem (POSCartItem oItem) { m_oPOSCartItem = oItem; }
		public	String				getRequestedDate() { return m_sRequestedDate; }
		public	void				setRequestedDate (String sRequestedDate) { m_sRequestedDate = sRequestedDate; }
		public	String				getRequiresProdAssociation ()	{ return m_sRequiresProdAssociation; }
		public	void				setRequiresProdAssociation (String sRequiresProdAssociation) { m_sRequiresProdAssociation = sRequiresProdAssociation; }

		// read-only getters
		public	ComponentSchedule	getComponentSchedule () { return m_oComponentSchedule; }
		public	boolean				getIsService () { return (getItemGroupCode().equals("PS") || getItemGroupCode().equals("DS")); }
		public	boolean				getIsDeliveryService () { return m_sItemGroupCode.equals("DS"); }
		public	boolean				getIsProductService () { return m_sItemGroupCode.equals("PS"); }
		public	boolean				getIsAssociatedToProduct () { return getRequiresProdAssociation().equalsIgnoreCase ("Y"); }		
		
		protected	String				m_sOrderLineKey;
		protected	String				m_sLineType;
		protected	String				m_sItemKey;
		protected	String				m_sOrgCode;
		protected	String				m_sItemID;
		protected	String				m_sProductClass;
		protected	String				m_sItemGroupCode;
		protected	String				m_sUOM;
		protected	String				m_sItemShortDesc;
		protected	String				m_sQty;	
		protected	String				m_sSvcQty;	
		protected	String				m_sPrice;
		protected	String				m_sUnitPrice;
		protected	String				m_sTransactionalLineId;
		protected	String				m_sPrimeLineNo;
		protected	String				m_sRequiresProdAssociation;
		protected	String				m_sRequestedDate;
		protected	POSCartItem	m_oPOSCartItem;
		protected	ComponentSchedule	m_oComponentSchedule;
	}	


	public	class ComponentSchedule implements Serializable
	{
		public	static	final	int	SLOT_AVAILABLE	 = 0;
		public	static	final	int	SLOT_UNAVAILABLE = 1;
		public	static	final	int	SLOT_NONWORKINGDAY = 2;
		
		@SuppressWarnings("rawtypes")
		public		ComponentSchedule (Component oComponent)
		{
			m_oComponent = oComponent;
			m_iSlotRows = 0;
			m_iSlotCols = 0;
			m_iSlotRowSelected = -1;
			m_iSlotColSelected = -1;
			m_vecConstraints = new Vector();
			m_vecNonWorkingDays = new Vector();
			m_vecSlotRowTitles = new Vector();
			m_vecSlotColTitles = new Vector();
		}	

		public	void	Reset ()
		{
			m_oComponent = null;
			m_iSlotRows = 0;
			m_iSlotCols = 0;
			m_iSlotRowSelected = -1;
			m_iSlotColSelected = -1;
			m_vecConstraints.clear();
			m_vecSlotRowTitles.clear();
			m_vecSlotColTitles.clear();
			m_vecNonWorkingDays.clear();
			for (int iRow = 0; iRow < m_iSlotRows; iRow++)
			{
				m_vecAvailableDays[iRow].clear();
				for (int iCol = 0; iCol < m_iSlotCols; iCol++)
				{
					m_dtSlotBegDateTime[iRow][iCol] = null;
					m_dtSlotEndDateTime[iRow][iCol] = null;
				}
			}
		}
		
		public		Document		getPossibleSchedules (String sHorizonDays) throws Exception
		{
			POSCart	oPOSCart = getComponent().getPOSCartItem().getPOSCart();
			
			// get component's details (description and price)
			YFCDocument	docPromise = YFCDocument.createDocument ("Promise");
			YFCElement	elePromise = docPromise.getDocumentElement ();
						
			elePromise.setAttribute ("OrderHeaderKey", oPOSCart.getOrderHeaderKey());
			elePromise.setAttribute ("OrderLineKey", getComponent().getOrderLineKey());
			elePromise.setAttribute ("ReturnMultipleSrvcSlots", "Y");
			elePromise.setAttribute ("DelayWindow", sHorizonDays);
			elePromise.setAttribute ("MaximumRecords", "1");
			elePromise.setAttribute ("IgnoreMinNotificationTime", "Y");
			elePromise.setAttribute ("Mode", "Inquire");
			elePromise.setAttribute ("CheckInventory", "Y");
			
			// prepare to call getItemDetails()
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getPossibleSchedules() for Component");
				System.out.println (docPromise.getString());
			}
			return (docPromise.getDocument());

		}

		// call with document returned from getPossibleSchedules() service
		@SuppressWarnings({ "rawtypes", "deprecation", "unused" })
		public void	getPossibleSchedules (Document docIn) throws Exception
		{	
			YFCDocument	docPromise = YFCDocument.getDocumentFor (docIn);
			YFCElement	elePromise = docPromise.getDocumentElement ();
			YFCElement	eleSuggestedOption = elePromise.getChildElement ("SuggestedOption");
			YFCElement	eleOption = eleSuggestedOption.getChildElement ("Option");
			YFCElement	elePromiseServiceLines = eleOption.getChildElement ("PromiseServiceLines");
			YFCElement	eleInteractions = eleOption.getChildElement ("Interactions");
			Iterator	iPromiseServiceLines = elePromiseServiceLines.getChildren ();
			Iterator	iInteractions = eleInteractions.getChildren ();
			
			
			if (iInteractions.hasNext())
			{
				YFCElement eleInteraction = (YFCElement)iInteractions.next ();
				YFCElement eleNonWorkingDays = eleInteraction.getChildElement ("NonWorkingDays");
				
				if (eleNonWorkingDays != null)
				{
					buildNonWorkingDays (eleNonWorkingDays.getChildren());
				}
			}
			
			YFCDate	dtFirstDate = null;
			YFCDate	dtLastDate = null;
			YFCDate	dtFirstPossibleDate = null;
			YFCDate	dtAssignedDate = null;

			// iterate over all promise service lines (s/b just one)
			while (iPromiseServiceLines.hasNext ())
			{
				YFCElement elePromiseServiceLine = (YFCElement)iPromiseServiceLines.next ();

				// if promise line's order line key matches components order line key
				if (elePromiseServiceLine.getAttribute ("OrderLineKey").equals (getComponent().getOrderLineKey()))
				{
					// get first and last possible dates				
					dtFirstDate = YFCDate.getYFCDate (elePromiseServiceLine.getAttribute ("DeliveryStartSearchDate"));
					dtLastDate = YFCDate.getYFCDate (elePromiseServiceLine.getAttribute ("DeliveryEndSearchDate"));
					
					// get assigned dates
					YFCElement	eleAssignments = elePromiseServiceLine.getChildElement ("Assignments");
					
					Iterator iAssignments = eleAssignments.getChildren ();

					// if date is assigned get that date
					if (iAssignments.hasNext())
					{
						YFCElement eleAssignment = (YFCElement)iAssignments.next ();
						dtAssignedDate = YFCDate.getYFCDate(eleAssignment.getAttribute ("ApptDate"));
					}
					else
						dtAssignedDate = dtFirstDate;
					YFCElement	eleSlots = elePromiseServiceLine.getChildElement ("Slots");
					Iterator iSlots = eleSlots.getChildren();
					buildSlotGrid (iSlots, dtFirstDate, dtLastDate, dtAssignedDate);
					break;
				}
			}		
		}

		public	boolean selectFirstAvailableSlot ()
		{
			// select first available slot
			int	iRowSelected = -1;
			int iColSelected = -1;
			for (int iCol = 0; iCol < getSlotColCount(); iCol++)
			{
				for (int iRow = 0; iRow < getSlotRowCount(); iRow++)
				{
					if (getSlotAvailability (iRow, iCol) == SLOT_AVAILABLE)
					{
						iRowSelected = iRow;
						iColSelected = iCol;
					}
				}
				if (iRowSelected >=0 || iColSelected >= 0)
					break;
			}
			if (iRowSelected != -1)
			{
				setSlotSelected (iRowSelected, iColSelected);
				return true;
			}
			return false;
		}
		
		@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
		protected	void	buildNonWorkingDays (Iterator iNonWorkingDays)
		{
			while (iNonWorkingDays.hasNext ())
			{
				YFCElement eleNonWorkingDay = (YFCElement)iNonWorkingDays.next ();
				YFCDate	dtNonWorkingDay = YFCDate.getYFCDate (eleNonWorkingDay.getAttribute ("Date"));				
				dtNonWorkingDay.removeTimeComponent();
				m_vecNonWorkingDays.add (dtNonWorkingDay);
			}
			if (YFSUtil.getDebug())
				System.out.println ("Non-Working Days Count="+getNonWorkingDaysCount());
			
		}
				
		@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
		protected	void	buildSlotGrid (Iterator iSlots, YFCDate dtFirstDate, YFCDate dtLastDate, YFCDate dtAssignedDate)
		{
			int	iDays;
			int	iRow, iCol;
			iRow = 0;
			Vector	vecSlots = new Vector();
			
			// store slots in vector
			while (iSlots.hasNext())
			{
				YFCElement eleSlot = (YFCElement)iSlots.next();
				vecSlots.add (eleSlot);
			}
			m_iSlotRows = vecSlots.size();

			// build row titles
			for (iRow = 0; iRow < m_iSlotRows; iRow++)
			{
				YFCElement eleSlot = (YFCElement)vecSlots.elementAt (iRow);
				m_vecSlotRowTitles.add (eleSlot.getAttribute ("ServiceSlotDesc")+"-("+eleSlot.getAttribute ("StartTime")+"-"+eleSlot.getAttribute ("EndTime")+")");					
			}
			// get first slot date
			YFCDate	dtSlotDate = dtFirstDate;				
			
			// get the number of days and extend for each non working day
			iDays  = dtFirstDate.diffDays (dtLastDate);
			iDays += getNonWorkingDaysCount();
			m_iSlotCols = iDays;
			
			// build column titles
			for (iCol = 0; iCol < m_iSlotCols; iCol++)
			{
				String	sMonth = POSCart.CALENDAR_MONTHS [dtSlotDate.getMonth()];
				String	sDay   = Integer.toString (dtSlotDate.getDayOfMonth());
				// make columns equal length if
				if (dtSlotDate.getDayOfMonth() < 10)
					sDay = "0"+sDay;
				m_vecSlotColTitles.add (sMonth+" "+sDay);				
				dtSlotDate = dtSlotDate.getNewDate (1);
			}
			
			// capture available date/time for each slot
			m_vecAvailableDays = new Vector[m_iSlotRows];			
			for (iRow = 0; iRow < m_iSlotRows; iRow++)
			{
				YFCElement eleSlot = (YFCElement)vecSlots.elementAt (iRow);
				YFCElement eleAvailableDates = eleSlot.getChildElement ("AvailableDates");
				Iterator iAvailableDates = eleAvailableDates.getChildren();
				m_vecAvailableDays[iRow] = new Vector();
				
				while (iAvailableDates.hasNext ())
				{
					YFCElement eleAvailableDate = (YFCElement)iAvailableDates.next ();
										
					// add available date/time to slot
					if (eleAvailableDate.getAttribute ("Confirmed").equals("Y"))
					{
						YFCDate	dtAvailable = YFCDate.getYFCDate (eleAvailableDate.getAttribute ("Date")+"T"+eleSlot.getAttribute ("StartTime"));
						m_vecAvailableDays[iRow].add (dtAvailable);
					}
				}
			}
			
			// now build availability grid
			m_dtSlotBegDateTime = new YFCDate [m_iSlotRows][m_iSlotCols];
			m_dtSlotEndDateTime = new YFCDate [m_iSlotRows][m_iSlotCols];
			m_iSlotAvailability = new int [m_iSlotRows][m_iSlotCols];
			
			for (iRow = 0; iRow < m_iSlotRows; iRow++)
			{
				YFCElement eleSlot = (YFCElement)vecSlots.elementAt (iRow);
				dtSlotDate = dtFirstDate;
				
				for (iCol = 0; iCol < m_iSlotCols; iCol++)
				{
					String	sSlotBegTime = eleSlot.getAttribute ("StartTime");
					String	sSlotEndTime = eleSlot.getAttribute ("EndTime");					
					YFCDate	dtApptBegDateTime = YFCDate.getYFCDate(dtSlotDate.getString (YFCDate.ISO_DATETIME_FORMAT).substring (0,10)+"T"+sSlotBegTime);
					YFCDate	dtApptEndDateTime = YFCDate.getYFCDate(dtSlotDate.getString (YFCDate.ISO_DATETIME_FORMAT).substring (0,10)+"T"+sSlotEndTime);

					// add to array
					m_dtSlotBegDateTime[iRow][iCol] = new YFCDate (dtApptBegDateTime);
					m_dtSlotEndDateTime[iRow][iCol] = new YFCDate (dtApptEndDateTime);
					m_iSlotAvailability[iRow][iCol] = getSlotAvailability (dtApptBegDateTime);
					dtSlotDate = dtSlotDate.getNewDate (1);
					if (YFSUtil.getDebug())
						System.out.println ("Slot "+iRow+","+iCol+" StartTime="+m_dtSlotBegDateTime[iRow][iCol].getString (YFCDate.ISO_DATETIME_FORMAT)+" EndTime="+m_dtSlotEndDateTime[iRow][iCol].getString (YFCDate.ISO_DATETIME_FORMAT)+" Available="+m_iSlotAvailability[iRow][iCol]);
				}
			}
			// clear out vector containing slots
			vecSlots.clear();				
		}
		
		@SuppressWarnings("deprecation")
		protected	int	getSlotAvailability (YFCDate dtDate)
		{
			YFCDate dtCompareDate1 = new YFCDate (dtDate);
			
			for (int iSlot = 0; iSlot < m_vecAvailableDays.length; iSlot++)
			{
				for (int iDate = 0; iDate < m_vecAvailableDays[iSlot].size(); iDate++)
				{
					YFCDate	dtCompareDate2 = (YFCDate)m_vecAvailableDays[iSlot].elementAt (iDate);
					if (dtCompareDate1.equals (dtCompareDate2))
						return SLOT_AVAILABLE;
				}
				
			}
			dtCompareDate1.removeTimeComponent ();
			for (int iDay = 0; iDay < m_vecNonWorkingDays.size(); iDay++)
			{
				YFCDate	dtCompareDate2 = (YFCDate)m_vecNonWorkingDays.elementAt (iDay);
				if (dtCompareDate1.equals (dtCompareDate2))
					return SLOT_NONWORKINGDAY;
			}
			return SLOT_UNAVAILABLE;
		}
		
		// read only getter's
		public		String		getSlotRowTitle (int iRow) { return (String)m_vecSlotRowTitles.elementAt (iRow); }
		public		String		getSlotColTitle (int iCol) { return (String)m_vecSlotColTitles.elementAt (iCol); }
		public		int			getSlotRowCount () { return m_iSlotRows; }
		public		int			getSlotColCount() { return m_iSlotCols; }
		public		int			getSlotAvailability (int iRow, int iCol) { return m_iSlotAvailability[iRow][iCol]; }		
		@SuppressWarnings("deprecation")
		public		YFCDate		getSlotBegDateTime (int iRow, int iCol) { return m_dtSlotBegDateTime[iRow][iCol]; }
		@SuppressWarnings("deprecation")
		public		YFCDate		getSlotEndDateTime (int iRow, int iCol) { return m_dtSlotEndDateTime[iRow][iCol]; }
		public		Component	getComponent () { return m_oComponent; }
		public		void		setSlotSelected (int iRow, int iCol) { m_iSlotRowSelected = iRow; m_iSlotColSelected = iCol; }

		// selection
		public		int			getSlotRowSelected () { return m_iSlotRowSelected; }
		public		int			getSlotColSelected() { return m_iSlotColSelected; }
		@SuppressWarnings("deprecation")
		public		YFCDate		getSlotBegSelected() { return getSlotBegDateTime (m_iSlotRowSelected, m_iSlotColSelected); }
		@SuppressWarnings("deprecation")
		public		YFCDate		getSlotEndSelected() { return getSlotEndDateTime (m_iSlotRowSelected, m_iSlotColSelected); }
		public		int			getSlotAvailSelected() { return getSlotAvailability (m_iSlotRowSelected, m_iSlotColSelected); }

		// protected methods
		protected	int			getNonWorkingDaysCount () { return m_vecNonWorkingDays.size(); }
		@SuppressWarnings("deprecation")
		protected	YFCDate		getNonWorkingDay (int iDay) { return (YFCDate)m_vecNonWorkingDays.elementAt (iDay); }
		protected	int			getAvailableDaysCount (int iSlot) { return m_vecAvailableDays[iSlot].size(); }
		@SuppressWarnings("deprecation")
		protected	YFCDate		getAvailableDay (int iSlot, int iDay) { return (YFCDate)m_vecAvailableDays[iSlot].elementAt (iDay); }

		// protected member variables			
		protected	Component	m_oComponent;
		@SuppressWarnings("rawtypes")
		protected	Vector		m_vecConstraints;
		@SuppressWarnings("rawtypes")
		protected	Vector		m_vecSlotRowTitles;
		@SuppressWarnings("rawtypes")
		protected	Vector		m_vecSlotColTitles;
		@SuppressWarnings("rawtypes")
		protected	Vector		m_vecNonWorkingDays;
		@SuppressWarnings("rawtypes")
		protected	Vector		m_vecAvailableDays[];
		protected	int			m_iSlotRows;
		protected	int			m_iSlotCols;
		protected	int			m_iSlotRowSelected;
		protected	int			m_iSlotColSelected;
		protected	int			m_iSlotAvailability[][];
		@SuppressWarnings("deprecation")
		protected	YFCDate		m_dtSlotBegDateTime[][];
		@SuppressWarnings("deprecation")
		protected	YFCDate		m_dtSlotEndDateTime[][];
	}
	
	// protected member variables
	private		boolean	m_bIsKit;
	private		boolean	m_bHasServices;
	protected	String	m_sOrderLineKey;
	protected	String	m_sLineType;
	protected	String	m_sItemKey;
	protected	String	m_sOrgCode;
	protected	String	m_sOEM;
	protected	String	m_sItemID;
	protected	String	m_sProductClass;
	protected	String	m_sItemGroupCode;
	protected	String	m_sUOM;
	protected	String	m_sItemShortDesc;
	protected	String	m_sQty;	
	protected	String	m_sSvcQty;	
	protected	String	m_sPrice;
	protected	String	m_sUnitPrice;
	protected	String	m_sTotalPrice;
	protected	String	m_sShipping;	
	protected	String	m_sDiscount;
	protected	String	m_sTransactionalLineId;
	protected	String	m_sPrimeLineNo;
	protected	String	m_sRequestedDate;
	protected	String	m_sRequiresProdAssociation;

	// protected object member variables
	protected	POSCart	m_oPOSCart;	
	@SuppressWarnings("rawtypes")
	protected	Vector	m_vecComponents;
}

