/**
  * ShoppingCart.java
  *
  **/

// PACKAGE
package com.custom.yantra.shoppingcart;

import java.util.*;
import com.custom.yantra.util.*;
import com.custom.yantra.customer.*;
import com.custom.yantra.shoppingcart.ShoppingCartItem.Component;
import com.custom.yantra.vendor.*;
import org.w3c.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.util.*;
import com.yantra.yfc.dom.*;
import java.math.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class ShoppingCart implements Serializable
{
	public 		static final String		CALENDAR_MONTHS[] = 
	{"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
	 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	public		static final String		CALENDAR_DAYS[] = 
	{ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };


    public ShoppingCart()
    {		
		m_sOrderHeaderKey = "";
		m_sCartTotal = "0.00";
		m_sCartSubTotal = "0.00";
		m_sCartDiscount = "0.00";
		m_sCartShipping = "0.00";
		m_sCartTaxes = "0.00";
		m_sOrderNo = "";
		m_sOrderName = "";
		m_sOrderSource = "";
		m_sEnteredBy = "";
		m_sOrderType = "";
		m_sRequestedDate = "";
		m_sOrderDate = "";
		m_sPriceProgram = "";
		m_sLocaleCode = "en_US_EST";
		m_sSCAC = "";
		m_sCarrierService="";
		m_sIntegrationID = "";
				
		m_sSearchField1Name = "SearchCriteria1";
		m_sSearchField2Name = "SearchCriteria2";
		m_sSearchField1Value = "";
		m_sSearchField2Value = "";
		m_sDiscountCategory = "";
		m_sShippingCategory = "";
		m_sTaxName = "";
		m_vecShoppingCartItems = new Vector<ShoppingCartItem> ();
		m_vecSchedules = new Vector<Component>();
		m_bLinesPriced = true;
    }

	// cart 	
	public		String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public		void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public		String	getIntegrationID () { return m_sIntegrationID; }
	public		void	setIntegrationID (String sIntegrationID) { m_sIntegrationID = sIntegrationID; }
	public		String	getCartSubTotal () { return m_sCartSubTotal; }
	public		void	setCartSubTotal (String sCartSubTotal) { m_sCartSubTotal = sCartSubTotal; }
	public		String	getCartTotal () { return m_sCartTotal; }
	public		void	setCartTotal (String sCartTotal) { m_sCartTotal = sCartTotal; }
	public		String	getCartShipping () { return m_sCartShipping; }
	public		void	setCartShipping (String sCartShipping) { m_sCartShipping = sCartShipping; }
	public		String	getCartDiscount () { return m_sCartDiscount; }
	public		void	setCartDiscount (String sCartDiscount) { m_sCartDiscount = sCartDiscount; }
	public		String	getCartTaxes () { return m_sCartTaxes; }
	public		void	setCartTaxes (String sCartTaxes) { m_sCartTaxes = sCartTaxes; }
	public		String	getOrderName () { return m_sOrderName; }
	public		void	setOrderName (String sOrderName) { m_sOrderName = sOrderName; }
	public		String	getOrderSource () { return m_sOrderSource; }
	public		void	setOrderSource (String sOrderSource) { m_sOrderSource = sOrderSource; }
	public		String	getOrderType () { return m_sOrderType; }
	public		void	setOrderType (String sOrderType) { m_sOrderType = sOrderType; }
	public		String	getOrderNumber () { return m_sOrderNo; }
	public		void	setOrderNumber (String sOrderNo) { m_sOrderNo = sOrderNo; }
	public		String	getOrderSCAC () { return m_sSCAC; }
	public		void	setOrderSCAC (String sSCAC) { m_sSCAC = sSCAC; }
	public		String	getOrderCarrierService () { return m_sCarrierService; }
	public		void	setOrderCarrierService (String sCarrierService) { m_sCarrierService = sCarrierService; }
	public		String	getEnteredBy () { return m_sEnteredBy; }
	public		void	setEnteredBy (String sEnteredBy) { m_sEnteredBy = sEnteredBy; }
	public		String	getOrderDate() { return m_sOrderDate; }
	public		void	setOrderDate (String sOrderDate) { m_sOrderDate = sOrderDate; }
	public		String	getRequestedDate() { return m_sRequestedDate; }
	public		void	setRequestedDate (String sRequestedDate) { m_sRequestedDate = sRequestedDate; }
	public		String	getLocaleCode() { return m_sLocaleCode; }
	public		void	setLocaleCode (String sLocaleCode) 
	{ 
		m_sLocaleCode = sLocaleCode;
		// initialize the locale to given locale code
		YFCLocaleUtils.init (getLocaleCode());
	}

	// header discount, shipping and tax category names
	public	String	getDiscountCategory ()	{ return m_sDiscountCategory; }
	public	void	setDiscountCategory(String sDiscountCategory)	{ m_sDiscountCategory = sDiscountCategory; }
	public	String	getShippingCategory()	{ return m_sShippingCategory; }
	public	void	setShippingCategory(String sShippingCategory)	{ m_sShippingCategory = sShippingCategory; }
	public	String	getTaxName ()			{ return m_sTaxName; }
	public	void	setTaxName(String sTaxName)						{ m_sTaxName = sTaxName; }

	public	void calcCartTotals (boolean bApplyDiscountsToItems, boolean bUseLineDiscounts, boolean bUseLineShipping, String sTaxRate)
	{
		BigDecimal	bdDiscountTotal = new BigDecimal ("0.00").setScale (2, BigDecimal.ROUND_HALF_UP);
		BigDecimal	bdShippingTotal = new BigDecimal ("0.00").setScale (2, BigDecimal.ROUND_HALF_UP);
		BigDecimal	bdSubTotal = new BigDecimal ("0.00").setScale (2, BigDecimal.ROUND_HALF_UP);
		BigDecimal	bdTotal = new BigDecimal ("0.00").setScale (2, BigDecimal.ROUND_HALF_UP);
		BigDecimal	bdTaxTotal = new BigDecimal ("0.00").setScale (2, BigDecimal.ROUND_HALF_UP);
				
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem (iItem);
			oItem.calcTotalPrice (bApplyDiscountsToItems);
			BigDecimal	bdTotalPrice = new BigDecimal(oItem.getTotalPrice()).setScale (2, BigDecimal.ROUND_HALF_UP);
			BigDecimal	bdDiscount = new BigDecimal (oItem.getDiscount()).setScale (2, BigDecimal.ROUND_HALF_UP);
			BigDecimal	bdShipping = new BigDecimal (oItem.getShipping()).setScale (2, BigDecimal.ROUND_HALF_UP);
			BigDecimal	bdLineTax = new BigDecimal (sTaxRate).setScale (2, BigDecimal.ROUND_HALF_UP);
			bdLineTax = bdLineTax.multiply (bdTotalPrice).setScale (2, BigDecimal.ROUND_HALF_UP);
			bdTaxTotal = bdTaxTotal.add (bdLineTax);
			oItem.setTax (bdLineTax.toString());
			bdSubTotal = bdSubTotal.add (bdTotalPrice);
			bdDiscountTotal = bdDiscountTotal.add (bdDiscount);
			bdShippingTotal = bdShippingTotal.add (bdShipping);
		}
		// set discounts
		if (!bUseLineDiscounts)
			bdDiscountTotal = new BigDecimal (getCartDiscount()).setScale (2);
		setDiscountCategory ("Discount");
		setCartDiscount (bdDiscountTotal.toString());

		// set shipping
		if (!bUseLineShipping)
			bdShippingTotal = new BigDecimal (getCartShipping()).setScale (2);
		setShippingCategory ("Shipping");
		setCartShipping (bdShippingTotal.toString());

		// set taxes
		setCartTaxes (bdTaxTotal.toString());
		setTaxName ("Tax");
		setCartSubTotal (bdSubTotal.toString());
		
		// total cart
		bdTotal = bdTotal.add (bdSubTotal.add(bdShippingTotal).add (bdTaxTotal).subtract (bdDiscountTotal));
		setCartTotal (bdTotal.toString());			
	}

	public	void calcCartTotals (boolean bApplyDiscountsToItems, boolean bUseLineDiscounts, boolean bUseLineShipping)
	{
		calcCartTotals (bApplyDiscountsToItems, bUseLineDiscounts, bUseLineShipping, "0.00");
	}
	

	// items
	public		ShoppingCartItem	getShoppingCartItem (int iItem)	{ return (ShoppingCartItem)m_vecShoppingCartItems.elementAt (iItem); }
	public		int					getShoppingCartItemCount () { return m_vecShoppingCartItems.size(); }

	// customer Org info
	public		String				getOrganizationCode() { return m_sOrganizationCode; }
	public		void				setOrganizationCode(String sOrganizationCode) { m_sOrganizationCode = sOrganizationCode; }
	public		Customer			getCustomer () { return m_oCustomer; }
	public		void				setCustomer (Customer oCustomer) { m_oCustomer = oCustomer; }
	public		Vendor				getVendor () { return m_oVendor; }
	public		void				setVendor (Vendor oVendor) { m_oVendor = oVendor; }

	// pricing program
	public		String				getPriceProgram () { return m_sPriceProgram; }
	public		void				setPriceProgram (String sPriceProgram) { m_sPriceProgram = sPriceProgram; }
	public		boolean				getIsLinesPriced () { return m_bLinesPriced; }
	public		void				setIsLinesPriced (boolean bLinesPriced) { m_bLinesPriced = bLinesPriced; }

	// search by fields
	public		String				getSearchField1Name () { return m_sSearchField1Name; }
	public		void				setSearchField1Name (String sName) { m_sSearchField1Name = sName; }
	public		String				getSearchField2Name () { return m_sSearchField2Name; }
	public		void				setSearchField2Name (String sName)	{ m_sSearchField2Name = sName; }
	public		String				getSearchField1Value () { return m_sSearchField1Value; }
	public		void				setSearchField1Value (String sValue) { m_sSearchField1Value = sValue.toUpperCase(); }
	public		String				getSearchField2Value () { return m_sSearchField2Value; }
	public		void				setSearchField2Value (String sValue)	{ m_sSearchField2Value = sValue.toUpperCase(); }	

	public	ShoppingCartItem createNewShoppingCartItem()
	{
		return new ShoppingCartItem(this);
	}

	public	void	Reset ()
	{
		m_sCartTotal = "0.00";
		m_sCartSubTotal = "0.00";
		m_sCartDiscount = "0.00";
		m_sCartShipping = "0.00";
		m_sCartTaxes = "0.00";
		m_sOrderNo = "";
		m_sOrderName = "";
		m_sOrderType = "";
		m_sRequestedDate = "";
		m_sOrderDate = "";
		m_sPriceProgram = "";
		m_sLocaleCode = "en_US_EST";

		m_sSearchField1Name = "SearchCriteria1";
		m_sSearchField2Name = "SearchCriteria2";
		m_sSearchField1Value = "";
		m_sSearchField2Value = "";

		// reset all items in cart
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
			getShoppingCartItem(iItem).Reset();

		// remove all items in the cart	
		m_vecShoppingCartItems.clear();
		m_vecSchedules.clear();
	}

	public	void	addSchedule (ShoppingCartItem.Component oComponent)
	{
		oComponent.createNewComponentSchedule ();
		m_vecSchedules.add (oComponent);
	}

				
	public	void	addShoppingCartItem (ShoppingCartItem oSCItem) throws Exception
	{
		addShoppingCartItem (oSCItem, true, false);
	}

	public	void	addShoppingCartItem (ShoppingCartItem oSCItem, boolean bLoadDetailsFromYantra, boolean bLoadOptionsFromYantra) throws Exception
	{
		if (bLoadDetailsFromYantra)
		{
			oSCItem.loadItem(bLoadOptionsFromYantra);
			oSCItem.loadShippingAndDiscountsForItem();
		}
		m_vecShoppingCartItems.add (oSCItem);
	}
	
	public	void	addShoppingCartItem (String sItemID, String sQty, String sUOM, String sPC, boolean bLoadDetailsFromYantra, boolean bLoadOptionsFromYantra) throws Exception
	{
		ShoppingCartItem	oSCItem = (ShoppingCartItem)createNewShoppingCartItem();
		oSCItem.setItemID(sItemID);
		oSCItem.setQty (sQty);
		oSCItem.setUOM (sUOM);
		oSCItem.setProductClass (sPC);
		addShoppingCartItem (oSCItem, bLoadDetailsFromYantra, bLoadOptionsFromYantra);						
	}

	public	void	addShoppingCartItem (String sItemID, String sQty, String sUOM, String sPC) throws Exception
	{
		addShoppingCartItem (sItemID, sQty, sUOM, sPC, true, false);
	}
	
	public	void	addShoppingCartItem (String sItemID, String sQty, String sUOM, String sPC, boolean bLoadDetailsFromYantra) throws Exception
	{
		ShoppingCartItem	oSCItem = (ShoppingCartItem)createNewShoppingCartItem();
		oSCItem.setItemID(sItemID);
		oSCItem.setQty (sQty);
		oSCItem.setUOM (sUOM);
		oSCItem.setProductClass (sPC);
		addShoppingCartItem (oSCItem, bLoadDetailsFromYantra, false);
	}

	
	public	void	createOrder (boolean bScheduleImmediately) throws Exception
	{
		createOrder (false, bScheduleImmediately);
	}

	public	void	createOrder (boolean bDraftOrder, boolean bScheduleImmediately) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();

		// call createOrder API
		Document docOutXML = api.createOrder (env, createOrderDoc(bDraftOrder));	
		String	sOrderConfirmation = YFSXMLUtil.getXMLString(docOutXML);
		setOrderNumber (YFSXMLUtil.getAttrValue (sOrderConfirmation, "OrderNo"));
		setOrderHeaderKey (YFSXMLUtil.getAttrValue (sOrderConfirmation, "OrderHeaderKey"));
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from create order API is ... ");
			System.out.println( sOrderConfirmation);
			System.out.println ("OrderNo="+getOrderNumber());
			System.out.println ("OrderHeader="+getOrderHeaderKey());
		}
		// now go get corresponding keys for all lines created
		getOrderKeys ();

		// if we're to schedule the order immediately		
		if (bScheduleImmediately)
			scheduleOrder ();
		return;
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	public	Document	createOrderDoc (boolean bDraftOrder) throws Exception
	{
		YFSXMLParser inXml = new YFSXMLParser();
		
		// create order
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("EnterpriseCode", getOrganizationCode ());
		htOrder.put ("SellerOrganizationCode", getVendor().getSellerOrganizationCode ());
		htOrder.put ("BuyerOrganizationCode", getCustomer().getBuyerOrganizationCode ());
		htOrder.put ("CustomerID", getCustomer().getCustomerID());
		if (bDraftOrder)
		{
			htOrder.put ("DraftOrderFlag", "Y");
		}
		else
		{
			htOrder.put ("DraftOrderFlag", "N");
			htOrder.put ("PaymentStatus", "AUTHORIZED");
		}
		htOrder.put ("PriceProgramName", getPriceProgram());
		htOrder.put ("OrderNo", getOrderNumber());
		htOrder.put ("OrderType", getOrderType());
		htOrder.put ("OrderName", getOrderName());
		htOrder.put ("OrderSource", getOrderSource());
		htOrder.put ("EnteredBy", getEnteredBy());
		htOrder.put ("SCAC", getOrderSCAC());
		htOrder.put ("ScacAndService", getOrderCarrierService());
		if (getOrderDate().length() == 0)
			setOrderDate (new YFCDate().getString (YFCLocale.getDefaultLocale(), false));
		htOrder.put ("OrderDate", YFCLocaleUtils.makeXMLDateTime (getOrderDate()));
		htOrder.put ("ReqDeliveryDate", YFCLocaleUtils.makeXMLDateTime (getRequestedDate()));
		htOrder.put ("Currency", YFCLocale.getDefaultLocale().getCurrency());

		// add elements too assist in searching
		if (getSearchField1Value().length() == 0)
			setSearchField1Value (getCustomer().getBTLastName());
		if (getSearchField2Value().length() == 0)
			setSearchField2Value (getCustomer().getBTZip());
		htOrder.put (getSearchField1Name(), getSearchField1Value());
		htOrder.put (getSearchField2Name(), getSearchField2Value());
		htOrder.put ("CustomerPONo", getCustomer().getBTPhone().toUpperCase());
		htOrder.put ("CustomerEMailID", getCustomer().getBTEmail ().toLowerCase());
		
		// generate XML for Order element			
		Element eleOrder = inXml.createRootElement ("Order", htOrder);

		// generate XML for OrderLines
		if (getIntegrationID().length() > 0)
		{
			Hashtable<String, String> htExtn = new Hashtable<String, String>();
			htExtn.put ("ExtnSiebelIntegrationId", getIntegrationID());
			inXml.createChild (eleOrder, "Extn", htExtn);
		}	
		Element eleOrderLines = inXml.createChild (eleOrder, "OrderLines", null);
		Element eleProductServiceAssocs = inXml.createChild (eleOrder, "ProductServiceAssocs", null);
		int	iLineNo = 1;
		
		// iterate over shopping cart items
		System.out.println ("No Items in Cart="+getShoppingCartItemCount());
		for (int iEle = 0; iEle < getShoppingCartItemCount(); iEle++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iEle);
			
			// if the item is not a kit then add the item to order
			if (!oItem.getIsKit())
			{
				// set transactional line id for this item
				if (oItem.getTransactionalLineId ().length() == 0)
					oItem.setTransactionalLineId("Line"+iLineNo);
				oItem.setPrimeLineNo (Integer.toString (iLineNo));
				
				// create OrderLine XML
				Hashtable<String, String> htOrderLine = new Hashtable<String, String>();
				htOrderLine.put("OrderedQty", oItem.getQty());
				htOrderLine.put("TransactionalLineId", oItem.getTransactionalLineId());
				htOrderLine.put("PrimeLineNo", oItem.getPrimeLineNo());
				htOrderLine.put("SubLineNo", "1");
				htOrderLine.put("ItemGroupCode", oItem.getItemGroupCode());
				htOrderLine.put("LineType", oItem.getLineType());
				Element eleOrderLine = inXml.createChild(eleOrderLines, "OrderLine", htOrderLine);
				if (oItem.getIntegrationID().length() > 0)
				{
					Hashtable<String, String>	htExtn = new Hashtable<String, String> ();
					htExtn.put ("ExtnSiebelIntegrationId", oItem.getIntegrationID());
					inXml.createChild (eleOrderLine, "Extn", htExtn);
				}

				// add item information to order line			
				Hashtable<String, String> htItem = new Hashtable<String, String> ();
				htItem.put("ItemID", oItem.getItemID ());
				htItem.put("UnitOfMeasure", oItem.getUOM());			
				htItem.put("ProductClass", oItem.getProductClass());			
				htItem.put("ProductLine", oItem.getProductLine());
				inXml.createChild(eleOrderLine, "Item", htItem);
				
				// add pricing information to order line
				Hashtable<String, String> htPriceInfo = new Hashtable<String, String>();
				// if lines are pre-priced by client
				BigDecimal bdUnitPrice;
				if (getIsLinesPriced())
				{
					// compute the unit price based on price/qty
					bdUnitPrice = new BigDecimal (oItem.getPrice());
					bdUnitPrice.divide (new BigDecimal (oItem.getQty()), 2);
				}
				else
					bdUnitPrice = new BigDecimal(oItem.getUnitPrice());
				bdUnitPrice = bdUnitPrice.setScale (2);
				htPriceInfo.put ("UnitPrice", bdUnitPrice.toString());
				inXml.createChild(eleOrderLine, "LinePriceInfo", htPriceInfo);				
				if (oItem.getParentBundle().length() > 0)
				{
					Hashtable<String, String>	htBundleParent = new Hashtable<String, String>();
					htBundleParent.put ("TransactionalLineId", oItem.getParentBundle());
					inXml.createChild (eleOrderLine, "BundleParentLine", htBundleParent);
				}
				iLineNo++;
			}
						
			// now iterate over items associated components/services/bundles
			int iComponent;
			if (YFSUtil.getDebug())
			{
				System.out.println ("Processing "+oItem.getComponentCount()+" Components for Item "+oItem.getItemID());
			}
			for (iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component	oComponent = oItem.getComponent (iComponent);
				
				// set transactional line id for this component
				if (oComponent.getTransactionalLineId ().length() == 0)
					oComponent.setTransactionalLineId("Line"+iLineNo);
				oComponent.setPrimeLineNo (Integer.toString (iLineNo));

				// if this is a service component				
				if (oComponent.getIsService())
				{
					// add service item							
					Hashtable<String, String> htServiceLine = new Hashtable<String, String>();
					htServiceLine.put("OrderedQty", oComponent.getSvcQty());
					htServiceLine.put("TransactionalLineId", oComponent.getTransactionalLineId());
					htServiceLine.put("PrimeLineNo", oComponent.getPrimeLineNo());
					htServiceLine.put("SubLineNo", "1");
					htServiceLine.put("ItemGroupCode", oComponent.getItemGroupCode());			
					htServiceLine.put("FullfillmentType", "PRODUCT_SOURCING");
					htServiceLine.put("LineType", oComponent.getLineType());
					
					//htServiceLine.put("ApptStatus", "CONFIRMED");
					//htServiceLine.put("PromisedApptStartDate",makeXMLDateTime (oComponent.getRequestedDate())+"T08:00:00");
					//htServiceLine.put("PromisedApptEndDate",makeXMLDateTime (oComponent.getRequestedDate())+"T12:00:00");
					//htServiceLine.put("Timezone","America/New_York");
					Element eleServiceLine = inXml.createChild(eleOrderLines, "OrderLine", htServiceLine);
					if (oComponent.getIntegrationID().length() > 0)
					{
						Hashtable<String, String>	htExtn = new Hashtable<String, String> ();
						htExtn.put ("ExtnSiebelIntegrationId", oComponent.getIntegrationID());
						inXml.createChild (eleServiceLine, "Extn", htExtn);
					}

					// add item information to order line			
					Hashtable<String, String> htServiceItem = new Hashtable<String, String> ();
					htServiceItem.put("ItemID", oComponent.getItemID ());
					htServiceItem.put("UnitOfMeasure", oComponent.getUOM());			
					htServiceItem.put("ProductLine", oComponent.getProductLine());
					inXml.createChild(eleServiceLine, "Item", htServiceItem);


					Element	eleOrderLineOptions = inXml.createChild (eleServiceLine, "OrderLineOptions", null);

					for (int iOption = 0; iOption < oItem.getOptionCount(); iOption++)
					{
						ShoppingCartItem.Option	oOption = oItem.getOption (iOption);
						if (oOption.getComponent() == oComponent)
						{
							Hashtable<String, String>	htOrderLineOption = new Hashtable<String, String>();
							htOrderLineOption.put ("OptionItemID", oOption.getItemID());
							htOrderLineOption.put ("OptionUOM", oOption.getUOM());

							// yantra will compute the quantity
							Element eleOrderLineOption = inXml.createChild(eleOrderLineOptions, "OrderLineOption", htOrderLineOption);
							if (oOption.getIntegrationID().length() > 0)
							{
								Hashtable<String, String>	htExtn = new Hashtable<String, String>();
								htExtn.put ("ExtnSiebelIntegrationId", oOption.getIntegrationID());
								inXml.createChild (eleOrderLineOption, "Extn", htExtn);
							}
						}
					}
					
					// add pricing information to order line
					Hashtable<String, String> htPriceInfo = new Hashtable<String, String>();
					BigDecimal bdUnitPrice;
					// if lines are pre-priced by client
					if (getIsLinesPriced())
					{
						// compute the unit price based on price/qty
						bdUnitPrice = new BigDecimal (oComponent.getPrice());
						bdUnitPrice.divide (new BigDecimal (oComponent.getSvcQty()), 2);
					}
					else
						bdUnitPrice = new BigDecimal(oComponent.getUnitPrice());
					bdUnitPrice = bdUnitPrice.setScale (2);
					htPriceInfo.put ("UnitPrice", bdUnitPrice.toString());
					inXml.createChild(eleServiceLine, "LinePriceInfo", htPriceInfo);				

					// if component is associated to the product line
					//if (oComponent.getIsAssociatedToProduct())
					if (true)
					{
						// generate XML for ProductServiceAssocs on Order element
						Hashtable<?, ?> htProductServiceAssoc = new Hashtable<Object, Object>();
						Element eleProductServiceAssoc = inXml.createChild (eleProductServiceAssocs, "ProductServiceAssoc", null);

						Hashtable<String, String> htProduct = new Hashtable<String, String>();
						htProduct.put("TransactionalLineId", oItem.getTransactionalLineId());
						inXml.createChild (eleProductServiceAssoc, "ProductLine", htProduct);

						Hashtable<?, ?> htService = new Hashtable<Object, Object>();
						htProduct.put("TransactionalLineId", oComponent.getTransactionalLineId());
						inXml.createChild (eleProductServiceAssoc, "ServiceLine", htProduct);
					}
					if (oComponent.getParentBundle().length() > 0)
					{
						Hashtable<String, String>	htBundleParent = new Hashtable<String, String>();
						htBundleParent.put ("TransactionalLineId", oComponent.getParentBundle());
						inXml.createChild (eleServiceLine, "BundleParentLine", htBundleParent);
					}
					iLineNo++;
				}
				else
				{					
					// create OrderLine XML
					Hashtable<String, String> htOrderLine = new Hashtable<String, String>();
					htOrderLine.put("OrderedQty", oComponent.getQty());
					htOrderLine.put("TransactionalLineId", oComponent.getTransactionalLineId());
					htOrderLine.put("PrimeLineNo", oComponent.getPrimeLineNo());
					htOrderLine.put("SubLineNo", "1");
					htOrderLine.put("ItemGroupCode", oComponent.getItemGroupCode());
					htOrderLine.put("LineType", oComponent.getLineType());

					Element eleOrderLine = inXml.createChild(eleOrderLines, "OrderLine", htOrderLine);

					// add item information to order line			
					Hashtable<String, String> htItem = new Hashtable<String, String> ();
					htItem.put("ItemID", oComponent.getItemID ());
					htItem.put("UnitOfMeasure", oComponent.getUOM());			
					htItem.put("ProductClass", oComponent.getProductClass());			
					htItem.put("ProductLine", oComponent.getProductLine());
					inXml.createChild(eleOrderLine, "Item", htItem);
					if (oComponent.getIntegrationID().length() > 0)
					{
						Hashtable<String, String>	htExtn = new Hashtable<String, String> ();
						htExtn.put ("ExtnSiebelIntegrationId", oComponent.getIntegrationID());
						inXml.createChild (eleOrderLine, "Extn", htExtn);
					}

					// add pricing information to order line
					Hashtable<String, String> htPriceInfo = new Hashtable<String, String>();
					
					// if lines are pre-priced by client
					BigDecimal	bdUnitPrice;
					if (getIsLinesPriced())
					{
						// compute the unit price based on price/qty
						bdUnitPrice = new BigDecimal (oComponent.getPrice());
						bdUnitPrice.divide (new BigDecimal (oComponent.getQty()), 2);
					}
					else
						bdUnitPrice = new BigDecimal(oComponent.getUnitPrice());
					bdUnitPrice.setScale (2);
					htPriceInfo.put ("UnitPrice", oComponent.getUnitPrice());
					inXml.createChild(eleOrderLine, "LinePriceInfo", htPriceInfo);				
					if (oComponent.getParentBundle().length() > 0)
					{
						Hashtable<String, String>	htBundleParent = new Hashtable<String, String>();
						htBundleParent.put ("TransactionalLineId", oComponent.getParentBundle());
						inXml.createChild (eleOrderLine, "BundleParentLine", htBundleParent);
					}
					iLineNo++;
				}
			}
		}

		// if customer information provided 
		if (getCustomer() != null)
		{
			// finish filling out address fields
			Hashtable<String, String> htBillToPersonInfo = new Hashtable<String, String>();
			Customer	oCustomer = getCustomer();
			htBillToPersonInfo.put ("FirstName", oCustomer.getBTFirstName());
			htBillToPersonInfo.put ("LastName", oCustomer.getBTLastName());
			htBillToPersonInfo.put ("AddressLine1", oCustomer.getBTStreetAddress());
			htBillToPersonInfo.put ("AddressLine2", oCustomer.getBTAddress2());
			htBillToPersonInfo.put ("City", oCustomer.getBTCity());
			htBillToPersonInfo.put ("State", oCustomer.getBTState());
			htBillToPersonInfo.put ("ZipCode", oCustomer.getBTZip());
			htBillToPersonInfo.put ("Country", oCustomer.getBTCountry());
			htBillToPersonInfo.put ("EmailID", oCustomer.getBTEmail());
			htBillToPersonInfo.put ("DayPhone", oCustomer.getBTPhone());
			inXml.createChild (eleOrder, "PersonInfoBillTo", htBillToPersonInfo);
					
			// finish filling out address fields
			Hashtable<String, String> htShipToPersonInfo = new Hashtable<String, String>();
			htShipToPersonInfo.put ("FirstName", oCustomer.getSTFirstName());
			htShipToPersonInfo.put ("LastName", oCustomer.getSTLastName());
			htShipToPersonInfo.put ("AddressLine1", oCustomer.getSTStreetAddress());
			htShipToPersonInfo.put ("AddressLine2", oCustomer.getSTAddress2());
			htShipToPersonInfo.put ("City", oCustomer.getSTCity());
			htShipToPersonInfo.put ("State", oCustomer.getSTState());
			htShipToPersonInfo.put ("ZipCode", oCustomer.getSTZip());
			htShipToPersonInfo.put ("Country", oCustomer.getSTCountry());
			htShipToPersonInfo.put ("EmailID", oCustomer.getSTEmail());
			htShipToPersonInfo.put ("DayPhone", oCustomer.getSTPhone());
			inXml.createChild (eleOrder, "PersonInfoShipTo", htShipToPersonInfo);
			
			if (oCustomer.getCCName().length() > 0)
			{
				Element elePaymentMethods = inXml.createChild (eleOrder, "PaymentMethods", null);
				
				Hashtable<String, String> htPaymentMethod = new Hashtable<String, String>();
				htPaymentMethod.put ("PaymentType", "CREDIT_CARD");
				htPaymentMethod.put ("CreditCardType", oCustomer.getCCName());
				htPaymentMethod.put ("ChargeSequence", "1");
				htPaymentMethod.put ("CreditCardExpDate", oCustomer.getCCExpirationMonth()+"/"+oCustomer.getCCExpirationYear ());
				htPaymentMethod.put ("CreditCardName", oCustomer.getCCNameOnCard ());
				htPaymentMethod.put ("CreditCardNo", oCustomer.getCCNumber ());
				inXml.createChild (elePaymentMethods, "PaymentMethod", htPaymentMethod);
			}
		}		
		
		Element eleHeaderCharges = inXml.createChild (eleOrder, "HeaderCharges", null);

		// if shipping charges applicable
		if (getShippingCategory().length() > 0)
		{
			Hashtable<String, String> htHeaderCharge = new Hashtable<String, String> ();
			htHeaderCharge.put ("ChargeAmount", getCartShipping());
			htHeaderCharge.put ("ChargeCategory", getShippingCategory());
			inXml.createChild (eleHeaderCharges, "HeaderCharge", htHeaderCharge);
		}
		
		// if discounts applicatble
		if (getDiscountCategory().length() > 0)
		{
			Hashtable<String, String> htHeaderCharge = new Hashtable<String, String> ();
			htHeaderCharge.put ("ChargeAmount", getCartDiscount());
			htHeaderCharge.put ("ChargeCategory", getDiscountCategory());
			inXml.createChild (eleHeaderCharges, "HeaderCharge", htHeaderCharge);
		}

		// if taxes applicable
		Element eleHeaderTaxes = inXml.createChild (eleOrder, "HeaderTaxes", null);
		if (getTaxName().length() > 0)
		{
			Hashtable<String, String> htHeaderTax = new Hashtable<String, String>();
			htHeaderTax.put ("TaxName", getTaxName());
			htHeaderTax.put ("Tax", getCartTaxes());
			inXml.createChild (eleHeaderTaxes, "HeaderTax", htHeaderTax);
		}
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input for create order API is ... ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		return inXml.getDocument();
	}

	public	void	findInventory (int iDelayWindow) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document	docInXML = findInventoryDoc (iDelayWindow);

		// call createOrder API
		Document docOutXML = api.findInventory (env, docInXML);
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output for findInventory API is ... ");
			System.out.println( YFSXMLUtil.getXMLString(docOutXML));
		}				
		YFCDocument	docFindInventory = YFCDocument.getDocumentFor (docOutXML);
		Enumeration<Component>	enumScheduleComponents = getComponentsRequiringScheduling ();
		while (enumScheduleComponents.hasMoreElements ())
		{
			ShoppingCartItem.Component oComponent = (ShoppingCartItem.Component)enumScheduleComponents.nextElement();
			ShoppingCartItem.ComponentSchedule oSchedule  = oComponent.getComponentSchedule ();
	
			// get possible schedules for delivery/installation
			oSchedule.getPossibleSchedulesFromFindInventory (docFindInventory);

			// display column headings
			if (YFSUtil.getDebug())
			{
				String strIndent = "                             ";
				if (oComponent.getIsDeliveryService())	
					System.out.println (strIndent+"Delivery Schedule for Item "+oComponent.getItemID()+"<BR>");
				if (oComponent.getIsProductService())	
					System.out.println (strIndent+"Installation Schedule for Item "+oComponent.getItemID()+"<BR>");

				// display column headings
				System.out.print (strIndent);
				for (int iCol = 0; iCol < oSchedule.getSlotColCount(); iCol++)
				{
					System.out.print (oSchedule.getSlotColTitle(iCol)+" ");
				}
				System.out.println ("");
				
				// display table
				for (int iRow = 0; iRow < oSchedule.getSlotRowCount(); iRow++)
				{
					// display row title
					System.out.print (oSchedule.getSlotRowTitle (iRow)+strIndent.substring(0, strIndent.length()-oSchedule.getSlotRowTitle(iRow).length()));
		
					// display first/next column
					for (int iCol = 0; iCol < oSchedule.getSlotColCount(); iCol++)
					{
						if (oSchedule.getSlotAvailability (iRow, iCol) == ShoppingCartItem.ComponentSchedule.SLOT_AVAILABLE)
							System.out.print ("  [+]  ");
						else if (oSchedule.getSlotAvailability (iRow, iCol) == ShoppingCartItem.ComponentSchedule.SLOT_UNAVAILABLE)
							System.out.print ("  [X]  ");
						else if (oSchedule.getSlotAvailability (iRow, iCol) == ShoppingCartItem.ComponentSchedule.SLOT_NONWORKINGDAY)
							System.out.print ("  [/]  ");		
					}
					System.out.println ("");	
				}
				System.out.println ("");
				System.out.println ("");
			}
		}
		return;
	}
		
	@SuppressWarnings("deprecation")
	public	Document	findInventoryDoc (int iDelayWindow) throws Exception
	{
		YFSXMLParser inXml = new YFSXMLParser();
		
		// create order
		Hashtable<String, String>	htPromise = new Hashtable<String, String>();
		htPromise.put ("OrganizationCode", getOrganizationCode ());
		htPromise.put ("CheckInventory", "Y");
		htPromise.put ("ReturnMultipleSrvcSlots", "Y");
		htPromise.put ("DeliveryDateBased", "Y");
		htPromise.put ("ReqStartDate", YFCLocaleUtils.makeXMLDateTime (getRequestedDate()));
		if (iDelayWindow >= 0)
		{
			YFCDate	dtReqEndDate = new YFCDate (getRequestedDate(), YFCLocale.getDefaultLocale (), true);
			dtReqEndDate.changeDate (iDelayWindow);
			htPromise.put ("ReqEndDate", dtReqEndDate.getString(YFCDate.ISO_DATETIME_FORMAT));
		}
		
		// generate XML for Order element			
		Element elePromise = inXml.createRootElement ("Promise", htPromise);
		
		// generate XML for OrderLines
		Element elePromiseLines = inXml.createChild (elePromise, "PromiseLines", null);
		Element elePromiseServiceLines = inXml.createChild (elePromise, "PromiseServiceLines", null);
		Element	eleProductServiceAssocs = inXml.createChild (elePromise, "ServiceAssociations", null);

		int	iLineNo = 1;

		// iterate over shopping cart items
		for (int iEle = 0; iEle < getShoppingCartItemCount(); iEle++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iEle);

			// if the item is not a kit then add the item to promise
			if (!oItem.getIsKit())
			{
				// set transactional line id for this item - use integration id if one provided
				if (oItem.getIntegrationID ().length() == 0)
					oItem.setTransactionalLineId("Line"+iLineNo);
				else
					oItem.setTransactionalLineId(oItem.getIntegrationID ());
				oItem.setPrimeLineNo (Integer.toString (iLineNo));
				
				// create OrderLine XML
				Hashtable<String, String> htPromiseLine = new Hashtable<String, String>();
				htPromiseLine.put("RequiredQty", oItem.getQty());
				htPromiseLine.put("LineId", oItem.getTransactionalLineId());

				// add item information to order line			
				htPromiseLine.put("ItemID", oItem.getItemID ());
				htPromiseLine.put("UnitOfMeasure", oItem.getUOM());			
				htPromiseLine.put("ProductClass", oItem.getProductClass());			
				inXml.createChild(elePromiseLines, "PromiseLine", htPromiseLine);				
				iLineNo++;
			}
			
			// now iterate over items associated components/services
			int iComponent;
			if (YFSUtil.getDebug())
			{
				System.out.println ("Processing "+oItem.getComponentCount()+" Components for Item "+oItem.getItemID());
			}
			for (iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component	oComponent = oItem.getComponent (iComponent);
				
				// set transactional line id for this component
				if (oComponent.getIntegrationID().length() == 0)
					oComponent.setTransactionalLineId("Line"+iLineNo);
				else
					oComponent.setTransactionalLineId (oComponent.getIntegrationID());

				// if this is a service component				
				if (oComponent.getIsService())
				{
					// add service item							
					Hashtable<String, String> htServiceLine = new Hashtable<String, String>();
					//htServiceLine.put("ServiceQty", oComponent.getSvcQty());
					htServiceLine.put("RequiredQty", oComponent.getSvcQty());
					htServiceLine.put("ItemGroupCode", oComponent.getItemGroupCode());			
					htServiceLine.put("LineId", oComponent.getTransactionalLineId());					

					// add item information to order line			
					htServiceLine.put("ItemID", oComponent.getItemID ());
					htServiceLine.put("UnitOfMeasure", oComponent.getUOM());			
					inXml.createChild(elePromiseServiceLines, "PromiseServiceLine", htServiceLine);
					iLineNo++;					
					
					// if component is associated to the product line
					//if (oComponent.getIsAssociatedToProduct())

					// generate XML for ServiceAssociations on Promise element
					Hashtable<String, String> htServiceAssociation = new Hashtable<String, String>();
					htServiceAssociation.put("ProductLineId", oItem.getTransactionalLineId());
					htServiceAssociation.put("ProductQty", oItem.getQty());
					htServiceAssociation.put("ServiceLineId", oComponent.getTransactionalLineId());
					htServiceAssociation.put("ServiceQty", oComponent.getSvcQty());
					inXml.createChild (eleProductServiceAssocs, "ServiceAssociation", htServiceAssociation);

					// now add options if any loaded
					for (int iOption = 0; iOption < oItem.getOptionCount(); iOption++)
					{
						ShoppingCartItem.Option	oOption = oItem.getOption (iOption);
						if (oOption.getComponent() == oComponent)
						{
							// set transactional line id for this component
							if (oOption.getIntegrationID().length() == 0)
								oOption.setTransactionalLineId("Line"+iLineNo);
							else
								oOption.setTransactionalLineId (oOption.getIntegrationID());

							htServiceLine = new Hashtable<String, String>();
							htServiceLine.put("RequiredQty", oOption.getQty());
							htServiceLine.put("ItemGroupCode", oOption.getItemGroupCode());			
							htServiceLine.put("LineId", oOption.getTransactionalLineId());					

							// add item information to order line			
							htServiceLine.put("ItemID", oOption.getItemID ());
							htServiceLine.put("UnitOfMeasure", oOption.getUOM());			
							inXml.createChild(elePromiseServiceLines, "PromiseServiceLine", htServiceLine);
							iLineNo++;
					
							// generate XML for ServiceAssociations on Promise element
							htServiceAssociation = new Hashtable<String, String>();

							htServiceAssociation.put("ProductLineId", oItem.getTransactionalLineId());
							htServiceAssociation.put("ProductQty", oItem.getQty());
							htServiceAssociation.put("ServiceLineId", oOption.getTransactionalLineId());
							htServiceAssociation.put("ServiceQty", oOption.getQty());
							inXml.createChild (eleProductServiceAssocs, "ServiceAssociation", htServiceAssociation);
						}
					}					
				}
				else
				{					
					// create PromiseLine for kit component
					Hashtable<String, String> htPromiseLine = new Hashtable<String, String>();
					htPromiseLine.put("RequiredQty", oComponent.getQty());
					htPromiseLine.put("LineId", oComponent.getTransactionalLineId());
					htPromiseLine.put("ItemID", oComponent.getItemID ());
					htPromiseLine.put("UnitOfMeasure", oComponent.getUOM());			
					htPromiseLine.put("ProductClass", oComponent.getProductClass());			
					inXml.createChild(elePromiseLines, "PromiseLine", htPromiseLine);
					iLineNo++;
				}
			}
		}

		// if customer information provided 
		if (getCustomer() != null)
		{
			Customer oCustomer = getCustomer();
			
			// finish filling out address fields
			Hashtable<String, String> htShipToPersonInfo = new Hashtable<String, String>();
			htShipToPersonInfo.put ("ZipCode", oCustomer.getSTZip());
			htShipToPersonInfo.put ("Country", oCustomer.getSTCountry());
			inXml.createChild (elePromise, "ShipToAddress", htShipToPersonInfo);						
		}		
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input for findInventory API is ... ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		return inXml.getDocument();
	}

	public	void getOrderKeys () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = null;
		YFSXMLParser inXml = new YFSXMLParser();
		
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey());
		inXml.createRootElement ("Order", htOrder);		
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getOrderDetails() API:");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument ()));
		}
		docOutXML = api.getOrderDetails (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getOrderDetails() API:");
			System.out.println (YFSXMLUtil.getXMLString (docOutXML));
		}
		
		// now iterate over all orderlines and match to corresponding
		// item/component in ShoppingCart
		// now parse through the XML output document and load Yantra Order
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docOutXML);
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		
		// iterate over order lines in actual order
		for (Iterator<?> i = eleOrderLines.getChildren(); i.hasNext(); )
		{
			YFCElement	eleOrderLine = (YFCElement)i.next();
			String	sOrderLineKey = eleOrderLine.getAttribute ("OrderLineKey");
			String	sPrimeLineNo = eleOrderLine.getAttribute ("PrimeLineNo");

			// set order line key in corresponding item/component
			setOrderLineKey (sPrimeLineNo, sOrderLineKey);
		}
		return;
	}		

	public	Enumeration<Component> getOrderComponentsWithSchedules ()
	{
		if (m_vecSchedules.size() != 0)
		{
			return m_vecSchedules.elements();
		}
		return null;
	}
	
	public	Enumeration<Component> getComponentsRequiringScheduling()
	{
		return getComponentsRequiringScheduling (true);
	}
	
	public	Enumeration<Component> getComponentsRequiringScheduling(boolean bServiceAndDeliveriesOnly)
	{
		// if components requiring scheduling is not yet determined
		if (m_vecSchedules.size() == 0)
		{
			if (YFSUtil.getDebug())
				System.out.println ("Getting Components Requiring Scheduling");

			if (!bServiceAndDeliveriesOnly)
				getProductItems();
				
			// get deliverable items requiring scheduling
			getDeliverableItems ();
		
			// add servicable items requiring scheduling
			getServicableItems ();
		}
		return m_vecSchedules.elements();
	}

	protected	void	getProductItems ()
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iItem);
			if (YFSUtil.getDebug())
				System.out.println ("Finding Product Components for Item: "+oItem.getItemID());
			
			for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component oComponent = oItem.getComponent (iComponent);
				if (!oComponent.getIsService())
				{
					if (YFSUtil.getDebug())
						System.out.println ("Product Item Being Added to Schedulable Components: " + oComponent.getItemShortDesc());
					addSchedule (oComponent);
				}
			}		
		}
	}
	
	protected	void	getDeliverableItems ()
	{
		boolean	bHasServices = false;
		// before any order line containing an associated delivery 
		// service can be scheduled, the delivery itself needs to be 
		// scheduled.  Therefore the process for scheduling an order
		// is
		// 1) Schedule Product (Sourcing of Products)
		// 2) Schedule Deliveries (constrained to product delivery dates)
		// 3) Schedule Installation Services
		// The installation services will have a corresponding constraint
		// which will be the associated items delivery date scheduled
		// iterate over all lines/components
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iItem);
			if (YFSUtil.getDebug())
				System.out.println ("Finding Deliverable Components for Item: "+oItem.getItemID());
			
			if (oItem.getHasServices())
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					ShoppingCartItem.Component oComponent = oItem.getComponent (iComponent);
					if (oComponent.getIsDeliveryService())
					{
						if (YFSUtil.getDebug())
							System.out.println ("Delivery Service Being Added to Schedulable Components: " + oComponent.getItemShortDesc());
						bHasServices = true;
						addSchedule (oComponent);
					}
				}		
			}
			if (!bHasServices)
			{
				if (YFSUtil.getDebug())
					System.out.println ("Item Contains No Deliverable Components: "+oItem.getItemID());
			}
		}		
	}
	
	protected	void	getServicableItems ()
	{
		boolean	bHasServices = false;
		
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iItem);
			if (YFSUtil.getDebug())
				System.out.println ("Finding Servicable Components for Item: "+oItem.getItemID());
				
			if (oItem.getHasServices())
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					ShoppingCartItem.Component oComponent = oItem.getComponent (iComponent);
					if (oComponent.getIsProductService ())
					{
						if (YFSUtil.getDebug())
							System.out.println ("Product Service Being Added to Schedulable Components: " + oComponent.getItemShortDesc());
						bHasServices = true;
						addSchedule (oComponent);
					}
				}		
			}
			if (!bHasServices)
			{
				if (YFSUtil.getDebug())
					System.out.println ("Item Contains No Servicable Components: "+oItem.getItemID());
			}
		}
	}

	public	void	scheduleOrder () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = null;
		YFSXMLParser inXml = new YFSXMLParser();
		
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey ());
		htOrder.put ("IgnoreMinNotificationTime", "Y");
		htOrder.put ("IgnoreReleaseDate", "Y");
		htOrder.put ("ScheduleAndRelease", "N");		
		inXml.createRootElement ("ScheduleOrder", htOrder);
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to scheduleOrder() API:");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument ()));
		}

		// schedule order
		docOutXML = api.scheduleOrder (env, inXml.getDocument());
		String sOrder = YFSXMLUtil.getXMLString (docOutXML);

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from scheduleOrder() API:");
			System.out.println (sOrder);
		}
	
		return;
	}
			

	@SuppressWarnings("deprecation")
	public	void	scheduleDeliveryOrInstallation (ShoppingCartItem.Component oComponent) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		Document docOutXML = null;
		YFSXMLParser inXml = new YFSXMLParser();
		
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey ());
		htOrder.put ("Action", "MODIFY");
		htOrder.put ("Override", "Y");
				
		// create root order element
		Element eleOrder = inXml.createRootElement ("Order", htOrder);
		
		// create child order lines element
		Element	eleOrderLines = inXml.createChild (eleOrder, "OrderLines", null);

		// if component has been scheduled (date/time selected)
		if (oComponent.getComponentSchedule() != null)
		{
			ShoppingCartItem.ComponentSchedule oSchedule = oComponent.getComponentSchedule ();
			Hashtable<String, String> htOrderLine = new Hashtable<String, String>();
						
			htOrderLine.put ("OrderLineKey", oComponent.getOrderLineKey());
			htOrderLine.put ("Action", "Modify");
			htOrderLine.put ("ApptStatus", "CONFIRMED");
			htOrderLine.put ("PromisedApptStartDate", oSchedule.getSlotBegSelected().getString (YFCDate.ISO_DATETIME_FORMAT));
			htOrderLine.put ("PromisedApptEndDate", oSchedule.getSlotEndSelected().getString (YFCDate.ISO_DATETIME_FORMAT));
			inXml.createChild (eleOrderLines, "OrderLine", htOrderLine);
		}			
		

		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to changeOrder() API:");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument ()));
		}
		// change order
		docOutXML = api.changeOrder (env, inXml.getDocument());
		String sOrder = YFSXMLUtil.getXMLString (docOutXML);

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from changeOrder() API:");
			System.out.println (sOrder);
		}	

		// now schedule order with confirmed appt date to reserve that date
		scheduleOrder();
		
		return;
	}

	protected	void setOrderLineKey (String sPrimeLineNo, String sOrderLineKey)
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
		{
			ShoppingCartItem	oItem = getShoppingCartItem(iItem);
			if (!oItem.getTransactionalLineId ().equals ("Line"+sPrimeLineNo))
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					ShoppingCartItem.Component oComponent = oItem.getComponent (iComponent);
					if (oComponent.getPrimeLineNo().equals (sPrimeLineNo))
					{
						oComponent.setOrderLineKey (sOrderLineKey);
						if (YFSUtil.getDebug())
						{
							System.out.println ("Component "+oComponent.getItemID()+ " of Item "+oItem.getItemID ()+" assigned OrderLineKey of "+sOrderLineKey);
						}
						return;
					}
				}		
			}
			else
			{
				oItem.setOrderLineKey (sOrderLineKey);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Item "+oItem.getItemID ()+" assigned OrderLineKey of "+sOrderLineKey);
				}
				return;
			}	
		}	
	}
	
	
	// protected member data for shopping cart	
	protected	String		m_sOrderHeaderKey;
	protected	String		m_sIntegrationID;
	protected	String		m_sOrganizationCode;
	protected	String		m_sCartSubTotal;
	protected	String		m_sCartTotal;
	protected	String		m_sCartShipping;
	protected	String		m_sCartDiscount;
	protected	String		m_sCartTaxes;
	protected	String		m_sOrderName;
	protected	String		m_sOrderSource;
	protected	String		m_sEnteredBy;
	protected	String		m_sOrderType;
	protected	String		m_sOrderNo;
	protected	String		m_sSCAC;
	protected	String		m_sCarrierService;
	protected	String		m_sPriceProgram;
	protected	String		m_sLocaleCode;
	protected	String		m_sRequestedDate;
	protected	String		m_sOrderDate;
	protected	String		m_sSearchField1Name;
	protected	String		m_sSearchField2Name;
	protected	String		m_sSearchField1Value;
	protected	String		m_sSearchField2Value;
	protected	String		m_sDiscountCategory;
	protected	String		m_sShippingCategory;
	protected	String		m_sTaxName;
	
	protected	Customer	m_oCustomer;
	protected	Vendor		m_oVendor;
	protected	Vector<ShoppingCartItem>		m_vecShoppingCartItems;	
	protected	Vector<Component>		m_vecSchedules;
	protected	boolean		m_bLocaleInitialized;
	protected	boolean		m_bLinesPriced;	
}

