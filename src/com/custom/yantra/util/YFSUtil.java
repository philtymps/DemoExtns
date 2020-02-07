/**
  * YFSUtil.java
  *
  **/


package com.custom.yantra.util;

import java.util.*;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;

public class YFSUtil
{
	//To get Yantra's API handle
	public static YIFApi getYIFApi (boolean bLocal) throws Exception
	{
		if (yifApi == null || bLocal != bIsLocalApi)
		{
			if (bLocal)
				yifApi = YIFClientFactory.getInstance().getLocalApi ();
			else
				yifApi = YIFClientFactory.getInstance().getApi ();
			bIsLocalApi = bLocal;
		}
		return yifApi;
	}

	public static YIFApi getYIFApi() throws Exception 
	{
		return getYIFApi (false);
	}//getYIFApi

	//To get Yantra's Environment variable (Database connection)
	public static YFSEnvironment getYFSEnv(String sUserId, String sPassword, String sProgId) throws Exception
	{
		yifApi = null;
		yfsEnv = null;
		setUserId (sUserId);
		setPassword (sPassword);
		setProgId (sProgId);
		return getYFSEnv();
	}

	public static YFSEnvironment pushYFSEnv ()
	{
		return yfsEnv;
	}

	public static YFSEnvironment pushYFSEnv (YFSEnvironment newEnv)
	{
		YFSEnvironment	oldEnv = yfsEnv;
		yfsEnv = newEnv;
		return oldEnv;
	}
	
	public static void	popYFSEnv (YFSEnvironment env)
	{
		yfsEnv = env;
	}
	
	//To get Yantra's Environment variable (Database connection)
	public static YFSEnvironment getYFSEnv() throws Exception 
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

		// new for 9x this is required
		envElement.setAttribute ("LoginID", m_sLoginID);
		envElement.setAttribute ("Password", m_sPassword);

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

    public static void releaseYFSEnvironment () throws Exception {
	
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
		}
    	yfsEnv = null;
		yifApi = null;
    }
	
	@SuppressWarnings("rawtypes")
	public static void dumpMapData (Map<?, ?> mapData)
	{
		if (YFSUtil.getDebug())
		{					
			Set<?>	s = mapData.entrySet();
			System.out.println ("mapData:\tKey\t\t\tValue");
			System.out.println ("\t\t-----------------------\t-------------------------------------------------");
			for (Iterator<?> i = s.iterator(); i.hasNext(); )
			{
				Map.Entry e = (Map.Entry)i.next();
				String sKey = (String)e.getKey();
				String sVal = (String)e.getValue();
				System.out.print ("\t\t"+sKey);
				if (sKey.length() < 16)
					System.out.print ("\t");
				if (sKey.length() < 8)
					System.out.print ("\t");
				System.out.println ("\t"+sVal);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static String mapDataToAttributes (Map<?, ?> mapData)
	{
		Set<?>	s = mapData.entrySet();
		StringBuffer	sBuf = new StringBuffer();
		
		for (Iterator<?> i = s.iterator(); i.hasNext(); )
		{
			Map.Entry e = (Map.Entry)i.next();
			String sKey = (String)e.getKey();
			String sVal = (String)e.getValue();
			sBuf.append (" "+sKey+"=\""+sVal+"\"");
		}
		return sBuf.toString();
	}

	public	static	void	setYIFApi (YIFApi api) { yifApi = api; }	
	public	static	void	setYFSEnv (YFSEnvironment env) { yfsEnv = env; }
	public	static	void	setUserId (String sUserId) { m_sUserId = sUserId; m_sLoginID = sUserId; }
	public	static	void	setProgId (String sProgId) { m_sProgId = sProgId; }
	public	static	void	setPassword (String sPassword) { m_sPassword = sPassword; }
	public	static	void	setSessionId (String sSessionId) { m_sSessionId = sSessionId; }
	public	static	void	setDebug (boolean bDebug) { m_bDebug = bDebug; }
	public	static	boolean	getDebug()	  { return m_bDebug; }
		
	private	static	String			m_sUserId = null;
	private static	String			m_sLoginID = null;
	private static	String			m_sPassword = null;
	private	static	String			m_sProgId = null;
	private	static	String			m_sSessionId = null;
	private static	YIFApi			yifApi = null;
	private static	boolean			bIsLocalApi = false;
	private static	YFSEnvironment	yfsEnv = null;
	private static	boolean			m_bDebug = true;

};//class YFSUtil


