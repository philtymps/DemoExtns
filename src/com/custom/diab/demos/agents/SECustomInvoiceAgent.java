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

public class SECustomInvoiceAgent extends YCPBaseTaskAgent {

	@Override
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception
	{
		YFCDocument	docTaskQueue = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleTaskQueue = docTaskQueue.getDocumentElement();
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("In Agent CPG_ORDER_INVOICE (SECustomInvoiceAgent.java)");
			System.out.println ("Task Input:");
			System.out.println (docTaskQueue.getString());
		}
		
		// determine what type of invoice to create via criteria Parameter InvoiceType (e.g.  PRO_FORMA , or INFO)
		YFCDocument	docOrderInvoice = YFCDocument.getDocumentFor ("<OrderInvoice OrderHeaderKey=\"" + eleTaskQueue.getAttribute ("DataKey") + "InvoiceType=" + eleTaskQueue.getAttribute ("InvoiceType") + "\"/>");
		YFCDocument	docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"" + eleTaskQueue.getAttribute ("DataKey")+ "\"/>");

		
		YFCDocument docOutputTemplate = YFCDocument.getDocumentFor ("<Order><PriceInfo/><HeaderTaxes/><HeaderCharges><HeaderCharge/></HeaderCharges><OrderLines><OrderLine><Item/><LineCharges/><LineTaxes><LineTax/></LineTaxes></OrderLine></OrderLines></Order>");
	    env.setApiTemplate("getOrderDetails", docOutputTemplate.getDocument());
		docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
		env.clearApiTemplate("getOrderDetails");

		// generate an INFO or PRO_FORMA 
		api.recordInvoiceCreation(env, createInvoice (docOrder, docOrderInvoice));
		
		changeOrderLineStatus (env, eleTaskQueue);
		
		registerTaskComplete (env, eleTaskQueue);
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private Document	createInvoice (YFCDocument docOrder, YFCDocument docOrderInvoice)
	{
		YFCElement	eleOrder = docOrder.getDocumentElement();
		YFCElement	eleOrderInvoice = docOrderInvoice.getDocumentElement();
		YFCElement	elePriceInfo = eleOrder.getChildElement ("PriceInfo");
		
		eleOrderInvoice.setAttribute("SellerOrganizationCode", eleOrder.getAttribute ("SellerOrganizationCode"));
		eleOrderInvoice.setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey"));
		eleOrderInvoice.setAttribute("DocumentType", eleOrder.getAttribute ("DocumentType"));
		eleOrderInvoice.setAttribute("EnterpriseCode", eleOrder.getAttribute ("EnterpriseCode"));
		eleOrderInvoice.setAttribute("Currency", elePriceInfo.getAttribute ("Currency"));
		
		YFCElement	eleOrderHeaderTaxes = eleOrder.getChildElement ("HeaderTaxes");
		Iterator	iHeaderTaxes   = eleOrderHeaderTaxes.getChildren();
		YFCElement	eleTaxBreakupList = eleOrderInvoice.createChild ("TaxBreakupList");

		while (iHeaderTaxes.hasNext())
		{
			YFCElement	eleHeaderTax = (YFCElement)iHeaderTaxes.next();
			YFCElement	eleTaxBreakup = eleTaxBreakupList.createChild("TaxBreakup");
			eleTaxBreakup.setAttribute("TaxName", eleHeaderTax.getAttribute ("TaxName"));
			eleTaxBreakup.setAttribute("TaxPercentage", eleHeaderTax.getAttribute ("TaxPercentage"));
			eleTaxBreakup.setAttribute("Tax", eleHeaderTax.getAttribute ("TaxName"));
			eleTaxBreakup.setAttribute("ChargeCategory", eleHeaderTax.getAttribute ("ChargeName"));
			eleTaxBreakup.setAttribute("ChargeName", eleHeaderTax.getAttribute ("ChargeName"));			
		}

		YFCElement	eleOrderHeaderCharges = eleOrder.getChildElement ("HeaderCharges");
		Iterator	iHeaderCharges   = eleOrderHeaderCharges.getChildren();
		YFCElement	eleHeaderChargeList = eleOrderInvoice.createChild ("HeaderChargeList");

		while (iHeaderCharges.hasNext())
		{
			YFCElement	eleOrderHeaderCharge = (YFCElement)iHeaderTaxes.next();
			YFCElement	eleHeaderCharge = eleHeaderChargeList.createChild("HeaderCharge");

			eleHeaderCharge.setAttribute("ChargeCategory", eleOrderHeaderCharge.getAttribute ("ChargeName"));
			eleHeaderCharge.setAttribute("ChargeName", eleOrderHeaderCharge.getAttribute ("ChargeName"));			
			eleHeaderCharge.setAttribute("ChargePerLine", eleOrderHeaderCharge.getAttribute ("ChargePerLine"));
			eleHeaderCharge.setAttribute("ChargePerUnit", eleOrderHeaderCharge.getAttribute ("ChargePerUnit"));
			eleHeaderCharge.setAttribute("ChargeAmount", eleOrderHeaderCharge.getAttribute ("ChargeAmount"));
		}
		
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator	iOrderLines   = eleOrderLines.getChildren();
		YFCElement	eleLineDetails = eleOrderInvoice.createChild("LineDetails");
		
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
			YFCElement	eleItem = eleOrderLine.getChildElement ("Item");
			YFCElement	eleLineDetail = eleLineDetails.createChild("LineDetail");
			
			// from <OrderLine> element
			eleLineDetail.setAttribute("OrderLineKey", eleOrderLine.getAttribute ("OrderLineKey"));
			eleLineDetail.setAttribute("PrimeLineNo", eleOrderLine.getAttribute ("PrimeLineNo"));
			eleLineDetail.setAttribute("SubLineNo", eleOrderLine.getAttribute ("SubLineNo"));
			eleLineDetail.setAttribute("Quantity", eleOrderLine.getAttribute ("OrderQty"));
			// from <Item> element
			eleLineDetail.setAttribute("ItemID", eleItem.getAttribute ("ItemID"));
			eleLineDetail.setAttribute("ProductClass", eleItem.getAttribute ("ProductClass"));
			eleLineDetail.setAttribute("UnitOfMeasure", eleItem.getAttribute ("UnitOfMeasure"));
			YFCElement	eleOrderLineCharges = eleOrderLine.getChildElement ("LineCharges");
			if (!YFCObject.isNull(eleOrderLineCharges))
			{
				YFCElement	eleLineChargeList = eleLineDetail.createChild("LineChargeList");
				Iterator	iOrderLineCharges = eleOrderLineCharges.getChildren();
				while (iOrderLineCharges.hasNext())
				{
					YFCElement	eleOrderLineCharge = (YFCElement)iOrderLineCharges.next();
					YFCElement	eleLineCharge = eleLineChargeList.createChild("LineCharge");
					eleLineCharge.setAttribute("ChargeCategory", eleOrderLineCharge.getAttribute ("ChargeCategory"));
					eleLineCharge.setAttribute("ChargeName", eleOrderLineCharge.getAttribute("ChargeName"));
					eleLineCharge.setAttribute("ChargePerLine", eleOrderLineCharge.getAttribute("ChargePerLine"));
					eleLineCharge.setAttribute("ChargePerUnit", eleOrderLineCharge.getAttribute("ChargePerUnit"));
					eleLineCharge.setAttribute("ChargeAmount", eleOrderLineCharge.getAttribute("ChargeAmount"));
				}
			}
			YFCElement	eleOrderLineTaxes = eleOrderLine.getChildElement ("LineTaxes");
			if (!YFCObject.isNull(eleOrderLineTaxes))
			{
				YFCElement	eleLineTaxList = eleLineDetail.createChild("LineTaxList");
				Iterator	iOrderLineTaxes = eleOrderLineTaxes.getChildren();
				while (iOrderLineTaxes.hasNext())
				{
					YFCElement	eleOrderLineTax = (YFCElement)iOrderLineTaxes.next();
					YFCElement	eleLineTax = eleLineTaxList.createChild("LineTax");
					eleLineTax.setAttribute("ChargeCategory", eleOrderLineTax.getAttribute ("ChargeCategory"));
					eleLineTax.setAttribute("ChargeName", eleOrderLineTax.getAttribute("ChargeName"));
					eleLineTax.setAttribute("TaxName", eleOrderLineTax.getAttribute("TaxName"));
					eleLineTax.setAttribute("Tax", eleOrderLineTax.getAttribute("Tax"));
					eleLineTax.setAttribute("TaxPercentage", eleOrderLineTax.getAttribute("TaxPercentage"));
				}
			}
		}
		return docOrderInvoice.getDocument();
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
