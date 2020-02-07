/**
  * Vendor.java
  *
  **/

// PACKAGE
package com.custom.yantra.vendor;

import	com.custom.yantra.util.*;

import	com.yantra.yfc.dom.*;
import	com.yantra.yfc.core.YFCObject;
import	com.yantra.yfs.japi.YFSEnvironment;
import  com.yantra.interop.japi.YIFApi;

import  java.io.Serializable;

@SuppressWarnings("serial")
public class Vendor implements Serializable
{
    public Vendor()
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
		YFCDocument		docVendor = YFCDocument.getDocumentFor ("Vendor");
		YFCElement		eleVendor = docVendor.getDocumentElement();
		YIFApi api = YFSUtil.getYIFApi();
		
		// set up search criteria (by Status)		
		eleVendor.setAttribute ("OrganizationCode", sOrganizationCode);
		if (sVendorID != null && sVendorID.length() > 0)
			eleVendor.setAttribute ("VendorID", sVendorID);
		else if (sSellerOrganizationCode != null && sSellerOrganizationCode.length() > 0)
			eleVendor.setAttribute ("SellerOrganizationCode", sSellerOrganizationCode);
		
		// create XML input document with search criteria
		// Note We customized the XML output template to include Item 
		// information
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getVendorDetails()");
			System.out.println (docVendor.getString());
		}

		// call get item details API for package item
		docVendor = YFCDocument.getDocumentFor (api.getVendorDetails (env, docVendor.getDocument ()));
		eleVendor = docVendor.getDocumentElement ();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from getVendorDetails()");
			System.out.println (docVendor.getString());
		}

		YFCElement	eleSellerOrganization = eleVendor.getChildElement ("SellerOrganization");
		YFCElement	eleCorporatePersonInfo = eleVendor.getChildElement ("CorporatePersonInfo");
		

		// store key (physical and logical keys) to org
		setVendorKey (eleVendor.getAttribute("VendorKey"));
		setOrganizationCode (eleVendor.getAttribute ("OrganizationCode"));
		setSellerOrganizationCode (eleSellerOrganization.getAttribute ("OrganizationCode"));

		// get provider's company details
		if (YFCObject.isVoid (eleSellerOrganization.getAttribute ("OrganizationName")))		
			setCompany (eleCorporatePersonInfo.getAttribute ("Company"));
		else
			setCompany (eleSellerOrganization.getAttribute ("OrganizationName"));
			
		setContactFirstName (eleCorporatePersonInfo.getAttribute ("FirstName"));
		setContactLastName (eleCorporatePersonInfo.getAttribute ("LastName"));
		setStreetAddress (eleCorporatePersonInfo.getAttribute ("AddressLine1"));
		setCity (eleCorporatePersonInfo.getAttribute ("City"));
		setState (eleCorporatePersonInfo.getAttribute ("State"));
		setZip (eleCorporatePersonInfo.getAttribute ("ZipCode"));
		setCountry (eleCorporatePersonInfo.getAttribute ("Country"));
		setEmail (eleCorporatePersonInfo.getAttribute ("Email"));
		setPhone (eleCorporatePersonInfo.getAttribute ("DayPhone"));
	}

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

