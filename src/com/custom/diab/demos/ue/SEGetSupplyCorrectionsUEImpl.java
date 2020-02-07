/**
 * 
 */
package com.custom.diab.demos.ue;

import java.util.Iterator;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.INVGetSupplyCorrectionsUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCCommon;
/**
 * @author diab
 *
 */
public class SEGetSupplyCorrectionsUEImpl implements INVGetSupplyCorrectionsUE {

/*	
 * 
 * This UE has been implemented to enforce Allocations based on Order Source (e.g. Marketplace vs. Web Channels).
 * It leverages a CommonCode Table named DEMO_SUPPLY_ALLOC which has a list of Order Sources and a Maximum
 * allocation percentage that should be made available to that channel.
*/
	
	@Override
	public Document getSupplyCorrections (YFSEnvironment env, Document docIn) throws YFSUserExitException
	{
		YFCDocument	docItems = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleItems = docItems.getDocumentElement();
		String		sAuthorizedClient = null;
		String		sEnterpriseCode = null;
		String		sOrderName = null;
		double		dblMaxAllocationPercentage;
		
		if (YFSUtil.getDebug())
		{
				System.out.println ("In getSupplyCorrections UE - Input XML:");
				System.out.println (docItems.getString());
		}
		// first determine the source of the order using OrderReference
		String	sOrderHeaderKey = eleItems.getAttribute("OrderReference");
		
		// if no order header key is passed we're done
		if (YFCObject.isNull(sOrderHeaderKey))
			return docIn;
		
		try {

			YFCDocument	docOrderTemplate = YFCDocument.getDocumentFor ("<Order AuthorizedClient=\"\" EntryType=\"\" EnterpriseCode=\"\" OrderName=\"\" />");
			YFCDocument	docOrderInput = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\""+sOrderHeaderKey+"\"/>");
			env.setApiTemplate("getOrderDetails", docOrderTemplate.getDocument());
			if (YFSUtil.getDebug())
			{
				System.out.println ("getOrderDetails Input XML:");
				System.out.println (docOrderInput.getString());
			}

			// call get order details API
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
			YFCDocument	docOrder = YFCDocument.getDocumentFor (api.getOrderDetails(env, docOrderInput.getDocument()));

			if (YFSUtil.getDebug())
			{
				System.out.println ("getOrderDetails Output XML:");
				System.out.println (docOrder.getString());
			}

			sAuthorizedClient = docOrder.getDocumentElement().getAttribute("AuthorizedClient");
			sEnterpriseCode   = docOrder.getDocumentElement().getAttribute("EnterpriseCode");
			sOrderName        = docOrder.getDocumentElement().getAttribute("OrderName");
			
			// if no order source or order name found assume 100% allocation
			if (YFCObject.isNull(sAuthorizedClient) || YFCObject.isVoid(sAuthorizedClient) || YFCObject.isVoid(sOrderName))
				return docIn;
						
			if (sOrderName.startsWith("Channel Allocation"))
			{
				dblMaxAllocationPercentage = getMaxAllocationConfiguration (env, sEnterpriseCode, sAuthorizedClient);
				if (dblMaxAllocationPercentage < 1)
					getSupplyCorrectionsForChannelAllocationDemo (eleItems, dblMaxAllocationPercentage);
			}
			else if (sOrderName.startsWith("Batch Allocation"))
				getSupplyCorrectionsForBatchedInventoryDemo (env, eleItems, sOrderName);

		} catch (Exception e) {
			e.printStackTrace (System.out);
			throw new YFSUserExitException (e.getMessage())	;		

		} finally {
			env.clearApiTemplate("getOrderDetails");
		}
				
