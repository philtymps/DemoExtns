package com.custom.diab.demos.ue;


import com.custom.yantra.util.YFSUtil;
import com.ibm.commerce.otmz.client.MIME;
import com.ibm.commerce.otmz.client.SIPWebClient;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetExternalCostForOptionsUE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;


public class SESIPOptimizeOrderUEImpl implements YIFCustomApi, OMPGetExternalCostForOptionsUE {

	private	Properties	m_props;
	
	public SESIPOptimizeOrderUEImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_props = props;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Document getExternalCostForOptions(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
        YFCDocument docPromise = YFCDocument.getDocumentFor(docIn);
        YFCElement	elePromise = docPromise.getDocumentElement();
        if(YFSUtil.getDebug())
        {
    		System.out.println("Input to before SEGetExternalCostForOptions UE:");
            System.out.println(elePromise.getString());
        }

        boolean		bIsOrderEligibleForOptimizer = isOrderEligibleForOptimizer(env, docPromise); 
        String		sOrderNo = elePromise.getAttribute("OrderNo");
        
    	if (bIsOrderEligibleForOptimizer)
    	{
            if(YFSUtil.getDebug())
            	
            {
                System.out.println("Order No: " + sOrderNo + " IS Eligible for Optimization");
            }
    	}
    	else
    	{
            if(YFSUtil.getDebug())
            {
                System.out.println("Order IS NOT Eligible for Optimization");
            }
            return docIn;
    	}

        
        YFCElement elePromiseLines = elePromise.getChildElement("PromiseLines");
        Iterator<YFCElement> iPromiseLines = elePromiseLines.getChildren();
        
        while(iPromiseLines.hasNext()) 
        {
            YFCElement elePromiseLine = (YFCElement)iPromiseLines.next();
            YFCElement eleShipToAddress = elePromiseLine.getChildElement("ShipToAddress");
            
            YFCNodeList<YFCElement> eleAssignments = elePromiseLine.getElementsByTagName("Assignment");
            Iterator iAssignments = eleAssignments.iterator();

            // substitute OMS CarrierServiceCode with Optimizer SHIPPING Group based on API args passed
            String sCarrierServiceCode = elePromiseLine.getAttribute("CarrierServiceCode");
            if (!YFCObject.isVoid(m_props.getProperty(sCarrierServiceCode)))
            	elePromiseLine.setAttribute("CarrierServiceCode", m_props.getProperty(sCarrierServiceCode));

            // if no country default to US
            if (!YFCObject.isNull(eleShipToAddress) && YFCObject.isVoid(eleShipToAddress.getAttribute("Country")))
				eleShipToAddress.setAttribute("Country", "US");
            
           // String sDeliveryDate = getOrderLineRDD(env, elePromiseLine.getAttribute("OrderLineReference"));
            while(iAssignments.hasNext()) 
            {
                YFCElement eleAssignment = (YFCElement)iAssignments.next();
                sCarrierServiceCode = eleAssignment.getAttribute("CarrierServiceCode");
                if (!YFCObject.isVoid(m_props.getProperty(sCarrierServiceCode)))
                	eleAssignment.setAttribute("CarrierServiceCode", m_props.getProperty(sCarrierServiceCode));
                String sCapacityConsumed = eleAssignment.getAttribute("CapacityConsumed");
                if (YFCObject.isVoid(sCapacityConsumed))
                	eleAssignment.setIntAttribute("CapacityConsumed", 0);
                
                /*
                if(YFCObject.isVoid(sDeliveryDate))
                {
                    String sCarrierService = eleAssignment.getAttribute("CarrierServiceCode");
                    YFCDate dtDeliveryDate = eleAssignment.getDateTimeAttribute("ShipDate");
                    dtDeliveryDate.changeDate(getCarrierTransitDays(sCarrierService));
                    eleAssignment.setDateTimeAttribute("DeliveryDate", dtDeliveryDate);
                }
                else
                {
                    eleAssignment.setAttribute("DeliveryDate", sDeliveryDate);
                }
             	*/
            }
        }

        String	sTenantId = getTenantProperty (env);
        try {
            elePromise.setAttribute("OrderDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        	if(YFSUtil.getDebug())
        	{
        		System.out.println("Input to SIP before Optimization:");
        		System.out.println(docPromise.getString());
        	}
        	YFCDocument docOut = YFCDocument.getDocumentFor(SIPWebClient.getInstance(sTenantId).invokeOptimizerApi(docPromise.getString(), MIME.APPLICATION_XML, "optimizer"));
        	if(YFSUtil.getDebug())
        	{
        		System.out.println("Output from SIP after Optimization");
        		System.out.println(docOut.getString());
        	}
        	return docOut.getDocument();
        } catch (Exception e) {
        	throw new YFSUserExitException (e.getMessage());
        }
    }
	
	boolean isOrderEligibleForOptimizer (YFSEnvironment env, YFCDocument docPromise) throws YFSUserExitException
	{
		boolean bRet = false;
		String	sOrderHeaderKey = docPromise.getDocumentElement().getAttribute("OrderHeaderKey");
		if (YFCObject.isVoid(sOrderHeaderKey))
			return false;
		
		try
        {
            YIFApi api = YIFClientFactory.getInstance().getLocalApi();
            YFCDocument docOrderDetails = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"" + sOrderHeaderKey + "\"/>");
            YFCDocument docOrderDetailsTemplate = YFCDocument.getDocumentFor("<Order OrderHeaderKey=\"\" OrderNo=\"\" DocumentType=\"\" ><Extn ExtnOptimizerFlag=\"\"/></Order>");
            env.setApiTemplate("getOrderDetails", docOrderDetailsTemplate.getDocument());
            if (YFSUtil.getDebug())
            {
            	System.out.println ("Input to getOrderDetails");
            	System.out.println (docOrderDetails.getString());
            }
            docOrderDetails = YFCDocument.getDocumentFor(api.getOrderDetails(env, docOrderDetails.getDocument()));
            if (YFSUtil.getDebug())
            {
            	System.out.println ("Output from getOrderDetails");
            	System.out.println (docOrderDetails.getString());
            }
            YFCElement eleOrderDetails = docOrderDetails.getDocumentElement();
    		YFCElement	eleExtn = eleOrderDetails.getChildElement("Extn");
    		if (!YFCObject.isNull(eleExtn))
    		{
        		String		sExtnOptimizerFlag = eleExtn.getAttribute("ExtnOptimizerFlag"); 
        		String		sDocumentType = eleOrderDetails.getAttribute("DocumentType");
        		bRet = !YFCObject.isVoid(sExtnOptimizerFlag) && sExtnOptimizerFlag.equals("Y") && sDocumentType.equals("0001");
    		}
        }
        catch(Exception e)
        {
        	env.clearApiTemplate("getOrderDetails");
            throw new YFSUserExitException(e.getMessage());
        } finally {
        	env.clearApiTemplate("getOrderDetails");
        }
		
		return bRet;
	}

