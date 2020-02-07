/**
  * YantraOrderLine.java
  *
  **/

// PACKAGE
package com.custom.yantra.orders;

import	com.yantra.yfc.core.YFCObject;
import java.io.Serializable;


@SuppressWarnings("serial")
public class YantraOrderLine implements Serializable
{
    public YantraOrderLine(Object oOrder)
    {
		m_oOrder = oOrder;
		m_sOrderLineKey = "";	
		m_sOrderReleaseKey = "";	
		m_sOrderHeaderKey = "";	
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sUOM = "";
		m_sProductClass = "";
		m_sItemShortDesc = "";
		m_sQty = "";
		m_sStatus = "";
		m_sStatusCode = "";
		m_bIsReturnable = true;
		m_bIsShipable = false;
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sLotNumber = "";
		m_sSerialNo = "";
		m_sDeliveryMethod = "";
		m_sAvailableQty = "";
		m_sUnAvailableQty = "";
		m_sAvailableDate = "";
		m_sShipDate = "";
		m_sDeliveryDate = "";
		m_sSegmentType = "";
		m_sSegment = "";
		m_sTransactionalLineId = "";
		m_sCarrierServiceCode = "";
		m_sFulfillmentType = "";

		m_sSTKey = "";
		m_sSTPrefix = "";
		m_sSTFirstName = "";
		m_sSTCompany = "";
		m_sSTLastName = "";
		m_sSTStreetAddress = "";
		m_sSTAddress2 = "";
		m_sSTCity = "";
		m_sSTState = "";
		m_sSTZip = "";
		m_sSTCountry = "";
		m_sSTEmail = "";
		m_sSTPhone = "";
    }
	
	public	Object	getOrder() { return m_oOrder; }
	public	String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public	void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public	String	getOrderLineKey () { return m_sOrderLineKey; }
	public	void	setOrderLineKey (String sOrderLineKey) { m_sOrderLineKey = sOrderLineKey; }
	public	String	getOrderReleaseKey () { return m_sOrderReleaseKey; }
	public	void	setOrderReleaseKey (String sOrderReleaseKey) { m_sOrderReleaseKey = sOrderReleaseKey; }
	public	String	getItemID () { return m_sItemID; }
	public	void	setItemID (String sItemID) { m_sItemID = sItemID; }
	public	String	getItemGroupCode () { return m_sItemGroupCode; }
	public	void	setItemGroupCode (String sItemGroupCode) { m_sItemGroupCode = sItemGroupCode; }
	public	String	getQty () { return m_sQty; }
	public	void	setQty (String sQty) { m_sQty = sQty; }
	public	String	getUOM () { if (m_sUOM != null) return m_sUOM; else return new String(); }
	public	void	setUOM (String sUOM) { if (sUOM !=null ) m_sUOM = sUOM; }
	public	String	getProductClass () { return m_sProductClass; }
	public	void	setProductClass (String sProductClass) { m_sProductClass = sProductClass; }
	public	String	getItemShortDesc ()	{ return m_sItemShortDesc; }
	public	void	setItemShortDesc (String sItemShortDesc) { m_sItemShortDesc = sItemShortDesc; }
	public	String	getStatus () { return m_sStatus; }
	public	void	setStatus (String sStatus) { m_sStatus = sStatus; }
	public	String	getStatusCode () { return m_sStatusCode; }
	public	void	setStatusCode (String sStatusCode) { m_sStatusCode = sStatusCode; }
	public	void	setIsReturnable (boolean bIsReturnable) { m_bIsReturnable = bIsReturnable; }
	public	boolean	getIsReturnable () { return m_bIsReturnable; }
	public	void	setIsShipable (boolean bIsShipable) { m_bIsShipable = bIsShipable; }
	public	boolean	getIsShipable () { return m_bIsShipable; }
	public	void	setShipNode (String sShipNode) {m_sShipNode = sShipNode; }
	public	String	getShipNode() { return m_sShipNode; }
	public	void	setReceiveNode (String sReceiveNode) {m_sReceiveNode = sReceiveNode; }
	public	String	getReceiveNode() { return m_sReceiveNode; }
	public	void	setLotNumber (String sLotNumber) {m_sLotNumber = sLotNumber; }
	public	String	getLotNumber() { return m_sLotNumber; }
	public	void	setSerialNo (String sSerialNo) {m_sSerialNo = sSerialNo; }
	public	String	getSerialNo() { return m_sSerialNo; }
	public	String	getDeliveryMethod () { return m_sDeliveryMethod; }
	public	void	setDeliveryMethod (String sDeliveryMethod) { m_sDeliveryMethod = sDeliveryMethod; }
	public	String	getAvailableQty() { return m_sAvailableQty; }
	public	void	setAvailableQty(String sAvailableQty) { m_sAvailableQty = sAvailableQty; }
	public	String	getUnAvailableQty() { return m_sUnAvailableQty; }
	public	void	setUnAvailableQty(String sUnAvailableQty) { m_sUnAvailableQty = sUnAvailableQty; }
	public	String	getAvailableDate() { return m_sAvailableDate; }
	public	void	setAvailableDate (String sAvailableDate) { m_sAvailableDate = sAvailableDate; }
	public	void	setFulfillmentType (String sFulfillmentType) {m_sFulfillmentType= sFulfillmentType; }
	public	String	getFulfillmentType() { return m_sFulfillmentType; }

