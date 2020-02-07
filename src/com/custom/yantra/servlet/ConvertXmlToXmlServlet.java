/**
  * ConvertXmlToXmlServlet.java
  *
  **/

// PACKAGE
package com.custom.yantra.servlet;

// IMPORTS
import com.haht.xml.textdata.*;
import javax.servlet.http.*;

import java.io.*;

import com.custom.yantra.xmlmapper.*;
/**
  * 
  * 
  * The default is extends javax.servlet.http.HttpServlet.
  * You could use the extends javax.servlet.GenericServlet.
  *
  **/
@SuppressWarnings("serial")
public class ConvertXmlToXmlServlet extends javax.servlet.http.HttpServlet
{
    public ConvertXmlToXmlServlet()
    {
    }
	
	public void doPost (HttpServletRequest aRequest, HttpServletResponse aResponse)
	{
		try {	
			PrintWriter	out = aResponse.getWriter();

			// get the parameters from the POST			
			String sParamsDoc = null;
			if (aRequest.getParameter ("PARAMS") != null)
				sParamsDoc = aRequest.getParameter ("PARAMS");
			else
				sParamsDoc = new String (new TextData (aRequest.getInputStream()).getBytes());	
			ConvertXmlToXml conv = new ConvertXmlToXml ();
			out.print (conv.convert (sParamsDoc));
		} catch (Exception e) {
		}
	}	
}


