/*
 * Created on 31-Aug-2003
 *
 */
package com.custom.yantra.api;

import java.util.Properties;
import java.lang.Runtime;
import java.lang.Process;
import java.lang.System;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.custom.yantra.util.*;

/**
 * @author Phil Tympanick
 *
 */
public class executeProgramImpl implements YIFCustomApi
{
	public static final String CMD_PROPERTY_KEY = "cmd";
	public static final String ARG_PROPERTY_KEY = "arg";
	public static final String WAIT_PROPERTY_KEY = "wait";

	private Properties mProp;
	
	public executeProgramImpl () { }
		
	
	public Document executeProgram (YFSEnvironment env, Document docIn) throws Exception
	{
		String	sCmd, sArg, sWait, sExitValue;
		YFCDocument	docExecuteProgram = YFCDocument.getDocumentFor (docIn);
		YFCElement	eleExecuteProgram = docExecuteProgram.getDocumentElement();

		// if program to execute not passed in with input document		
		if (docIn == null || eleExecuteProgram == null || eleExecuteProgram.getAttribute ("Cmd")==null)
		{	
			// use API arguments instead
			sCmd = evaluateXPathExpression ((String)mProp.getProperty(CMD_PROPERTY_KEY), docIn);
			if (sCmd == null || sCmd.trim().length() == 0)
			{
				throw new Exception("cmd property does not exist or is blank");
			}
			sArg = evaluateXPathExpression ((String)mProp.getProperty(ARG_PROPERTY_KEY), docIn);
			if (sArg == null || sArg.trim().length() == 0)
			{
				throw new Exception("arg property does not exist or is blank");
			}
			sWait = evaluateXPathExpression ((String)mProp.getProperty(WAIT_PROPERTY_KEY), docIn);
			if (sWait == null || sWait.trim().length() == 0)
			{
				throw new Exception("wait property does not exist or is blank");
			}
		}
		else
		{
			// use XML document to get program information
			sCmd = eleExecuteProgram.getAttribute ("Cmd");
			sArg = eleExecuteProgram.getAttribute ("Arg");
			sWait = eleExecuteProgram.getAttribute ("WaitFlag");
			if (sWait == null)
				sWait = "Y";
		}
		if (YFSUtil.getDebug())
		{
			System.out.println ("Entering executeProgram - Input XML:");
			System.out.println (docExecuteProgram.getString());
			System.out.println ("Executing Program: "+sCmd + " " + sArg);
			System.out.println ("WaitFlag = "+sWait);
		}			

		// execute the command in a new process and wait if requested to wait
		Runtime	rt = Runtime.getRuntime();
		Process pr = rt.exec (sCmd + " " + sArg);
		if (sWait.equalsIgnoreCase ("Y"))
		{
			pr.waitFor ();
			sExitValue = Integer.toString(pr.exitValue());
		}
		else
			sExitValue = "0";
		
		YFCDocument	docOut = YFCDocument.getDocumentFor ("<ExecuteProgram Cmd=\""+sCmd+"\" Arg=\""+sArg+"\" WaitFlag=\""+sWait+"\" ExitValue=\""+sExitValue+"\"/>");
		if (YFSUtil.getDebug())
		{
			System.out.println ("Exiting executeProgram - Output XML:"); 
			System.out.println (docOut.getString());
			System.out.println ("Command Exit Value:" + sExitValue);
		}			
		return docOut.getDocument();
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
