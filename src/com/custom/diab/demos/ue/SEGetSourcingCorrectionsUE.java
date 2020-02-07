package com.custom.diab.demos.ue;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetSourcingCorrectionsUE;

public class SEGetSourcingCorrectionsUE implements OMPGetSourcingCorrectionsUE {

	private static YFCLogCategory logger = YFCLogCategory.instance(SEGetSourcingCorrectionsUE.class);
	
	@Override
	public Document getSourcingCorrections(YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		YFCDocument	docPromise = YFCDocument.getDocumentFor(docIn);
		YFCElement	elePromise = docPromise.getDocumentElement();
		YFCElement	elePromiseLines = elePromise.getChildElement("PromiseLines");
		YFCElement	elePromiseServiceLines = elePromise.getChildElement("PromiseServiceLines");
		YFCDocument	docOutPromise = YFCDocument.createDocument ("Promise");
		YFCElement	eleOutPromise = docOutPromise.getDocumentElement();
		YFCDocument	docOrder = YFCDocument.createDocument ("Order");
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		boolean		bHasOrderReferences = false;
		
		logger.debug("Input to getSourcingCorrections UE:");
		logger.debug(docPromise.getString());

		
		if (!YFCObject.isNull(elePromiseLines))
		{
			YFCIterable<YFCElement>	iPromiseLines = elePromiseLines.getChildren();
			YFCElement	eleOutPromiseLines = eleOutPromise.createChild ("PromiseLines");
			eleOrder.createChild("OrderLines");
			
			while (iPromiseLines.hasNext())
			{
				YFCElement	eleOutPromiseLine = eleOutPromiseLines.createChild ("PromiseLine");
				YFCElement	elePromiseLine = (YFCElement)iPromiseLines.next();
				eleOutPromiseLine.setAttribute ("LineId", elePromiseLine.getAttribute ("LineId"));

				// get the order header key if it's not yet known
				eleOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey (env, eleOrder.getAttribute("OrderHeaderKey"), elePromiseLine.getAttribute("OrderLineReference")));
		
				// log a note for this promise line as a trace of the sourcing rule used
				logNoteForSourcingRule (env, elePromiseLine, eleOutPromiseLine, eleOrder);
				bHasOrderReferences |= !YFCObject.isVoid(elePromiseLine.getAttribute("OrderLineReference"));
			}			
		}
		if (!YFCObject.isNull(elePromiseServiceLines))
		{
			YFCIterable<YFCElement>	iPromiseServiceLines = elePromiseServiceLines.getChildren();
			YFCElement	eleOutPromiseServiceLines = eleOutPromise.createChild ("PromiseServiceLines");
			eleOrder.createChild("OrderLines");
			
			while (iPromiseServiceLines.hasNext())
			{
				YFCElement	eleOutPromiseServiceLine = eleOutPromiseServiceLines.createChild ("PromiseServiceLine");
				YFCElement	elePromiseServiceLine = (YFCElement)iPromiseServiceLines.next();
				eleOutPromiseServiceLine.setAttribute ("LineId", elePromiseServiceLine.getAttribute ("LineId"));

				// get the order header key if it's not yet known
				eleOrder.setAttribute ("OrderHeaderKey", getOrderHeaderKey (env, eleOrder.getAttribute("OrderHeaderKey"), elePromiseServiceLine.getAttribute("OrderLineReference")));
		
				// log a note for this promise service line as a trace of the sourcing rule used
				logNoteForSourcingRule (env, elePromiseServiceLine, eleOutPromiseServiceLine, eleOrder);
				bHasOrderReferences |= !YFCObject.isVoid(elePromiseServiceLine.getAttribute("OrderLineReference"));
			}
		}
		//  add notes to the order showing the traced sourcing
		if (bHasOrderReferences)
		{
			try {
				YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
				logger.debug("Input to changeOrder API:");
				logger.debug(docOrder.getString());
				api.changeOrder (env, docOrder.getDocument());
			} catch (Exception e) {
				throw new YFSUserExitException(e.getMessage());
			}
		}

