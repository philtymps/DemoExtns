package com.custom.diab.demos.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;


public class SEPOSOrderDefaults implements YIFCustomApi {

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

	public Document setPOSOrderDefaultsOnChange (YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOut = docIn;
/*		NOTE THIS UE ONLY AVAILABILE WITH TCXGravity
		
		try {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Entering setPOSOrderDefaultsOnChange - Input XML:");
				System.out.println (YFCDocument.getDocumentFor (docIn).getString());
			}
			YFSBeforeChangeOrderUEForPOS uePOS = new YFSBeforeChangeOrderUEForPOS ();
			docOut = uePOS.beforeChangeOrder(env, docIn);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Exiting setPOSOrderDefaultsOnChange - Output XML:");
				System.out.println (YFCDocument.getDocumentFor (docOut).getString());
			}
		} catch (YFSUserExitException e) {
			throw new YFSException (e.getMessage());
		}
*/
		return docOut;
	}

	public Document setPOSOrderDefaultsOnCreate (YFSEnvironment env, Document docIn) throws YFSException
	{
		Document	docOut = docIn;
		/*		NOTE THIS UE ONLY AVAILABILE WITH TCXGravity
		
		try {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Entering setPOSOrderDefaultsOnCreate - Input XML:");
				System.out.println (YFCDocument.getDocumentFor (docIn).getString());
			}
			YFSBeforeCreateOrderUEForPOS uePOS = new YFSBeforeCreateOrderUEForPOS ();
			docOut = uePOS.beforeCreateOrder(env, docIn);
			if (YFSUtil.getDebug())
			{
				System.out.println ("Exiting setPOSOrderDefaultsOnCreate - Output XML:");
				System.out.println (YFCDocument.getDocumentFor (docOut).getString());
			}
		} catch (YFSUserExitException e) {
			throw new YFSException (e.getMessage());
		}
*/
		return docOut;
	}

	public Properties getProperties() {
		return m_Props;
	}

	private Properties m_Props;
}
