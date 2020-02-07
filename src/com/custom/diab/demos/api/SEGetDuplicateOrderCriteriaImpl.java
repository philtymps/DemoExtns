package com.custom.diab.demos.api;

import java.util.Properties;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Document;


@SuppressWarnings("deprecation")
public class SEGetDuplicateOrderCriteriaImpl implements YIFCustomApi 
{

	public Document	getDuplicateOrderInputCriteria (YFSEnvironment env, Document docIn)
	{

		YFCDocument criteriaDoc = YFCDocument.createDocument("Order");
        YFCElement criteriaRoot = criteriaDoc.getDocumentElement();
        YFCDocument docOrder  = YFCDocument.getDocumentFor(docIn);
        
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Entering SEGetDuplicateOrderCriteriaImpl - Input:");
        	System.out.println (docOrder.getString());
        }
        
        YFCElement eleOrder = docOrder.getDocumentElement();
        String sBillToID = eleOrder.getAttribute("BillToID");
        String sEnterpriseCode = eleOrder.getAttribute("EnterpriseCode");
        String sSourceIPAddress = eleOrder.getAttribute("SourceIPAddress");
        String sOrderName = eleOrder.getAttribute ("OrderName");
        criteriaRoot.setAttribute("BillToID", sBillToID);
        criteriaRoot.setAttribute("EnterpriseCode", sEnterpriseCode);
        criteriaRoot.setAttribute("SourceIPAddress", sSourceIPAddress);
        
        if (!YFCCommon.isVoid(sOrderName))
        	criteriaRoot.setAttribute("OrderName", sOrderName);
        YFCDate fromDate = eleOrder.getDateTimeAttribute ("OrderDate");     
        
        if(!YFCCommon.isVoid(fromDate))
        {
        	// orders created within 20 seconds of each other
        	YFCDate toDate = new YFCDate (fromDate);
            toDate.setSeconds(toDate.getSeconds() + 10);
            fromDate.setSeconds (fromDate.getSeconds() - 10);
            criteriaRoot.setAttribute("OrderDateQryType", "BETWEEN");
            criteriaRoot.setDateTimeAttribute("FromOrderDate", fromDate);
            criteriaRoot.setDateTimeAttribute("ToOrderDate", toDate);
        }

        YFCElement elePriceInfo = eleOrder.getChildElement("PriceInfo");
        if(elePriceInfo != null)
        {
            double totalAmount = elePriceInfo.getDoubleAttribute("TotalAmount");
            double fromAmount = totalAmount - 2.5D;
            if(fromAmount < 0.0D)
                fromAmount = 0.0D;
            double toAmount = totalAmount + 2.5D;
            YFCElement criteriaPriceInfo = criteriaRoot.createChild("PriceInfo");
            criteriaPriceInfo.setAttribute("TotalAmountQryType", "BETWEEN");
            criteriaPriceInfo.setAttribute("FromTotalAmount", fromAmount);
            criteriaPriceInfo.setAttribute("ToTotalAmount", toAmount);
        }
        // 
        if (YFSUtil.getDebug())
        {
        	System.out.println ("Exiting SEGetDuplicateOrderCriteriaImpl - Output:");
        	System.out.println (criteriaDoc.getString());
        }
        
        return criteriaDoc.getDocument();
	}
	
	@Override
	public void setProperties(Properties props) throws Exception 
	{
		m_props = props;
	}
	
	@SuppressWarnings("unused")
	private	Properties	m_props;
}
