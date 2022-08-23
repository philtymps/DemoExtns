package com.custom.diab.demos.api;

import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;

public class CPGUpdateExpectedShipDates implements YIFCustomApi {

	private	Properties	m_Props;
	
	public CPGUpdateExpectedShipDates() {
		// TODO Auto-generated constructor stub
	}
	
	public Document	UpdateExpectedShipDates (YFSEnvironment env, Document docIn) throws YFSException
	{
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
		String		sOrderType = eleOrder.getAttribute("OrderType");

		
		// only update expected ship date for CHANNEL orders (CPG)
		if (YFCObject.isVoid(sOrderType) || !sOrderType.equals("CHANNEL"))
			return docIn;
		
		YFCElement	eleOrderLines = eleOrder.getChildElement("OrderLines");
		Iterator<YFCElement>	iOrderLines = eleOrderLines.getChildren();
		YFCDocument	docChangeOrder = YFCDocument.createDocument("Order");
		YFCElement eleChangeOrder = docChangeOrder.getDocumentElement();
		eleChangeOrder.setAttribute ("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
		eleChangeOrder.setAttribute ("OrderNo", eleOrder.getAttribute("OrderNo"));
		YFCElement	eleChangeOrderLines = eleChangeOrder.createChild("OrderLines");
		
		// iterate over all the order lines and update the custom date attribute named SHIPMENT with new Expected Ship Date
		while (iOrderLines.hasNext())
		{
			YFCElement	eleOrderLine = iOrderLines.next();
			YFCElement	eleChangeOrderLine = eleChangeOrderLines.createChild("OrderLine");
			eleChangeOrderLine.setAttribute("OrderLineKey", eleOrderLine.getAttribute("OrderLineKey"));
			YFCElement	eleOrderLineSchedules = eleOrderLine.getChildElement("Schedules");
			if (!YFCObject.isVoid(eleOrderLineSchedules))
			{
				YFCElement	eleChangeOrderLineDates = eleChangeOrderLine.createChild("OrderDates");
				YFCElement	eleOrderLineSchedule = eleOrderLineSchedules.getFirstChildElement();
				if (!YFCObject.isVoid(eleOrderLineSchedule))
				{
					YFCElement	eleChangeOrderLineDate = eleChangeOrderLineDates.createChild("OrderDate");
					eleChangeOrderLineDate.setAttribute("DateTypeId", "SHIPMENT");
					eleChangeOrderLineDate.setAttribute("ExpectedDate", eleOrderLineSchedule.getAttribute("ExpectedShipmentDate"));
				}
			}
		}
		
		// All the expected ship dates should now be present
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to changeOrder API");
				System.out.println (docChangeOrder.getString());
			}
			api.changeOrder (env, docChangeOrder.getDocument());
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return docIn;	
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

}
