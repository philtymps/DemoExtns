package com.custom.diab.demos.ue;



import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.ydm.japi.ue.YDMBeforeCreateShipment;

public class SEBeforeCreateShipmentUEImpl implements YDMBeforeCreateShipment {

	
	public Document beforeCreateShipment(YFSEnvironment env, Document docIn)
	throws YFSUserExitException {
		try {
			YFCDocument	docShipment = YFCDocument.getDocumentFor(docIn);
			YFCElement	eleShipment = docShipment.getDocumentElement();
			YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
			
			if (YFSUtil.getDebug())
			{
					System.out.println ("Input to beforeCreateShipment() UE is: ");
					System.out.println (docShipment.getString());
			}
			String sDocType = eleShipment.getAttribute("DocumentType");
			String sDeliveryMethod = eleShipment.getAttribute("DeliveryMethod");
			
			if(!YFCObject.isVoid(sDocType) && sDocType.contentEquals("0006")) {
				eleShipment.setAttribute("SCAC", "Y_ANY");
				eleShipment.setAttribute("CarrierServiceCode", "EXPRESS_AURE");
				eleShipment.setAttribute("ExpectedShipmentDate", YFCDateUtils.getCurrentDate(true).getString("yyyy-MM-dd"));
			}
			
			if(!YFCObject.isVoid(sDeliveryMethod) && (sDeliveryMethod.contentEquals("SHP")  && YFCCommon.isVoid(eleShipment.getAttribute("SCAC"))))
			{
				eleShipment.setAttribute("SCAC", "Y_ANY");
				eleShipment.setAttribute("CarrierServiceCode", "STANDARD_AURE");
			}

			/*			PHT - Removed this Logic for V4 BDA - No Longer Required
			Iterator	iShipmentLines = eleShipmentLines.getChildren();
			Boolean		bIsSingleOrderNo = true;
			String		sSingleOrderNo = null;
			String		sNewOrderNo, sNewOrderHeaderKey;
			while (iShipmentLines.hasNext()){
				YFCElement	eleShipmentLine = (YFCElement)iShipmentLines.next();
				sNewOrderNo = eleShipmentLine.getAttribute("OrderNo");
				if (!YFCObject.isVoid(sNewOrderNo)){
					// if order numbers all the same for each line
					if (sSingleOrderNo == null || sNewOrderNo.equals(sSingleOrderNo)){
						sSingleOrderNo = sNewOrderNo; 
					} else {
						bIsSingleOrderNo = false;
						break;
					}
				}
			}
			if (bIsSingleOrderNo){
				eleShipment.setAttribute ("OrderNo", sSingleOrderNo);
				eleShipment.setAttribute ("IsSingleOrder", "Y");
				if (YFSUtil.getDebug())
				{
					System.out.println ("Single Order Shipment OrderNo: " + sSingleOrderNo);
				}
			}
*/			
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from beforeCreateShipment() UE is: ");
				System.out.println (docShipment.getString());
			}
			return docShipment.getDocument();
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	} 
}
