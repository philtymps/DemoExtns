/**
  * YantraShipment.java
  *
  **/

// PACKAGE
package com.custom.yantra.shipments;

import	java.util.*;
import	org.w3c.dom.*;

import com.custom.yantra.util.*;
import com.yantra.yfc.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import java.io.Serializable;
import com.yantra.yfc.dom.*;

@SuppressWarnings("serial")
public class YantraShipment implements Serializable
{
    public YantraShipment()
    {
		m_vecShipmentLines = new Vector<Object> ();
		m_sEnterpriseCode = "DEFAULT";
		m_sOrderHeaderKey = "";
		m_sShipmentKey = "";
		m_sShipmentNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sShipmentDate = "";
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
		m_sShipmentKey = "";
		m_sShipmentNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sShipmentDate = "";
		m_sDeliveryDate = "";
		m_sStatus = "";	
		m_sStatusDesc = "";
		m_sNumOfPallets="0";
		m_sNumOfCartons="0";
		for (int iEle = 0; iEle < getShipmentLineCount(); iEle++)		
			getShipmentLine(iEle).Reset();
		m_vecShipmentLines.clear();
	}

	public void addShipmentLine (Object oLine)
	{
		m_vecShipmentLines.addElement (oLine);
	}
	
	public String	getEnterpriseCode () { return m_sEnterpriseCode; }
	public void		setEnterpriseCode (String sEnterpriseCode) { m_sEnterpriseCode = sEnterpriseCode; }
	public String	getDocumentType () { return m_sDocumentType; }
	public void		setDocumentType (String sDocumentType) { m_sDocumentType = sDocumentType; }
	public	String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public	void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public	String	getShipmentKey () { return m_sShipmentKey; }
	public	void	setShipmentKey (String sShipmentKey) { m_sShipmentKey = sShipmentKey; }
	public	String	getShipmentNo () { return m_sShipmentNo; }
	public	void	setShipmentNo (String sShipmentNo) { m_sShipmentNo = sShipmentNo; }
	public	String	getShipmentDate () { if (m_sShipmentDate != null) return m_sShipmentDate; else return new String(); }
	public	void	setShipmentDate (String sShipmentDate) { if (sShipmentDate !=null ) m_sShipmentDate = sShipmentDate; }
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

	public	Object	createNewShipmentLine () {	return (Object)new YantraShipmentLine(this); }

	
	public	YantraShipmentLine getShipmentLine (int iLine)
	{
		return (YantraShipmentLine)m_vecShipmentLines.elementAt (iLine);
	}

	public	Vector<Object>	getShipmentLines () { return m_vecShipmentLines; }
	public	int		getShipmentLineCount () { return m_vecShipmentLines.size(); }
		