	public	String	getShipDate() { return m_sShipDate; }
	public	void	setShipDate (String sShipDate) { m_sShipDate = sShipDate; }
	public	String	getDeliveryDate() { return m_sDeliveryDate; }
	public	void	setDeliveryDate (String sDeliveryDate) { m_sDeliveryDate = sDeliveryDate; }

	public	void	setSegmentType (String sSegmentType) {m_sSegmentType = sSegmentType; }
	public	String	getSegmentType () { return m_sSegmentType; }
	public	void	setSegment (String sSegment) {m_sSegment = sSegment; }
	public	String	getSegment () { return m_sSegment; }
	public	String	getTransactionalLineId ()	{ return m_sTransactionalLineId; }
	public	void	setTransactionalLineId (String sTransactionalLineId) { m_sTransactionalLineId = sTransactionalLineId; }
	public String	getCarrierServiceCode () { return m_sCarrierServiceCode; }
	public void		setCarrierServiceCode (String sCarrierServiceCode) { m_sCarrierServiceCode = sCarrierServiceCode; }

	// ship to
	public		String	getSTKey () { return m_sSTKey; }
	public		void	setSTKey (String sSTKey) { m_sSTKey = sSTKey; }
	public		String	getSTPrefix () { return m_sSTPrefix; }
	public		void	setSTPrefix (String sSTPrefix) { m_sSTPrefix = sSTPrefix; }
	public		String	getSTFirstName () { return m_sSTFirstName; }
	public		void	setSTFirstName (String sSTFirstName) { m_sSTFirstName = sSTFirstName; }
	public		String	getSTLastName () { return m_sSTLastName; }
	public		void	setSTLastName (String sSTLastName) { m_sSTLastName = sSTLastName; }
	public		String	getSTCompany () { return m_sSTCompany; }
	public		void	setSTCompany (String sSTCompany) { m_sSTCompany = sSTCompany; }
	public		String	getSTStreetAddress () { return m_sSTStreetAddress; }
	public		void	setSTStreetAddress (String sSTStreetAddress) { m_sSTStreetAddress = sSTStreetAddress; }
	public		String	getSTAddress2 () { return m_sSTAddress2; }
	public		void	setSTAddress2 (String sSTAddress2) { m_sSTAddress2 = sSTAddress2; }
	public		String	getSTCity () { return m_sSTCity; }
	public		void	setSTCity (String sSTCity) { m_sSTCity = sSTCity; }
	public		String	getSTState () { return m_sSTState; }
	public		void	setSTState (String sSTState) { m_sSTState = sSTState; }
	public		String	getSTZip () { return m_sSTZip; }
	public		void	setSTZip (String sSTZip) { m_sSTZip = sSTZip; }
	public		String	getSTCountry () { return m_sSTCountry; }
	public		void	setSTCountry (String sSTCountry) { m_sSTCountry = sSTCountry; }
	public		String	getSTEmail () { return m_sSTEmail; }
	public		void	setSTEmail (String sSTEmail) { m_sSTEmail = sSTEmail; }
	public		String	getSTPhone () { return m_sSTPhone; }
	public		void	setSTPhone (String sSTPhone) { m_sSTPhone = sSTPhone; }

