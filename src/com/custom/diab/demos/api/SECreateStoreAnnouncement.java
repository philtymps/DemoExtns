package com.custom.diab.demos.api;

import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YDate;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

@SuppressWarnings({ "unused", "deprecation" })
public class SECreateStoreAnnouncement implements YIFCustomApi {


	public Document createStoreAnnouncementXML (YFSEnvironment env, Document docIn)
	{
		YFCDocument docShipment = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleShipment = docShipment.getDocumentElement();
		YFCDocument	docAnnouncement = YFCDocument.createDocument ("Inbox");
		YFCElement	eleAnnouncement = docAnnouncement.getDocumentElement();
		
		
		String		sShipNode = eleShipment.getAttribute ("ShipNode");
		eleAnnouncement.setAttribute("ShipnodeKey", sShipNode);
		eleAnnouncement.setAttribute("QueueId", "YCD_ANNOUNCEMENT_"+sShipNode.toUpperCase());
		eleAnnouncement.setAttribute("Description", m_Props.getProperty("Description"));
		eleAnnouncement.setAttribute("DetailDescription", m_Props.getProperty("Detail"));
		if (YFCObject.isVoid(eleAnnouncement.getAttribute ("DetailDescription")))
			eleAnnouncement.setAttribute ("DetailedDescription", eleAnnouncement.getAttribute ("Description"));
		eleAnnouncement.setAttribute("ExceptionType", "YCD_ANNOUNCEMENT");
		eleAnnouncement.setAttribute ("ExpirationDays", m_Props.getProperty ("ExpirationDays"));
		if (YFCObject.isVoid(eleAnnouncement.getAttribute("ExpirationDays")))
				eleAnnouncement.setAttribute("ExpirationDays", "1");
		
		// calculate the ResolveByDate to be end of the current day
		int iExpirationDays = eleAnnouncement.getIntAttribute ("ExpirationDays");
		YFCDate	dtExpiresOnDate = eleShipment.getDateTimeAttribute("StatusDate");
		dtExpiresOnDate.changeDate(iExpirationDays);
		dtExpiresOnDate.setBeginOfDay();
		eleAnnouncement.setDateTimeAttribute ("ResolutionDate", dtExpiresOnDate);
		
		getReferences (eleShipment, eleAnnouncement);
				
		return docAnnouncement.getDocument();
	}
	
