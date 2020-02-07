/**
  * Customer.java
  *
  **/

// PACKAGE
package com.custom.yantra.customer;

import	java.util.*;
import  java.io.Serializable;
import	com.custom.yantra.util.*;
import	com.yantra.yfc.dom.*;
import	com.yantra.yfc.core.YFCObject;
import	com.yantra.yfs.japi.YFSEnvironment;
import  com.yantra.interop.japi.YIFApi;

@SuppressWarnings("serial")
public class Customer implements Serializable
{
    public Customer()
    {
		m_sCustomerKey = "";
		m_sCustomerID = "";
		m_sOrganizationCode = "";
		m_sBuyerOrganizationCode = "";
		m_sCCName = "";
		m_sCCNameOnCard = "";
		m_sCCFirstName = "";
		m_sCCLastName = "";
		m_sCCNumber = "";
		m_sCCExpirationMonth = "";
		m_sCCExpirationYear = "";
		m_sCCStreetAddress = "";
		m_sCCCity = "";
		m_sCCState = "";
		m_sCCZip = "";
		m_sCCSecurityCode = "";

		m_sBTKey = "";
		m_sBTPrefix = "";
		m_sBTFirstName = "";
		m_sBTCompany = "";
		m_sBTLastName = "";
		m_sBTStreetAddress = "";
		m_sBTAddress2 = "";
		m_sBTCity = "";
		m_sBTState = "";
		m_sBTZip = "";
		m_sBTCountry = "";
		m_sBTEmail = "";
		m_sBTPhone = "";

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
		m_vecContacts = new Vector<Object>();
		m_vecPayments = new Vector<Object>();
	}


