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
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.custom.yantra.util.*;
import java.util.*;

public class TmsInterfaceTaskAgentsImpl extends YCPBaseTaskAgent
{
    public TmsInterfaceTaskAgentsImpl()
    {
		if (YFSUtil.getDebug())
		{
			System.out.println ("In TmsInterfaceAgentsImpl Constructor");
		}
    }

	protected	static	YFCDocument	docAllTasks;
	@SuppressWarnings("rawtypes")
	protected	static	java.util.List	lstTasks;

	@SuppressWarnings("rawtypes")
	public java.util.List getJobs (YFSEnvironment env, Document inXml, Document lastMessageXml)
	{
		lstTasks = null;
		try {
			docAllTasks = YFCDocument.createDocument ("TasksToExecute");
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to getJobs:");
				System.out.println (YFCDocument.getDocumentFor (inXml).getString());
			}
			lstTasks = super.getJobs (env, inXml, lastMessageXml);
			if (lstTasks != null)
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Output from getJobs:");
					Iterator<?> iTasks = lstTasks.iterator ();
					for (int iTask = 1; iTasks.hasNext(); iTask++)
					{
						YFCDocument	docTask = YFCDocument.getDocumentFor ((Document)iTasks.next());
						YFCElement	eleTask = docTask.getDocumentElement();
	
						//  add task number and total task count so we can compute pending job count
						eleTask.setIntAttribute ("TaskNo", iTask);
						eleTask.setIntAttribute ("TotalTasks", lstTasks.size());
						System.out.println ("Task #" + iTask + " Input:");
						System.out.println (docTask.getString());
					}
				}
			}
			else
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("No Jobs returned from getJobs");
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
		return lstTasks;
	}
	
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception 
	{
		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();
		YFCElement	eleTask = docAllTasks.getDocumentElement().createChild ("Task");
		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent SEND_TO_TMS (TmsInterfaceAgentsImpl)");
			System.out.println ("Task Input:");
			System.out.println (docTaskQueue.getString());
		}
		
		//  add task to an output XML document
		eleTask.setAttribute ("TaskQKey", eleTaskQueue.getAttribute ("TaskQKey"));
		eleTask.setAttribute ("TransactionKey", eleTaskQueue.getAttribute ("TransactionKey"));
		eleTask.setAttribute ("DataKey", eleTaskQueue.getAttribute ("DataKey"));
		eleTask.setAttribute ("DataType", eleTaskQueue.getAttribute ("DataType"));

		// change the status of the order to relfect the agent's drop status		
		changeOrderStatus (env, eleTaskQueue);

		// register the task record as completed
		registerTaskComplete (env, eleTaskQueue);

		// if no pending jobs remain		
		if (getPendingJobCount (env, eleTaskQueue) == 0)
			// complete this job or batch
			completeJobs (env, eleTaskQueue);

		return null;
    }
		
	private void	completeJobs (YFSEnvironment env, YFCElement eleLastTask) throws Exception
	{
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();

			// call a synchronous service to export all the orders to the TMS interface
			//api.executeFlow (env, "exportOrders", docAllTasks.getDocument());
						
			// raise the ON_SUCCESS event for this transaction
			if (YFSUtil.getDebug ())
			{
				System.out.println ("All Tasks Executed - Raising Event ON_SUCCESS:");
				System.out.println (docAllTasks.getString());		
			}
			YFCDocument	docRaiseEvent = YFCDocument.createDocument ("RaiseEvent");
			YFCElement	eleRaiseEvent = docRaiseEvent.getDocumentElement ();
			eleRaiseEvent.setAttribute ("TransactionId", eleLastTask.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));
			eleRaiseEvent.setAttribute ("EventId", "ON_SUCCESS");
			YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
			YFCElement	eleData = eleDataMap.createChild ("Data");
			eleData.setAttribute ("Name", "TransactionId");
			eleData.setAttribute ("Value", eleLastTask.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));

			YFCElement	eleXmlData = eleDataMap.createChild ("XMLData");
			eleXmlData.setNodeValue (docAllTasks.getString());		
			api.raiseEvent (env, docRaiseEvent.getDocument());	
	}
	
	private void 	changeOrderStatus (YFSEnvironment env, YFCElement eleTaskQueue) throws Exception
	{
		YFCDocument	docOrderStatusChange = YFCDocument.createDocument ("OrderStatusChange");
		YFCElement	eleOrderStatusChange = docOrderStatusChange.getDocumentElement ();
		
		eleOrderStatusChange.setAttribute ("OrderHeaderKey", eleTaskQueue.getAttribute ("DataKey"));
		eleOrderStatusChange.setAttribute ("TransactionId", eleTaskQueue.getChildElement ("TransactionFilters").getAttribute ("TransactionId"));
		eleOrderStatusChange.setAttribute ("BaseDropStatus", "3200.200");
		eleOrderStatusChange.setAttribute ("ChangeForAllAvailableQty", "Y");

		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
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
	
	public long getPendingJobCount (YFSEnvironment env, YFCElement eleCurrentTask)
	{
		long	lPendingJobCount = 0;
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("In getPendingJobCount:");
		}
		if (lstTasks != null && lstTasks.size() > 0)
		{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Total Jobs to Execute = " + lstTasks.size());
			}
			Iterator<?>	iTasks = lstTasks.iterator ();
			for (int iTask = 0; iTasks.hasNext(); iTask++)
			{
				YFCDocument	docTask = YFCDocument.getDocumentFor ((Document)iTasks.next());
				YFCElement	eleTask = docTask.getDocumentElement ();
				if (eleCurrentTask.getAttribute ("TaskQKey").equalsIgnoreCase (eleTask.getAttribute ("TaskQKey")))
					lPendingJobCount = lstTasks.size() - iTask - 1;
			}
		}
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Pending Job Count = " + lPendingJobCount);
		}
		return lPendingJobCount;
	}
}