	@SuppressWarnings("rawtypes")
	private	void	getReferences (YFCElement eleShipment, YFCElement eleAnnouncement)
	{
		YFCElement	eleShipmentLines = eleShipment.getChildElement ("ShipmentLines");
		
		eleAnnouncement.setAttribute("EnterpriseCode", eleShipment.getAttribute("EnterpriseCode"));
		eleAnnouncement.setAttribute("ShipnodeKey", eleShipment.getAttribute ("ShipNode"));
		eleAnnouncement.setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
		eleAnnouncement.setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
		

		Iterator	iShipmentLines = eleShipmentLines.getChildren();
		if (iShipmentLines.hasNext())
		{
			YFCElement	eleShipmentLine = (YFCElement)iShipmentLines.next();
		 
			eleAnnouncement.setAttribute("OrderNo", eleShipmentLine.getAttribute ("OrderNo"));
			eleAnnouncement.setAttribute("OrderHeaderKey", eleShipmentLine.getAttribute ("OrderHeaderKey"));	
		}
		
	}
	
	
	public Document createStoreAnnouncement (YFSEnvironment env, Document docIn) throws YFSException
	{		
		YFCDocument	docAnnouncement = YFCDocument.getDocumentFor (createStoreAnnouncementXML(env, docIn));
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			YFCDocument	docShipment = YFCDocument.getDocumentFor (docIn);
			YFCElement	eleShipment = docShipment.getDocumentElement();
			YFCDocument	docChangeShipment = YFCDocument.createDocument ("Shipment");
			YFCElement	eleChangeShipment = docChangeShipment.getDocumentElement();
			String		sDocType = eleShipment.getAttribute("DocumentType");
			
			String	sAnnouncementFlag = m_Props.getProperty ("CreateStoreAnnouncement"); 
			if (!YFCObject.isVoid(sAnnouncementFlag) && !YFCObject.isNull(sAnnouncementFlag) && sAnnouncementFlag.equals("Y"))
			{
				api.createException(env, docAnnouncement.getDocument());
			}
			
			// compute the new expected shipment date to reflect the Pick By Date.
			YFCDate	dtExpectedShipmentDate = eleShipment.getDateTimeAttribute("ExpectedShipmentDate");
			long	lTime = System.currentTimeMillis();
			
			// use common code table DEMO_STORE_PICK_SLA to determine Picking time by store and delivery method
			
			// Compute new ExpectedShipDate to represent the PickByDateTime
			if (eleShipment.getAttribute("DeliveryMethod").equals("SHP"))
			{
				if (sDocType.equals ("0006"))
				{
					// this hack forces Transfer Shipments to be expected to ship by 5:00 PM less the configured SLA (e.g. -60 Min)
					// and to be shipped by Y_ANY - PREMIUM_AURE SCAC/CarrierServiceCode
					dtExpectedShipmentDate.setHours(18);
					dtExpectedShipmentDate.setMinutes(0);
					eleChangeShipment.setAttribute("SCAC", "Y_ANY");
					eleChangeShipment.setAttribute("CarrierServiceCode", "PREMIUM_AURE");
				}
				lTime = dtExpectedShipmentDate.getTime();
			}
			
			lTime += getPickingTimeMillis (env, eleShipment);			
			dtExpectedShipmentDate.setTime(lTime);
			
			// change the shipment with new expected date
			eleChangeShipment.setAttribute("ShipmentKey", eleShipment.getAttribute ("ShipmentKey"));
			eleChangeShipment.setDateTimeAttribute ("ExpectedShipmentDate", dtExpectedShipmentDate);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to changeShipment API: ");
				System.out.println (docChangeShipment.getString());
			}
			docChangeShipment = YFCDocument.getDocumentFor(api.changeShipment(env, docChangeShipment.getDocument()));
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from changeShipment API: ");
				System.out.println (docChangeShipment.getString());
			}
			
		} catch (Exception e) {
				throw new YFSException (e.getMessage());
		}
		return docAnnouncement.getDocument();
	}
	
	public Document	cancelPickupAnnouncement (YFSEnvironment env, Document docIn) throws YFSException
	{
		YFCDocument	docAnnouncement = YFCDocument.getDocumentFor (createStoreAnnouncementXML(env, docIn));
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			api.createException(env, docAnnouncement.getDocument());
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return docIn;
	}

	@SuppressWarnings("rawtypes")
	private long	getPickingTimeMillis (YFSEnvironment env, YFCElement eleShipment)
	{
		YFCDocument	docCommonCode = YFCDocument.createDocument("CommonCode");
	  	YFCElement	eleCommonCode = docCommonCode.getDocumentElement ();
		YFCElement	eleCommonCodes = null;
				
		String		sDeliveryMethod = eleShipment.getAttribute("DeliveryMethod");
		
		if (YFCObject.isVoid(sDeliveryMethod))
			return 30;
		
		Boolean		bIsShipping = sDeliveryMethod.equals("SHP");
		String		sShipNode = eleShipment.getAttribute ("ShipNode");
		String		sCommonCodeLookupValue = sShipNode + "_" + sDeliveryMethod;
		long		lSLAPickTime = bIsShipping ? -90 : 30;
		
		eleCommonCode.setAttribute ("CodeType", "DEMO_STORE_PICK_SLA");
		eleCommonCode.setAttribute("CallingOrganizationCode", eleShipment.getAttribute ("EnterpriseCode"));
		try {
			YIFApi	api = YFSUtil.getYIFApi ();
			YFCDocument docOut = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
			eleCommonCodes = docOut.getDocumentElement ();
			if (eleCommonCodes != null)
			{
				Iterator	iCommonCodes = eleCommonCodes.getChildren();			
				while (iCommonCodes.hasNext())
				{	
					eleCommonCode = (YFCElement)iCommonCodes.next();
					if (eleCommonCode.getAttribute("CodeValue").equals(sCommonCodeLookupValue))
					{
						lSLAPickTime = Long.parseLong(eleCommonCode.getAttribute("CodeShortDescription"));
						if (YFSUtil.getDebug())
						{
							System.out.println (eleCommonCode.getAttribute ("CodeLongDescription") + " = " + lSLAPickTime + " Minutes.");
						}
						break;
					}
				}
			}
		} catch (Exception e) {
		}
		return lSLAPickTime * 60 * 1000;
	}
	
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}
	private Properties m_Props;
}
