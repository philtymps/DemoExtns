package com.custom.diab.demos.ue;



import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
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

			// if not a Sales Order Shipment or Shipment lines don't exists we skip this processing
			if (YFCObject.isVoid(sDocType) || !sDocType.equals("0001") || YFCObject.isNull(eleShipmentLines))
				return docIn;

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
