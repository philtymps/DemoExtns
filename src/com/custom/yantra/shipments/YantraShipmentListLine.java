/**
  * YantraShipmentListLine.java
  *
  **/

// PACKAGE
package com.custom.yantra.shipments;

import java.io.Serializable;

@SuppressWarnings("serial")
public class YantraShipmentListLine implements Serializable
{
    public YantraShipmentListLine()
    {	
		m_sShipmentHeaderKey = "";	
		m_sShipmentNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sShipmentDate = "";
		m_sDeliveryDate = "";
		m_sStatus = "";
		m_sStatusDesc = "";
    }
	
	public	String	getShipmentKey () { return m_sShipmentHeaderKey; }
	public	void	setShipmentKey (String sShipmentHeaderKey) { m_sShipmentHeaderKey = sShipmentHeaderKey; }
	public	String	getShipmentNo () { return m_sShipmentNo; }
	public	void	setShipmentNo (String sShipmentNo) { m_sShipmentNo = sShipmentNo; }
	public	String	getShipmentDate () { if (m_sShipmentDate != null) return m_sShipmentDate; else return new String(); }
	public	void	setShipmentDate (String sShipmentDate) { if (sShipmentDate !=null ) m_sShipmentDate = sShipmentDate; }
	public	String	getDeliveryDate ()	{ return m_sDeliveryDate; }
	public	void	setDeliveryDate (String sDeliveryDate) { m_sDeliveryDate = sDeliveryDate; }
	public	String	getStatus () { return m_sStatus; }
	public	void	setStatus (String sStatus) { m_sStatus = sStatus; }
	public	String	getStatusDescription () { return m_sStatusDesc; }
	public	void	setStatusDescription (String sStatusDesc) { m_sStatusDesc = sStatusDesc; }
	public	String	getDocumentType () { return m_sDocumentType; }
	public	void	setDocumentType (String sDocumentType) { m_sDocumentType = sDocumentType; }
	public	String	getShipNode () { return m_sShipNode; }
	public	void	setShipNode (String sShipNode) { m_sShipNode = sShipNode; }
	public	String	getReceiveNode () { return m_sReceiveNode; }
	public	void	setReceiveNode (String sReceiveNode) { m_sReceiveNode = sReceiveNode; }
	
	public	void	Reset ()
	{
		m_sShipmentHeaderKey = "";	
		m_sShipmentNo = "";
		m_sDocumentType = "";
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sShipmentDate = "";
		m_sDeliveryDate = "";
		m_sStatus = "";
		m_sStatusDesc = "";
	}
	// protected member variables
	protected	String	m_sShipmentHeaderKey;	
	protected	String	m_sShipmentNo;
	protected	String	m_sShipmentDate;
	protected	String	m_sDeliveryDate;
	protected	String	m_sStatus;	
	protected	String	m_sStatusDesc;	
	protected	String	m_sDocumentType;	
	protected	String	m_sShipNode;	
	protected	String	m_sReceiveNode;	
}

