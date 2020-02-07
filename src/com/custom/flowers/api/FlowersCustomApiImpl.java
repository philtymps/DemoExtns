/**
  * FlowersCustomApiImpl.java
  *
  **/

// PACKAGE
package com.custom.flowers.api;

import com.custom.yantra.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfc.dom.*;

import org.w3c.dom.*;
import java.util.*;

public class FlowersCustomApiImpl implements YIFCustomApi
{
    public FlowersCustomApiImpl()
    {
    }
	
	public	Document getOrderReleaseListByPersonInfo(YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOrderReleaseList = null;
		try {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to getOrderReleaseListByPersonInfo() API is: ");
				System.out.println (YFSXMLUtil.getXMLString (docIn));
			}
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			docOrderReleaseList = filterReleaseListByPersonInfo (docIn, api.getOrderReleaseList (env, docIn));

			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from getOrderReleaseListByPersonInfo() API is: ");
				System.out.println (YFSXMLUtil.getXMLString (docOrderReleaseList));
			}
			
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return docOrderReleaseList;
	}
	
	@SuppressWarnings("rawtypes")
	public	Document	filterReleaseListByPersonInfo (Document	docIn, Document docOut)
	{
		YFCDocument docOrderReleaseList = YFCDocument.getDocumentFor (docOut);
		YFCElement	elePersonInfoShipTo = YFCDocument.getDocumentFor (docIn).getDocumentElement().getChildElement ("PersonInfoShipTo");

		if (docOrderReleaseList != null && elePersonInfoShipTo != null)
		{
			YFCElement	eleOrderReleaseList	= docOrderReleaseList.getDocumentElement();
			Iterator	iOrderReleases = eleOrderReleaseList.getChildren();
			// find any element that doesn't match the person info record passed in and remove it from the list
			while (iOrderReleases.hasNext ())
			{
				YFCElement	eleOrderRelease = (YFCElement)iOrderReleases.next ();
				YFCElement	elePersonInfoShipToCompare = eleOrderRelease.getChildElement ("PersonInfoShipTo");

				if (!comparePersonInfo(elePersonInfoShipTo, elePersonInfoShipToCompare))
				{
					eleOrderReleaseList.removeChild (eleOrderRelease);
				}
			}
		}
		return docOrderReleaseList.getDocument();
	}
	
	private	boolean	comparePersonInfo (YFCElement elePersonInfo, YFCElement elePersonInfoToCompare)
	{
		NamedNodeMap	mapPersonInfo = elePersonInfo.getDOMNode ().getAttributes ();
		int				iMax = mapPersonInfo.getLength();

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Comparing PersonInfoElements");
			System.out.println ("Comparing PersonInfoElement1");
			System.out.println (elePersonInfo.getString());
			System.out.println ("Comparing PersonInfoElement2");
			System.out.println (elePersonInfoToCompare.getString());
		}		
		for (int iMap = 0; iMap < iMax; iMap++)
		{
			String	sKey = mapPersonInfo.item(iMap).getNodeName();
			if (YFSUtil.getDebug ())
				System.out.println ("Comparing Key: " + sKey);
			
			if (!elePersonInfo.getAttribute (sKey).equals (elePersonInfoToCompare.getAttribute (sKey)))
				return false;
		}
		if (YFSUtil.getDebug ())
			System.out.println ("PersonInfo Elements Match");	
		return true;
	}
	
	
	@SuppressWarnings("rawtypes")
	public	Document createShipmentForDropShip(YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOutput = null;
		try{
			YFCDocument	docDropShipment = YFCDocument.getDocumentFor (docIn);
			
			if (YFSUtil.getDebug ())
			{
				System.out.print ("XML Input to createShipmentsForDropShip");
				System.out.println (docDropShipment.getString());
				docOutput = docDropShipment.getDocument();
			}
			YFCElement	eleDropShipment = docDropShipment.getDocumentElement ();
			YFCElement	eleOrderReleases = eleDropShipment.getChildElement ("OrderReleases");
			if (eleOrderReleases != null)
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				Iterator	iOrderReleases = eleOrderReleases.getChildren();
				while (iOrderReleases.hasNext ())
				{
					YFCDocument	docShipment = YFCDocument.createDocument ("Shipment");
					YFCElement	eleShipment = docShipment.getDocumentElement();
					eleShipment.setAttribute ("Action", "Create");
					eleShipment.setAttribute ("IgnoreOrdering", "Y");
					eleOrderReleases = eleShipment.createChild ("OrderReleases");
					YFCElement	eleOrderRelease = eleOrderReleases.createChild ("OrderRelease");
					YFCElement	eleDropShipRelease = (YFCElement)iOrderReleases.next();
					eleOrderRelease.setAttribute ("OrderReleaseKey", eleDropShipRelease.getAttribute ("OrderReleaseKey"));
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Input to createShipment API:");
						System.out.println (docShipment.getString());
					}
					// create the shipment
					docShipment = YFCDocument.getDocumentFor (api.createShipment (env, docShipment.getDocument()));
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Output from createShipment API:");
						System.out.println (docShipment.getString());
					}	
					eleShipment = docShipment.getDocumentElement ();
					eleDropShipRelease.setAttribute ("ShipmentKey", eleShipment.getAttribute ("ShipmentKey"));
				}
				docOutput = docDropShipment.getDocument();
			}
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}		
		return docOutput;
	}
	
	@SuppressWarnings("rawtypes")
	public	Document confirmShipmentForDropShip(YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOutput = null;
		try{
			YFCDocument	docDropShipment = YFCDocument.getDocumentFor (docIn);
			
			if (YFSUtil.getDebug ())
			{
				System.out.print ("XML Input to confirmShipmentsForDropShip");
				System.out.println (docDropShipment.getString());
				docOutput = docDropShipment.getDocument();
			}
			YFCElement	eleDropShipment = docDropShipment.getDocumentElement ();
			YFCElement	eleOrderShipments = eleDropShipment.getChildElement ("OrderShipments");
			if (eleOrderShipments != null)
			{
				YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
				Iterator	iOrderShipments = eleOrderShipments.getChildren();
				while (iOrderShipments.hasNext ())
				{
					YFCElement	eleDropShipShipment = (YFCElement)iOrderShipments.next();
					YFCDocument	docShipment = YFCDocument.createDocument ("Shipment");
					YFCElement	eleShipment = docShipment.getDocumentElement();
					eleShipment.setAttribute ("ShipComplete", "Y");
					eleShipment.setAttribute ("ShipmentKey", eleDropShipShipment.getAttribute ("ShipmentKey"));
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Input to confirmShipment API:");
						System.out.println (docShipment.getString());
					}
					// create the shipment
					docShipment = YFCDocument.getDocumentFor (api.confirmShipment (env, docShipment.getDocument()));
					if (YFSUtil.getDebug ())
					{
						System.out.println ("Output from createShipment API:");
						System.out.println (docShipment.getString());
					}	
					eleShipment = docShipment.getDocumentElement ();
					eleDropShipShipment.setAttribute ("ShipmentKey", eleShipment.getAttribute ("ShipmentKey"));

				}
				docOutput = docDropShipment.getDocument();
			}
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}		
		return docOutput;
	}

	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
	}
	
	public Properties getProperties() {
		return mProp;
	}

	private Properties mProp;
}

