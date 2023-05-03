package com.custom.diab.demos.iv;

import org.w3c.dom.Element;

import com.custom.yantra.util.YFSUtil;
import com.yantra.shared.siv.IMassageInputBeforeIVCall;
import com.yantra.yfs.japi.YFSEnvironment;

/*
 * To override api input invoked from Adapter:
#iv_integration.input.massager=com.custom.diab.demos.iv.SEIVMessageAPIInput
 * 
 */

public class SEIVMassageAPIInput implements IMassageInputBeforeIVCall {

	public SEIVMassageAPIInput() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Element massageInput(YFSEnvironment env, Element eleInput) {
		// TODO Auto-generated method stub
		if (YFSUtil.getDebug())
			System.out.println ("Input Before Processing :: "  + eleInput.getFirstChild().getTextContent());
		
		// Handle CARRY lines when doing IV Inquiries by Delivery Method and ignore safety stock
		if (eleInput.getFirstChild().getTextContent().contains("\"deliveryMethod\":\"CARRY\""))
		{
			String sInput = eleInput.getFirstChild().getTextContent();

			if (sInput.contains("considerSafetyStock"))
				sInput = sInput.replaceAll ("\"considerSafetyStock\":\"true\"", "\"considerSafetyStock\":\"false\"");
			else
				sInput = sInput.replaceAll("\"demandType\"", "\"considerSafetyStock\":\"false\",\"demandType\"");
			
			sInput.replaceAll("\"deliveryMethod\":\"CARRY\"", "\"deliveryMethod\":\"PICK\"");
			eleInput.getFirstChild().setTextContent(sInput);
		}
		if (YFSUtil.getDebug())
			System.out.println ("Input Post Processing :: "  + eleInput.getFirstChild().getTextContent());
		return eleInput;
	}
}
