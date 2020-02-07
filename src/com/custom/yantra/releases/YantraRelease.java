/**
  * YantraRelease.java
  *
  **/

// PACKAGE
package com.custom.yantra.releases;

import	java.util.*;
import	org.w3c.dom.*;

import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import java.io.Serializable;
import com.yantra.yfc.dom.*;

@SuppressWarnings("serial")
public class YantraRelease implements Serializable
{
    public YantraRelease()
    {
		m_vecReleaseLines = new Vector<Object> ();
		m_sEnterpriseCode = "DEFAULT";
		m_sOrderHeaderKey = "";
		m_sReleaseKey = "";
		m_sReleaseNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sReleaseDate = "";
		m_sDeliveryDate = "";
		m_sStatus = "";
		m_sStatusDesc = "";
		m_sNumOfPallets="0";
		m_sNumOfCartons="0";
    }
	

	public void Reset()
	{
		m_sEnterpriseCode = "";
		m_sOrderHeaderKey = "";
		m_sReleaseKey = "";
		m_sReleaseNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sReleaseDate = "";
		m_sDeliveryDate = "";
		m_sStatus = "";	
		m_sStatusDesc = "";
		m_sNumOfPallets="0";
		m_sNumOfCartons="0";
		for (int iEle = 0; iEle < getReleaseLineCount(); iEle++)		
			getReleaseLine(iEle).Reset();
		m_vecReleaseLines.clear();
	}

	public void addReleaseLine (Object oLine)
	{
		m_vecReleaseLines.addElement (oLine);
	}
	
	public String	getEnterpriseCode () { return m_sEnterpriseCode; }
	public void		setEnterpriseCode (String sEnterpriseCode) { m_sEnterpriseCode = sEnterpriseCode; }
	public String	getDocumentType () { return m_sDocumentType; }
	public void		setDocumentType (String sDocumentType) { m_sDocumentType = sDocumentType; }
	public	String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public	void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public	String	getReleaseKey () { return m_sReleaseKey; }
	public	void	setReleaseKey (String sReleaseKey) { m_sReleaseKey = sReleaseKey; }
	public	String	getReleaseNo () { return m_sReleaseNo; }
	public	void	setReleaseNo (String sReleaseNo) { m_sReleaseNo = sReleaseNo; }
	public	String	getReleaseDate () { if (m_sReleaseDate != null) return m_sReleaseDate; else return new String(); }
	public	void	setReleaseDate (String sReleaseDate) { if (sReleaseDate !=null ) m_sReleaseDate = sReleaseDate; }
	public	String	getDeliveryDate ()	{ return m_sDeliveryDate; }
	public	void	setDeliveryDate (String sDeliveryDate) { m_sDeliveryDate = sDeliveryDate; }
	public	String	getNumOfPallets () { return m_sNumOfPallets; }
	public	void	setNumOfPallets (String sNumOfPallets) { m_sNumOfPallets = sNumOfPallets; }
	public	String	getNumOfCartons () { return m_sNumOfCartons; }
	public	void	setNumOfCartons (String sNumOfCartons) { m_sNumOfCartons = sNumOfCartons; }
	public	String	getStatus () { return m_sStatus; }
	public	void	setStatus (String sStatus) { m_sStatus = sStatus; }
	public	String	getStatusDescription () { return m_sStatusDesc; }
	public	void	setStatusDescription (String sStatusDesc) { m_sStatusDesc = sStatusDesc; }
	public	String	getShipNode () { return m_sShipNode; }
	public	void	setShipNode (String sShipNode) { m_sShipNode = sShipNode; }
	public	String	getReceiveNode () { return m_sReceiveNode; }
	public	void	setReceiveNode (String sReceiveNode) { m_sReceiveNode = sReceiveNode; }

	public	Object	createNewReleaseLine () {	return (Object)new YantraReleaseLine(this); }

	
	public	YantraReleaseLine getReleaseLine (int iLine)
	{
		return (YantraReleaseLine)m_vecReleaseLines.elementAt (iLine);
	}

	public	Vector<Object>	getReleaseLines () { return m_vecReleaseLines; }
	public	int		getReleaseLineCount () { return m_vecReleaseLines.size(); }
		
