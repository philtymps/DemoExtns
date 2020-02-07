/**
  * TmsInterfaceAgentsImpl.java
  *
  **/

// PACKAGE
package com.custom.at21.agents;

import org.w3c.dom.*;
import com.yantra.yfs.japi.*;
import com.yantra.ycp.japi.util.*;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.core.YFCObject;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.custom.yantra.util.*;
import java.util.*;

public class TmsInterfaceAgentsImpl extends YCPBaseAgent
{
    public TmsInterfaceAgentsImpl()
    {
		if (YFSUtil.getDebug())
		{
			System.out.println ("In TmsInterfaceAgentsImpl Constructor");
		}
    }
  
	@SuppressWarnings({ "rawtypes" })
	public java.util.List getJobs (YFSEnvironment env, Document inXML, Document lastMessageXml)
	{
		String sDebugOn = inXML.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));

		ArrayList<Document> lstJobs = null;
		boolean	  bMinOrderStatusesExist = false;
		
		try {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to getJobs:");
				System.out.println (YFCDocument.getDocumentFor (inXML).getString());
			}

			YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
			YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
			YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
			YFCDocument	docMessageXml = YFCDocument.getDocumentFor (inXML);
			YFCElement	eleMessageXml = docMessageXml.getDocumentElement ();
			
			eleOrderLineStatusList.setAttribute ("TransactionId", eleMessageXml.getAttribute ("TransactionId"));
			eleOrderLineStatusList.setAttribute ("MaximumRecords", eleMessageXml.getAttribute ("NumRecordsToBuffer"));
				
			docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
			eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();

			// get minimum records to send to Optimizer
			long	lMinimumRecords = eleMessageXml.getLongAttribute  ("MinimumRecords");

			// if minimum order status exist
			if (bMinOrderStatusesExist = canSendToOptimizer (env, eleOrderLineStatusList, lMinimumRecords))
			{
				eleOrderLineStatusList.setAttribute ("TransactionId", eleMessageXml.getAttribute ("TransactionId"));
				eleOrderLineStatusList.setAttribute ("MaximumRecords", eleMessageXml.getAttribute ("NumRecordsToBuffer"));
				eleOrderLineStatusList.setAttribute ("OptimizationType", eleMessageXml.getAttribute ("OptimizationType"));
				eleOrderLineStatusList.setAttribute ("DebugOn", eleMessageXml.getAttribute ("DebugOn"));

				// note the following assumes a single drop status is present in the transaction
				YFCElement	eleDropStatuses = eleOrderLineStatusList.getChildElement ("DropStatuses");
				YFCElement	eleDropStatus = eleDropStatuses.getChildElement ("DropStatus");		
				eleOrderLineStatusList.setAttribute ("DropStatus", eleDropStatus.getAttribute ("Status"));
						
				lstJobs = new ArrayList<Document> ();
				lstJobs.add (docOrderLineStatusList.getDocument());
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Output from getJobs:");
					System.out.println (docOrderLineStatusList.getString());
				}
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Exception in getJobs:");
				System.out.println (e.getMessage());
				e.printStackTrace (System.out);
			}
		}
		if (!bMinOrderStatusesExist)
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("No Jobs returned from getJobs");
			}
			lstJobs = null;
		}
		return lstJobs;
	}

	public void executeJob (YFSEnvironment env, Document inXML) throws Exception 
	{
		YFCDocument	docOrderLineStatusList = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent SEND_TO_OPTIMIZER (TmsInterfaceAgentsImpl)");
			System.out.println ("Job Input:");
			System.out.println (docOrderLineStatusList.getString());
		}

		// raise an event and send the job xml to the event handler
		sendJobToOptimizer (env, docOrderLineStatusList);
				
		Iterator<?>	iOrderLineStatusList = eleOrderLineStatusList.getChildren();
		String		sOrderHeaderKey = null;
		ArrayList<YFCElement>	lstOrderLines = new ArrayList<YFCElement> ();

		// iterate over all order line status and change status of each line grouped by OrderHeaderKey
		while (iOrderLineStatusList.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatusList.next();
			
			// only look for OrderStatus records in the list and ignore any other elements in the list
			if (eleOrderLineStatus.getNodeName ().equals ("OrderStatus"))
			{
				// change the status of the order lines to relfect the agent's drop status
				if (sOrderHeaderKey != null && !sOrderHeaderKey.equals(eleOrderLineStatus.getAttribute ("OrderHeaderKey")))
				{
					changeOrderLineStatus (env, sOrderHeaderKey, lstOrderLines, eleOrderLineStatusList.getAttribute ("TransactionId"), eleOrderLineStatusList.getAttribute ("DropStatus"));
					lstOrderLines.clear();
				}
				sOrderHeaderKey = eleOrderLineStatus.getAttribute ("OrderHeaderKey");
				lstOrderLines.add (eleOrderLineStatus);
			}
		}
		// flush the last batch of order lines
		if (lstOrderLines.size() > 0)
				changeOrderLineStatus (env, sOrderHeaderKey, lstOrderLines, eleOrderLineStatusList.getAttribute ("TransactionId"), eleOrderLineStatusList.getAttribute ("DropStatus"));
		
		completeJob (env, docOrderLineStatusList);
    }
		
	private boolean	canSendToOptimizer (YFSEnvironment env, YFCElement eleOrderLineStatusList, long lMinimumRecords)
	{
		boolean	bMinOrderStatusesExist = false;
		
		// if no minimum specified
		if (lMinimumRecords == 0)
			lMinimumRecords = -1;

		Iterator<?>	iOrderLineStatusList = eleOrderLineStatusList.getChildren();
		while (iOrderLineStatusList.hasNext())
		{
			YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatusList.next();

			// if at least one order line status record exits
			if (eleOrderLineStatus.getNodeName ().equals ("OrderStatus"))
			{
				if (lMinimumRecords <= 0)
				{
					bMinOrderStatusesExist = true;
					break;
				}
				else
					lMinimumRecords--;
			}
		}
		return bMinOrderStatusesExist;
	}
	
	private void	sendJobToOptimizer (YFSEnvironment env, YFCDocument docJobXml) throws Exception
	{
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
			YFCElement	eleJobXml = docJobXml.getDocumentElement();
			
			// call a synchronous service to export all the orders to the Optimizer interface
			//api.executeFlow (env, "exportOrders", docJobXml.getDocument());
						
			// raise the ON_SUCCESS event for this transaction
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Job Executing - Raising Event ON_SEND_TO_OPTIMIZER:");
			}

			YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
			YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
			eleRaiseEvent.setAttribute ("TransactionId", eleJobXml.getAttribute ("TransactionId"));
			eleRaiseEvent.setAttribute ("EventId", "ON_SEND_TO_OPTIMIZER");

			YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
			YFCElement	eleData = eleDataMap.createChild ("Data");
			eleData.setAttribute ("Name", "TransactionId");
			eleData.setAttribute ("Value", eleJobXml.getAttribute ("TransactionId"));

			YFCElement	eleDataType = eleRaiseEvent.createChild ("DataType");
			eleDataType.setNodeValue ("1");	// Data Type is 1 for TYPE_XML_STRING or 0 for TYPE_JAVA_MAP

			YFCElement	eleXmlData = eleRaiseEvent.createChild ("XMLData");
			eleXmlData.setNodeValue (docJobXml.getString());		
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to RaiseEvent:");
				System.out.println (docRaiseEvent.getString());
			}
			api.raiseEvent (env, docRaiseEvent.getDocument());	
	}

	private void	completeJob (YFSEnvironment env, YFCDocument docJobXml) throws Exception
	{
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
			YFCElement	eleJobXml = docJobXml.getDocumentElement();
			
						
			// raise the ON_SUCCESS event for this transaction
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Job Completed - Raising Event ON_SUCCESS:");
			}
			YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
			YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
			eleRaiseEvent.setAttribute ("TransactionId", eleJobXml.getAttribute ("TransactionId"));
			eleRaiseEvent.setAttribute ("EventId", "ON_SUCCESS");
			YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
			YFCElement	eleData = eleDataMap.createChild ("Data");
			eleData.setAttribute ("Name", "TransactionId");
			eleData.setAttribute ("Value", eleJobXml.getAttribute ("TransactionId"));
			YFCElement	eleDataType = eleRaiseEvent.createChild ("DataType");
			eleDataType.setNodeValue ("1");	// Data Type is 1 for TYPE_XML_STRING or 0 for TYPE_JAVA_MAP
			
			YFCElement	eleXmlData = eleRaiseEvent.createChild ("XMLData");
			eleXmlData.setNodeValue (docJobXml.getString());		
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to RaiseEvent:");
				System.out.println (docRaiseEvent.getString());
			}
			api.raiseEvent (env, docRaiseEvent.getDocument());	
	}
	
	private void 	changeOrderLineStatus (YFSEnvironment env, String sOrderHeaderKey, ArrayList<YFCElement> lstOrderLines, String sTransactionId, String sDropStatus) throws Exception
	{
		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
		
		eleOrderStatusChange.setAttribute ("OrderHeaderKey", sOrderHeaderKey);
		eleOrderStatusChange.setAttribute ("TransactionId", sTransactionId);
		YFCElement	eleOrderLines = eleOrderStatusChange.createChild ("OrderLines");

		// for each of the orderlines in this order header key
		for (int i = 0; i < lstOrderLines.size(); i++)
		{
			YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
			eleOrderLine.setAttribute ("OrderLineKey", ((YFCElement)lstOrderLines.get(i)).getAttribute ("OrderLineKey"));
			eleOrderLine.setAttribute ("OrderReleaseKey", ((YFCElement)lstOrderLines.get(i)).getAttribute ("OrderReleaseKey"));
			eleOrderLine.setAttribute ("BaseDropStatus", sDropStatus);
			eleOrderLine.setAttribute ("ChangeForAllAvailableQty", "Y");
		}
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to changeOrderStatus:");
			System.out.println (docOrderStatusChange.getString());
		}
		api.changeOrderStatus (env, docOrderStatusChange.getDocument());		
	}	
}

