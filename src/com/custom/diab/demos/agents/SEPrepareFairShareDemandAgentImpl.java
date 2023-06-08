package com.custom.diab.demos.agents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.ycp.core.YCPContext;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class SEPrepareFairShareDemandAgentImpl extends YCPBaseAgent implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(SEPrepareFairShareDemandAgentImpl.class);
	private Properties	m_Props;
	
	
	public SEPrepareFairShareDemandAgentImpl() {
		// TODO Auto-generated constructor stub
	}

	public Document	recordBackorderedItemsInIBA (YFSEnvironment env, Document docIn) throws Exception
	{
		// we're getting SCHEDULE.ON_BACKORDER.xml as input to this API
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator	iOrderLines = eleOrderLines.getChildren();
//		Connection	conDB = ((YCPContext)env).getDBConnection();

		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to recordBackorderedItemsInIBA:");
			System.out.println (docOrder.getString());
		}
		
		
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			YFCElement	eleItem = eleOrderLine.getChildElement("Item");

/*
			String sSQL = "SELECT IBA_TRIGGER_KEY, ORGANIZATION_CODE, ITEM_ID, UOM, PRODUCT_CLASS, NODE_KEY FROM YFS_IBA_TRIGGER WHERE ORGANIZATION_CODE='" + eleOrder.getAttribute("EnterpriseCode") + "' AND ITEM_ID='"+ eleItem.getAttribute("ItemID") + "' AND UOM='" + eleItem.getAttribute("UnitOfMeasure") + "' AND PRODUCT_CLASS='" + eleItem.getAttribute("ProductClass") + "'";
			System.out.println ("Executing Query: " + sSQL);
			
			PreparedStatement ps = conDB.prepareStatement(sSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet			rsResults = ps.executeQuery();
			
			// there will be 1 YFS_IBA_TRIGGER record for each backordered item so each job will handle one item
			if (!rsResults.next())
			{
*/
				YFCDocument	docManageItemBasedAllocationTrigger = YFCDocument.createDocument("ItemBasedAllocation");
				YFCElement	eleManageItemBasedAllocationTrigger = docManageItemBasedAllocationTrigger.getDocumentElement();
				eleManageItemBasedAllocationTrigger.setAttribute("OrganizationCode", eleOrder.getAttribute("EnterpriseCode"));
				eleManageItemBasedAllocationTrigger.setAttribute ("ItemID", eleItem.getAttribute("ItemID"));
				eleManageItemBasedAllocationTrigger.setAttribute ("UnitOfMeasure", eleItem.getAttribute("UnitOfMeasure"));
				eleManageItemBasedAllocationTrigger.setAttribute("ProductClass", eleItem.getAttribute("ProductClass"));
				eleManageItemBasedAllocationTrigger.setAttribute("IBARequired", "Y");
				eleManageItemBasedAllocationTrigger.setAttribute("ProcessingByAgent", "N");
				YFCNodeList<YFCElement>	nodeDetails = eleOrderLine.getElementsByTagName("Detail");
				Iterator iDetailsList = nodeDetails.iterator();
				if (iDetailsList.hasNext())
				{
					YFCElement	eleDetails = (YFCElement)iDetailsList.next();
					eleManageItemBasedAllocationTrigger.setAttribute("Node", eleDetails.getAttribute("ShipNode"));
				}
				else
				{
					eleManageItemBasedAllocationTrigger.setAttribute("Node", m_Props.getProperty("BackorderShipNode"));
				}
				YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to manageItemBaseAllocationTrigger API:");
					System.out.println (docManageItemBasedAllocationTrigger.getString());
				}
				api.manageItemBasedAllocationTrigger(env, docManageItemBasedAllocationTrigger.getDocument());
			}