	public	void Reset ()
	{
		m_sCustomerKey = "";
		m_sCustomerID = "";
		m_sOrganizationCode = "";
		m_sBuyerOrganizationCode = "";
		m_sCCName = "";
		m_sCCNameOnCard = "";
		m_sCCFirstName = "";
		m_sCCLastName = "";
		m_sCCNumber = "";
		m_sCCExpirationMonth = "";
		m_sCCExpirationYear = "";
		m_sCCStreetAddress = "";
		m_sCCCity = "";
		m_sCCState = "";
		m_sCCZip = "";
		m_sCCSecurityCode = "";

		m_sBTKey = "";
		m_sBTPrefix = "";
		m_sBTFirstName = "";
		m_sBTCompany = "";
		m_sBTLastName = "";
		m_sBTStreetAddress = "";
		m_sBTAddress2 = "";
		m_sBTCity = "";
		m_sBTState = "";
		m_sBTZip = "";
		m_sBTCountry = "";
		m_sBTEmail = "";
		m_sBTPhone = "";

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

		for (int iEle = 0; iEle < getCustomerContactCount(); iEle++)		
			getCustomerContact(iEle).Reset();
		m_vecContacts.clear();
		m_vecContacts = new Vector<Object>();

		for (int iEle = 0; iEle < getCustomerPaymentMethodCount(); iEle++)		
			getCustomerPaymentMethod(iEle).Reset();
		m_vecPayments.clear();
		m_vecPayments = new Vector<Object>();
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
	public		Vector<Object>	getContacts() { return m_vecContacts; }
	
	// bill to
	public		String	getBTKey () { return m_sBTKey; }
	public		void	setBTKey (String sBTKey) { m_sBTKey = sBTKey; }
	public		String	getBTPrefix () { return m_sBTPrefix; }
	public		void	setBTPrefix (String sBTPrefix) { m_sBTPrefix = sBTPrefix; }
	public		String	getBTFirstName () { return m_sBTFirstName; }
	public		void	setBTFirstName (String sBTFirstName) { m_sBTFirstName = sBTFirstName; }
	public		String	getBTLastName () { return m_sBTLastName; }
	public		void	setBTLastName (String sBTLastName) { m_sBTLastName = sBTLastName; }
	public		String	getBTCompany () { return m_sBTCompany; }
	public		void	setBTCompany (String sBTCompany) { m_sBTCompany = sBTCompany; }
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
		String	sFullAddress = getBTFirstName() + " " + getBTLastName()+sBreak;

		// add company name if present
		if (!YFCObject.isVoid (getBTCompany()))
			sFullAddress = sFullAddress + getBTCompany() + sBreak;

		// add full street address
		sFullAddress = sFullAddress + getBTStreetAddress() + sBreak;

		if (!YFCObject.isVoid(getBTAddress2()))
				sFullAddress = sFullAddress + getBTAddress2() + sBreak;
		
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

	// payment method and informaiton
	public		String	getCCName () { return m_sCCName; }
	public		void	setCCName (String sCCName) { m_sCCName = sCCName; }
	public		String	getCCNameOnCard () { return m_sCCNameOnCard; }
	public		void	setCCNameOnCard (String sCCNameOnCard) { m_sCCNameOnCard = sCCNameOnCard; }
	public		String	getCCFirstName() { return m_sCCFirstName; }
	public		void	setCCFirstName (String sCCFirstName) { m_sCCFirstName = sCCFirstName; }
	public		String	getCCLastName () { return m_sCCLastName; }
	public		void	setCCLastName	 (String sCCLastName) { m_sCCLastName = sCCLastName; }
	public		String	getCCStreetAddress () { return m_sCCStreetAddress; }
	public		void	setCCStreetAddress (String sCCStreetAddress) { m_sCCStreetAddress = sCCStreetAddress; }
	public		String	getCCCity () { return m_sCCCity; }
	public		void	setCCCity (String sCCCity) { m_sCCCity = sCCCity; }
	public		String	getCCState () { return m_sCCState; }
	public		void	setCCState (String sCCState) { m_sCCState = sCCState; }
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

	public	String getCustomerIdOfBuyerOrganization (String sOrganizationCode, String sBuyerOrganizationCode) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YFCDocument		docCustomer = YFCDocument.createDocument ("Customer");
		YFCElement		eleCustomer = docCustomer.getDocumentElement();
		YIFApi 			api = YFSUtil.getYIFApi();

		eleCustomer.setAttribute ("OrganizationCode", sOrganizationCode);	
		eleCustomer.setAttribute ("BuyerOrganizationCode", sBuyerOrganizationCode);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getCustomerList:");
			System.out.println (docCustomer.getString());
		}
		YFCDocument	docCustomerList = YFCDocument.getDocumentFor (api.getCustomerList(env, docCustomer.getDocument()));
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getCustomerList:");
			System.out.println (docCustomerList.getString());
		}		
		Iterator<?>	iCustomerList = docCustomerList.getDocumentElement().getChildren();
		while (iCustomerList.hasNext ())
		{
			eleCustomer = (YFCElement)iCustomerList.next();
			if (!YFCObject.isVoid (eleCustomer.getAttribute ("BuyerOrganizationCode")))	
			{
				if (sBuyerOrganizationCode.equals (eleCustomer.getAttribute ("BuyerOrganizationCode")))
				{
					setCustomerID(eleCustomer.getAttribute ("CustomerID"));
					return (eleCustomer.getAttribute ("CustomerID"));
				}
			}
		}
		return "";
	}	

	protected void loadCustomerDetails (String sOrganizationCode, String sCustomerID, String sBuyerOrganizationCode) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YFCDocument		docCustomer = YFCDocument.createDocument ("Customer");
		YFCElement		eleCustomer = docCustomer.getDocumentElement();
		YIFApi 			api = YFSUtil.getYIFApi();
				
		eleCustomer.setAttribute ("OrganizationCode", sOrganizationCode);
		if (sCustomerID != null && sCustomerID.length() > 0)
		{
			eleCustomer.setAttribute ("CustomerID", sCustomerID); // e-mail address
		}
		else if (sBuyerOrganizationCode != null && sBuyerOrganizationCode.length() > 0)
		{
			eleCustomer.setAttribute ("CustomerID", sBuyerOrganizationCode);
			eleCustomer.setAttribute ("BuyerOrganizationCode", sBuyerOrganizationCode);
		}
		// create XML input document with search criteria
		// Note We customized the XML output template to include Item
		// information
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getCustomerDeatils()");
			System.out.println (docCustomer.getString());
		}

		// call get item details API for package item
		docCustomer = YFCDocument.getDocumentFor (api.getCustomerDetails (env, docCustomer.getDocument ()));
		eleCustomer	= docCustomer.getDocumentElement();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output to getCustomerDeatils()");
			System.out.println (docCustomer.getString());
		}

		YFCElement	eleShipTo = findDefaultAddress (eleCustomer, "IsDefaultShipTo");
		YFCElement	eleBillTo = findDefaultAddress (eleCustomer, "IsDefaultBillTo");
		YFCElement	eleBuyer = eleCustomer.getChildElement ("BuyerOrganization");
		YFCElement	eleConsumer = eleCustomer.getChildElement ("Consumer");
		
		if (eleBillTo == null)
		{ 		
			if (eleConsumer != null && eleCustomer.getAttribute ("CustomerType").equals ("02"))
				eleBillTo = eleConsumer.getChildElement ("BillingPersonInfo");
			else if (eleBuyer != null && eleCustomer.getAttribute ("CustomerType").equals ("01"))
				eleBillTo = eleBuyer.getChildElement ("BillingPersonInfo");
		}
		if (eleBuyer != null)
			setBuyerOrganizationCode (eleBuyer.getAttribute ("OrganizationCode"));
	
		if (eleShipTo == null)
			eleShipTo = eleBillTo;
			
		if (eleShipTo != null)			
		{
			eleShipTo = eleShipTo.getChildElement ("PersonInfo");
			setSTKey (eleShipTo.getAttribute ("PersonInfoKey"));
			setSTStreetAddress (eleShipTo.getAttribute ("AddressLine1"));
			setSTAddress2 (eleShipTo.getAttribute ("AddressLine2"));
			setSTCity (eleShipTo.getAttribute ("City"));
			setSTState (eleShipTo.getAttribute ("State"));
			setSTZip (eleShipTo.getAttribute ("ZipCode"));
			setSTCountry (eleShipTo.getAttribute ("Country"));
			setSTCompany (eleShipTo.getAttribute ("Company"));
			setSTFirstName (eleShipTo.getAttribute ("FirstName"));
			setSTLastName (eleShipTo.getAttribute ("LastName"));
			setSTEmail (eleShipTo.getAttribute ("EMailID"));
			setSTPhone (eleShipTo.getAttribute ("DayPhone"));

			// default credit cart information based on Bill-To
			setDefaultCCDetails (eleShipTo);
		}

		// bill to information
		if (eleBillTo != null)
		{
			eleBillTo = eleBillTo.getChildElement ("PersonInfo");
			setBTKey(eleBillTo.getAttribute ("PersonInfoKey"));
			setBTStreetAddress (eleBillTo.getAttribute ("AddressLine1"));
			setBTAddress2 (eleBillTo.getAttribute ("AddressLine2"));
			setBTCity (eleBillTo.getAttribute ("City"));
			setBTState (eleBillTo.getAttribute ("State"));
			setBTZip (eleBillTo.getAttribute ("ZipCode"));
			setBTCountry (eleBillTo.getAttribute ("Country"));
			setBTCompany (eleBillTo.getAttribute ("Company"));
			setBTFirstName (eleBillTo.getAttribute ("FirstName"));
			setBTLastName (eleBillTo.getAttribute ("LastName"));
			setBTEmail (eleBillTo.getAttribute ("EMailID"));
			setBTPhone (eleBillTo.getAttribute ("DayPhone"));

			setDefaultCCDetails (eleBillTo);
		}

		// load customer contact list
		YFCElement	eleCustomerContactList = eleCustomer.getChildElement ("CustomerContactList");
		if (eleCustomerContactList != null)
		{
			Iterator<?>	iCustomerContacts = eleCustomerContactList.getChildren ();
			while (iCustomerContacts.hasNext ())
			{
				YFCElement		eleCustomerContact = (YFCElement)iCustomerContacts.next();
				CustomerContact	oCustomerContact = (CustomerContact)createNewCustomerContact ();
				
				oCustomerContact.setContactKey		(eleCustomerContact.getAttribute ("CustomerContactKey"));
				oCustomerContact.setContactFirstName(eleCustomerContact.getAttribute ("FirstName"));
				oCustomerContact.setContactLastName (eleCustomerContact.getAttribute ("LastName"));
				oCustomerContact.setContactPhone	(eleCustomerContact.getAttribute ("DayPhone"));
				oCustomerContact.setContactEmail    (eleCustomerContact.getAttribute ("EmailID"));
				oCustomerContact.setContactUserId	(eleCustomerContact.getAttribute ("UserID"));
				oCustomerContact.setContactId		(eleCustomerContact.getAttribute ("CustomerContactID"));

				// if we have not billto or shipto, we'll default billto/shipto to the first customer contact record we find				
				if (eleBillTo == null)
				{
					eleBillTo = eleCustomerContact;
					setSTCompany (eleCustomerContact.getAttribute ("Company"));
					setSTFirstName (eleCustomerContact.getAttribute ("FirstName"));
					setSTLastName (eleCustomerContact.getAttribute ("LastName"));
					setSTEmail (eleCustomerContact.getAttribute ("EMailID"));
					setSTPhone (eleCustomerContact.getAttribute ("DayPhone"));
					setBTCompany (eleCustomerContact.getAttribute ("Company"));
					setBTFirstName (eleCustomerContact.getAttribute ("FirstName"));
					setBTLastName (eleCustomerContact.getAttribute ("LastName"));
					setBTEmail (eleCustomerContact.getAttribute ("EMailID"));
					setBTPhone (eleCustomerContact.getAttribute ("DayPhone"));
					setDefaultCCDetails (eleBillTo, true);
				}
				addCustomerContact ((Object)oCustomerContact);
			}
		}

		YFCElement	elePaymentMethod = findDefaultPaymentMethod (eleCustomer, "IsDefaultMethod");
		if (elePaymentMethod != null)
		{
			CustomerPaymentMethod oCustomerPaymentMethod = (CustomerPaymentMethod)createNewCustomerPaymentMethod ();
			oCustomerPaymentMethod.setIsDefaultMethod(elePaymentMethod.getAttribute ("IsDefaultMethod"));
			oCustomerPaymentMethod.setAccountLimit(elePaymentMethod.getAttribute ("AccountLimit"));
	        oCustomerPaymentMethod.setAccountLimitCurrency(elePaymentMethod.getAttribute ("AccountLimitCurrency"));
	        oCustomerPaymentMethod.setAvailableAccountBalance(elePaymentMethod.getAttribute ("AvailableAccountBalance"));
	        oCustomerPaymentMethod.setCreditCardExpDate(elePaymentMethod.getAttribute ("CreditCardExpDate"));
	        oCustomerPaymentMethod.setCreditCardName(elePaymentMethod.getAttribute ("CreditCardName"));
			oCustomerPaymentMethod.setCreditCardType(elePaymentMethod.getAttribute ("CreditCardType"));
	        oCustomerPaymentMethod.setCustomerAccountNo(elePaymentMethod.getAttribute ("CustomerAccountNo"));
			oCustomerPaymentMethod.setDisplayCreditCardNo(elePaymentMethod.getAttribute ("DisplayCreditCardNo"));
	        oCustomerPaymentMethod.setDisplayCustomerAccountNo(elePaymentMethod.getAttribute ("DisplayCustomerAccountNo"));
			oCustomerPaymentMethod.setDisplayPaymentReference1(elePaymentMethod.getAttribute ("DisplayPaymentReference1"));
	        oCustomerPaymentMethod.setFirstName(elePaymentMethod.getAttribute ("FirstName"));
			oCustomerPaymentMethod.setLastName(elePaymentMethod.getAttribute ("LastName"));
			oCustomerPaymentMethod.setMiddleName(elePaymentMethod.getAttribute ("MiddleName"));
	        oCustomerPaymentMethod.setPaymentReference1(elePaymentMethod.getAttribute ("PaymentReference1"));
			oCustomerPaymentMethod.setPaymentReference2(elePaymentMethod.getAttribute ("PaymentReference2"));
	        oCustomerPaymentMethod.setPaymentReference3(elePaymentMethod.getAttribute ("PaymentReference3"));
			oCustomerPaymentMethod.setPaymentType(elePaymentMethod.getAttribute ("PaymentType"));
			addCustomerPaymentMethod ((Object)oCustomerPaymentMethod);
		}
		else
		{
			// for consumers create a default payment type as a credit card with just their name information on it
			if (eleCustomer.getAttribute ("CustomerType").equals ("02"))
			{
				CustomerPaymentMethod oCustomerPaymentMethod = (CustomerPaymentMethod)createNewCustomerPaymentMethod ();
				oCustomerPaymentMethod.setPaymentType ("CREDIT_CARD");
				oCustomerPaymentMethod.setFirstName (getCCFirstName());
				oCustomerPaymentMethod.setLastName (getCCLastName());
				oCustomerPaymentMethod.setIsDefaultMethod ("Y");
				addCustomerPaymentMethod ((Object)oCustomerPaymentMethod);
			}
		}		
		// store key (physical and logical keys) to org (customer)
		setCustomerKey (eleCustomer.getAttribute("CustomerKey"));  // not used but just in case
		setOrganizationCode (eleCustomer.getAttribute ("OrganizationCode"));
		setCustomerID((sCustomerID == null ? "" : sCustomerID));
	}

	protected	YFCElement	findDefaultAddress (YFCElement eleElement, String sDefaultAttribute)
	{
		return findDefaultElement (eleElement, "CustomerAdditionalAddressList", sDefaultAttribute);
	}
	
	protected	YFCElement	findDefaultPaymentMethod (YFCElement eleElement, String sDefaultAttribute)
	{
		return findDefaultElement (eleElement, "CustomerPaymentMethodList", sDefaultAttribute);
	}
	
	protected	YFCElement	findDefaultElement (YFCElement	eleListElement, String sListElement, String sDefaultAttribute)
	{
		YFCNodeList<?>	nlList = eleListElement.getElementsByTagName (sListElement);
		if (nlList != null)
		{
			Iterator<?>	iElementList = nlList.iterator ();
			while (iElementList.hasNext())
			{
				YFCNode	nodeList = (YFCNode)iElementList.next();
				Iterator<?>	iElement = nodeList.getChildren();
				while (iElement.hasNext ())
				{
					YFCElement	eleElement = (YFCElement)iElement.next();
					boolean bIsDefault = eleElement.getBooleanAttribute (sDefaultAttribute);
					if (bIsDefault)
					{
						return eleElement;
					}
				}
			}
		}
		return null;			
	}
	
	public	class CustomerContact {
		public	CustomerContact ()
		{
			m_sContactKey = "";
			m_sContactFirstName = "";
			m_sContactLastName = "";
			m_sContactCompany = "";
			m_sContactEmail = "";
			m_sContactPhone = "";
			m_sContactTitle = "";		
			m_sContactId = "";
			m_sContactUserId = "";
		}		
						
		public		String 	getContactKey () { return m_sContactKey; }
		public		void	setContactKey (String sContactKey) { m_sContactKey = sContactKey; }
		public		String	getContactFirstName () { return m_sContactFirstName; }
		public		void	setContactFirstName (String sContactFirstName) { m_sContactFirstName = sContactFirstName; }
		public		String	getContactLastName () { return m_sSTLastName; }
		public		void	setContactLastName (String sContactLastName) { m_sContactLastName = sContactLastName; }
		public		String	getContactEmail () { return m_sContactEmail; }
		public		void	setContactEmail (String sContactEmail) { m_sContactEmail = sContactEmail; }
		public		String	getContactPhone () { return m_sContactPhone; }
		public		void	setContactPhone (String sContactPhone) { m_sContactPhone = sContactPhone; }
		public		String	getContactTitle () { return m_sContactTitle; }
		public		void	setContactTitle (String sContactTitle) { m_sContactTitle = sContactTitle; }
		public		String 	getContactUserId() { return m_sContactUserId; }
		public		void	setContactUserId (String sContactUserId) { m_sContactUserId= sContactUserId; }
		public		String 	getContactId() { return m_sContactId; }
		public		void	setContactId (String sContactId) { m_sContactId = sContactId; }

		public		void	Reset ()
		{
			m_sContactKey = "";
			m_sContactFirstName = "";
			m_sContactLastName = "";
			m_sContactCompany = "";
			m_sContactEmail = "";
			m_sContactPhone = "";
			m_sContactTitle = "";		
			m_sContactId = "";
			m_sContactUserId = "";
		}				

		// contact information
		protected	String	m_sContactKey;
		protected	String	m_sContactFirstName;
		protected	String	m_sContactLastName;
		protected	String	m_sContactCompany;
		protected	String	m_sContactEmail;
		protected	String	m_sContactPhone;
		protected	String	m_sContactTitle;
		protected	String	m_sContactId;
		protected	String	m_sContactUserId;

	}


	public	class CustomerPaymentMethod {
		public	CustomerPaymentMethod()
		{
			m_sAccountLimit = "";
            m_sAccountLimitCurrency = "";
            m_sAvailableAccountBalance="";
			m_sCreditCardExpDate="";
            m_sCreditCardName="";
			m_sCreditCardType="";
			m_sCreditCardNo="";
			m_sDisplayCreditCardNo="";
            m_sCustomerAccountNo="";
            m_sDisplayCustomerAccountNo="";
			m_DisplayPaymentReference1="";
            m_sFirstName="";
			m_sIsDefaultMethod="";
			m_sLastName="";
			m_sMiddleName="";
            m_sPaymentReference1="" ;
			m_sPaymentReference2="";
            m_sPaymentReference3="";
			m_sPaymentType="";			
		}

		public	void Reset()
		{
			m_sAccountLimit = "";
            m_sAccountLimitCurrency = "";
            m_sAvailableAccountBalance="";
			m_sCreditCardExpDate="";
            m_sCreditCardName="";
			m_sCreditCardType="";
			m_sCreditCardNo="";
			m_sDisplayCreditCardNo="";
            m_sCustomerAccountNo="";
            m_sDisplayCustomerAccountNo="";
			m_DisplayPaymentReference1="";
            m_sFirstName="";
			m_sIsDefaultMethod="";
			m_sLastName="";
			m_sMiddleName="";
            m_sPaymentReference1="" ;
			m_sPaymentReference2="";
            m_sPaymentReference3="";
			m_sPaymentType="";			
		}

		public	String	getAccountLimit() { return m_sAccountLimit; }
		public	void	setAccountLimit(String sAccountLimit) { m_sAccountLimit = sAccountLimit; }
        public	String	getAccountLimitCurrency() { return m_sAccountLimitCurrency; }
        public	void	setAccountLimitCurrency(String sAccountLimitCurrency) { m_sAccountLimitCurrency = sAccountLimitCurrency; }
        public	String	getAvailableAccountBalance() { return m_sAvailableAccountBalance; }
        public	void	setAvailableAccountBalance(String sAvailableAccountBalance) { m_sAvailableAccountBalance = sAvailableAccountBalance; }
        public	String	getCreditCardExpDate() { return m_sCreditCardExpDate; }
        public	void	setCreditCardExpDate(String sCreditCardExpDate) { m_sCreditCardExpDate = sCreditCardExpDate; }
        public	String	getCreditCardName() { return m_sCreditCardName; }
        public	void	setCreditCardName(String sCreditCardName) { m_sCreditCardName = sCreditCardName; }
		public	String	getCreditCardType() { return m_sCreditCardType; }
		public	void	setCreditCardType(String sCreditCardType) { m_sCreditCardType = sCreditCardType; }
		public	String	getCreditCardNo() { return m_sCreditCardNo; }
		public	void	setCreditCardNo(String sCreditCardNo) { m_sCreditCardNo = sCreditCardNo; }
		public	String	getDisplayCreditCardNo() { return m_sDisplayCreditCardNo; }
		public	void	setDisplayCreditCardNo(String sDisplayCreditCardNo) { m_sDisplayCreditCardNo = sDisplayCreditCardNo; }
        public	String	getCustomerAccountNo() { return m_sCustomerAccountNo; }
        public	void	setCustomerAccountNo(String sCustomerAccountNo) { m_sCustomerAccountNo = sCustomerAccountNo; }
        public	String	getDisplayCustomerAccountNo() { return m_sDisplayCustomerAccountNo; }
        public	void	setDisplayCustomerAccountNo(String sDisplayCustomerAccountNo) { m_sDisplayCustomerAccountNo = sDisplayCustomerAccountNo; }
		public	String	getDisplayPaymentReference1() { return m_DisplayPaymentReference1; }
		public	void	setDisplayPaymentReference1(String DisplayPaymentReference1) { m_DisplayPaymentReference1 = DisplayPaymentReference1; }
        public	String	getFirstName() { return m_sFirstName; }
        public	void	setFirstName(String sFirstName) { m_sFirstName = sFirstName; }
		public	String	getIsDefaultMethod() { return m_sIsDefaultMethod; }
		public	void	setIsDefaultMethod(String sIsDefaultMethod) { m_sIsDefaultMethod = sIsDefaultMethod; }
		public	String	getLastName() { return m_sLastName; }
		public	void	setLastName(String sLastName) { m_sLastName = sLastName; }
		public	String	getMiddleName() { return m_sMiddleName; }
		public	void	setMiddleName(String sMiddleName) { m_sMiddleName = sMiddleName; }
        public	String	getPaymentReference1() { return m_sPaymentReference1; }
        public	void	setPaymentReference1(String sPaymentReference1) { m_sPaymentReference1 = sPaymentReference1; }
		public	String	getPaymentReference2() { return m_sPaymentReference2; }
		public	void	setPaymentReference2(String sPaymentReference2) { m_sPaymentReference2 = sPaymentReference2; }
        public	String	getPaymentReference3() { return m_sPaymentReference3; }
        public	void	setPaymentReference3(String sPaymentReference3) { m_sPaymentReference3 = sPaymentReference3; }
		public	String	getPaymentType() { return m_sPaymentType;			 }
		public	void	setPaymentType(String sPaymentType) { m_sPaymentType = sPaymentType; }

		protected	String m_sAccountLimit;
        protected	String m_sAccountLimitCurrency;
        protected	String m_sAvailableAccountBalance;
		protected	String m_sCreditCardExpDate;
        protected	String m_sCreditCardName;
		protected	String m_sCreditCardType;
		protected	String m_sCreditCardNo;
		protected	String m_sDisplayCreditCardNo;
        protected	String m_sCustomerAccountNo;
        protected	String m_sDisplayCustomerAccountNo;
		protected	String m_DisplayPaymentReference1;
        protected	String m_sFirstName;
		protected	String m_sIsDefaultMethod;
		protected	String m_sLastName;
		protected	String m_sMiddleName;
        protected	String m_sPaymentReference1;
		protected	String m_sPaymentReference2;
        protected	String m_sPaymentReference3;
		protected	String m_sPaymentType;			
	}
	
	public	Object			createNewCustomerContact () {	return (Object)new CustomerContact (); }
	public	Vector<Object>			getCustomerContacts () { return m_vecContacts; }
	public	CustomerContact getCustomerContact (int iContact) { return (CustomerContact)m_vecContacts.elementAt (iContact); }
	public	int				getCustomerContactCount () { return m_vecContacts.size(); }
	public	void 			addCustomerContact (Object oCustomerContact) { m_vecContacts.addElement ((Object)oCustomerContact); }


	public	Object			createNewCustomerPaymentMethod () {	return (Object)new CustomerPaymentMethod (); }
	public	Vector<Object>			getCustomerPaymentMethods () { return m_vecPayments; }
	public	CustomerPaymentMethod	getCustomerPaymentMethod (int iPayment) { return (CustomerPaymentMethod)m_vecPayments.elementAt (iPayment); }
	public	int				getCustomerPaymentMethodCount () { return m_vecPayments.size(); }
	public	void 			addCustomerPaymentMethod (Object oCustomerPaymentMethod) { m_vecPayments.addElement ((Object)oCustomerPaymentMethod); }
	
	protected	void setDefaultCCDetails (YFCElement elePersonInfo)
	{
		setDefaultCCDetails (elePersonInfo, false);
	}
	
	protected	void setDefaultCCDetails (YFCElement elePersonInfo, boolean bConactInfoOnly)
	{
		// default credit cart information based on Bill-To
		String	sFirstName = elePersonInfo.getAttribute ("FirstName");
		String	sLastName  = elePersonInfo.getAttribute ("LastName");
		String	sFullName  = sFirstName;
		if (sFullName == null || sFullName.length() == 0)
			sFullName = sLastName;
		else
			sFullName = sFullName + " " + sLastName;
		setCCFirstName (sFirstName);
		setCCLastName (sLastName);
		setCCNameOnCard (sFullName);
		
		if (!bConactInfoOnly)
		{
			setCCStreetAddress (elePersonInfo.getAttribute ("AddressLine1"));
			setCCCity  (elePersonInfo.getAttribute ("City"));
			setCCState (elePersonInfo.getAttribute ("State"));
			setCCZip (elePersonInfo.getAttribute("ZipCode"));	
		}
	}
	
	// authentication information
	protected	String	m_sCustomerKey;
	protected	String	m_sCustomerID;
	protected	String	m_sOrganizationCode;
	protected	String	m_sBuyerOrganizationCode;
	protected	String	m_sPassword;
	protected	String	m_sPasswordReminder;

	// credit card information
	protected	String	m_sCCName;
	protected	String	m_sCCFirstName;
	protected	String	m_sCCLastName;
	protected	String	m_sCCNameOnCard;
	protected	String	m_sCCNumber;
	protected	String	m_sCCStreetAddress;
	protected	String	m_sCCCity;
	protected	String	m_sCCState;
	protected	String	m_sCCZip;
	protected	String	m_sCCExpirationMonth;
	protected	String	m_sCCExpirationYear;
	protected	String	m_sCCSecurityCode;

	// bill to information
	protected	String	m_sBTKey;
	protected	String	m_sBTPrefix;
	protected	String	m_sBTFirstName;
	protected	String	m_sBTLastName;
	protected	String	m_sBTCompany;
	protected	String	m_sBTStreetAddress;
	protected	String	m_sBTAddress2;
	protected	String	m_sBTCity;
	protected	String	m_sBTState;
	protected	String	m_sBTZip;
	protected	String	m_sBTCountry;
	protected	String	m_sBTEmail;
	protected	String	m_sBTPhone;

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
	protected	Vector<Object>	m_vecContacts;
	protected	Vector<Object>	m_vecPayments;
}