		if (YFSUtil.getDebug())
		{
				System.out.println ("In getSupplyCorrections UE - Output XML:");
				System.out.println (docItems.getString());
		}
		return docItems.getDocument();
	}
	

	@SuppressWarnings("rawtypes")
	private	void	getSupplyCorrectionsForChannelAllocationDemo (YFCElement eleItems, double dblMaxAllocationPercentage)
	{
		// we now have a buyer organization to work with
		Iterator	iItems = eleItems.getChildren();
		
		// assume we're going to limit ONHAND from the first node we pass
		while (iItems.hasNext())
		{
			YFCElement	eleItem = (YFCElement)iItems.next();
			
			Iterator	iSupplies = eleItem.getFirstChild().getChildren();
			while (iSupplies.hasNext())
			{
				YFCElement	eleSupply = (YFCElement)iSupplies.next();
								
				String		sSupplyType = eleSupply.getAttribute("SupplyType");

				// correct supply to reflect maximum allocation for the channel
				if (sSupplyType.equals("ONHAND"))
				{
					double	dblQty = eleSupply.getDoubleAttribute("Quantity");
					Double	dblMaxQty = dblQty * dblMaxAllocationPercentage;
					eleSupply.setIntAttribute ("Quantity", dblMaxQty.intValue());
				}
			}
		}
	}

	
	@SuppressWarnings("rawtypes")
	private	void	getSupplyCorrectionsForBatchedInventoryDemo (YFSEnvironment env, YFCElement eleItems, String sOrderName) throws Exception
	{
		// we now have a buyer organization to work with
		Iterator	iItems = eleItems.getChildren();
		String		sBatchToLookFor = sOrderName.substring(sOrderName.indexOf('=')+1).trim();


		try {
			// setup an API instance
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi();
			
			while (iItems.hasNext())
			{
				YFCElement	eleItem = (YFCElement)iItems.next();

				YFCDocument	docSupplyDetails = YFCDocument.createDocument("getSupplyDetails");
				YFCElement	eleSupplyDetails = docSupplyDetails.getDocumentElement();
				eleSupplyDetails.setAttribute("ItemID", eleItem.getAttribute("ItemID"));
				eleSupplyDetails.setAttribute("UnitOfMeasure", eleItem.getAttribute("UnitOfMeasure"));
				eleSupplyDetails.setAttribute("ProductClass", eleItem.getAttribute("ProductClass"));
				eleSupplyDetails.setAttribute("OrganizationCode", eleItem.getAttribute("OrganizationCode"));
				if (YFSUtil.getDebug())
				{
					System.out.println ("Input to getSupplyDetails:");
					System.out.println (eleSupplyDetails.getString());
				}
				eleSupplyDetails = YFCDocument.getDocumentFor(api.getSupplyDetails (env, docSupplyDetails.getDocument())).getDocumentElement();
				if (YFSUtil.getDebug())
				{
					System.out.println ("Output from getSupplyDetails:");
					System.out.println (eleSupplyDetails.getString());
				}

				Iterator	iSupplies = eleItem.getFirstChild().getChildren();
				while (iSupplies.hasNext())
				{
					YFCElement	eleSupply = (YFCElement)iSupplies.next();
									
					String		sSupplyType = eleSupply.getAttribute("SupplyType");
					String		sShipNode   = eleSupply.getAttribute("ShipNode");
					String		sQuantity  = eleSupply.getAttribute("Quantity");
					
					eleSupply.setDoubleAttribute("Quantity", getUseableSupply (eleSupplyDetails, sShipNode, sSupplyType, sQuantity, sBatchToLookFor));
				}
			}
			// assume we're going to limit ONHAND from the first node we pass
		} catch (Exception eException) {
			throw eException;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private double getUseableSupply (YFCElement eleCompleteSupplyDetails, String sShipNode, String sSupplyType, String sQuantity, String sBatchToLookFor)
	{
		double	dblUsableQty = 0;
		YFCElement	eleShipNodes = eleCompleteSupplyDetails.getChildElement("ShipNodes");
		Iterator	iShipNodes = eleShipNodes.getChildren();
		
		while (iShipNodes.hasNext())
		{
			YFCElement	eleShipNode = (YFCElement)iShipNodes.next();
			if (eleShipNode.getAttribute("ShipNode").equals(sShipNode))
			{
				YFCElement	eleSupplies = eleShipNode.getChildElement("Supplies");
				Iterator	iSupplies = eleSupplies.getChildren();
				while (iSupplies.hasNext())
				{
					YFCElement	eleSupply = (YFCElement)iSupplies.next();
					if (eleSupply.getAttribute("SupplyType").equals(sSupplyType))
					{
						YFCElement	eleSupplyDetails = eleSupply.getChildElement("SupplyDetails");
						if (!YFCObject.isVoid(eleSupplyDetails))
						{
							if (eleSupplyDetails.getAttribute("TagNumber").contains(sBatchToLookFor) && sQuantity.equals(eleSupplyDetails.getAttribute("Quantity")))
								dblUsableQty = dblUsableQty + eleSupplyDetails.getDoubleAttribute("Quantity");
						}
						else
							dblUsableQty = dblUsableQty + eleSupply.getDoubleAttribute("TotalQuantity");
					}
				}
			}
		}
		return dblUsableQty;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private double getMaxAllocationConfiguration (YFSEnvironment env, String sEnterpriseCode, String sAuthorizedClient) throws Exception
	{
	  	YFCDocument	docCommonCode = YFCDocument.createDocument ("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
		eleCommonCode.setAttribute ("CodeType", "DEMO_SUPPLY_ALLOC");
		eleCommonCode.setAttribute("CallingOrganizationCode", sEnterpriseCode);
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
		} catch (Exception e) {
			throw e;
		}
		// if the DEMO_SUPPLY_ALLOC common code table was found
		// Note Entries must be in the following format
		// CodeValue: Order Source (i.e. AuthorizedClient)
		// CodeShortDescription: Maximum Allocation Percentage
		// CodeLongDescription: Description
		
		if (eleCommonCodes != null)
		{
			Iterator	iCommonCodes = eleCommonCodes.getChildren();			
			while (iCommonCodes.hasNext())
			{	
				eleCommonCode = (YFCElement)iCommonCodes.next();
				if (YFCCommon.equals(sAuthorizedClient, eleCommonCode.getAttribute ("CodeValue")))
				 return eleCommonCode.getDoubleAttribute ("CodeShortDescription");
			}
		}
		return 1;
	}
}
