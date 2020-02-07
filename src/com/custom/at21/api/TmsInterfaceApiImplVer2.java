/**
  * TmsInterfaceAPIImplVer2.java
  *
  **/

// PACKAGE
package com.custom.at21.api;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.core.YFCObject;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.interop.japi.YIFApi;
import com.custom.yantra.util.*;

import com.haht.xml.textdata.*;

import org.w3c.dom.*;
import java.util.*;
import javax.xml.xpath.*;
import java.sql.*;
import java.io.*;
import java.math.*;

@SuppressWarnings("deprecation")
public class TmsInterfaceApiImplVer2 implements YIFCustomApi 
{
    public TmsInterfaceApiImplVer2()
    {
		m_con = null;
		m_stmtHeader = null;
		m_rsHeader = null;
		m_rsDetail = null;
		YFSUtil.setDebug (false);
    }
	
	public static final String dtFmtTmsDateFormat = "MM/dd/yyyy";
	public static final String conDefaultURL = "jdbc:sqlserver://localhost:1433;databaseName=AT21;user=sa;password=sa";
	public static final String conDefaultClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String zipCodeLookupSQL = "SELECT * from YFS_ZIP_CODE_LOCATION WHERE ROUND(LATITUDE,1) = ROUND(%LATITUDE%,1) AND ROUND(LONGITUDE,0) = ROUND(%LONGITUDE%,0)";
	public static final String sNO_NSN = "NO-NSN";
	public static final String sNO_NSN_NAME = "No NSN_NAME Data Available from Order DB";
	
	public void setProperties(Properties prop) throws Exception
	{
        m_Properties = prop;
    }

