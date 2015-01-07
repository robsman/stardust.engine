/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import java.lang.reflect.Proxy;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerCarrier;


/**
 * @author sauer
 * @version $Revision: $
 */
public class ApplicationQueueMessageHandler extends AbstractMessageHandler
{

   public void onMessage(Message message)
   {
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(Parameters.instance()
            .getString(JmsProperties.RESPONSE_HANDLER_FAILURE_MODE_PROPERTY,
                  JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.RESPONSE_HANDLER_RETRY_PAUSE_PROPERTY, 500);

      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MessageHandlingInvocationManager(this,
                  JmsProperties.MDB_NAME_RESPONSE_HANDLER,
                  new ApplicationQueueMessageDeliveryAction(message), nRetries, tPause,
                  rollbackOnError));

      action.execute();
   }

   private class ApplicationQueueMessageDeliveryAction implements Action
   {
      private final Message message;

      public ApplicationQueueMessageDeliveryAction(Message message)
      {
         this.message = message;
      }

      public Object execute()
      {
         ForkingService service = null;
         try
         {
            
            ResponseHandlerCarrier carrier = new ResponseHandlerCarrier(
                  "applicationQueue", message);
            carrier.extract(message);
            
            mergeDefaultCredentials(carrier);
            
            ForkingServiceFactory factory = (ForkingServiceFactory)
                  Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
            service = factory.get();
            service.isolate(carrier.createAction());
         }
         catch (JMSException e)
         {
            throw new InternalException(e);
         }
         finally
         {
// @todo (france, ub):               ForkingServiceLocator.release(service);
         }
         return null;
      }

      /**
       * @param carrier
       */
      private void mergeDefaultCredentials(ResponseHandlerCarrier carrier)
      {
         // merge default security context

         if (StringUtils.isEmpty(carrier.getPartitionId()))
         {
            String partitionId = Parameters.instance().getString(
                  SecurityProperties.DEFAULT_PARTITION,
                  PredefinedConstants.DEFAULT_PARTITION_ID);

            carrier.setPartitionId(partitionId);

            // PartitionId is set by default value. This means that any existing 
            // domainId has to be overwritten.
            String domainId = Parameters.instance().getString(
                  SecurityProperties.DEFAULT_DOMAIN);

            if ( !StringUtils.isEmpty(domainId))
            {
               carrier.setUserDomainId(domainId);
            }
            else
            {
               // Fall back to partitionId
               RuntimeLog.SECURITY
                     .info("Cannot find any default setting for domainId. Using partitionId.");
               carrier.setUserDomainId(partitionId);
            }

            return;
         }

         if (StringUtils.isEmpty(carrier.getUserDomainId()))
         {
            carrier.setUserDomainId(carrier.getPartitionId());
         }
      }

   }

}
