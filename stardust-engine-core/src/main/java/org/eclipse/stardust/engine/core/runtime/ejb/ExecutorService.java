package org.eclipse.stardust.engine.core.runtime.ejb;

import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.WorkflowException;

public interface ExecutorService extends Ejb3ManagedService
{
   QueueConnectionFactory getQueueConnectionFactory();

   Queue getQueue(String queueName);

   Object run(Action<?> action) throws WorkflowException;

   Object run(Action<?> action, ExecutorService proxyService) throws WorkflowException;

   void release();
}
