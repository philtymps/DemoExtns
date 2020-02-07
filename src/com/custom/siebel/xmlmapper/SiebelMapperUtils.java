/**
  * SiebelMapperUtils.java
  *
  **/

// PACKAGE
package com.custom.siebel.xmlmapper;

import java.math.*;
import com.custom.yantra.util.*;
import org.w3c.dom.*;
import com.custom.yantra.xmlmapper.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.custom.yantra.customer.*;

public class SiebelMapperUtils extends YantraMapperUtils
{
    public SiebelMapperUtils()
    {
		super();
    }

	public static void setDefaultOrgCode (String sDefaultOrgCode)
	{
		strDefaultOrgCode =	sDefaultOrgCode;
	}
	
	public	static String getDefaultOrgCode ()
	{
		return strDefaultOrgCode;
	}
	
	public static	String	computeDependencyShippingRule (String strDeliverTogether, String strShipComplete)
	{	
		String strDependancyShippingRule = "";

		// if input is "Yes, true" for Deliver Together
		if (isFlagTrue (strDeliverTogether))
			strDependancyShippingRule = "02";
		else if (isFlagTrue (strShipComplete))
			strDependancyShippingRule = "01";

		return strDependancyShippingRule;
	}
	
	@SuppressWarnings("unused")
	public static String computeOrganizationCode (String sOrganizationId, String sOrganizationIntegrationId)
	{
		String	strOrgCode;
		
		// if organization integration ID passed, use that
		if (sOrganizationIntegrationId.length() > 0)
			strOrgCode = sOrganizationIntegrationId;
		// else if organization code passed, use that
		else if (sOrganizationId.length() > 0)
			strOrgCode = sOrganizationId;

		setOrganizationId (sOrganizationId);
		
		// return strOrgCode;
		return getDefaultOrgCode();
	}

	public static String computeBuyerOrganizationCode (String sBuyerId, String sBuyerIntegrationId)
	{
		String	strOrgCode = getDefaultOrgCode();
		
		// if organization integration ID passed, use that
		if (sBuyerIntegrationId.length() > 0)
			strOrgCode = sBuyerIntegrationId;
		// else if organization code passed, use that
		else if (sBuyerId.length() > 0)
			strOrgCode = sBuyerId;

		// store off original buyer id
		setOrganizationId (sBuyerId);

		// if Catepillar Inc. (standard mfg demo)
		if (getDefaultOrgCode().equalsIgnoreCase ("DEFAULT"))
		{
			if (sBuyerId.equals ("1-3OPGU"))
				strOrgCode = "CAT_HYD";
		}
		else
			strOrgCode = "HTC";
	
		return strOrgCode;
	}

	public static String computeSellerOrganizationCode (String strSellerOrganization)
	{
		String	strOrgCode = getDefaultOrgCode();
				
		// return strOrgCode;
		return strOrgCode;
	}
	
	public static String computeShipToZipCode (YFSEnvironment env, String strShipToAccountId, String strShipToAccountIntegrationId) throws Exception
	{
		String			sBuyerOrgCode = computeBuyerOrganizationCode (strShipToAccountId, strShipToAccountIntegrationId);
		// use Billing information to compute Ship To Address
		return (getBuyerDetails(env, sBuyerOrgCode).getBTZip());
	}

	public static String computeShipToCountry (YFSEnvironment env, String strShipToAccountId, String strShipToAccountIntegrationId) throws Exception
	{
		String			sBuyerOrgCode = computeBuyerOrganizationCode (strShipToAccountId, strShipToAccountIntegrationId);
		// use Billing information to compute Ship To Address
		return (getBuyerDetails(env, sBuyerOrgCode).getBTCountry());
	}

	public static Customer getBuyerDetails (YFSEnvironment env, String sBuyerOrgCode) throws Exception
	{		
		if (oCustomer == null || !oCustomer.getBuyerOrganizationCode ().equals(sBuyerOrgCode))
		{
			YFSEnvironment	oldEnv = YFSUtil.pushYFSEnv();
			try {
				YFSUtil.setYFSEnv(env);
				oCustomer = new Customer();
				oCustomer.getBuyerDetails(getDefaultOrgCode(), sBuyerOrgCode);
			} catch (Exception e) {
				oCustomer.getCustomerDetails(getDefaultOrgCode(),sBuyerOrgCode);
			} finally {
				YFSUtil.popYFSEnv (oldEnv);
			}
		}
		return (oCustomer);
	}	
	public static String computeOrderType (String strOrderType, String strOrderTypeId, boolean bIn)
	{
		String strRetType = null;
		
		if (bIn)
		{
			strRetType = "";
			if (strOrderType.equalsIgnoreCase ("Sales Order"))
			{
				strRetType = "Standard";
				if (getDefaultOrgCode().equals ("CME"))
					strRetType = "TRIPLAY";
			}
		}
		else
		{
			strRetType = "Sales Order";
			if (strOrderType.equalsIgnoreCase ("Standard"))
				strRetType = "Sales Order";
		}
		return strRetType;		
	}

