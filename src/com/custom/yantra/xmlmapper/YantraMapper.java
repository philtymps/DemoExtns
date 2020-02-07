/**
  * YantraMapper.java
  *
  **/

// PACKAGE
package com.custom.yantra.xmlmapper;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;

import java.util.Properties;

public abstract class YantraMapper extends com.haht.xml.transform.Mapper
{
    public YantraMapper()
    {
    }
	// if you want your mapper utils to have access to Yantra's YFSEnvironment
	// variable, base your MapperUtils class off of this base class.
	// e.g. public class MyMapperUtils extends YantraMapperUtils
	
	public void 			setEnv (YFSEnvironment env) {m_Env = env; }
	public YFSEnvironment	getEnv() { return m_Env; }
	public void				setProps (Properties props) { m_Props = props; }
	public Properties		getProps ()	{ return m_Props; }
	public	YIFApi			getLocalApi () throws Exception { return getApi (true); }
	public	YIFApi			getApi () throws Exception { return getApi (false); }

	protected	YIFApi			getApi (boolean bLocal) throws Exception
	{
		if (m_Api == null)
		{
			if (bLocal)
				m_Api = YIFClientFactory.getInstance().getLocalApi ();
			else			
				m_Api = YIFClientFactory.getInstance().getApi ();
		}
		return m_Api;
	}
	public	void			setApi (YIFApi api)
	{
		m_Api = api;
	}
	
	// protected member variables
	protected YFSEnvironment	m_Env = null;
	protected YIFApi			m_Api = null;
	protected Properties		m_Props = null;
}

