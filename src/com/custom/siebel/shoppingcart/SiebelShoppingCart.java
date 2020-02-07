/**
  * SiebelShoppingCart.java
  *
  **/

// PACKAGE
package com.custom.siebel.shoppingcart;

import com.custom.yantra.shoppingcart.*;
import com.custom.yantra.customer.*;
import com.custom.yantra.vendor.*;
import com.custom.yantra.xmlwrapper.*;
import com.custom.yantra.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfc.util.*;
import com.yantra.yfc.dom.*;

import org.w3c.dom.*;
import java.util.*;

@SuppressWarnings("serial")
public class SiebelShoppingCart extends ShoppingCart
{
    public SiebelShoppingCart()
    {
		super();
    }

	public	Document	createOrder (YFSEnvironment env, Document inOrder) throws Exception
	{
		Document	docOrder = createOrderDoc (env, inOrder);
		YIFApi		api = YFSUtil.getYIFApi();
		return api.createOrder (env, docOrder);
	}		
	
	public	Document	createOrderDoc (YFSEnvironment env, Document inOrder) throws Exception
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SiebelShoppingCart.createOrder()-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (inOrder));
		}
		YFSCreateOrderInputDoc	oInOrder = new YFSCreateOrderInputDoc (YFSXMLUtil.getXMLString (inOrder));
		YFSCreateOrderInputDoc.OrderLines oInOrderLines = oInOrder.getOrder().getOrderLines();

		// initialize the Bean environment and api
		YFSUtil.setYFSEnv (env);
		YIFApi	api = YFSUtil.getYIFApi (env != null ? true : false);
		env = YFSUtil.getYFSEnv ();
		YFCLocaleUtils.init("en_US_EST");

		// initialize the shopping cart with order header attributes
		initializeShoppingCart (oInOrder);								
		if (oInOrderLines != null)
		{
			Enumeration<?>	enumInOrderLines = oInOrderLines.getOrderLineList();
			while (enumInOrderLines.hasMoreElements ())
			{
				YFSCreateOrderInputDoc.OrderLine oInOrderLine = (YFSCreateOrderInputDoc.OrderLine)enumInOrderLines.nextElement();
				YFSCreateOrderInputDoc.BundleParentLine oInOrderLineParent = oInOrderLine.getBundleParentLine ();
								
				String sItemID = oInOrderLine.getItem().getAttribute ("ItemID");
				String sQty = oInOrderLine.getAttribute ("OrderedQty");
				String sUOM = oInOrderLine.getItem().getAttribute ("UnitOfMeasure");
				String sPC = oInOrderLine.getItem().getAttribute ("ProductClass");
				String sTLID = oInOrderLine.getAttribute ("TransactionalLineId");
				String sParentBundle = "";
				
				if (oInOrderLineParent != null)
					sParentBundle = oInOrderLineParent.getAttribute ("TransactionalLineId");
			
				// now call getItemList API to get item based on just item id
				YFSXMLParser	inXml = new YFSXMLParser ();
				Hashtable<String, String>		htItem = new Hashtable<String, String>();
				htItem.put ("ItemID", sItemID);
				htItem.put ("OrganizationCode", getOrganizationCode());
				inXml.createRootElement ("Item", htItem);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getItemList()");
					System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
				}
				YFCDocument docItemList = YFCDocument.getDocumentFor (api.getItemList (env, inXml.getDocument()));
				YFCElement	eleItemList = docItemList.getDocumentElement ();
						
				// now parse through the XML output document and load description
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getItemList()");
					System.out.println (docItemList.getString());
				}
	
				// don't add items already in the cart			
				boolean bAddItem = false;
				if (eleItemList != null)
				{
					Iterator<?>	iItemList = eleItemList.getChildren ();
					if (iItemList.hasNext ())
					{
						YFCElement eleItem = (YFCElement)iItemList.next ();
						YFCElement elePrimaryInformation = eleItem.getChildElement ("PrimaryInformation");
						String		sItemGroupCode = eleItem.getAttribute ("ItemGroupCode");
						String		sRequiresProdAssociation = elePrimaryInformation.getAttribute ("RequiresProdAssociation");

						// use UOM/PC from Yantra
						sUOM = eleItem.getAttribute ("UnitOfMeasure");
						sPC = elePrimaryInformation.getAttribute ("DefaultProductClass");
					
						if (sItemGroupCode.equals ("PROD"))
							bAddItem = true;
//?????
						else if((sItemGroupCode.equals ("PS") || sItemGroupCode.equals ("DS")) && sRequiresProdAssociation.equals ("N"))
							bAddItem = true;
					}
					else
					{
						bAddItem = false;
						if (YFSUtil.getDebug())
							System.out.println ("WARNING-Siebel Part No "+sItemID+" Not Configured in Yantra");
					}
				}
				else
				{
					bAddItem = false;
					if (YFSUtil.getDebug())
						System.out.println ("WARNING-Siebel Part No "+sItemID+" Not Configured in Yantra");
				}	
				if (bAddItem && !IsItemInCart(sItemID, sParentBundle))
				{
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Adding Product/Service to Siebel Order");
						System.out.println ("\tItemID="+sItemID);
						System.out.println ("\t  TLID="+sTLID);
						System.out.println ("\tParent="+sParentBundle);
						System.out.println ("\t   Qty="+sQty);
						System.out.println ("\t   UOM="+sUOM);
						System.out.println ("\t    PC="+sPC);
					}						
					addShoppingCartItem (oInOrder, oInOrderLine, sItemID, sQty, sUOM, sPC);
				}
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Cart Contents before Finalize");
			dumpCart();
		}
		
		// finalize the cart
		finalizeShoppingCart(oInOrder);		

		if (YFSUtil.getDebug())
		{
			System.out.println ("Cart Contents after Finalize");
			dumpCart();
		}
		return createOrderDoc (false);
	}
	
	public	Document findInventory (YFSEnvironment env, Document inPromise) throws Exception
	{
		inPromise = findInventoryDoc(env, inPromise);
		YIFApi		api = YFSUtil.getYIFApi();
		return api.findInventory (env, inPromise);
	}

	public	Document findInventoryDoc (YFSEnvironment env, Document inPromise) throws Exception
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SiebelShoppingCart.findInventoryDoc()-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (inPromise));
		}
		YFSFindInventoryInputDoc 	oInPromise = new YFSFindInventoryInputDoc (YFSXMLUtil.getXMLString (inPromise));
		YFSFindInventoryInputDoc.PromiseLines oInPromiseLines = oInPromise.getPromise().getPromiseLines();

		// initialize the Bean environment and api
		YFSUtil.setYFSEnv (env);
		YIFApi	api = YFSUtil.getYIFApi (env != null ? true : false);
		env = YFSUtil.getYFSEnv ();
		YFCLocaleUtils.init("en_US_EST");

		// initialize the shopping cart with order header attributes
		initializeShoppingCart (oInPromise);								
		if (oInPromiseLines != null)
		{
			Enumeration<?>	enumInPromiseLines = oInPromiseLines.getPromiseLineList();
			while (enumInPromiseLines.hasMoreElements ())
			{
				YFSFindInventoryInputDoc.PromiseLine oInPromiseLine = (YFSFindInventoryInputDoc.PromiseLine)enumInPromiseLines.nextElement();
				YFSFindInventoryInputDoc.BundleParentLine oInBundleParent = oInPromiseLine.getBundleParentLine();
				
				String sItemID = oInPromiseLine.getAttribute ("ItemID");
				String sTLID = oInPromiseLine.getAttribute ("LineId");
				String sQty = oInPromiseLine.getAttribute ("RequiredQty");
				String sUOM = oInPromiseLine.getAttribute ("UnitOfMeasure");
				String sPC = oInPromiseLine.getAttribute ("ProductClass");
				String sParentBundle = "";
				
				if (oInBundleParent != null)
					sParentBundle = oInPromiseLine.getAttribute ("LineId");
				
							
				// now call getItemList API to get item based on just item id
				YFSXMLParser	inXml = new YFSXMLParser ();
				Hashtable<String, String>		htItem = new Hashtable<String, String>();
				htItem.put ("ItemID", sItemID);
				htItem.put ("OrganizationCode", getOrganizationCode());
				inXml.createRootElement ("Item", htItem);
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getItemList()");
					System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
				}
				YFCDocument docItemList = YFCDocument.getDocumentFor (api.getItemList (env, inXml.getDocument()));
				YFCElement	eleItemList = docItemList.getDocumentElement ();		
				// now parse through the XML output document and load description
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getItemList()");
					System.out.println (docItemList.getString());
				}
	
				// don't add items already in the cart			
				boolean bAddItem = false;
				if (eleItemList != null)
				{
					Iterator<?> iItemList = eleItemList.getChildren ();
					if (iItemList.hasNext ())
					{
						YFCElement	eleItem = (YFCElement)iItemList.next ();
						YFCElement	elePrimaryInformation = eleItem.getChildElement ("PrimaryInformation");
						String		sItemGroupCode = eleItem.getAttribute ("ItemGroupCode");
						String		sRequiresProdAssociation = elePrimaryInformation.getAttribute ("RequiresProdAssociation");

						// use UOM/PC from Yantra
						sUOM = eleItem.getAttribute ("UnitOfMeasure");
						sPC = elePrimaryInformation.getAttribute ("DefaultProductClass");
					
						if (sItemGroupCode.equals ("PROD"))
							bAddItem = true;
///???????????????
						else if((sItemGroupCode.equals ("PS") || sItemGroupCode.equals ("DS")) && sRequiresProdAssociation.equals ("N"))
							bAddItem = true;
					}
					else
					{
						bAddItem = false;
						if (YFSUtil.getDebug())
							System.out.println ("WARNING-Siebel Part No "+sItemID+" Not Configured in Yantra");
					}
				}
				else
				{
					bAddItem = false;
					if (YFSUtil.getDebug())
						System.out.println ("WARNING-Siebel Part No "+sItemID+" Not Configured in Yantra");
				}	
				if (bAddItem && !IsItemInCart(sItemID, sParentBundle))
				{
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Adding Product/Service to Siebel Order");
						System.out.println ("\tItemID="+sItemID);
						System.out.println ("\t  TLID="+sTLID);
						System.out.println ("\tParent="+sParentBundle);
						System.out.println ("\t   Qty="+sQty);
						System.out.println ("\t   UOM="+sUOM);
						System.out.println ("\t    PC="+sPC);
					}						
					addShoppingCartItem (oInPromise, oInPromiseLine, sItemID, sQty, sUOM, sPC);					
				}
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Cart Contents before Finalize");
			dumpCart();
		}
		
		// finalize the cart
		finalizeShoppingCart(oInPromise);		

		if (YFSUtil.getDebug())
		{
			System.out.println ("Cart Contents after Finalize");
			dumpCart();
		}
		return findInventoryDoc (-1);
	}

	public Document getOrderDetails (YFSEnvironment env, Document inOrder) throws Exception
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SiebelShoppingCart.getOrderDetails()-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (inOrder));
		}
		// initialize the Bean environment and api
		YFSUtil.setYFSEnv (env);
		YIFApi	api = YFSUtil.getYIFApi (env != null ? true : false);
		env = YFSUtil.getYFSEnv ();

		// call getOrderDetails for the give order document
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to getOrderDetails-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (inOrder));
		}
		env.setApiTemplate("getOrderDetails", "template/api/extn/getOrderDetails_Siebel.xml");
		YFCDocument docOut = YFCDocument.getDocumentFor (api.getOrderDetails (env, inOrder));
		env.clearApiTemplate ("getOrderDetails");

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getOrderDetails-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (docOut.getDocument()));
		}

		// we need to move the order line options into seperate order lines for Siebel
		YFCElement eleOrder = docOut.getDocumentElement();
		YFCElement eleExtn = eleOrder.getChildElement ("Extn");
		if (eleExtn != null)
		{
			eleOrder.setAttribute ("CustomerPONo", eleExtn.getAttribute ("ExtnSiebelIntegrationId"));
			eleOrder.removeChild(eleExtn);
		}
		YFCElement eleOrderLines = eleOrder.getChildElement("OrderLines");
		if (eleOrderLines != null)
		{
			for (Iterator<?> iOrderLine = eleOrderLines.getChildren(); iOrderLine.hasNext(); )
			{
				YFCElement eleOrderLine = (YFCElement)iOrderLine.next();
				eleExtn = eleOrderLine.getChildElement ("Extn");
				if (eleExtn != null)
				{
					eleOrderLine.setAttribute ("CustomerLinePONo", eleExtn.getAttribute ("ExtnSiebelIntegrationId"));
					eleOrderLine.removeChild(eleExtn);
				}
				YFCElement eleOrderLineOptions = eleOrderLine.getChildElement ("OrderLineOptions");
				if (eleOrderLineOptions != null)
				{
					for (Iterator<?> iOrderLineOptions = eleOrderLineOptions.getChildren(); iOrderLineOptions.hasNext(); )
					{
						YFCElement	eleOrderLineOption = (YFCElement)iOrderLineOptions.next();
						eleExtn = eleOrderLineOption.getChildElement ("Extn");
						if (eleExtn != null)
						{
							YFCElement	eleNewOrderLine = eleOrderLines.createChild ("OrderLine");
							YFCElement	eleNewItem = eleNewOrderLine.createChild ("Item");
							eleNewItem.setAttribute("ItemID", eleOrderLineOption.getAttribute ("OptionItemID"));
							eleNewItem.setAttribute("UnitOfMeasure", eleOrderLineOption.getAttribute ("OptionUOM"));
							eleNewOrderLine.setAttribute("OrderedQty", eleOrderLineOption.getAttribute ("Quantity"));
							eleNewOrderLine.setAttribute("MaxLineStatus", eleOrderLine.getAttribute ("MaxLineStatus"));
							eleNewOrderLine.setAttribute("MaxLineStatusDesc", eleOrderLine.getAttribute ("MaxLineStatusDesc"));
							eleNewOrderLine.setAttribute("CustomerLinePONo", eleExtn.getAttribute("ExtnSiebelIntegrationId"));	
							eleOrderLineOption.removeChild (eleExtn);
						}
					}
				}
			}
		}
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting SiebelShoppingCart.getOrderDetails-XML Document:");
			System.out.println (YFSXMLUtil.getXMLString (docOut.getDocument()));
		}
		return docOut.getDocument();
	}

	protected	void dumpCart ()
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Cart Contents:");
			for (int iItem = 0; iItem < getShoppingCartItemCount(); iItem++)
			{
				ShoppingCartItem	oSCItem = getShoppingCartItem(iItem);
				System.out.println ("Item: "+oSCItem.getItemID());				
				System.out.println ("TLID: "+oSCItem.getTransactionalLineId ());				
				for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
				{
					ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
				
					System.out.println ("\t Component:");
					System.out.println ("\t\tItemID="+oComponent.getItemID());
					System.out.println ("\t\t  TLID="+oComponent.getTransactionalLineId());
					System.out.println ("\t\tParent="+oComponent.getParentBundle());
					System.out.println ("\t\t   Qty="+ (oComponent.getIsService() || oComponent.getIsOption() ? oComponent.getQty() : oComponent.getSvcQty()));
					System.out.println ("\t\t   UOM="+oComponent.getUOM());
					if (oComponent.getItemGroupCode().equals("PROD"))
						System.out.println ("\t\t    PC="+oComponent.getProductClass());	
				}
				for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
				{
					ShoppingCartItem.Option	oOption = oSCItem.getOption (iOption);
				
					System.out.println ("\t Option:");
					System.out.println ("\t\tItemID=" + oOption.getItemID());
					System.out.println ("\t\t  TLID=" + oOption.getTransactionalLineId());
					System.out.println ("\t\tParent="+oOption.getParentBundle());
					System.out.println ("\t\t   Qty=" + oOption.getQty());
					System.out.println ("\t\t   UOM=" + oOption.getUOM());
				}
			}
		}						
	}

	@SuppressWarnings("deprecation")
	protected	void initializeShoppingCart (YFSCreateOrderInputDoc oInOrderDoc) throws Exception
	{
		Reset();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SiebelShoppingCart.initializeShoppingCart()");
			if(oInOrderDoc.getOrder() == null)
			{
				System.out.println ("ERROR - Order Element not Found!");
			}
		}
		YFSCreateOrderInputDoc.Order oInOrder = oInOrderDoc.getOrder();
		setCustomer (new Customer());
		setVendor (new Vendor());
		
		setOrganizationCode (oInOrder.getAttribute ("EnterpriseCode"));
		// look for B2B then B2C customer if that fails.
		try {
			getCustomer().getBuyerDetails(getOrganizationCode(),oInOrder.getAttribute ("BuyerOrganizationCode"));
		} catch (Exception e) {
			getCustomer().getCustomerDetails(getOrganizationCode(),oInOrder.getAttribute ("BuyerOrganizationCode"));
		}			
		getVendor().setOrganizationCode(getOrganizationCode());
		getVendor().setSellerOrganizationCode(oInOrder.getAttribute ("SellerOrganizationCode"));
		setRequestedDate (oInOrder.getAttribute ("ReqDeliveryDate"));
		setOrderNumber (oInOrder.getAttribute ("OrderNo"));
		setOrderType (oInOrder.getAttribute ("OrderType"));
		setOrderSource ("SIEBEL");
		setOrderSCAC   (oInOrder.getAttribute("SCAC"));
		setOrderCarrierService (oInOrder.getAttribute("CarrierServiceCode"));
		setEnteredBy (oInOrder.getAttribute ("EnteredBy"));
		setIntegrationID (oInOrder.getAttribute ("CustomerPONo"));
		setIsLinesPriced(true);
		// if requested date passed it's in XML format.  Convert to locale format		
		if (getRequestedDate().length() > 0)
		{
			YFCDate dtReqDate = new YFCDate (getRequestedDate(), "yyyy-MM-dd'T'hh:mm:ss'Z'", true);
			setRequestedDate(dtReqDate.getString (YFCLocale.getDefaultLocale(), false));
		}
		else
			setRequestedDate(new YFCDate ().getString (YFCLocale.getDefaultLocale(), false));

		if (YFSUtil.getDebug ())

		{
			System.out.println ("Header Initialized");
		}
		YFSCreateOrderInputDoc.HeaderCharges	oHeaderCharges = oInOrder.getHeaderCharges ();
		if (oHeaderCharges != null)
		{
			Enumeration<?>	enumHeaderCharges = oHeaderCharges.getHeaderChargeList();
			while (enumHeaderCharges.hasMoreElements ())
			{
				YFSCreateOrderInputDoc.HeaderCharge oHeaderCharge = (YFSCreateOrderInputDoc.HeaderCharge)enumHeaderCharges.nextElement();
				if (oHeaderCharge.getAttribute("ChargeCategory").equals("Shipping"))
				{
					setCartShipping (oHeaderCharge.getAttribute ("ChargeAmount"));
				}
				else if (oHeaderCharge.getAttribute ("ChargeCategory").equals("Discount"))
				{
					setCartDiscount (oHeaderCharge.getAttribute ("ChargeAmount"));
				}
			}					
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Header Charges//Discounts Initialized");
			}
		}
		YFSCreateOrderInputDoc.HeaderTaxes	oHeaderTaxes = oInOrder.getHeaderTaxes();
		if (oHeaderTaxes != null)
		{
			Enumeration<?>	enumHeaderTaxes = oHeaderTaxes.getHeaderTaxList();
			if (enumHeaderTaxes.hasMoreElements ())
			{
				YFSCreateOrderInputDoc.HeaderTax oHeaderTax = (YFSCreateOrderInputDoc.HeaderTax)enumHeaderTaxes.nextElement();
				setCartTaxes (oHeaderTax.getAttribute ("Tax"));
			}					
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Header Taxes Initialized");
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SiebelShoppingCart.initializeShoppingCart()");
		}
	}

	protected	void finalizeShoppingCart (YFSCreateOrderInputDoc oInOrder) throws Exception
	{
		// now remove unused components and options
		removeUnusedComponentsAndOptions (oInOrder);
		// load the integration ID values into items/components/options
//		loadIntegrationIdValuesIntoCart (oInOrder);
	}	

	@SuppressWarnings("deprecation")
	protected	void initializeShoppingCart (YFSFindInventoryInputDoc oInPromiseDoc) throws Exception
	{
		Reset();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering SiebelShoppingCart.initializeShoppingCart()");
			if(oInPromiseDoc.getPromise() == null)
			{
				System.out.println ("ERROR - Promise Element not Found!");
			}
		}

		YFSFindInventoryInputDoc.Promise oInPromise = oInPromiseDoc.getPromise();
		setCustomer (new Customer());
		setVendor (new Vendor());
		
		setOrganizationCode (oInPromise.getAttribute ("OrganizationCode"));
		Customer oCustomer = getCustomer();
		oCustomer.setSTZip (oInPromise.getShipToAddress().getAttribute ("ZipCode"));
		oCustomer.setSTCountry (oInPromise.getShipToAddress().getAttribute ("Country"));		
		setRequestedDate (oInPromise.getAttribute ("ReqStartDate"));
		setOrderSCAC   (oInPromise.getAttribute("SCAC"));
		setOrderCarrierService (oInPromise.getAttribute("CarrierServiceCode"));
		// if requested date passed it's in XML format.  Convert to locale format		
		if (getRequestedDate().length() > 0)
		{
			YFCDate dtReqDate = new YFCDate (getRequestedDate(), "yyyy-MM-dd'T'hh:mm:ss'Z'", true);
			setRequestedDate(dtReqDate.getString (YFCLocale.getDefaultLocale(), false));
		}
		else
			setRequestedDate(new YFCDate ().getString (YFCLocale.getDefaultLocale(), false));

		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SiebelShoppingCart.initializeShoppingCart()");
		}
	}

	protected	void finalizeShoppingCart (YFSFindInventoryInputDoc oInPromise) throws Exception
	{
		// now remove unused components and options
		removeUnusedComponentsAndOptions (oInPromise);

		// load the integration ID values into items/components/options
//		loadIntegrationIdValuesIntoCart (oInPromise);
	}	


	protected	void removeUnusedComponentsAndOptions (YFSCreateOrderInputDoc oInOrder) throws Exception
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount (); iItem++)
		{
			ShoppingCartItem	oSCItem = getShoppingCartItem (iItem);
			
			for (int iComponent = oSCItem.getComponentCount()-1; iComponent >= 0; iComponent--)
			{
				ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
				if (!IsItemInOrder (oInOrder, oComponent.getItemID(), oComponent.getParentBundle()))
					oSCItem.removeComponent (iComponent);
			}
			for (int iOption = oSCItem.getOptionCount()-1; iOption >= 0; iOption--)
			{
				ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
				if (!IsItemInOrder (oInOrder, oOption.getItemID(), oOption.getParentBundle()))
					oSCItem.removeOption (iOption);
			}
		}
	}

	protected	void removeUnusedComponentsAndOptions (YFSFindInventoryInputDoc oInPromise) throws Exception
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount (); iItem++)
		{
			ShoppingCartItem	oSCItem = getShoppingCartItem (iItem);
			
			for (int iComponent = oSCItem.getComponentCount()-1; iComponent >= 0; iComponent--)
			{
				ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
				if (!IsItemInOrder (oInPromise, oComponent.getItemID(), oComponent.getParentBundle()))
					oSCItem.removeComponent (iComponent);
			}
			for (int iOption = oSCItem.getOptionCount()-1; iOption >= 0; iOption--)
			{
				ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
				if (!IsItemInOrder (oInPromise, oOption.getItemID(), oOption.getParentBundle()))
					oSCItem.removeOption (iOption);
			}
		}
	}

