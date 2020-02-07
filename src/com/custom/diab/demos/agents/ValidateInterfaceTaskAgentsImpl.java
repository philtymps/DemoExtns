/**
  * ValidateInterfaceAgentsImpl.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.agents;

import org.w3c.dom.*;
import com.yantra.yfs.japi.*;
import com.yantra.ycp.japi.util.*;
import com.yantra.yfc.dom.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.custom.yantra.util.*;
import java.util.*;

public class ValidateInterfaceTaskAgentsImpl extends YCPBaseTaskAgent
{
    public ValidateInterfaceTaskAgentsImpl()
    {
		if (YFSUtil.getDebug())
		{
			System.out.println ("In ValidateInterfaceTaskAgentsImpl Constructor");
		}
    }

	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception 
	{
		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent VALIDATE_TELCO (ValidateInterfaceAgentsImpl)");
			System.out.println ("Task Input:");
			System.out.println (docTaskQueue.getString());
		}
		
		// raise an event to trigger the validation
		raiseValidationEvent (env, eleTaskQueue);
		
		// change orderline status to drop status of this transaction
		changeOrderLineStatus (env, eleTaskQueue);

		// register the task record as completed
		registerTaskComplete (env, eleTaskQueue);

		return null;
    }

		
	private void	raiseValidationEvent (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();


			// raise the ON_SUCCESS event for this transaction
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Raising Event ON_VALIDATE_EVENT:");
			}

			// get order details to send to validation event
			YFCDocument	docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"" + eleTaskQueue.getAttribute ("DataKey") + "\"/>");
			docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
						
			YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
			YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
			eleRaiseEvent.setAttribute ("TransactionId", eleTaskQueue.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));
			eleRaiseEvent.setAttribute ("EventId", "ON_VALIDATE");
			YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
			YFCElement	eleData = eleDataMap.createChild ("Data");
			eleData.setAttribute ("Name", "TransactionId");
			eleData.setAttribute ("Value", eleTaskQueue.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));

			YFCElement	eleDataType = eleRaiseEvent.createChild ("DataType");
			eleDataType.setNodeValue ("1");	// Data Type is 1 for TYPE_XML_STRING or 0 for TYPE_JAVA_MAP
			YFCElement	eleXmlData = eleRaiseEvent.createChild ("XMLData");
			eleXmlData.setNodeValue (docOrder.getString());	
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to RaiseEvent:");
				System.out.println (docRaiseEvent.getString());
			}
			api.raiseEvent (env, docRaiseEvent.getDocument());	
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

		// for each of the orderlines in this order header key
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

