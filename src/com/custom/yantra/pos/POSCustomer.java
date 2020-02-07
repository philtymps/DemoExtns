/**
  * Customer.java
  *
  **/

// PACKAGE
package com.custom.yantra.pos;

import  java.io.Serializable;

/*
import	com.yantra.yfs.japi.YFSEnvironment;
import  com.yantra.interop.japi.YIFApi;
*/

@SuppressWarnings("serial")
public class POSCustomer implements Serializable
{
    public POSCustomer()
    {
		m_sCustomerKey = "";
		m_sCustomerID = "";
		m_sOrganizationCode = "";
		m_sBuyerOrganizationCode = "";

		m_sCCName = "";
		m_sCCNameOnCard = "";
		m_sCCNumber = "";
		m_sCCExpirationMonth = "";
		m_sCCExpirationYear = "";
		m_sCCStreetAddress = "";
		m_sCCZip = "";
		m_sCCSecurityCode = "";

		m_sBTPrefix = "";
		m_sBTFirstName = "";
		m_sBTLastName = "";
		m_sBTStreetAddress = "";
		m_sBTAddress2 = "";
		m_sBTCity = "";
		m_sBTState = "";
		m_sBTZip = "";
		m_sBTCountry = "";
		m_sBTEmail = "";
		m_sBTPhone = "";

		m_sSTPrefix = "";
		m_sSTFirstName = "";
		m_sSTLastName = "";
		m_sSTStreetAddress = "";
		m_sSTAddress2 = "";
		m_sSTCity = "";
		m_sSTState = "";
		m_sBTFirstName = "";
		m_sSTZip = "";
		m_sSTCountry = "";
		m_sSTEmail = "";
		m_sSTPhone = "";
	}

	// used for authentication purposes (customer login)
	public		String	getCustomerKey () { return m_sCustomerKey; }
	public		void	setCustomerKey (String sCustomerKey) { m_sCustomerKey = sCustomerKey; }
	public		String	getCustomerID () { return m_sCustomerID; }
	public		void	setCustomerID (String sCustomerID) { m_sCustomerID = sCustomerID; }
	public		String	getOrganizationCode () { return m_sOrganizationCode; }
	public		void	setOrganizationCode (String sOrganizationCode) { m_sOrganizationCode = sOrganizationCode; }
	public		void	setBuyerOrganizationCode (String sBuyerOrganizationCode) { m_sBuyerOrganizationCode = sBuyerOrganizationCode; }
	public		String	getBuyerOrganizationCode () { return m_sBuyerOrganizationCode; }
	public		String	getPassword () { return m_sPassword; }
	public		void	setPassword (String sPassword) { m_sPassword = sPassword; }
	public		String	getPasswordReminder () { return m_sPasswordReminder; }
	public		void	setPasswordReminder (String sPasswordReminder) { m_sPasswordReminder = sPasswordReminder; }

	// bill to
	public		String	getBTPrefix () { return m_sBTPrefix; }
	public		void	setBTPrefix (String sBTPrefix) { m_sBTPrefix = sBTPrefix; }
	public		String	getBTFirstName () { return m_sBTFirstName; }
	public		void	setBTFirstName (String sBTFirstName) { m_sBTFirstName = sBTFirstName; }
	public		String	getBTLastName () { return m_sBTLastName; }
	public		void	setBTLastName (String sBTLastName) { m_sBTLastName = sBTLastName; }
	public		String	getBTStreetAddress () { return m_sBTStreetAddress; }
	public		void	setBTStreetAddress (String sBTStreetAddress) { m_sBTStreetAddress = sBTStreetAddress; }
	public		String	getBTAddress2 () { return m_sBTAddress2; }
	public		void	setBTAddress2 (String sBTAddress2) { m_sBTAddress2 = sBTAddress2; }
	public		String	getBTCity () { return m_sBTCity; }
	public		void	setBTCity (String sBTCity) { m_sBTCity = sBTCity; }
	public		String	getBTState () { return m_sBTState; }
	public		void	setBTState (String sBTState) { m_sBTState = sBTState; }
	public		String	getBTZip () { return m_sBTZip; }
	public		void	setBTZip (String sBTZip) { m_sBTZip = sBTZip; }
	public		String	getBTCountry () { return m_sBTCountry; }
	public		void	setBTCountry (String sBTCountry) { m_sBTCountry = sBTCountry; }
	public		String	getBTEmail () { return m_sBTEmail; }
	public		void	setBTEmail (String sBTEmail) { m_sBTEmail = sBTEmail; }
	public		String	getBTPhone () { return m_sBTPhone; }
	public		void	setBTPhone (String sBTPhone) { m_sBTPhone = sBTPhone; }

