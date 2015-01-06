package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_PROPERTIES;

import java.io.Serializable;
import java.util.*;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

public class GetPropertiesSubCommand extends AbstractSubCommand
{

   public GetPropertiesSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Find the process instance context
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
      // which properties?
      Set<String> propertyIds = null;
      if (StringUtils.isNotEmpty(endpoint.getProperties()))
      {
         propertyIds = new HashSet<String>(Arrays.asList(endpoint.getProperties().split(
               ",")));
      }
      // retrieve properties
      LOG.info("Retrieving properties for process instance OID <" + processInstanceOid
            + ">");
      Map<String, Serializable> processProperties = getWorkflowService().getInDataPaths(
            processInstanceOid, propertyIds);
      // manipulate exchange
      if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         exchange.getOut().setHeader(PROCESS_INSTANCE_PROPERTIES, processProperties);
      else
         exchange.getIn().setHeader(PROCESS_INSTANCE_PROPERTIES, processProperties);
   }

}
