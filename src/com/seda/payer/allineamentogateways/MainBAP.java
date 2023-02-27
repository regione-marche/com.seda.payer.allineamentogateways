package com.seda.payer.allineamentogateways;

import com.seda.bap.components.core.BapException;
import com.seda.bap.components.core.spi.ClassRunnableHandler;

public class MainBAP extends ClassRunnableHandler {

	@Override
	public void run(String[] arg0) throws BapException {
		AllineaGateways.bIsBAP = true;
		AllineaGateways.main(arg0);
		this.setCode(AllineaGateways.returnCode.getCodeErro());
		this.setMessage(AllineaGateways.returnCode.getMessErro());
	}

}