	public		String	getBTFullAddress (boolean bHtml)
	{
		String	sBreak = bHtml ? "<BR>" : "\r\n";
		String	sFullAddress = getBTFirstName() + " " + getBTLastName()+"\r\n"+
							   getBTStreetAddress()+sBreak;
		if (getBTAddress2().length() > 0)
				sFullAddress = sFullAddress +
							   getBTAddress2()+sBreak;
		sFullAddress = sFullAddress +
					   getBTCity() + "," + getBTState() + " "+getBTZip() + sBreak+
					   getBTPhone();
		return sFullAddress;
	}

	public		String	getBTFullAddress ()
	{
		return (getBTFullAddress (false));
	}

	// ship to
	public		String	getSTPrefix () { return m_sSTPrefix; }
	public		void	setSTPrefix (String sSTPrefix) { m_sSTPrefix = sSTPrefix; }
	public		String	getSTFirstName () { return m_sSTFirstName; }
	public		void	setSTFirstName (String sSTFirstName) { m_sSTFirstName = sSTFirstName; }
	public		String	getSTLastName () { return m_sSTLastName; }
	public		void	setSTLastName (String sSTLastName) { m_sSTLastName = sSTLastName; }
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
		String	sFullAddress = getSTFirstName() + " " + getSTLastName()+"\r\n"+
							   getSTStreetAddress()+sBreak;
		if (getSTAddress2().length() > 0)
				sFullAddress = sFullAddress +
							   getSTAddress2()+sBreak;
		sFullAddress = sFullAddress +
					   getSTCity() + "," + getSTState() + " "+getSTZip() + sBreak+
					   getSTPhone();
		return sFullAddress;
	}

	public		String	getSTFullAddress ()
	{
		return getSTFullAddress (false);
	}

	// payment method and informaiton
	public		String	getCCName () { return m_sCCName; }
	public		void	setCCName (String sCCName) { m_sCCName = sCCName; }
	public		String	getCCNameOnCard () { return m_sCCNameOnCard; }
	public		void	setCCNameOnCard (String sCCNameOnCard) { m_sCCNameOnCard = sCCNameOnCard; }
	public		String	getCCStreetAddress () { return m_sCCStreetAddress; }
	public		void	setCCStreetAddress (String sCCStreetAddress) { m_sCCStreetAddress = sCCStreetAddress; }
	public		String	getCCZip () { return m_sCCZip; }
	public		void	setCCZip (String sCCZip) { m_sCCZip = sCCZip; }
	public		String	getCCNumber () { return m_sCCNumber; }
	public		void	setCCNumber (String sCCNumber) { m_sCCNumber = sCCNumber; }
	public		String	getCCExpirationMonth () { return m_sCCExpirationMonth; }
	public		void	setCCExpirationMonth (String sCCExpirationMonth) { m_sCCExpirationMonth = sCCExpirationMonth; }
	public		String	getCCExpirationYear () { return m_sCCExpirationYear; }
	public		void	setCCExpirationYear (String sCCExpirationYear) { m_sCCExpirationYear = sCCExpirationYear; }
	public		String	getCCSecurityCode () { return m_sCCSecurityCode; }
	public		void	setCCSecurityCode (String sSecurityCode) { m_sCCSecurityCode = sSecurityCode; }

