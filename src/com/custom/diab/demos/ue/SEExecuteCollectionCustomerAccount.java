/**
  * SEExecuteCollectionCustomerAccount.java
  *
  **/

// PACKAGE
package com.custom.diab.demos.ue;

import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCollectionCustomerAccountUE;

import java.util.Properties;
import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;

@SuppressWarnings("deprecation")
public class SEExecuteCollectionCustomerAccount  implements YFSCollectionCustomerAccountUE
{

    public SEExecuteCollectionCustomerAccount()
    {
        m_props = null;
    }
    public void setProperties(Properties prop)
        throws Exception
    {
        m_props = prop;
    }

    public Document executeCollectionCustomerAccount(YFSEnvironment oEnv, Document inputDoc)
    {
        cat.debug("Entering SEExecuteCollectionCustomerAccount.executeCollectionCustomerAccount");
        String sResponseCode = "";
        YFCDocument inDoc = YFCDocument.getDocumentFor(inputDoc);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering executeCollectionCustomerAccount");
			System.out.println ("Input XML:");
			System.out.println (inDoc.getString());
		}
        sResponseCode = "APPROVED";
        YFCDocument outDoc = constructOutputDoc(inDoc, sResponseCode);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting executeCollectionCustomerAccount");
			System.out.println (outDoc.getString());
		}
        cat.debug("Exiting SEExecuteCollectionCustomerAccount.executeCollectionCustomerAccount");
        return outDoc.getDocument();
    }

    public YFCDocument constructOutputDoc(YFCDocument inDoc, String sResponseCode)
    {
        YFCDocument outDoc = YFCDocument.createDocument();
        YFCElement root = inDoc.getDocumentElement();
        YFCElement paymentRoot = outDoc.createElement("Payment");
        if(sResponseCode.equals("APPROVED"))
        {
            paymentRoot.setAttribute("ResponseCode", "APPROVED");
            paymentRoot.setAttribute("AsynchRequestProcess", "false");
            paymentRoot.setAttribute("AuthAVS", "AVSAuthorized");
            paymentRoot.setAttribute("AuthCode", "AuthCode1");
            paymentRoot.setAttribute("AuthorizationAmount", root.getDoubleAttribute("RequestAmount"));
            paymentRoot.setAttribute("AuthorizationExpirationDate", YTimestamp.HIGH_DATE.toString());
            paymentRoot.setAttribute("AuthorizationId", "AutId1");
            paymentRoot.setAttribute("AuthReturnCode", "AuthSuccess");
            paymentRoot.setAttribute("AuthReturnFlag", "T");
            paymentRoot.setAttribute("AuthReturnMessage", "AuthSuccess");
            paymentRoot.setAttribute("AuthTime", YTimestamp.newMutableTimestamp().getString(YFCDate.XML_DATE_FORMAT));
            paymentRoot.setAttribute("HoldOrderAndRaiseEvent", "N");
            paymentRoot.setAttribute("HoldReason", "");
            paymentRoot.setAttribute("TranAmount", root.getDoubleAttribute("RequestAmount"));
            paymentRoot.setAttribute("TranType", root.getAttribute("ChargeType"));
        } else if(sResponseCode.equals("HARD_DECLINED"))
        {
            paymentRoot.setAttribute("ResponseCode", "HARD_DECLINED");
            paymentRoot.setAttribute("AsynchRequestProcess", "false");
            paymentRoot.setDoubleAttribute("AuthorizationAmount", 0.0D);
            paymentRoot.setAttribute("AuthReturnFlag", "F");
            paymentRoot.setAttribute("AuthReturnMessage", "Credit Limit Exceeded");
            paymentRoot.setAttribute("HoldOrderAndRaiseEvent", "N");
            paymentRoot.setAttribute("HoldReason", " ");
            paymentRoot.setDoubleAttribute("TranAmount", 0.0D);
            paymentRoot.setAttribute("TranType", root.getAttribute("ChargeType"));
        } else if(sResponseCode.equals("SOFT_DECLINED"))
        {
            paymentRoot.setAttribute("ResponseCode", "SOFT_DECLINED");
            paymentRoot.setAttribute("AsynchRequestProcess", "false");
            paymentRoot.setDoubleAttribute("AuthorizationAmount", 0.0D);
            paymentRoot.setAttribute("AuthReturnFlag", "F");
            paymentRoot.setAttribute("AuthReturnMessage", "Declined");
            paymentRoot.setAttribute("HoldOrderAndRaiseEvent", "N");
            paymentRoot.setAttribute("HoldReason", " ");
            paymentRoot.setDoubleAttribute("TranAmount", 0.0D);
            paymentRoot.setAttribute("TranType", root.getAttribute("ChargeType"));
        } else if(sResponseCode.equals("BANK_HOLD"))
        {
            paymentRoot.setAttribute("ResponseCode", "BANK_HOLD");
            paymentRoot.setAttribute("AsynchRequestProcess", "false");
            paymentRoot.setDoubleAttribute("AuthorizationAmount", 0.0D);
            paymentRoot.setAttribute("AuthReturnFlag", "F");
            paymentRoot.setAttribute("AuthReturnMessage", "Bank Held");
            paymentRoot.setAttribute("HoldOrderAndRaiseEvent", "N");
            paymentRoot.setAttribute("HoldReason", " ");
            paymentRoot.setDoubleAttribute("TranAmount", 0.0D);
            paymentRoot.setAttribute("TranType", root.getAttribute("ChargeType"));
        } else if(sResponseCode.equals("SERVICE_UNAVAILABLE"))
        {
            paymentRoot.setAttribute("ResponseCode", "SERVICE_UNAVAILABLE");
            paymentRoot.setAttribute("AsynchRequestProcess", "false");
            paymentRoot.setDoubleAttribute("AuthorizationAmount", 0.0D);
            paymentRoot.setAttribute("AuthReturnFlag", "F");
            paymentRoot.setAttribute("HoldOrderAndRaiseEvent", "N");
            paymentRoot.setAttribute("HoldReason", " ");
            paymentRoot.setDoubleAttribute("TranAmount", 0.0D);
            paymentRoot.setAttribute("TranType", root.getAttribute("ChargeType"));
        }
        outDoc.appendChild(paymentRoot);
        return outDoc;
    }

    private Properties m_props;
    
	private static YFCLogCategory cat = YFCLogCategory.instance("com.custom.diab.demos.ue.SEExecuteCollectionCustomerAccount");
	@Override
	public YFSExtnPaymentCollectionOutputStruct collectionCustomerAccount(
			YFSEnvironment arg0, YFSExtnPaymentCollectionInputStruct arg1)
			throws YFSUserExitException {
		// TODO Auto-generated method stub
		return null;
	}
	public Properties getProperties() {
		return m_props;
	}

}