/*
	protected	void	loadIntegrationIdValuesIntoCart (YFSCreateOrderInputDoc oInOrder) throws Exception
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount (); iItem++)
		{
			ShoppingCartItem	oSCItem = getShoppingCartItem (iItem);


			// all of these valuse should have been set during the initial load
//			oSCItem.setIntegrationID(oSCItem.getTransactionalLineId ());
			oSCItem.setIntegrationID(getIntegrationID(oInOrder, oSCItem.getItemID()));
			oSCItem.setTransactionalLineId(oSCItem.getIntegrationID());
			oSCItem.setParentBundle (getBundleParentID(oInOrder, oSCItem.getItemID()));
			
			for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
//				oComponent.setIntegrationID (oComponent.getTransactionalLineId ());
				oComponent.setIntegrationID (getIntegrationID (oInOrder, oComponent.getItemID(), oSCItem.getParentBundle()));
				oComponent.setTransactionalLineId(oComponent.getIntegrationID());
//				oComponent.setParentBundle (getBundleParentID(oInOrder, oComponent.getItemID()));
				oComponent.setParentBundle (oSCItem.getParentBundle());
			}
			for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
			{
				ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
//				oOption.setIntegrationID (oOption.getTransactionalLineId());
				oOption.setIntegrationID (getIntegrationID (oInOrder, oOption.getItemID(), oSCItem.getParentBundle()));
				oOption.setTransactionalLineId(oOption.getIntegrationID ());
//				oOption.setParentBundle (getBundleParentID(oInOrder, oOption.getItemID()));
				oOption.setParentBundle (oSCItem.getParentBundle());
			}
		}
	}
*/

