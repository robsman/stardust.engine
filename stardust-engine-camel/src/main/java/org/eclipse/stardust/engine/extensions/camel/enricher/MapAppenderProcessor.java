package org.eclipse.stardust.engine.extensions.camel.enricher;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ADDITIONAL_SPRING_BEANS_DEF_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CONSUMER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PROCESS_CONTEXT_HEADERS_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRODUCER_ROUTE_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.MODEL_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PASSWORD;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.USER;

import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * This class is used set the extended attributes in the header of the exchange
 * 
 * @author Fradj.ZAYEN
 * 
 */
public class MapAppenderProcessor implements Processor
{
   private static String EMAIL_OVERLAY_ATTRIBUTE = "stardust:emailOverlay";

   // TODO : add otheres such as REST and Script.

   private BpmRuntimeEnvironment bpmRt;

   public static final Logger logger = LogManager.getLogger(MapAppenderProcessor.class);

   public void process(Exchange exchange) throws Exception
   {

      bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      Map<String, Object> attributes = bpmRt.getCurrentActivityInstance().getActivity().getApplication()
            .getAllAttributes();

      Map<String, Object> currentHeaders = exchange.getIn().getHeaders();

      for (Iterator<String> i = attributes.keySet().iterator(); i.hasNext();)
      {

         String key = i.next();

         Object attribute = attributes.get(key);

         if (!removeFromHeaders(key))
         {
            currentHeaders.put(key, attribute);
         }
         else
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Extended Attribute " + key + " skipped.");
            }
         }
      }

      exchange.getIn().setHeaders(currentHeaders);

      if (attributes.containsKey(PROCESS_CONTEXT_HEADERS_EXT_ATT)
            && attributes.get(PROCESS_CONTEXT_HEADERS_EXT_ATT) != null)
      {
         Boolean attributeValue = false;
         if (attributes.get(PROCESS_CONTEXT_HEADERS_EXT_ATT) instanceof Boolean)
         {
            attributeValue = (Boolean) attributes.get(PROCESS_CONTEXT_HEADERS_EXT_ATT);
         }
         else if (attributes.get(PROCESS_CONTEXT_HEADERS_EXT_ATT) instanceof String)
         {
            attributeValue = new Boolean((String) attributes.get(PROCESS_CONTEXT_HEADERS_EXT_ATT));
         }
         if (attributeValue)
         {
            addProcessContextHeaders(exchange.getIn());
         }
      }
      else
      {
         // per default the process context headers are added.
         addProcessContextHeaders(exchange.getIn());
      }
   }

   private void addProcessContextHeaders(Message message)
   {
      if (this.bpmRt != null)
      {
         IActivityInstance activityInstance = this.bpmRt.getCurrentActivityInstance();

         message.setHeader(PARTITION, SecurityProperties.getPartition().getId());
         message.setHeader(MODEL_ID, activityInstance.getActivity().getModel().getId());
         message.setHeader(PROCESS_ID, activityInstance.getProcessInstance().getProcessDefinition().getId());
         message.setHeader(PROCESS_INSTANCE_OID, activityInstance.getProcessInstanceOID());
         message.setHeader(ACTIVITY_ID, activityInstance.getActivity().getId());
         message.setHeader(ACTIVITY_INSTANCE_OID, activityInstance.getOID());
      }

   }

   private boolean removeFromHeaders(String key)
   {
      if (key == null || "".equals(key))
      {
         return true;
      }

      if (key.startsWith(EMAIL_OVERLAY_ATTRIBUTE))
      {
         return true;
      }

      if (key.equals(PRODUCER_ROUTE_ATT))
      {
         return true;
      }

      if (key.equals(CONSUMER_ROUTE_ATT))
      {
         return true;
      }

      if (key.equals(ADDITIONAL_SPRING_BEANS_DEF_ATT))
      {
         return true;
      }

      if (key.equals(USER))
      {
         return true;
      }

      if (key.equals(PASSWORD))
      {
         return true;
      }

      return false;
   }
}
