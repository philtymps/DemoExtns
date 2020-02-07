/**
  * YantraOrderListLine.java
  *
  **/

// PACKAGE
package com.custom.yantra.orders;

import java.io.Serializable;

@SuppressWarnings("serial")
public class YantraOrderListLine implements Serializable
{
    public YantraOrderListLine()
    {	
		m_sOrderHeaderKey = "";	
		m_sOrderNo = "";
		m_sOrderDate = "";
		m_sCustomerPONum = "";
		m_sRequestedDate = "";
		m_sGrandTotal = "";
		m_sStatus = "";
		m_sSearchCriteria1 = "";
		m_sSearchCriteria2 = "";
		m_sCustomerEMailID = "";
		m_sCustomerFirstName = "";
		m_sCustomerLastName = "";
		m_sCustomerZipCode = "";				
		m_sCustomerPhoneNo = "";				
    }
	
	public	String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public	void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public	String	getOrderNo () { return m_sOrderNo; }
	public	void	setOrderNo (String sOrderNo) { m_sOrderNo = sOrderNo; }
	public	String	getGrandTotal () { return m_sGrandTotal; }
	public	void	setGrandTotal (String sGrandTotal) { m_sGrandTotal = sGrandTotal; }
	public	String	getOrderDate () { if (m_sOrderDate != null) return m_sOrderDate; else return new String(); }
	public	void	setOrderDate (String sOrderDate) { if (sOrderDate !=null ) m_sOrderDate = sOrderDate; }
	public	String	getCustomerPONo () { return m_sCustomerPONum; }
	public	void	setCustomerPONo (String sCustomerPONum) { m_sCustomerPONum = sCustomerPONum; }
	public	String	getCustomerEMailID () { return m_sCustomerEMailID; }
	public	void	setCustomerEMailID (String sCustomerEMailID) { m_sCustomerEMailID = sCustomerEMailID; }
	public	String	getCustomerFirstName () { return m_sCustomerFirstName; }
	public	void	setCustomerFirstName (String sCustomerFirstName) { m_sCustomerFirstName = sCustomerFirstName; }
	public	String	getCustomerLastName () { return m_sCustomerLastName; }
	public	void	setCustomerLastName (String sCustomerLastName) { m_sCustomerLastName = sCustomerLastName; }
	public	String	getCustomerZipCode() { return m_sCustomerZipCode; }
	public	void	setCustomerZipCode (String sCustomerZipCode) { m_sCustomerZipCode = sCustomerZipCode; }
	public	String	getCustomerPhoneNo() { return m_sCustomerPhoneNo; }
	public	void	setCustomerPhoneNo (String sCustomerPhoneNo) { m_sCustomerPhoneNo = sCustomerPhoneNo; }
	public	String	getSearchCriteria1 () { return m_sSearchCriteria1; }
	public	void	setSearchCriteria1 (String sValue) { m_sSearchCriteria1 = sValue.toUpperCase(); }
	public	String	getSearchCriteria2 () { return m_sSearchCriteria2; }
	public	void	setSearchCriteria2 (String sValue)	{ m_sSearchCriteria2 = sValue.toUpperCase(); }
	public	String	getRequestedDate ()	{ return m_sRequestedDate; }
	public	void	setRequestedDate (String sRequestedDate) { m_sRequestedDate = sRequestedDate; }
	public	String	getStatus () { return m_sStatus; }
	public	void	setStatus (String sStatus) { m_sStatus = sStatus; }
	

	public	void	Reset ()
	{
		m_sOrderHeaderKey = "";	
		m_sOrderNo = "";
		m_sOrderDate = "";
		m_sCustomerPONum = "";
		m_sRequestedDate = "";
		m_sGrandTotal = "";
		m_sStatus = "";
		m_sSearchCriteria1 = "";
		m_sSearchCriteria2 = "";
		m_sCustomerEMailID = "";
		m_sCustomerFirstName = "";
		m_sCustomerLastName = "";
		m_sCustomerZipCode = "";
		m_sCustomerPhoneNo = "";
	}
	// protected member variables
	protected	String	m_sOrderHeaderKey;	
	protected	String	m_sOrderNo;
	protected	String	m_sOrderDate;
	protected	String	m_sCustomerPONum;
	protected	String	m_sCustomerFirstName;
	protected	String	m_sCustomerLastName;
	protected	String	m_sCustomerZipCode;
	protected	String	m_sCustomerPhoneNo;
	protected	String	m_sCustomerEMailID;
	protected	String	m_sRequestedDate;
	protected	String	m_sGrandTotal;
	protected	String	m_sStatus;	
	protected	String	m_sSearchCriteria1;
	protected	String	m_sSearchCriteria2;
}
