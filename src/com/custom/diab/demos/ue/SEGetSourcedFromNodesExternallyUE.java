package com.custom.diab.demos.ue;

import java.util.Iterator;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetSourcedFromNodesExternallyUE;

public class SEGetSourcedFromNodesExternallyUE implements OMPGetSourcedFromNodesExternallyUE {

	public SEGetSourcedFromNodesExternallyUE() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Document getSourcedFromNodesExternally(YFSEnvironment env, Document docIn) throws YFSUserExitException {
		// TODO Auto-generated method stub
		YFCDocument	docPromise = YFCDocument.getDocumentFor(docIn);
		YFCElement	elePromise = docPromise.getDocumentElement();
		String		sEnterpriseCode = elePromise.getAttribute("EnterpriseCode");
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to SEGetSourcedFromNodesExternallyUE");
			System.out.println (docPromise.getString());
		}
		YFCElement						elePromiseLines = elePromise.getChildElement("PromiseLines");
		Iterator<YFCElement>			iPromiseLines = elePromiseLines.getChildren();

		YFCDocument	docOutPromise = YFCDocument.createDocument ("Promise");
		YFCElement	eleOutPromise = docOutPromise.getDocumentElement();
		YFCElement	eleOutPromiseLines = eleOutPromise.createChild("PromiseLines");
		
		while (iPromiseLines.hasNext())
		{
			YFCElement	elePromiseLine = iPromiseLines.next();
			YFCElement	eleSourcingRuleDetail = elePromiseLine.getChildElement("SourcingRuleDetail");
			YFCElement	eleSourcingTemplate = eleSourcingRuleDetail.getChildElement("SourcingTemplate");
			YFCElement	eleAdditionalData = eleSourcingTemplate.getChildElement("AdditionalData");
			String		sExternallyDefinedReasonCode = eleAdditionalData.getAttribute("ExternallyDefinedReasonCode");
			
			if (!YFCObject.isVoid(sExternallyDefinedReasonCode) && sExternallyDefinedReasonCode.equals("DROPSHIP"))
			{
				YFCElement	eleOutPromiseLine = eleOutPromiseLines.createChild("PromiseLine");
				eleOutPromiseLine.setAttribute("LineId", elePromiseLine.getAttribute("LineId"));
				YFCElement	eleOutShipNodes = eleOutPromiseLine.createChild("ShipNodes");
				
				getQualfiyingDSVS (env, sEnterpriseCode, getLineTotal (env, elePromiseLine), eleOutShipNodes);
			}
		}
		elePromiseLines = elePromise.createChild("PromiseLines");
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from SEGetSourcedFromNodesExternallyUE");
			System.out.println (docOutPromise.getString());
		}
		return docOutPromise.getDocument();
	}
	
	protected	void	getQualfiyingDSVS (YFSEnvironment env, String sEnterpriseCode, double dblDSVLineTotal, YFCElement eleShipNodes) throws YFSUserExitException
	{
  		YFCDocument docCommonCodes = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCodes = docCommonCodes.getDocumentElement ();
		eleCommonCodes.setAttribute ("CodeType", "DEMO_DSV_MINORDERAMT");
		eleCommonCodes.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Input to getCommonCodeList() API:");
			System.out.println (docCommonCodes.getString());
		}
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
			 docCommonCodes = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCodes.getDocument()));
			if (YFSUtil.getDebug()) {
				System.out.println ("Output from getCommonCodeList() API:");
				System.out.println (docCommonCodes.getString());
			}
		} catch (Exception e) {
			throw (new YFSUserExitException (e.getMessage()));
		}
	  	eleCommonCodes = docCommonCodes.getDocumentElement ();
		Iterator<YFCElement>	iCommonCodes = eleCommonCodes.getChildren();
		while (iCommonCodes.hasNext())
		{
			YFCElement eleCommonCode = iCommonCodes.next();
			double dblMinDSVOrderAmt = eleCommonCode.getDoubleAttribute ("CodeShortDescription");
			System.out.println ("Line Total:  " + dblDSVLineTotal);
			System.out.println ("DSV Minimum: " + dblMinDSVOrderAmt);
			if (dblMinDSVOrderAmt <= dblDSVLineTotal)
			{
				YFCElement	eleShipNode = eleShipNodes.createChild("ShipNode");
				eleShipNode.setAttribute("ShipNode", eleCommonCode.getAttribute("CodeValue"));
			}
		}
	}

	protected	double	getLineTotal (YFSEnvironment env, YFCElement elePromiseLine)
	{
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			YFCDocument	docOrderLineDetailsTemplate = YFCDocument.getDocumentFor("<OrderLine OrderLineKey=\"\" OrderHeaderKey=\"\"><LinePriceInfo LineTotal=\"\"/></OrderLine>");
			
			YFCDocument	docOrderLineDetails = YFCDocument.createDocument("OrderLineDetail");
			YFCElement	eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
			eleOrderLineDetails.setAttribute("OrderLineKey", elePromiseLine.getAttribute("OrderLineReference"));
			env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsTemplate.getDocument());
			docOrderLineDetails = YFCDocument.getDocumentFor(api.getOrderLineDetails(env, docOrderLineDetails.getDocument()));
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getOrderLineDetails");
				System.out.println (docOrderLineDetails.getString());
			}
			env.clearApiTemplate("getOrderLineDetails");
			eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
			YFCElement	eleLinePriceInfo = eleOrderLineDetails.getChildElement("LinePriceInfo");
			if (!YFCObject.isVoid(eleLinePriceInfo))
				return eleLinePriceInfo.getDoubleAttribute("LineTotal");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			env.clearApiTemplate("getOrderLineDetails");
		}
		return 0;
	}
	
	protected Document callServiceOrApi(YFSEnvironment env, Document docIn, String sServiceName, boolean bIsService)
	{
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			Document dResponse;
			if (bIsService)
					dResponse = api.executeFlow(env, sServiceName, docIn);
			else
					dResponse = api.invoke(env, sServiceName, docIn);
			return dResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
