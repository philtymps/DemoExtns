package com.custom.diab.demos.api;

import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.*;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class SEUpgradeOrderLineShippingMethod implements YIFCustomApi {

	@SuppressWarnings("rawtypes")
	public	Document	upgradeOrderLineShippingMethod (YFSEnvironment env, Document docIn)
	{
		YFCDocument	docMonitorConsolidation = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleMonitorConsolidation = docMonitorConsolidation.getDocumentElement();
		YFCDocument	docOrder = YFCDocument.createDocument("Order");
		YFCElement	eleOrder = eleMonitorConsolidation.getChildElement("Order");
		docOrder.getDocumentElement().setAttribute("OrderHeaderKey", eleOrder.getAttribute ("OrderHeaderKey"));
		
		if (YFSUtil.getDebug())
		{
			logger.info("Input to upgradeOrderLineShippingMethod API:");
			logger.info(docMonitorConsolidation.getString());
		}
		try {
			// get the order details
			YFCDocument	docGetOrderDetailsTemplate = YFCDocument.getDocumentFor ("<Order OrderHeaderKey=\"\"><OrderLines><OrderLine OrderLineKey=\"\" CarrierServiceCode=\"\"/></OrderLines><Promotions><Promotion PromotionId=\"\" PromotionType=\"\" /></Promotions><Awards><Award PromotionId=\"\"/></Awards></Order>");
			YIFApi	api = YIFClientFactory.getInstance().getLocalApi ();
			env.setApiTemplate("getOrderDetails", docGetOrderDetailsTemplate.getDocument());
			if (YFSUtil.getDebug())
			{
				logger.info("Input to getOrderDetails API:");
				logger.info(docOrder.getString());
			}

			docOrder = YFCDocument.getDocumentFor (api.getOrderDetails(env, docOrder.getDocument()));
			eleOrder = docOrder.getDocumentElement();
			env.clearApiTemplate("getOrderDetails");
			if (YFSUtil.getDebug())
			{
				logger.info("Output from getOrderDetails API:");
				logger.info(docOrder.getString());
			}
			
			YFCElement	eleOrderLines = eleOrder.getChildElement ("OrderLines");
			Iterator	iOrderLines   = eleOrderLines.getChildren();
			String		sCarrierServiceCode = m_Props.getProperty("CarrierServiceCode");
			boolean		bUpgradedShipping = false;
			
			while (iOrderLines.hasNext())
			{
				YFCElement	eleOrderLine = (YFCElement)iOrderLines.next();
				String		sCurrentCarrierServiceCode = eleOrderLine.getAttribute("CarrierServiceCode");
				
				if (!YFCObject.isVoid(sCurrentCarrierServiceCode))
				{
					if (sCarrierServiceCode.contains("EXPRESS") && (sCurrentCarrierServiceCode.contains("STANDARD")))
					{
						eleOrderLine.setAttribute("CarrierServiceCode", sCarrierServiceCode);
						bUpgradedShipping = true;
					}
					else if (sCarrierServiceCode.contains("PREMIUM") && (sCurrentCarrierServiceCode.contains("STANDARD") || sCurrentCarrierServiceCode.contains("EXPRESS")))
					{
						eleOrderLine.setAttribute("CarrierServiceCode", sCarrierServiceCode);
						bUpgradedShipping = true;
					}
				}
				eleOrderLine.setAttribute("CarrierServiceCode", sCarrierServiceCode);	
			}
			if (bUpgradedShipping)
			{
				managePromotion (eleOrder, sCarrierServiceCode);
			}
			// change order with updated carrier service codes on each line
			if (YFSUtil.getDebug())
			{
				logger.info("Input to changeOrder API:");
				logger.info(docOrder.getString());
			}
			api.changeOrder(env, docOrder.getDocument());
		} catch (Exception e){
			env.clearApiTemplate("getOrderDetails");
			throw new YFSException (e.getMessage());
		}
		return docIn;
	}
	
	@SuppressWarnings("rawtypes")
	private void	managePromotion (YFCElement eleOrder, String sCarrierServiceCode)
	{
		YFCElement	elePromotions = eleOrder.getChildElement ("Promotions");
		
		if (YFCObject.isNull(elePromotions))
			elePromotions = eleOrder.createChild("Promotions");
		
		YFCElement	elePromotion;
		boolean		bAddCoupon = true;
		
		// if promotions exist
		if (elePromotions.hasChildNodes())
		{
			// find existing upgrade shipping coupons
			Iterator	iPromotions = elePromotions.getChildren();
			while (iPromotions.hasNext())
			{
				elePromotion = (YFCElement)iPromotions.next();
				
				String	sPromotionId = elePromotion.getAttribute("PromotionId");
				String	sPromotionType = elePromotion.getAttribute ("PromotionType");
				
				// if an upgrade from standard to express coupon found
				if (sPromotionId.equals("STD2EXP") && sPromotionType.equals("COUPON"))
				{
					// if coupon for upgrade from standard to express already exists
					if (sCarrierServiceCode.contains("EXPRESS"))
						bAddCoupon = false;
					// else if upgrading from express to premium remove STD2EXP coupon and corresponding Award
					else if (sCarrierServiceCode.contains("PREMIUM"))
						manageAward (eleOrder, elePromotion);
				}
				// else if an upgrade from express to premium coupon found
				else if (sPromotionId.equals("EXP2PRE") && sPromotionType.equals("COUPON"))
				{
					// if coupon for upgrade from express to premium already exists
					if (sCarrierServiceCode.contains("PREMIUM"))
						bAddCoupon = false;
					// else if down grading from premium to express remove EXP2PRE coupon and corresponding Award
					else if (sCarrierServiceCode.contains("EXPRESS"))
						manageAward (eleOrder, elePromotion);
				}
			}
		}
		// if a new coupon is needed add it
		if (bAddCoupon)
		{
			// determine coupon needed to offset cost of upgraded shipping
			String	sCouponId = sCarrierServiceCode.contains("EXPRESS") ? "STD2EXP" : "EXP2PRE";
			
			// add the coupon to the order promotions
			elePromotion = elePromotions.createChild("Promotion");
			elePromotion.setAttribute ("Action", "CREATE");
			elePromotion.setAttribute ("PromotionId", sCouponId);
			elePromotion.setAttribute ("PromotionType", "COUPON");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void manageAward (YFCElement eleOrder, YFCElement elePromotion)
	{
		YFCElement	eleAwards = eleOrder.getChildElement ("Awards");
		
		if (!YFCObject.isVoid(eleAwards))
		{
			Iterator	iAwards = eleAwards.getChildren();
			while (iAwards.hasNext())
			{
				YFCElement	eleAward = (YFCElement)iAwards.next();
				if (eleAward.getAttribute("PromotionId").equals(elePromotion.getAttribute("PromotionId")))
					eleAward.setAttribute("Action", "REMOVE");
			}
		}
		elePromotion.setAttribute("Action", "REMOVE");
	}
	
	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}
	private Properties	m_Props;
	private static YFCLogCategory logger = YFCLogCategory.instance(SEUpgradeOrderLineShippingMethod.class);
	
}
