/**
  * FlowersItems.java
  *
  **/

// PACKAGE
package com.custom.flowers.items;

import com.custom.yantra.items.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.custom.yantra.util.*;

@SuppressWarnings("serial")
public class FlowersItems extends YantraItemList
{
    public FlowersItems()
    {
		super();
    }

	public void getItemsForStyle (YFSEnvironment env, String sItemID) throws Exception
	{
		// load all items for this style
		Reset();

		// load the list of items for this style		
		getItemList (env, FlowersItem.getStyle(sItemID), null);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Starting Discount Check");
			System.out.println ("Number of Items in Style "+FlowersItem.getStyle(sItemID)+" = "+getItemCount());
		}
	}	
	
	public int selectDiscountedItems ()
	{		
		// reset selected items
		ResetSelected();
		
		// iterate over all items in this style
		for (int iItem = 0; iItem < getItemCount(); iItem++)
		{
			String sDiscount = getItem (iItem).getAdditionalAttribute ("DISCOUNT");
			if (YFSUtil.getDebug())
			{
				System.out.println ("Discounted Item Ordered: "+getItem(iItem).getItemID());
				System.out.println ("Discount Amount: "+sDiscount);
			}			
			if (sDiscount.length() > 0)
				addItemSelected (iItem);	
		}
		return getItemSelectedCount();
	}
	
	public	String getDiscountForItem (String sItemID)
	{
		String 	sDiscount = "0.0";
		
		for (int iItem = 0; iItem < getItemSelectedCount(); iItem++)
		{
			if (sItemID.equalsIgnoreCase (getItem (getItemSelected (iItem)).getItemID()))
			{
				sDiscount = getItem(getItemSelected(iItem)).getAdditionalAttribute ("DISCOUNT");
				break;
			}
		}
		return sDiscount;
	}

	public int selectPreSaleItems ()
	{		
		// reset selected items
		ResetSelected();
		
		// iterate over all items in this style
		for (int iItem = 0; iItem < getItemCount(); iItem++)
		{
			String sDate = getItem (iItem).getAdditionalAttribute ("STREETDATE");
			if (YFSUtil.getDebug())
			{
				System.out.println ("PreSales Item Ordered: "+getItem(iItem).getItemID());
				System.out.println ("PreSales Date: "+sDate);
			}			
			if (sDate.length() > 0)
				addItemSelected (iItem);	
		}
		return getItemSelectedCount();
	}
	
	public	String	getPreSaleDateForItem (String sItemID)
	{
		String sDate = "";
		
		for (int iItem = 0; iItem < getItemSelectedCount(); iItem++)
		{
			if (sItemID.equalsIgnoreCase (getItem (getItemSelected (iItem)).getItemID()))
			{
				sDate = getItem(getItemSelected(iItem)).getAdditionalAttribute ("STREETDATE");
				break;
			}
		}
		return sDate;
	}
	
	public	Object	createNewItem ()
	{
		FlowersItem	oFlowersItem = new FlowersItem(this);
		
		return oFlowersItem;	
	}
}

