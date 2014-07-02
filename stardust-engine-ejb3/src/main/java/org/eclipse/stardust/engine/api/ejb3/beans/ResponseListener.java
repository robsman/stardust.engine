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
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerCarrier;

@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/CarnotApplicationQueue") })
public class ResponseListener extends AbstractEjb3MessageListener
{
   private static final Logger trace = LogManager.getLogger(ResponseListener.class);

   public ResponseListener()
   {
      super(Kind.ResponseHandler);
   }

   @Override
   protected MDAction createAction(Message message, ForkingService forkingService)
   {
      return new MDAction(message, forkingService)
      {
         @SuppressWarnings("deprecation")
         public Object execute()
         {
            // ensure the model manager was bootstrapped
            try
            {
               bootstrapModelManager();
            }
            catch (Exception e)
            {
               trace.warn(e);
            }

            try
            {
               ResponseHandlerCarrier carrier = new ResponseHandlerCarrier("applicationQueue", message);
               carrier.extract(message);
               mergeDefaultCredentials(carrier);
               forkingService.run(carrier.createAction(), forkingService);
            }
            catch (WorkflowException e)
            {
               throw e.getRootCause();
            }
            catch (JMSException e)
            {
               throw new InternalException(e);
            }
            return null;
         }

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
               if (!StringUtils.isEmpty(domainId))
               {
                  carrier.setUserDomainId(domainId);
               }
               else
               {
                  // Fall back to partitionId
                  RuntimeLog.SECURITY.info(
                        "Cannot find any default setting for domainId. Using partitionId.");
                  carrier.setUserDomainId(partitionId);
               }
            }
            else if (StringUtils.isEmpty(carrier.getUserDomainId()))
            {
               carrier.setUserDomainId(carrier.getPartitionId());
            }
         }
      };
   }


}