/*
			else {
				System.out.println ("Item Trigger Already Added: " + eleItem.getAttribute("ItemID"));
			}
			rsResults.close();
			ps.close();
		}
*/		return docIn;

	}
	
	public List getJobs (YFSEnvironment env, Document inXML)
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Prepare for Fair Share Allocation Agent - getJobs() Input:");
			System.out.println (YFCDocument.getDocumentFor (inXML).getString());
		}
		/* get Orders that are Backordered or Partially Scheduled
		 * 
		 if (lastMessageXml != null)
			 return null;
		 */
		 
		DateFormat	dfCurrent = DateFormat.getDateTimeInstance();
		String sCurrentDateTime = dfCurrent.format(new Date());
		ArrayList<Document> lstJobs = new ArrayList <Document>();
		YFCDocument	docIn = YFCDocument.getDocumentFor(inXML);
		YFCElement	eleIn = docIn.getDocumentElement();
		String		sTransactionId = eleIn.getAttribute("TransactionId");
		
		// this API object can be used to call an IBM OMS API like getOrderList() API for example.
		try {
			Connection	conDB = ((YCPContext)env).getDBConnection();
			if (YFSUtil.getDebug())
			{
				System.out.println (sCurrentDateTime + ": OMS DB Connection Successful on Schema " + conDB.getSchema());
			}
		    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
			
			String sOrganizationCode = eleIn.getAttribute("OrganizationCode");
			String sOrganizationWhereClause = "";
			if (!YFCObject.isVoid(sOrganizationCode))
				sOrganizationWhereClause = " AND ORGANIZATION_CODE = '" + sOrganizationCode + "'";

			String sSQL = "SELECT IBA_TRIGGER_KEY, ORGANIZATION_CODE, ITEM_ID, UOM, PRODUCT_CLASS, NODE_KEY, IBA_REQUIRED, IBA_RUN_REQUIRED, PROCESSING_BY_AGENT FROM YFS_IBA_TRIGGER WHERE IBA_REQUIRED='Y' AND PROCESSING_BY_AGENT = 'N' "+ sOrganizationWhereClause + " FOR UPDATE";
			PreparedStatement ps = conDB.prepareStatement(sSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			ResultSet			rsResults = ps.executeQuery();
			// there will be 1 YFS_IBA_TRIGGER record for each backordered item so each job will handle one item
			if (rsResults.next())
			{
				if (YFSUtil.getDebug())
				{
					System.out.println (sCurrentDateTime + ": Successfully Executed Query:" + sSQL);
				}

				
				do {
					sOrganizationCode = rsResults.getString("ORGANIZATION_CODE").trim();
					if (YFSUtil.getDebug())
					{
						System.out.println (sCurrentDateTime + ": Retrieved Organization Code from Results:" + sOrganizationCode);
					}

					//  get list of orders lines for this item that are not released
					String		sIBATriggerKey = rsResults.getString("IBA_TRIGGER_KEY").trim();
					String		sItemID = rsResults.getString("ITEM_ID").trim();
					String		sUOM = rsResults.getString("UOM").trim();
					String		sProductClass = rsResults.getString("PRODUCT_CLASS").trim();
					
					rsResults.updateString("PROCESSING_BY_AGENT", "Y");
					rsResults.updateRow();
					if (YFSUtil.getDebug())
					{
						System.out.println ("Successfully Updated PROCESSING_BY_AGENT");
					}
					
				    try {
						YFCDocument docOrderLineListTemplate = YFCDocument.getDocumentFor("<OrderLineList><OrderLine OrderNo=\"\" OrderHeaderKey=\"\" OrderLineKey=\"\" OrderedQty=\"\" MinLineStatus=\"\" ShipToID=\"\"/></OrderLineList>");
						YFCDocument docOrderLineList = YFCDocument.getDocumentFor("<OrderLine OrganizationCode=\""+ sOrganizationCode + "\"/>");
						
						YFCElement	eleOrderLineList = docOrderLineList.getDocumentElement();						
						YFCElement	eleItem = eleOrderLineList.createChild("Item");
						YFCElement	eleOrder = eleOrderLineList.createChild("Order");
						eleOrder.setAttribute("DocumentType", "0001");
						
						eleItem.setAttribute("ItemID", sItemID);
						eleItem.setAttribute("UnitOfMeasure", sUOM);
						eleItem.setAttribute("ProductClass", sProductClass);
						System.out.println (docOrderLineList.getString());
					
					    env.setApiTemplate("getOrderLineList", docOrderLineListTemplate.getDocument());
					    if (YFSUtil.getDebug())
					    {
					    	System.out.println ("Input to getOrderLineList:");
					    	System.out.println (docOrderLineList.getString());
					    }
				    	docOrderLineList = YFCDocument.getDocumentFor(api.getOrderLineList(env,  docOrderLineList.getDocument()));
				    	env.clearApiTemplate("getOrderLineList");
					    if (YFSUtil.getDebug())
					    {
					    	System.out.println ("Output from getOrderLineList:");
					    	System.out.println (docOrderLineList.getString());
					    }

				    	eleOrderLineList = docOrderLineList.getDocumentElement();
				    	eleOrderLineList.setAttribute("TransactionId", sTransactionId);
				    	eleOrderLineList.setAttribute("OrganizationCode", sOrganizationCode);
			    		eleOrderLineList.setAttribute("IBATriggerKey", sIBATriggerKey);
			    		eleOrderLineList.setAttribute("ItemID", sItemID);
			    		eleOrderLineList.setAttribute("UnitOfMeasure", sUOM);
			    		eleOrderLineList.setAttribute("ProductClass", sProductClass);
			    		
			    		if (YFSUtil.getDebug())
					    {
					    	System.out.println ("Adding the following XML to Jobs List:");
					    	System.out.println (docOrderLineList.getString());
					    }
			    		lstJobs.add (docOrderLineList.getDocument());				    	
				    } catch (Exception e) {
				    	env.clearApiTemplate("getOrderLineList");
					    if (YFSUtil.getDebug())
					    {
					    	System.out.println ("Exception in getJobs: " + e.getMessage());
					    }
				    }
				} while (rsResults.next());
				ps.close();
				rsResults.close();
				conDB.commit();
			}
		} 
		catch (SQLException e)
		{
		    if (YFSUtil.getDebug())
		    {
		    	System.out.println ("SQL Exception in getJobs(): " + e.getMessage());
		    }
		}
		catch (Exception e)
		{
		    if (YFSUtil.getDebug())
		    {
		    	System.out.println ("Exception in getJobs(): " + e.getMessage());
		    }
		}
    	System.out.println ("Exiting Prepare for Fair Share Allocation Agent - getJobs(): " );
		
		return(lstJobs);
	}
	
	public void executeJob(YFSEnvironment env, Document inXML) {
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Prepare for Fair Share Allocation Agent - executeJob Input:");
			System.out.println (YFCDocument.getDocumentFor (inXML).getString());
		}
		
		try {
				YFCDocument				docOrderLineList = YFCDocument.getDocumentFor(inXML);
				YFCElement				eleOrderLineList = docOrderLineList.getDocumentElement();
				Iterator<YFCElement>	iOrderLineList = eleOrderLineList.getChildren();
			    
				String		sOrganizationCode = eleOrderLineList.getAttribute("OrganizationCode");
				String		sIBATriggerKey = eleOrderLineList.getAttribute("IBATriggerKey");
				
				if (YFCObject.isVoid(sOrganizationCode))
					sOrganizationCode = "DEFAULT";
			    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();

				// collect total supply and demand details for the given OLK
				collectDemandsAndSupplies(env, eleOrderLineList);
				
			    // Update common code demands/supplies and status of order lines being updated for fair share agent
				while (iOrderLineList.hasNext())
				{
					YFCElement	eleOrderLineNext = (YFCElement)iOrderLineList.next();

					// ignore lines beyond scheduled status or before back ordered status
					if (eleOrderLineNext.getAttribute("MinLineStatus").compareTo("1500") > 0 ||
						eleOrderLineNext.getAttribute("MinLineStatus").compareTo("1300") < 0)
						continue;
					

					String		sOLK	  = eleOrderLineNext.getAttribute ("OrderLineKey");					
					persistDemandInCommonCodeTable (env, sOrganizationCode, eleOrderLineNext);
					if (YFSUtil.getDebug())
					{
						String		sTotalDemand = eleOrderLineList.getAttribute("TotalDemand");
						String		sTotalSupply = eleOrderLineList.getAttribute("TotalSupply");
						String		sTotalShortage = eleOrderLineList.getAttribute("TotalShortage");
						System.out.println ("Successfully Persisted New Demand for OLK=" + sOLK + " TOTALSUPPLY#TOTALDEMAND#TOTALSHORTAGE=" + sTotalSupply + "#" + sTotalDemand + "#" + sTotalShortage);
					}

					
					// update status of lines to "Ready for Fair Share Allocation"
					YFCDocument docChangeOrderStatus = YFCDocument.createDocument("OrderStatusChange");
					YFCElement	eleChangeOrderStatus = docChangeOrderStatus.getDocumentElement();
					eleChangeOrderStatus.setAttribute("OrderHeaderKey", eleOrderLineNext.getAttribute("OrderHeaderKey"));
					eleChangeOrderStatus.setAttribute("TransactionId", eleOrderLineList.getAttribute("TransactionId"));

					//eleChangeOrderStatus.setAttribute("BaseDropStatus", "1300.100");
					//eleChangeOrderStatus.setAttribute("ChangeForAllAvailableQty", "N");
					YFCElement eleOrderLines = eleChangeOrderStatus.createChild("OrderLines");
					YFCElement eleOrderLine = eleOrderLines.createChild("OrderLine");
					YFCElement	eleOrderLineTranQuantity = eleOrderLine.createChild("OrderLineTranQuantity");
					eleOrderLine.setAttribute("OrderLineKey", eleOrderLineNext.getAttribute("OrderLineKey"));
					eleOrderLine.setAttribute ("ChangeForAllAvailableQty", "Y");
					eleOrderLine.setAttribute("BaseDropStatus", "1300.100");
					eleOrderLineTranQuantity.setAttribute("Quantity", eleOrderLineNext.getAttribute("OrderedQty"));
					eleOrderLineTranQuantity.setAttribute("TransactionalUOM", eleOrderLineList.getAttribute("UnitOfMeasure"));
					
					/*
					double	dblBackorderedQty = eleOrderLineNext.getDoubleAttribute ("TotalBackorderedQty");
					double	dblScheduledQty = eleOrderLineNext.getDoubleAttribute ("TotalScheduledQty");
					if (dblScheduledQty > 0)
					{
						YFCElement eleOrderLine = eleOrderLines.createChild("OrderLine");
						YFCElement	eleOrderLineTranQuantity = eleOrderLine.createChild("OrderLineTranQuantity");
						eleOrderLine.setAttribute("OrderLineKey", eleOrderLineNext.getAttribute("OrderLineKey"));
						eleOrderLineTranQuantity.setAttribute("Quantity", dblScheduledQty);
						eleOrderLineTranQuantity.setAttribute("TransactionalUOM", eleOrderLineList.getAttribute("UnitOfMeasure"));
					}
					if (dblBackorderedQty > 0)
					{
						YFCElement eleOrderLine = eleOrderLines.createChild("OrderLine");
						YFCElement	eleOrderLineTranQuantity = eleOrderLine.createChild("OrderLineTranQuantity");
						eleOrderLine.setAttribute("OrderLineKey", eleOrderLineNext.getAttribute("OrderLineKey"));
						eleOrderLineTranQuantity.setAttribute("Quantity", dblBackorderedQty);
						eleOrderLineTranQuantity.setAttribute("TransactionalUOM", eleOrderLineList.getAttribute("UnitOfMeasure"));
					}
					*/
					if (YFSUtil.getDebug())
					{
						System.out.println ("Input to changeOrderStatus API:");
						System.out.println (docChangeOrderStatus.getString());
					}
					try {
					api.changeOrderStatus (env, docChangeOrderStatus.getDocument());
					} catch (Exception ignore) {
						
					}
				}
				// safe to delete the IBA Trigger
				deleteIBATrigger (env, sIBATriggerKey);
				

				// raise ON_SUCCESS event
				completeJob(env, YFCDocument.getDocumentFor(inXML));
		} 
		catch (Exception e) 
		{
			System.out.println ("Exception Occured in executeJob of Prepare for Fair Share Allocation Agent: " + e.getClass() + e.getMessage());
		}
	}
	
	private void collectDemandsAndSupplies (YFSEnvironment env, YFCElement eleOrderLineList) throws Exception
	{
		double					dblTotalOrderedQty = 0;
		double					dblTotalScheduledQty = 0;
		double					dblTotalShortQty = 0;
    	Iterator<YFCElement>	iOrderLineList = eleOrderLineList.getChildren();
	    YIFApi 					api = YIFClientFactory.getInstance().getLocalApi ();

    	System.out.println ("Collecting Status Quantities...");
    	while (iOrderLineList.hasNext())
    	{
    		YFCElement	eleLineNext = iOrderLineList.next();
    		YFCDocument	docOrderLineStatusTemplate = YFCDocument.getDocumentFor("<OrderLineStatusList><OrderStatus OrderHeaderKey=\"\" OrderLineKey=\"\" StatusDescription=\"\" Status=\"\" StatusQty=\"\" ShipNode=\"\" TotalQuantity=\"\"/></OrderLineStatusList>");
    		YFCDocument docOrderLineStatusList = YFCDocument.getDocumentFor("<OrderLineStatus OrderHeaderKey=\"" + eleLineNext.getAttribute("OrderHeaderKey") + "\" OrderLineKey=\"" + eleLineNext.getAttribute("OrderLineKey") + "\"/>");
    		env.setApiTemplate("getOrderLineStatusList", docOrderLineStatusTemplate.getDocument());
    		if (YFSUtil.getDebug())
    		{
    			System.out.println ("Input to getOrderLineStatusList API:");
    			System.out.println (docOrderLineStatusList.getString());
    		}
    		docOrderLineStatusList = YFCDocument.getDocumentFor(api.getOrderLineStatusList(env, docOrderLineStatusList.getDocument()));
    		if (YFSUtil.getDebug())
    		{
    			System.out.println ("Output from getOrderLineStatusList API:");
    			System.out.println (docOrderLineStatusList.getString());
    		}
    		env.clearApiTemplate("getOrderLineStatusList");
    		
    		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();
    		dblTotalOrderedQty = dblTotalOrderedQty + eleLineNext.getDoubleAttribute("OrderedQty");
    		dblTotalScheduledQty = dblTotalScheduledQty + getScheduledQty (eleOrderLineStatusList);
    		dblTotalShortQty = dblTotalShortQty + getBackorderedQty (eleOrderLineStatusList);
    	}
		eleOrderLineList.setAttribute ("TotalDemand", dblTotalOrderedQty);
		eleOrderLineList.setAttribute ("TotalSupply", dblTotalScheduledQty);
		eleOrderLineList.setAttribute ("TotalShortage", dblTotalShortQty);
		eleOrderLineList.setDoubleAttribute("TotalScheduledQty", dblTotalScheduledQty);
		eleOrderLineList.setDoubleAttribute("TotalBackorderedQty", dblTotalShortQty);
	}	

	private void deleteIBATrigger(YFSEnvironment env, String sIBATriggerKey) throws Exception
	{
		Connection	conDB = ((YCPContext)env).getDBConnection();

		try {
			String sSQL = "DELETE FROM YFS_IBA_TRIGGER WHERE IBA_TRIGGER_KEY = '" + sIBATriggerKey+ "'";
			PreparedStatement ps = conDB.prepareStatement(sSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			ps.execute();
			ps.close();
		} catch (SQLException se) {
			System.out.println ("SQLException Occured: " + se.getClass() + " " + se.getMessage());
		}
	}

	private boolean IsAllQtyBackordered (YFCElement eleOrderLine)
	{
		return getBackorderedQty(eleOrderLine) == eleOrderLine.getDoubleAttribute("OrderedQty");
	}
	private double	getBackorderedQty (YFCElement eleOrderLine)
	{
		return getStatusQty(eleOrderLine, "1300");
	}
	
	private double	getScheduledQty (YFCElement eleOrderLine)
	{
		return getStatusQty(eleOrderLine, "1500");
	}
	
	private String	getShceduledNodesAndQtysAsString (YFCElement eleOrderLine)
	{
		List<String>	lstNodesAndQtys = getScheduledNodesAndQtys (eleOrderLine);
		
		int				iNode;
		StringBuilder	sNodesAndQuantities = new StringBuilder();
		
		for (iNode = 0; iNode < lstNodesAndQtys.size(); iNode++)
		{
			sNodesAndQuantities.append(lstNodesAndQtys.get(iNode));
			if (iNode < lstNodesAndQtys.size()-1)
				sNodesAndQuantities.append ('#');
		}
		return sNodesAndQuantities.toString();
	}
	
	private List<String>	getScheduledNodesAndQtys (YFCElement eleOrderLineStatusList)
	{
		Iterator<YFCElement>	iStatusList = eleOrderLineStatusList.getChildren();
		List<String>			lstNodesAndQtys = new ArrayList<String>();
		
		while (iStatusList.hasNext())
		{
			YFCElement eleStatus = iStatusList.next();
			if (eleStatus.getAttribute("Status").startsWith("1500"))
			{
				String sNodeAndQty = eleStatus.getAttribute ("ShipNode") + "#" + eleStatus.getAttribute("StatusQty");
				System.out.println ("Adding The Following List Entry to Node and Qty: " + sNodeAndQty);
				lstNodesAndQtys.add (sNodeAndQty);
			}
		}
		return lstNodesAndQtys;
	}

	private double	getStatusQty (YFCElement eleOrderLineStatusList, String sBaseStatus)
	{
		Iterator<YFCElement>	iStatusList = eleOrderLineStatusList.getChildren();
		double					dblStatusQty = 0;
		while (iStatusList.hasNext())
		{
			YFCElement eleStatus = iStatusList.next();
			if (eleStatus.getAttribute("Status").startsWith(sBaseStatus))
				dblStatusQty += eleStatus.getDoubleAttribute ("StatusQty");
		}
		return dblStatusQty;
	}
	
	private void persistDemandInCommonCodeTable (YFSEnvironment env, String sOrganizationCode, YFCElement eleOrderLine) throws Exception
	{
	    System.out.println ("Entering persistDemandsInCommonCodeTable");

	    YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
		YFCElement	eleCommonCode = docCommonCode.getDocumentElement();
		eleCommonCode.setAttribute("OrganizationCode", sOrganizationCode);
		eleCommonCode.setAttribute ("Action", "Manage");

	    YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
	    String		sOLK		= eleOrderLine.getAttribute("OrderLineKey");
	    String		sTotalDemand = eleOrderLine.getParentElement().getAttribute("TotalDemand");
	    String		sTotalSupply = eleOrderLine.getParentElement().getAttribute("TotalSupply");
	    String		sTotalShortage = eleOrderLine.getParentElement().getAttribute("TotalShortage");
	    
		eleCommonCode.setAttribute ("CodeType", "CPG_FAIRSHARE_DEMAND");
		eleCommonCode.setAttribute ("CodeValue", sOLK);
		eleCommonCode.setAttribute ("CodeShortDescription", sTotalSupply + "#" + sTotalDemand + "#" + sTotalShortage);
		eleCommonCode.setAttribute ("CodeLongDescription", getShceduledNodesAndQtysAsString (eleOrderLine));
		if (YFSUtil.getDebug())
		{
		   System.out.println ("Input to manageCommonCode API:");
		    System.out.println (docCommonCode.getString());
		}
		api.manageCommonCode(env, docCommonCode.getDocument());
	    System.out.println ("Exiting persistDemandsInCommonCodeTable");
	    return;
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
		String		sTransactionId = eleJobXml.getAttribute ("TransactionId");
		
		eleRaiseEvent.setAttribute ("TransactionId", sTransactionId);
		eleRaiseEvent.setAttribute ("EventId", "ON_SUCCESS");
		YFCElement	eleDataMap = eleRaiseEvent.createChild ("DataMap");
		YFCElement	eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TransactionId");
		eleData.setAttribute ("Value", sTransactionId);		
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "OrganizationCode");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("OrganizationCode"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "ItemID");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("ItemID"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "UnitOfMeasure");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("UnitOfMeasure"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "ProductClass");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("ProductClass"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "IBATriggerKey");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("IBATriggerKey"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TotalDemand");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("TotalDemand"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TotalShortage");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("TotalShortage"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "TotalSupply");
		eleData.setAttribute ("Value", eleJobXml.getAttribute("TotalSupply"));
		eleData = eleDataMap.createChild ("Data");
		eleData.setAttribute ("Name", "CommonCodesForDemand");
		eleData.setAttribute ("Value", "CPG_FAIRSHARE_DEMAND");

		
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

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}


}
