package com.custom.diab.demos.ue;

import org.w3c.dom.Document;

import java.util.Iterator;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;


import com.yantra.ydm.japi.ue.YDMDetermineShipmentToConsolidateWith;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class SEDetermineShipmentToConsolidateWithUE implements YDMDetermineShipmentToConsolidateWith 
{
	// this user exit implementation copies all of the instructions from the associated shipment 
	// release to the shipment so that
	@SuppressWarnings("unused")
	private static YFCLogCategory logger = YFCLogCategory.instance(SEDetermineShipmentToConsolidateWithUE.class);
	

	@SuppressWarnings("rawtypes")
	@Override
	public Document determineShipmentToConsolidateWith(YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		YFCDocument	docDetermineConsolidateToShipment = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleDetermineConsolidateToShipment = docDetermineConsolidateToShipment.getDocumentElement();


//		logger.debug("Input to DetermineShipmentToConsolidateWith UE:");
//		logger.debug(docDetermineConsolidateToShipment.getString());
		if (YFSUtil.getDebug())
		{
			System.out.println("Input to DetermineShipmentToConsolidateWith UE:");
			System.out.println(docDetermineConsolidateToShipment.getString());
		}
		
		/* This User Exit will find Shipments (Inbound or Outbound) that share the following:
		 * 		Ship Node and Receive Node (typical for Transfer Orders)
		 * 		Ship To CustomerID and Bill To CustomerID (typical for Sales Orders)
		 * 		Enterprise, Seller, Buyer Organization Codes
		 * 		Document Type
		 * 		Status in Created or Ready for Backroom Pick Only
		 */
		YFCElement	eleShipmentToConsolidate = eleDetermineConsolidateToShipment.getChildElement("Shipment");
		String		sBuyerOrganizationCode = eleShipmentToConsolidate.getAttribute("BuyerOrganizationCode");
		String		sSellerOrganizationCode = eleShipmentToConsolidate.getAttribute("SellerOrganizationCode");
		String		sDocumentType = eleShipmentToConsolidate.getAttribute("DocumentType");
		String		sShipNode = eleShipmentToConsolidate.getAttribute("ShipNode");
		String		sReceivingNode = eleShipmentToConsolidate.getAttribute("ReceivingNode");
		String		sEnterpriseCode = eleShipmentToConsolidate.getAttribute("EnterpriseCode");
		String		sShipToCustomerId = eleShipmentToConsolidate.getAttribute("ShipToCustomerId");
		String		sBillToCustomerId = eleShipmentToConsolidate.getAttribute("BillToCustomerId");
		String		sDeliveryMethod = eleShipmentToConsolidate.getAttribute("DeliveryMethod");
		
		YFCDocument	docShipmentOut = YFCDocument.createDocument("Shipment");
		YFCElement	eleShipmentOut = docShipmentOut.getDocumentElement();
		YFCElement	eleShipments = eleDetermineConsolidateToShipment.getChildElement("Shipments");
		Iterator	iShipments = (Iterator)eleShipments.getChildren();
		eleShipmentOut.setAttribute ("CreateNewShipment", "Y");
		
		// if system found no shipments to consolidate
		if (!iShipments.hasNext() && (sDocumentType.equals("0001") || sDocumentType.equals("0005") || sDocumentType.equals("0006")))
		{
			try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument docShipmentList = YFCDocument.createDocument ("Shipment");
			YFCElement  eleShipmentList = docShipmentList.getDocumentElement();
			YFCDocument	docShipmentListTemplate = YFCDocument.parse(getClass().getResourceAsStream("/global/template/userexit/determineShipmentToConsolidateWith.xml"));
			env.setApiTemplate("getShipmentList", docShipmentListTemplate.getDocument());
			
			eleShipmentList.setAttribute ("DocumentType", sDocumentType);
			eleShipmentList.setAttribute ("EnterpriseCode", sEnterpriseCode);
			eleShipmentList.setAttribute ("ShipNode", sShipNode);
			eleShipmentList.setAttribute ("BuyerOrganizationCode", sBuyerOrganizationCode);
			eleShipmentList.setAttribute ("SellerOrganizationCode", sSellerOrganizationCode);
			eleShipmentList.setAttribute ("DeliveryMethod", sDeliveryMethod);
			if (!YFCCommon.isVoid(sReceivingNode))
				eleShipmentList.setAttribute ("ReceivingNode", sReceivingNode);
			
//			logger.debug ("Input to getShipmentList API:");
//			logger.debug (docShipmentList.getString());
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getShipmentList API:");
				System.out.println (docShipmentList.getString());
			}
			
			// Call getShipmentList API to find similar shipments
			docShipmentList = YFCDocument.getDocumentFor (api.getShipmentList (env, docShipmentList.getDocument()));
			eleShipments = docShipmentList.getDocumentElement();
			iShipments = (Iterator)eleShipments.getChildren();

//			logger.debug ("Output from getShipmentList API:");
//			logger.debug (docShipmentList.getString());

			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getShipmentList API:");
				System.out.println (docShipmentList.getString());
			}

			} catch (Exception e) {
				throw new YFSUserExitException (e.getMessage());
			} finally {
				env.clearApiTemplate("getShipmentList");
			}
		}
		// search for eligible shipments we can consolidate with
		while (iShipments.hasNext())
		{
			YFCElement	eleShipment = (YFCElement)iShipments.next();
			
			// for sales orders (outbound) make sure customer is the same before consolidating
			if (sDocumentType.equals("0001") &&
				(!eleShipment.getAttribute("ShipToCustomerId").equals (sShipToCustomerId) ||
				 !eleShipment.getAttribute("BillToCustomerId").equals (sBillToCustomerId)))
				 	continue;
			
			//  Only Shipments in Created or Ready for Backroom Pick status can be consolidated 
			if (eleShipment.getAttribute("Status").equals("1100") || eleShipment.getAttribute("Status").equals("1100.70.06.10"))
			{
				eleShipmentOut.setAttribute("CreateNewShipment", "N");
				eleShipmentOut.setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
				break;
			}
		}
		
//		logger.debug("Output from DetermineShipmentToConsolidateWith UE:");
//		logger.debug(docShipmentOut.getString());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from DetermineShipmentToConsolidateWith UE:");
			System.out.println (docShipmentOut.getString());
		}

		return docShipmentOut.getDocument();
	}
}