	public		Document importAllOrders (YFSEnvironment env, Document docIn) throws Exception
	{
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));

		int iTotalOrderCount = 0;
		int iOrderCount = 0;						

		YFCDocument	docOutputOrders = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOutputOrders = docOutputOrders.getDocumentElement ();


		// it is recommended that you call this API asynchronously by posting the invoking message
		// on a JMS queue, and then having a service that picks up that message and processes it through an
		// integration server
		
		// start a loop importing Orders calling multi-api with a maximum number of Orders per invocation
		// if any failure results, this API can be called again to restart the import of all remaining Orders
		try {
			do {
				YFCDocument docOrders = YFCDocument.getDocumentFor (importOrders (env, docIn, true));
				YFCElement	eleOrders = docOrders.getDocumentElement();
		
				iOrderCount = eleOrders.getIntAttribute ("OrderCount");

				// if Orders found that need to be created
				if (iOrderCount > 0)
				{
					YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
					Runtime	rt = Runtime.getRuntime ();
					if (YFSUtil.getDebug ())
					{
						System.out.println ("About to call multiAPI:");
						System.out.println ("Total Memory: " + rt.totalMemory ());
						System.out.println (" Used Memory: " + (rt.totalMemory () - rt.freeMemory ()));
						System.out.println (" Free Memory: " + rt.freeMemory());
					}
					api.multiApi (env, docOrders.getDocument());
					iTotalOrderCount += iOrderCount;
					eleOrders = null;
					docOrders = null;
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Finished call to multiAPI:");
						System.out.println ("Total Memory: " + rt.totalMemory ());
						System.out.println (" Used Memory: " + (rt.totalMemory () - rt.freeMemory ()));
						System.out.println (" Free Memory: " + rt.freeMemory());
					}
					
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Memory Before Garbage Collection:");
						System.out.println ("Total Memory: " + rt.totalMemory ());
						System.out.println (" Used Memory: " + (rt.totalMemory () - rt.freeMemory ()));
						System.out.println (" Free Memory: " + rt.freeMemory());
					}
					// run garbage collection
					rt.gc ();
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Memory After Garbage Collection:");
						System.out.println ("Total Memory: " + rt.totalMemory ());
						System.out.println (" Used Memory: " + (rt.totalMemory () - rt.freeMemory ()));
						System.out.println (" Free Memory: " + rt.freeMemory());
					}
				}			
			} while (iOrderCount > 0);
			eleOutputOrders.setIntAttribute ("OrderCount", iTotalOrderCount);
		} catch (Exception e) {
			throw e;
		} finally {
		  	if (m_rsHeader != null) try { m_rsHeader.close(); } catch(Exception e) {}
		  	if (m_stmtHeader != null) try { m_stmtHeader.close(); } catch(Exception e) {}
			if (m_con != null) try { m_con.close(); } catch(Exception e) {}
			m_con = null;
			m_stmtHeader = null;
			m_rsHeader = null;
		}
		return docOutputOrders.getDocument();
	}

	public Document	importOrders (YFSEnvironment env, Document docIn) throws Exception
	{
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));
		return importOrders (env, docIn, false);
	}

	protected Document	importOrders (YFSEnvironment env, Document docIn, boolean bAllOrders) throws Exception
	{
		Document	docOut = null;
		
		try {
		  // Establish the connection.
		  if (m_con == null)
		  {
	          Class.forName(getServiceParameter ("JdbcClass", docIn, conDefaultClass));
    	      m_con = DriverManager.getConnection(getServiceParameter ("JdbcUrl", docIn, conDefaultURL));
		  }
		  String sOrderType =  getServiceParameter ("OrderType", docIn);
		  if (sOrderType == null || sOrderType.trim().length() == 0)
		  {
			throw new Exception("OrderType argument does not exist or is blank");
		  }

		  if (sOrderType.equalsIgnoreCase ("TPFDD"))
		  {
		  	docOut = importTPFDDOrders (env, docIn);
		  }
		  else
		  {
		  	docOut = importReqOrders (env, docIn, sOrderType);
		  }
      }
	   // Handle any errors that may have occurred.
      catch (Exception e) {
		throw e;
      } finally {
	  		if (!bAllOrders)
			{
			  	if (m_rsHeader != null) try { m_rsHeader.close(); } catch(Exception e) {}
			  	if (m_stmtHeader != null) try { m_stmtHeader.close(); } catch(Exception e) {}
				if (m_con != null) try { m_con.close(); } catch(Exception e) {}
				m_con = null;
				m_stmtHeader = null;
				m_rsHeader = null;
			}
	  }
	  return docOut;
	}		

	public		Document exportOrders (YFSEnvironment env, Document docIn) throws Exception
	{
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));

		YFCDocument	docOrderLineStatusList = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement();
		Iterator<?>	iOrderLineStatusList = eleOrderLineStatusList.getChildren();
		File		fileExport = File.createTempFile (eleOrderLineStatusList.getAttribute ("OptimizationType"), 
													  Long.toHexString (System.currentTimeMillis()), 
													  getOutboundDirectory());

		PrintWriter	fIO = new PrintWriter(new BufferedWriter(new FileWriter(fileExport, false)));
		try {		
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				
			// iterate over all order line status and change status of each line grouped by OrderHeaderKey
			while (iOrderLineStatusList.hasNext())
			{
				YFCElement	eleOrderLineStatus = (YFCElement)iOrderLineStatusList.next();
				String		sOrderReleaseKey = eleOrderLineStatus.getAttribute ("OrderReleaseKey");
				
				// only look for OrderStatus records in the list and ignore any other elements in the list
				if (eleOrderLineStatus.getNodeName ().equals ("OrderStatus"))
				{
					YFCDocument	docOrderLineDetail = YFCDocument.createDocument ("OrderLineDetail");
					YFCElement	eleOrderLineDetail = docOrderLineDetail.getDocumentElement ();
					eleOrderLineDetail.setAttribute ("OrderLineKey", eleOrderLineStatus.getAttribute ("OrderLineKey"));
				
					if (YFSUtil.getDebug())
					{
						System.out.println ("Input to getOrderLineDetails:");
						System.out.println (docOrderLineDetail.getString());
					}
					docOrderLineDetail = YFCDocument.getDocumentFor (api.getOrderLineDetails (env, docOrderLineDetail.getDocument()));
					eleOrderLineDetail = docOrderLineDetail.getDocumentElement ();
					if (YFSUtil.getDebug())
					{
						System.out.println ("Output from getOrderLineDetails:");
						System.out.println (docOrderLineDetail.getString());
					}
					exportOrderLine (fIO, sOrderReleaseKey, eleOrderLineDetail);			
				}
			}
			// flush and close file
			fIO.flush();
			fIO.close();	
			
			// move it to the working folder
			fileExport.renameTo (new File (System.getProperty ("YFS_HOME")+ File.separator + "data" + File.separator + "outbound" + File.separator + "working", fileExport.getName()));
			eleOrderLineStatusList.setAttribute ("ExportFile", fileExport.getName());
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Exiting exportOrders API - Output XML:");
				System.out.println (docOrderLineStatusList.getString());		
			}
		} catch (Exception e) {
			fileExport.delete ();
			throw e;
		}
		return docOrderLineStatusList.getDocument();
	}

	public	Document createShipmentsAndLoads (YFSEnvironment env, Document docIn) throws Exception
	{		
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));

		YFCDocument	docDeliveryPlans = YFCDocument.createDocument ("DeliveryPlans");
		YFCElement	eleDeliveryPlans = docDeliveryPlans.getDocumentElement();
		Document	docOut = docIn;
		
		if (docIn.getDocumentElement ().getNodeName ().equals ("Orders"))
		{
			YFCDocument	docOrders = YFCDocument.getDocumentFor (docIn);
			YFCElement	eleOrders = docOrders.getDocumentElement ();
			Iterator<?>	iOrders = eleOrders.getChildren();
			
			while (iOrders.hasNext())
			{		
				YFCElement	eleOrder = (YFCElement)iOrders.next();
				YFCDocument	docOrder = YFCDocument.getDocumentFor (eleOrder.getString());
				YFCDocument docDeliveryPlan = YFCDocument.getDocumentFor (createShipmentsAndLoads (env, docOrder));
				YFCElement	eleDeliveryPlan = docDeliveryPlan.getDocumentElement();
				eleDeliveryPlans.getDOMNode ().appendChild (eleDeliveryPlan.cloneNode (true).getDOMNode ());
			}
			docOut = docDeliveryPlans.getDocument();
		}
		else
		{
			YFCDocument	docOrder = YFCDocument.getDocumentFor (docIn);
			YFCDocument docDeliveryPlan= YFCDocument.getDocumentFor (createShipmentsAndLoads (env, docOrder));
			docOut = docDeliveryPlan.getDocument();
		}
		return docOut;
	}
	
	protected Document createShipmentsAndLoads (YFSEnvironment env, YFCDocument docOrder) throws Exception
	{
		// This API takes it's input from the TMS optimizer and creates a multiApi input XML
		// to create the shipments, and loads according to the optimizer's recommendations and then
		// creates a delivery plan to execute	
		YFCDocument	docDeliveryPlan = YFCDocument.createDocument ("DeliveryPlan");
		YFCElement	eleDeliveryPlan = docDeliveryPlan.getDocumentElement ();
		YFCElement	eleDeliveryPlanShipments = eleDeliveryPlan.createChild ("Shipments");
		YFCElement	eleOrder = docOrder.getDocumentElement();
		String		sEnterpriseCode = getServiceParameter ("EnterpriseCode", docOrder.getDocument(), "USTRANSCOM");
		String		sDocType = getServiceParameter ("DocumentType", docOrder.getDocument(), "1001");				
		eleDeliveryPlan.setAttribute ("EnterpriseCode", sEnterpriseCode);
		eleDeliveryPlan.setAttribute ("DocumentType", sDocType);
		
		// we have two versions of the load creation that is configurable as to which version is used
		int	iVersion = Integer.parseInt (getServiceParameter ("LoadVersion", docOrder.getDocument(), "2"));		
		eleOrder.setAttribute ("EnterpriseCode", sEnterpriseCode);

		// create shipments
		createShipments (env, eleOrder, eleDeliveryPlanShipments);

		// create delivery plan
		createDeliveryPlan (env, docDeliveryPlan);
						
		// createLoads (env, eleOrder);
		if (iVersion < 2)
			createLoads (env, eleOrder);
		else
			createLoadsVer2 (env, eleOrder, eleDeliveryPlanShipments);
		return docDeliveryPlan.getDocument();
	}
	
	public		Document importAllSites (YFSEnvironment env, Document docIn) throws Exception
	{
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));

		int iTotalSiteCount = 0;
		int iSiteCount = 0;						

		YFCDocument	docOutputSites = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleOutputSites = docOutputSites.getDocumentElement ();

		// it is recommended that you call this API asynchronously by posting the invoking message
		// on a JMS queue, and then having a service that picks up that message and processes it through an
		// integration server
		
		// start a loop importing sites calling multi-api with a maximum number of sites per invocation
		// if any failure results, this API can be called again to restart the import of all remaining sites
		try {
			do {
				YFCDocument docSites = YFCDocument.getDocumentFor (importSites (env, docIn, true));
				YFCElement	eleSites = docSites.getDocumentElement();
			
				iSiteCount = eleSites.getIntAttribute ("SiteCount");

				// if sites found that need to be created
				if (iSiteCount > 0)
				{
					YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
					api.multiApi (env, docSites.getDocument());
					iTotalSiteCount += iSiteCount;
				}			
			} while (iSiteCount > 0);
			eleOutputSites.setIntAttribute ("SiteCount", iTotalSiteCount);
		} catch (Exception e) {
			throw e;
		} finally {
		  	if (m_rsHeader != null) try { m_rsHeader.close(); } catch(Exception e) {}
		  	if (m_stmtHeader != null) try { m_stmtHeader.close(); } catch(Exception e) {}
			if (m_con != null) try { m_con.close(); } catch(Exception e) {}
			m_con = null;
			m_stmtHeader = null;
			m_rsHeader = null;
		}
		return docOutputSites.getDocument();
	}
	
	public		Document importSites (YFSEnvironment env, Document docIn) throws Exception
	{
		String sDebugOn = docIn.getDocumentElement().getAttribute ("DebugOn");
		if (!YFCObject.isVoid(sDebugOn))
			YFSUtil.setDebug (sDebugOn.equals ("Y"));
		return importSites (env, docIn, false);
	}
	
	protected	Document importSites (YFSEnvironment env, Document docIn, boolean bAllSites) throws Exception
	{
		Document	docOut = null;
		
		try {
		  // Establish the connection.
		  if (m_con == null)
		  {
	          Class.forName(getServiceParameter ("JdbcClass", docIn, conDefaultClass));
    	      m_con = DriverManager.getConnection(getServiceParameter ("JdbcUrl", docIn, conDefaultURL));
		  }
		  docOut = importSitesMultiApi (env, docIn);
 		}
     	// Handle any errors that may have occurred.
      	catch (Exception e) {
			throw e;
		}
		finally {
			if (!bAllSites)
			{
			  	if (m_rsHeader != null) try { m_rsHeader.close(); } catch(Exception e) {}
			  	if (m_stmtHeader != null) try { m_stmtHeader.close(); } catch(Exception e) {}
				if (m_con != null) try { m_con.close(); } catch(Exception e) {}
				m_con = null;
				m_stmtHeader = null;
				m_rsHeader = null;
			}
		}
		return docOut;
	}
	
	protected Document importSitesMultiApi (YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument	docMultiApi = YFCDocument.createDocument ("MultiApi");

		try {

		  // Create and execute an SQL statement that returns some data.
          File	fSQL = new File (getSqlFileName(docIn));
          TextData	td = new TextData  (new FileInputStream (fSQL));
		  String sSitesSQL = new String (td.getBytes());

//          m_stmtHeader = m_con.createStatement();
//          m_rsHeader = m_stmtHeader.executeQuery(sSitesSQL);
		  if (m_stmtHeader == null)
		  {
	          m_stmtHeader = m_con.prepareStatement (sSitesSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    	      m_rsHeader = m_stmtHeader.executeQuery();
		  }

 		  // Iterate through the data in the result set and display it.
		  YFCElement	eleMultiApi = docMultiApi.getDocumentElement ();		 
		  int			iSiteCount = 0;
		  int			iMaxSiteCount = -1;	// default for all orders in database
		  
		  String	sSiteCount = getServiceParameter ("MaximumRecords", docIn);
		  if (sSiteCount != null || sSiteCount.length() > 0)
		  	iMaxSiteCount = Integer.parseInt (sSiteCount);
			
		  while (m_rsHeader.next()&& (iSiteCount < iMaxSiteCount && iMaxSiteCount > 0))
		  {
			boolean	bIsSiteValid = false;
		 	String	sSiteKey = m_rsHeader.getString ("GEO_CODE");

			YFCDocument docOrganization = YFCDocument.getDocumentFor ("<Organization OrganizationKey=\""+ sSiteKey + "\"/>");

			// the following logic allows for this service to be restarted against the same data set
			// sites that already have been imported will be skipped becuase validateSites will return false
			// if the site already exists					
			if (bIsSiteValid = validateSite (env, docOrganization))
			{
				YFCElement eleApi = eleMultiApi.createChild ("API");
				eleApi.setAttribute ("Name", "createOrganizationHierarchy");
				YFCElement	eleInput = eleApi.createChild ("Input");
				YFCElement	eleOrganization = eleInput.createChild ("Organization");
				eleOrganization.setAttribute ("OrganizationKey", sSiteKey);
				eleOrganization.setAttribute ("OrganizationCode", sSiteKey);
				setSiteDefaults (env, docIn, eleOrganization);
			
				setSiteValues (env, eleOrganization, m_rsHeader);
				iSiteCount++;
			}
		  }
		  eleMultiApi.setIntAttribute ("SiteCount", iSiteCount);
		  eleMultiApi.setIntAttribute ("MaximumRecords", iMaxSiteCount);
		  
	  } catch (Exception e) {
	  	  throw e;
      }
 	  return docMultiApi.getDocument();		
	}

	protected	boolean	validateSite (YFSEnvironment env, YFCDocument docOrganization)
	{
		boolean	bIsSiteValid = true;
		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			
			YFCElement	eleOrganization = docOrganization.getDocumentElement();
			YFCDocument	docOrgTemplate = YFCDocument.getDocumentFor ("<Organization OrganizationKey=\"\" OrganizationCode=\"\"/>");
			env.setApiTemplate ("getOrganizationHierarchy", docOrgTemplate.getDocument());
			docOrganization = YFCDocument.getDocumentFor (api.getOrganizationHierarchy (env, docOrganization.getDocument()));
			env.clearApiTemplate ("getOrganizationHierarchy");
			
			eleOrganization = docOrganization.getDocumentElement();
			String		sOrganizationCode = eleOrganization.getAttribute ("OrganizationCode");
			if (sOrganizationCode != null && sOrganizationCode.length () > 0)	
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Node: " + sOrganizationCode + " already exits on system.  Skipping this Node");
				}
			
				// duplicate site defined
				bIsSiteValid = false;
			}
		} catch (Exception ignore) {
		}
		return bIsSiteValid;
	}

	protected	void	setSiteDefaults (YFSEnvironment env, Document docIn, YFCElement eleOrganization) throws Exception
	{
		eleOrganization.setAttribute ("CreatorOrganizationKey", getServiceParameter ("CreatorOrganizationKey", docIn, "DEFAULT"));
		eleOrganization.setAttribute ("InventoryPublished", "N");
		eleOrganization.setAttribute ("ParentOrganizationCode", getServiceParameter("ParentOrganizationCode", docIn, "USTRANSCOM"));
		eleOrganization.setAttribute ("PrimaryEnterpriseKey", getServiceParameter ("PrimaryEnterpriseKey", docIn, "USTRANSCOM"));
		YFCElement	eleOrgRoleList = eleOrganization.createChild ("OrgRoleList");
		YFCElement	eleOrgRole = eleOrgRoleList.createChild ("OrgRole");
		eleOrgRole.setAttribute ("RoleKey", "NODE");
		YFCElement	eleNode = eleOrganization.createChild ("Node");
		eleNode.setAttribute ("Inventorytype", "INFINITE");
		eleNode.setAttribute ("InventoryTracked", "N");
		eleNode.setAttribute ("NodeType", "PORT");
		eleNode.setAttribute ("CanShipToAllNodes", "Y");
		eleNode.setAttribute ("CanShipToOtherAddresses", "Y");
		eleNode.setAttribute ("ActiveFlag", "Y");
	}
	
	protected	void	setSiteValues (YFSEnvironment env, YFCElement eleOrganization, ResultSet rsSite) throws Exception
	{
		eleOrganization.setAttribute ("OrganizationName", rsSite.getString ("GEO_NAME"));
		
		YFCElement	eleNode = eleOrganization.getChildElement("Node");
		if (eleNode == null)
			eleNode = eleOrganization.createChild ("Node");
		eleNode.setAttribute ("Latitude", rsSite.getString ("LATITUDE"));
		eleNode.setAttribute ("Longitude", rsSite.getString ("LONGITUDE"));
		eleNode.setAttribute ("IdentifiedByParentAs", rsSite.getString ("GEO_CODE"));

		YFCElement	eleCorporatePersonInfo = eleOrganization.getChildElement("CorporatePersonInfo");
		if (eleCorporatePersonInfo == null)
			eleCorporatePersonInfo = eleOrganization.createChild ("CorporatePersonInfo");
			
		getSiteAddressFromGeoLocation (env, eleCorporatePersonInfo, rsSite);					
	}
	
	protected	void	getSiteAddressFromGeoLocation (YFSEnvironment env, YFCElement eleCorporatePersonInfo, ResultSet rsSite) throws Exception
	{

		// see if US city/state matches the latitude/logitude from GEO locations.  If so then we can poupluate
		// more of the location address details based on the YFS_ZIP_CODE_LOCATIONS table.   Assumes this data
		// has been loaded into Yantra
		Connection	con = null;
		Statement	stmt = null;
		ResultSet	rs = null;

		try {		
			YFCDocument	docConnParams = YFCDocument.createDocument ("GetDBConnParams");
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			docConnParams = YFCDocument.getDocumentFor (api.getDBConnParams(env, docConnParams.getDocument()));
			YFCElement	eleConnParams = docConnParams.getDocumentElement ();
			eleCorporatePersonInfo.setAttribute ("Latitude", rsSite.getString ("LATITUDE"));
			eleCorporatePersonInfo.setAttribute ("Longitude", rsSite.getString ("LONGITUDE"));
			eleCorporatePersonInfo.setAttribute ("AddressLine1", rsSite.getString ("GEO_NAME"));
			eleCorporatePersonInfo.setAttribute ("AddressLine4", rsSite.getString ("COUNTRY_OR_STATE"));

			// now connect to yantra database to access the zip code location table
			con = DriverManager.getConnection (eleConnParams.getAttribute ("ConnectionStr"), eleConnParams.getAttribute ("UserId"), eleConnParams.getAttribute("Password"));
			stmt = con.createStatement();

			String strSql = zipCodeLookupSQL;
			strSql = strSql.replaceAll ("%LONGITUDE%", rsSite.getString ("LONGITUDE"));
			strSql = strSql.replaceAll ("%LATITUDE%", rsSite.getString ("LATITUDE"));	
			rs = stmt.executeQuery(strSql);
		
			if(rs.next())
			{
				eleCorporatePersonInfo.setAttribute ("City", rs.getString ("CITY"));
				eleCorporatePersonInfo.setAttribute ("State", rs.getString ("STATE"));
				eleCorporatePersonInfo.setAttribute ("ZipCode", rs.getString ("ZIP_CODE"));
				eleCorporatePersonInfo.setAttribute ("Country", rs.getString ("COUNTRY"));
			}
			else
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("Import Sites Warning: Missing YFS_ZIP_CODE_LOCATION record for Latitude=" + rsSite.getString ("LATITUDE") + " Longitude=" + rsSite.getString ("LONGITUDE") + " In Country or State: " + rsSite.getString ("COUNTRY_OR_STATE"));
					System.out.println ("Country, City, State or Zip could not be populated for site:" + rsSite.getString("GEO_CODE")+ " - "+rsSite.getString ("GEO_NAME"));
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null) { try { rs.close(); } catch (Exception ignore) {} }
			if (stmt != null) { try { stmt.close(); } catch (Exception ignore) {} }
			if (con != null) { try { con.close(); } catch (Exception ignore) {} }
		}				
	}

	protected	Document importTPFDDOrders (YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument	docMultiApi = YFCDocument.createDocument ("MultiApi");
		YFCElement	eleMultiApi = docMultiApi.getDocumentElement ();		 

		// initialize detail result set and statement
		m_stmtDetail = null;
		m_rsDetail = null;
	
		try {

		  // Create and execute an SQL statement that returns some data.
          File	fSQL = new File (getSqlFileName(docIn, "_Details"));
          TextData	td = new TextData  (new FileInputStream (fSQL));
		  String sOrderLinesSQL = new String (td.getBytes());
		  
		  fSQL = new File (getSqlFileName (docIn, "_Headers"));
          td = new TextData  (new FileInputStream (fSQL));		  
		  String sOrderHeaderSQL = new String (td.getBytes());
		  String sStartDate = getServiceParameter ("StartDate", docIn, "9/30/2007");
		  String sEndDate   = getServiceParameter ("EndDate", docIn, "11/30/2007");
		  String sOrderHeaderSQLToExecute = sOrderHeaderSQL.replaceAll ("%StartDate%", sStartDate);
		  sOrderHeaderSQLToExecute = sOrderHeaderSQLToExecute.replaceAll ("%EndDate%", sEndDate);
         
		  if (m_stmtHeader == null)
		  {
//			m_stmtHeader = m_con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//			rsHeader = m_stmtHeader.executeQuery(sOrderHeaderSQL);
          	m_stmtHeader = m_con.prepareStatement (sOrderHeaderSQLToExecute, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		    m_rsHeader   = m_stmtHeader.executeQuery ();
		  }

 		  // Iterate through the data in the result set and display it.
		  int			iOrderCount = 0;
		  int			iTotalOrderLineCount = 0;
		  int			iMaxOrderCount = -1;	// default for all orders in database
		  
		  String	sOrderCount = getServiceParameter ("MaximumRecords", docIn);
		  if (sOrderCount != null || sOrderCount.length() > 0)
		  	iMaxOrderCount = Integer.parseInt (sOrderCount);
		  			
		  while (m_rsHeader.next()&& (iOrderCount < iMaxOrderCount && iMaxOrderCount > 0))
		  {
			boolean	bIsOrderValid = false;
			int		iOrderLineCount = 0;
		 	String	sOrderHeaderKey = m_rsHeader.getString ("THID");
			String	sOrderNo = m_rsHeader.getString ("ORDERNO");
			int		iPassengers = m_rsHeader.getInt ("PASSENGERS");
			String 	sActualOrderHeaderKey=sOrderHeaderKey;
			String	sActualOrderNo = "D-" + sOrderNo;
			
			YFCDocument docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\""+ sActualOrderHeaderKey + "\"/>");

			// the following logic allows for this service to be restarted against the same data set
			// Orders that already have been imported will be skipped becuase validateOrders will return false
			// if the Order already exists					
			if (bIsOrderValid = validateOrder (env, docOrder))
			{
				YFCElement eleApi = eleMultiApi.createChild ("API");
				eleApi.setAttribute ("Name", "createOrder");
				YFCElement	eleInput = eleApi.createChild ("Input");
				YFCElement	eleOrder = eleInput.createChild ("Order");
				eleOrder.setAttribute ("OrderHeaderKey", sActualOrderHeaderKey);
				eleOrder.setAttribute ("OrderNo", sActualOrderNo);
				setOrderDefaults (env, docIn, eleOrder, "TPFDD");
			
				setOrderValues (env, eleOrder, m_rsHeader);
				iOrderCount++;
					
				// now add the order lines element
				YFCElement eleOrderLines = eleOrder.createChild ("OrderLines");
				if (iPassengers > 0)
				{				
					// now add an order line element for the passengers
				   	YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
					eleOrderLine.setAttribute ("OrderLineKey", sActualOrderHeaderKey +  (iOrderLineCount+1));
					setOrderLineDefaults (env, eleOrderLine, iOrderLineCount+1);
					setOrderLineValues (env, eleOrderLine, m_rsHeader);
					iTotalOrderLineCount++;
					iOrderLineCount++;	
				}		
				String	sOrderLinesSQLToExecute = sOrderLinesSQL.replaceAll ("%RLN%", sOrderNo);
//				m_stmtDetail = m_con.createStatement();
//				m_rsDetail = m_stmtDetail.executeQuery (sOrderLinesSQLToExecute);
				m_stmtDetail = m_con.prepareStatement (sOrderLinesSQLToExecute, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				m_rsDetail = m_stmtDetail.executeQuery ();

				// get all detail for this line
				while (m_rsDetail.next())
				{
				   	YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
					eleOrderLine.setAttribute ("OrderLineKey", sActualOrderHeaderKey + (iOrderLineCount+1));
					setOrderLineDefaults (env, eleOrderLine, iOrderLineCount+1);
					setOrderLineValues (env, eleOrderLine, m_rsDetail);
					iTotalOrderLineCount++;
					iOrderLineCount++;
				}
				// close detail statement and result set
				// finalizeOrder (env, eleOrder);
				m_stmtDetail.close();
				m_rsDetail.close();
				m_stmtDetail = null;
				m_rsDetail = null;
				
				// if this is an order with 0 order lines
				if (iOrderLineCount == 0)
				{
					// remove the entire API node from the DOM document
					eleApi.getParentNode().removeChild (eleApi.getDOMNode());
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Warning:");
						System.out.println ("Order with RLN="+sOrderNo+" 0 Lines.  Order cannot be imported")		;
					}
				}			
			}
		  }
		  eleMultiApi.setIntAttribute ("OrderCount", iOrderCount);
		  eleMultiApi.setIntAttribute ("OrderLineCount", iTotalOrderLineCount);
		  eleMultiApi.setIntAttribute ("MaximumOrders", iMaxOrderCount);
		  
	  } catch (Exception e) {
	  	  throw e;
      } finally {
		  if (m_rsDetail != null) try { m_rsDetail.close(); } catch(Exception e) {}
		  if (m_stmtDetail != null) try { m_stmtDetail.close(); } catch(Exception e) {}
		  m_stmtDetail = null;
		  m_rsDetail = null;
      }
 	  return docMultiApi.getDocument();
	}



	protected Document importReqOrders (YFSEnvironment env, Document docIn, String sOrderType) throws Exception
	{
		YFCDocument	docMultiApi = YFCDocument.createDocument ("MultiApi");

//		Statement			stmtOrders = null;
		
		try {

		  // Create and execute an SQL statement that returns some data.
          File	fSQL = new File (getSqlFileName(docIn));
          TextData	td = new TextData  (new FileInputStream (fSQL));
		  String sOrdersSQL = new String (td.getBytes());
		  String sStartDate = getServiceParameter ("StartDate", docIn, "9/30/2007");
		  String sEndDate   = getServiceParameter ("EndDate", docIn, "11/30/2007");
		  String sOrdersSQLToExecute = sOrdersSQL.replaceAll ("%StartDate%", sStartDate);
		  sOrdersSQLToExecute = sOrdersSQLToExecute.replaceAll ("%EndDate%", sEndDate);

		  if (m_stmtHeader == null)
		  {
//		  	  m_stmtOrders = m_con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//		 	  m_rsHeader = stmtOrders.executeQuery(sOrdersSQL);
	          m_stmtHeader = m_con.prepareStatement (sOrdersSQLToExecute, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    	      m_rsHeader = m_stmtHeader.executeQuery();
		  }
		  
 		  // Iterate through the data in the result set and display it.
		  YFCElement	eleMultiApi = docMultiApi.getDocumentElement ();		 
		  int			iOrderCount = 0;
		  int			iTotalOrderLineCount = 0;
		  int			iMaxOrderCount = -1;	// default for all orders in database
		  
		  String	sOrderCount = getServiceParameter ("MaximumRecords", docIn);
		  if (sOrderCount != null || sOrderCount.length() > 0)
		  	iMaxOrderCount = Integer.parseInt (sOrderCount);
			
		  while (m_rsHeader.next()&& (iOrderCount < iMaxOrderCount && iMaxOrderCount > 0))
		  {
			boolean	bIsOrderValid = false;
			int		iOrderLineCount = 0;
		 	String	sOrderKey = m_rsHeader.getString ("THID");
			String	sActualOrderKey = sOrderKey;
			String	sOrderNo = m_rsHeader.getString ("ORDERNO");
			String	sActualOrderNo = sOrderType.substring (0, 1) + "-" + sOrderNo;
						
			YFCDocument docOrder = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\""+ sActualOrderKey + "\"/>");

			// the following logic allows for this service to be restarted against the same data set
			// Orders that already have been imported will be skipped becuase validateOrders will return false
			// if the Order already exists					
			if (bIsOrderValid = validateOrder (env, docOrder))
			{
				YFCElement eleApi = eleMultiApi.createChild ("API");
				eleApi.setAttribute ("Name", "createOrder");
				YFCElement	eleInput = eleApi.createChild ("Input");
				YFCElement	eleOrder = eleInput.createChild ("Order");
				eleOrder.setAttribute ("OrderHeaderKey", sActualOrderKey);
				eleOrder.setAttribute ("OrderNo", sActualOrderNo);
				setOrderDefaults (env, docIn, eleOrder, sOrderType);
			
				setOrderValues (env, eleOrder, m_rsHeader);
				iOrderCount++;
				
				// now add the order lines element
				YFCElement	eleOrderLines = eleOrder.createChild ("OrderLines");
			   	YFCElement	eleOrderLine = eleOrderLines.createChild ("OrderLine");
				eleOrderLine.setAttribute ("OrderLineKey", sActualOrderKey + "1");
				setOrderLineDefaults (env, eleOrderLine, iOrderLineCount+1);
				setOrderLineValues (env, eleOrderLine, m_rsHeader);
				iTotalOrderLineCount++;
				iOrderLineCount++;
				// finalizeOrder (env, eleOrder);
			}
		  }
		  eleMultiApi.setIntAttribute ("OrderCount", iOrderCount);
		  eleMultiApi.setIntAttribute ("MaximumRecords", iMaxOrderCount);
		  
	  } catch (Exception e) {
	  	  throw e;
      }
	  return docMultiApi.getDocument();		
	}

	private boolean validateOrder (YFSEnvironment env, YFCDocument docOrder)
	{
		boolean	bIsOrderValid = true;

		try {
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
	
			YFCElement	eleOrder = docOrder.getDocumentElement();
			YFCDocument	docOrderTemplate = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\""+eleOrder.getAttribute ("OrderHeaderKey")+"\" OrderNo=\"\"/>");
			env.setApiTemplate ("getOrderDetails", docOrderTemplate.getDocument());
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to getOrderDetails:"+docOrder.getString());
			}
			docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
			env.clearApiTemplate ("getOrderDetails");
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getOrderDetails:"+docOrder.getString());
			}
			// if there is already an order on the system with the given order header key
			eleOrder = docOrder.getDocumentElement();
			String 	sOrderNo = eleOrder.getAttribute ("OrderNo");

			if (sOrderNo != null && sOrderNo.length() > 0)
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Order No: " + sOrderNo + " already exits on system.  Skipping this order");
				}
				bIsOrderValid = false;
			}		
		} catch (Exception ignore) {}
		return bIsOrderValid;
	}	

	protected	void setOrderDefaults (YFSEnvironment env, Document docIn, YFCElement eleOrder, String sOrderType) throws Exception
	{
			// set common defaults for all order types
			eleOrder.setAttribute ("EnterpriseCode", getServiceParameter ("EnterpriseCode", docIn, "USTRANSCOM"));
			eleOrder.setAttribute ("BuyerOrganizationCode", eleOrder.getAttribute ("EnterpriseCode"));
			eleOrder.setAttribute ("SellerOrganizaitonCode", eleOrder.getAttribute ("EnterpriseCode"));
			eleOrder.setAttribute ("OrderType", sOrderType);
			eleOrder.setAttribute ("ApplyDefaultTemplate", "Y");
			eleOrder.setAttribute ("DocumentType", "0001");	
			eleOrder.setAttribute ("OrderDate", getServiceParameter ("OrderDate", docIn, new YFCDate (true).getString()));
	}

	@SuppressWarnings("deprecation")
	protected void setOrderValues (YFSEnvironment env, YFCElement eleOrder, ResultSet rsDetail) throws Exception
	{
			// set common defaults for all order types
			eleOrder.setAttribute ("EntryType", rsDetail.getString ("ORDERNO"));
			eleOrder.setAttribute ("ReceivingNode", rsDetail.getString ("TOID"));
			eleOrder.setAttribute ("ShipNode", rsDetail.getString ("FROMID"));			

			if (rsDetail.getString ("EARLIEST_ARRIVAL").trim().length() > 0)
			{
				YFCDate	dtReqDeliveryDate = new YFCDate (rsDetail.getString ("EARLIEST_ARRIVAL"), dtFmtTmsDateFormat, true);		
				eleOrder.setDateAttribute ("ReqDeliveryDate", dtReqDeliveryDate);
			}
			eleOrder.setAttribute ("Priority", rsDetail.getString ("PRIORITY"));
	}

	protected void setOrderLineDefaults (YFSEnvironment env, YFCElement eleOrderLine, int iOrderLine)
	{
			// set common defaults for all order types
			eleOrderLine.setAttribute ("DeliveryMethod", "SHP");
			eleOrderLine.setAttribute ("IsFirmPredefinedNode", "Y");
			eleOrderLine.setIntAttribute ("PrimeLineNo", iOrderLine);		
			return;
	}

	protected void setOrderLineValues (YFSEnvironment env, YFCElement eleOrderLine, ResultSet rsDetail)  throws Exception
	{
			// set common defaults for all order types
			eleOrderLine.setAttribute ("ReceivingNode", rsDetail.getString ("TOID"));
			eleOrderLine.setAttribute ("ShipNode", rsDetail.getString ("FROMID"));			
			
			if (rsDetail.getString ("EARLIEST_ARRIVAL").trim().length() > 0)
			{
				YFCDate	dtReqDeliveryDate = new YFCDate (rsDetail.getString ("EARLIEST_ARRIVAL"), dtFmtTmsDateFormat, true);	
				eleOrderLine.setDateAttribute ("ReqDeliveryDate", dtReqDeliveryDate);
			}
			

			YFCElement eleOrder = eleOrderLine.getParentElement().getParentElement();
			String		sOrderType = eleOrder.getAttribute ("OrderType");
			
			YFCElement eleItem = eleOrderLine.createChild ("Item");

			// get line item information
			String	sItemID = rsDetail.getString ("NSN");
			String	sItemDescription = rsDetail.getString ("NSN_NAME");
			String	sProductType = rsDetail.getString ("PRODUCT_TYPE");
			String	sPassengers = rsDetail.getString ("PASSENGERS");
			String	sQty = rsDetail.getString ("PRODUCT_QTY");
			
			// if TPFDD orders
			if (sOrderType.equalsIgnoreCase ("TPFDD"))
			{
				String	sLineType = (rsDetail.getInt ("PASSENGERS") > 0) ? "TPFDD_PAX" : "TPFDD_CRGO";
				if (sLineType.equalsIgnoreCase ("TPFDD_CRGO"))
				{
					sItemID = rsDetail.getString ("NSN_NAME");
					sItemDescription = sItemID;
				}
				else if (sLineType.equalsIgnoreCase ("TPFDD_PAX"))
				{
					sItemID = "PASSENGER";
					sItemDescription = "Passenger(s)";
					sQty = sPassengers;
				}
				eleOrderLine.setAttribute ("LineType", sLineType);
			}
			// for all other orders
			else
			{
				// if passengers are passed on the order
				if (sPassengers != null && sPassengers.trim().length() > 0 && Integer.parseInt(sPassengers) > 0)
				{
					sItemID= "PASSENGER";
					sItemDescription = "Passenger(s)";
					sQty = sPassengers;
				}	
				// if theater requisition
				if (sProductType != null && sProductType.equalsIgnoreCase ("THEATER"))
				{
					sItemID = "THEATER";
					sItemDescription = "Theater Requisition";
				}
			}
			// generate default for required fields if blank from database feed
			if (sItemID.trim().length() == 0)
				sItemID = sNO_NSN;

			if (sItemDescription.trim().length() == 0)
				sItemDescription = sNO_NSN_NAME;

			if (sQty.trim().length() == 0)
				sQty = "1";
			
			// set up the item information
			eleItem.setAttribute ("ItemID", sItemID);
			eleItem.setAttribute ("ItemShortDesc", sItemDescription);
			eleItem.setAttribute ("ItemDesc", sItemDescription);
			eleItem.setAttribute ("UnitOfMeasure", "EACH");
			eleOrderLine.setAttribute ("OrderedQty", sQty);
			
			YFCElement	eleOrderDates = eleOrderLine.createChild ("OrderDates");

			// add custom dates Pick By, Drop By, Pick Available, Drop Available
			if (rsDetail.getString ("PICK_BY").trim().length() > 0)
			{
				YFCElement	elePickByDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtPickByDate = new YFCDate (rsDetail.getString ("PICK_BY"), dtFmtTmsDateFormat, true);		
				elePickByDate.setAttribute ("DateTypeId", "PICK_BY");
				elePickByDate.setDateAttribute ("ExpectedDate", dtPickByDate);

				YFCElement	eleDepartFromOrigin = eleOrderDates.createChild ("OrderDate");
				eleDepartFromOrigin.setAttribute ("DateTypeId", "DEPART_FROM_ORIGIN");
				eleDepartFromOrigin.setDateAttribute ("ExpectedDate", dtPickByDate);
			}
			if (rsDetail.getString ("DROP_BY").trim().length() > 0)
			{
				YFCElement	eleDropByDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtDropByDate = new YFCDate (rsDetail.getString ("DROP_BY"), dtFmtTmsDateFormat, true);		
				eleDropByDate.setAttribute ("DateTypeId", "DROP_BY");
				eleDropByDate.setDateAttribute ("ExpectedDate", dtDropByDate);
			}
			if (rsDetail.getString ("PICK_AVAIL").trim().length() > 0)
			{			
				YFCElement	elePickAvailDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtPickAvailDate = new YFCDate (rsDetail.getString ("PICK_AVAIL"), dtFmtTmsDateFormat, true);		
				elePickAvailDate.setAttribute ("DateTypeId", "PICK_AVAIL");
				elePickAvailDate.setDateAttribute ("ExpectedDate", dtPickAvailDate);
			}
			if (rsDetail.getString ("DROP_AVAIL").trim().length() > 0)
			{			
				YFCElement	eleDropAvailDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtDropAvailDate = new YFCDate (rsDetail.getString ("DROP_AVAIL"), dtFmtTmsDateFormat, true);		
				eleDropAvailDate.setAttribute ("DateTypeId", "DROP_AVAIL");
				eleDropAvailDate.setDateAttribute ("ExpectedDate", dtDropAvailDate);
			}

			// add custom dates Ready To Load, Available to Load, Earliest Arrival, Latest Arrival
			/*
			if (rsDetail.getString ("READY_TO_LOAD").trim().length() > 0)
			{
				YFCElement	eleReadyToLoadDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtReadyToLoadDate = new YFCDate (rsDetail.getString ("READY_TO_LOAD"), dtFmtTmsDateFormat, true);		
				eleReadyToLoadDate.setAttribute ("DateTypeId", "READY_TO_LOAD");
				eleReadyToLoadDate.setDateAttribute ("ExpectedDate", dtReadyToLoadDate);
			}
			*/
			
			if (rsDetail.getString ("AVAIL_TO_LOAD").trim().length() > 0)
			{			
				YFCElement	eleAvailToLoadDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtAvailToLoadDate = new YFCDate (rsDetail.getString ("AVAIL_TO_LOAD"), dtFmtTmsDateFormat, true);		
				eleAvailToLoadDate.setAttribute ("DateTypeId", "AVAIL_TO_LOAD");
				eleAvailToLoadDate.setDateAttribute ("ExpectedDate", dtAvailToLoadDate);
			}
			if (rsDetail.getString ("EARLIEST_ARRIVAL").trim().length() > 0)
			{
				YFCElement	eleEarliestArrivalDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtEarliestArrivalDate = new YFCDate (rsDetail.getString ("EARLIEST_ARRIVAL"), dtFmtTmsDateFormat, true);		
				eleEarliestArrivalDate.setAttribute ("DateTypeId", "EARLIEST_ARRIVAL");
				eleEarliestArrivalDate.setDateAttribute ("ExpectedDate", dtEarliestArrivalDate);
			}
			if (rsDetail.getString ("LATEST_ARRIVAL").trim().length() > 0)
			{			
				YFCElement	eleLatestArrivalDate = eleOrderDates.createChild ("OrderDate");
				YFCDate		dtLatestArrivalDate = new YFCDate (rsDetail.getString ("LATEST_ARRIVAL"), dtFmtTmsDateFormat, true);		
				eleLatestArrivalDate.setAttribute ("DateTypeId", "LATEST_ARRIVAL");
				eleLatestArrivalDate.setDateAttribute ("ExpectedDate", dtLatestArrivalDate);

				YFCElement	eleArriveAtDestination = eleOrderDates.createChild ("OrderDate");
				eleArriveAtDestination.setAttribute ("DateTypeId", "ARRIVE_AT_DESTINATION");
				eleArriveAtDestination.setDateAttribute ("ExpectedDate", dtLatestArrivalDate);

			}

			// add all other miscellaneous elements as order line refernces
			YFCElement	eleReferences = eleOrderLine.createChild ("References");
			YFCElement	eleRefExpectedWeight  = eleReferences.createChild ("Reference");
			eleRefExpectedWeight.setAttribute ("Name", "EXPECTED_WEIGHT");
			eleRefExpectedWeight.setAttribute ("Value", rsDetail.getString ("WEIGHT"));
			YFCElement	eleRefExpectedVolume  = eleReferences.createChild ("Reference");
			eleRefExpectedVolume.setAttribute ("Name", "EXPECTED_VOLUME");
			eleRefExpectedVolume.setAttribute ("Value", rsDetail.getString ("VOLUME"));
			YFCElement	eleRefExpectedSqft  = eleReferences.createChild ("Reference");
			eleRefExpectedSqft.setAttribute ("Name", "EXPECTED_SQFT");
			eleRefExpectedSqft.setAttribute ("Value", rsDetail.getString ("SQFT"));
			YFCElement	eleRefProductType  = eleReferences.createChild ("Reference");
			eleRefProductType.setAttribute ("Name", "PRODUCT_TYPE");
			eleRefProductType.setAttribute ("Value", rsDetail.getString ("PRODUCT_TYPE"));
			YFCElement	eleRefPOD  = eleReferences.createChild ("Reference");
			eleRefPOD.setAttribute ("Name", "POD");
			eleRefPOD.setAttribute ("Value", rsDetail.getString ("POD"));
			YFCElement	eleRefPOE  = eleReferences.createChild ("Reference");
			eleRefPOE.setAttribute ("Name", "POE");
			eleRefPOE.setAttribute ("Value", rsDetail.getString ("POE"));
			YFCElement	eleRefPriority  = eleReferences.createChild ("Reference");
			eleRefPriority.setAttribute ("Name", "PRIORITY");
			eleRefPriority.setAttribute ("Value", rsDetail.getString ("PRIORITY"));
			YFCElement	eleRefTransCode  = eleReferences.createChild ("Reference");
			eleRefTransCode.setAttribute ("Name", "TRANSCODE");
			eleRefTransCode.setAttribute ("Value", rsDetail.getString ("TRANSCODE"));
			YFCElement	eleRefCCC  = eleReferences.createChild ("Reference");
			eleRefCCC.setAttribute ("Name", "CCC");
			eleRefCCC.setAttribute ("Value", rsDetail.getString ("CCC"));
			YFCElement	eleRefMode  = eleReferences.createChild ("Reference");
			eleRefMode.setAttribute ("Name", "MODE");
			eleRefMode.setAttribute ("Value", rsDetail.getString ("TPFDD_MODE"));
	}
	
	protected void finalizeOrder (YFSEnvironment env, YFCElement eleOrder) throws Exception
	{		
		YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
		BigDecimal	bdWeightTotal = new BigDecimal ("0.00");
		if (eleOrderLines != null)
		{
			Iterator<?>	iOrderLines = eleOrderLines.getChildren ();
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				YFCElement	eleOrderLineReferences = eleOrderLine.getChildElement ("References");
				if (eleOrderLineReferences != null)
				{
					Iterator<?>	iReferences = eleOrderLineReferences.getChildren ();
					while (iReferences.hasNext())
					{
						YFCElement	eleReference = (YFCElement)iReferences.next();
						if (eleReference.getAttribute ("Name").equalsIgnoreCase ("EXPECTED_WEIGHT"))
						{
							String	sExpectedWeight = eleReference.getAttribute ("Value");
							if (sExpectedWeight != null)
							{
								bdWeightTotal = bdWeightTotal.add (new BigDecimal (sExpectedWeight));
							}
							break;
						}
					}
				}
			}
		}

		// add total weight of all lines to header reference field
		bdWeightTotal = bdWeightTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
		if (bdWeightTotal.compareTo (new BigDecimal ("0.00")) != 0)
		{
			YFCElement	eleOrderReferences = eleOrder.getChildElement ("References");
			if (eleOrderReferences == null)
				eleOrderReferences = eleOrder.createChild ("References");
			YFCElement	eleOrderReference = eleOrderReferences.createChild ("Reference");
			eleOrderReference.setAttribute ("Name", "EXPECTED_WEIGHT");
			eleOrderReference.setAttribute ("Value", bdWeightTotal.toString ());
		}
		// set search criteria to 'Y' if weight exceeds 1300
		if (bdWeightTotal.compareTo (new BigDecimal (getServiceParameter ("BigOrderWeight", null, "1300.00"))) > 0)
			eleOrder.setAttribute ("SearchCriteria1", "Y");
		else
			eleOrder.setAttribute ("SearchCriteria1", "N");		
	}
	
	protected Document	createDeliveryPlan (YFSEnvironment env, YFCDocument	docDeliveryPlan) throws Exception
	{
		// now create the delivery plan
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to manageDeliveryPlay");
			System.out.println (docDeliveryPlan.getString());
		}
		return api.manageDeliveryPlan (env, docDeliveryPlan.getDocument());
	}
	
	protected void		createShipments (YFSEnvironment env, YFCElement eleOrder, YFCElement eleDeliveryPlanShipments) throws Exception
	{
		YFCElement	eleShipments = eleOrder.getChildElement ("Shipments");
		Iterator<?>	iShipments = eleShipments.getChildren ();
		String		sOrderLineKey = eleOrder.getAttribute ("OrderKey");
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();
			
		while (iShipments.hasNext ())
		{
			YFCElement	eleShipment = (YFCElement)iShipments.next ();
			
			// let system generate the shipment no
			eleShipment.removeAttribute ("ShipmentNo");
			
			getShipmentLineDetailsForOrderLine (env, eleShipment, sOrderLineKey);
			eleShipment.setAttribute ("Action", "Create-Modify");
			try {
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Input to changeShipment API:");
					System.out.println (eleShipment.getString());
				}
				YFCDocument	docShipment = YFCDocument.getDocumentFor (eleShipment.getString());				
				docShipment = YFCDocument.getDocumentFor (api.changeShipment (env, docShipment.getDocument ()));
				eleShipment = docShipment.getDocumentElement ();
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Output from changeShipment API:");
					System.out.println (eleShipment.getString());
				}
				
				YFCElement	eleDeliveryPlanShipment = eleDeliveryPlanShipments.createChild ("Shipment");
				eleDeliveryPlanShipment.setAttribute ("EnterpriseCode", eleShipment.getAttribute ("EnterpriseCode"));
				eleDeliveryPlanShipment.setAttribute ("SellerOrganizationCode", eleShipment.getAttribute ("SellerOrganizationCode"));
				eleDeliveryPlanShipment.setAttribute ("ShipmentKey", eleShipment.getAttribute ("ShipmentKey"));
				eleDeliveryPlanShipment.setAttribute ("ShipmentNo", eleShipment.getAttribute ("ShipmentNo"));
				eleDeliveryPlanShipment.setAttribute ("ShipNode", eleShipment.getAttribute ("ShipNode"));
				eleDeliveryPlanShipment.setAttribute ("ReceivingNode", eleShipment.getAttribute ("ReceivingNode"));
				
			} catch (Exception e) {
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Exception in changeShipment");
					e.printStackTrace (System.out);
					throw e;
				}
			}	
		}	
	}	

	protected	void createLoads (YFSEnvironment env, YFCElement eleOrder) throws Exception
	{
		YFCElement	eleLoads = eleOrder.getChildElement ("Loads");
		Iterator<?>	iLoads = eleLoads.getChildren ();
		String		sOrderLineKey = eleOrder.getAttribute ("OrderKey");
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();

		while (iLoads.hasNext ())
		{
			YFCElement	eleLoad = (YFCElement)iLoads.next();
			eleLoad.setAttribute ("EnterpriseCode", eleOrder.getAttribute ("EnterpriseCode"));
			eleLoad.setAttribute ("CreateOneLoadPerShipment", "N");
			
			YFCElement	eleLoadStops = eleLoad.getChildElement ("LoadStops");
			Iterator<?>	iLoadStops = eleLoadStops.getChildren ();
			boolean		bIsFirstStop = true;
			boolean		bIsLastStop = false;
			
			
			while (iLoadStops.hasNext ())
			{
				YFCElement	eleLoadStop = (YFCElement)iLoadStops.next();
				bIsLastStop = !iLoadStops.hasNext();
				
				String	sCarrier = eleLoadStop.getAttribute ("Carrier");
				if (sCarrier != null && eleLoad.getAttribute ("ScacAndService") == null)
				{
					String	sSCACAndService = "";
				
					if (sCarrier.equalsIgnoreCase ("Direct Air"))
						sSCACAndService = "TT13";
					else if (sCarrier.equalsIgnoreCase ("Commercial Air"))
						sSCACAndService = "CA11";
					else if (sCarrier.equalsIgnoreCase ("Organic Air"))
						sSCACAndService = "OA11";
					else if (sCarrier.equalsIgnoreCase ("Theater Air"))
						sSCACAndService = "TA11";
					else if (sCarrier.equalsIgnoreCase ("Commercial Ship"))
						sSCACAndService = "CS12";
					else if (sCarrier.equalsIgnoreCase ("Organic Ship"))
						sSCACAndService = "OS12";
					else if (sCarrier.equalsIgnoreCase ("Theater Truck"))
						sSCACAndService = "TT13";
					eleLoad.setAttribute ("ScacAndService", sSCACAndService);
				}
				String	sShipID = eleLoadStop.getAttribute ("ShipID");
				if (sShipID != null && eleLoad.getAttribute ("TrailerNo") == null)
				{
					eleLoad.setAttribute ("VoyageNo", sShipID);
					eleLoad.setAttribute ("TrailerNo", sShipID);
				}
				String	sRouteID = eleLoadStop.getAttribute ("RouteID");
				if (sRouteID != null && eleLoad.getAttribute ("LoadType") == null)
				{
					if (sRouteID.equalsIgnoreCase ("Unrouted"))
						eleLoad.setAttribute ("LoadType", "UNROUTED");
				}
				if (bIsFirstStop)
				{
					eleLoadStop.setAttribute ("StopType", "O");
					bIsFirstStop = false;
				}
				else if (bIsLastStop)
				{
					eleLoadStop.setAttribute ("StopType", "D");
				}
				else
					eleLoadStop.setAttribute ("StopType", "I");
			}
		}
		try {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to manageLoad API:");
				System.out.println (eleLoads.getString());
			}
			api.manageLoad (env, YFCDocument.getDocumentFor (eleLoads.getString()).getDocument());
				
		} catch (Exception e) {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Exception in manageLoad");
				e.printStackTrace (System.out);
				throw e;
			}
		}	
	}
	
	protected	void createLoadsVer2 (YFSEnvironment env, YFCElement eleOrder, YFCElement eleDeliveryPlanShipments) throws Exception
	{
		YFCElement	eleLanes = eleOrder.getChildElement ("Loads");
		Iterator<?>	iLanes = eleLanes.getChildren ();
		String		sOrderLineKey = eleOrder.getAttribute ("OrderKey");
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();

		YFCDocument	docLoads = YFCDocument.createDocument ("Loads");
		YFCElement	eleLoads = docLoads.getDocumentElement ();
		YFCElement	eleLoad = eleLoads.createChild ("Load");
		YFCElement	eleLoadStops = eleLoad.createChild ("LoadStops");
		int			iSequenceNo = 1;
		
		// loads in this input document represent a lane.  We need to move the lanes into load stops
		if (iLanes != null)
		{
			while (iLanes.hasNext ())
			{
				YFCElement	eleLane = (YFCElement)iLanes.next();
				eleLoad.setAttribute ("EnterpriseCode", eleOrder.getAttribute ("EnterpriseCode"));
				eleLoad.setAttribute ("CreateOneLoadPerShipment", "N");
		
			// get the lane shipments element
				YFCElement	eleLaneShipments = eleLane.getChildElement ("LoadShipments");
				YFCElement	eleLaneShipment = eleLaneShipments.getChildElement ("LoadShipment");
				String		sShipmentKey = eleLaneShipment.getAttribute ("ShipmentKey");
			
				// add the load shipments element if it does not already exist
				YFCElement	eleLoadShipments = eleLoad.getChildElement ("LoadShipments");
				YFCElement	eleShipment;
				if (eleLoadShipments == null)
				{
					eleLoadShipments = eleLoad.createChild ("LoadShipments");
					YFCElement	eleLoadShipment = eleLoadShipments.createChild ("LoadShipment");
					eleLoadShipment.setAttribute ("ShipmentKey", sShipmentKey);
					eleShipment = getDeliveryPlanShipment (sShipmentKey, eleDeliveryPlanShipments);
				}
				else
					eleShipment = getDeliveryPlanShipment (sShipmentKey, eleDeliveryPlanShipments);
				
				// get the source and destination for the shipment
				String		sShipNode = eleShipment.getAttribute ("ShipNode");
				String		sReceivingNode = eleShipment.getAttribute ("ReceivingNode");
				YFCElement	eleLaneStops = eleLane.getChildElement ("LoadStops");
				Iterator<?>	iLaneStops = eleLaneStops.getChildren ();

				// loop over the lane stops
				while (iLaneStops.hasNext ())
				{
					YFCElement	eleLaneStop = (YFCElement)iLaneStops.next();

					// get load stop if it already exits
					YFCElement	eleIntermediateStop = getStopOnLoad (eleLoadStops, eleLaneStop);

					// if a this lane stop is already on the load stops
					if (eleIntermediateStop != null)
					{
						// this is an existing origin, intermediate or destination stop
						String	sExpectedDepartureDate = eleIntermediateStop.getAttribute ("ExpectedDepartureDate");
						String	sExpectedArrivalDate = eleIntermediateStop.getAttribute ("ExpectedArrivalDate");
				
						// add departure/arrival date form lane stop date to existing load stop
						if (YFCObject.isVoid(sExpectedDepartureDate) && !YFCObject.isVoid (eleLaneStop.getAttribute ("ExpectedDepartureDate")))
							eleIntermediateStop.setDateAttribute ("ExpectedDepartureDate", getExpectedDateFromLoad (eleLaneStop.getAttribute ("ExpectedDepartureDate")));
						if (YFCObject.isVoid(sExpectedArrivalDate) && !YFCObject.isVoid(eleLaneStop.getAttribute ("ExpectedArrivalDate")))
							eleIntermediateStop.setDateAttribute ("ExpectedArrivalDate", getExpectedDateFromLoad (eleLaneStop.getAttribute ("ExpectedArrivalDate")));
					}
					else
					{
	
						YFCElement	eleLoadStop = eleLoadStops.createChild ("LoadStop");
						boolean		bIsOriginNode = false;
					
						// this is a new orgin, intermediate, or destination stop
						if (!YFCObject.isVoid (eleLaneStop.getAttribute ("ExpectedDepartureDate")))
							eleLoadStop.setDateAttribute ("ExpectedDepartureDate", getExpectedDateFromLoad (eleLaneStop.getAttribute ("ExpectedDepartureDate")));
						if (!YFCObject.isVoid (eleLaneStop.getAttribute ("ExpectedArrivalDate")))
							eleLoadStop.setDateAttribute ("ExpectedArrivalDate", getExpectedDateFromLoad (eleLaneStop.getAttribute ("ExpectedArrivalDate")));

						String		sRouteID = eleLaneStop.getAttribute ("RouteID");
						if (eleLaneStop.getAttribute ("StopNode").equals (sShipNode))
						{
							eleLoadStop.setAttribute ("StopType", "O");
							bIsOriginNode = true;
						}
						else if (eleLaneStop.getAttribute ("StopNode").equals (sReceivingNode))
						{
							eleLoadStop.setAttribute ("StopType", "D");
						}
						else
						{
							eleLoadStop.setAttribute ("StopType", "I");
							eleLoadStop.setIntAttribute ("StopSequenceNo", iSequenceNo++);
						}
						eleLoadStop.setAttribute ("StopNode", eleLaneStop.getAttribute ("StopNode"));
				
						if (!YFCObject.isVoid(sRouteID) && YFCObject.isVoid (eleLoad.getAttribute ("LoadType")))
						{
							if (sRouteID.equalsIgnoreCase ("Unrouted"))
								eleLoad.setAttribute ("LoadType", "UNROUTED");
						}
						String	sCarrier = eleLaneStop.getAttribute ("Carrier");
						if (!YFCObject.isVoid(sCarrier) && bIsOriginNode)
						{
							String	sSCACAndService = "";

							if (sCarrier.equalsIgnoreCase ("Direct Air"))
								sSCACAndService = "TT13";
							else if (sCarrier.equalsIgnoreCase ("Commercial Air"))
								sSCACAndService = "CA11";
							else if (sCarrier.equalsIgnoreCase ("Organic Air"))
								sSCACAndService = "OA11";
							else if (sCarrier.equalsIgnoreCase ("Theater Air"))
								sSCACAndService = "TA11";
							else if (sCarrier.equalsIgnoreCase ("Commercial Ship"))
								sSCACAndService = "CS12";
							else if (sCarrier.equalsIgnoreCase ("Organic Ship"))
								sSCACAndService = "OS12";
							else if (sCarrier.equalsIgnoreCase ("Theater Truck"))
								sSCACAndService = "TT13";
							eleLoad.setAttribute ("ScacAndService", sSCACAndService);
						}
					}
				}
			}
		}
		try {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to manageLoad API:");
				System.out.println (eleLoads.getString());
			}
			api.manageLoad (env, YFCDocument.getDocumentFor (eleLoads.getString()).getDocument());
				
		} catch (Exception e) {
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Exception in manageLoad");
				e.printStackTrace (System.out);
				throw e;
			}
		}	
	}

	protected	YFCDate	getExpectedDateFromLoad (String sDateTime)
	{
		YFCDate	dtTodaysDate = new YFCDate (false);
		YFCDate	dtExpectedDate;
		
		try {
			dtExpectedDate = new YFCDate(sDateTime, dtFmtTmsDateFormat, true);
		} catch (Exception e) {
			dtExpectedDate = dtTodaysDate;
		}
		return dtExpectedDate;
	}
	
	protected	YFCElement	getDeliveryPlanShipment (String sShipmentKey, YFCElement eleDeliveryPlanShipments)
	{
		Iterator<?>	iDeliveryPlanShipments = eleDeliveryPlanShipments.getChildren();
		while (iDeliveryPlanShipments.hasNext ())
		{
			YFCElement	eleDeliveryPlanShipment = (YFCElement) iDeliveryPlanShipments.next();
			if (sShipmentKey.equals (eleDeliveryPlanShipment.getAttribute ("ShipmentKey")))
				return eleDeliveryPlanShipment;
		}
		return null;
	}

	protected	YFCElement	getStopOnLoad (YFCElement eleLoadStops, YFCElement eleStop)
	{
		Iterator<?>	iLoadStops = eleLoadStops.getChildren();
		while (iLoadStops.hasNext ())
		{
			YFCElement	eleLoadStop = (YFCElement)iLoadStops.next ();
			if (eleLoadStop.getAttribute ("StopNode").equals (eleStop.getAttribute ("StopNode")))
				return eleLoadStop;
		}
		return null;
	}	
	
	protected	void getShipmentLineDetailsForOrderLine (YFSEnvironment env, YFCElement eleShipment, String sOrderLineKey) throws Exception
	{
		YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
		YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
		eleOrderLineStatusList.setAttribute ("OrderLineKey", sOrderLineKey);
		String	sOrderReleaseKey = "";
		YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
						
		// get getOrderLineStatusList for the given order line	
		docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
		eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
		YFCElement	eleOrderStatus = eleOrderLineStatusList.getChildElement ("OrderStatus");
				
		// if order line status exits
		if (eleOrderStatus != null)
		{
			YFCElement	eleOrderStatusTranQuantity = eleOrderStatus.getChildElement ("OrderStatusTranQuantity");
			
			// create a shipment line element			
			YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
			if (eleShipmentLines == null)
				eleShipmentLines = eleShipment.createChild ("ShipmentLines");

			// add the shipment line child elemtn to the shipment lines element
			YFCElement	eleShipmentLine = eleShipmentLines.createChild ("ShipmentLine");
			eleShipmentLine.setAttribute ("Action", "Create");
			eleShipmentLine.setAttribute ("OrderLineKey", sOrderLineKey);
			eleShipmentLine.setAttribute ("OrderHeaderKey", eleOrderStatus.getAttribute ("OrderHeaderKey"));
			eleShipmentLine.setAttribute ("OrderReleaseKey", eleOrderStatus.getAttribute ("OrderReleaseKey"));
			eleShipmentLine.setAttribute ("Quantity", eleOrderStatusTranQuantity.getAttribute ("StatusQty"));
			eleShipmentLine.setAttribute ("UnitOfMeasure", eleOrderStatusTranQuantity.getAttribute ("TransactionalUOM"));
		}
	}


	// CODE TO SUPPORT JEROME'S XSLT Tranform Process - OBSOLETE
	/*
	public Document	getOrderReleaseKeysForShipments (YFSEnvironment env, Document docIn) throws Exception
	{
		try {
			YFCDocument	docOptimizedPlan = YFCDocument.getDocumentFor (docIn);
			YFCElement	eleMultiApi = docOptimizedPlan.getDocumentElement ();
			
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Entering getOrderReleaseKeysForShipments");
				System.out.println ("Input XML=");
				System.out.println (docOptimizedPlan.getString());
			}

			Iterator	iApis = eleMultiApi.getChildren ();
			if (iApis!= null)
			{
				while (iApis.hasNext ())
				{
					YFCElement	eleApi = (YFCElement)iApis.next();
					String	sApiName = eleApi.getAttribute ("Name");
					if (sApiName.equals ("createShipment"))
					{
						YFCElement	eleInput = eleApi.getChildElement ("Input");
						YFCElement	eleShipment = eleInput.getChildElement ("Shipment");
						addOrderReleaseKeysForShipment (env, eleShipment);
					}
				}
			}
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Exiting getOrderReleaseKeysForShipments");
				System.out.println ("Output XML=");
				System.out.println (docOptimizedPlan.getString());
			}
			return docOptimizedPlan.getDocument();
			
		} catch (Exception e) {
			throw e;
		}
	}

	protected	void	addOrderReleaseKeysForShipment (YFSEnvironment env, YFCElement eleShipment) throws Exception
	{
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Entering addOrderReleaseKeysForShipment");
			System.out.println ("Shipment XML=");
			System.out.println (eleShipment.getString());
		}

		YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
		Iterator	iShipmentLines = eleShipmentLines.getChildren ();
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi ();		

		
		// iterate over all shipment lines in the given shipment		
		while (iShipmentLines.hasNext ())
		{
			YFCElement	eleShipmentLine	 = (YFCElement)iShipmentLines.next ();
			String		sOrderLineKey = eleShipmentLine.getAttribute ("OrderLineKey");
			String		sOrderReleaseKey = null;

			// if shipment line has order line key associated
			if (sOrderLineKey != null)
			{
				YFCDocument	docOrderLineStatusList = YFCDocument.createDocument ("OrderLineStatus");
				YFCElement	eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
				eleOrderLineStatusList.setAttribute ("OrderLineKey", sOrderLineKey);
				
				// get getOrderLineStatusList for the given order line	
				docOrderLineStatusList = YFCDocument.getDocumentFor (api.getOrderLineStatusList (env, docOrderLineStatusList.getDocument()));
				eleOrderLineStatusList = docOrderLineStatusList.getDocumentElement ();
				YFCElement	eleOrderStatus = eleOrderLineStatusList.getChildElement ("OrderStatus");
				
				// if order line status exits
				if (eleOrderStatus != null)
				{
					// get order release key
					sOrderReleaseKey = eleOrderStatus.getAttribute ("OrderReleaseKey");		
				}
			}
			// if we found an order release key
			if (sOrderReleaseKey != null)
			{
				eleShipmentLine.setAttribute ("OrderReleaseKey", sOrderReleaseKey);
				YFCElement	eleOrderReleases = eleShipment.getChildElement ("OrderReleases");
				if (eleOrderReleases != null)
				{
					Iterator	iOrderReleases = eleOrderReleases.getChildren ();
					while (iOrderReleases.hasNext ())
					{
						YFCElement	eleOrderRelease = (YFCElement)iOrderReleases.next();
						eleOrderRelease.setAttribute ("OrderReleaseKey", sOrderReleaseKey);
					}
				}
				if (YFSUtil.getDebug ())
				{
					System.out.println ("OrderReleaseKey = " + sOrderReleaseKey + " found for OrderLineKey = " + sOrderLineKey);
				}
			}
			else
			{
				if (YFSUtil.getDebug ())
				{
					System.out.println ("WARNING - No Order Release Found for OrderLineKey="+sOrderLineKey);
				}
			}
		}
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Exiting addOrderReleaseKeysForShipment");
			System.out.println ("Output Shipment XML=");
			System.out.println (eleShipment.getString());
		}
	}	
	*/
	// END OF CODE TO SUPPORT JEROME'S XSLT Tranform Process - OBSOLETE
	
	
	protected	void	exportOrderLine (PrintWriter out, String sOrderReleaseKey, YFCElement eleOrderLineDetail) throws Exception
	{
		YFCElement	eleOrder = eleOrderLineDetail.getChildElement ("Order");
		YFCElement	eleItem = eleOrderLineDetail.getChildElement ("Item");
		Hashtable<String, String>	htOrderLineReferences = getOrderLineReferences (eleOrderLineDetail);

		String	sLineType = getExportField (eleOrderLineDetail.getAttribute ("LineType"), false);
		String 	sItemID = getExportField (eleItem.getAttribute ("ItemID"), false);
		String	sQty = getExportField (eleOrderLineDetail.getAttribute ("OrderedQty"), false);
		String	sPassengers = "0";
		String	sOrderType = eleOrder.getAttribute ("OrderType");
		boolean	bAdjustDate = false;
				
		if (sOrderType.equals ("TPFDD"))
		{
			sOrderType = sLineType;
			if (sOrderType.equals ("TPFDD_CRGO"))
				sPassengers = getExportField(htOrderLineReferences.get ("EXPECTED_SQFT"),false);
					
			// for PAX orders we want to set all dates to three days before the actual date so as to ensure
			// people arrive some number of days prior to cargo.  This adjustment is configurable by passing
			// an argument named AdjustPaxDays to the exportOrders custom API.  The value should represent
			// the number of days +/- to adjust the date fields
			else if (sOrderType.equals ("TPFDD_PAX"))
			{
				bAdjustDate = true;
				sPassengers = sQty;
			}
		}								
		else if (sItemID.equalsIgnoreCase ("PASSENGER"))
		{
			sPassengers = sQty;
			sQty = "";
		}
		
		// Line Format:
		// ID|Type|FromID|ToID|Quantity1(weight)|Quantity2(volume)|Quantity3(passengers)|PickAvl|DropAvl|PickBy|DropBy|ProductQty|ProductType|OrderNo|POE|POD|Available2Load|EarliestArrival|LatestArrival|Priority|TransCode|Nsn|NsnName|ccc|sqft|TPFDDMode'
		String	sLineData = 
				getExportField (eleOrderLineDetail.getAttribute ("OrderLineKey")) +			// ID
				getExportField (sOrderType) +													// TYPE
				getExportField (eleOrderLineDetail.getAttribute ("ShipNode")) + 				// FROM ID
				getExportField (eleOrderLineDetail.getAttribute ("ReceivingNode")) +			// TOID
				getExportField (htOrderLineReferences.get ("EXPECTED_WEIGHT")) + 				// WEIGHT (Quantity 1)
				getExportField (htOrderLineReferences.get ("EXPECTED_VOLUME")) +				// VOLUME (Quantity 2)
				getExportField (sPassengers) +													// PASSENGERS/SQFT (Quantity 3)
				getExportDateField (htOrderLineReferences.get ("PICK_AVAIL"), bAdjustDate) +	// Pick Available Date
				getExportDateField (htOrderLineReferences.get ("DROP_AVAIL"), bAdjustDate) +	// Drop Available Date
				getExportDateField (htOrderLineReferences.get ("PICK_BY"), bAdjustDate) + 		// Pick By Date
				getExportDateField (htOrderLineReferences.get ("DROP_BY"), bAdjustDate) +		// Drop By Date
				getExportField (eleOrderLineDetail.getAttribute ("OrderedQty")) + 				// Product Quantity Ordered	
				getExportField (htOrderLineReferences.get ("PRODUCT_TYPE")) +					// Product Type
				getExportField (eleOrder.getAttribute ("OrderNo")) +							// Order No		 	 
				getExportField (htOrderLineReferences.get ("POE")) + 							// POE
				getExportField (htOrderLineReferences.get ("POD")) +							// POD
				getExportDateField (htOrderLineReferences.get ("AVAIL_TO_LOAD"), bAdjustDate) +// Available to Load
				getExportDateField (htOrderLineReferences.get ("EARLIEST_ARRIVAL"), bAdjustDate) +	// Earliest Arrival
				getExportDateField (htOrderLineReferences.get ("LATEST_ARRIVAL"), bAdjustDate) +	// Latest Arrival
				getExportField (htOrderLineReferences.get ("PRIORITY")) + 						// Priority
				getExportField (htOrderLineReferences.get ("TRANSCODE")) + 					// Trans Code
				getExportField (eleItem.getAttribute ("ItemID")) +								// NSN
				getExportField (eleItem.getAttribute ("ItemShortDesc")) + 						// NSN Name
				getExportField (htOrderLineReferences.get ("CCC")) + 							// CCC					 
				getExportField (htOrderLineReferences.get ("EXPECTED_SQFT")) + 				// SQFT
				getExportField (htOrderLineReferences.get ("MODE"), false);	 					// TPDFF_MODE

		out.println (sLineData);
	}
	
	private	String		getExportDateField (Object sValue, boolean bAdjustDate)
	{
		return	getExportDateField (sValue, bAdjustDate, true);
	}
	
	private	String		getExportDateField (Object sValue, boolean bAdjustDate, boolean bPipeDelimit)
	{
		String	sDateValue =  (String)sValue;

		// if we're to adjust dates do so
		if (bAdjustDate && sDateValue != null & sDateValue.length() > 0)
		{
			YFCDate	dtDateValue = new YFCDate (sDateValue, dtFmtTmsDateFormat, true);
			try {
				int	iAdjustDays = Integer.parseInt (getServiceParameter ("AdjustPaxDays", null, "-3"));
				dtDateValue.changeDate (iAdjustDays);
				sDateValue = getExportField (dtDateValue.getString (dtFmtTmsDateFormat), bPipeDelimit);
			} catch (Exception ignore) {
			}
		}
		else
			sDateValue = getExportField (sValue, bPipeDelimit);
		return sDateValue;		
	}

	private String		getExportField (Object sValue)
	{
		return getExportField (sValue, true);
	}	

	private String		getExportField (Object sValue, boolean bPipeDelimit)
	{
		if (sValue == null)
			sValue = "";

		// if values were defaulted on the order (i.e. not provided by order DB)
		if (((String)sValue).equalsIgnoreCase ("NO-NSN"))
			sValue = "";			
		else if (((String)sValue).startsWith("No NSN_NAME"))
			sValue = "";
		else if (((String)sValue).equalsIgnoreCase ("PASSENGER"))
			sValue = "";
		else if (((String)sValue).startsWith("Passenger"))
			sValue = "";
		return (String)sValue + ((bPipeDelimit) ? "|" : "");
	}	

	private	Hashtable<String, String>	getOrderLineReferences (YFCElement eleOrderLineDetail)
	{
		YFCElement	eleReferences = eleOrderLineDetail.getChildElement ("References");
		Hashtable<String, String>	htOut = new Hashtable<String, String>();
		
		if (eleReferences != null)
		{
			Iterator<?>	iReferences = eleReferences.getChildren ();
			while (iReferences.hasNext())
			{
				YFCElement	eleReference = (YFCElement)iReferences.next();
				htOut.put (eleReference.getAttribute ("Name"), eleReference.getAttribute ("Value"));
			}
		}
		YFCElement	eleDates = eleOrderLineDetail.getChildElement ("OrderDates");
		if (eleDates != null)
		{
			Iterator<?>	iDates = eleDates.getChildren ();
			while (iDates.hasNext())
			{
				YFCElement	eleDate = (YFCElement)iDates.next();
				YFCDate		dtExpectedDate = eleDate.getDateAttribute ("ExpectedDate");
				
				if (dtExpectedDate != null)
					htOut.put (eleDate.getAttribute ("DateTypeId"), dtExpectedDate.getString (dtFmtTmsDateFormat));
			}
		}
		return htOut;
	}

	protected String getServiceParameter (String strAttrName, Document docIn, String strDefault) throws Exception
	{
		String	strAttrValue = getServiceParameter (strAttrName, docIn);
		
		if (strAttrValue == null || strAttrValue.length() == 0)
			strAttrValue = strDefault;
		return strAttrValue;
	}

	protected String getServiceParameter (String strAttrName, Document docIn) throws Exception
	{
		String strAttrValue = null;
		Properties			mProps = getProperties();
				
		if (mProps != null && mProps.containsKey(strAttrName))
			strAttrValue = evaluateXPathExpression ((String)mProps.getProperty(strAttrName), docIn);
		
		if (strAttrValue == null && docIn != null)
		{
			YFCDocument	docServiceInput = YFCDocument.getDocumentFor (docIn);
			YFCElement	eleServiceInput = docServiceInput.getDocumentElement();
			strAttrValue = eleServiceInput.getAttribute (strAttrName);
		}
		return strAttrValue;
	}
	
	protected String evaluateXPathExpression (String sXPathExpr, Document inDoc) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:") && inDoc != null)
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, inDoc, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;			
		}
		return sResult;
	}	

	protected String getSqlFileName (Document docIn, String sSuffix) throws Exception
	{
		String	sFileName = getSqlFileName (docIn);
		if (sFileName != null)
		{
			int iSqlExtension = sFileName.indexOf (".sql");
			if (iSqlExtension >= 0)
				sFileName = sFileName.substring (0, iSqlExtension) + sSuffix + ".sql";
		}
		return sFileName;
	}
	
	protected String getSqlFileName (Document docIn) throws Exception
	{
		String sFileName = getServiceParameter ("Query", docIn);
		if (!(sFileName.startsWith ("\\") || sFileName.startsWith ("/") || sFileName.indexOf(":") >= 0))
		{
			String sYFSHome = System.getProperty ("YFS_HOME");
			if (sYFSHome != null)
				sFileName = sYFSHome + File.separator + sFileName;
		}
		return sFileName;	
	}

	protected	File	getOutboundDirectory ()
	{
		return new File (System.getProperty ("YFS_HOME")+ File.separator + "data" + File.separator + "outbound");
	}
	
	protected	Properties	getProperties ()
	{
		return m_Properties;
	}

	private java.sql.Connection m_con;
	private PreparedStatement	m_stmtHeader;
	private PreparedStatement	m_stmtDetail;
	private ResultSet			m_rsHeader;
	private ResultSet			m_rsDetail;	
	private Properties m_Properties;
}

