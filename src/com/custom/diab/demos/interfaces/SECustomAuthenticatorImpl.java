package com.custom.diab.demos.interfaces;

import java.util.Map;
import java.util.HashMap;

import com.custom.yantra.util.YFSUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.util.YFSAuthenticator;
import com.yantra.yfs.japi.util.YFSAuthenticatorEx;

public class SECustomAuthenticatorImpl implements YFSAuthenticator,
		YFSAuthenticatorEx {

	@Override
	public Map<String, String> authenticate(YFCElement eleUserNameElementPassed, YFCElement eleUserNameElementFromDB) throws Exception {
		HashMap<String, String>	mapRet;
		boolean	bSuccess;
		mapRet = new HashMap<String, String>();
		
		// test password
		bSuccess = false;
		if (YFSUtil.getDebug())
		{
			System.out.println ("In Custom Authenticator Class:");
			System.out.println ("\nUser Element Passed:\n" + eleUserNameElementPassed.getString());
			System.out.println ("\nUser Element from DB:\n" + eleUserNameElementFromDB.getString());
		}
		// if login ID and Password match what is on the Database
		if (eleUserNameElementPassed.getAttribute("LoginID").equals(eleUserNameElementFromDB.getAttribute("LoginID")))
			if (eleUserNameElementPassed.getAttribute("Password").equals(eleUserNameElementFromDB.getAttribute("Password")))
					bSuccess = true;
		if (!bSuccess)
		{
			Exception eAuthenticationException = new Exception ("Custom Authentication Failure: UserName=" + eleUserNameElementPassed.getAttribute ("Loginid") + " Password=" + eleUserNameElementPassed.getAttribute("Password"));
			throw (eAuthenticationException);
		}
		mapRet.put ("Success", "Y");
		// OPTIONAL Add the Following Map entries if Password is near expiration
		//   mapRet.put("ExpiresInDays", "15");
        //   mapRet.put("ChangePasswordLink", "html link to password management screen");

		return mapRet;
	}

	@Override
	public Map<String, String> authenticate(String sUserName, String sPassword) throws Exception {

		// This Authentication Example Never Fails 
		YFCDocument	docLoginPassed = YFCDocument.createDocument("Login");
		YFCElement	eleLoginPassed = docLoginPassed.getDocumentElement();
		eleLoginPassed.setAttribute ("LoginID", sUserName);
		eleLoginPassed.setAttribute("Password", sPassword);
		return (authenticate (eleLoginPassed, eleLoginPassed));
	}
}
