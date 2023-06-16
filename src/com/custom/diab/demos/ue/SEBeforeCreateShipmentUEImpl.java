package com.custom.diab.demos.ue;



import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.ydm.japi.ue.YDMBeforeCreateShipment;

public class SEBeforeCreateShipmentUEImpl implements YDMBeforeCreateShipment {

	
	public Document beforeCreateShipment(YFSEnvironment env, Document docIn)
	throws YFSUserExitException {
		try {
			YFCDocument	docShipment = YFCDocument.getDocumentFor(docIn);
			
			if (YFSUtil.getDebug())
			{
					System.out.println ("Input to beforeCreateShipment() UE is: ");
					System.out.println (docShipment.getString());
			}
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from beforeCreateShipment() UE is: ");
				System.out.println (docShipment.getString());
			}
			return docShipment.getDocument();
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	} 
}