/*
	protected	void	loadIntegrationIdValuesIntoCart (YFSFindInventoryInputDoc oInPromise) throws Exception
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount (); iItem++)
		{
			ShoppingCartItem	oSCItem = getShoppingCartItem (iItem);
			
//			oSCItem.setIntegrationID(oSCItem.getTransactionalLineId ());
			oSCItem.setIntegrationID(getIntegrationID(oInPromise, oSCItem.getItemID()));
			for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
//				oComponent.setIntegrationID (oComponent.getTransactionalLineId ());
				oComponent.setIntegrationID (getIntegrationID (oInPromise, oComponent.getItemID()));
			}
			for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
			{
				ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
//				oOption.setIntegrationID (oOption.getTransactionalLineId ());
				oOption.setIntegrationID (getIntegrationID (oInPromise, oOption.getItemID()));
			}
		}
	}
*/
	
	protected	void	loadIntegrationIdValuesIntoComponentsAndOptions (YFSCreateOrderInputDoc oInOrder, ShoppingCartItem oSCItem) throws Exception
	{
		// all components and options of this line should have the same parent bundle		
		for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
		{
			ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
			oComponent.setIntegrationID (getIntegrationID (oInOrder, oComponent.getItemID(), oSCItem.getParentBundle()));
			oComponent.setTransactionalLineId(oComponent.getIntegrationID());
			oComponent.setParentBundle (oSCItem.getParentBundle());
		}
		for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
		{
			ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
			oOption.setIntegrationID (getIntegrationID (oInOrder, oOption.getItemID(), oSCItem.getParentBundle()));
			oOption.setTransactionalLineId(oOption.getIntegrationID ());
			oOption.setParentBundle (oSCItem.getParentBundle());
		}
	}

	protected	void	loadIntegrationIdValuesIntoComponentsAndOptions (YFSFindInventoryInputDoc oInPromise, ShoppingCartItem oSCItem) throws Exception
	{
		// all components and options of this line should have the same parent bundle		
		for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
		{
			ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
			oComponent.setIntegrationID (getIntegrationID (oInPromise, oComponent.getItemID(), oSCItem.getParentBundle()));
			oComponent.setTransactionalLineId(oComponent.getIntegrationID());
			oComponent.setParentBundle (oSCItem.getParentBundle());
		}
		for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
		{
			ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
			oOption.setIntegrationID (getIntegrationID (oInPromise, oOption.getItemID(), oSCItem.getParentBundle()));
			oOption.setTransactionalLineId(oOption.getIntegrationID ());
			oOption.setParentBundle (oSCItem.getParentBundle());
		}
	}

	protected	boolean	IsItemInCart (String sItemID, String sParentBundle) throws Exception
	{
		for (int iItem = 0; iItem < getShoppingCartItemCount (); iItem++)
		{
			ShoppingCartItem	oSCItem = getShoppingCartItem (iItem);
			String				sItemParentBundle = oSCItem.getParentBundle();
						
			if (oSCItem.getItemID().equals (sItemID) && sItemParentBundle.equals(sParentBundle))
				return true;

			for (int iComponent = 0; iComponent < oSCItem.getComponentCount(); iComponent++)
			{
				ShoppingCartItem.Component	oComponent = oSCItem.getComponent (iComponent);
				String	sComponentBundleParent = oComponent.getParentBundle ();
				
				if (oComponent.getItemID().equals (sItemID) && sParentBundle.equals (sComponentBundleParent))
					return true;
			}
			for (int iOption = 0; iOption < oSCItem.getOptionCount(); iOption++)
			{
				ShoppingCartItem.Option		oOption = oSCItem.getOption (iOption);
				String						sOptionBundleParent = oOption.getParentBundle();
				
				if (oOption.getItemID().equals (sItemID) && sOptionBundleParent.equals(sParentBundle))
					break;
			}
		}
		return false;
	}

	protected	boolean	IsItemInOrder (YFSCreateOrderInputDoc oInOrder, String sItemID, String sParentBundle) throws Exception
	{
		YFSCreateOrderInputDoc.OrderLines oInOrderLines = oInOrder.getOrder().getOrderLines();
		
		if (oInOrderLines != null)
		{
			Enumeration<?>	enumInOrderLines = oInOrderLines.getOrderLineList();
			while (enumInOrderLines.hasMoreElements())
			{
				YFSCreateOrderInputDoc.OrderLine oInOrderLine = (YFSCreateOrderInputDoc.OrderLine)enumInOrderLines.nextElement();
				YFSCreateOrderInputDoc.BundleParentLine oInBundleParent = oInOrderLine.getBundleParentLine();
				String sInItemID = oInOrderLine.getItem().getAttribute ("ItemID");
				String sInParentBundle = "";
				if (oInBundleParent != null)
					sInParentBundle = oInBundleParent.getAttribute ("TransactionalLineId");
				
				if (sInItemID.equals (sItemID) && sInParentBundle.equals (sParentBundle))
					return true;
			}
		}
		return false;
	}

	protected	boolean	IsItemInOrder (YFSFindInventoryInputDoc oInPromise, String sItemID, String sParentBundle) throws Exception
	{
		YFSFindInventoryInputDoc.PromiseLines oInPromiseLines = oInPromise.getPromise().getPromiseLines();
		
		if (oInPromiseLines != null)
		{
			Enumeration<?>	enumInPromiseLines = oInPromiseLines.getPromiseLineList();
			while (enumInPromiseLines.hasMoreElements())
			{
				YFSFindInventoryInputDoc.PromiseLine oInPromiseLine = (YFSFindInventoryInputDoc.PromiseLine)enumInPromiseLines.nextElement();
				YFSFindInventoryInputDoc.BundleParentLine oInBundleParent = oInPromiseLine.getBundleParentLine();
				
				String sInItemID = oInPromiseLine.getAttribute ("ItemID");
				String sInParentBundle = "";
				
				if (oInBundleParent != null)
					sInParentBundle = oInBundleParent.getAttribute ("LineId");
					
				if (sInItemID.equals (sItemID) && sInParentBundle.equals (sParentBundle))
					return true;
			}
		}
		return false;
	}


	protected	String	getIntegrationID (YFSCreateOrderInputDoc oInOrder , String sItemID, String sParentBundle) throws Exception
	{
		YFSCreateOrderInputDoc.OrderLines oInOrderLines = oInOrder.getOrder().getOrderLines();
		
		if (oInOrderLines != null)
		{
			Enumeration<?>	enumInOrderLines = oInOrderLines.getOrderLineList();
			while (enumInOrderLines.hasMoreElements())
			{
				YFSCreateOrderInputDoc.OrderLine oInOrderLine = (YFSCreateOrderInputDoc.OrderLine)enumInOrderLines.nextElement();
				YFSCreateOrderInputDoc.BundleParentLine oInBundleParent = oInOrderLine.getBundleParentLine ();
				
				String sInItemID = oInOrderLine.getItem().getAttribute ("ItemID");
				String sInBundleParent = "";
				if (oInBundleParent != null)
					sInBundleParent = oInBundleParent.getAttribute ("TransactionalLineId");
				
				if (sInItemID.equals (sItemID) && sParentBundle.equals (sInBundleParent))
					return oInOrderLine.getAttribute ("CustomerLinePONo");
			}
		}
		return "";
	}

	protected	String	getIntegrationID (YFSFindInventoryInputDoc oInPromise , String sItemID, String sParentBundle) throws Exception
	{
		YFSFindInventoryInputDoc.PromiseLines oInPromiseLines = oInPromise.getPromise().getPromiseLines();
		
		if (oInPromiseLines != null)
		{
			Enumeration<?>	enumInPromiseLines = oInPromiseLines.getPromiseLineList();
			while (enumInPromiseLines.hasMoreElements())
			{
				YFSFindInventoryInputDoc.PromiseLine oInPromiseLine = (YFSFindInventoryInputDoc.PromiseLine)enumInPromiseLines.nextElement();
				YFSFindInventoryInputDoc.BundleParentLine oInBundleParent = oInPromiseLine.getBundleParentLine();
				
				String sInItemID = oInPromiseLine.getAttribute ("ItemID");
				String sInParentBundle = "";
				if (oInBundleParent != null)
					sInParentBundle = oInBundleParent.getAttribute ("LineId");
				if (sInItemID.equals (sItemID) && sParentBundle.equals (sInParentBundle))
					return oInPromiseLine.getAttribute ("LineId");
			}
		}
		return "";
	}

