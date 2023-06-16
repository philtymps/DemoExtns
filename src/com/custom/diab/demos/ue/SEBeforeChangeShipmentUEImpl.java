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
import com.yantra.ydm.japi.ue.YDMBeforeChangeShipment;

public class SEBeforeChangeShipmentUEImpl implements YDMBeforeChangeShipment {

	
	public Document beforeChangeShipment(YFSEnvironment env, Document docIn)
	throws YFSUserExitException {
		try {
			YFCDocument	docShipment = YFCDocument.getDocumentFor(docIn);
			YFCElement	eleShipment = docShipment.getDocumentElement();
			YFCElement	eleScacAndService = eleShipment.getChildElement("ScacAndService");
			
			if (YFSUtil.getDebug())
			{
					System.out.println ("Input to beforeChangeShipment() UE is: ");
					System.out.println (docShipment.getString());
			}
			
			String sDocType = eleShipment.getAttribute("DocumentType");
			String sDeliveryMethod = eleShipment.getAttribute("DeliveryMethod");

			if (!YFCObject.isVoid(sDeliveryMethod) && sDeliveryMethod.equals("SHP") && YFCObject.isNull(eleScacAndService))
			{
				
				if(!YFCObject.isVoid(sDocType) && sDocType.equals("0006")) {
					eleShipment.setAttribute("SCAC", "Y_ANY");
					eleShipment.setAttribute("CarrierServiceCode", "EXPRESS_AURE");
					eleScacAndService = eleShipment.createChild("ScacAndService");
					eleScacAndService.setAttribute("ScacKey", "Y_ANY");
					eleScacAndService.setAttribute("ScacAndService", "Y_ANY - EXPRESS_AURE");
					eleShipment.setAttribute("ExpectedShipmentDate", YFCDateUtils.getCurrentDate(true).getString("yyyy-MM-dd"));
				}
				
				if(!YFCObject.isVoid(sDeliveryMethod) && (sDeliveryMethod.contentEquals("SHP")  && YFCCommon.isVoid(eleShipment.getAttribute("SCAC"))))
				{
					eleShipment.setAttribute("SCAC", "Y_ANY");
					eleShipment.setAttribute("CarrierServiceCode", "STANDARD_AURE");
					eleScacAndService = eleShipment.createChild("ScacAndService");
					eleScacAndService.setAttribute("ScacKey", "Y_ANY");
					eleScacAndService.setAttribute("ScacAndService", "Y_ANY - STANDARD_AURE");
				}
			}
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from beforeChangeShipment() UE is: ");
				System.out.println (docShipment.getString());
			}
			return docShipment.getDocument();
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	} 
}
