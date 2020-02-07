package com.custom.yantra.api;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import java.util.*;
import org.w3c.dom.Document;
import com.yantra.interop.japi.YIFCustomApi;

public class getCarrierRatesAndZonesImpl implements YIFCustomApi{

	private Properties _properties = null;

	public void setProperties(Properties prop) throws Exception {
        _properties = prop;
    }

	public Document getCarrierRatesAndZones(YFSEnvironment env, Document inDoc) throws YFSException
	{
	
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement inElem = yfcInDoc.getDocumentElement();
		String id = inElem.getAttribute("Id");

		//String file = "C:/yantra/yantra70sp2/webpages/extn/avery/xmls/carrierServiceZones.xml";
		String file = _properties.getProperty("zones");
		if (id.equals("Rates"))	{
			file = _properties.getProperty("rates");
			//file = "C:/yantra/yantra70sp2/webpages/extn/avery/xmls/carrierServiceRates.xml";
		}

		YFCDocument carrierServiceDetailsDoc = YFCDocument.getDocumentForXMLFile(file);

		return carrierServiceDetailsDoc.getDocument();
	}
	
}

	

