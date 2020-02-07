/**
  * Vendor.java
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
public class POSVendor implements Serializable
{
    public POSVendor()
    {
		m_sVendorKey = "";
		m_sOrganizationCode = "";
		m_sSellerOrganizationCode = "";
		m_sCompany = "";
		m_sContactFirstName = "";
		m_sContactLastName = "";
		m_sStreetAddress = "";
		m_sAddress2 = "";
		m_sCity = "";
		m_sState = "";
		m_sZip = "";
		m_sEmail = "";
		m_sPhone = "";
		
    }
/*	
	public	void getVendorDetails () throws Exception
	{
		getVendorDetails (getOrganizationCode(), getVendorID());
	}

	public	void getVendorDetails (String sOrganizationCode, String sVendorID) throws Exception
	{
		loadVendorDetails (sOrganizationCode, sVendorID, null);
	}
	
	public	void getSellerDetails (String sOrganizationCode, String sSellerOrganizationCode) throws Exception
	{
		loadVendorDetails (sOrganizationCode, null, sSellerOrganizationCode);
	}
	
	protected	void	loadVendorDetails (String sOrganizationCode, String sVendorID, String sSellerOrganizationCode) throws Exception
	{
		YFSEnvironment env = YFSUtil.getYFSEnv();

		// set up search criteria (by Status)		
		Hashtable htVendor = new Hashtable();
		htVendor.put("OrganizationCode", sOrganizationCode);
		if (sVendorID != null && sVendorID.length() > 0)
			htVendor.put("VendorID", sVendorID);
		else if (sSellerOrganizationCode != null && sSellerOrganizationCode.length() > 0)
			htVendor.put("SellerOrganizationCode", sSellerOrganizationCode);
		
		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		YFSXMLParser inXml = new YFSXMLParser();
		inXml.createRootElement("Vendor", htVendor);
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getVendorDetails()");
			System.out.println (YFSXMLUtil.getXMLString(inXml.getDocument()));
		}

		// call get item details API for package item
		Document docVendorDetails = api.getVendorDetails (env, inXml.getDocument());

		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getVendorDetails()");
			System.out.println (YFSXMLUtil.getXMLString(docVendorDetails));
		}

		String	sVendorDetails = YFSXMLUtil.getXMLString (docVendorDetails);
		YCPVendorDetailsOutputDoc oVendorDetails = new YCPVendorDetailsOutputDoc (sVendorDetails);
		YCPVendorDetailsOutputDoc.SellerOrganization oSeller = oVendorDetails.getVendor().getSellerOrganization();
		YCPVendorDetailsOutputDoc.CorporatePersonInfo oCorpInfo = oVendorDetails.getVendor().getSellerOrganization ().getCorporatePersonInfo ();

		// store key (physical and logical keys) to org
		setVendorKey (oVendorDetails.getVendor().getAttribute("VendorKey"));
		setOrganizationCode (oVendorDetails.getVendor().getAttribute ("OrganizationCode"));
		setSellerOrganizationCode (oSeller.getAttribute ("OrganizationCode"));

		// get provider's company details
		if (oSeller.getAttribute ("OrganizationName").length() == 0)		
			setCompany (oCorpInfo.getAttribute ("Company"));
		else
			setCompany (oSeller.getAttribute ("OrganizationName"));
			
		setContactFirstName (oCorpInfo.getAttribute ("FirstName"));
		setContactLastName (oCorpInfo.getAttribute ("LastName"));
		setStreetAddress (oCorpInfo.getAttribute ("AddressLine1"));
		setCity (oCorpInfo.getAttribute ("City"));
		setState (oCorpInfo.getAttribute ("State"));
		setZip (oCorpInfo.getAttribute ("ZipCode"));
		setCountry (oCorpInfo.getAttribute ("Country"));
		setEmail (oCorpInfo.getAttribute ("Email"));
		setPhone (oCorpInfo.getAttribute ("DayPhone"));
	}
*/

	// Service provider details
	public		String	getVendorKey () { return m_sVendorKey; }
	public		void	setVendorKey (String sVendorKey) { m_sVendorKey = sVendorKey; }
	public		String	getVendorID () { return m_sVendorID; }
	public		void	setVendorID (String sVendorID) { m_sVendorID = sVendorID; }
	public		String	getOrganizationCode () { return m_sOrganizationCode; }
	public		void	setOrganizationCode (String sOrganizationCode) { m_sOrganizationCode = sOrganizationCode; }
	public		String	getSellerOrganizationCode () { return m_sSellerOrganizationCode; }
	public		void	setSellerOrganizationCode (String sSellerOrganizationCode) { m_sSellerOrganizationCode = sSellerOrganizationCode; }
	public		String	getCompany () { return m_sCompany; }
	public		void	setCompany (String sVendorCompany) { m_sCompany = sVendorCompany; }
	public		String	getContactFirstName () { return m_sContactFirstName; }
	public		void	setContactFirstName (String sVendorContactFirstName) { m_sContactFirstName = sVendorContactFirstName; }
	public		String	getContactLastName () { return m_sContactLastName; }
	public		void	setContactLastName (String sVendorContactLastName) { m_sContactLastName = sVendorContactLastName; }
	public		String	getStreetAddress () { return m_sStreetAddress; }
	public		void	setStreetAddress (String sStreetAddress) { m_sStreetAddress = sStreetAddress; }
	public		String	getAddress2 () { return m_sAddress2; }
	public		void	setAddress2 (String sAddress2) { m_sAddress2 = sAddress2; }
	public		String	getCity () { return m_sCity; }
	public		void	setCity (String sVendorCity) { m_sCity = sVendorCity; }
	public		String	getState () { return m_sState; }
	public		void	setState (String sVendorState) { m_sState = sVendorState; }
	public		String	getZip () { return m_sZip; }
	public		void	setZip (String sVendorZip) { m_sZip = sVendorZip; }
	public		String	getEmail () { return m_sEmail; }
	public		void	setEmail (String sVendorEmail) { m_sEmail = sVendorEmail; }
	public		String	getPhone () { return m_sPhone; }
	public		void	setPhone (String sVendorPhone) { m_sPhone = sVendorPhone; }
	public		String	getCountry () { return m_sCountry; }
	public		void	setCountry (String sVendorCountry) { m_sCountry = sVendorCountry; }
	
	// provider information (ship parts to provider, service to ship-to)
	protected	String	m_sVendorKey;
	protected	String	m_sVendorID;
	protected	String	m_sOrganizationCode;
	protected	String	m_sSellerOrganizationCode;
	protected	String	m_sCompany;
	protected	String	m_sContactFirstName;	
	protected	String	m_sContactLastName;
	protected	String	m_sStreetAddress;
	protected	String	m_sAddress2;
	protected	String	m_sCity;
	protected	String	m_sState;
	protected	String	m_sZip;
	protected	String	m_sCountry;
	protected	String	m_sEmail;	
	protected	String	m_sPhone;	
}

