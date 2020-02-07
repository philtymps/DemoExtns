/**
  * ShoppingCartItem.java
  *
  **/

// PACKAGE
package com.custom.yantra.shoppingcart;

import	com.custom.yantra.util.*;
import	com.yantra.yfs.japi.YFSEnvironment;
import  com.yantra.interop.japi.YIFApi;
import	com.yantra.yfc.util.*;
import	com.yantra.yfc.dom.*;
import	java.util.*;
import	java.math.*;
import  java.io.Serializable;
import	org.w3c.dom.*;

@SuppressWarnings("serial")
public class ShoppingCartItem implements Serializable
{
    public ShoppingCartItem (ShoppingCart oShoppingCart)
    {	
		m_oShoppingCart = oShoppingCart;
		m_sIntegrationID = "";
		m_sItemKey = "";
		m_sOrderLineKey = "";
		m_sLineType = "";
		m_sProductLine = "";
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
		m_sTax = "0.00";
		m_bIsKit = false;
		m_bHasServices = false;
		m_sTransactionalLineId = "";
		m_sParentBundle = "";
		m_sPrimeLineNo = "";
		m_sRequestedDate = "";
		m_sDeliveryDate = "";
		m_sAvailableDate = "";
		m_sRequiresProdAssociation = "N";
		m_vecComponents = new Vector<Component>();
		m_vecOptions = new Vector<Option>();
    }
	
	public	String	getOrderLineKey () { return m_sOrderLineKey; }
	public	void	setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
	public	String	getIntegrationID () { return m_sIntegrationID; }
	public	void	setIntegrationID (String sIntegrationID) { m_sIntegrationID = sIntegrationID; }
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
	public	String	getTax () { return m_sTax; }
	public	void	setTax (String sTax) { m_sTax = sTax; }
	public	boolean	getIsKit () { return m_bIsKit; }
	public	void	setIsKit (boolean bIsKit) { m_bIsKit = bIsKit; }
	public	String	getParentBundle() { return m_sParentBundle; }
	public	void	setParentBundle (String sParentBundle) { m_sParentBundle = sParentBundle; }
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
	public	String	getDeliveryDate() { return m_sDeliveryDate; }
	public	void	setDeliveryDate (String sDeliveryDate) { m_sDeliveryDate = sDeliveryDate; }
	public	String	getAvailableDate() { return m_sAvailableDate; }
	public	void	setAvailableDate (String sAvailableDate) { m_sAvailableDate = sAvailableDate; }
	public	String	getProductLine () { return m_sProductLine; }
	public	void	setProductLine (String sProductLine) { m_sProductLine = sProductLine; }
	
	public	void	calcTotalPrice (boolean bApplyDiscount)
	{
		if (YFSUtil.getDebug())
		{
			System.out.println ("UnitPrice="+getUnitPrice());
			System.out.println ("Quantity="+getQty());
			System.out.println ("Discount="+getDiscount());
		}
		BigDecimal	bdUnitPrice = new BigDecimal (getUnitPrice());
		BigDecimal	bdQty = new BigDecimal (getQty());
		BigDecimal	bdDiscount = new BigDecimal (getDiscount());
		BigDecimal	bdTotal = bdUnitPrice.multiply (bdQty);
		if (bApplyDiscount)
			bdTotal = bdTotal.subtract (bdDiscount).setScale (2);
		setTotalPrice (bdTotal.toString());
	}
	
	// read-only getters
	public	boolean			getIsService () { return (getItemGroupCode().equals("PS") || getItemGroupCode().equals("DS")); }
	public	boolean			getIsDeliveryService () { return m_sItemGroupCode.equals("DS"); }
	public	boolean			getIsProductService () { return m_sItemGroupCode.equals("PS"); }
	public	ShoppingCart	getShoppingCart () { return m_oShoppingCart; }
				
	public void	Reset ()
	{
		m_sItemKey = "";
		m_sOrderLineKey = "";
		m_sIntegrationID = "";
		m_sLineType = "";
		m_sProductLine = "";
		m_sOrgCode = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sUOM = "EA";
		m_sItemShortDesc = "";
		m_sQty = "0";
		m_sSvcQty = "0";
		m_sPrice = "0.00";
		m_sTotalPrice = "0.00";
		m_sShipping = "0.00";
		m_sDiscount = "0.00";
		m_sUnitPrice = "0.00";
		m_sTax = "0.00";
		m_bIsKit = false;
		m_sParentBundle = "";
		m_bHasServices = false;
		m_sTransactionalLineId = "";
		m_sPrimeLineNo = "";
		m_sRequestedDate = "";
		m_sAvailableDate = "";
		m_sDeliveryDate = "";
		m_sRequiresProdAssociation = "N";

		for (int iComponent = 0; iComponent < getComponentCount(); iComponent++)
			getComponent (iComponent).Reset();
		m_vecComponents.clear();			

		for (int iOption = 0; iOption < getOptionCount(); iOption++)
			getOption (iOption).Reset();
		m_vecOptions.clear();
		
		m_oShoppingCart = null;		
	}
	
	public	void loadShippingAndDiscountsForItem() throws Exception
	{
		return;
	}

	public String loadItem() throws Exception
	{
		return loadItem (false);
	}	