	public		String	getSTFullAddress (boolean bHtml)
	{
		String	sBreak = bHtml ? "<BR>" : "\r\n";

		String	sFullAddress = getSTFirstName() + " " + getSTLastName() + sBreak;

		// add company name if provided				
		if (!YFCObject.isVoid(getSTCompany()))
			sFullAddress = sFullAddress + getSTCompany() + sBreak;

		// add full sreet address
		sFullAddress = sFullAddress + getSTStreetAddress() + sBreak;

		if (!YFCObject.isVoid (getSTAddress2()))
				sFullAddress = sFullAddress + getSTAddress2() + sBreak;
				
		sFullAddress = sFullAddress +
					   getSTCity() + "," + getSTState() + " "+getSTZip() + sBreak+
					   getSTPhone();
		return sFullAddress;
	}

	public		String	getSTFullAddress ()
	{
		return getSTFullAddress (false);
	}


	
	public	void Reset()
	{
		m_oOrder = null;
		m_sOrderLineKey = "";	
		m_sOrderHeaderKey = "";	
		m_sOrderReleaseKey = "";	
		m_sItemID = "";
		m_sItemGroupCode = "";
		m_sUOM = "";
		m_sProductClass = "";
		m_sItemShortDesc = "";
		m_sQty = "";
		m_sStatus = "";
		m_bIsReturnable = true;
		m_bIsShipable = false;
		m_sShipNode = "";
		m_sReceiveNode = "";
		m_sLotNumber = "";
		m_sSerialNo = "";
		m_sDeliveryMethod = "";
		m_sAvailableQty = "";
		m_sUnAvailableQty = "";
		m_sAvailableDate = "";
		m_sShipDate = "";
		m_sDeliveryDate = "";
		m_sSegmentType = "";
		m_sSegment = "";
		m_sTransactionalLineId = "";
		m_sCarrierServiceCode = "";
		m_sFulfillmentType = "";

		m_sSTKey = "";
		m_sSTPrefix = "";
		m_sSTFirstName = "";
		m_sSTCompany = "";
		m_sSTLastName = "";
		m_sSTStreetAddress = "";
		m_sSTAddress2 = "";
		m_sSTCity = "";
		m_sSTState = "";
		m_sSTZip = "";
		m_sSTCountry = "";
		m_sSTEmail = "";
		m_sSTPhone = "";
	}
	
	// protected member variables
	protected	Object	m_oOrder;
	protected	String	m_sOrderLineKey;	
	protected	String	m_sOrderHeaderKey;	
	protected	String	m_sOrderReleaseKey;	
	protected	String	m_sItemID;
	protected	String	m_sItemGroupCode;
	protected	String	m_sUOM;
	protected	String	m_sProductClass;
	protected	String	m_sItemShortDesc;
	protected	String	m_sQty;
	protected	String	m_sStatus;
	protected	String	m_sStatusCode;
	protected	boolean	m_bIsReturnable;
	protected	boolean	m_bIsShipable;
	protected	String	m_sShipNode;
	protected	String	m_sReceiveNode;
	protected	String	m_sLotNumber;
	protected	String	m_sSerialNo;
	protected	String	m_sDeliveryMethod;
	protected	String	m_sAvailableDate;
	protected	String	m_sShipDate;
	protected	String	m_sDeliveryDate;
	protected	String	m_sAvailableQty;
	protected	String	m_sUnAvailableQty;
	protected	String	m_sSegmentType;
	protected	String	m_sSegment;
	protected	String	m_sTransactionalLineId;
	protected	String	m_sCarrierServiceCode;
	protected	String	m_sFulfillmentType;
	
	// ship to information
	protected	String	m_sSTKey;
	protected	String	m_sSTPrefix;
	protected	String	m_sSTFirstName;
	protected	String	m_sSTLastName;
	protected	String	m_sSTCompany;
	protected	String	m_sSTStreetAddress;
	protected	String	m_sSTAddress2;
	protected	String	m_sSTCity;
	protected	String	m_sSTState;
	protected	String	m_sSTZip;
	protected	String	m_sSTCountry;
	protected	String	m_sSTEmail;
	protected	String	m_sSTPhone;

}

