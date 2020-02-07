package com.custom.diab.demos.api;


import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.custom.diab.demos.ue.SEProcessReturnCompletionUEImpl;
import com.ue.DemoCreateCustomerForOrder;


public class SEWebOrderExtensions implements YIFCustomApi {

    public SEWebOrderExtensions()
    {
    }

	@SuppressWarnings("unused")
	private static YFCLogCategory logger = YFCLogCategory.instance(SEWebOrderExtensions.class);

    @Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}
	
	public Document	processPayments (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCDocument	docOrderInput = getOrderDetailsInput (docOrder);
        
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering processPayments - Input:");
        	System.out.println (docOrder.getString());
        }

    	try {
        	
        	YFCDocument	docProcessOrderPayments = getOrderDetailsInput (docOrder);
            if (YFSUtil.getDebug())
            {
            	System.out.println ("processOrderPayments - Input:");
            	System.out.println (docProcessOrderPayments.getString());
            }
        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
            
            // process the payments on the order
            docProcessOrderPayments = YFCDocument.getDocumentFor(api.processOrderPayments(env, docProcessOrderPayments.getDocument()));
            if (YFSUtil.getDebug())
            {
            	System.out.println ("processOrderPayments - Output:");
            	System.out.println (docProcessOrderPayments.getString());
            }
            return docOrderInput.getDocument();

        } catch (Exception e) {
        	try {
        		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
        		api.executeFlow(env, "ShipOrderSimulator", docIn);
        	} catch (Exception eIgnore) {}        		
        	throw new YFSException (e.getMessage() + "\nWill Retry via ShipOrderSimulator Service");
        } finally {
        }
	}

	public Document shipAndConfirmOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument		docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCDocument		docOrderInput = getOrderDetailsInput (docOrder);
        
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering shipAndConfirmOrder - Input:");
        	System.out.println (docOrder.getString());
        }
        try {
        	YFCDocument	docConfirmShipmentTemplate = YFCDocument.getDocumentFor ("<Order DocumentType=\"\" EnterpriseCode=\"\" OrderHeaderKey=\"\" OrderNo=\"\"><OrderLines><OrderLine OrderHeaderKey=\"\" OrderLineKey=\"\" OrderedQty=\"\" DeliveryMethod=\"\"><Item ItemID=\"\" UnitOfMeasure=\"\"/></OrderLine></OrderLines></Order>");
        	        	
        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
        	env.setApiTemplate("getOrderDetails", docConfirmShipmentTemplate.getDocument());
        	docOrder = YFCDocument.getDocumentFor(api.getOrderDetails(env, docOrderInput.getDocument()));
        	env.clearApiTemplate("getOrderDetails");

        	// generate the shipment document based on the original order (only shippable lines will be on the shipment document)
        	YFCDocument	docShipment = createShipmentDoc (env, docOrder);
        	YFCElement	eleShipment = docShipment.getDocumentElement();
        	YFCElement	eleShipmentLines = eleShipment.getChildElement("ShipmentLines");

        	// if there are lines that are shippable
        	if (!YFCObject.isVoid(eleShipmentLines) && eleShipmentLines.hasChildNodes())
        	{
        		api = YIFClientFactory.getInstance().getLocalApi ();
            	// debug message
        		if (YFSUtil.getDebug ())
        		{
        			System.out.println( "Input for createShipment() API is ...");
        			System.out.println( docShipment.getString());
        		}
            	YFCDocument	docCreateShipmentTemplate = YFCDocument.getDocumentFor ("<Shipment ShipmentKey=\"\"><ShipmentLines><ShipmentLine OrderLineKey=\"\" ShipmentKey=\"\" ShipmentLineKey=\"\" Quantity=\"\"></ShipmentLine></ShipmentLines></Shipment>");
            	env.setApiTemplate("createShipment", docCreateShipmentTemplate.getDocument());          
        		YFCDocument docConfirmShipment = YFCDocument.getDocumentFor (api.createShipment (env, docShipment.getDocument()));
        		env.clearApiTemplate("createShipment");
        		if (YFSUtil.getDebug())
        		{
        			System.out.println( "Output from createShipment() API is ...");
        			System.out.println( docConfirmShipment.getString());
        		}

        		// copy back room pick flag to new shipment
        		copyIsBackroomPickRequired (docShipment, docConfirmShipment);
        		
        		// confirm only the shipment lines that don't require a back room pick
        		docConfirmShipment = confirmOrderShipment (env, docConfirmShipment);

        		// create the shipment invoices
        		if (!YFCObject.isNull (docConfirmShipment) && docConfirmShipment.getDocumentElement().getNodeName().equals("Shipment"))
        			createInvoices (env, docConfirmShipment.getDocument());        		
        	}
    		return docOrderInput.getDocument();
        } catch (Exception e) {
        	try {
        		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
        		api.executeFlow(env, "ShipOrderSimulator", docIn);
        	} catch (Exception eIgnore) {}
        	throw new YFSException (e.getMessage() + "\nWill Retry via ShipOrderSimulator Service");
        } finally {
        	env.clearApiTemplate("getOrderDetails");
    		env.clearApiTemplate("createShipment");
        }
	}
	
	public Document	scheduleAndReleaseOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCDocument	docOrderInput = getOrderDetailsInput (docOrder);
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering scheduleAndReleaseOrder - Input:");
        	System.out.println (docOrder.getString());
        }
        try {
        	String	sScheduleAndRelease = m_Props.getProperty("ScheduleAndRelease");

        	if (YFCObject.isVoid(sScheduleAndRelease))
        		sScheduleAndRelease = "Y";

        	YFCDocument	docScheduleOrderTemplate = YFCDocument.getDocumentFor ("<Order AllocationRuleID=\"\" DocumentType=\"\" EnterpriseCode=\"\" OrderHeaderKey=\"\" OrderNo=\"\"/>");

        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
        	env.setApiTemplate("getOrderDetails", docScheduleOrderTemplate.getDocument());
        	YFCDocument	docScheduleOrder = YFCDocument.getDocumentFor(api.getOrderDetails(env, docOrderInput.getDocument()));
        	env.clearApiTemplate("getOrderDetails");
        	YFCElement	eleScheduleOrder = docScheduleOrder.getDocumentElement();
        	eleScheduleOrder.setAttribute ("IgnoreMinNotificationTime", "Y");
        	
        	if (YFSUtil.getDebug())
            {
            	System.out.println ("scheduleOrder - Input:");
            	System.out.println (docScheduleOrder.getString());
            }
            // schedule the order
        	api.scheduleOrder(env, docScheduleOrder.getDocument());

        	// if forcing the release as well
        	if (sScheduleAndRelease.equals("Y"))
        		return (forceReleaseOrder (env, docScheduleOrder.getDocument()));
        	
            return docOrderInput.getDocument();
        } catch (Exception e) {
        	try {
        		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
        		api.executeFlow(env, "ShipOrderSimulator", docIn);
        	} catch (Exception eIgnore) {}
        	throw new YFSException (e.getMessage() + "\nWill Retry via ShipOrderSimulator Service");
        } finally {
        	env.clearApiTemplate("getOrderDetails");
        }
	}

	public Document suspendOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCElement	eleOrder  = docOrder.getDocumentElement();
        YFCDocument	docOrderStatusChange = YFCDocument.createDocument("OrderStatusChange");
        YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement();
        
        eleOrderStatusChange.setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey"));
        eleOrderStatusChange.setAttribute("ChangeForAllAvailableQty", "Y");
        eleOrderStatusChange.setAttribute("BaseDropStatus", "1000.9000");
        eleOrderStatusChange.setAttribute("TransactionId", "POS_SUSPEND_ORDER.0001.ex");
        try {
        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
        	return  api.changeOrderStatus(env, docOrderStatusChange.getDocument());
        } catch (Exception e) {
        	throw new YFSException(e.getMessage());
		}
	}
	
	public Document processReturnForCompletion (YFSEnvironment env, Document docIn) throws YFSException
	{
		SEProcessReturnCompletionUEImpl	returnUE = new SEProcessReturnCompletionUEImpl ();
		Document	docOut;
		try {
			docOut = returnUE.processReturnCompletion(env, docIn); 
		} catch (YFSUserExitException e) {
			throw new YFSException (e.getMessage());
		}
		return docOut;
	}
	
	public Document manageCustomerOnOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOut;
		
		try {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Entering manageCustomerOnOrder - Input XML:");
				System.out.println (YFCDocument.getDocumentFor (docIn).getString());
			}
			DemoCreateCustomerForOrder ue = new DemoCreateCustomerForOrder ();
			docOut = ue.beforeCreateOrder(env, docIn);
									
			if (YFSUtil.getDebug())
			{
				System.out.println ("Exiting manageCustomerOnOrder - Output XML:");
				System.out.println (YFCDocument.getDocumentFor (docOut).getString());
			}
		} catch (YFSUserExitException e) {
			throw new YFSException (e.getMessage());
		}
		return docOut;
	}
	
	public Document updateOrderSourcingClassification (YFSEnvironment env, Document docInXML) throws YFSException
	{
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docInXML);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		
		// dynamically assign sourcing classification based on effectivity dates 
		String	sSourcingClassification = eleOrder.getAttribute ("SourcingClassification");
		if (!YFCObject.isVoid(sSourcingClassification) && sSourcingClassification.equals("USEEFFECTIVEDATESOURCING"))
			eleOrder.setAttribute("SourcingClassification", getEffectiveSourcingClassification (env, eleOrder));
		return docOrder.getDocument();
	}
	
	@SuppressWarnings("rawtypes")
	protected String getEffectiveSourcingClassification (YFSEnvironment env, YFCElement eleOrder) throws YFSException
	{
		YFCDocument	docCommonCode = YFCDocument.createDocument("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		String		sSourcingClassification = "";
		
		eleCommonCode.setAttribute ("CodeType", "ORDR_SRCNG_CLASSN");
		eleCommonCode.setAttribute("CallingOrganizationCode", eleOrder.getAttribute ("EnterpriseCode"));
		
		try {
			YIFApi	api = YFSUtil.getYIFApi ();

			System.out.println("Input to getCommonCodeList:");
			System.out.println(docCommonCode.getString());

			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
			System.out.println("Output from getCommonCodeList:");
			System.out.println(docOut.getString());

			if (eleCommonCodes != null)
			{
				Iterator	iCommonCodes = eleCommonCodes.getChildren();			
				long		lCurrentDateTime = eleOrder.getDateTimeAttribute("OrderDate").getTime();
				System.out.println("Current Time Stamp Value =" + lCurrentDateTime);
				while (iCommonCodes.hasNext())
				{	
					eleCommonCode = (YFCElement)iCommonCodes.next();
					if (!eleCommonCode.getAttribute("CodeValue").equals("USEEFFECTIVEDATESOURCING"))
					{
						try {
							long	lStartDateTime = eleCommonCode.getDateTimeAttribute("CodeShortDescription").getTime();
							long	lEndDateTime   = eleCommonCode.getDateTimeAttribute("CodeLongDescription").getTime();
							
							System.out.println("Order Sourcing Classfication:" + eleCommonCode.getAttribute("CodeValue"));
							System.out.println("Effective Start Time Stamp=" + lStartDateTime);
							System.out.println("Effective End   Time Stamp=" + lEndDateTime);

							if (lCurrentDateTime >= lStartDateTime && lCurrentDateTime <= lEndDateTime)
							{
								sSourcingClassification = eleCommonCode.getAttribute("CodeValue");
								break;
							}
						} catch (Exception eIgnore) {
							// ignore Order Sourcing Classifications not for this purpose 
							// i.e. Short and Long Descriptions must be Date/Time Stamps
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return sSourcingClassification;

	}

	
	
	 @SuppressWarnings("rawtypes")
	public Document manageDeliveryMethodUpdate(YFSEnvironment env, Document docIn)
     throws Exception
     {
         YFCDocument docOrder = YFCDocument.getDocumentFor(docIn);
         YFCElement eleOrder = docOrder.getDocumentElement();
         YFCElement eleOrderLines = eleOrder.getChildElement("OrderLines");
         if(!YFCObject.isNull(eleOrderLines))
         {
             for(YFCIterable iOrderLines = eleOrderLines.getChildren(); iOrderLines.hasNext();)
             {
                 YFCElement eleOrderLine = (YFCElement)iOrderLines.next();
                 String sDeliveryMethod = eleOrderLine.getAttribute("DeliveryMethod");
                 String sCarrierServiceCode = eleOrderLine.getAttribute("CarrierServiceCode");
                 if("PickUpInStore".equals(sCarrierServiceCode))
                     eleOrderLine.setAttribute("CarrierServiceCode", "");
                 if("PICK".equals(sDeliveryMethod))
                 {
                     eleOrderLine.setAttribute("ShipToID", "");
                     eleOrderLine.setAttribute("ShipToKey", "");
                     eleOrderLine.setAttribute("FillQuantity", "0");
                     eleOrderLine.setAttribute("CarrierServiceCode", "");
                     YFCElement elePersonInfoShipTo = eleOrderLine.getChildElement("PersonInfoShipTo");
                     if(!YFCObject.isNull(elePersonInfoShipTo))
                         eleOrderLine.removeChild(elePersonInfoShipTo);
                 }
             }
         }
         return docOrder.getDocument();
     }

	 @SuppressWarnings("rawtypes")
	public Document	setCustomerDefaultsOnOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        boolean		bDefaultCustomerInformation;

        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering setCustomerDefaultsOnOrder - Input:");
        	System.out.println (docOrder.getString());
        }

		// see if order has a matching customer master record and default values from customer if necessary
        YFCElement eleOrder = docOrder.getDocumentElement();

		try {

			env.setApiTemplate("getCustomerList", YFCDocument.getDocumentFor("<CustomerList><Customer CustomerKey=\"\" CustomerRewardsNo=\"\" CustomerID=\"\"/></CustomerList>").getDocument());
			bDefaultCustomerInformation = loadCustomerInformation (env, eleOrder);
			env.clearApiTemplate("getCustomerList");
			
			// use arguments to API to set additional default order Level attributes
			Enumeration	eProps = m_Props.keys();
			while (eProps.hasMoreElements())
			{
				String	sKey = (String)eProps.nextElement();
				eleOrder.setAttribute (sKey, evaluateXPathExpression (m_Props.getProperty(sKey), docIn));
			}
			
			
			// look for Pay In Store orders and modify accordingly
			if (IsPayInStore (docOrder))
			{
				// Suspend the Order
				eleOrder.setAttribute("DraftOrderFlag", "Y");
				eleOrder.setAttribute("OrderType", "PAYINSTORE");
				eleOrder.setAttribute("EntryType", "STORE");
				eleOrder.setAttribute("PaymentRuleId", "DEFAULT-POS");
			}

			// set the flag to default customer information if we found a valid customer and missing or incomplete PersonInfo was provided 
			eleOrder.setAttribute ("DefaultCustomerInformation", bDefaultCustomerInformation);			
			
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		} finally {
			env.clearApiTemplate("getCustomerList");			
		}
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Exiting setCustomerDefaultsOnOrder - Output:");
        	System.out.println (docOrder.getString());
        }
		return docOrder.getDocument();        
	}
		
	private void	createInvoices (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docShipment		 = YFCDocument.getDocumentFor(docIn);
    	YFCElement	eleShipment		 = docShipment.getDocumentElement();
        YFCDocument	docShipmentInput = YFCDocument.createDocument ("Shipment");
    	YFCElement	eleShipmentInput = docShipmentInput.getDocumentElement();
        
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering createInvoices - Input:");
        	System.out.println (docShipment.getString());
        }
        try {
        	
        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
        	eleShipmentInput.setAttribute ("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
        	eleShipmentInput.setAttribute ("TransactionId", "CREATE_SHMNT_INVOICE.0001");
            if (YFSUtil.getDebug())
            {
            	System.out.println ("createShipmentInvoice - Input:");
            	System.out.println (docShipmentInput.getString());
            }
            
            // process the payments on the order
            YFCDocument	 docShipmentInvoice = YFCDocument.getDocumentFor(api.createShipmentInvoice(env, docShipmentInput.getDocument()));
            if (YFSUtil.getDebug())
            {
            	System.out.println ("createShipmentInvoice - Output:");
            	System.out.println (docShipmentInvoice.getString());
            }
            return;

        } catch (Exception e) {
        	throw new YFSException (e.getMessage());
        } finally {
        }
	}

	private Document	forceReleaseOrder (YFSEnvironment env, Document docIn) throws YFSException
	{
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        YFCElement	eleOrder  = docOrder.getDocumentElement();
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering forceReleaseOrder - Input:");
        	System.out.println (docOrder.getString());
        }
        try {
        	YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();

        	// ignore release date  
        	eleOrder.setAttribute("IgnoreReleaseDate", "Y");

        	if (YFSUtil.getDebug())
            {
            	System.out.println ("releaseOrder - Input:");
            	System.out.println (docOrder.getString());
            }
            // schedule the order
        	api.releaseOrder(env, docOrder.getDocument());
            return docIn;
        } catch (Exception e) {
        	throw new YFSException (e.getMessage());
        } finally {
        }
	}

	private YFCDocument getOrderDetailsInput (YFCDocument docOrder)
	{
        YFCElement	eleOrder  = docOrder.getDocumentElement ();
        YFCDocument	docOrderInput = YFCDocument.createDocument ("Order");
        YFCElement	eleOrderInput = docOrderInput.getDocumentElement();
		
    	String	sOrderHeaderKey = eleOrder.getAttribute ("OrderHeaderKey");

    	if (YFCObject.isVoid(sOrderHeaderKey))
    	{
    		eleOrderInput.setAttribute ("OrderNo", eleOrder.getAttribute("OrderNo"));
    		eleOrderInput.setAttribute ("EnterpriseCode", eleOrder.getAttribute("EnterpriseCode"));
    		eleOrderInput.setAttribute ("DocumentType", eleOrder.getAttribute("DocumentType"));
    	}
    	else
    	{
    		eleOrderInput.setAttribute ("OrderHeaderKey", sOrderHeaderKey);
    	}

    	return docOrderInput;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean	loadCustomerInformation (YFSEnvironment env, YFCElement eleOrder) throws Exception
	{
        String		sOrganizationCode = eleOrder.getAttribute("EnterpriseCode");
        String		sBuyerUserId = eleOrder.getAttribute ("BuyerUserId");
        String		sContactId = eleOrder.getAttribute ("CustomerContactID");
        boolean		bDefaultCustomerInformation = true;

        
        // if no buyer user id or customer contact id is passed we can't default customer information
        if (YFCObject.isVoid(sBuyerUserId) && YFCObject.isVoid(sContactId))
        {
        	bDefaultCustomerInformation = false;
        	sBuyerUserId = "";
        	sContactId = "";
			if (YFSUtil.getDebug())
			{
				System.out.println ("No BuyerUserId or CustomerContactID was Passed - Can't Default Customer Information");
			}
        }
        else if (YFCObject.isVoid(sBuyerUserId))
        	sBuyerUserId = "";
        else if (YFCObject.isVoid(sContactId))
        	sContactId = "";

        // if defaulting of customer information still possible
		if (bDefaultCustomerInformation)
		{
			YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
	        YFCElement	eleCustomer = docCustomer.getDocumentElement();
	        YFCElement 	eleCustomerContactList = eleCustomer.createChild("CustomerContactList");
	        YFCElement	eleCustomerContact = eleCustomerContactList.createChild("CustomerContact");

	        eleCustomer.setAttribute("OrganizationCode", sOrganizationCode);
	        eleCustomerContact.setAttribute("UserID", sBuyerUserId);
	        eleCustomerContact.setAttribute("CustomerContactID", sContactId);

			// lookup customer master record using BuyerUserId information 
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
			if (YFSUtil.getDebug())
			{
				System.out.println ("getCustomerList - Input:");
				System.out.println (docCustomer.getString());
			}
			YFCDocument docCustomerList = YFCDocument.getDocumentFor(api.getCustomerList(env, docCustomer.getDocument()));
			if (YFSUtil.getDebug())
			{
				System.out.println ("getCustomerList - Output:");
				System.out.println (docCustomerList.getString());
			}
			// if output document returned
			YFCElement	eleCustomerList = docCustomerList.getDocumentElement();
			Iterator	iCustomerList = eleCustomerList.getChildren();
			
			// if a matching customer record exists
			if (bDefaultCustomerInformation = iCustomerList.hasNext())
			{
				YFCElement	elePersonInfoBillTo = eleOrder.getChildElement ("PersonInfoBillTo");
				YFCElement	elePersonInfoShipTo = eleOrder.getChildElement ("PersonInfoShipTo");

				eleCustomer = (YFCElement)iCustomerList.next();
				String		sCustomerID = eleCustomer.getAttribute ("CustomerID");
				String		sCustomerRewardsNo = eleCustomer.getAttribute("CustomerRewardsNo");

				// set BillToID and CustomerRewardsNo based on the Customer Record Found
				eleOrder.setAttribute("CustomerRewardsNo", sCustomerRewardsNo);
				
				// if API argument is set to Default Customer information
				String 	sDefaultCustomerInformation = m_Props.getProperty("DefaultCustomerInformation");
				String  sDefaultShipToInformation = m_Props.getProperty("DefaultShipToInformation");
				String  sDefaultBillToInformation = m_Props.getProperty("DefaultBillToInformation");
				
				bDefaultCustomerInformation = !YFCObject.isVoid(sDefaultCustomerInformation) && sDefaultCustomerInformation.equals("Y");
				boolean bDefaultShipToInformation = !YFCObject.isVoid(sDefaultShipToInformation) && sDefaultShipToInformation.equals("Y");
				boolean bDefaultBillToInformation = !YFCObject.isVoid(sDefaultBillToInformation) && sDefaultBillToInformation.equals("Y");
				
				// if API argument is set to Default Customer information or BillTo information
				if((bDefaultCustomerInformation || bDefaultBillToInformation) && IsPersonInfoIncomplete(elePersonInfoBillTo))
				{
					// default the BillTo ID based on Customer Master "Default" bill to and remove incomplete bill to
					eleOrder.setAttribute("BillToID", sCustomerID);
					// remove the PersonInfoBillTo element passed in
					eleOrder.removeChild (elePersonInfoBillTo);
				}
				// if API argument is set to Default Customer information or ShipTo information
				if((bDefaultCustomerInformation || bDefaultShipToInformation) && IsPersonInfoIncomplete(elePersonInfoShipTo))
				{
					// default the ShipTo ID based on Customer Master "Default" ship to and remove incomplete ship to
					eleOrder.setAttribute("ShipToID", sCustomerID);		
					// remove the PersonInfoShipTo element passed in
					eleOrder.removeChild (elePersonInfoShipTo);
				}
			}
			else if (!YFCObject.isVoid (sBuyerUserId))
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Customer with User ID " + sBuyerUserId + " Doesn't Exist.  Must Synch Customer Master");
				}
			}
			else if (!YFCObject.isVoid (sContactId))
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Customer with Contact ID " + sContactId + " Doesn't Exist.  Must Synch Customer Master");
				}
			}
		}			
		return bDefaultCustomerInformation;
	}
	
	@SuppressWarnings("rawtypes")
	private	boolean	IsPayInStore (YFCDocument docOrder)
	{
		YFCElement	eleOrder = docOrder.getDocumentElement();
		
		YFCElement	elePaymentMethods = eleOrder.getChildElement ("PaymentMethods");
		if (!YFCObject.isVoid(elePaymentMethods))
		{
			Iterator	iPaymentMethods = elePaymentMethods.getChildren();
			while (iPaymentMethods.hasNext())
			{
				YFCElement	elePaymentMethod = (YFCElement)iPaymentMethods.next();
				if (elePaymentMethod.getAttribute("PaymentType").equals(("PayInStore")))
				{
					elePaymentMethods.removeChild(elePaymentMethod);
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean	IsPersonInfoIncomplete (YFCElement elePersonInfo)
	{
		boolean bRet = false;
		
		if (!YFCObject.isNull(elePersonInfo))
		{
			String	sAddress1  = elePersonInfo.getAttribute("AddressLine1");
			String	sCity	   = elePersonInfo.getAttribute("City");
			String	sState	   = elePersonInfo.getAttribute("State");
			String	sZip	   = elePersonInfo.getAttribute("ZipCode");

			// if incomplete bill-to information passed in remove the incomplete address and default from the customer master
			bRet = YFCObject.isVoid(sAddress1) || YFCObject.isVoid(sCity) || YFCObject.isVoid(sState) || YFCObject.isVoid(sZip);
		}
		return bRet;
	}

	
	@SuppressWarnings("rawtypes")
	private void copyIsBackroomPickRequired (YFCDocument docShipmentInput, YFCDocument docShipmentOutput)
	{
		YFCElement	eleShipmentOutput = docShipmentOutput.getDocumentElement();
		YFCElement	eleShipmentLines = eleShipmentOutput.getChildElement("ShipmentLines");
		
		Iterator iShipmentLines = eleShipmentLines.getChildren();

		// iterate over the created shipment lines
		while (iShipmentLines.hasNext())
		{
			YFCElement	eleShipmentLine = (YFCElement)iShipmentLines.next();
			eleShipmentLine.setAttribute ("IsBackroomPickRequired", getIsBackroomPickRequired (docShipmentInput, eleShipmentLine.getAttribute ("OrderLineKey")));
		}
		return;
	}
	
	@SuppressWarnings("rawtypes")
	private YFCDocument	confirmOrderShipment (YFSEnvironment env, YFCDocument docConfirmShipment) throws Exception
	{
		// confirm lines that are being shipped but not if being picked up (must confirm using Sterling Store)
		YFCElement	eleConfirmShipment = docConfirmShipment.getDocumentElement();
    	YFCElement	eleShipmentLines = eleConfirmShipment.getChildElement("ShipmentLines");

		Iterator iShipmentLines = eleShipmentLines.getChildren();
		if (YFSUtil.getDebug())
		{
			System.out.println("Preparing confirmShipment Input Document Using:");
			System.out.println (docConfirmShipment.getString());
		}
		// 
		while (iShipmentLines.hasNext())
		{
			YFCElement	eleShipmentLine = (YFCElement)iShipmentLines.next();
			if (eleShipmentLine.getBooleanAttribute ("IsBackroomPickRequired"))
			{
				// removing shipment lines that requires a back room pick (e.g. Usually Pickup in Store or Ship From Store)
				eleShipmentLines.removeChild(eleShipmentLine);
				eleShipmentLines = eleConfirmShipment.getChildElement("ShipmentLines");

				// restart the iterator
				if (!YFCObject.isVoid(eleShipmentLines) && eleShipmentLines.hasChildNodes())
					iShipmentLines = eleShipmentLines.getChildren();
				else
					break;
			}
		}
		// if we have shipment lines that don't require a back room pick
		if (!YFCObject.isVoid(eleShipmentLines) && eleShipmentLines.hasChildNodes())
		{
	    	YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
    		// debug message
    		if (YFSUtil.getDebug ())
    		{
    			System.out.println( "Input for confirmShipment() API is ...");
    			System.out.println( docConfirmShipment.getString());
    		}
    		// now confirm only those lines not being picked up in store
    		docConfirmShipment = YFCDocument.getDocumentFor (api.confirmShipment (env, docConfirmShipment.getDocument()));
    		if (YFSUtil.getDebug())
    		{
    			System.out.println( "Output from confirmShipment() API is ...");
    			System.out.println( docConfirmShipment.getString());
    		}    		
    		return docConfirmShipment;		
		}
		else
		{
			if (YFSUtil.getDebug ())
    		{
				System.out.println ("No Lines to Confirm Because All Lines on the Order Require a Backroom Pick");
    		}	
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private String	getIsBackroomPickRequired (YFCDocument docShipment, String sOrderLineKey)
	{
		YFCElement	eleShipment = docShipment.getDocumentElement();
		YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
		
		Iterator iShipmentLines = eleShipmentLines.getChildren();
		while (iShipmentLines.hasNext())
		{
			YFCElement	eleShipmentLine = (YFCElement)iShipmentLines.next();
			if (eleShipmentLine.getAttribute ("OrderLineKey").equals(sOrderLineKey))
				return eleShipmentLine.getAttribute("IsBackroomPickRequired");
		}
		return "";
	}
	
	@SuppressWarnings("rawtypes")
	private YFCDocument createShipmentDoc (YFSEnvironment env, YFCDocument docOrder) throws Exception
	{
		YFCDocument		docShipment = YFCDocument.createDocument ("Shipment");	
		YFCElement 		eleShipment = docShipment.getDocumentElement ();	
		YFCElement		eleOrder = docOrder.getDocumentElement();
		
		// generate XML for OrderLines
		try {
			YIFApi	api =  YIFClientFactory.getInstance().getLocalApi ();

	    	YFCElement eleShipmentLines = eleShipment.createChild ("ShipmentLines");

	    	YFCDocument docOrderStatusList = YFCDocument.createDocument("OrderLineStatus");
			YFCElement	eleOrderStatusList = docOrderStatusList.getDocumentElement();
			eleOrderStatusList.setAttribute ("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
			eleOrderStatusList.setAttribute ("DocumentType", eleOrder.getAttribute ("DocumentType"));
			eleOrderStatusList.setAttribute ("TransactionId", "INCLUDE_SHIPMENT");
			if (YFSUtil.getDebug ())
			{
				System.out.println( "Input for getOrderLineStatusList():");
				System.out.println (docOrderStatusList.getString());
			}
			env.setApiTemplate("getOrderLineStatusList", YFCDocument.getDocumentFor ("<OrderLineStatusList><OrderStatus OrderHeaderKey=\"\" OrderLineKey=\"\" OrderReleaseKey=\"\" ProcureFromNode=\"\" ReceivingNode=\"\" ShipNode=\"\" Status=\"\" StatusQty=\"\"></OrderStatus><DropStatuses><DropStatus Status=\"\"/></DropStatuses></OrderLineStatusList>").getDocument());
			docOrderStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderStatusList.getDocument()));
			env.clearApiTemplate("getOrderLineStatusList");
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Output from getOrderLineStatusList():");
				System.out.println (docOrderStatusList.getString());
			}    		
			eleOrderStatusList = docOrderStatusList.getDocumentElement();

	    	// iterate over order lines
	    	Iterator	iOrderLines = eleOrder.getChildElement("OrderLines").getChildren();
	    	
	    	while (iOrderLines.hasNext())
			{
	    		YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
	    		YFCElement	eleItem = eleOrderLine.getChildElement("Item");
				// create OrderLine 

				// iterate over the status line list
				for (Iterator iOrderLineStatus = eleOrderStatusList.getChildren (); iOrderLineStatus.hasNext(); )
				{		
					YFCElement	eleOrderStatus = (YFCElement)iOrderLineStatus.next();			
					if (eleOrderStatus.getNodeName().equals("OrderStatus"))
					{
						// if line in a pickup status for a return
						boolean bRequiresBackroomPick = IsBackroomPickRequired (env, eleOrderStatus, eleOrderLine.getAttribute("DeliveryMethod"));
						boolean bShipable  = (eleOrderLine.getAttribute("OrderLineKey").equals (eleOrderStatus.getAttribute ("OrderLineKey")) && !YFCObject.isVoid(eleOrderStatus.getAttribute ("OrderReleaseKey")));

						if (bShipable)
						{
							YFCElement	eleShipmentLine = eleShipmentLines.createChild ("ShipmentLine");
			    			eleShipmentLine.setAttribute ("OrderHeaderKey", eleOrderLine.getAttribute ("OrderHeaderKey"));
			    			eleShipmentLine.setAttribute ("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
			    			eleShipmentLine.setAttribute ("OrderReleaseKey", eleOrderStatus.getAttribute("OrderReleaseKey"));
			    			eleShipmentLine.setAttribute ("ItemID", eleItem.getAttribute("ItemID"));
			    			eleShipmentLine.setAttribute ("UnitOfMeasure", eleItem.getAttribute ("UnitOfMeasure"));
			    			eleShipmentLine.setAttribute ("Quantity", eleOrderStatus.getAttribute ("StatusQty"));
			    			eleShipmentLine.setAttribute ("IsBackroomPickRequired", bRequiresBackroomPick);
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			env.clearApiTemplate("getOrderLineStatusList");
		}
		return docShipment;
	}
	
	private boolean	IsBackroomPickRequired (YFSEnvironment env, YFCElement eleOrderLineStatus, String sDeliveryMethod) throws Exception
	{
		YIFApi		api =  YIFClientFactory.getInstance().getLocalApi ();
		String		sRuleName = sDeliveryMethod.equals("SHP") ? "YCD_STORE_SHP_BP_REQD" : "YCD_STORE_IS_BP_REQD";
		YFCDocument	docRules = YFCDocument.getDocumentFor("<Rules RuleSetFieldName=\"" + sRuleName + "\" OrganizationCode=\"" + eleOrderLineStatus.getAttribute("ShipNode") + "\"/>");
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to getRuleDetails API:");
			System.out.println (docRules.getString());
		}
		docRules = YFCDocument.getDocumentFor(api.getRuleDetails(env, docRules.getDocument()));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getRuleDetails API:");
			System.out.println (docRules.getString());
		}
		
		YFCElement	eleRules = docRules.getDocumentElement();

		// if rule doesn't exist for this organization assume no back room pick is required
		if (YFCObject.isVoid(eleRules.getAttribute("RulesKey")))
			return false;
		return eleRules.getBooleanAttribute("RuleSetValue");
	}


	private String evaluateXPathExpression (String sXPathExpr, Document inDoc) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, inDoc, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;
		}
		return sResult;
	}

	Properties m_Props;
}
