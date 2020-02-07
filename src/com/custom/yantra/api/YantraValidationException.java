/**
  * thowException.java
  *
  **/

// PACKAGE
package com.custom.yantra.api;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.interop.japi.YIFCustomApi;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.xpath.*;

public class YantraValidationException implements YIFCustomApi
{
    public YantraValidationException()
    {
    }

	public static final String EXCEPTION_CODE_PROPERTY_KEY = "ExceptionCode";
	public static final String EXCEPTION_DESCRIPTION_PROPERTY_KEY = "ExceptionDescription";
	private Properties mProp;
	
		
	public Document throwException (YFSEnvironment env, Document docIn) throws YFSException
	{
		String sExceptionCode, sExceptionDesc;
		sExceptionCode = null;
		sExceptionDesc = null;
		try {
			sExceptionCode = evaluateXPathExpression ((String)mProp.getProperty(EXCEPTION_CODE_PROPERTY_KEY), docIn);
		} catch (Exception ignore){	}
		try {
			sExceptionDesc = evaluateXPathExpression ((String)mProp.getProperty(EXCEPTION_DESCRIPTION_PROPERTY_KEY), docIn);
		} catch (Exception ignore){	}
		YFSException	e = new YFSException ();
		if (sExceptionCode != null)
			e.setErrorCode (sExceptionCode);
		if (sExceptionDesc != null)
			e.setErrorDescription (sExceptionDesc);
		throw e;
		//return docIn;
	}

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

