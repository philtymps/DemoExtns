package com.custom.diab.demos.api;

import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class CPGUpdateShippingCalendarForWeather implements YIFCustomApi {

	protected	Properties m_Props;
	public CPGUpdateShippingCalendarForWeather() {
		
	}
	
	public Document	CPGUpdateShippingCalendarForWeather (YFSEnvironment env, Document docIn) throws YFSException
	{
		
		YFCDocument	docWeather = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleWeather = docWeather.getDocumentElement();
		YFCElement	ele5DayForecast = eleWeather.getChildElement("FiveDayForecast");
		int			iHighTemp = eleWeather.getIntAttribute ("HighTempShutOff");
		
		YFCDocument	docChangeCalendar = YFCDocument.createDocument("Calendar");
		YFCElement	eleChangeCalendar = docChangeCalendar.getDocumentElement();
		YFCElement	eleCalendarDayExceptions = eleChangeCalendar.createChild("CalendarDayExceptions");

		eleCalendarDayExceptions.setAttribute("ResetAll", "Y");
		eleChangeCalendar.setAttribute("OrganizationCode", eleWeather.getAttribute("OrganizationCode"));
		eleChangeCalendar.setAttribute("CalendarId", eleWeather.getAttribute("CalendarId"));
		
		Iterator<YFCElement>	i5DayForecast = ele5DayForecast.getChildren();
		YFCDate					dtNow = new YFCDate (System.currentTimeMillis());
		while (i5DayForecast.hasNext())
		{
			YFCElement	eleForecast = i5DayForecast.next();
			int	iForecastHighTemp = eleForecast.getIntAttribute("HighTemp");
			if (iForecastHighTemp >= iHighTemp)
			{
				YFCElement	eleCalendarDayException = eleCalendarDayExceptions.createChild("CalendarDayException");
				eleCalendarDayException.setDateAttribute("Date", dtNow);
				eleCalendarDayException.setAttribute("ExceptionType", "1");
			}
			dtNow.changeDate(1);
		}
		try {
		YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Input to changeCalendar API:");
			System.out.println (docChangeCalendar.getString());
		}
		docIn = api.changeCalendar(env, docChangeCalendar.getDocument());	
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return docIn;
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

}
