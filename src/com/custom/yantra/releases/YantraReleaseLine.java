/**
  * YantraReleaseLine.java
  *
  **/

// PACKAGE
package com.custom.yantra.releases;
import  java.io.Serializable;

@SuppressWarnings("serial")
public class YantraReleaseLine implements Serializable
{
    public YantraReleaseLine(Object oRelease)
    {
		m_oRelease = oRelease;
		m_sReleaseLineKey = "";	
		m_sOrderLineKey = "";	
		m_sLineNo = "";
		m_sSubLineNo = "";
		m_sProductClass = "";
		m_sItemID = "";
		m_sUOM = "";
		m_sDescription = "";
		m_sQty = "";
		m_sReceiveQty = "";
		m_sLotNumber = "";
		m_sSerialNo = "";
		m_sLotNumberToReceive = "";
		m_sSerialNoToReceive = "";
    }
	
	public	Object	getRelease() { return m_oRelease; }
	public	String	getReleaseLineKey () { return m_sReleaseLineKey; }
	public	void	setReleaseLineKey (String sReleaseLineKey) { m_sReleaseLineKey = sReleaseLineKey; }
	public	String	getOrderLineKey () { return m_sOrderLineKey; }
	public	void	setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
	public	String	getLineNo () { return m_sLineNo; }
	public	void	setLineNo (String sLineNo) { m_sLineNo = sLineNo; }
	public	String	getDescription () { return m_sDescription; }
	public	void	setDescription (String sDescription) { m_sDescription = sDescription; }
	public	String	getSubLineNo () { return m_sSubLineNo; }
	public	void	setSubLineNo (String sSubLineNo) { m_sSubLineNo = sSubLineNo; }
	public	String	getItemID () { return m_sItemID; }
	public	void	setItemID (String sItemID) { m_sItemID = sItemID; }
	public	String	getProductClass () { return m_sProductClass; }
	public	void	setProductClass (String sProductClass) { m_sProductClass = sProductClass; }
	public	String	getUOM () { if (m_sUOM != null) return m_sUOM; else return new String(); }
	public	void	setUOM (String sUOM) { if (sUOM !=null ) m_sUOM = sUOM; }
	public	String	getQty ()	{ return m_sQty; }
	public	void	setQty (String sQty) { m_sQty = sQty; }
	public	String	getReceiveQty ()	{ return m_sReceiveQty; }
	public	void	setReceiveQty (String sQty) { m_sReceiveQty = sQty; }
	public	String	getLotNumber () { return m_sLotNumber; }
	public	void	setLotNumber (String sLotNumber) { m_sLotNumber = sLotNumber; }
	public	String	getSerialNo () { return m_sSerialNo; }
	public	void	setSerialNo (String sSerialNumber) { m_sSerialNo = sSerialNumber; }
	public	String	getLotNumberToReceive () { return m_sLotNumberToReceive; }
	public	void	setLotNumberToReceive (String sLotNumberToReceive) { m_sLotNumberToReceive = sLotNumberToReceive; }
	public	String	getSerialNoToReceive () { return m_sSerialNoToReceive; }
	public	void	setSerialNoToReceive (String sSerialNoToReceive) { m_sSerialNoToReceive = sSerialNoToReceive; }
	
	public	void	Reset ()
	{
		m_sReleaseLineKey = "";	
		m_sOrderLineKey = "";	
		m_sLineNo = "";
		m_sSubLineNo = "";
		m_sItemID = "";
		m_sProductClass = "";
		m_oRelease = null;
		m_sUOM = "";
		m_sDescription = "";
		m_sQty = "";
		m_sReceiveQty = "";
		m_sLotNumber = "";
		m_sSerialNo = "";
		m_sLotNumberToReceive = "";
		m_sSerialNoToReceive = "";
	}
	
	// protected member variables
	protected	Object	m_oRelease;
	protected	String	m_sReleaseLineKey;	
	protected	String	m_sOrderLineKey;	
	protected	String	m_sLineNo;
	protected	String	m_sSubLineNo;
	protected	String	m_sUOM;
	protected	String	m_sQty;
	protected	String	m_sReceiveQty;
	protected	String	m_sDescription;
	protected	String	m_sItemID;
	protected	String	m_sProductClass;
	protected	String	m_sLotNumber;
	protected	String	m_sSerialNo;
	protected	String	m_sLotNumberToReceive;
	protected	String	m_sSerialNoToReceive;
}

