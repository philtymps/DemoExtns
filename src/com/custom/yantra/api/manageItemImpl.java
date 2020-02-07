/*
 * Created on 31-Aug-2003
 *
 */
package com.custom.yantra.api;

import java.util.*;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

import com.custom.yantra.util.*;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.interop.japi.YIFApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNode;

/**
 * @author Phil Tympanick
 *
 */
public class manageItemImpl implements YIFCustomApi
{
	private Properties mProp;
	
	public manageItemImpl () { }
		
	
	public Document changeSingleItem (YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument docItem = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleItem = docItem.getDocumentElement();

		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering changeSingleItem - Input XML:");
			System.out.println (docItem.getString());
		}			
		// set up ItemList element		
		YFCDocument	docModifyItemList = YFCDocument.createDocument ("ItemList");
		docModifyItemList.getDocumentElement ().setAttribute ("Action", "MODIFY");
		docModifyItemList.getDocumentElement().importNode ((YFCNode)eleItem);
		
		manageItem (env, docModifyItemList.getDocument());
		YIFApi api = YFSUtil.getYIFApi ();
		docItem = YFCDocument.getDocumentFor (api.getItemDetails (env, docItem.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting chnageSingleItem - Output XML:");
			System.out.println (docItem.getString());
		}			
		return 	(docItem.getDocument());			
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public Document manageItem (YFSEnvironment env, Document docIn) throws Exception
	{
		YFCDocument docManageItem = YFCDocument.getDocumentFor (docIn);
		YFCDocument	docNewItemList = YFCDocument.createDocument ("ItemList");
		YIFApi api = YFSUtil.getYIFApi ();
		boolean bHasNode = false;
				
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering manageItem - Input XML:");
			System.out.println (docManageItem.getString());
		}			
		
		YFCElement	eleManageItems = docManageItem.getDocumentElement ();
		String	sAction = eleManageItems.getAttribute ("Action");
		if (sAction == null || sAction.length() == 0)
			sAction = "CREATE";
		else
			sAction = sAction.toUpperCase ();
		// see which items we need to create/modify
		if (eleManageItems != null)
		{
			Iterator iManageItems = eleManageItems.getChildren();
			while (iManageItems.hasNext())
			{
				YFCElement	eleItem = (YFCElement)iManageItems.next();
				YFCDocument	docItem = YFCDocument.getDocumentFor (eleItem.getString());

									
				// now see which items we need to actually create/modify
				try {						
		
					// throws an exception if the item does not exist
					api.getItemDetails (env, docItem.getDocument());
					
					if (sAction.indexOf("MODIFY") >= 0)
					{
						docNewItemList.getDocumentElement().importNode ((YFCNode)eleItem);
						bHasNode = true;
					}
				} catch (Exception e) {
					if (sAction.indexOf("CREATE") >= 0)
					{
						docNewItemList.getDocumentElement().importNode ((YFCNode)eleItem);
						bHasNode = true;
					}
				} 
			}
			if (bHasNode)
			{
				String sApi = "createItem";
				if (YFSUtil.getDebug())
				{
					if (sAction.indexOf("MODIFY") >= 0)
						sApi = "modifyItem";
					System.out.println ("Input to " + sApi + "- Input XML:");
					System.out.println (docNewItemList.getString());
				}			
				
				if (sAction.indexOf("MODIFY") >= 0)
					api.modifyItem (env, docNewItemList.getDocument());
				else
					api.createItem (env, docNewItemList.getDocument());
			}
		}	
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting manageItem - Output XML:");
			System.out.println (docNewItemList.getString());
		}			

		return docNewItemList.getDocument();
	}

	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
	}

	public Properties getProperties()
	{
		return mProp;
	}	

	@SuppressWarnings("unused")
	private String evaluateXPathExpression (String sXPathExpr, Document inDoc) throws Exception
	{
		String sResult = null;
		if (sXPathExpr != null)
		{
			if (sXPathExpr.startsWith("xml:"))
			{
				XPath xpath = XPathFactory.newInstance().newXPath();
	           	String expression = sXPathExpr.substring (4);
				sResult = new String ((String)xpath.evaluate(expression, inDoc, XPathConstants.STRING));
			}
			else
				sResult = sXPathExpr;			
		}
		return sResult;
	}	
}
