package com.custom.diab.demos.api;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;

public class SERebalanceInventory implements YIFCustomApi {

    public void SEReblanceInventory()
    {
    }

	
	public Document rebalanceInventory (YFSEnvironment env, Document docIn) throws Exception
	{
        YFCDocument docItem = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleItem = docItem.getDocumentElement();
		String		sDefault;


		// use XML Input to establish the API properties		
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG1Min")))
			m_props.setProperty ("SEG1Min", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG1Max")))
			m_props.setProperty ("SEG1Max", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG2Min")))
			m_props.setProperty ("SEG2Min", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG2Max")))
			m_props.setProperty ("SEG2Max", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG1Name")))
			m_props.setProperty ("SEG1Name", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("SEG2Name")))
			m_props.setProperty ("SEG2Name", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("DistributionRuleId")))
			m_props.setProperty ("DistributionRuleId", sDefault);
		if (!YFCObject.isNull (sDefault= eleItem.getAttribute ("IgnoreSoftDemands")))
			m_props.setProperty ("IgnoreSoftDemands", sDefault);
			
		// rebalance the item inventory			
		rebalanceInventoryItem (env, docItem.getDocumentElement());
		return docIn;
	}
	
	public Document rebalanceInventoryMonitor (YFSEnvironment env, Document docIn) throws Exception
	{
        YFCDocument docEventXML = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleEventXML = docEventXML.getDocumentElement ();
		YFCElement	eleItem = eleEventXML.getChildElement ("Item");
		YFCElement	eleInventoryItem = eleEventXML.getChildElement("InventoryItem");

		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to rebalanceInventoryMonitor API");
			System.out.println (docEventXML.getString());
		}

		// use the inventory organization from the Event XML if passed
		if (!YFCObject.isNull(eleInventoryItem))
			eleItem.setAttribute("OrganizationCode", eleInventoryItem.getAttribute("InventoryOrganizationCode"));
					
		rebalanceInventoryItem (env, eleItem);
		return docIn;
	}

    private void rebalanceInventoryItem (YFSEnvironment env, YFCElement eleItem) throws Exception
	{		
		YFCDocument docSupplyDetails = YFCDocument.createDocument ("getSupplyDetails");
		YFCElement	eleSupplyDetails = docSupplyDetails.getDocumentElement ();
		
		
		eleSupplyDetails.setAttribute ("OrganizationCode", eleItem.getAttribute ("OrganizationCode"));
		eleSupplyDetails.setAttribute ("ItemID", eleItem.getAttribute ("ItemID"));
		eleSupplyDetails.setAttribute ("ProductClass", eleItem.getAttribute ("ProductClass"));
		eleSupplyDetails.setAttribute ("UnitOfMeasure", eleItem.getAttribute ("UnitOfMeasure"));
		String	sDistributionRuleId = m_props.getProperty ("DistributionRuleId");
		String	sConsiderAllNodes;
		if (YFCObject.isNull (sDistributionRuleId))
		{
			sConsiderAllNodes = "Y";
			sDistributionRuleId = "";
		}
		else
			sConsiderAllNodes = "N";
			
		eleSupplyDetails.setAttribute ("DistributionRuleId", sDistributionRuleId);	
		eleSupplyDetails.setAttribute ("ConsiderAllNodes", sConsiderAllNodes);
		eleSupplyDetails.setAttribute ("ConsiderAllSegments", "Y");
		eleSupplyDetails.setAttribute ("ConsiderUnassignedDemand", "Y");
		
		YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Input to getSupplyDetails API");
			System.out.println (docSupplyDetails.getString());
		}
		// call the getSupplyDetails API
		Document docOut = api.getSupplyDetails (env, docSupplyDetails.getDocument());
		docSupplyDetails = YFCDocument.getDocumentFor (docOut);
		eleSupplyDetails = docSupplyDetails.getDocumentElement ();
		if (YFSUtil.getDebug ())
		{
			System.out.println ("Output from getSupplyDetails API");
			System.out.println (docSupplyDetails.getString());
		}

		// put the inventory organization into the eleSupplyDetails top-level element
		eleSupplyDetails.setAttribute ("OrganizationCode", eleItem.getAttribute ("OrganizationCode"));

		// do the rebalancing if needed
		doRebalanceInventory (env, eleSupplyDetails);
	}

