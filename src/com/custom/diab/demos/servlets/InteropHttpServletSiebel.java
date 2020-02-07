// Source File Name:   InteropHttpServletSiebel.java

package com.custom.diab.demos.servlets;

import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.*;
import org.xml.sax.SAXException;

import com.yantra.interop.client.*;
import com.yantra.interop.japi.*;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.core.YFSSystem;
import com.haht.xml.textdata.*;

// Referenced classes of package com.yantra.interop.client:
//            InteropEnvStub

@SuppressWarnings("serial")
public class InteropHttpServletSiebel extends HttpServlet
{

    public InteropHttpServletSiebel()
    {
    }

    public void init(ServletConfig config)
        throws ServletException
    {
        cat.debug("Servlet init called. Initializing local api");
        try
        {
            localApi = YIFClientFactory.getInstance().getLocalApi();
            cat.debug("Successfully intialized the local api");
        }
        catch(YIFClientCreationException e)
        {
            cat.fatal("Could not create local client", e);
            throw new ServletException(e.toString());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        cat.debug("Inside do get");
        processRequest(req, res);
        cat.debug("Completed do get");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        cat.debug("Inside do post");
        processRequest(req, res);
        cat.debug("Completed do post");
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        cat.debug("Entered process request");
        String apiName = getParameter(req, "InteropApiName");
        cat.debug("Processing api: " + apiName);
        cat.assertLog(null != apiName, "Api Name in the request cannot be null");
        String isFlow = getParameter(req, "IsFlow");
        String envUserId = getParameter(req, "YFSEnvironment.userId");
        String envProgId = getParameter(req, "YFSEnvironment.progId");
        String envPassword = getParameter (req, "YFSEnvironment.password");
        String resourceId = getParameter(req, "YFSEnvironment.resourceId");
        String adapterName = getParameter(req, "YFSEnvironment.adapterName");
        String systemName = getParameter(req, "YFSEnvironment.systemName");
        String localeCode = getParameter(req, "YFSEnvironment.locale");
        String version = getParameter(req, "YFSEnvironment.version");
        cat.debug("UserId: " + envUserId);
        cat.debug("ProgId: " + envProgId);
        cat.debug("ResourceId: " + resourceId);
        String apiData = getParameter(req, "InteropApiData");
        if (apiData == null || apiData.length() == 0)
        {
        	TextData	txtData = new TextData (req.getInputStream());
        	apiData = new String (txtData.getBytes());
        }
		
        String templateData = getParameter(req, "TemplateData");
        cat.verbose("Api Data is: " + apiData);
        InteropEnvStub envStub = new InteropEnvStub(envUserId, envProgId);
        envStub.setAdapterName(adapterName);
        envStub.setSystemName(systemName);
        envStub.setResourceId(resourceId);
        envStub.setLocaleCode(localeCode);
        envStub.setVersion(version);
        envStub.setPassword(envPassword);
        cat.assertLog(req.getContentLength() > 0, "Content length of servlet request must be positive");
        try
        {
            res.setHeader("InteropSentData", "true");
            res.setContentType("text/xml; charset=UTF-8");
            YFCDocument apiDoc = YFCDocument.parse(apiData);
            if(!YFCObject.isVoid(templateData))
            {
                YFCDocument templateDoc = YFCDocument.getDocumentFor(templateData);
                envStub.setApiTemplate(apiName, templateDoc.getDocument());
            }
            org.w3c.dom.Document retDoc = null;
            if(!apiName.equals("createEnvironment"));
            if(!apiName.equals("createEnvironment") && !apiName.equals("releaseEnvironment"))
                if(!YFCCommon.isVoid(isFlow) && isFlow.equalsIgnoreCase("Y"))
                    retDoc = localApi.executeFlow(envStub, apiName, apiDoc.getDocument());
                else
                    retDoc = localApi.invoke(envStub, apiName, apiDoc.getDocument());
            if(null == retDoc)
            {
                cat.debug("API returned a null doc. will create an empty doc");
                retDoc = YFCDocument.createDocument("ApiSuccess").getDocument();
            }
            YFCDocument.getDocumentFor(retDoc).serialize(res.getWriter());
        }
        catch(SAXException e)
        {
            cat.fatal("SAX Exception while invoking api " + apiName, e);
            res.getWriter().write((new YFCException(e)).getXMLErrorBuf());
        }
        catch(IOException e)
        {
            cat.fatal("IO Exception while invoking api " + apiName, e);
            res.getWriter().write((new YFCException(e)).getXMLErrorBuf());
        }
        catch(YFSException e)
        {
            cat.fatal("YFS Exception while invoking api " + apiName, e);
            res.getWriter().write(e.getMessage());
        }
        catch(YFCException e)
        {
            cat.fatal("YFC Exception while invoking api " + apiName, e);
            res.getWriter().write(e.getXMLErrorBuf());
        }
    }

    /*
    private void authenticate(HttpServletRequest req, InteropEnvStub envStub)
        throws YFSException, RemoteException
    {
        if(YIFAuthenticationHook.getAuthenticationRequired())
        {
            HttpSession session = req.getSession(false);
            if(session == null)
            {
                session = req.getSession(true);
                YIFAuthenticationHook.authenticateEnvironment(envStub);
				// 1.4 SDK
				// session.setAttribute ("yfs.authenticate.api.userid", envStub.getUserId());
            	// 1.5 SDK
			    session.putValue ("yfs.authenticate.api.userid", envStub.getUserId());
            } else

            // if(YFCCommon.isVoid((String)session.getAttribute("yfs.authenticate.api.userid")))
			if(YFCCommon.isVoid((String)session.getValue("yfs.authenticate.api.userid")))
            {
                YIFAuthenticationHook.authenticateEnvironment(envStub);
				// 1.4 SDK
			    // session.setAttribute ("yfs.authenticate.api.userid", envStub.getUserId());
            	// 1.5 SDK
			    session.putValue ("yfs.authenticate.api.userid", envStub.getUserId());
            }
        }
    }
*/
    public static String getParameter(HttpServletRequest req, String name)
    {
        String retVal = req.getParameter(name);
        if(retVal == null)
            return "";
        if(!isVoid(YFSSystem.getProperty("yfs.request.encoding")))
            try
            {
                byte ba[] = retVal.getBytes(YFSSystem.getProperty("yfs.request.encoding"));
                retVal = new String(ba, "UTF-8");
            }
            catch(Exception e)
            {
                return "";
            }
        retVal = retVal.trim();
        return retVal;
    }

    public static boolean isVoid(String sInStr)
    {
        if(sInStr == null)
            return true;
        return sInStr.trim().length() == 0;
    }

    @SuppressWarnings("rawtypes")
	static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static YFCLogCategory cat;
    private YIFApi localApi;

    static 
    {
        cat = YFCLogCategory.instance(com.custom.diab.demos.servlets.InteropHttpServletSiebel.class);
    }
}
