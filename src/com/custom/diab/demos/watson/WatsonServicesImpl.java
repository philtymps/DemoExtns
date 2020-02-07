package com.custom.diab.demos.watson;

import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class WatsonServicesImpl implements YIFCustomApi {

	
	public WatsonServicesImpl() {
		super();
		this.m_props = new Properties();
		this.m_sUserId = null;
		this.m_sLoginID = null;
		this.m_sPassword = null;
		this.m_sProgId = null;
		this.m_sSessionId = null;
		this.yifApi = null;
		this.yfsEnv = null;
	}
	
	public Document	getWatsonOrderDetails (String sEnterpriseCode, String sDocumentType, String sOrderNo) throws Exception
	{
		YFCDocument	docOrder = YFCDocument.createDocument("Order");
		YFCElement	eleOrder = docOrder.getDocumentElement();
		try {
			YFSEnvironment  env = getYFSEnv();
			YIFApi			api = YIFClientFactory.getInstance().getApi();
			
			eleOrder.setAttribute ("EnterpriseCode", sEnterpriseCode);
			eleOrder.setAttribute ("OrderNo", sOrderNo);
			eleOrder.setAttribute ("DocumentType", sDocumentType);
			
			return api.getOrderDetails(env,  docOrder.getDocument());
			
		} catch (YFSException e) {
			throw new Exception (e.getMessage());
		}
	}
	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_props = props;
	}
		
	public static void main(String[] args) throws Exception
	{	
		String sEnterpriseCode = "Aurora";
		String sDocType = "0001";
		String sOrderNo = "OM1000000";
		for (int i = 0; i < args.length; i++){
			if (args[i].equals("-enterprise")){
				sEnterpriseCode = args[i + 1];
			} else if (args[i].equals("-doctype")){
				sDocType = args[i + 1];
			} else if (args[i].equals("-orderno")){
				sOrderNo = args[i + 1];
			}
		}
		WatsonServicesImpl 	wsi = new WatsonServicesImpl();

		System.out.println (YFCDocument.getDocumentFor(wsi.getWatsonOrderDetails (sEnterpriseCode, sDocType, sOrderNo)).getString());
	}
	
	public YFSEnvironment getYFSEnv() throws Exception 
	{
		DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder         db = dbf.newDocumentBuilder();
		Document           		envDoc = db.newDocument();
		Element         		envElement = envDoc.createElement( "YFSEnvironment" );

		if (yfsEnv != null)
			return yfsEnv;

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
			setProgId ("WatsonBOTS");
			
		envElement.setAttribute( "userId", m_sUserId );
		envElement.setAttribute( "progId", m_sProgId );

		// new for 9x this is required
		envElement.setAttribute ("LoginID", m_sLoginID);
		envElement.setAttribute ("Password", m_sPassword);

		envDoc.appendChild( envElement );
		yifApi = YIFClientFactory.getInstance().getApi();
		yfsEnv = yifApi.createEnvironment(envDoc);

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

	
	public	void	setUserId (String sUserId) { m_sUserId = sUserId; m_sLoginID = sUserId; }
	public	void	setProgId (String sProgId) { m_sProgId = sProgId; }
	public	void	setPassword (String sPassword) { m_sPassword = sPassword; }
	public	String 	getSessionId() { return m_sSessionId;	}
	public	void 	setSessionId(String sSessionId) {	m_sSessionId = sSessionId; }

	public Properties getProperties() {
		return m_props;
	}

	private	String			m_sUserId = null;
	private	String			m_sLoginID = null;
	private	String			m_sPassword = null;
	private	String			m_sProgId = null;
	private	String			m_sSessionId = null;
	private	YIFApi			yifApi = null;
	private	YFSEnvironment	yfsEnv = null;
	private Properties		m_props;

}
