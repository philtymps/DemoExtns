package com.custom.diab.demos.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class SEProcessBackOrdersInStore implements YIFCustomApi {

	
	public Document SEProcessBackOrderInStore (YFSEnvironment env, Document docIn)
	{
		// the release is back ordered from the node event is ON_BACKORDER of RELEASE.0001
		YFCDocument	docOrder = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleOrder = docOrder.getDocumentElement();
	
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to SEProcessBackOrdersInStore:");
			System.out.println (docOrder.getString());
		}
		YFCElement	eleBackorderedFrom = getBackorderedFrom (eleOrder);
		YFCDocument	docRelease = YFCDocument.createDocument ("Release");
		YFCElement	eleRelease = docRelease.getDocumentElement();
				
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			eleRelease.setAttribute("OrderReleaseKey", eleBackorderedFrom.getAttribute (eleBackorderedFrom.getAttribute("OrderReleaseKey")));
			eleRelease.setAttribute("Action", "BACKORDER");
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to changeRelease:");
				System.out.println (docRelease.getString());
			}
			api.changeRelease(env, docRelease.getDocument());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException (e.getMessage());
		}
		return docIn;
	}
	
	@SuppressWarnings("rawtypes")
	private	YFCElement	getBackorderedFrom (YFCElement eleOrder)
	{
		YFCElement	eleBackorderedFrom = null;
		
		YFCNodeList nlBackOrderedLines = eleOrder.getElementsByTagName("BackorderedFrom");
		if (!YFCObject.isNull(nlBackOrderedLines))
		{
			eleBackorderedFrom = (YFCElement)nlBackOrderedLines.iterator().next();
			YFCElement	eleDetails = eleBackorderedFrom.getChildElement("Details");
			eleBackorderedFrom.setAttribute("ShipNode", eleDetails.getAttribute("ShipNode"));
			eleBackorderedFrom.setAttribute("ExpectedShipmentDate", eleDetails.getAttribute("ExpectedShipmentDate"));
			eleBackorderedFrom.setAttribute("ExpectedDeliveryDate", eleDetails.getAttribute("ExpectedDeliveryDate"));
		}
		return eleBackorderedFrom;
	}
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}
	
	
	public Properties getProperties() {
		return m_Props;
	}


	private Properties m_Props;	
}