	private	void doRebalanceInventory (YFSEnvironment env, YFCElement eleGetSupplyDetails) throws Exception
	{
		YFCElement	eleShipNodes = eleGetSupplyDetails.getChildElement ("ShipNodes");
		Iterator<?>	iShipNodes = eleShipNodes.getChildren();
	
		// initialize totals
		int		iTotalOnHand = 0;
		int		iTotalAvailable = 0;
		int		iTotalSEG2Available = 0;
		int		iTotalSEG1Available = 0;
		int		iSEG1Min = DEFAULT_SEG1MIN;
		int		iSEG1Max = DEFAULT_SEG1MAX;
		int		iSEG2Min = DEFAULT_SEG2MIN;
		int		iSEG2Max = DEFAULT_SEG2MAX;
		
		boolean	bIgnoreSoftDemands = true;
		boolean	bSegmentsToRebalanceFound = false;

		String	sSEG1Min = m_props.getProperty ("SEG1Min");
		String	sSEG1Max = m_props.getProperty ("SEG1Max");
		String	sSEG2Min = m_props.getProperty ("SEG2Min");
		String	sSEG2Max = m_props.getProperty ("SEG2Max");
		String  sSEG1Name = m_props.getProperty ("SEG1Name");
		String	sSEG2Name = m_props.getProperty ("SEG2Name");
		

		String	sIgnoreSoftDemands = m_props.getProperty ("IgnoreSoftDemands");
		bIgnoreSoftDemands = !YFCObject.isNull(sIgnoreSoftDemands) && sIgnoreSoftDemands.toUpperCase().startsWith("Y");
				
		if (!YFCObject.isNull (sSEG1Min))
			iSEG1Min = Integer.parseInt (sSEG1Min);
		else
			m_props.setProperty ("SEG1Min", Integer.toString(iSEG1Min));

		if (!YFCObject.isNull (sSEG1Max))
			iSEG1Max = Integer.parseInt (sSEG1Max);
		else
			m_props.setProperty ("SEG1Max", Integer.toString(iSEG1Max));

		if (!YFCObject.isNull (sSEG2Min))
			iSEG2Min = Integer.parseInt (sSEG2Min);
		else
			m_props.setProperty ("SEG2Min", Integer.toString(iSEG2Min));

		if (!YFCObject.isNull (sSEG2Max))
			iSEG1Max = Integer.parseInt (sSEG2Max);
		else
			m_props.setProperty ("SEG2Max", Integer.toString(iSEG2Max));
			
		if (!YFCObject.isNull(sSEG1Name)) {
		} else
			m_props.setProperty ("SEG1Name", sSEG1Name);

		if (!YFCObject.isNull(sSEG2Name)) {
		} else
			m_props.setProperty ("SEG2Name", sSEG2Name);
		
		
		// iterate over each ship node
		while (iShipNodes.hasNext ())
		{
			YFCElement	eleShipNode = (YFCElement)iShipNodes.next();
			YFCElement	eleSupplies = eleShipNode.getChildElement ("Supplies");
			Iterator<?>	iSupplies = eleSupplies.getChildren();
			
			// iterate over each supply
			while (iSupplies.hasNext ())
			{
				YFCElement	eleSupply = (YFCElement)iSupplies.next();
				String		sSupplyType = eleSupply.getAttribute ("SupplyType");
			
				// if OHHAND SupplyType (i.e. we're only looking for onhand availability/quantity)
				if (sSupplyType.equalsIgnoreCase ("ONHAND"))
				{
					// increment total available and total onhand
					iTotalAvailable += new BigDecimal (eleSupply.getDoubleAttribute ("AvailableQty")).intValue();
					iTotalOnHand    += new BigDecimal (eleSupply.getDoubleAttribute ("TotalQuantity")).intValue();
	
					Iterator<?>	iSupplyDetails = eleSupply.getChildren();

					// iterate over each segment of supply (i.e. each supply detail element)
					while (iSupplyDetails.hasNext ())
					{
						YFCElement	eleSupplyDetails = (YFCElement)iSupplyDetails.next();
						YFCElement	eleExactMatchedDemands = eleSupplyDetails.getChildElement ("ExactMatchedDemands");
						YFCElement	eleSoftMatchedDemands = eleSupplyDetails.getChildElement ("SoftMatchedDemands");
						
						String		sSegment = eleSupplyDetails.getAttribute ("Segment");
						sSupplyType = eleSupplyDetails.getAttribute ("SupplyType");

						// if the supply type is ONHAND					
						// IMPORTANT: you must either put SEG2 and SEG1 inventory in it's own segment
						if (sSupplyType.equalsIgnoreCase ("ONHAND"))
						{
							// determine any hard or soft demands in this supply
							int	iDemand = 0;
							if (eleExactMatchedDemands != null)
							{
								iDemand = new BigDecimal (eleExactMatchedDemands.getDoubleAttribute ("TotalQuantity")).intValue();
							}
							if (eleSoftMatchedDemands != null && !bIgnoreSoftDemands)
							{
								iDemand += new BigDecimal (eleSoftMatchedDemands.getDoubleAttribute ("TotalQuantity")).intValue();
							}
							// increment total segment on hand
							if (sSegment.equalsIgnoreCase (sSEG1Name))
							{
								// increment total SEG1 on hand
								iTotalSEG1Available += new BigDecimal (eleSupplyDetails.getDoubleAttribute ("Quantity")).intValue() - iDemand;
								bSegmentsToRebalanceFound = true;
							}
							else if (sSegment.equalsIgnoreCase (sSEG2Name))
							{
								// increment total SEG2 on hand
								iTotalSEG2Available += new BigDecimal (eleSupplyDetails.getDoubleAttribute ("Quantity")).intValue() - iDemand;
								bSegmentsToRebalanceFound = true;
							}
						}
					}
				}
				// set the established totals back into the passed element to simplify logic if rebalancing required
				eleGetSupplyDetails.setAttribute ("ShipNode", eleShipNode.getAttribute ("ShipNode"));
				eleGetSupplyDetails.setIntAttribute ("TotalOnHand", iTotalOnHand);
				eleGetSupplyDetails.setIntAttribute ("TotalAvailable", iTotalAvailable);
				eleGetSupplyDetails.setIntAttribute ("TotalSEG2Available", iTotalSEG2Available);
				eleGetSupplyDetails.setIntAttribute ("TotalSEG1Available", iTotalSEG1Available);
				if (YFSUtil.getDebug ())
				{
					System.out.println ("Availability Details:");
					System.out.println ("TotalOnHand=" + eleGetSupplyDetails.getAttribute ("TotalOnHand"));
					System.out.println ("TotalAvailable=" + eleGetSupplyDetails.getAttribute ("TotalAvailable"));
					System.out.println ("SEG1Min=" + iSEG1Min);
					System.out.println ("SEG2Min=" + iSEG2Min);
					System.out.println ("TotalSEG2Available=" + eleGetSupplyDetails.getAttribute ("TotalSEG2Available"));
					System.out.println ("TotalSEG1Available=" + eleGetSupplyDetails.getAttribute ("TotalSEG1Available"));
				}
				// if rebalancing should be performed based on Mins
				if (bSegmentsToRebalanceFound && ((iTotalSEG1Available < iSEG1Min && iTotalSEG2Available > iSEG2Min) || (iTotalSEG2Available < iSEG2Min && iTotalSEG1Available > iSEG1Min)))	
					rebalanceInventory (env, eleGetSupplyDetails, iSEG1Min, iSEG1Max, iSEG2Min, iSEG2Max);
			}
		}
	}

