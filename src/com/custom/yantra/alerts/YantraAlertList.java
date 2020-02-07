/**
  * YantraAlertList.java
  *
  **/

// PACKAGE
package com.custom.yantra.alerts;

import	java.util.*;
import	java.util.TreeMap;
import	org.w3c.dom.*;

import java.io.Serializable;
import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;

//import com.custom.yantra.xmlwrapper.*;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCDate;

@SuppressWarnings({ "deprecation", "serial" })
public class YantraAlertList implements Serializable
{
    public YantraAlertList()
    {
		m_vecInboxList = new Vector<YantraAlert>();
		m_iSortBy = ALERT_SORTBYCREATEDATE;
		m_iSortOrder = ALERT_SORTDESCENDING;
		m_tmSortedInboxList = null;
    }
	
	public	static	final	int	ALERTSUMMARY_EXCEPTIONTYPE	 = 0;
	public	static	final	int	ALERTSUMMARY_GENERATEDON	 = 1;
	public	static	final	int	ALERTSUMMARY_CLOSEDON		 = 2;
	public	static	final	int	ALERTSUMMARY_ASSIGNEDUSER	 = 3;
	public	static	final	int	ALERTSUMMARY_ACTIVE			 = 4;
	
	public static	final	int ALERT_SORTBYRESOLVEDATE		= 0;
	public static	final	int ALERT_SORTBYCREATEDATE		= 1;
	
	public static	final	int ALERT_SORTASCENDING			= 0;
	public static	final	int ALERT_SORTDESCENDING		= 1;
	
	public void addAlert (YantraAlert oAlert)
	{
		m_vecInboxList.addElement (oAlert);
		m_tmSortedInboxList.put (oAlert.getAlertKey(), oAlert);
	}

	public	YantraAlert getAlert (int iLine)
	{
		return (YantraAlert)m_vecInboxList.elementAt (iLine);
	}
		
	public YantraAlert getAlert (String sKey)
	{
		return (YantraAlert)m_tmSortedInboxList.get (sKey);
	}
		
	public	int	getAlertCount ()
	{
		return m_vecInboxList.size();
	}

	public	Vector<YantraAlert>	getAlerts () { return m_vecInboxList; }
	public	TreeMap<String, YantraAlert> getSortedAlerts () { return m_tmSortedInboxList; }	
	
	public	void Reset ()
	{
		m_vecInboxList.clear ();
		if (m_tmSortedInboxList != null)
			m_tmSortedInboxList.clear();
		m_tmSortedInboxList = null;
		m_sInboxList = null;
	}

	public	void	setSortBy (int iSortBy)
	{
		m_iSortBy = iSortBy;
	}
	
	public int	getSortBy ()
	{
		return m_iSortBy;
	}

	public	void	setSortOrder (int iSortOrder)
	{
		m_iSortOrder = iSortOrder;
	}
	
	public int	getSortOrder ()
	{
		return m_iSortOrder;
	}

	
	public	String getAlertList () throws Exception
	{
		return getAlertList (false);
	}

	public	String getAlertList (boolean bActiveAlerts) throws Exception
	{
		Hashtable<String, String> htInbox = new Hashtable<String, String>();
		if (bActiveAlerts)
			htInbox.put ("ActiveFlag", "Y");
		return loadAlerts (htInbox);
	}
	
	public	String	getAlertList (String sAssignedToUser, boolean bActiveOnly) throws Exception
	{
		// set up search criteria (by Status)		
		Hashtable<String, String> htInbox = new Hashtable<String, String>();
		if (sAssignedToUser != null)
		{
			htInbox.put ("AssignedToUserId", sAssignedToUser);
			if (bActiveOnly)
				htInbox.put ("ActiveFlag", "Y");
		}

		return loadAlerts (htInbox);
	}
	
