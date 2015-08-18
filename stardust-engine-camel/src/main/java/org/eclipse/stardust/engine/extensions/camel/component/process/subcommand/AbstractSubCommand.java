package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import org.apache.camel.Processor;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

public abstract class AbstractSubCommand implements Processor
{
   protected Logger LOG = LogManager.getLogger(this.getClass().getName());

   protected ProcessEndpoint endpoint;

   protected ServiceFactory sf;

   public AbstractSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      this.endpoint = endpoint;
      this.sf = sf;
   }

   public WorkflowService getWorkflowService()
   {
      return this.sf.getWorkflowService();
   }

   public QueryService getQueryService()
   {
      return (QueryService) this.sf.getQueryService();
   }
   
   
   
}
