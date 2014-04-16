package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDrivenContext;

import org.eclipse.stardust.engine.api.ejb3.ForkingService;

public class AbstractEjb3MessageListener {

	@EJB
	private ForkingService forkingService;

	@Resource
	protected MessageDrivenContext context;

	public org.eclipse.stardust.engine.api.ejb3.ForkingService getForkingService() {

		return this.forkingService;
	}

}