	public String getReleaseDetails () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable<String, String> htRelease = new Hashtable<String, String>();
		htRelease.put ("ReleaseKey", getReleaseKey());

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Release", htRelease);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for getReleaseDetails() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		Document docRelease = api.getOrderReleaseDetails (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from getReleaseDetails() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docRelease));
		}
		YFCElement eleRelease = YFCDocument.getDocumentFor (docRelease).getDocumentElement();
		setEnterpriseCode (eleRelease.getAttribute ("EnterpriseCode"));
		setOrderHeaderKey (eleRelease.getAttribute ("OrderHeaderKey"));
		setDocumentType (eleRelease.getAttribute ("DocumentType"));		
		setReleaseNo (eleRelease.getAttribute ("ReleaseNo"));
		setReleaseKey (eleRelease.getAttribute ("ReleaseKey"));
		setStatus (eleRelease.getAttribute ("Status"));
		setShipNode (eleRelease.getAttribute ("ShipNode"));
		setReceiveNode (eleRelease.getAttribute ("ReceivingNode"));
		setNumOfPallets (eleRelease.getAttribute ("NumOfPallets"));
		setNumOfCartons (eleRelease.getAttribute ("NumOfCartons"));
		
		if (eleRelease.getAttribute ("ActualDeliveryDate") != null)
			setDeliveryDate (eleRelease.getAttribute ("ActualDeliveryDate"));		
		else	
			setDeliveryDate (eleRelease.getAttribute ("ExpectedDeliveryDate"));		
		if (eleRelease.getAttribute ("ActualReleaseDate") != null)
			setReleaseDate (eleRelease.getAttribute ("ActualReleaseDate"));		
		else	
			setReleaseDate (eleRelease.getAttribute ("ExpectedReleaseDate"));
		YFCElement eleStatus = eleRelease.getChildElement ("Status");
		if (eleStatus != null)
			setStatusDescription (eleStatus.getAttribute ("Description"));
		
		// now load the order line list
		loadReleaseLines (docRelease);
		return YFSXMLUtil.getXMLString (docRelease);
	}

	public String changeRelease () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSXMLParser	inXml = new YFSXMLParser();	
		Document		docOutXML = null;

		Hashtable<String, String>	htRelease = new Hashtable<String, String>();
		htRelease.put("ReleaseKey", getReleaseKey());
		Element eleRelease = inXml.createRootElement ("Release", htRelease);
		Hashtable<String, String>	htReleaseLines = new Hashtable<String, String>();
		htReleaseLines.put ("Replace", "N");
		Element eleReleaseLines = inXml.createChild (eleRelease, "ReleaseLines", htReleaseLines);
		// iterate over release lines lines
		for (int iEle = 0; iEle < getReleaseLines ().size(); iEle++)
		{
			YantraReleaseLine oReleaseLine = getReleaseLine(iEle);
			
			// generate XML for confirm release API
			Hashtable<String, String>	htReleaseLine = new Hashtable<String, String>();
			htReleaseLine.put("ReleaseLineKey", oReleaseLine.getReleaseLineKey());
			Element eleReleaseLine = inXml.createChild (eleReleaseLines, "ReleaseLine", htReleaseLine);	

			// if lot number/serial number not empty
			if (oReleaseLine.getLotNumber().length () > 0 || oReleaseLine.getSerialNo().length() > 0)
			{
				Hashtable<String, String>	htReleaseTagSerials = new Hashtable<String, String> ();
				htReleaseTagSerials.put ("Replace", "Y");
		
				Element eleReleaseTagSerials = inXml.createChild (eleReleaseLine, "ReleaseTagSerials", htReleaseTagSerials);
				Hashtable<String, String>	htReleaseTagSerial = new Hashtable<String, String> ();
				
				if (oReleaseLine.getLotNumber().length () > 0)
				{
					htReleaseTagSerial.put ("LotNumber", oReleaseLine.getLotNumber());
					htReleaseTagSerial.put ("Quantity", oReleaseLine.getQty ());
				}

				if (oReleaseLine.getSerialNo ().length() > 0)
				{
					htReleaseTagSerial.put ("SerialNo", oReleaseLine.getSerialNo ());
					htReleaseTagSerial.put ("Quantity", "1");
				}
				inXml.createChild (eleReleaseTagSerials, "ReleaseTagSerial", htReleaseTagSerial);
			}
		}
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for changeRelease() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.changeRelease (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeRelease() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}
		return YFSXMLUtil.getXMLString (docOutXML);
	
	}	

	public String receiveRelease () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSXMLParser	inXml = new YFSXMLParser();	
		Document		docOutXML = null;
		
		Hashtable<String, String>	htReceipt = new Hashtable<String, String>();
		htReceipt.put ("DocumentType", getDocumentType());
		htReceipt.put ("ReleaseKey", getReleaseKey());
		htReceipt.put ("ReceivingNode", getReceiveNode());
		htReceipt.put ("NumOfCartons", getNumOfCartons());
		htReceipt.put ("NumOfPallets", getNumOfPallets());
		inXml.createRootElement("Receipt", htReceipt);
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for startReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.startReceipt (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from startReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}
		YFCDocument	docReceipt = YFCDocument.getDocumentFor (docOutXML);
		String	sReceiptHeaderKey = docReceipt.getDocumentElement().getAttribute ("ReceiptHeaderKey");

		// a receipt is now open, add receipt lines based on release lines (one for one)
		inXml = new YFSXMLParser ();
		htReceipt = new Hashtable<String, String>();
		htReceipt.put ("DocumentType", getDocumentType());
		htReceipt.put ("ReceiptHeaderKey", sReceiptHeaderKey);
		htReceipt.put ("ReceivingNode", getReceiveNode());
		Element eleReceipt = inXml.createRootElement("Receipt", htReceipt);

		// now iterate over release lines and add to receipt lines in same quantity
		Element eleReceiptLines = inXml.createChild (eleReceipt, "ReceiptLines", null);
		for (int iReleaseLine = 0; iReleaseLine < getReleaseLineCount(); iReleaseLine++)
		{
			YantraReleaseLine oReleaseLine = getReleaseLine (iReleaseLine);
			Hashtable<String, String>	htReceiptLine = new Hashtable<String, String>();
			htReceiptLine.put ("ReleaseLineKey", oReleaseLine.getReleaseLineKey());
			htReceiptLine.put ("Quantity", oReleaseLine.getReceiveQty());
			
			// add serial & lot numbers
			if (oReleaseLine.getLotNumberToReceive().length () > 0)
			{
				htReceiptLine.put ("LotNumber", oReleaseLine.getLotNumberToReceive());
			}
			if (oReleaseLine.getSerialNoToReceive ().length() > 0)
			{
				htReceiptLine.put ("SerialNo", oReleaseLine.getSerialNoToReceive ());				
			}
			inXml.createChild (eleReceiptLines, "ReceiptLine", htReceiptLine);
		}		
		
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for receiveOrder() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		// receive the release
		docOutXML = api.receiveOrder (env, inXml.getDocument());

		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from receiveOrder() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}		
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for closeReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		inXml = new YFSXMLParser ();
		inXml.createRootElement("Receipt", htReceipt);
		Document docCloseReceipt = api.closeReceipt (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from closeReceipt() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docCloseReceipt));
		}			
		return YFSXMLUtil.getXMLString (docOutXML);
	}
	
	protected	void loadReleaseLines (Document docRelease) throws Exception
	{									
		YFCElement	eleRelease = YFCDocument.getDocumentFor (docRelease).getDocumentElement();
		YFCElement	eleReleaseLines = eleRelease.getChildElement ("ReleaseLines");
		
		// if at least one order line
		for (Iterator<?> iReleases = eleReleaseLines.getChildren(); iReleases.hasNext(); )
		{
			// get the first/next order line from output XML
			YFCElement eleReleaseLine = (YFCElement)iReleases.next();
			
			// create order line saving relevant details
			YantraReleaseLine	yfsReleaseLine = (YantraReleaseLine)createNewReleaseLine();
			yfsReleaseLine.setReleaseLineKey (eleReleaseLine.getAttribute ("ReleaseLineKey"));
			yfsReleaseLine.setLineNo (eleReleaseLine.getAttribute ("ReleaseLineNo"));
			yfsReleaseLine.setSubLineNo (eleReleaseLine.getAttribute ("ReleaseSubLineNo"));
			yfsReleaseLine.setItemID (eleReleaseLine.getAttribute ("ItemID"));
			yfsReleaseLine.setProductClass (eleReleaseLine.getAttribute ("ProductClass"));
			yfsReleaseLine.setUOM(eleReleaseLine.getAttribute ("UnitOfMeasure"));
			yfsReleaseLine.setQty(eleReleaseLine.getAttribute ("Quantity"));
			yfsReleaseLine.setReceiveQty(eleReleaseLine.getAttribute ("Quantity"));
			yfsReleaseLine.setDescription (eleReleaseLine.getAttribute ("ItemDesc"));
			// if release associated to order in Yantra
			if (eleReleaseLine.getAttribute ("OrderLineKey") != null)
				yfsReleaseLine.setOrderLineKey (eleReleaseLine.getAttribute ("OrderLineKey"));
	
			// warning....We can store one lot number and/or one serial number for each release line.
			// support for multiple serial numbers would need changes to this bean
			if (eleReleaseLine.getChildElement ("ReleaseTagSerials") != null)
			{
				YFCElement	eleReleaseTagSerials = eleReleaseLine.getChildElement ("ReleaseTagSerials");
				if (eleReleaseTagSerials != null)
				{
					for (Iterator<?> iReleaseTagSerials = eleReleaseTagSerials.getChildren (); iReleaseTagSerials.hasNext(); )
					{
						YFCElement	eleReleaseTagSerial = (YFCElement)iReleaseTagSerials.next();
						if (eleReleaseTagSerial.getAttribute ("LotNumber") != null)
							yfsReleaseLine.setLotNumber (eleReleaseTagSerial.getAttribute ("LotNumber"));
						if (eleReleaseTagSerial.getAttribute ("SerialNo") != null)
							yfsReleaseLine.setSerialNo (eleReleaseTagSerial.getAttribute ("SerialNo"));
						
						// assume only one lot or serial number per release line
						break;
					}
				}
			}
			addReleaseLine (yfsReleaseLine);
		}
	}

	// protected member variables
	protected	String	m_sDocumentType;	
	protected	String	m_sEnterpriseCode;
	protected	String	m_sOrderHeaderKey;
	protected	String	m_sReleaseKey;
	protected	String	m_sReleaseNo;
	protected	String	m_sReleaseDate;
	protected	String	m_sDeliveryDate;
	protected	String	m_sNumOfPallets;	
	protected	String	m_sNumOfCartons;	
	protected	String	m_sShipNode;	
	protected	String	m_sReceiveNode;	
	protected	String	m_sStatus;	
	protected	String	m_sStatusDesc;	
	
	protected Vector<Object>	m_vecReleaseLines;
}

