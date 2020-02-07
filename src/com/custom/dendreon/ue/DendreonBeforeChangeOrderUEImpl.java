/**
  * DendreonBeforeChangeOrderUEImpl.java
  *
  **/

// PACKAGE
package com.custom.dendreon.ue;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.ue.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;
import com.custom.yantra.util.*;

public class DendreonBeforeChangeOrderUEImpl  implements YFSBeforeChangeOrderUE
{
    public DendreonBeforeChangeOrderUEImpl()
    {
    }
	
	public String beforeChangeOrder(YFSEnvironment env, String inXML)
                                       throws YFSUserExitException
	{
		try {
			return YFSXMLUtil.getXMLString (beforeChangeOrder (env, YFCDocument.createDocument (inXML).getDocument()));
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	}
	
	public Document beforeChangeOrder(YFSEnvironment env, Document inXML)
                                       throws YFSUserExitException
	{
	  try {
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeChangeOrder() UE is: ");
				System.out.println (YFSXMLUtil.getXMLString (inXML));
		}

		// find any items missing item id's and use the line no from order to
		// get the corresponding item id
		YFCDocument	docOrder = YFCDocument.getDocumentFor (inXML);
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from beforeChangeOrder() UE is: ");
			System.out.println (docOrder.getString());
		}
		return docOrder.getDocument();
	  } 
	  catch (YFSException e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }		
	  catch (Exception e) 
	  {
		throw new YFSUserExitException (e.getMessage());
	  }		
	}		
}