	private String	loadAlerts (Hashtable<String, String> htInbox) throws Exception
	{
		// create XML input document with search criteria
		// Note We customized the XML output template to include Inbox 
		// information
		if (getSortOrder() == ALERT_SORTBYCREATEDATE)
			m_tmSortedInboxList = new TreeMap<String, YantraAlert> (new AlertCreatedOnDateComparator(this));
		else
			m_tmSortedInboxList = new TreeMap<String, YantraAlert> (new AlertResolveByDateComparator(this));
		
		YFSXMLParser inXml = new YFSXMLParser();
		Element	eInbox = inXml.createRootElement("Inbox", htInbox);
		if (htInbox.containsKey("AssignedToUserId"))
		{
			Hashtable<String, String>	htUser = new Hashtable<String, String>();
			htUser.put ("Loginid", htInbox.get("AssignedToUserId"));
			inXml.createChild (eInbox, "User", htUser);
		}
		YFSEnvironment env = YFSUtil.getYFSEnv();
		YIFApi api = YFSUtil.getYIFApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to getExceptionList() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (inXml.getDocument()));
		}
		Document docOut = api.getExceptionList (env, inXml.getDocument());
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output From to getExceptionList() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (docOut));
		}
		YFCDocument	docInboxList = YFCDocument.getDocumentFor (docOut);
		YFCElement	eleInboxList = docInboxList.getDocumentElement ();
		

		// now parse through the XML output document and load YantraInbox
		m_sInboxList = YFSXMLUtil.getXMLString (docOut);
		for (Iterator<?> iInboxList = eleInboxList.getChildren(); iInboxList.hasNext(); )
		{
			YFCElement	eleInbox = (YFCElement)iInboxList.next();
						
			// create order line saving relevant details
			YantraAlert	oAlert = createNewAlert();
			oAlert.setAlertKey (eleInbox.getAttribute ("InboxKey"));
			oAlert.setActiveFlag (eleInbox.getAttribute ("ActiveFlag"));
			oAlert.setAssignedToUserId (eleInbox.getAttribute("AssignedToUserId"));
			oAlert.setAssignedToUserKey  (eleInbox.getAttribute ("AssignedToUserKey"));
			oAlert.setClosedOn (eleInbox.getAttribute ("ClosedOn"));
			oAlert.setResolveBy (eleInbox.getAttribute ("ResolveBy"));
			oAlert.setGeneratedOn (eleInbox.getAttribute ("GeneratedOn"));
			oAlert.setDescription(eleInbox.getAttribute ("Description"));
			oAlert.setDetailedDescription(eleInbox.getAttribute ("DetailDescription"));
			oAlert.setExceptionType (eleInbox.getAttribute ("ExceptionType"));
			oAlert.setItemKey (eleInbox.getAttribute ("ItemId"));
			oAlert.setOrderHeaderKey (eleInbox.getAttribute ("OrderHeaderKey"));
			oAlert.setShipmentKey (eleInbox.getAttribute ("ShipmentKey"));	
			oAlert.setStatus (eleInbox.getAttribute ("Status"));		
			addAlert (oAlert);				
		}
		return m_sInboxList;
	}

	private YantraAlert	getAlertForComparator (String sKey)
	{
		for (int i = 0; i < m_vecInboxList.size(); i++)
		{
			YantraAlert	oAlert = (YantraAlert)m_vecInboxList.elementAt (i);
			if (oAlert.getAlertKey().equals (sKey))
				return oAlert;
		}
		return null;
	}


	public	AlertSummary	getAlertSummaryByExceptionType ()
	{
		return getAlertSummary	(ALERTSUMMARY_EXCEPTIONTYPE);
	}
	
	public	AlertSummary	getAlertSummaryUserId ()
	{
		return getAlertSummary	(ALERTSUMMARY_ASSIGNEDUSER);
	}

	public	AlertSummary	getAlertSummaryClosedOn ()
	{
		return getAlertSummary	(ALERTSUMMARY_CLOSEDON);
	}
	
	public	AlertSummary	getAlertSummaryActive ()
	{
		return getAlertSummary	(ALERTSUMMARY_ACTIVE);
	}

	public	AlertSummary	getAlertGeneratedOn ()
	{
		return getAlertSummary	(ALERTSUMMARY_GENERATEDON);
	}
	
	
	public AlertSummary	getAlertSummary (int iSummaryType)
	{
		int	iSummary;
		AlertSummary	oSummary = new AlertSummary ();
		
		
		for (iSummary = 0; iSummary < getAlertCount (); iSummary++)
		{
			Integer	oCount;
			String	sKey = null;
			
			switch (iSummaryType)
			{
				case ALERTSUMMARY_GENERATEDON:
					sKey = getAlert(iSummary).getGeneratedOn();
					break;
				case ALERTSUMMARY_CLOSEDON:
					sKey = getAlert(iSummary).getClosedOn();
					break;
				case ALERTSUMMARY_EXCEPTIONTYPE:
					sKey = getAlert(iSummary).getExceptionType();
					break;
				case ALERTSUMMARY_ASSIGNEDUSER:
					sKey = getAlert(iSummary).getAssignedToUserId();
					break;
				case ALERTSUMMARY_ACTIVE:
					sKey = getAlert(iSummary).getActiveFlag();
					break;
				default:
					sKey = "";
					break;
			}
			if (sKey.length() > 0)
			{
				if (oSummary.containsKey (sKey))
					oCount = new Integer (Integer.parseInt ((String)oSummary.remove (sKey))+1);
				else
					oCount = new Integer (1);
				oSummary.put (sKey, oCount.toString());
			}
		}	
		return oSummary;
	}	
	
	protected	YantraAlert	createNewAlert ()
	{
		YantraAlert	oYantraAlert = new YantraAlert();
		
		return (YantraAlert)oYantraAlert;	
	}
	
	
	public	class	AlertSummary extends Hashtable<Object, Object>
	{
		public	AlertSummary () 
		{	
			super ();
			m_enumKeys = null;
			m_colVals = null;
			m_iVal = 0;
		}			
		
		
		public	boolean	hasMoreKeys()
		{
			if (m_enumKeys == null)
			{
				m_enumKeys = this.keys();
				m_colVals = this.values().toArray();
				m_iVal 	  = 0;
			}
			return m_enumKeys.hasMoreElements();
		}
		
		public	String	nextKey ()
		{
			return (String)m_enumKeys.nextElement();
		}
		
		public	String	nextValue ()
		{
			return (String)m_colVals[m_iVal++];
		}
		
		private	Enumeration<?>	m_enumKeys;
		private Object [] 	m_colVals;
		private	int			m_iVal;
	}

	// protected member variables
	protected	Vector<YantraAlert>		m_vecInboxList;
	protected	TreeMap<String, YantraAlert>		m_tmSortedInboxList;
	protected	int			m_iSortBy;
	protected	int			m_iSortOrder;

	// cached inbox list from getExceptionList API				
	protected	String		m_sInboxList; 

		
	protected class AlertCreatedOnDateComparator implements Comparator<Object>
	{
    	public AlertCreatedOnDateComparator(YantraAlertList oAlerts)
	    {
			m_oAlerts = oAlerts;
    	}
	
		
		public	int	compare (Object o1, Object o2)	
		{
			YantraAlert oAlert1 = m_oAlerts.getAlertForComparator ((String)o1);
			YantraAlert oAlert2 = m_oAlerts.getAlertForComparator ((String)o2);
			YFCDate		oDate1 = new YFCDate(oAlert1.getGeneratedOn(), YFCDate.ISO_DATETIME_FORMAT, false);
			YFCDate		oDate2 = new YFCDate(oAlert2.getGeneratedOn(), YFCDate.ISO_DATETIME_FORMAT, false);
			
			if (m_oAlerts.getSortOrder() == YantraAlertList.ALERT_SORTASCENDING)
				return oDate1.compareTo (oDate2);
			else
				return oDate2.compareTo (oDate1);
		}

		public	boolean	equals (Object o)
		{
			return this.equals (o);
		}		
		YantraAlertList m_oAlerts;
	}
	
	protected class AlertResolveByDateComparator implements Comparator<Object>
	{
    	public AlertResolveByDateComparator(YantraAlertList oAlerts)
	    {
			m_oAlerts = oAlerts;
    	}
	
		
		public	int	compare (Object o1, Object o2)	
		{
			YantraAlert oAlert1 = m_oAlerts.getAlertForComparator ((String)o1);
			YantraAlert oAlert2 = m_oAlerts.getAlertForComparator ((String)o2);
			YFCDate		oDate1 = new YFCDate(oAlert1.getResolveBy(), YFCDate.ISO_DATETIME_FORMAT, false);
			YFCDate		oDate2 = new YFCDate(oAlert2.getResolveBy(), YFCDate.ISO_DATETIME_FORMAT, false);
			if (m_oAlerts.getSortOrder() == YantraAlertList.ALERT_SORTASCENDING)
				return oDate1.compareTo (oDate2);
			else
				return oDate2.compareTo (oDate1);
		}

		public	boolean	equals (Object o)
		{
			return this.equals (o);
		}		
		YantraAlertList	m_oAlerts;
	}
}