	@SuppressWarnings("deprecation")
	public String loadItem(boolean bLoadOptionsFromYantra) throws Exception
	{
		// load pricing, discount and shipping charges for package item
		loadShippingAndDiscountsForItem();
		
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable<String, String> htItem = new Hashtable<String, String>();
		htItem.put("ItemID", getItemID());
		htItem.put("UnitOfMeasure" , getUOM());
		htItem.put("ProductClass", getProductClass());
		htItem.put("OrganizationCode", getShoppingCart().getOrganizationCode());

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Item", htItem);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getItemDeatils() for Item");
			System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
		}

		// call get item details API for package item
		Document docOutXml = api.getItemDetails (env, inXml.getDocument());

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getItemDetails() for Item");
			System.out.println (YFSXMLUtil.getXMLString (docOutXml));
		}
		
		// set item properties into cart item
		YFCDocument	docItemDetails = YFCDocument.getDocumentFor (docOutXml);
		YFCElement	eleItemDetails = docItemDetails.getDocumentElement ();
		YFCElement	elePrimaryInformation = eleItemDetails.getChildElement ("PrimaryInformation");
			
		setItemKey (eleItemDetails.getAttribute ("ItemKey"));
		setItemGroupCode (eleItemDetails.getAttribute ("ItemGroupCode"));
		setOrgCode (eleItemDetails.getAttribute ("OrganizationCode"));
		if (YFSUtil.getDebug())
		{
			System.out.println ("Item Description: "+elePrimaryInformation.getAttribute("ShortDescription"));
		}
		setProductLine (elePrimaryInformation.getAttribute ("ProductLine"));
		setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));
		
		// if caller is relying on Yantra to do pricing of lines		
		if (getShoppingCart().getPriceProgram ().length() > 0 && !getShoppingCart().getIsLinesPriced())
		{
			// now get the price from the item
			Hashtable<String, String>	htComputePriceForItem = new Hashtable<String, String>();
			htComputePriceForItem.put ("OrganizationCode", getShoppingCart().getOrganizationCode());
			htComputePriceForItem.put ("Currency", YFCLocale.getDefaultLocale ().getCurrency());
			htComputePriceForItem.put ("PriceProgramName", getShoppingCart().getPriceProgram ());
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
			inXml.createRootElement("ComputePriceForItem", htComputePriceForItem);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to computePriceForItem() for Item");
				System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
			}

			docOutXml = api.computePriceForItem (env, inXml.getDocument());				

			// parse output and add price to item
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from computePriceForItem() for Item");
				System.out.println (YFSXMLUtil.getXMLString (docOutXml));
			}

			YFCDocument	docComputePriceForItem = YFCDocument.getDocumentFor (docOutXml);
			YFCElement	eleComputePriceForItem = docComputePriceForItem.getDocumentElement();			
			setUnitPrice (eleComputePriceForItem.getAttribute ("UnitPrice"));
			BigDecimal	bdPrice = new BigDecimal (getUnitPrice()).setScale (2);
			BigDecimal	bdQty = new BigDecimal(getIsService () ? getSvcQty() : getQty()).setScale(2);
			bdPrice.multiply (bdQty);
			setPrice (bdPrice.toString());
		}
		loadComponents (docItemDetails, bLoadOptionsFromYantra);
		return docItemDetails.getString();
	}
		
	private	void	loadComponents (YFCDocument docItemDetails, boolean bLoadOptionsFromYantra) throws Exception
	{
		int	iComponents = 0;
		int iServices = 0;
		// now parse through the XML output document and load Yantra Order
		YFCElement	eleItemDetails = docItemDetails.getDocumentElement();
		YFCElement	eleComponents = eleItemDetails.getChildElement ("Components");
		
		// assume we don't have a kit
		setIsKit (false);

		// assume we have no associated services for this item
		setHasServices (false);

		// if we have kit components
		if (eleComponents != null)
		{				
			// if kit items exist
			for (Iterator<?> i = eleComponents.getChildren(); i.hasNext(); )
			{
				// get the first/next component from output XML
				YFCElement eleComponent = (YFCElement)i.next();

				// set kit flag
				setIsKit (true);
							
				// create order line saving relevant details
				ShoppingCartItem.Component	oscComponent = createNewComponent ();
				oscComponent.setItemKey (eleComponent.getAttribute ("ComponentItemKey"));
				oscComponent.setItemID (eleComponent.getAttribute ("ComponentItemId"));
				oscComponent.setOrgCode (eleComponent.getAttribute ("ComponentOrganizationCode"));
				oscComponent.setQty (eleComponent.getAttribute ("KitQuantity"));
				oscComponent.setUOM (eleComponent.getAttribute ("ComponentUnitOfMeasure"));
				oscComponent.loadComponentDetails (bLoadOptionsFromYantra);
				addComponent (oscComponent);
				iComponents++;
			}
		}
		if (YFSUtil.getDebug())
		{
			if (getIsKit())
				System.out.println ("Loaded "+iComponents+" Components for Item "+getItemID());
			else
				System.out.println ("No Components Configured for Item "+getItemID());
		}
		
		// now look for associated service/delivery components
		YFCElement	eleItemServiceAssocs = eleItemDetails.getChildElement ("ItemServiceAssocList");
		
		// if the associated service list is not null
		if (eleItemServiceAssocs != null)
		{
			for (Iterator<?> iItemServiceAssocs = eleItemServiceAssocs.getChildren(); iItemServiceAssocs.hasNext(); )
			{					
				// get the first/next order line from output XML
				YFCElement	eleItemServiceAssoc = (YFCElement)iItemServiceAssocs.next();

				// set has services to true		
				setHasServices (true);

				// create order line saving relevant details
				ShoppingCartItem.Component	oscService = createNewComponent ();
				oscService.setItemID (eleItemServiceAssoc.getAttribute ("ServiceItemId"));
				oscService.setOrgCode (eleItemServiceAssoc.getAttribute ("ServiceOrganizationCode"));
				oscService.setQty (eleItemServiceAssoc.getAttribute ("ProductQuantity"));
				oscService.setSvcQty (eleItemServiceAssoc.getAttribute ("ServiceQuantity"));
				oscService.setUOM (eleItemServiceAssoc.getAttribute ("ServiceUOM"));
				oscService.loadComponentDetails (bLoadOptionsFromYantra);
				addComponent (oscService);
				iServices++;
			}
		}
		if (YFSUtil.getDebug())
		{
			if (getHasServices())
				System.out.println ("Loaded "+iServices+" Services for Item "+getItemID());
			else
				System.out.println ("No Associated Services Configured for Item "+getItemID());
		}
	}
	
	public	Component createNewComponent ()
	{
		return new Component (this);
	}

	public	void	addComponent (Component oComponent)
	{
		m_vecComponents.add (oComponent);
		if (oComponent.getIsService())
		{
			setHasServices(true);
			if (YFSUtil.getDebug())
				System.out.println ("Added Service Component "+oComponent.getItemID() + " to Item: "+getItemID());
		}
		else
		{
			if (YFSUtil.getDebug())
				System.out.println ("Added Kit Component "+oComponent.getItemID() + " to Item: "+getItemID());
		}
	}
	
	public	void	removeComponent (int iComponent)
	{
		m_vecComponents.remove (iComponent);
	}
	
	public	void	addOption (Option oOption)
	{
		m_vecOptions.add (oOption);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Added Option "+oOption.getItemID() + " to Item: "+getItemID());
		}
	}

	public	void	removeOption (int iOption)
	{
		m_vecOptions.remove (iOption);
	}
		
	public	int			getComponentCount () { return m_vecComponents.size(); }
	public	Component	getComponent (int iComponent) { return (Component)m_vecComponents.elementAt (iComponent); }

	public	int			getOptionCount () { return m_vecOptions.size(); }
	public	Option		getOption (int iOption) { return (Option)m_vecOptions.elementAt (iOption); }

	public	class	Component implements Serializable
	{
		public	Component (ShoppingCartItem oShoppingCartItem)
		{
			m_oShoppingCartItem = oShoppingCartItem;
			m_sSvcQty = "0";
			m_sItemKey = "";
			m_sOrderLineKey = "";
			m_sIntegrationID = "";
			m_sLineType = "";
			m_sOrgCode = "";
			m_sItemID = "";
			m_sProductClass = "";
			m_sProductLine = "";
			m_sItemGroupCode = "";
			m_sUOM = "";
			m_sItemShortDesc = "";
			m_sQty = "0";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
			m_sRequestedDate = "";
			m_sDeliveryDate = "";
			m_sAvailableDate = "";
			m_sPrimeLineNo = "";
			m_sTransactionalLineId = "";
			m_sParentBundle = "";
			m_sRequiresProdAssociation = "N";
			m_oComponentSchedule = null;
		}

		public	void	Reset ()
		{
			m_sSvcQty = "0";
			m_sItemKey = "";
			m_sIntegrationID = "";
			m_sOrderLineKey = "";
			m_sLineType = "";
			m_sOrgCode = "";
			m_sItemID = "";
			m_sProductClass = "";
			m_sProductLine = "";
			m_sItemGroupCode = "";
			m_sUOM = "";
			m_sItemShortDesc = "";
			m_sQty = "0";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
			m_sRequestedDate = "";
			m_sDeliveryDate = "";
			m_sAvailableDate = "";
			m_sPrimeLineNo = "";
			m_sTransactionalLineId = "";
			m_sParentBundle = "";
			m_sRequiresProdAssociation = "N";
			m_oShoppingCartItem = null;
			if (m_oComponentSchedule != null)
				m_oComponentSchedule.Reset();
			m_oComponentSchedule = null;
		}
		
		@SuppressWarnings("deprecation")
		private void loadComponentDetails(boolean bLoadOptionsForComponent) throws Exception
		{
			YFSEnvironment env = YFSUtil.getYFSEnv();
			YIFApi api = YFSUtil.getYIFApi();
			int iOptions = 0;
			
			// get component's details (description and price)
			Hashtable<String, String> htItem = new Hashtable<String, String>();
			if (getItemKey().length() == 0)
			{
				htItem.put ("ItemID", getItemID());
				htItem.put ("OrganizationCode", getShoppingCartItem().getShoppingCart().getOrganizationCode());
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
			Document docOutXml = api.getItemDetails (env, inXml.getDocument());
		
			// now parse through the XML output document and load description
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getItemDetails() for Component");
				System.out.println (YFSXMLUtil.getXMLString (docOutXml));
			}
			YFCDocument	docItemDetails = YFCDocument.getDocumentFor (docOutXml);
			YFCElement	eleItemDetails = docItemDetails.getDocumentElement ();
			YFCElement	elePrimaryInformation = eleItemDetails.getChildElement ("PrimaryInformation");
			setItemGroupCode (eleItemDetails.getAttribute ("ItemGroupCode"));
			setItemKey (eleItemDetails.getAttribute ("ItemKey"));
			setItemID (eleItemDetails.getAttribute ("ItemID"));
			setUOM (eleItemDetails.getAttribute ("UnitOfMeasure"));
			if (YFSUtil.getDebug())
			{
				System.out.println ("Component's Item Description: "+elePrimaryInformation.getAttribute ("ShortDescription"));
			}
			setProductLine (elePrimaryInformation.getAttribute ("ProductLine"));
			setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));
			setRequiresProdAssociation(elePrimaryInformation.getAttribute("RequiresProdAssociation"));

			// if it's not a service		
			if (!getIsService())
				setProductClass (elePrimaryInformation.getAttribute ("DefaultProductClass"));
		
			// if caller is relying on Yantra to do pricing of lines		
			if (getShoppingCartItem().getShoppingCart().getPriceProgram().length() > 0 
			&& !getShoppingCartItem().getShoppingCart().getIsLinesPriced())
			{
				// now get the price from the item
				Hashtable<String, String>	htComputePriceForItem = new Hashtable<String, String>();
				htComputePriceForItem.put ("OrganizationCode", getShoppingCartItem().getShoppingCart().getOrganizationCode());
				htComputePriceForItem.put ("Currency", YFCLocale.getDefaultLocale().getCurrency());
				htComputePriceForItem.put ("PriceProgramName", getShoppingCartItem().getShoppingCart().getPriceProgram ());
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
				inXml.createRootElement("ComputePriceForItem", htComputePriceForItem);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to computePriceForItem() for Component");
					System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
				}
				docOutXml = api.computePriceForItem (env, inXml.getDocument());		

				// parse output and add price to item
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from computePriceForItem() for Component");
					System.out.println (YFSXMLUtil.getXMLString (docOutXml));
				}
				YFCDocument	docComputePriceForItem = YFCDocument.getDocumentFor (docOutXml);
				YFCElement	eleComputePriceForItem = docComputePriceForItem.getDocumentElement();
				setUnitPrice (eleComputePriceForItem.getAttribute ("UnitPrice"));		
				BigDecimal	bdPrice = new BigDecimal (getUnitPrice()).setScale(2);
				BigDecimal	bdQty = new BigDecimal(getIsService () ? getSvcQty() : getQty()).setScale(2);
				bdPrice.multiply (bdQty);
				setPrice (bdPrice.toString());
			}
			YFCElement	eleItemOptionList = eleItemDetails.getChildElement ("ItemOptionList");
			if (eleItemOptionList != null)
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Options for Component: "+getItemID()+" found.");
				}
				if (bLoadOptionsForComponent)
				{					
					for (Iterator<?> i = eleItemOptionList.getChildren(); i.hasNext(); )
					{
						YFCElement	eleItemOption = (YFCElement)i.next();
						Option oOption = new ShoppingCartItem.Option (getShoppingCartItem(), this);					
						oOption.setItemID (eleItemOption.getAttribute ("OptionItemId"));					
						oOption.setUOM (eleItemOption.getAttribute ("OptionUOM"));					
						oOption.loadOptionDetails ();
						getShoppingCartItem().addOption(oOption);
						iOptions++;
					}
				}
			}
			if (YFSUtil.getDebug())
			{
				if (iOptions > 0)
					System.out.println ("Loaded "+iOptions+" Options for Component "+getItemID());
				else
					System.out.println ("No Options Configured for Component "+getItemID());
			}
		}

		public	ComponentSchedule createNewComponentSchedule ()
		{
			return (m_oComponentSchedule = new ComponentSchedule (this));
		}
		
		public	String				getOrderLineKey () { return m_sOrderLineKey; }
		public	void				setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
		public	String				getIntegrationID () { return m_sIntegrationID; }
		public	void				setIntegrationID (String sIntegrationID) { m_sIntegrationID = sIntegrationID; }
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
		public	String				getParentBundle() { return m_sParentBundle; }
		public	void				setParentBundle (String sParentBundle) { m_sParentBundle = sParentBundle; }
		public	ShoppingCartItem	getShoppingCartItem () { return m_oShoppingCartItem; }
		public	void				setShoppingCartItem (ShoppingCartItem oItem) { m_oShoppingCartItem = oItem; }
		public	String				getRequestedDate() { return m_sRequestedDate; }
		public	void				setRequestedDate (String sRequestedDate) { m_sRequestedDate = sRequestedDate; }
		public	String				getDeliveryDate() { return m_sDeliveryDate; }
		public	void				setDeliveryDate (String sDeliveryDate) { m_sDeliveryDate = sDeliveryDate; }
		public	String				getAvailableDate() { return m_sAvailableDate; }
		public	void				setAvailableDate (String sAvailableDate) { m_sAvailableDate = sAvailableDate; }
		public	String				getRequiresProdAssociation ()	{ return m_sRequiresProdAssociation; }
		public	void				setRequiresProdAssociation (String sRequiresProdAssociation) { m_sRequiresProdAssociation = sRequiresProdAssociation; }
		public	String				getProductLine () { return m_sProductLine; }
		public	void				setProductLine (String sProductLine) { m_sProductLine = sProductLine; }

		// read-only getters
		public	ComponentSchedule	getComponentSchedule () { return m_oComponentSchedule; }
		public	boolean				getIsService () { return (getItemGroupCode().equals("PS") || getItemGroupCode().equals("DS")); }
		public	boolean				getIsDeliveryService () { return m_sItemGroupCode.equals("DS"); }
		public	boolean				getIsProductService () { return m_sItemGroupCode.equals("PS"); }
		public	boolean				getIsAssociatedToProduct () { return m_sRequiresProdAssociation.equalsIgnoreCase("Y"); }		
		public	boolean				getIsOption () { return (boolean)(getItemGroupCode().equals("PSOPT") || getItemGroupCode().equals("DSOPT")); }
		
		protected	String				m_sOrderLineKey;
		protected	String				m_sIntegrationID;
		protected	String				m_sLineType;
		protected	String				m_sItemKey;
		protected	String				m_sOrgCode;
		protected	String				m_sItemID;
		protected	String				m_sProductClass;
		protected	String				m_sProductLine;
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
		protected	ShoppingCartItem	m_oShoppingCartItem;
		protected	ComponentSchedule	m_oComponentSchedule;
	}	

	public	class	Option implements Serializable
	{
		public	Option (ShoppingCartItem oShoppingCartItem, Component oComponent)
		{
			m_oShoppingCartItem = oShoppingCartItem;
			m_oComponent = oComponent;
			m_sOrderLineOptionKey = "";
			m_sIntegrationID = "";
			m_sItemKey = "";
			m_sItemID = "";
			m_sItemGroupCode = "";
			m_sTransactionalLineId = "";
			m_sParentBundle = "";
			m_sUOM = "";
			m_sQty = "1";
			m_sPricingUOM = "";
			m_sItemShortDesc = "";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
		}

		public	void	Reset ()
		{
			m_sOrderLineOptionKey = "";
			m_sIntegrationID = "";
			m_sItemKey = "";
			m_sItemID = "";
			m_sItemGroupCode = "";
			m_sTransactionalLineId = "";
			m_sParentBundle = "";
			m_sUOM = "";
			m_sQty = "1";
			m_sPricingUOM = "";
			m_sItemShortDesc = "";
			m_sPrice = "0.00";
			m_sUnitPrice = "0.00";
		}

		protected	void loadOptionDetails () throws Exception
		{
			YFSEnvironment env = YFSUtil.getYFSEnv();
			YIFApi api = YFSUtil.getYIFApi();

			Hashtable<String, String> htItem = new Hashtable<String, String>();
			htItem.put ("ItemID", getItemID());
			htItem.put ("UnitOfMeasure", getUOM());
			htItem.put ("OrganizationCode", getShoppingCartItem().getShoppingCart().getOrganizationCode());

			// prepare to call getItemDetails()
			YFSXMLParser inXml = new YFSXMLParser();
			inXml.createRootElement("Item", htItem);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getItemDetails() for Option");
				System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
			}
			Document docOutXml = api.getItemDetails (env, inXml.getDocument());
		
			// now parse through the XML output document and load description
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getItemDetails() for Option");
				System.out.println (YFSXMLUtil.getXMLString (docOutXml));
			}
			YFCDocument	docItemDetails = YFCDocument.getDocumentFor (docOutXml);
			YFCElement	eleItemDetails = docItemDetails.getDocumentElement ();
			YFCElement	elePrimaryInformation = eleItemDetails.getChildElement ("PrimaryInformation");
			setItemID (eleItemDetails.getAttribute ("ItemID"));
			setUOM (eleItemDetails.getAttribute ("UnitOfMeasure"));
			setItemGroupCode (eleItemDetails.getAttribute ("ItemGroupCode"));
			setItemShortDesc (elePrimaryInformation.getAttribute ("ShortDescription"));			
			if (YFSUtil.getDebug())
			{
				System.out.println ("Option's Item Description: "+elePrimaryInformation.getAttribute ("ShortDescription"));
			}			
			// TO DO - Pricing for Option(s)
		}

		public	ShoppingCartItem	getShoppingCartItem () { return m_oShoppingCartItem; }
		public	void				setShoppingCartItem (ShoppingCartItem oItem) { m_oShoppingCartItem = oItem; }
		public	Component			getComponent () { return m_oComponent; }
		public	void				setComponent (Component oComponent) { m_oComponent = oComponent; } 
		public	String				getIntegrationID () { return m_sIntegrationID; }
		public	void				setIntegrationID (String sIntegrationID) { m_sIntegrationID = sIntegrationID; }
		public	String				getTransactionalLineId () { return m_sTransactionalLineId; }
		public	void				setParentBundle (String sParentBundle) { m_sParentBundle = sParentBundle; }
		public	String				getParentBundle () { return m_sParentBundle; }
		public	void				setTransactionalLineId (String sTransactionalLineId) { m_sTransactionalLineId = sTransactionalLineId; }
		public	String				getOrderLineOptionKey () { return m_sOrderLineOptionKey; }
		public	void				setOrderLineOptionKey (String sOrderLineOptionKey) { m_sOrderLineOptionKey = sOrderLineOptionKey; }
		public	String				getItemKey () { return m_sItemKey; }
		public	void				setItemKey (String sItemKey) { m_sItemKey = sItemKey; }
		public	String				getItemID () { return m_sItemID; }
		public	void				setItemID (String sItemID) { m_sItemID = sItemID; }
		public	String				getItemGroupCode () { return m_sItemGroupCode; }
		public	void				setItemGroupCode (String sItemGroupCode) { m_sItemGroupCode = sItemGroupCode; }
		public	String				getQty () { return m_sQty; }
		public	void				setQty (String sQty) { m_sQty = sQty; }
		public	String				getUOM () { if (m_sUOM != null) return m_sUOM; else return new String(); }
		public	void				setUOM (String sUOM) { if (sUOM !=null ) m_sUOM = sUOM; }
		public	String				getPricingUOM () { if (m_sPricingUOM != null) return m_sPricingUOM; else return new String(); }
		public	void				setPricingUOM (String sPricingUOM) { if (sPricingUOM !=null ) m_sPricingUOM = sPricingUOM; }
		public	String				getItemShortDesc ()	{ return m_sItemShortDesc; }
		public	void				setItemShortDesc (String sItemShortDesc) { m_sItemShortDesc = sItemShortDesc; }
		public	String				getPrice () { return m_sPrice; }
		public	void				setPrice (String sPrice) { m_sPrice = sPrice; }
		public	String				getUnitPrice () { return m_sUnitPrice; }
		public	void				setUnitPrice (String sUnitPrice) { m_sUnitPrice = sUnitPrice; }

		protected	ShoppingCartItem	m_oShoppingCartItem;
		protected	Component			m_oComponent;
		protected	String				m_sIntegrationID;
		protected	String				m_sTransactionalLineId;
		protected	String				m_sParentBundle;
		protected	String				m_sOrderLineOptionKey;
		protected	String				m_sItemKey;
		protected	String				m_sItemID;
		protected	String				m_sItemGroupCode;
		protected	String				m_sUOM;
		protected	String				m_sQty;
		protected	String				m_sPricingUOM;
		protected	String				m_sItemShortDesc;
		protected	String				m_sPrice;
		protected	String				m_sUnitPrice;
	}

	public	class ComponentSchedule implements Serializable
	{
		public	static	final	int	SLOT_AVAILABLE	 = 0;
		public	static	final	int	SLOT_UNAVAILABLE = 1;
		public	static	final	int	SLOT_NONWORKINGDAY = 2;
		
		@SuppressWarnings("deprecation")
		public		ComponentSchedule (Component oComponent)
		{
			m_oComponent = oComponent;
			m_iSlotRows = 0;
			m_iSlotCols = 0;
			m_iSlotRowSelected = -1;
			m_iSlotColSelected = -1;
			m_vecConstraints = new Vector<Object>();
			m_vecNonWorkingDays = new Vector<YFCDate>();
			m_vecSlotRowTitles = new Vector<Object>();
			m_vecSlotColTitles = new Vector<Object>();
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
		
		public		void		getPossibleSchedules (String sHorizonDays) throws Exception
		{
			YFSEnvironment env = YFSUtil.getYFSEnv();
			YIFApi api = YFSUtil.getYIFApi();
			ShoppingCart	oShoppingCart = getComponent().getShoppingCartItem().getShoppingCart();
			
			// get component's details (description and price)
			Hashtable<String, String> htPromise = new Hashtable<String, String>();
			htPromise.put ("OrderHeaderKey", oShoppingCart.getOrderHeaderKey());
			htPromise.put ("OrderLineKey", getComponent().getOrderLineKey());
			htPromise.put ("ReturnMultipleSrvcSlots", "Y");
			htPromise.put ("DelayWindow", sHorizonDays);
			htPromise.put ("MaximumRecords", "1");
			htPromise.put ("IgnoreMinNotificationTime", "Y");
			htPromise.put ("Mode", "Inquire");
			htPromise.put ("CheckInventory", "Y");
			
			// prepare to call getItemDetails()
			YFSXMLParser inXml = new YFSXMLParser();
			inXml.createRootElement("Promise", htPromise);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getPossibleSchedules() for Component");
				System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
			}

			Document docOutXml = api.getPossibleSchedules (env, inXml.getDocument());
		
			// now parse through the XML output document and load description
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getPossibleSchedules() for Component");
				System.out.println (YFSXMLUtil.getXMLString (docOutXml));
			}
			YFCDocument	docPossibleSchedules = YFCDocument.getDocumentFor (docOutXml);
			getPossibleSchedulesFromPossibleSchedules (docPossibleSchedules);
			return;
		}

		@SuppressWarnings("deprecation")
		public void	getPossibleSchedulesFromPossibleSchedules (YFCDocument docPossibleSchedules) throws Exception
		{
			YFCElement	elePromise = docPossibleSchedules.getDocumentElement ();
			YFCElement	eleSuggestedOption = elePromise.getChildElement ("SuggestedOption");
			if (eleSuggestedOption == null)
				return;
			YFCElement	eleOption = eleSuggestedOption.getChildElement ("Option");
			if (eleOption == null)
				return;
				
			YFCDate	dtFirstDate = null;
			YFCDate	dtLastDate = null;
			YFCDate	dtAssignedDate = null;
			YFCElement eleInteractions = eleOption.getChildElement ("Interactions");
			if (eleInteractions != null)
			{			
				for (Iterator<?> iInteractions = eleInteractions.getChildren(); iInteractions.hasNext(); )
				{
					YFCElement	eleInteraction = (YFCElement)iInteractions.next();
					YFCElement	eleNonWorkingDays = eleInteraction.getChildElement ("NonWorkingDays");
					buildNonWorkingDays (eleNonWorkingDays);
				}			
			}

			YFCElement	elePromiseServiceLines = eleOption.getChildElement ("PromiseServiceLines");
			if (elePromiseServiceLines != null)
			{
				for (Iterator<?> iPromiseServiceLines = elePromiseServiceLines.getChildren(); iPromiseServiceLines.hasNext(); )
				{
					YFCElement	elePromiseServiceLine = (YFCElement)iPromiseServiceLines.next();

					// if promise line's order line key matches components order line key
					if (elePromiseServiceLine.getAttribute ("OrderLineKey").equals (getComponent().getOrderLineKey()))
					{
						// get first and last possible dates				
						dtFirstDate = YFCDate.getYFCDate (elePromiseServiceLine.getAttribute ("DeliveryStartSearchDate"));
						dtLastDate = YFCDate.getYFCDate (elePromiseServiceLine.getAttribute ("DeliveryEndSearchDate"));
					
						// get assigned dates
						YFCElement	eleAssignments = elePromiseServiceLine.getChildElement ("Assignments");
						if (eleAssignments != null)
						{
							for (Iterator<?> iAssignments = eleAssignments.getChildren(); iAssignments.hasNext(); )
							{
								YFCElement	eleAssignment = (YFCElement)iAssignments.next();
								dtAssignedDate = YFCDate.getYFCDate(eleAssignment.getAttribute ("ApptDate"));
							}
						}
						else
							dtAssignedDate = dtFirstDate;
						YFCElement	eleSlots = elePromiseServiceLines.getChildElement ("Slots");
						buildSlotGrid (eleSlots, dtFirstDate, dtLastDate, dtAssignedDate);
						break;
					}
				}
			}
		}

		@SuppressWarnings("deprecation")
		public	void	getPossibleSchedulesFromFindInventory (YFCDocument docFindInventory) throws Exception
		{
			YFCElement	eleFindInventory = docFindInventory.getDocumentElement ();
			YFCElement	eleSuggestedOption = eleFindInventory.getChildElement ("SuggestedOption");
			if (eleSuggestedOption == null)
				return;
			YFCElement	eleOption = eleSuggestedOption.getChildElement ("Option");
			if (eleOption == null)
				return;
			YFCElement	elePromiseLines = eleOption.getChildElement ("PromiseLines");				
			YFCElement	elePromiseServiceLines = eleOption.getChildElement ("PromiseServiceLines");
			YFCElement	eleInteractions = eleOption.getChildElement ("Interactions");						

			
			YFCDate	dtFirstDate = null;
			YFCDate	dtLastDate = null;
			YFCDate	dtAssignedDate = null;

			for (Iterator<?> iInteractions = eleInteractions.getChildren (); iInteractions.hasNext(); )
			{
				YFCElement	eleInteraction = (YFCElement)iInteractions.next();

				// get first and last possible dates				
				YFCDate dtNewFirst = YFCDate.getYFCDate (eleInteraction.getAttribute ("EarliestDate"));
				YFCDate dtNewLast = YFCDate.getYFCDate (eleInteraction.getAttribute ("LastDate"));
				// the true first date is the latest of all first dates
				if (dtFirstDate == null || dtFirstDate.gt(dtNewFirst, true))
					dtFirstDate = dtNewFirst;

				// the true last date is the earliest of all last dates
				if (dtLastDate == null || dtLastDate.gt (dtLastDate, true))
					dtLastDate = dtNewLast;
				YFCElement	eleNonWorkingDays = eleInteraction.getChildElement ("NonWorkingDays");
				buildNonWorkingDays (eleNonWorkingDays);
			}

			// iterate over all promise lines
			for (Iterator<?> iPromiseLines = elePromiseLines.getChildren(); iPromiseLines.hasNext(); )
			{
				YFCElement elePromiseLine = (YFCElement)iPromiseLines.next();

				// if promise line's order line key matches components order line key
				if (elePromiseLine.getAttribute ("LineId").equals (getComponent().getTransactionalLineId()))
				{
					// get assigned dates
					YFCElement	eleAssignments = elePromiseLine.getChildElement ("Assignments");
					if (eleAssignments == null)
						continue;
					Iterator<?> iAssignments = eleAssignments.getChildren();					
					if (iAssignments == null)
						continue;
						
					// if get first availability date
					if (iAssignments.hasNext())
					{
						YFCElement eleAssignment = (YFCElement)iAssignments.next();
						getComponent().setDeliveryDate (eleAssignment.getAttribute ("DeliveryDate"));
						getComponent().setAvailableDate (eleAssignment.getAttribute ("ProductAvailDate"));
					}
					return;
				}
			}
			
			// iterate over all promise service lines (s/b just one)
			for (Iterator<?> iPromiseServiceLines = elePromiseServiceLines.getChildren(); iPromiseServiceLines.hasNext(); )
			{
				YFCElement	elePromiseServiceLine = (YFCElement)iPromiseServiceLines.next();				

				// if promise line's order line key matches components order line key
				if (elePromiseServiceLine.getAttribute ("LineId").equals (getComponent().getTransactionalLineId()))
				{					
					// get assigned dates
					YFCElement	eleAssignments = elePromiseServiceLine.getChildElement ("Assignments");
					if (eleAssignments == null)
						continue;
					Iterator<?> iAssignments = eleAssignments.getChildren();					
					if (iAssignments == null)
						continue;
						
					// if date is assigned get that date
					if (iAssignments.hasNext())
					{
						YFCElement eleAssignment = (YFCElement)iAssignments.next();
						dtAssignedDate = YFCDate.getYFCDate(eleAssignment.getAttribute ("ApptDate"));
					}
					else
						dtAssignedDate = dtFirstDate;
					YFCElement	eleSlots = elePromiseServiceLine.getChildElement ("Slots");
					buildSlotGrid (eleSlots, dtFirstDate, dtLastDate, dtAssignedDate);
					return;
				}
			}
			return;
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
		
		@SuppressWarnings("deprecation")
		protected	void	buildNonWorkingDays (YFCElement eleNonWorkingDays)
		{
			if (eleNonWorkingDays != null)
			{
				for (Iterator<?> i = eleNonWorkingDays.getChildren(); i.hasNext();)
				{
					YFCElement	eleNonWorkingDay = (YFCElement)i.next();
					YFCDate	dtNonWorkingDay = YFCDate.getYFCDate (eleNonWorkingDay.getAttribute ("Date"));				
					dtNonWorkingDay.removeTimeComponent();
					m_vecNonWorkingDays.add (dtNonWorkingDay);
					if (YFSUtil.getDebug())
						System.out.println ("Non-Working Day: "+dtNonWorkingDay.getString());
				}
				if (YFSUtil.getDebug())
					System.out.println ("Non-Working Days Count="+getNonWorkingDaysCount());
			}
		}
				
		@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
		protected	void	buildSlotGrid (YFCElement eleSlots, YFCDate dtFirstDate, YFCDate dtLastDate, YFCDate dtAssignedDate)
		{
			int	iDays;
			int	iRow, iCol;
			iRow = 0;
			Vector	vecSlots = new Vector();
			
			// store slots in vector
			for (Iterator<?> i = eleSlots.getChildren(); i.hasNext(); )
			{
				YFCElement	eleSlot = (YFCElement)i.next();
				vecSlots.add (eleSlot);
			}
			m_iSlotRows = vecSlots.size();

			// build row titles
			for (iRow = 0; iRow < m_iSlotRows; iRow++)
			{
				YFCElement eleSlot = (YFCElement)vecSlots.elementAt (iRow);
				m_vecSlotRowTitles.add ((String)(eleSlot.getAttribute ("ServiceSlotDesc")+"-("+eleSlot.getAttribute ("StartTime")+"-"+eleSlot.getAttribute ("EndTime")+")"));					
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
				String	sMonth = ShoppingCart.CALENDAR_MONTHS [dtSlotDate.getMonth()];
				String	sDay   = Integer.toString (dtSlotDate.getDayOfMonth());
				dtSlotDate.getDayOfWeek ();
				
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
				YFCElement eleAvailableDates = eleSlot.getChildElement("AvailableDates");
				m_vecAvailableDays[iRow] = new Vector<Object>();
				if (eleAvailableDates != null)
				{
					for (Iterator<?> i = eleAvailableDates.getChildren(); i.hasNext(); )
					{
						YFCElement	eleAvailableDate = (YFCElement)i.next();
										
						// add available date/time to slot
						if (eleAvailableDate.getAttribute ("Confirmed").equals("Y"))
						{
							YFCDate	dtAvailable = YFCDate.getYFCDate (eleAvailableDate.getAttribute ("Date")+"T"+eleSlot.getAttribute ("StartTime"));
							m_vecAvailableDays[iRow].add (dtAvailable);
						}
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
		public	int	getSlotAvailability (YFCDate dtDate)
		{
			return (getSlotAvailability (dtDate, false));
		}
		
		@SuppressWarnings("deprecation")
		public	int	getSlotAvailability (YFCDate dtDate, boolean bAnyTime)
		{
			YFCDate dtCompareDate1 = new YFCDate (dtDate);			
			dtCompareDate1.removeTimeComponent ();
			for (int iDay = 0; iDay < m_vecNonWorkingDays.size(); iDay++)
			{
				YFCDate	dtCompareDate2 = (YFCDate)m_vecNonWorkingDays.elementAt (iDay);
				if (dtCompareDate1.equals (dtCompareDate2))
					return SLOT_NONWORKINGDAY;
			}
			dtCompareDate1 = dtDate;
			if (bAnyTime)
				dtCompareDate1.removeTimeComponent();
				
			for (int iSlot = 0; iSlot < m_vecAvailableDays.length; iSlot++)
			{
				for (int iDate = 0; iDate < m_vecAvailableDays[iSlot].size(); iDate++)
				{
					YFCDate	dtCompareDate2 = (YFCDate)m_vecAvailableDays[iSlot].elementAt (iDate);
					if (bAnyTime)
						dtCompareDate2.removeTimeComponent();
						
					if (dtCompareDate1.equals (dtCompareDate2))
						return SLOT_AVAILABLE;
				}
				
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
		public		boolean		getIsService (String sItemGroupCode) { return (sItemGroupCode.equals("PS") || sItemGroupCode.equals("DS")); }
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
		@SuppressWarnings("deprecation")
		protected	Vector<YFCDate>		m_vecNonWorkingDays;
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
	protected	String	m_sIntegrationID;
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
	protected	String	m_sTax;
	protected	String	m_sTransactionalLineId;
	protected	String	m_sParentBundle;
	protected	String	m_sPrimeLineNo;
	protected	String	m_sRequestedDate;
	protected	String	m_sDeliveryDate;
	protected	String	m_sAvailableDate;
	protected	String	m_sRequiresProdAssociation;
	protected	String	m_sProductLine;

	// protected object member variables
	protected	ShoppingCart		m_oShoppingCart;	
	protected	Vector<Component>	m_vecComponents;
	protected	Vector<Option>		m_vecOptions;
}

