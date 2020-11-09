package com.custom.diab.demos.iv;

import com.custom.yantra.util.YFSUtil;
import com.ibm.iv.adapter.RemoteIVAPICall;
import org.w3c.dom.Document;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;



public class SERemoteIVAPICall extends RemoteIVAPICall {

	public SERemoteIVAPICall () {
		super();
	}
	
	public Document invoke (YFSEnvironment env, Document docInXML)
	{
		YFCDocument docIn = YFCDocument.getDocumentFor(docInXML);
		YFCElement	eleDocIn = docIn.getDocumentElement();
		YFCElement  eleInput = eleDocIn.getChildElement("Input");
		String	sInput = eleInput.getNodeValue();
		
		// fix issue with CARRY items on an order created in Store 
		if (sInput.contains("\"deliveryMethod\":\"CARRY\""))
		{
			if (YFSUtil.getDebug())
			{
				System.out.println ("Original Input to SERemoteIVAPICall:");
				System.out.println (docIn.getString());
			}
			if (sInput.contains("considerSafetyStock"))
				sInput = sInput.replaceAll ("\"considerSafetyStock\":\"true\"", "\"considerSafetyStock\":\"false\"");
			else
				sInput = sInput.replaceAll("\"demandType\"", "\"considerSafetyStock\":\"false\",\"demandType\"");
			sInput = sInput.replaceAll("\"deliveryMethod\":\"CARRY\"", "\"deliveryMethod\":\"PICK\"");
			eleInput.setNodeValue(sInput);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Transformed Input to RemoteIVAPICall:");
				System.out.println (docIn.getString());
			}		
		}
		return super.invoke(env,  docInXML);
	}
}