	public static String computeOrderStatus (String strOrderStatusId, String strOrderStatusDesc)
	{
        String strRetStatus = strOrderStatusDesc;
		if (strOrderStatusDesc != null) 
 		{
			strRetStatus = strOrderStatusDesc;
			if (strRetStatus.equals("Draft Order Created"))
				strRetStatus = "Created in Back Office";
			else if (strRetStatus.equalsIgnoreCase("Partially Installation Authorized") || strRetStatus.equalsIgnoreCase("Cable Install Authorized") || strRetStatus.equalsIgnoreCase("DSL Install Authorized")) {
				strRetStatus="Install Authorized";
  			} else if (strRetStatus.equalsIgnoreCase("Partially Awaiting DSL Install") || strRetStatus.equalsIgnoreCase("Awaiting DSL Inst Auth")) {
				strRetStatus="Awaiting DSL Install"; 
			} else if (strRetStatus.equalsIgnoreCase("Awaiting Install Authorization")) {
				strRetStatus="Awaiting Install Auth"; 
			} 
		}
		return strRetStatus;
	}

	public static String computeCountry(String strCountry, boolean bIn)
	{
		String	strRet = strCountry;
		if (bIn)
		{
			if (strCountry.equalsIgnoreCase("USA"))
				strRet = "US";
		}
		else
		{
			if (strCountry.equalsIgnoreCase ("US"))
				strRet = "USA";
		}	
		return strRet;
	}
	
	public static String computeItemID (int idx, String strRowId[], String strIntegrationId[], String strPartNumber[], String strProductId[], String strProductIntegrationId[])
	{
		if (strProductIntegrationId[idx].length() > 0) {
		} else {
		}

		setIntegrationId (idx, strIntegrationId[idx]);
		setRowId (idx, strRowId[idx]);
		setPartNumber (idx, strPartNumber[idx]);			
		setProductId (idx, strProductId[idx]);
		setProductIntegrationId (idx, strProductIntegrationId[idx]);
		
		//		return strItemID;			
		return strPartNumber[idx].length() > 0 ? strPartNumber[idx] : "11443";
	}	
	
	public static String computeQty (String sQty)
	{
		BigDecimal	bdQty = new BigDecimal (sQty);		
		return bdQty.toBigInteger ().toString();
	}
	
	public static String computeUOM (String strUOM, boolean bIn)
	{
		// convert to uppercase UOM
		String	strRet = strUOM;
		if (bIn)
		{
			strRet = strUOM.toUpperCase();
		}
		else
		{
			if (strUOM.equalsIgnoreCase ("EACH"))
				strRet = "Each";
		}
		return strRet;
	}
		
	public static String getIntegrationId (int idx)
	{
		return strIntegrationIds[idx];
	}

	public static void setIntegrationId (int idx, String strIntegrationId)
	{
		if (strIntegrationIds == null)
			strIntegrationIds = new String[100];
		strIntegrationIds[idx] = strIntegrationId;
	}

	public static String getProductId (int idx)
	{
		return strProductIds[idx];
	}

	public static void setProductId (int idx, String strProductId)
	{
		if (strProductIds == null)
			strProductIds = new String[100];
		strProductIds[idx] = strProductId;
	}

	public static String getPartNumber (int idx)
	{
		return strPartNums[idx];
	}

	public static void setPartNumber (int idx, String strPartNum)
	{
		if (strPartNums == null)
			strPartNums = new String[100];
		strPartNums[idx] = strPartNum;
	}

	public static String getProductIntegrationId (int idx)
	{
		return strProductIntegrationIds[idx];
	}

	public static void setProductIntegrationId (int idx, String strProductIntegrationId)
	{
		if (strProductIntegrationIds == null)
			strProductIntegrationIds = new String[100];
		strProductIntegrationIds[idx] = strProductIntegrationId;
	}

	public static String getRowId (int idx)
	{
		return strRowIds[idx];
	}

	public static void setRowId (int idx, String strRowId)
	{
		if (strRowIds == null)
			strRowIds = new String[100];
		strRowIds[idx] = strRowId;
	}

	public static String getOrganizationId ()
	{
		return strOrganizationId;
	}

	public static void setOrganizationId (String strOrgId)
	{
		strOrganizationId = strOrgId;
	}

	public static String getOriginalDocAttribute (String strElement, String strAttribute) throws Exception
	{
		Document	docOriginal = SiebelXMLMapper.getSiebelXML();
		
		if (docOriginal != null)
		{
			String	strOriginalDocument = YFSXMLUtil.getXMLString (docOriginal);
			return (YFSXMLUtil.getAttrValue (YFSXMLUtil.getXMLElement (strOriginalDocument, "<"+strElement+">"),strAttribute));
		}
		else
			return "";
	}		