	public String getShipmentDetails () throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable<String, String> htShipment = new Hashtable<String, String>();
		htShipment.put ("ShipmentKey", getShipmentKey());

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Shipment", htShipment);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for getShipmentDetails() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		Document docShipment = api.getShipmentDetails (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from getShipmentDetails() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docShipment));
		}
		YFCElement eleShipment = YFCDocument.getDocumentFor (docShipment).getDocumentElement();
		setEnterpriseCode (eleShipment.getAttribute ("EnterpriseCode"));
		setOrderHeaderKey (eleShipment.getAttribute ("OrderHeaderKey"));
		setDocumentType (eleShipment.getAttribute ("DocumentType"));		
		setShipmentNo (eleShipment.getAttribute ("ShipmentNo"));
		setShipmentKey (eleShipment.getAttribute ("ShipmentKey"));
		setStatus (eleShipment.getAttribute ("Status"));
		setShipNode (eleShipment.getAttribute ("ShipNode"));
		setReceiveNode (eleShipment.getAttribute ("ReceivingNode"));
		setNumOfPallets (eleShipment.getAttribute ("NumOfPallets"));
		setNumOfCartons (eleShipment.getAttribute ("NumOfCartons"));
		
		if (eleShipment.getAttribute ("ActualDeliveryDate") != null)
			setDeliveryDate (eleShipment.getAttribute ("ActualDeliveryDate"));		
		else	
			setDeliveryDate (eleShipment.getAttribute ("ExpectedDeliveryDate"));		
		if (eleShipment.getAttribute ("ActualShipmentDate") != null)
			setShipmentDate (eleShipment.getAttribute ("ActualShipmentDate"));		
		else	
			setShipmentDate (eleShipment.getAttribute ("ExpectedShipmentDate"));
		YFCElement eleStatus = eleShipment.getChildElement ("Status");
		if (eleStatus != null)
			setStatusDescription (eleStatus.getAttribute ("Description"));
		
		// now load the order line list
		loadShipmentLines (docShipment);
		return YFSXMLUtil.getXMLString (docShipment);
	}

	public String confirmShipment() throws Exception
	{
		return confirmShipment ("MODIFY");
	}

	public String changeShipment () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSXMLParser	inXml = new YFSXMLParser();	
		Document		docOutXML = null;

		Hashtable<String, String>	htShipment = new Hashtable<String, String>();
		htShipment.put("ShipmentKey", getShipmentKey());
		Element eleShipment = inXml.createRootElement ("Shipment", htShipment);
		Hashtable<String, String>	htShipmentLines = new Hashtable<String, String>();
		htShipmentLines.put ("Replace", "N");
		Element eleShipmentLines = inXml.createChild (eleShipment, "ShipmentLines", htShipmentLines);
		// iterate over shipment lines lines
		for (int iEle = 0; iEle < getShipmentLines ().size(); iEle++)
		{
			YantraShipmentLine oShipmentLine = getShipmentLine(iEle);
			
			// generate XML for confirm shipment API
			Hashtable<String, String>	htShipmentLine = new Hashtable<String, String>();
			htShipmentLine.put("ShipmentLineKey", oShipmentLine.getShipmentLineKey());
			Element eleShipmentLine = inXml.createChild (eleShipmentLines, "ShipmentLine", htShipmentLine);	

			// if lot number/serial number not empty
			if (oShipmentLine.getLotNumber().length () > 0 || oShipmentLine.getSerialNo().length() > 0)
			{
				Hashtable<String, String>	htShipmentTagSerials = new Hashtable<String, String> ();
				htShipmentTagSerials.put ("Replace", "Y");
		
				Element eleShipmentTagSerials = inXml.createChild (eleShipmentLine, "ShipmentTagSerials", htShipmentTagSerials);
				Hashtable<String, String>	htShipmentTagSerial = new Hashtable<String, String> ();
				
				if (oShipmentLine.getLotNumber().length () > 0)
				{
					htShipmentTagSerial.put ("LotNumber", oShipmentLine.getLotNumber());
					htShipmentTagSerial.put ("Quantity", oShipmentLine.getQty ());
				}

				if (oShipmentLine.getSerialNo ().length() > 0)
				{
					htShipmentTagSerial.put ("SerialNo", oShipmentLine.getSerialNo ());
					htShipmentTagSerial.put ("Quantity", "1");
				}
				inXml.createChild (eleShipmentTagSerials, "ShipmentTagSerial", htShipmentTagSerial);
			}
		}
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for changeShipment() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.changeShipment (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from changeShipment() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}
		return YFSXMLUtil.getXMLString (docOutXML);
	
	}	

	@SuppressWarnings({ "deprecation", "unused" })
	public String confirmShipment (String sAction) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSXMLParser	inXml = new YFSXMLParser();	
		Document		docOutXML = null;
					
		Hashtable<String, String>	htShipment = new Hashtable<String, String>();
		htShipment.put("Action", sAction);
		htShipment.put("ShipmentKey", getShipmentKey());
		htShipment.put("ActualDeliveryDate", new YFCDate().getString (YFCDate.ISO_DATETIME_FORMAT));
		Element eleShipment = inXml.createRootElement ("Shipment", htShipment);
