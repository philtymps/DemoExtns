/**
  * SiebelBeforeCreateOrderUEImpl.java
  *
  **/

// PACKAGE
package com.custom.siebel.ue;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.ue.*;
import com.yantra.yfc.dom.*;
import org.w3c.dom.*;

import com.custom.siebel.shoppingcart.*;
import com.custom.yantra.util.*;


public class SiebelBeforeCreateOrderUEImpl  implements YFSBeforeCreateOrderUE
{
    public SiebelBeforeCreateOrderUEImpl()
    {
    }
	
	public String beforeCreateOrder(YFSEnvironment env, String inXML)
                                       throws YFSUserExitException
	{
		try {
			return YFSXMLUtil.getXMLString (beforeCreateOrder (env, YFCDocument.createDocument (inXML).getDocument()));
		} catch (Exception e) {
			throw new YFSUserExitException (e.getMessage());
		}
	}
	
	public Document beforeCreateOrder(YFSEnvironment env, Document inXML)
                                       throws YFSUserExitException
	{
	  try {
		YFCDocument docOrder = YFCDocument.getDocumentFor (inXML);
		YFCElement	eleOrder = docOrder.getDocumentElement ();
		
		if (YFSUtil.getDebug())
		{
				System.out.println ("Input to beforeCreateOrder() API is: ");
				System.out.println (docOrder.getString());
		}

		// find any items missing item id's and use the line no from order to
		// get the corresponding item id
		Document outXML = inXML;
		if (eleOrder.getAttribute("OrderSource").equals("SIEBEL"))
		{
			SiebelShoppingCart	oShoppingCart = new SiebelShoppingCart();
			outXML = oShoppingCart.createOrderDoc (env, inXML);			
		}		
		if (YFSUtil.getDebug())
		{
			System.out.println ("Output from beforeCreateOrder() API is: ");
			System.out.println (YFSXMLUtil.getXMLString (outXML));
		}
		return outXML;
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
