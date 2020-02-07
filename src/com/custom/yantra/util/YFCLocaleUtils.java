/**
  * YFCLocaleUtils.java
  *
  **/

// PACKAGE
package com.custom.yantra.util;

import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.*;

public class YFCLocaleUtils 
{
    public YFCLocaleUtils()
    {
    }

	public	static final String			LOCALES = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	"<Locales SystemLocale=\"en_US_EST\" DefaultLocale=\"en_US_EST\">" +
		"<Locale " +
		" Country=\"US\"" +
	    " Currency=\"USD\"" +
		" DateFormat=\"MM/dd/yyyy\"" +
		" DateHourMinuteFormat=\"MM/dd/yyyy HH:mm\"" +
		" DayDisplayDateFormat=\"E d\"" +
		" DimensionUom=\"IN\"" +
		" HourMinuteTimeFormat=\"HH:mm\"" +
	    " Language=\"en\"" +
		" LocaleDescription=\"US Eastern time\"" +
	    " LocaleKey=\"EN_US_EST\"" +
		" LocaleCode=\"en_US_EST\"" +
		" MonthDisplayDateFormat=\"MMMMM yyyy\"" +
		" TimeFormat=\"HH:mm:ss\"" +
		" TimestampFormat=\"MM/dd/yyyy HH:mm:ss\"" +
		" Timezone=\"America/New_York\"" +
		" Variant=\"\"" +
		" VolumeUom=\"CIN\"" +
		" WeightUom=\"LBS\"" +
		" />" + 
		"<Locale " +
		" Country=\"CA\"" +
	    " Currency=\"CAD\"" +
		" DateFormat=\"dd/MM/yyyy\"" +
		" DateHourMinuteFormat=\"dd/MM/yyyy HH:mm\"" +
		" DayDisplayDateFormat=\"E d\"" +
		" DimensionUom=\"IN\"" +
		" HourMinuteTimeFormat=\"HH:mm\"" +
	    " Language=\"fr\"" +
		" LocaleDescription=\"CA Eastern time\"" +
	    " LocaleKey=\"FR_CA_EST\"" +
		" LocaleCode=\"fr_CA_EST\"" +
		" MonthDisplayDateFormat=\"MMMMM yyyy\"" +
		" TimeFormat=\"HH:mm:ss\"" +
		" TimestampFormat=\"dd/MM/yyyy HH:mm:ss\"" +
		" Timezone=\"Canada/Montreal\"" +
		" Variant=\"\"" +
		" VolumeUom=\"CIN\"" +
		" WeightUom=\"LBS\"" +
		" />" + 
	"</Locales>";		

	public static	void init (String sLocaleCode)
	{
	  	try {
			// if YFCLocale not initialized (running in client mode)
			if (YFCLocale.getDefaultLocale()==null)
			{
				if (YFSUtil.getDebug())
				{
					System.out.println ("WARNING: Initializing Locales for Yantra Applicaiton-Only en_US_EST Locale is available");
					System.out.println ("Any attempt to use any other Locale will result in unpredictable behavior");
				}
				YFCLocale.init (YFCDocument.getDocumentFor (LOCALES), sLocaleCode);	
			}
		} catch (Exception e) {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Exception in YFCLocaleUtils.init() Initializing  YFCLocale.  Message Returned is "+e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static	String formatXMLDate () throws Exception
	{
		return formatXMLDate (new YFCDate().getString (YFCLocale.getDefaultLocale(), false));
	}
	
	@SuppressWarnings("deprecation")
	public static	String	formatXMLDate (String sDate) throws Exception
	{
		// get hub's local
		YFCLocale	localeDefault = YFCLocale.getDefaultLocale();
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Requested Date="+sDate);
			System.out.println ("Default Locale = "+localeDefault.getLocale().getDisplayName());
			System.out.println ("Default Date Format ="+localeDefault.getDateFormat ());
		}
		
		YFCDate	dtDate	= YFCDate.getYFCDate (sDate);
		return dtDate.getString (localeDefault, true).substring (0,10);
	}

	@SuppressWarnings("deprecation")
	public static	String formatXMLDateTime () throws Exception
	{
		return formatXMLDate (new YFCDate().getString (YFCLocale.getDefaultLocale(), true));
	}

	@SuppressWarnings("deprecation")
	public static	String	formatXMLDateTime (String sDate) throws Exception
	{
		// get hub's local
		YFCLocale	localeDefault = YFCLocale.getDefaultLocale();
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Requested Date="+sDate);
			System.out.println ("Default Locale = "+localeDefault.getLocale().getDisplayName());
			System.out.println ("Default Date Format ="+localeDefault.getDateFormat ());
		}
		
		YFCDate	dtDate	= YFCDate.getYFCDate (sDate);
		return dtDate.getString (localeDefault, true);
	}

	@SuppressWarnings("deprecation")
	public static	String makeXMLDateTime () throws Exception
	{
		return makeXMLDateTime (new YFCDate().getString (YFCLocale.getDefaultLocale (), true));	
	}
	@SuppressWarnings("deprecation")
	public static	String	makeXMLDateTime (String sDateTime) throws Exception
	{
		// get hub's local
		YFCLocale	localeDefault = YFCLocale.getDefaultLocale();
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Requested Date Time="+sDateTime);
			System.out.println ("Default Locale = "+localeDefault.getLocale().getDisplayName());
			System.out.println ("Default Date Format ="+localeDefault.getDateFormat()+" "+localeDefault.getTimeFormat());
		}
		
		YFCDate	dtDate	= new YFCDate (sDateTime, localeDefault, false);		
		return dtDate.getString(YFCDate.ISO_DATETIME_FORMAT);
	}
	
	@SuppressWarnings("deprecation")
	public static String formatDate (String sDateTime, String sOutFormat)
	{
		// get hub's local
		YFCLocale	localeDefault = YFCLocale.getDefaultLocale();
		
		if (YFSUtil.getDebug()) {
			System.out.println ("Requested Date="+sDateTime);
			System.out.println ("Default Locale = "+localeDefault.getLocale().getDisplayName());
			System.out.println ("Default Date Format ="+localeDefault.getDateFormat ());
		}
		
		YFCDate	dtDate	= YFCDate.getYFCDate (sDateTime);
		return dtDate.getString (sOutFormat);		
	}
}