	public static String getOriginalDocElement (String strElement) throws Exception
	{
		Document	docOriginal = SiebelXMLMapper.getSiebelXML();
		
		if (docOriginal != null)
		{
			String	strOriginalDocument = YFSXMLUtil.getXMLString (docOriginal);
			return (YFSXMLUtil.getXMLField (strOriginalDocument, "<"+strElement+">"));
		}
		else
			return "";
	}		

	public static String getOriginalDocElement (String strGroupingElement, String strRepeatElement, int idx, String strElement) throws Exception
	{
		Document	docOriginal = SiebelXMLMapper.getSiebelXML();

		// if original document found		
		if (docOriginal != null)
		{
			String	strOriginalDocument = YFSXMLUtil.getXMLString (docOriginal);
			String	strGroupElements = YFSXMLUtil.getXMLField (strOriginalDocument, "<"+strGroupingElement+">");
			if (YFSUtil.getDebug())
			{
				System.out.println ("Group Elements:");
				System.out.println (strGroupElements);
			}
			String	strRepElement = null;
				
			// iterate until grouped element at index reached
			for (int i = 0; i <= idx; i++)
			{
				// get first/next element in group of elements
				String strGroupElement = YFSXMLUtil.getXMLElement (strGroupElements, "<"+strRepeatElement+">");
				strRepElement = null;
				
				if (YFSUtil.getDebug())
				{
					System.out.println ("Group Element");
					System.out.println (strGroupElement);
				}
				
				// if next element in group found
				if (strGroupElement != null)
				{
					strRepElement = YFSXMLUtil.getXMLField (strGroupElement, "<"+strElement+">");
					if(YFSUtil.getDebug ())
					{
						System.out.println ("'"+strElement+"'"+" Element at Index +"+idx);
						System.out.println (strRepElement);		
					}
				}
				if (strRepElement == null)
					break;

				// remove current group element and iterate to next element
				strGroupElements = strGroupElements.substring (strGroupElement.length());
			}
			if (strRepElement == null)
				strRepElement = "";
			return strRepElement;	
		}
		else
			return "";
	}		

	public static String initPromiseLineCounter (String sLineId)
	{
		iPromiseLineCounter = Integer.parseInt ("1");
		return sLineId;
	}	

	public static String getPromiseLineCounter ()
	{
		return new Integer (iPromiseLineCounter).toString();
	}
	
	public static String incPromiseLineCounter()
	{
		return new Integer (iPromiseLineCounter++).toString();
	}
	
	
	private static boolean isFlagTrue(String strFlag)
	{
		boolean	bRet = false;

		// if flag is not empty		
		if (strFlag.length () > 0)
			// look for 'Y', 'y', true, or Yes
			bRet = (strFlag.equalsIgnoreCase ("Y") || Boolean.valueOf (strFlag).booleanValue());
		return bRet;
	}
	
	public static Document	removeSOAPEnvelope(Document docIn) throws Exception
	{
		String			sDoc  = YFSXMLUtil.getXMLString (docIn);
		return removeSOAPEnvelope (sDoc);
	}
	
	public static Document	removeSOAPEnvelope(String sDoc) throws Exception
	{
		String			sBody = YFSXMLUtil.getXMLField (sDoc, "<SOAP-ENV:Body>");
		YFSXMLParser	docNew = null;
		
		if (sBody != null && sBody.length() > 0)
		{
			if (YFSUtil.getDebug())
			{
				System.out.println ("SOAP Message Received:");
				System.out.println (sDoc);
				System.out.println ("\r\n");
				System.out.println ("\r\nStripping SOAP Envelope-Resulting Document:");
				System.out.println (sBody);
				System.out.println ("\r\n");
			}
			docNew = new YFSXMLParser (strXMLHeader+sBody, false);
		}
		return docNew.getDocument();	
	}
	
	public static Document addSOAPEnvelope (Document docOut) throws Exception
	{
		String	sDoc  = YFSXMLUtil.getXMLString (docOut);
		String	sBody = sDoc.substring (sDoc.indexOf ("?>")+2);
		YFSXMLParser	docNew = null;
				
		if (sBody != null && sBody.length() > 0)
		{
			docNew = new YFSXMLParser (strSOAPHeader+sBody+strSOAPFooter, false);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Adding SOAP Envelope-Resulting Message");
				System.out.println (YFSXMLUtil.getXMLString (docNew.getDocument()));
			}
		}
		return docNew.getDocument();
	}


	private	static String	strSOAPHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><SOAP-ENV:Body>";
	private	static String	strSOAPFooter = "</SOAP-ENV:Body></SOAP-ENV:Envelope>";
	private	static String	strXMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private	static String	strDefaultOrgCode = "DEFAULT";
	private static String[] strRowIds;
	private static String[] strIntegrationIds;
	private static String[] strProductIds;
	private static String[] strPartNums;
	private static String[] strProductIntegrationIds;
	private static String	strOrganizationId;	
	private static int		iPromiseLineCounter;
	private	static Customer	oCustomer = null;
}

