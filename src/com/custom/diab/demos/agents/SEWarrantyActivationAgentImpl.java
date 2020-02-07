package com.custom.diab.demos.agents;

import java.util.Iterator;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;

public class SEWarrantyActivationAgentImpl extends YCPBaseTaskAgent {

	@Override
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception {
		// TODO Auto-generated method stub
		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent " + eleTaskQueue.getAttribute("TransactionId") + " (SEWarrantyActivationAgentImpl.java)");
			System.out.println ("Task Input:");
			System.out.println (docTaskQueue.getString());
		}
				
		changeOrderLineStatus (env, eleTaskQueue);
		
		registerTaskComplete (env, eleTaskQueue);
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void 	changeOrderLineStatus (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{

		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
		YFCElement	eleTransactionFilters = eleTaskQueue.getChildElement("TransactionFilters");
		// get all the lines on the current order in the pickup status for this transaction					
		eleOrderLineStatusList.setAttribute ("TransactionId", eleTransactionFilters.getAttribute ("TransactionId"));
		eleOrderLineStatusList.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));		
		docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
		eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();

		// get the lone drop status (assumes a single drop status)
		YFCElement	eleDropStatuses = eleOrderLineStatusList.getChildElement ("DropStatuses");
		YFCElement	eleDropStatus = eleDropStatuses.getChildElement(("DropStatus"));
		String 		sDropStatus = eleDropStatus.getAttribute ("Status");
		Iterator	iDropStatuses = eleDropStatuses.getChildren();
		String		sSimulatedDropStatus = eleTransactionFilters.getAttribute("SimulateDropStatus");

		// look over all the drop statuses and match the one passed into the criteria parameters
		if (!YFCObject.isVoid(sSimulatedDropStatus))
		{
			while (iDropStatuses.hasNext())
			{
				eleDropStatus = (YFCElement)iDropStatuses.next();
				if (eleDropStatus.getAttribute("Status").equals(sSimulatedDropStatus))
				{
					sDropStatus = eleDropStatus.getAttribute ("Status");
					break;
				}
			}
		}

		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
		
		eleOrderStatusChange.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));
		eleOrderStatusChange.setAttribute ("TransactionId", eleTaskQueue.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));
		YFCElement	eleOrderLines = eleOrderStatusChange.createChild ("OrderLines");

		// for each of the order lines picked up by the agent
		Iterator	iOrderLineStatus = eleOrderLineStatusList.getChildren();

		while (iOrderLineStatus.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatus.next();
			
			if (eleOrderLineStatus.getNodeName().equals ("OrderStatus"))
			{
				YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
				eleOrderLine.setAttribute ("OrderLineKey", eleOrderLineStatus.getAttribute ("OrderLineKey"));
				eleOrderLine.setAttribute ("BaseDropStatus", sDropStatus);
				eleOrderLine.setAttribute ("ChangeForAllAvailableQty", "Y");
			}
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to changeOrderStatus:");
			System.out.println (docOrderStatusChange.getString());
		}
		api.changeOrderStatus (env, docOrderStatusChange.getDocument());		
	}	

	private void	registerTaskComplete (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		
		YFCDocument	docRegisterTaskCompletion = YFCDocument.createDocument ("RegisterProcessCompletionInput");
		YFCElement	eleRegisterTaskCompletion = docRegisterTaskCompletion.getDocumentElement ();
		
		eleRegisterTaskCompletion.setAttribute ("KeepTaskOpen", "N");
		YFCElement	eleCurrentTask = eleRegisterTaskCompletion.createChild ("CurrentTask");
		eleCurrentTask.setAttribute ("TaskQKey", eleTaskQueue.getAttribute ("TaskQKey"));
		eleCurrentTask.setAttribute ("DataKey", eleTaskQueue.getAttribute ("DataKey"));
		eleCurrentTask.setAttribute ("DataType", eleTaskQueue.getAttribute ("DataType"));
		eleCurrentTask.setAttribute ("TransactionId", eleTaskQueue.getChildElement("TransactionFilters").getAttribute ("TransactionId"));
		eleCurrentTask.setAttribute ("AvailableDate", eleTaskQueue.getAttribute ("AvailableDate"));
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Calling registerProcessCompletion API:");
			System.out.println (docRegisterTaskCompletion.getString());
		}
		
		api.registerProcessCompletion (env, docRegisterTaskCompletion.getDocument());	
	}

}