/*		
		// iterate over shipment lines lines
		for (int iEle = 0; iEle < getShipmentLines ().size(); iEle++)
		{
			// generate XML for confirm shipment API
			Hashtable	htShipmentLine = new Hashtable();
			htShipmentLine.put("ShipmentLineKey", getShipmentLine(iEle).getShipmentLineKey());
			htShipmentLine.put("ActualDeliveryDate", new YFCDate().getString (YFCDate.ISO_DATETIME_FORMAT));
			Element eleShipment = inXml.createChild (eleShipment, "ShipmentLine", htShipmentLine);	
		}
*/
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for confirmShipment() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		docOutXML = api.confirmShipment (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from confirmShipment() API is ...");
			System.out.println( YFSXMLUtil.getXMLString (docOutXML));
		}
		return YFSXMLUtil.getXMLString (docOutXML);
	}

	public String receiveShipment () throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSXMLParser	inXml = new YFSXMLParser();	
		Document		docOutXML = null;
		
		Hashtable<String, String>	htReceipt = new Hashtable<String, String>();
		htReceipt.put ("DocumentType", getDocumentType());
		htReceipt.put ("ShipmentKey", getShipmentKey());
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

		// a receipt is now open, add receipt lines based on shipment lines (one for one)
		inXml = new YFSXMLParser ();
		htReceipt = new Hashtable<String, String>();
		htReceipt.put ("DocumentType", getDocumentType());
		htReceipt.put ("ReceiptHeaderKey", sReceiptHeaderKey);
		htReceipt.put ("ReceivingNode", getReceiveNode());
		Element eleReceipt = inXml.createRootElement("Receipt", htReceipt);

		// now iterate over shipment lines and add to receipt lines in same quantity
		Element eleReceiptLines = inXml.createChild (eleReceipt, "ReceiptLines", null);
		for (int iShipmentLine = 0; iShipmentLine < getShipmentLineCount(); iShipmentLine++)
		{
			YantraShipmentLine oShipmentLine = getShipmentLine (iShipmentLine);
			Hashtable<String, String>	htReceiptLine = new Hashtable<String, String>();
			htReceiptLine.put ("ShipmentLineKey", oShipmentLine.getShipmentLineKey());
			htReceiptLine.put ("Quantity", oShipmentLine.getReceiveQty());
			
			// add serial & lot numbers
			if (oShipmentLine.getLotNumberToReceive().length () > 0)
			{
				htReceiptLine.put ("LotNumber", oShipmentLine.getLotNumberToReceive());
			}
			if (oShipmentLine.getSerialNoToReceive ().length() > 0)
			{
				htReceiptLine.put ("SerialNo", oShipmentLine.getSerialNoToReceive ());				
			}
			inXml.createChild (eleReceiptLines, "ReceiptLine", htReceiptLine);
		}		
		
		if (YFSUtil.getDebug())
		{
			// debug message
			System.out.println( "Input for receiveOrder() API is ...");
			System.out.println( YFSXMLUtil.getXMLString(inXml.getDocument()));
		}
		// receive the shipment
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
	
	protected	void loadShipmentLines (Document docShipment) throws Exception
	{									
		YFCElement	eleShipment = YFCDocument.getDocumentFor (docShipment).getDocumentElement();
		YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
		
		// if at least one order line
		for (Iterator<?> iShipments = eleShipmentLines.getChildren(); iShipments.hasNext(); )
		{
			// get the first/next order line from output XML
			YFCElement eleShipmentLine = (YFCElement)iShipments.next();
			
			// create order line saving relevant details
			YantraShipmentLine	yfsShipmentLine = (YantraShipmentLine)createNewShipmentLine();
			yfsShipmentLine.setShipmentLineKey (eleShipmentLine.getAttribute ("ShipmentLineKey"));
			yfsShipmentLine.setLineNo (eleShipmentLine.getAttribute ("ShipmentLineNo"));
			yfsShipmentLine.setSubLineNo (eleShipmentLine.getAttribute ("ShipmentSubLineNo"));
			yfsShipmentLine.setItemID (eleShipmentLine.getAttribute ("ItemID"));
			yfsShipmentLine.setProductClass (eleShipmentLine.getAttribute ("ProductClass"));
			yfsShipmentLine.setUOM(eleShipmentLine.getAttribute ("UnitOfMeasure"));
			yfsShipmentLine.setQty(eleShipmentLine.getAttribute ("Quantity"));
			yfsShipmentLine.setReceiveQty(eleShipmentLine.getAttribute ("Quantity"));
			yfsShipmentLine.setDescription (eleShipmentLine.getAttribute ("ItemDesc"));
			// if shipment associated to order in Yantra
			if (eleShipmentLine.getAttribute ("OrderLineKey") != null)
				yfsShipmentLine.setOrderLineKey (eleShipmentLine.getAttribute ("OrderLineKey"));
	
			// warning....We can store one lot number and/or one serial number for each shipment line.
			// support for multiple serial numbers would need changes to this bean
			if (eleShipmentLine.getChildElement ("ShipmentTagSerials") != null)
			{
				YFCElement	eleShipmentTagSerials = eleShipmentLine.getChildElement ("ShipmentTagSerials");
				if (eleShipmentTagSerials != null)
				{
					for (Iterator<?> iShipmentTagSerials = eleShipmentTagSerials.getChildren (); iShipmentTagSerials.hasNext(); )
					{
						YFCElement	eleShipmentTagSerial = (YFCElement)iShipmentTagSerials.next();
						if (eleShipmentTagSerial.getAttribute ("LotNumber") != null)
							yfsShipmentLine.setLotNumber (eleShipmentTagSerial.getAttribute ("LotNumber"));
						if (eleShipmentTagSerial.getAttribute ("SerialNo") != null)
							yfsShipmentLine.setSerialNo (eleShipmentTagSerial.getAttribute ("SerialNo"));
						
						// assume only one lot or serial number per shipment line
						break;
					}
				}
			}
			addShipmentLine (yfsShipmentLine);
		}
	}

	// protected member variables
	protected	String	m_sDocumentType;	
	protected	String	m_sEnterpriseCode;
	protected	String	m_sOrderHeaderKey;
	protected	String	m_sShipmentKey;
	protected	String	m_sShipmentNo;
	protected	String	m_sShipmentDate;
	protected	String	m_sDeliveryDate;
	protected	String	m_sNumOfPallets;	
	protected	String	m_sNumOfCartons;	
	protected	String	m_sShipNode;	
	protected	String	m_sReceiveNode;	
	protected	String	m_sStatus;	
	protected	String	m_sStatusDesc;	
	
	protected Vector<Object>	m_vecShipmentLines;
}

