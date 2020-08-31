/**
  * SEDynamicConditions.java.java
  *
  **/

package com.custom.diab.demos.conditions;

import com.custom.yantra.util.*;
import com.yantra.ycp.japi.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.*;

import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;

public class SEDynamicConditionsImpl implements YCPDynamicCondition, YCPDynamicConditionEx
{
    public SEDynamicConditionsImpl()
    {
    }
	
	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document xmlData)
	{
		try {
			return evaluateCondition (env, name, mapData, YFSXMLUtil.getXMLString (xmlData));
		} catch (Exception ignore) {
			return false;
		}
	}
	

  	@SuppressWarnings("rawtypes")
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, String xmlData)
	{
		boolean	bRet = name.toUpperCase().startsWith ("NOT");
		
		try {	
			String	sDebug = (String)m_props.get("Debug");
			
			boolean	bDebug = !YFCObject.isNull(sDebug) && sDebug.toUpperCase().startsWith("Y");
			if (bDebug)
			{					
				System.out.println ("\nSE Dynamic Condition Executing");
				System.out.println ("name="+name);
				System.out.println ("xmlData="+xmlData);
				Iterator	iProps = m_props.keySet().iterator();
				System.out.println ("Condition Properties:");
				while (iProps.hasNext())
				{
						String	sPropName = (String)iProps.next();
						System.out.println (sPropName+ "\t\t" + m_props.get(sPropName));
				}
			}

			// test condition names
			if (name.startsWith ("Is Insurance Invalid") || name.startsWith("Is Insurance Valid"))
			{
				bRet = name.toUpperCase().contains("INVALID");
				YFCDocument	docOrder = YFCDocument.getDocumentFor (xmlData);
				/*
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument	docOrderOutputTemplate = YFCDocument.getDocumentFor ("<Order BuyerOrganizationCode=\"\" OrderHeaderKey=\"\" SourceIPAddress=\"\"/>");
				env.setApiTemplate ("getOrderDetails", docOrderOutputTemplate.getDocument());
				docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
				env.clearApiTemplate ("getOrderDetails");
				*/
				//String	sIPAddress = docOrder.getDocumentElement().getAttribute ("SourceIPAddress");
				String	sOrderName = docOrder.getDocumentElement().getAttribute("OrderName");
				
				// Look for orders with OrderName="Insurance Validation Failure..."
				if (!sOrderName.startsWith ("Insurance Validation Failure"))
					bRet ^= true;
			}
			if (name.startsWith ("Is SIM Enabled"))
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument	docGetProperty = YFCDocument.getDocumentFor("<Property Category=\"yfs\" PropertyName=\"yfs.sim.enabled\"/>");
				docGetProperty = YFCDocument.getDocumentFor(api.getProperty(env,  docGetProperty.getDocument()));
				String sPropertyValue = docGetProperty.getDocumentElement().getAttribute("PropertyValue");
				return (docGetProperty.getDocumentElement().getBooleanAttribute("PropertyValue") || sPropertyValue.equalsIgnoreCase("Y")); 
			}
			if (name.startsWith ("Is Monitor Event Organization Child Of"))
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				String		sParentOrganizationCode = (String)m_props.get("ParentOrganizationCode");
				String		sXpathExpr = (String)m_props.get("ChildOrganizationCode");				
				String		sChildOrganizationCode = evaluateXPathExpression (sXpathExpr, YFCDocument.getDocumentFor (xmlData).getDocument());

				YFCDocument	docOrganizationHierarchy = YFCDocument.getDocumentFor ("<Organization OrganizationCode=\"" + sParentOrganizationCode + "\"/>");
				docOrganizationHierarchy = YFCDocument.getDocumentFor(api.getOrganizationHierarchy(env, docOrganizationHierarchy.getDocument()));

				YFCElement eleSubOrganizationHierarchy = docOrganizationHierarchy.getDocumentElement().getChildElement("SubOrganization");
				bRet = isChildOfOrganization (eleSubOrganizationHierarchy, sChildOrganizationCode);				
			}
			if (name.startsWith ("Is Activation Required") || name.startsWith ("Not Is Activation Required"))
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument	docOrder = YFCDocument.getDocumentFor (xmlData);
				YFCDocument	docOrderOutputTemplate = YFCDocument.getDocumentFor ("<Order BuyerOrganizationCode=\"\" OrderHeaderKey=\"\" SourceIPAddress=\"\"/>");
				env.setApiTemplate ("getOrderDetails", docOrderOutputTemplate.getDocument());
				docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
				env.clearApiTemplate ("getOrderDetails");
				String	sIPAddress = docOrder.getDocumentElement().getAttribute ("SourceIPAddress");
				String	sBuyerOrganizationCode = docOrder.getDocumentElement().getAttribute ("BuyerOrganizationCode");
				
				// B2C orders (orders without buyer organization) that are not from 1.1.1.2 IP address require validation
				if (YFCObject.isVoid (sBuyerOrganizationCode) && !sIPAddress.equals("1.1.1.2"))
					bRet ^= true;
			}	
			if (name.startsWith ("Is Tier") || name.startsWith ("Not Is Tier"))
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				YFCDocument	docXMLData = YFCDocument.getDocumentFor (xmlData);
				YFCElement	eleXMLData = docXMLData.getDocumentElement();
				
				YFCDocument docOrder = YFCDocument.createDocument("Order");
				YFCElement	eleOrder = docOrder.getDocumentElement();
				eleOrder.setAttribute("OrderHeaderKey", eleXMLData.getAttribute("OrderHeaderKey"));
				
				YFCDocument	docOrderOutputTemplate = YFCDocument.getDocumentFor ("<Order BillToID=\"\" OrderType=\"\" EnterpriseCode=\"\"/>");
				env.setApiTemplate ("getOrderDetails", docOrderOutputTemplate.getDocument());
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getOrderDetails:");
					System.out.println (docOrder.getString());
				}
				docOrder = YFCDocument.getDocumentFor (api.getOrderDetails (env, docOrder.getDocument()));
				eleOrder = docOrder.getDocumentElement();
				env.clearApiTemplate ("getOrderDetails");
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getOrderDetails:");
					System.out.println (docOrder.getString());
				}
				String	sEnterpriseCode = eleOrder.getAttribute ("EnterpriseCode");
				String	sCustomerID = eleOrder.getAttribute("BillToID");
				

				YFCDocument	docCustomer = YFCDocument.createDocument("Customer");
				YFCElement	eleCustomer = docCustomer.getDocumentElement();
				
				YFCDocument	docCustomerOutputTemplate = YFCDocument.getDocumentFor ("<Customer CustomerID=\"\" CustomerLevel=\"\" />");
				eleCustomer.setAttribute("OrganizationCode", sEnterpriseCode);
				eleCustomer.setAttribute("CustomerID", sCustomerID);
				env.setApiTemplate ("getCustomerDetails", docCustomerOutputTemplate.getDocument());
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getCustomerDetails:");
					System.out.println (docCustomer.getString());
				}
				docCustomer = YFCDocument.getDocumentFor (api.getCustomerDetails (env, docCustomer.getDocument()));
				env.clearApiTemplate ("getCustomerDetails");
				eleCustomer = docCustomer.getDocumentElement();
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getCustomerDetails:");
					System.out.println (docCustomer.getString());
				}

				// get the values to test from Condition Args (OrderType, CustomerLevel)
				String sTestOrderType = (String) m_props.get("OrderType");
				String sTestCustomerLevel = (String) m_props.get ("CustomerLevel");

				// if OrderType=OrderType Arg Value and CustomerLevel = CustomerLevel Arg Value
				if (!YFCObject.isVoid(eleCustomer.getAttribute("CustomerLevel")) && !YFCObject.isVoid(eleOrder.getAttribute("OrderType")))
					if (sTestCustomerLevel.equals (eleCustomer.getAttribute("CustomerLevel"))
					&&  sTestOrderType.equals(eleOrder.getAttribute ("OrderType")))				
						bRet ^= true;
			}

			if (bDebug)
			{
				System.out.println ("SE Dynamic Condition Results");
				System.out.println ("bRet="+bRet);
			}			
		} catch (Exception e) {
			if (YFSUtil.getDebug())
				System.out.println ("Unexpected Exception in SEDynamicConditions"+"\r\nname="+name+"\r\n+message="+e.getMessage());
 			throw new RuntimeException("SEDynamicCondition Failed: "+"\r\nname="+name+"\r\n+message="+e.getMessage());
		}		
		return bRet;
	}	
	
	@SuppressWarnings("rawtypes")
	public void setProperties(Map props)
	{
			m_props = props;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean	isChildOfOrganization (YFCElement eleSubOrganization, String sChildOrganizationCode)
	{
		if (eleSubOrganization != null)
		{			
			Iterator	iSubOrganizations = eleSubOrganization.getChildren();
			while (iSubOrganizations.hasNext())
			{
				YFCElement	eleOrganization = (YFCElement)iSubOrganizations.next();
				
				String	sTestOrganizationCode = eleOrganization.getAttribute("OrganizationCode");
				if (!sChildOrganizationCode.equals(sTestOrganizationCode))
				{				
					eleSubOrganization = eleOrganization.getChildElement("SubOrganization");
					if (isChildOfOrganization(eleSubOrganization, sChildOrganizationCode))
						return true;
				}
				else
					return true;
			}
		}
		return false;
	}
	
	private String evaluateXPathExpression (String sXPathExpr, Document inDoc) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
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

	@SuppressWarnings("rawtypes")
	private Map	m_props;

}
