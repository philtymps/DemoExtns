/**
  * FlowersItem.java
  *
  **/

// PACKAGE
package com.custom.flowers.items;

import com.custom.yantra.items.*;
import com.custom.yantra.util.*;

@SuppressWarnings("serial")
public class FlowersItem extends YantraItem
{
    public FlowersItem(Object oItems)
    {
		super (oItems);
    }
		

	public static String getStyle (String sItemID)
	{
		String sStyle = sItemID;
		if (sItemID.length() >= 6)
		{
			sStyle = sItemID.substring (0, 6);
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Style Value = "+sStyle);
		}
		return sStyle;	
	}
	
	public static String getColor (String sItemID)
	{
		return "";
/*
		int iStyle = getStyleIndex(getStyle (sItemID));
		String	sColor = sItemID.substring (11,13);
		int	iColor = new Integer (sColor).intValue();
		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Color Value: "+sColor);
		}
				
		sColor = "n/a";
		switch (iStyle)
		{
			// 	stye = "33012318Y10"
			case 0:
				switch (iColor)
				{
					case 10:
						sColor = "Black/White";
						break;
					case 11:
						sColor = "Navy/Wite";
						break;
					case 12:
						sColor = "Grey/White";
						break;
				}
				break;
			// style = "45206604Y10"
			case 1:
				switch (iColor)
				{
					case 18:
						sColor = "Black";
						break;
					case 19:
						sColor = "Tobacco";
						break;
				}
				break;
				
			// style = "45527603Y10"
			case 2:
				switch (iColor)
				{
					case 13:
						sColor = "Crimson Red";
						break;
					case 14:
						sColor = "Stone";
						break;
					case 19:
						sColor = "Storm Grey";
						break;
				}
				break;			
		}
		return sColor;
*/
	}
	
	public String getFullDescription ()
	{
		String	sDescription = new String (getItemShortDesc());
		
		sDescription = sDescription + " " + FlowersItem.getSize(getItemID())+" "+FlowersItem.getColor(getItemID());
		return sDescription.toLowerCase();
	}
	
	public static String getSize (String sItemID)
	{
		String	sSize = "All";
		int		iStyle = getStyleIndex (getStyle(sItemID));

		switch (iStyle)
		{
			case 0:
			case 2:
				sSize = sItemID.substring (6);
				break;

			default:
				break;
		}
		if (sSize.equals("AN"))
		{
			sSize = "Annie";
		}else if (sSize.equals("KT"))
		{
			sSize = "Katie";
		}
		else if (sSize.equals("SL"))
		{
			sSize = "Sally";
		}
		return sSize;
	}	
	
	public static	String getStyleColumnWidth ()
	{
		return "60px";
	}
	
	public static String getSizeColumnWidth()
	{
		return "60px";
	}
	
	public static String getColorColumnWidth()
	{
		return "60px";
	}

	public static boolean	IsApparelItem (String sItemID)
	{
		return (getStyleIndex(getStyle(sItemID)) >= 0);
	}
	
	private static int	getStyleIndex (String sStyle)
	{
		for (int i = 0; i < sArrayStyles.length; i++)
		{
			if (sArrayStyles[i].equals (sStyle))
			{
				return i;
			}
		}
		return -1;
	}	

	private static final String[] sArrayStyles = {
		"850763",
	};
}
