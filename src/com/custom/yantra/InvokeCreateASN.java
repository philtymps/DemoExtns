/**
  * InvokeCreateASN.java
  *
  **/

// PACKAGE
package com.custom.yantra;

import java.util.*;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.*;

import com.custom.yantra.util.*;

public class InvokeCreateASN
{
	public Document invokeCreateShipmentAPI(YFSEnvironment env, Document inXML) throws Exception
	{
		System.out.println( "input xml is ... " );
		System.out.println( YFSXMLUtil.getXMLString(inXML) );
		
		YFSXMLParser par = new YFSXMLParser(inXML);
		Element shipment = par.getRootElement();
		String orderHeaderKey = shipment.getAttribute("OrderHeaderKey");

		System.out.println( "Order Header Key : " + orderHeaderKey );

		Hashtable<String, String> order = new Hashtable<String, String>();
		order.put("OrderHeaderKey", orderHeaderKey);

		YFSXMLParser inPar = new YFSXMLParser();
		inPar.createRootElement("Order", order);

		YIFApi api = YFSUtil.getYIFApi();
		Document orderDetails = api.getOrderDetails(env, inPar.getDocument() );
		
		par.reset();
		par                            = new YFSXMLParser(orderDetails);
		Element ord               = par.getRootElement();
		NodeList lst                = ord.getElementsByTagName("OrderStatuses");
		Element ordStatuses   = (Element)lst.item(0);
		lst                              = ordStatuses.getElementsByTagName("OrderStatus");
		Element ordStatus        = (Element)lst.item(0);
		String orderReleaseKey = ordStatus.getAttribute("OrderReleaseKey");

		lst                                = ord.getElementsByTagName("Extn");
		Element extn                 = (Element)lst.item(0);
		String extnSupplierName = extn.getAttribute( "ExtnSupplierName" );
		//get Warehouse and other order extensions here
		//if WHSE is "YD1" or  "BD1" throw exception with appropriate error msg from here…

		inPar.reset();
		inPar = new YFSXMLParser();

		Hashtable<String, String> inShip = new Hashtable<String, String>();
		inShip.put("Action","Create");
		inShip.put("DocumentType","0005");
		inShip.put("IgnoreOrdering","Y");
		
		Hashtable<String, String> inRelease = new Hashtable<String, String>();
		inRelease.put("OrderReleaseKey", orderReleaseKey);
		inRelease.put("AssociationAction", "Add");

		Hashtable<String, String> inExtn = new Hashtable<String, String>();
		inExtn.put("ExtnSupplierName",extnSupplierName);
		//other extensions to be added here

		Element eShip = inPar.createRootElement("Shipment", inShip);

		Element eReleases = inPar.createChild(eShip, "OrderReleases", null);

		inPar.createChild(eReleases, "OrderRelease", inRelease);
		inPar.createChild(eShip,"Extn", inExtn);

		System.out.println( "Input for createShipment API is ... ");
		System.out.println( YFSXMLUtil.getXMLString(inPar.getDocument()) );

		Document outShip = api.createShipment(env, inPar.getDocument() );

		System.out.println( "Output for createShipment API is ... ");
		System.out.println( YFSXMLUtil.getXMLString(outShip));

		return outShip;

	}//invokeCreateASN
};