    protected String getTenantProperty (YFSEnvironment env) throws YFSUserExitException
    {
            try
            {
            	YFCDocument docProperty = YFCDocument.getDocumentFor("<GetProperty CategoryName=\"iv_integration\" PropertyName=\"tenantId\"/>");
                YIFApi api = YIFClientFactory.getInstance().getLocalApi();
                docProperty = YFCDocument.getDocumentFor(api.getProperty(env, docProperty.getDocument()));
                return docProperty.getDocumentElement().getAttribute("PropertyValue");
            }
            catch(Exception e)
            {
                throw new YFSUserExitException(e.getMessage());
            }
    }
    
/*	
    String getOrderLineRDD(YFSEnvironment env, String sOrderLineKey) throws YFSUserExitException
    {
        String sDeliveryDate;
        sDeliveryDate = null;
        if(YFCObject.isVoid(sOrderLineKey))
            return sDeliveryDate;
        try
        {
            YIFApi api = YIFClientFactory.getInstance().getLocalApi();
            YFCDocument docOrderLineDetailsInput = YFCDocument.getDocumentFor((new StringBuilder("<OrderLineDetail OrderLineKey=\"")).append(sOrderLineKey).append("\"/>").toString());
            YFCDocument docOrderLineDetailsTemplate = YFCDocument.getDocumentFor("<OrderLine OrderLineKey=\"\" ReqDeliveryDate=\"\" />");
            env.setApiTemplate("getOrderLineDetails", docOrderLineDetailsTemplate.getDocument());
            YFCDocument docOrderLineDetails = YFCDocument.getDocumentFor(api.getOrderLineDetails(env, docOrderLineDetailsInput.getDocument()));
            YFCElement eleOrderLineDetails = docOrderLineDetails.getDocumentElement();
            sDeliveryDate = eleOrderLineDetails.getAttribute("ReqDeliveryDate");
        }
        catch(Exception e)
        {
        	env.clearApiTemplate("getOrderLineDetails");
            throw new YFSUserExitException(e.getMessage());
        } finally {
        	env.clearApiTemplate("getOrderLineDetails");
        }
        return sDeliveryDate;
    }

    protected int getCarrierTransitDays(String sCarrierServiceCode)
    {
        if(!YFCObject.isVoid(sCarrierServiceCode))
        {
            if(sCarrierServiceCode.contains("1") || sCarrierServiceCode.contains("PREMIUM") || sCarrierServiceCode.contains("NEXT"))
                return 1;
            if(sCarrierServiceCode.contains("2") || sCarrierServiceCode.contains("EXPRESS") || sCarrierServiceCode.contains("SECOND"))
                return 2;
        }
        return 7;
    }
*/
}
