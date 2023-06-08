package com.custom.diab.demos.ue;


import com.custom.yantra.util.YFSUtil;
import com.ibm.sterling.sfo.api.optimizer.SFOOptimizeOrderAPI;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetExternalCostForOptionsUE;
import java.util.Iterator;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Document;


@SuppressWarnings("deprecation")
public class SESFOOptimizeOrderAPI extends SFOOptimizeOrderAPI implements OMPGetExternalCostForOptionsUE, YIFCustomApi {

	public SESFOOptimizeOrderAPI() {
		// TODO Auto-generated constructor stub
	}
	
	Properties	m_props;
	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_props = props;
	}
	

	@Override
	public Document getExternalCostForOptions(YFSEnvironment env, Document docIn) throws YFSUserExitException
    {
        YFCDocument docPromise = YFCDocument.getDocumentFor(docIn);
        YFCElement elePromise = docPromise.getDocumentElement();
        YFCElement elePromiseLines = elePromise.getChildElement("PromiseLines");
        Iterator<YFCElement> iPromiseLines = elePromiseLines.getChildren();
        
        elePromise.setAttribute("OrderDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        
        if(YFSUtil.getDebug())
        	
        {
    		System.out.println("Input to before SEGetExternalCostForOptions UE:");
            System.out.println(elePromise.getString());
        }
        while(iPromiseLines.hasNext()) 
        {
            YFCElement	elePromiseLine = (YFCElement)iPromiseLines.next();
            YFCElement	eleShipToAddress = elePromiseLine.getChildElement("ShipToAddress");
            YFCNodeList eleAssignments = elePromiseLine.getElementsByTagName("Assignment");
            Iterator iAssignments = eleAssignments.iterator();
            String sDeliveryDate = getOrderLineRDD(env, elePromiseLine.getAttribute("OrderLineReference"));
            
            // substitute OMS CarrierServiceCode with Optimizer SHIPPING Group based on API args passed
            String sCarrierServiceCode = elePromiseLine.getAttribute("CarrierServiceCode");
            if (!YFCObject.isVoid(m_props.getProperty(sCarrierServiceCode)))
            	elePromiseLine.setAttribute("CarrierServiceCode", m_props.getProperty(sCarrierServiceCode));

            // if no country default to US
            if (!YFCObject.isNull(eleShipToAddress) && YFCObject.isVoid(eleShipToAddress.getAttribute("Country")))
				eleShipToAddress.setAttribute("Country", "US");
            
            
            while(iAssignments.hasNext()) 
            {
                YFCElement eleAssignment = (YFCElement)iAssignments.next();
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
            }
        }
        if(YFSUtil.getDebug())
        	
        {
            System.out.println("Order No: " + elePromise.getAttribute("OrderNo") + " IS Eligible for Optimization");
            System.out.println(elePromise.getString());
        }
        Document docOut = super.invoke(env, docPromise.getDocument());
        if(YFSUtil.getDebug())
        {
            System.out.println("Output after SEGetExternalCostForOptions UE:");
            System.out.println(YFCDocument.getDocumentFor(docOut).getString());
        }
        return docOut;
    }

    String getOrderLineRDD(YFSEnvironment env, String sOrderLineKey)
        throws YFSUserExitException
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
}