/*
	protected	String	getBundleParentID (YFSCreateOrderInputDoc oInOrder , String sItemID) throws Exception
	{
		YFSCreateOrderInputDoc.OrderLines oInOrderLines = oInOrder.getOrder().getOrderLines();
		
		if (oInOrderLines != null)
		{
			Enumeration	enumInOrderLines = oInOrderLines.getOrderLineList();
			while (enumInOrderLines.hasMoreElements())
			{
				YFSCreateOrderInputDoc.OrderLine oInOrderLine = (YFSCreateOrderInputDoc.OrderLine)enumInOrderLines.nextElement();
				String sInItemID = oInOrderLine.getItem().getAttribute ("ItemID");
				if (sInItemID.equals (sItemID))
				{
					YFSCreateOrderInputDoc.BundleParentLine oBundleParentLine = oInOrderLine.getBundleParentLine();					
					if (oBundleParentLine != null)
						return oBundleParentLine.getAttribute ("TransactionalLineId");
					else
						break;
				}
			}
		}
		return "";
	}
*/

	protected void addShoppingCartItem (YFSCreateOrderInputDoc oInOrder, YFSCreateOrderInputDoc.OrderLine oInOrderLine, String sItemID, String sQty, String sUOM, String sPC) throws Exception
	{
		// add the order line to cart and load all components/options for the item
		addShoppingCartItem (sItemID, sQty, sUOM, sPC, true, true);

		// add the order line charges, discounts and taxes from original order line
		ShoppingCartItem oSCItem = getShoppingCartItem(getShoppingCartItemCount()-1);
		
		oSCItem.setPrimeLineNo (oInOrderLine.getAttribute ("PrimeLineNo"));
		oSCItem.setIntegrationID (oInOrderLine.getAttribute ("CustomerLinePONo"));
		oSCItem.setTransactionalLineId (oInOrderLine.getAttribute ("TransactionalLineId"));
		
		YFSCreateOrderInputDoc.BundleParentLine oInBundleParent = oInOrderLine.getBundleParentLine ();
		if (oInBundleParent != null)
			oSCItem.setParentBundle (oInBundleParent.getAttribute ("TransactionalLineId"));
		
		YFSCreateOrderInputDoc.LineCharges oLineCharges = oInOrderLine.getLineCharges();
		if (oLineCharges != null)
		{
			Enumeration<?>	enumLineCharges = oLineCharges.getLineChargeList ();
			while (enumLineCharges.hasMoreElements ())
			{
				YFSCreateOrderInputDoc.LineCharge	oLineCharge = (YFSCreateOrderInputDoc.LineCharge)enumLineCharges.nextElement();
				if (oLineCharge.getAttribute ("ChargeCategory").equals("Shipping"))
					oSCItem.setShipping (oLineCharge.getAttribute ("ChargePerUnit"));
				else if (oLineCharge.getAttribute ("ChargeCategory").equals ("Discount"))
					oSCItem.setDiscount (oLineCharge.getAttribute ("ChargePerUnit"));
			}
		}
		YFSCreateOrderInputDoc.LineTaxes oLineTaxes = oInOrderLine.getLineTaxes();
		if (oLineTaxes != null)
		{
			Enumeration<?>	enumLineTaxes = oLineTaxes.getLineTaxList();
			if (enumLineTaxes.hasMoreElements ())
			{
				YFSCreateOrderInputDoc.LineTax	oLineTax = (YFSCreateOrderInputDoc.LineTax)enumLineTaxes.nextElement();
				if (oLineTax.getAttribute ("TaxName").equals("Tax"))
					oSCItem.setTax(oLineTax.getAttribute ("Tax"));
			}
		}
		// load the relevant integration values into the associated components and options			
		loadIntegrationIdValuesIntoComponentsAndOptions(oInOrder, oSCItem);
	}

	protected void addShoppingCartItem (YFSFindInventoryInputDoc oInPromise, YFSFindInventoryInputDoc.PromiseLine oInPromiseLine, String sItemID, String sQty, String sUOM, String sPC) throws Exception
	{
		// add the order line to cart and load all components/options for the item
		addShoppingCartItem (sItemID, sQty, sUOM, sPC, true, true);

		// add the order line charges, discounts and taxes from original order line
		ShoppingCartItem oSCItem = getShoppingCartItem(getShoppingCartItemCount()-1);
		oSCItem.setIntegrationID (oInPromiseLine.getAttribute ("LineId"));
		oSCItem.setTransactionalLineId (oInPromiseLine.getAttribute("LineId"));
		YFSFindInventoryInputDoc.BundleParentLine oInBundleParent = oInPromiseLine.getBundleParentLine();
		if (oInBundleParent != null)
			oSCItem.setParentBundle (oInBundleParent.getAttribute ("LineId"));

		// load the relevant integration values into the associated components and options			
		loadIntegrationIdValuesIntoComponentsAndOptions(oInPromise, oSCItem);			
	}
}