	public	void rebalanceInventory (YFSEnvironment env, YFCElement eleSupplyDetails, int iSEG1Min, int iSEG1Max, int iSEG2Min, int iSEG2Max) throws Exception
	{
			int		iTotalSEG2Available = eleSupplyDetails.getIntAttribute ("TotalSEG2Available");
		int		iTotalSEG1Available = eleSupplyDetails.getIntAttribute ("TotalSEG1Available");
		boolean	bRebalanceRequired = false;
		
		YFCDocument	docItems = YFCDocument.createDocument ("Items");
		YFCElement	eleItems = docItems.getDocumentElement ();
		
		YFCElement	eleItemPrimary = eleItems.createChild ("Item");
		eleItemPrimary.setAttribute ("AdjustmentType", "ADJUSTMENT");
		eleItemPrimary.setAttribute ("Adjustment", "TRACK");
		eleItemPrimary.setAttribute ("SupplyType", "ONHAND");
		eleItemPrimary.setAttribute ("ShipNode", eleSupplyDetails.getAttribute ("ShipNode"));
		eleItemPrimary.setAttribute ("ItemID", eleSupplyDetails.getAttribute ("ItemID"));
		eleItemPrimary.setAttribute ("OrganizationCode", eleSupplyDetails.getAttribute ("OrganizationCode"));
		eleItemPrimary.setAttribute ("ProductClass", eleSupplyDetails.getAttribute ("ProductClass"));
		eleItemPrimary.setAttribute ("UnitOfMeasure", eleSupplyDetails.getAttribute ("UnitOfMeasure"));

		YFCElement	eleItemSecondary = eleItems.createChild ("Item");
		eleItemSecondary.setAttribute ("AdjustmentType", "ADJUSTMENT");
		eleItemSecondary.setAttribute ("Adjustment", "TRACK");
		eleItemSecondary.setAttribute ("SupplyType", "ONHAND");
		eleItemSecondary.setAttribute ("ShipNode", eleSupplyDetails.getAttribute ("ShipNode"));
		eleItemSecondary.setAttribute ("ItemID", eleSupplyDetails.getAttribute ("ItemID"));
		eleItemSecondary.setAttribute ("OrganizationCode", eleSupplyDetails.getAttribute ("OrganizationCode"));
		eleItemSecondary.setAttribute ("ProductClass", eleSupplyDetails.getAttribute ("ProductClass"));
		eleItemSecondary.setAttribute ("UnitOfMeasure", eleSupplyDetails.getAttribute ("UnitOfMeasure"));
		
		// if we're below the SEG2 minimum and we have available SEG1 inventory
		if (iTotalSEG2Available < iSEG2Min && iTotalSEG1Available > iSEG1Min)
		{
			// compute amount to move based on fill quantity passed
			int iSEG1Adjust = Math.min(iTotalSEG1Available, iSEG1Max);
			if (iTotalSEG1Available - iSEG1Adjust < iSEG1Min)
				iSEG1Adjust -= iSEG1Min - (iTotalSEG1Available - iSEG1Adjust);
			// steal from SEG1 Available and move to SEG2
			eleItemPrimary.setAttribute ("Segment", m_props.getProperty("SEG1Name"));
			eleItemPrimary.setIntAttribute ("Quantity", -iSEG1Adjust);
			eleItemSecondary.setAttribute ("Segment", m_props.getProperty("SEG2Name"));
			eleItemSecondary.setIntAttribute ("Quantity", iSEG1Adjust);
			bRebalanceRequired = true;
		}
		// else if we're below the SEG1 minimum and we have available SEG2 inventory
		else if (iTotalSEG1Available < iSEG1Min && iTotalSEG2Available > iSEG2Min)
		{
			int iSEG2Adjust = Math.min(iTotalSEG2Available, iSEG2Max);
			if (iTotalSEG2Available - iSEG2Adjust < iSEG2Min)
				iSEG2Adjust -= iSEG2Min - (iTotalSEG2Available - iSEG2Adjust);
			eleItemPrimary.setAttribute ("Segment", m_props.getProperty ("SEG2Name"));
			eleItemPrimary.setIntAttribute ("Quantity", -iSEG2Adjust);
			eleItemSecondary.setAttribute ("Segment", m_props.getProperty("SEG1Name"));
			eleItemSecondary.setIntAttribute ("Quantity", iSEG2Adjust);
			bRebalanceRequired = true;
		}	

		if (bRebalanceRequired)
		{		
			YIFApi api = YIFClientFactory.getInstance().getLocalApi ();
			if (YFSUtil.getDebug ())
			{
				System.out.println ("********** REBALANCE OF INVENTORY SEGMENTS BEING PERFOMRED ************");
				System.out.println ("Input to adjustInventory API");
				System.out.println (docItems.getString());
			}
			// rebalance the inventory for the given item
			api.adjustInventory (env, docItems.getDocument());		
		}
	}	
		
	// default values for min/max
	private	static int	DEFAULT_SEG1MIN = 3;
	private static int	DEFAULT_SEG1MAX = 15;
	private static int	DEFAULT_SEG2MIN = 3;
	private static int	DEFAULT_SEG2MAX = 10;
	@SuppressWarnings("unused")
	private static String DEFAULT_SEG1NAME = "FTL";
	@SuppressWarnings("unused")
	private static String DEFAULT_SEG2NAME = "DTC";

	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_props = props;
	}

	private	Properties	m_props;
}
