package org.eclipse.stardust.engine.extensions.camel.component;

import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

/**
 * IPP Camel Component
 * 
 * @author Fradj.ZAYEN
 * 
 */
public class IppComponent extends DefaultComponent
{

   private static final String DELIMITER = ":";
   private static final String CARNOT_CURRENT_VERSION = "Camel-Ipp version: " + CurrentVersion.getVersionName();
   private static final String JAVA_RUNTIME_VERSION = "Java Runtime Version: "
         + System.getProperty("java.runtime.version");
   private static Logger trace = LogManager.getLogger(IppComponent.class);

   public IppComponent()
   {
      trace.info(CARNOT_CURRENT_VERSION);
      trace.info(JAVA_RUNTIME_VERSION);

   }

/**
 * Attempt to resolve an endpoint for the given URI
 * 
 * @param uri the URI to create
 * @param remaining
 * @param parameters
 * @return a newly created Endpoint using the given uri
 * @throws Exception is thrown if error creating the endpoint
 */
protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception
   {
      AbstractIppEndpoint ippEndpoint = null;
      String endpointKey = extractEndpoint(remaining);
      String subCommand = extractSubCommand(remaining);
      // TODO might come in handy for global ipp parameters which should not
      // be passed to
      // the endpoint: getAndRemoveParameter(parameters, key, type)

      if (CamelConstants.Endpoint.PROCESS.equalsIgnoreCase(endpointKey))
      {
         ippEndpoint = new ProcessEndpoint(uri, this);
         setProperties(ippEndpoint, parameters);
      }
      else if (CamelConstants.Endpoint.ACTIVITY.equalsIgnoreCase(endpointKey))
      {
         ippEndpoint = new ActivityEndpoint(uri, this);
         setProperties(ippEndpoint, parameters);
      }
      else if (CamelConstants.Endpoint.AUTHENTICATE.equalsIgnoreCase(endpointKey))
      {
         ippEndpoint = new AuthenticationEndpoint(uri, this);
         setProperties(ippEndpoint, parameters);
      }

      if (StringUtils.isNotEmpty(subCommand) && null != ippEndpoint)
         ippEndpoint.setSubCommand(subCommand);

      return ippEndpoint;
   }

   private String extractEndpoint(String remaining)
   {
      int idx = remaining.indexOf(DELIMITER);
      if (idx > -1)
         return remaining.substring(0, idx);
      return remaining;
   }

/**
 * extract a sub command
 * 
 * @param remaining
 * @return SubCommand
 */
private String extractSubCommand(String remaining)
   {
      int idx = remaining.indexOf(DELIMITER);
      if (idx > -1)
         return remaining.substring(idx + 1);
      return null;
   }

}
