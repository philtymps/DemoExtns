package com.custom.diab.demos.ue;

import java.util.Iterator;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ydm.japi.ue.YDMGetShipmentInstructionsUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class SEGetShipmentInstructionsUEImpl implements YDMGetShipmentInstructionsUE {

	// this user exit implementation copies all of the instructions from the associated shipment 
	// release to the shipment so that
	private static YFCLogCategory logger = YFCLogCategory.instance(SEGetShipmentInstructionsUEImpl.class);
	
	@SuppressWarnings("rawtypes")
	public Document getShipmentInstructions (YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		YFCDocument	docShipment = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleShipment = docShipment.getDocumentElement();

		logger.debug("Input to getShipmentInstructions UE:");
		logger.debug(docShipment.getString());

		String		sShipmentKey = eleShipment.getAttribute("ShipmentKey");
		YFCDocument	docShipmentLineListTemplate = YFCDocument.getDocumentFor ("<ShipmentLines><ShipmentLine OrderReleaseKey=\"\"/></ShipmentLines>");
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument docShipmentLine = YFCDocument.getDocumentFor ("<ShipmentLine ShipmentKey=\"" + sShipmentKey + "\"/>");
			env.setApiTemplate("getShipmentLineList", docShipmentLineListTemplate.getDocument());
			if (YFSUtil.getDebug())
			{
				logger.debug ("Input to getShipmentLineList:");
				logger.debug (docShipment.getString());
			}
			YFCDocument docShipmentLineList = YFCDocument.getDocumentFor (api.getShipmentLineList (env, docShipmentLine.getDocument()));
			env.clearApiTemplate("getShipmentLineList");
			if (YFSUtil.getDebug())
			{
				logger.debug ("Output from getShipmentLineList:");
				logger.debug (docShipmentLineList.getString());
			}
			
			// get release key
			String	sOrderReleaseKey = docShipmentLineList.getDocumentElement().getFirstChildElement().getAttribute("OrderReleaseKey");
			
			// get the release details for the given shipment.  this has gift and other order/order line instructions
			YFCDocument	docOrderRelease = YFCDocument.getDocumentFor(api.getOrderReleaseDetails (env, YFCDocument.getDocumentFor("<OrderReleaseDetail OrderReleaseKey=\"" + sOrderReleaseKey + "\"/>").getDocument()));
			if (YFSUtil.getDebug())
			{
				logger.debug ("Output from getOrderReleaseDetails:");
				logger.debug (docOrderRelease.getString());
			}
			String sDocType = docOrderRelease.getDocumentElement().getAttribute("DocumentType");

			// if not a Sales Order Shipment
			if (YFCObject.isVoid(sDocType) || !sDocType.equals("0001"))
					return docIn;
			
			// get order line elements from the release
			YFCNodeList nlOrderLines = docOrderRelease.getDocumentElement().getElementsByTagName("OrderLine");
			Iterator	iOrderLines = nlOrderLines.iterator();
			
			YFCDocument	docOrderLineDetailsTemplate = YFCDocument.getDocumentFor ("<OrderLine OrderLineKey=\"\"><Instructions><Instruction InstructionText=\"\" InstructionType=\"\"/></Instructions></OrderLine>");
			env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsTemplate.getDocument());
			YFCElement	eleShipmentInstructions = eleShipment.createChild("Instructions");

			// iterator over all the order lines
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				String	sOrderLineKey = eleOrderLine.getAttribute("OrderLineKey");
				YFCDocument	docOrderLineDetails = YFCDocument.getDocumentFor("<OrderLineDetail OrderLineKey=\"" + sOrderLineKey + "\"/>");
				docOrderLineDetails = YFCDocument.getDocumentFor (api.getOrderLineDetails(env, docOrderLineDetails.getDocument()));
				if (YFSUtil.getDebug())
				{
					logger.debug ("Output from getOrderLineDetails:");
					logger.debug (docOrderLineDetails.getString());
				}
				YFCElement	eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
				YFCElement	eleInstructions = eleOrderLineDetails.getChildElement("Instructions");

				// iterator over all the order line instructions
				if (!YFCObject.isVoid(eleInstructions))
				{
					Iterator	iInstructions = eleInstructions.getChildren();
					while (iInstructions.hasNext())
					{
						YFCElement	eleOrderLineInstruction = (YFCElement)iInstructions.next();
						YFCElement	eleShipmentInstruction = eleShipmentInstructions.createChild("Instruction");
						eleShipmentInstruction.setAttribute("InstructionType", eleOrderLineInstruction.getAttribute("InstructionType"));
						eleShipmentInstruction.setAttribute("InstructionText", eleOrderLineInstruction.getAttribute("InstructionText"));
						eleShipmentInstruction.setAttribute("ReferenceKey", sOrderLineKey);
						eleShipmentInstruction.setAttribute("TableName", "YFS_ORDER_LINE");	
					}
				}
			}
			env.clearApiTemplate("getOrderLineDetails");
			logger.debug ("Output from getShipmentInstructions UE:");
			logger.debug (docShipment.getString());
			
			return docShipment.getDocument();
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
		
	}
}
