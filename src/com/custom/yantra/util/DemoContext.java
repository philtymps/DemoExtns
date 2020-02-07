/**
  * DemoContext.java
  *
  **/

// PACKAGE
package com.custom.yantra.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;

public class DemoContext 
{	
    public DemoContext()
    {
    }

	//To get Yantra's API handle
	public YIFApi getYIFApi (boolean bLocal) throws Exception
	{
		if (yifApi == null || bLocal != bIsLocalApi)
		{
			if (bLocal)
				yifApi = YIFClientFactory.getInstance().getLocalApi ();
			else
				yifApi = YIFClientFactory.getInstance().getApi ("HTTP");
			bIsLocalApi = bLocal;
		}
		return yifApi;
	}

	public YIFApi getYIFApi() throws Exception 
	{
		return getYIFApi (false);
	}//getYIFApi

	//To get Yantra's Environment variable (Database connection)
	public YFSEnvironment getYFSEnv(String sUserId, String sPassword, String sProgId) throws Exception
	{
		yifApi = null;
		yfsEnv = null;
		setUserId (sUserId);
		setPassword (sPassword);
		setProgId (sProgId);
		return getYFSEnv();
	}

	public YFSEnvironment pushYFSEnv ()
	{
		return yfsEnv;
	}

	public YFSEnvironment pushYFSEnv (YFSEnvironment newEnv)
	{
		YFSEnvironment	oldEnv = yfsEnv;
		yfsEnv = newEnv;
		return oldEnv;
	}
	
	public void	popYFSEnv (YFSEnvironment env)
	{
		yfsEnv = env;
	}
	
	//To get Yantra's Environment variable (Database connection)
	public YFSEnvironment getYFSEnv() throws Exception 
	{
		if (yifApi == null)
			yifApi = getYIFApi();

		if (yfsEnv != null)
			return yfsEnv;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder         db = dbf.newDocumentBuilder();
		Document            envDoc = db.newDocument();
		Element         envElement = envDoc.createElement( "YFSEnvironment" );
		

		if (m_sUserId == null)
		{
			if (YFCCommon.isVoid(System.getProperty("yif.httpapi.userid")))
				setUserId("admin");
			else
				setUserId(System.getProperty("yif.httpapi.userid"));
		}
		if (m_sPassword == null)
		{
			if (YFCCommon.isVoid(System.getProperty("yif.httpapi.password")))
				setPassword("password");
			else
				setPassword(System.getProperty("yif.httpapi.password"));
		}
		if (m_sProgId == null)
			setProgId ("DemoExtns");
			
		envElement.setAttribute( "userId", m_sUserId );
		envElement.setAttribute( "progId", m_sProgId );

/*
		// new for 9x - this may be required - uncomment if 403 errors occur
		envElement.setAttribute ("LoginID", m_sLoginID);
		envElement.setAttribute ("Password", m_sPassword);
*/
		envDoc.appendChild( envElement );
		
		yfsEnv = yifApi.createEnvironment( envDoc );

		
		// new for 9x this is required
		Document loginInput = db.newDocument();
		Element loginElement = loginInput.createElement("Login");
		loginElement.setAttribute ("LoginID", m_sLoginID);
		loginElement.setAttribute ("Password", m_sPassword);		
		loginInput.appendChild(loginElement);
		
		System.out.println ("Attempting Login...LoginID="+loginElement.getAttribute("LoginID")+" Password="+loginElement.getAttribute("Password"));
		Document loginDoc = yifApi.login(yfsEnv, loginInput);
		System.out.println ("Login Successful...");

		yfsEnv.setTokenID(loginDoc.getDocumentElement().getAttribute("UserToken"));
		setSessionId(loginDoc.getDocumentElement().getAttribute("SessionId"));

		return yfsEnv;
	
	}//getYFSEnv

    public void releaseYFSEnvironment () throws Exception {
	
		if (yfsEnv != null && yifApi != null)
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder         db = dbf.newDocumentBuilder();
    		Document logoutDoc = db.newDocument();
	    	Element logoutElement = logoutDoc.createElement("registerLogout");
    		logoutElement.setAttribute("UserId", yfsEnv.getUserId());
    		logoutElement.setAttribute("SessionId", m_sSessionId);
	    	logoutDoc.appendChild(logoutElement);
    		yifApi.registerLogout(yfsEnv, logoutDoc);
    		yifApi.releaseEnvironment(yfsEnv);
	    	yfsEnv = null;
			yifApi = null;
		}
    }


	public	void	setYIFApi (YIFApi api) { yifApi = api; }	
	public	void	setYFSEnv (YFSEnvironment env) { yfsEnv = env; }
	public	void	setUserId (String sUserId) { m_sUserId = sUserId; m_sLoginID = sUserId; }
	public	void	setProgId (String sProgId) { m_sProgId = sProgId; }
	public	void	setPassword (String sPassword) { m_sPassword = sPassword; }
	public	void	setSessionId (String sSessionId) { m_sSessionId = sSessionId; }
	public	void	setDebug (boolean bDebug) { m_bDebug = bDebug; }
	public	boolean	getDebug()	  { return m_bDebug; }
		
	private	String			m_sUserId = null;
	private String			m_sLoginID = null;
	private String			m_sPassword = null;
	private	String			m_sProgId = null;
	private	String			m_sSessionId = null;
	private YIFApi			yifApi = null;
	private boolean			bIsLocalApi = false;
	private YFSEnvironment	yfsEnv = null;
	private boolean			m_bDebug = true;

}
