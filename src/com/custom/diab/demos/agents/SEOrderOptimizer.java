package com.custom.diab.demos.agents;

import java.util.Iterator;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;

public class SEOrderOptimizer extends YCPBaseTaskAgent {

	@Override
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception
	{
		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent OPTIMIZE (SEOrderOptimizer.java)");
			System.out.println ("Task Input:");
			System.out.println (docTaskQueue.getString());
		}
		
		// get order details to send to validation event
		YFCDocument	docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"" + eleTaskQueue.getAttribute ("DataKey") + "\"/>");
		YFCDocument docOutputTemplate = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"\" OrderName=\"\"><OrderLines><OrderLine OrderLineKey=\"\" DeliveryMethod=\"\" ShipNode=\"\" /></OrderLines></Order>");
	    env.setApiTemplate("getOrderDetails", docOutputTemplate.getDocument());
		docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
		env.clearApiTemplate("getOrderDetails");
		
		// Call OPTIMIZER to Change ShipNode attribute of order lines to represent optimization decisions
		// TO DO: Implement this optimization logic here
		optimizeOrder (env, docOrder);
		
		changeOrderLineStatus (env, eleTaskQueue);
		
		registerTaskComplete (env, eleTaskQueue);
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private void	optimizeOrder (YFSEnvironment env, YFCDocument docOrder) throws Exception
	{
		YFCElement eleOrder = docOrder.getDocumentElement();
		String		sOrderName = eleOrder.getAttribute ("OrderName");
		String		sShipNode = "Aurora_WH1";
		boolean		bChanged = false;
		
		// find all lines that are shipping and optimize them to ship to one of three stores using order name as the optimal store indicator
		if (sOrderName.contains("Store 1"))
				sShipNode = "Auro_Store_1";
		else if (sOrderName.contains("Store 2"))
				sShipNode = "Auro_Store_2";
		else if (sOrderName.contains("Store 3"))
				sShipNode = "Auro_Store_3";
		
		// modify shipping lines to ship out of store selected for optimization
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
		while (iOrderLines.hasNext())
		{
			// for each line that has DeliveryMethod="SHP"
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			if (eleOrderLine.getAttribute("DeliveryMethod").equals("SHP"))
			{
				eleOrderLine.setAttribute("ShipNode", sShipNode);
				bChanged=true;
			}	
		}
		if (bChanged)
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Calling changeOrder API Input:)");
				System.out.println (docOrder.getString());
			}

			YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
			api.changeOrder(env, docOrder.getDocument());
		}
	}

	@SuppressWarnings("rawtypes")
	private void 	changeOrderLineStatus (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{

		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();

		// get all the lines on the current order in the pickup status for this transaction					
		eleOrderLineStatusList.setAttribute ("TransactionId", eleTaskQueue.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));
		eleOrderLineStatusList.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));		
		docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
		eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();

		// get the lone drop status (assumes a single drop status)
		YFCElement	eleDropStatuses = eleOrderLineStatusList.getChildElement ("DropStatuses");
		YFCElement	eleDropStatus = eleDropStatuses.getChildElement ("DropStatus");		
		String 		sDropStatus = eleDropStatus.getAttribute ("Status");

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
