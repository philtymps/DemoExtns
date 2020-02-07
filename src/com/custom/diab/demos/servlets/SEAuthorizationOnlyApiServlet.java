package com.custom.diab.demos.servlets;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;

import com.yantra.interop.client.InteropEnvStub;
import com.yantra.interop.client.InteropHttpServlet;
import com.yantra.interop.services.security.ServletRequestParams;
import com.yantra.yfc.util.YFCConfigurator;



@SuppressWarnings("serial")
public class SEAuthorizationOnlyApiServlet extends InteropHttpServlet {

    public SEAuthorizationOnlyApiServlet()
    {
    }

    protected boolean isAuthenticated(HttpServletRequest req, ServletRequestParams params)
    {
        return true;
    }

    protected boolean isSecurityEnabled()
    {
        return false;
    }

    protected boolean hasPermission(HttpServletRequest request, String ID)
    {
        if("YFSSYS00004O90".equals(ID))
            return true;
        else
            return super.hasPermission(request, ID);
    }

    protected void preInvoke(InteropEnvStub envStub, String apiName, Document inDoc, boolean isFlow)
    {
        envStub.setUserTokenIgnored(true);
    }

    @SuppressWarnings("unused")
	private static final boolean validateAuthentication = YFCConfigurator.getInstance().getBooleanProperty("authonlyservlet.validate.authentication", true);


}
