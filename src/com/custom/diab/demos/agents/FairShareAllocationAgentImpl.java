package com.custom.diab.demos.agents;

import java.util.List;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;


public class FairShareAllocationAgentImpl extends com.yantra.omp.agent.OMPItemBasedAllocationAgent 
{
	
	@SuppressWarnings({ "rawtypes" })
	
	public List getJobs (YFSEnvironment env, Document inXML, Document lastMessageXml)
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Fair Share Allocation - getJobs() Input:");
			System.out.println (YFCDocument.getDocumentFor (inXML).getString());
		}
		return(super.getJobs (env, inXML, lastMessageXml));
	}
	
	public void executeJob(YFSEnvironment env, Document inXML) {
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Fair Share Allocation - executeJob Input:");
			System.out.println (YFCDocument.getDocumentFor (inXML).getString());
		}
		
		// execute the Item Based Allocation Agent Code
		super.executeJob(env, inXML);
		
		try {
			// raise ON_SUCCESS event
			completeJob(env, YFCDocument.getDocumentFor(inXML));
		} 
		catch (Exception ignore) 
		{
			
		}
	}

	public void	completeJob (YFSEnvironment env, YFCDocument docJobXml) throws Exception
	{
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
		YFCElement	eleJobXml = docJobXml.getDocumentElement();

		
		// raise the ON_SUCCESS event for this transaction
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Fair Share Allocation - completeJob() Input:");
			System.out.println (eleJobXml.getString());
		}

		YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
		YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
		String		sTransactionId = "CPG_FAIR_SHARE_ALLOC.2001.ex";
		
		eleRaiseEvent.setAttribute ("TransactionId", sTransactionId);
		eleRaiseEvent.setAttribute ("EventId", "ON_SUCCESS");
		YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
		YFCElement	eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TransactionId");
		eleData.setAttribute ("Value", sTransactionId);		
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "HoldType");
		eleData.setAttribute ("Value", "CPG_IBA_HOLD");
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "IBATriggerKey");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("IBATriggerKey"));

		
		YFCElement	eleDataType = eleRaiseEvent.createChild ("DataType");
		eleDataType.setNodeValue ("0");	// Data Type is 1 for TYPE_XML_STRING or 0 for TYPE_JAVA_MAP
		
		//YFCElement	eleXmlData = eleRaiseEvent.createChild ("XMLData");
		//eleXmlData.setNodeValue (docJobXml.getString());		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to RaiseEvent:");
			System.out.println (docRaiseEvent.getString());
		}
		api.raiseEvent (env, docRaiseEvent.getDocument());	
	}
}
