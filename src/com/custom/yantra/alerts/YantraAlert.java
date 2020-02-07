/**
  * YantraAlert.java
  *
  **/

// PACKAGE
package com.custom.yantra.alerts;
import  java.io.*;

import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.*;

import com.custom.yantra.util.*;

@SuppressWarnings("serial")
public class YantraAlert implements Serializable
{
    public YantraAlert()
    {
		m_sAlertKey = "";
		m_sActiveFlag = "";
		m_sAssignedToUserKey = "";
		m_sAssignedToUserId = "";
		m_sClosedOn = "";
		m_sDescription = "";
		m_sDetailedDescription = "";
		m_sExceptionType = "";
		m_sGeneratedOn = "";
		m_sOrderHeaderKey = "";
		m_sItemKey = "";
		m_sShipmentKey = "";
		m_sResolveBy = "";		
		m_sStatus = "";
    }
	
    public void Reset()
    {
		m_sAlertKey = "";
		m_sActiveFlag = "";
		m_sAssignedToUserKey = "";
		m_sAssignedToUserId = "";
		m_sClosedOn = "";
		m_sDescription = "";
		m_sDetailedDescription = "";
		m_sExceptionType = "";
		m_sGeneratedOn = "";
		m_sOrderHeaderKey = "";
		m_sItemKey = "";
		m_sShipmentKey = "";
		m_sResolveBy = "";		
		m_sStatus = "";
    }
	
	public	String	getAlertKey () { return m_sAlertKey; }
	public	void	setAlertKey (String sAlertKey) { m_sAlertKey = sAlertKey; }
	public	String	getActiveFlag () { return m_sActiveFlag; }
	public	void	setActiveFlag (String sActiveFlag) { m_sActiveFlag = sActiveFlag; }
	public	String	getClosedOn () { return m_sClosedOn; }
	public	void	setClosedOn (String sClosedOn) { m_sClosedOn = sClosedOn; }
	public	String	getDescription () { return m_sDescription; }
	public	void	setDescription (String sDescription) { m_sDescription = sDescription; }
	public	String	getDetailedDescription () { return m_sDetailedDescription; }
	public	void	setDetailedDescription (String sDetailedDescription) { m_sDetailedDescription = sDetailedDescription; }
	public	void	setAssignedToUserId (String sAssignedToUserId) { m_sAssignedToUserId = sAssignedToUserId; }
	public	String	getAssignedToUserId () { return m_sAssignedToUserId; }
	public	void	setAssignedToUserKey (String sAssignedToUserKey) { m_sAssignedToUserKey = sAssignedToUserKey; }
	public	String	getAssignedToUserKey () { return m_sAssignedToUserKey; }
	public	String	getExceptionType () { return m_sExceptionType; }
	public	void	setExceptionType (String sExceptionType) { m_sExceptionType = sExceptionType; }
	public	String	getGeneratedOn ()	{ return m_sGeneratedOn; }
	public	void	setGeneratedOn (String sGeneratedOn) { m_sGeneratedOn = sGeneratedOn; }
	public	String	getResolveBy ()	{ return m_sResolveBy; }
	public	void	setResolveBy (String sResolveBy) { m_sResolveBy = sResolveBy; }
	public	String	getOrderHeaderKey () { return m_sOrderHeaderKey; }
	public	void	setOrderHeaderKey (String sOrderHeaderKey) { m_sOrderHeaderKey = sOrderHeaderKey; }
	public	String	getItemKey () { return m_sItemKey; }
	public	void	setItemKey (String sItemKey) { m_sItemKey = sItemKey; }
	public	String	getShipmentKey () { return m_sShipmentKey; }
	public	void	setShipmentKey (String sShipmentKey) { m_sShipmentKey = sShipmentKey; }
	public	String	getStatus () { return m_sStatus; }
	public	void	setStatus (String sStatus) { m_sStatus = sStatus; }


	public	String	resolveAlert () throws Exception
	{
		YFCDocument		docException = YFCDocument.createDocument ("ResolutionDetails");
		YFCElement		eleException = docException.getDocumentElement ();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSEnvironment	env = YFSUtil.getYFSEnv ();		
	
		// set up to call the resolveAlert API	
		eleException.setAttribute ("AutoResolveFlag", "N");
		eleException.setAttribute ("ResolvedBy", getAssignedToUserId());

		// add inbox key
		YFCElement	eleInbox = eleException.createChild ("Inbox");
		eleInbox.setAttribute ("InboxKey", getAlertKey());
		
		// call the resolveAlert API
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to resolveAlert API:");
			System.out.println (docException.getString());
		}
		docException = YFCDocument.getDocumentFor (api.resolveException (env, docException.getDocument()));
		if(YFSUtil.getDebug ())
		{
			System.out.println ("Output from resolveAlert API:");
			System.out.println (docException.getString());
		}
		return docException.getString();	
	}
	
	public	String getAlertDetails() throws Exception
	{
		YFCDocument		docInbox = YFCDocument.createDocument ("Inbox");
		YFCElement		eleInbox = docInbox.getDocumentElement ();
		YIFApi			api = YFSUtil.getYIFApi();
		YFSEnvironment	env = YFSUtil.getYFSEnv ();		

		eleInbox.setAttribute ("InboxKey", getAlertKey());
		// call the resolveAlert API
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to getExceptionDetails API:");
			System.out.println (docInbox.getString());
		}
		docInbox = YFCDocument.getDocumentFor (api.getExceptionDetails (env, docInbox.getDocument()));
		if(YFSUtil.getDebug ())
		{
			System.out.println ("Output from getExceptionDetails API:");
			System.out.println (docInbox.getString());
		}
		if (docInbox != null)
		{
			eleInbox = docInbox.getDocumentElement ();
			
			// create order line saving relevant details
			setAlertKey (eleInbox.getAttribute ("InboxKey"));
			setActiveFlag (eleInbox.getAttribute ("ActiveFlag"));
			setAssignedToUserId (eleInbox.getAttribute("AssignedToUserId"));
			setAssignedToUserKey  (eleInbox.getAttribute ("AssignedToUserKey"));
			setClosedOn (eleInbox.getAttribute ("ClosedOn"));
			setResolveBy (eleInbox.getAttribute ("ResolveBy"));
			setGeneratedOn (eleInbox.getAttribute ("GeneratedOn"));
			setDescription(eleInbox.getAttribute ("Description"));
			setDetailedDescription(eleInbox.getAttribute ("DetailDescription"));
			setExceptionType (eleInbox.getAttribute ("ExceptionType"));
			setItemKey (eleInbox.getAttribute ("ItemId"));
			setOrderHeaderKey (eleInbox.getAttribute ("OrderHeaderKey"));
			setShipmentKey (eleInbox.getAttribute ("ShipmentKey"));	
			setStatus (eleInbox.getAttribute ("Status"));		
		}
		return docInbox.getString();
	}
	
	// protected member variables
	protected	String	m_sAlertKey;
	protected	String	m_sActiveFlag;
	protected	String	m_sAssignedToUserId;
	protected	String	m_sClosedOn;
	protected	String	m_sResolveBy;
	protected	String	m_sDescription;
	protected	String	m_sExceptionType;
	protected	String	m_sGeneratedOn;
	protected	String	m_sDetailedDescription;
	protected	String	m_sAssignedToUserKey;	
	protected	String	m_sOrderHeaderKey;
	protected	String	m_sItemKey;
	protected	String	m_sShipmentKey;
	protected	String	m_sStatus;
}

