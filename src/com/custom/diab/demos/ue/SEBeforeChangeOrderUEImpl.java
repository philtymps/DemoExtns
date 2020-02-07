package com.custom.diab.demos.ue;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeChangeOrderUE;

public class SEBeforeChangeOrderUEImpl implements YFSBeforeChangeOrderUE {

	public Document beforeChangeOrder(YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		// This Handler will ensure that a service added to a PICKUP line shares the same Ship Node
		
		YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeChangeOrder() UE is: ");
				System.out.println (docOrder.getString());
		}

		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleOrderLineRelationships = eleOrder.getChildElement("OrderLineRelationships");
	
		// if we're adding a service
		if (!YFCObject.isNull(eleOrderLineRelationships))
		{
			YFCIterable<YFCElement>	iOrderLineRelationships = eleOrderLineRelationships.getChildren();
			
			// we're looking to see if there is a new Service Line being added to the order and if it's associated to a PICKUP line
			// we want to set the Service Line's ship node to match the Pickup Node to force service to be done in the pickup node
			while (iOrderLineRelationships.hasNext())
			{
				YFCElement	eleOrderLineRelationship = (YFCElement)iOrderLineRelationships.next();
				String		sRelationshipType = eleOrderLineRelationship.getAttribute("RelationshipType");
				
				if (!sRelationshipType.equals("ServiceLine"))
					return docIn;
				
				YFCElement	eleParentLine = eleOrderLineRelationship.getChildElement("ParentLine");
				YFCElement	eleChildLine  = eleOrderLineRelationship.getChildElement("ChildLine");
				YFCElement	eleParentOrderLine = getOrderLineDetails (env, eleOrder, eleParentLine);
				YFCElement	eleChildOrderLine  = getOrderLineDetails (env, eleOrder, eleChildLine);
				String 		sParentDeliveryMethod = eleParentOrderLine.getAttribute("DeliveryMethod"); 
				String		sParentShipNode = eleParentOrderLine.getAttribute("ShipNode");
				
				// if the associated order line is set for pickup and ship node is selected
				if (sParentDeliveryMethod.equals("PICK") && !YFCObject.isVoid(sParentShipNode))
					eleChildOrderLine.setAttribute("ShipNode", eleParentOrderLine.getAttribute ("ShipNode"));
			}
		}
		else
		{
			YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
			YFCIterable<YFCElement>	iOrderLines = eleOrderLines.getChildren();
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				YFCElement	eleParentOrderLine = getOrderLineDetails (env, eleOrder, eleOrderLine);
				if (eleParentOrderLine.getBooleanAttribute("HasServices"))
				{
					String 		sParentDeliveryMethod = eleParentOrderLine.getAttribute("DeliveryMethod"); 
					String		sParentShipNode = eleParentOrderLine.getAttribute("ShipNode");
					YFCElement	eleChildOrderLine = eleParentOrderLine.getChildElement("ServiceAssociations").getChildElement("ServiceAssociation").getChildElement("ChildLine");
					
					eleChildOrderLine = getOrderLineDetails (env, eleOrder, eleChildOrderLine);
					if (eleChildOrderLine.getBooleanAttribute("AddToChangeOrder"))
					{
						String		sCurrentWorkOrderKey = eleChildOrderLine.getAttribute("CurrentWorkOrderKey"); 
						if (sParentDeliveryMethod.equals("PICK") && !YFCObject.isVoid(sParentShipNode) && YFCObject.isVoid(sCurrentWorkOrderKey))
							eleChildOrderLine.setAttribute("ShipNode", eleParentOrderLine.getAttribute ("ShipNode"));
						else if (YFCObject.isVoid(sParentShipNode) && YFCObject.isVoid(sCurrentWorkOrderKey))
							eleChildOrderLine.setAttribute("ShipNode", "");
						eleOrderLines.appendChild(eleChildOrderLine);
					}
						
				}
				
			}

		}
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from beforeChangeOrder() UE is: ");
			System.out.println (docOrder.getString());
		}
		docIn = docOrder.getDocument();
		
		return docIn;
	}
	
	YFCElement	getOrderLineDetails (YFSEnvironment env, YFCElement eleOrder, YFCElement eleOrderLine) throws YFSUserExitException
	{
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		YFCIterable<YFCElement>	iOrderLines = eleOrderLines.getChildren();
		String		sOrderHeaderKey = eleOrder.getAttribute("OrderHeaderKey");
		String		sTransactionalLineId = eleOrderLine.getAttribute("TransactionalLineId");
		String		sOrderLineKey = eleOrderLine.getAttribute("OrderLineKey");
		YFCElement	eleOrderLineDetails = null;
		
		if (!YFCObject.isVoid(sOrderLineKey))
		{
			
			try {
				YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
				YFCDocument	docOrderLineDetailsInput = YFCDocument.getDocumentFor ("<OrderLineDetail OrderHeaderKey=\"" + sOrderHeaderKey + "\" OrderLineKey=\"" + sOrderLineKey + "\"/>");
				YFCDocument	docOrderLineDetailsTemplate = YFCDocument.getDocumentFor ("<OrderLine DeliveryMethod=\"\" CurrentWorkOrderKey=\"\" OrderLineKey=\"\" ShipNode=\"\"><ServiceAssociations><ServiceAssociation><ParentLine/><ChildLine/><ServiceAssociation/><ServiceAssociations></OrderLine>");
				env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsTemplate.getDocument());
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getOrderLineDetails:");
					System.out.println (docOrderLineDetailsInput.getString());
				}
				YFCDocument	docOrderLineDetails = YFCDocument.getDocumentFor(api.getOrderLineDetails(env,  docOrderLineDetailsInput.getDocument()));
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getOrderLineDetails:");
					System.out.println (docOrderLineDetails.getString());
				}
				eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
				if (!YFCObject.isVoid(eleOrderLineDetails.getChildElement("ServiceAssociations").getChildElement("ServiceAssociation")))
				{
					eleOrderLineDetails.setAttribute ("HasServiceLines", "Y");
					eleOrderLineDetails.setAttribute ("AddToChangeOrder", "Y");
				}
			} catch (Exception e) {
				throw new YFSUserExitException (e.getMessage());
			} finally {
				env.clearApiTemplate("getOrderLineDetails");
			}

		}
		else
		{
			while (iOrderLines.hasNext())
			{
				eleOrderLine = (YFCElement)iOrderLines.next();
				if (eleOrderLine.getAttribute("TransactionalLineId").equals(sTransactionalLineId))
						eleOrderLineDetails = eleOrderLine;
			}
		}
		return eleOrderLineDetails;
	}
}

