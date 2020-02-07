/**
  * YantraMapperUtils.java
  *
  **/

// PACKAGE
package com.custom.yantra.xmlmapper;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.custom.yantra.util.*;

public class YantraMapperUtils 
{
    public YantraMapperUtils() {}


	public static String callAPI(YFSEnvironment env, String s)
	{
		if (YFSUtil.getDebug())
		{
			System.out.println ("User ID from YFSEnvironment="+env.getUserId());
		}	
		return s;
	}
	
	public static String computeCountry (String sCountryIn)
	{
		String sRet = sCountryIn;
		
		if (sCountryIn.equalsIgnoreCase ("United States"))
			sRet = "US";
		return sRet;
	}

	public static String computeUOM (String sUOMIn)
	{
		String sRet = sUOMIn.toUpperCase ();
		return sRet;
	}
	
	@SuppressWarnings("unused")
	public static String computeBuyerOrg(YFSEnvironment env, String sBuyerNo) throws Exception
	{
		YFSEnvironment envOld = YFSUtil.pushYFSEnv ();
		try {
			YFSUtil.setYFSEnv (env);
			YIFApi	api = YFSUtil.getYIFApi ();

			// call getCustomerDetails API in Yantra to convert Buyer Account No to Yantra
			// Buyer organization code
			//Document docCustomerDetails = api.getCustomerDetails (YFSUtil.getYFSEnv(), YFCDocument.getDocumentFor ("<Customer CustomerID=\""+sBuyerNo+"\" OrganizationCode=\"DEFAULT\"/>").getDocument());
			//YFCElement	elemCustomer = YFCDocument.getDocumentFor (docCustomerDetails).getDocumentElement();
			//return elemCustomer.getChildElement ("BuyerOrganization").getAttribute ("OrganizationCode");
			return "";
		} catch (Exception e) {
			throw e;
		} finally {
			YFSUtil.popYFSEnv(envOld);
		}
	}

}

