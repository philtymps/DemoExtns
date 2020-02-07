/**
  * POSCart.java
  *
  **/

// PACKAGE
package com.custom.yantra.pos;

import java.util.*;
import com.custom.yantra.pos.POSCartItem.Component;
import com.custom.yantra.util.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;
import com.yantra.yfc.util.*;
import java.math.*;
import java.io.Serializable;

/*
import com.custom.yantra.customer.*;
import com.custom.yantra.vendor.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
*/
@SuppressWarnings("serial")
public class POSCart implements Serializable
{
	public 		static final String		CALENDAR_MONTHS[] = 
	{"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
	 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	public		static final String		CALENDAR_DAYS[] = 
	{ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };


    public POSCart()
    {		
		m_sOrderHeaderKey = "";
		m_sCartTotal = "0.00";
		m_sCartDiscount = "0.00";
		m_sCartShipping = "0.00";
		m_sCartTaxes = "0.00";
		m_sOrderNo = "";
		m_sOrderName = "";
		m_sOrderType = "";
		m_sRequestedDate = "";
		m_sPriceProgram = "";
		m_sLocaleCode = "en_US_EST";

		m_sSearchField1Name = "SearchCriteria1";
		m_sSearchField2Name = "SearchCriteria2";
		m_sSearchField1Value = "";
		m_sSearchField2Value = "";
		m_sDiscountCategory = "";
		m_sShippingCategory = "";
		m_sTaxName = "";
		m_vecPOSCartItems = new Vector<POSCartItem> ();
		m_vecSchedules = new Vector<Component>();
		m_bLinesPriced = true;
    }

	// cart 	
	public		String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public		void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
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
	public		String	getOrderType () { return m_sOrderType; }
	public		void	setOrderType (String sOrderType) { m_sOrderType = sOrderType; }
	public		String	getOrderNumber () { return m_sOrderNo; }
	public		void	setOrderNumber (String sOrderNo) { m_sOrderNo = sOrderNo; }
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
	
	// items
	public		POSCartItem	getPOSCartItem (int iItem)	{ return (POSCartItem)m_vecPOSCartItems.elementAt (iItem); }
	public		int					getPOSCartItemCount () { return m_vecPOSCartItems.size(); }

	// customer Org info
	public		String				getOrganizationCode() { return m_sOrganizationCode; }
	public		void				setOrganizationCode(String sOrganizationCode) { m_sOrganizationCode = sOrganizationCode; }
	public		POSCustomer			getCustomer () { return m_oCustomer; }
	public		void				setCustomer (POSCustomer oCustomer) { m_oCustomer = oCustomer; }
	public		POSVendor			getVendor () { return m_oVendor; }
	public		void				setVendor (POSVendor oVendor) { m_oVendor = oVendor; }

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

	public	POSCartItem	createNewPOSCartItem()
	{
		return new POSCartItem(this);
	}

	public	void	Reset ()
	{
		m_sCartTotal = "0.00";
		m_sCartDiscount = "0.00";
		m_sCartShipping = "0.00";
		m_sCartTaxes = "0.00";
		m_sOrderNo = "";
		m_sOrderName = "";
		m_sOrderType = "";
		m_sRequestedDate = "";
		m_sPriceProgram = "";
		m_sLocaleCode = "en_US_EST";

		m_sSearchField1Name = "SearchCriteria1";
		m_sSearchField2Name = "SearchCriteria2";
		m_sSearchField1Value = "";
		m_sSearchField2Value = "";

		// reset all items in cart
		for (int iItem = 0; iItem < getPOSCartItemCount(); iItem++)
			getPOSCartItem(iItem).Reset();

		// remove all items in the cart	
		m_vecPOSCartItems.clear();
		m_vecSchedules.clear();
	}

	public	void	addSchedule (POSCartItem.Component oComponent)
	{
		oComponent.createNewComponentSchedule ();
		m_vecSchedules.add (oComponent);
	}

				
	public	void	addPOSCartItem (POSCartItem oSCItem) throws Exception
	{
		addPOSCartItem (oSCItem, true);
	}

	public	void	addPOSCartItem (POSCartItem oSCItem, boolean bLoadDetailsFromYantra) throws Exception
	{
		if (bLoadDetailsFromYantra)
		{
			oSCItem.loadItem();
			oSCItem.loadShippingAndDiscountsForItem();
		}
		m_vecPOSCartItems.add (oSCItem);
	}
	
	public	void	addPOSCartItem (String sItemID, String sQty, String sUOM, String sPC) throws Exception
	{
		POSCartItem	oSCItem = createNewPOSCartItem();
		oSCItem.setItemID(sItemID);
		oSCItem.setQty (sQty);
		oSCItem.setUOM (sUOM);
		oSCItem.setProductClass (sPC);
		addPOSCartItem (oSCItem);
	}
	
	public	void	addPOSCartItem (String sItemID, String sQty, String sUOM, String sPC, boolean bLoadDetailsFromYantra) throws Exception
	{
		POSCartItem	oSCItem = createNewPOSCartItem();
		oSCItem.setItemID(sItemID);
		oSCItem.setQty (sQty);
		oSCItem.setUOM (sUOM);
		oSCItem.setProductClass (sPC);
		addPOSCartItem (oSCItem, bLoadDetailsFromYantra);
	}
	
	public	Document	createOrder (boolean bDraftOrder) throws Exception
	{

YFSXMLParser inXml = new YFSXMLParser();
		
		// create order
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("EnterpriseCode", getOrganizationCode ());
		htOrder.put ("SellerOrganizationCode", getVendor().getSellerOrganizationCode ());
		htOrder.put ("BuyerOrganizationCode", getCustomer().getBuyerOrganizationCode ());
		if (bDraftOrder)
			htOrder.put ("DraftOrderFlag", "Y");
		htOrder.put ("PriceProgramName", getPriceProgram());
		htOrder.put ("PaymentStatus", "AUTHORIZED");
		htOrder.put ("OrderType", getOrderType());
		htOrder.put ("OrderName", getOrderName());
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
		Element eleHeaderCharges = inXml.createChild (eleOrder, "HeaderCharges", null);

		// if shipping charges applicable
		if (getShippingCategory().length() > 0)
		{
			Hashtable<String, String> htHeaderCharge = new Hashtable<String, String> ();
			htHeaderCharge.put ("ChargeAmount", getCartShipping());
			htHeaderCharge.put ("ChargeCategory", "Shipping");
			inXml.createChild (eleHeaderCharges, "HeaderCharge", htHeaderCharge);
		}
		
		// if discounts applicatble
		if (getDiscountCategory().length() > 0)
		{
			Hashtable<String, String> htHeaderCharge = new Hashtable<String, String> ();
			htHeaderCharge.put ("ChargeAmount", getCartDiscount());
			htHeaderCharge.put ("ChargeCategory", "Discount");
			inXml.createChild (eleHeaderCharges, "HeaderCharge", htHeaderCharge);
		}

		// if taxes applicable
		Element eleHeaderTaxes = inXml.createChild (eleOrder, "HeaderTaxes", null);
		if (getTaxName().length() > 0)
		{
			Hashtable<String, String> htHeaderTax = new Hashtable<String, String>();
			htHeaderTax.put ("TaxName", "Tax");
			htHeaderTax.put ("Tax", getCartTaxes());
			inXml.createChild (eleHeaderTaxes, "HeaderTax", htHeaderTax);
		}
		
		// generate XML for OrderLines
		Element eleOrderLines = inXml.createChild (eleOrder, "OrderLines", null);
		int	iLineNo = 1;
		
		// iterate over shopping cart items
		for (int iEle = 0; iEle < getPOSCartItemCount(); iEle++)
		{
			POSCartItem	oItem = getPOSCartItem(iEle);

			// if the item is not a kit then add the item to order
			if (!oItem.getIsKit())
			{
				// set transactional line id for this item
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

				// add item information to order line			
				Hashtable<String, String> htItem = new Hashtable<String, String> ();
				htItem.put("ItemID", oItem.getItemID ());
				htItem.put("UnitOfMeasure", oItem.getUOM());			
				htItem.put("ProductClass", oItem.getProductClass());			
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
				bdUnitPrice.setScale (2);
				htPriceInfo.put ("UnitPrice", bdUnitPrice.toString());
				inXml.createChild(eleOrderLine, "LinePriceInfo", htPriceInfo);				
				iLineNo++;
			}
			
			Element eleProductServiceAssocs = inXml.createChild (eleOrder, "ProductServiceAssocs", null);
			
			// now iterate over items associated components/services
			int iComponent;
			if (YFSUtil.getDebug())
			{
				System.out.println ("Processing "+oItem.getComponentCount()+" Components for Item "+oItem.getItemID());
			}
			for (iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
			{
				POSCartItem.Component	oComponent = oItem.getComponent (iComponent);
				
				// set transactional line id for this component
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

					// add item information to order line			
					Hashtable<String, String> htServiceItem = new Hashtable<String, String> ();
					htServiceItem.put("ItemID", oComponent.getItemID ());
					htServiceItem.put("UnitOfMeasure", oComponent.getUOM());			
					inXml.createChild(eleServiceLine, "Item", htServiceItem);
					
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
					bdUnitPrice.setScale (2);
					htPriceInfo.put ("UnitPrice", bdUnitPrice.toString());
					inXml.createChild(eleServiceLine, "LinePriceInfo", htPriceInfo);				

					// if component is associated to the product line
					if (oComponent.getIsAssociatedToProduct())
					{
						new Hashtable<Object, Object>();
						Element eleProductServiceAssoc = inXml.createChild (eleProductServiceAssocs, "ProductServiceAssoc", null);

						Hashtable<String, String> htProduct = new Hashtable<String, String>();
						htProduct.put("TransactionalLineId", oItem.getTransactionalLineId());
						inXml.createChild (eleProductServiceAssoc, "ProductLine", htProduct);

						new Hashtable<Object, Object>();
						htProduct.put("TransactionalLineId", oComponent.getTransactionalLineId());
						inXml.createChild (eleProductServiceAssoc, "ServiceLine", htProduct);
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
					inXml.createChild(eleOrderLine, "Item", htItem);

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
					htPriceInfo.put ("UnitPrice", bdUnitPrice.toString());
					inXml.createChild(eleOrderLine, "LinePriceInfo", htPriceInfo);				
					iLineNo++;
				}
			}
		}

		// if customer information provided 
		if (getCustomer() != null)
		{
			// finish filling out address fields
			Hashtable<String, String> htBillToPersonInfo = new Hashtable<String, String>();
			POSCustomer	oCustomer = getCustomer();
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
		}		
		
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from create order API is ... ");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		return inXml.getDocument();
	}

	public	void createOrder (Document docIn) throws Exception
	{		
		String	sOrderConfirmation = YFSXMLUtil.getXMLString(docIn);
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
		getOrderKeys (docIn);
		return;
	}

	// Pass the Order Details for the Order to Compute Order Keys	
	protected	void getOrderKeys (Document docIn) throws Exception
	{
		YFSXMLUtil.getXMLString (docIn);
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
				
		Iterator<?> iOrderLines = eleOrderLines.getChildren();
		// iterate over order lines in actual order
		while (iOrderLines.hasNext())
		{
			YFCElement 	eleOrderLine = (YFCElement)iOrderLines.next();
			String	sOrderLineKey = eleOrderLine.getAttribute ("OrderLineKey");
			String	sPrimeLineNo = eleOrderLine.getAttribute ("PrimeLineNo");

			// set order line key in corresponding item/component
			setOrderLineKey (sPrimeLineNo, sOrderLineKey);
		}
		return;
	}		

	public	Enumeration<Component> getComponentsRequiringScheduling()
	{
		// if components requiring scheduling is not yet determined
		if (m_vecSchedules.size() == 0)
		{
			if (YFSUtil.getDebug())
				System.out.println ("Getting Components Requiring Scheduling");
				
			// get deliverable items requiring scheduling
			getDeliverableItems ();
		
			// add servicable items requiring scheduling
			getServicableItems ();
		}
		return m_vecSchedules.elements();
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
		for (int iItem = 0; iItem < getPOSCartItemCount(); iItem++)
		{
			POSCartItem	oItem = getPOSCartItem(iItem);
			if (YFSUtil.getDebug())
				System.out.println ("Finding Deliverable Components for Item: "+oItem.getItemID());
			
			if (oItem.getHasServices())
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					POSCartItem.Component oComponent = oItem.getComponent (iComponent);
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
		
		for (int iItem = 0; iItem < getPOSCartItemCount(); iItem++)
		{
			POSCartItem	oItem = getPOSCartItem(iItem);
			if (YFSUtil.getDebug())
				System.out.println ("Finding Servicable Components for Item: "+oItem.getItemID());
				
			if (oItem.getHasServices())
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					POSCartItem.Component oComponent = oItem.getComponent (iComponent);
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

	public	Document	scheduleOrder () throws Exception
	{
		YFSXMLParser inXml = new YFSXMLParser();
		
		Hashtable<String, String>	htOrder = new Hashtable<String, String>();
		htOrder.put ("OrderHeaderKey", getOrderHeaderKey ());
		htOrder.put ("IgnoreMinNotificationTime", "Y");
		htOrder.put ("IgnoreReleaseDate", "Y");
		htOrder.put ("ScheduleAndRelease", "N");		
		inXml.createRootElement ("ScheduleOrder", htOrder);
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output From scheduleOrder() API:");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument ()));
		}
		return inXml.getDocument();

/*
		// schedule order
		docOutXML = api.scheduleOrder (env, inXml.getDocument());
		String sOrder = YFSXMLUtil.getXMLString (docOutXML);

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from scheduleOrder() API:");
			System.out.println (sOrder);
		}
	
		return;
*/
	}
			
	@SuppressWarnings("deprecation")
	public	Document	scheduleDeliveryOrInstallation (POSCartItem.Component oComponent) throws Exception
	{
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
			POSCartItem.ComponentSchedule oSchedule = oComponent.getComponentSchedule ();
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
		return inXml.getDocument();
/*

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
*/
	}

	protected	void setOrderLineKey (String sPrimeLineNo, String sOrderLineKey)
	{
		for (int iItem = 0; iItem < getPOSCartItemCount(); iItem++)
		{
			POSCartItem	oItem = getPOSCartItem(iItem);
			if (!oItem.getTransactionalLineId ().equals ("Line"+sPrimeLineNo))
			{
				for (int iComponent = 0; iComponent < oItem.getComponentCount(); iComponent++)
				{
					POSCartItem.Component oComponent = oItem.getComponent (iComponent);
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
	protected	String		m_sOrganizationCode;
	protected	String		m_sCartTotal;
	protected	String		m_sCartShipping;
	protected	String		m_sCartDiscount;
	protected	String		m_sCartTaxes;
	protected	String		m_sOrderName;
	protected	String		m_sOrderType;
	protected	String		m_sOrderNo;
	protected	String		m_sPriceProgram;
	protected	String		m_sLocaleCode;
	protected	String		m_sRequestedDate;
	protected	String		m_sSearchField1Name;
	protected	String		m_sSearchField2Name;
	protected	String		m_sSearchField1Value;
	protected	String		m_sSearchField2Value;
	protected	String		m_sDiscountCategory;
	protected	String		m_sShippingCategory;
	protected	String		m_sTaxName;
	
	protected	POSCustomer	m_oCustomer;
	protected	POSVendor	m_oVendor;
	protected	Vector<POSCartItem>		m_vecPOSCartItems;	
	protected	Vector<Component>		m_vecSchedules;
	protected	boolean		m_bLocaleInitialized;
	protected	boolean		m_bLinesPriced;	
}