/*
	public	void getCustomerDetails () throws Exception
	{
		getCustomerDetails (getOrganizationCode(), getCustomerID());
	}

	public	void getCustomerDetails (String sOrganizationCode, String sCustomerID) throws Exception
	{
		loadCustomerDetails (sOrganizationCode, sCustomerID, null);
	}

	public	void getBuyerDetails (String sOrganizationCode, String sBuyerOrganizationCode) throws Exception
	{
		loadCustomerDetails (sOrganizationCode, null, sBuyerOrganizationCode);
	}

	protected void loadCustomerDetails (String sOrganizationCode, String sCustomerID, String sBuyerOrganizationCode) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();

		// load customer or buyer details
		Hashtable htCustomer = new Hashtable();
		htCustomer.put("OrganizationCode", sOrganizationCode);
		if (sCustomerID != null && sCustomerID.length() > 0)
			htCustomer.put("CustomerID", sCustomerID); // e-mail address
		else if (sBuyerOrganizationCode != null && sBuyerOrganizationCode.length() > 0)
			htCustomer.put("BuyerOrganizationCode", sBuyerOrganizationCode);

		// create XML input document with search criteria
		// Note We customized the XML output template to include Item
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Customer", htCustomer);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getCustomerDeatils()");
			System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
		}

		// call get item details API for package item
		Document docCustomerDetails = api.getCustomerDetails (env, inXml.getDocument());

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output to getCustomerDeatils()");
			System.out.println (YFSXMLUtil.getXMLString(docCustomerDetails));
		}

		// customer details
		String	sCustomerDetails = YFSXMLUtil.getXMLString (docCustomerDetails);
		YCPCustomerDetailsOutputDoc oCustomerDetails = new YCPCustomerDetailsOutputDoc (sCustomerDetails);
		YCPCustomerDetailsOutputDoc.Customer oCustomer = oCustomerDetails.getCustomer();
		YCPCustomerDetailsOutputDoc.BuyerOrganization  oBuyer = oCustomer.getBuyerOrganization ();
		YCPCustomerDetailsOutputDoc.BillingPersonInfo  oBillTo = oBuyer.getBillingPersonInfo();

		// use either contact person infor or corporate person info, which ever is populated
		// for the ship to information
		if (oBuyer.getContactPersonInfo() != null
		&&  oBuyer.getContactPersonInfo().getAttribute("LastName").length() > 0)
		{
			YCPCustomerDetailsOutputDoc.ContactPersonInfo  oShipTo = oBuyer.getContactPersonInfo();
			setSTPrefix ("Mr.");
			setSTFirstName (oShipTo.getAttribute ("FirstName"));
			setSTLastName (oShipTo.getAttribute ("LastName"));
			setSTStreetAddress (oShipTo.getAttribute ("AddressLine1"));
			setSTCity (oShipTo.getAttribute ("City"));
			setSTState (oShipTo.getAttribute ("State"));
			setSTZip (oShipTo.getAttribute ("ZipCode"));
			setSTCountry (oShipTo.getAttribute ("Country"));
			setSTEmail (oShipTo.getAttribute ("EMailID"));
			setSTPhone (oShipTo.getAttribute ("DayPhone"));
		}
		else
		{
			YCPCustomerDetailsOutputDoc.CorporatePersonInfo oShipTo = oBuyer.getCorporatePersonInfo();
			setSTPrefix ("Mr.");
			setSTFirstName (oShipTo.getAttribute ("FirstName"));
			setSTLastName (oShipTo.getAttribute ("LastName"));
			setSTStreetAddress (oShipTo.getAttribute ("AddressLine1"));
			setSTCity (oShipTo.getAttribute ("City"));
			setSTState (oShipTo.getAttribute ("State"));
			setSTZip (oShipTo.getAttribute ("ZipCode"));
			setSTCountry (oShipTo.getAttribute ("Country"));
			setSTEmail (oShipTo.getAttribute ("EMailID"));
			setSTPhone (oShipTo.getAttribute ("DayPhone"));
		}

		// store key (physical and logical keys) to org (customer)
		setCustomerKey (oCustomer.getAttribute("CustomerKey"));  // not used but just in case
		setOrganizationCode (oCustomer.getAttribute ("OrganizationCode"));
		setBuyerOrganizationCode (oBuyer.getAttribute ("OrganizationCode"));
		setCustomerID(sCustomerID);

		// bill to information
		setBTPrefix ("Mr.");
		setBTFirstName (oBillTo.getAttribute ("FirstName"));
		setBTLastName (oBillTo.getAttribute ("LastName"));
		setBTStreetAddress (oBillTo.getAttribute ("AddressLine1"));
		setBTCity (oBillTo.getAttribute ("City"));
		setBTState (oBillTo.getAttribute ("State"));
		setBTZip (oBillTo.getAttribute ("ZipCode"));
		setBTCountry (oBillTo.getAttribute ("Country"));
		setBTEmail (oBillTo.getAttribute ("EMailID"));
		setBTPhone (oBillTo.getAttribute ("DayPhone"));

		// default credit cart information based on Bill-To
		setCCNameOnCard (getBTFirstName()+" "+getBTLastName());
		setCCStreetAddress (getBTStreetAddress());
		setCCZip (getBTZip());
	}
*/

	// authentication information
	protected	String	m_sCustomerKey;
	protected	String	m_sCustomerID;
	protected	String	m_sOrganizationCode;
	protected	String	m_sBuyerOrganizationCode;
	protected	String	m_sPassword;
	protected	String	m_sPasswordReminder;

	// credit card information
	protected	String	m_sCCName;
	protected	String	m_sCCNameOnCard;
	protected	String	m_sCCNumber;
	protected	String	m_sCCStreetAddress;
	protected	String	m_sCCZip;
	protected	String	m_sCCExpirationMonth;
	protected	String	m_sCCExpirationYear;
	protected	String	m_sCCSecurityCode;

	// bill to information
	protected	String	m_sBTPrefix;
	protected	String	m_sBTFirstName;
	protected	String	m_sBTLastName;
	protected	String	m_sBTStreetAddress;
	protected	String	m_sBTAddress2;
	protected	String	m_sBTCity;
	protected	String	m_sBTState;
	protected	String	m_sBTZip;
	protected	String	m_sBTCountry;
	protected	String	m_sBTEmail;
	protected	String	m_sBTPhone;

	// ship to information
	protected	String	m_sSTPrefix;
	protected	String	m_sSTFirstName;
	protected	String	m_sSTLastName;
	protected	String	m_sSTStreetAddress;
	protected	String	m_sSTAddress2;
	protected	String	m_sSTCity;
	protected	String	m_sSTState;
	protected	String	m_sSTZip;
	protected	String	m_sSTCountry;
	protected	String	m_sSTEmail;
	protected	String	m_sSTPhone;
}