		return docOutPromise.getDocument();
	}
	/*
	protected	void logNoteForSourcingRule (YFSEnvironment env, YFCElement elePromiseLine, YFCElement eleOrder) throws YFSUserExitException
	{
		String		sOrderLineKey = elePromiseLine.getAttribute("OrderLineReference");
		YFCElement	eleSourcingRuleHeader = elePromiseLine.getChildElement("SourcingRuleHeader");
		
		YFCElement	eleNote = null;
		
		if (!YFCObject.isVoid(sOrderLineKey))
		{
			YFCElement	eleOrderLine = eleOrder.getChildElement ("OrderLines").createChild("OrderLine");
			eleOrderLine.setAttribute("OrderLineKey", sOrderLineKey);
			YFCElement	eleNotes = eleOrderLine.createChild("Notes");
			eleNote  = eleNotes.createChild("Note");
			eleNote.setAttribute("NoteText", eleSourcingRuleHeader.getString(false));
		}
	}
	*/
	protected String	getOrderHeaderKey (YFSEnvironment env, String sOrderHeaderKey, String sOrderLineKey) throws YFSUserExitException
	{
		if (YFCObject.isNull(sOrderHeaderKey))
		{
			try {
				YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
				YFCDocument	docOrderLineDetail = YFCDocument.getDocumentFor("<OrderLineDetail OrderLineKey=\"" + sOrderLineKey + "\"/>"); 
				YFCDocument	docOrderLineDetailsOutputTemplate = YFCDocument.getDocumentFor("<OrderLine OrderHeaderKey=\"\"/>"); 
				logger.debug("Input to getOrderLineDetails API:");
				logger.debug(docOrderLineDetail.getString());
				env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsOutputTemplate.getDocument());
				docOrderLineDetail = YFCDocument.getDocumentFor(api.getOrderLineDetails(env, docOrderLineDetail.getDocument()));
				sOrderHeaderKey = docOrderLineDetail.getDocumentElement().getAttribute("OrderHeaderKey");

			} catch (Exception e) {
				throw new YFSUserExitException(e.getMessage());
			} finally {
				env.clearApiTemplate("getOrderLineDetails");			
			}
		}
		return sOrderHeaderKey;
	}

	protected	void logNoteForSourcingRule (YFSEnvironment env, YFCElement elePromiseLine, YFCElement eleOutPromiseLine, YFCElement eleOrder) throws YFSUserExitException
	{
		String		sOrderLineKey = elePromiseLine.getAttribute("OrderLineReference");
		YFCElement	eleSourcingRuleHeader = elePromiseLine.getChildElement("SourcingRuleHeader");
		YFCElement	eleSourcingRuleDetails  = eleSourcingRuleHeader.getChildElement("SourcingRuleDetails");
		
		YFCElement	eleNote = null;
		
		if (!YFCObject.isVoid(sOrderLineKey))
		{
			YFCElement	eleOrderLine = eleOrder.getChildElement ("OrderLines").createChild("OrderLine");
			eleOrderLine.setAttribute("OrderLineKey", sOrderLineKey);
			YFCElement	eleNotes = eleOrderLine.createChild("Notes");
			eleNote  = eleNotes.createChild("Note");
			eleNote.setAttribute("NoteText", eleSourcingRuleHeader.getString(false));
		}
		// setup the output PromiseLine/PromiseServiceLine element
		eleOutPromiseLine.setAttribute("LineId", elePromiseLine.getAttribute ("LineId"));
		YFCElement	eleOutSourcingRuleDetails = eleOutPromiseLine.createChild("SourcingRuleDetails");
		YFCIterable<YFCElement>	iSourcingRuleDetails = eleSourcingRuleDetails.getChildren();
		while (iSourcingRuleDetails.hasNext())
		{
			YFCElement	eleSourcingRuleDetail = (YFCElement)iSourcingRuleDetails.next();
			
			YFCElement	eleOutSourcingRuleDetail = eleOutSourcingRuleDetails.createChild("SourcingRuleDetail");
			eleOutSourcingRuleDetail.setAttribute("SeqNo", eleSourcingRuleDetail.getAttribute ("SeqNo"));
	
			YFCElement	eleOutShipNodes = eleOutSourcingRuleDetail.createChild("ShipNodes");
			YFCElement	eleShipNodes = eleSourcingRuleDetail.getChildElement("ShipNodes");
			YFCIterable<YFCElement>	iShipNodes = eleShipNodes.getChildren();
		
			while (iShipNodes.hasNext())
			{
				YFCElement	eleShipNode = (YFCElement)iShipNodes.next();
				YFCElement	eleOutShipNode = eleOutShipNodes.createChild("ShipNode");	
				eleOutShipNode.setAttribute("ShipNode", eleShipNode.getAttribute ("ShipNode"));
				
				eleOutShipNode.setAttribute("ShipNode", eleShipNode.getAttribute("ShipNode"));
			}
		}
	}
}
