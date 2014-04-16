package org.eclipse.stardust.engine.api.ejb3;

import javax.ejb.Local;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;

@Local
public interface ForkingService extends Ejb3Service  {

	   QueueConnectionFactory getQueueConnectionFactory();
		   
	   
	   Queue getQueue(String queueName);	   
	   
	   Object run(Action action) throws WorkflowException;
	   
	   Object run(Action action, ForkingService proxyService)
		         throws WorkflowException;	
	   
}
