/**
  * SEProcessCustomOrderHold.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.api;

import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.custom.yantra.util.YFSUtil;

import java.util.Iterator;
import java.util.Properties;
import org.w3c.dom.Document;
import javax.xml.xpath.*;

public class SEProcessCustomOrderHold implements YIFCustomApi
{

    public SEProcessCustomOrderHold()
    {
    }

    @SuppressWarnings("rawtypes")
	public Document processCustomOrderHold (YFSEnvironment env, Document docIn) throws Exception
    {
        YFCDocument orderDoc = YFCDocument.getDocumentFor(docIn);
        YFCElement	eleHoldTypesToProcess = orderDoc.getDocumentElement().getChildElement("HoldTypesToProcess");
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering SEProcessCustomOrderHold:\r\n");
			System.out.println ("UE Input: \r\n" + orderDoc.getString ());
		}
        String status;
		String responseCodeDesc;
		String responseCode = getTestAttribute (env, (String)props.getProperty("CustomOrderHoldResponseCode"), docIn);
        if(!YFCCommon.equals("SUCCESS", responseCode))
		{
            status = "1200";	// rejected
			responseCodeDesc = "The System Rejected the Hold";
			if (!YFCObject.isVoid(props.getProperty("CustomOrderHoldResponseReason")))
				responseCodeDesc += " " + props.getProperty("CustomOrderHoldResponseReason");
		}
		else
		{
			status = "1300";	// approved
			responseCodeDesc = "The System Removed the Hold";
		}
			
        YFCDocument outDoc = YFCDocument.createDocument ("Order");
        YFCElement eleOrder = outDoc.getDocumentElement();
        YFCElement eleProcessedHoldTypes = eleOrder.createChild("ProcessedHoldTypes");
        for(Iterator iHoldTypesToProcess = eleHoldTypesToProcess.getChildren(); iHoldTypesToProcess.hasNext(); )
        {
            YFCElement eleHoldToProcess = (YFCElement)iHoldTypesToProcess.next();
        	YFCElement eleProcessedHold = eleProcessedHoldTypes.createChild("OrderHoldType");
            eleProcessedHold.setAttribute("HoldType", eleHoldToProcess.getAttribute("HoldType"));
            eleProcessedHold.setAttribute("ReasonText", responseCodeDesc);
			eleProcessedHold.setAttribute("Status", status);
        }
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting SEProcessCustomOrderHold:\r\n");
			System.out.println ("UE Output: \r\n" + outDoc.getString ());
		}
        return outDoc.getDocument();
    }

    public void setProperties(Properties prop)
        throws Exception
    {
        props = prop;
    }
	
	private String getTestAttribute (YFSEnvironment env, String sTestAttribute, Document docIn) throws Exception
	{
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to getOrderDetails:");
			System.out.println (YFCDocument.getDocumentFor (docIn).getString());
		}
		YFCDocument	docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docIn));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getOrderDetails:");
			System.out.println (docOrder.getString());
		}
		return evaluateXPathExpression (sTestAttribute, docOrder.getDocument());
	}

	private String evaluateXPathExpression (String sXPathExpr, Document docIn) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, docIn, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;			
		}
		return sResult;
	}	
	
	private Properties props;
}


