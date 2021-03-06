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

import java.lang.reflect.Proxy;
import java.util.Arrays;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.NonInteractiveSecurityContextInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;

@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/CarnotDaemonQueue")})
public class DaemonListener extends AbstractEjb3MessageListener
{
   private static final Logger trace = LogManager.getLogger(DaemonListener.class);

   public DaemonListener()
   {
      super(Kind.DaemonListener);
   }

   @Override
   protected MDAction createAction(Message message, ForkingService forkingService)
   {
      return new MDAction(message, forkingService)
      {
         public Object execute()
         {
            if (message instanceof MapMessage)
            {
               int messageType = ActionCarrier.extractMessageType(message);
               if (messageType == ActionCarrier.DAEMON_MESSAGE_TYPE_ID)
               {
                  trace.info("Start Daemon message received.");

                  DaemonCarrier carrier = null;
                  try
                  {
                     carrier = DaemonCarrier.extract((MapMessage) message);
                  }
                  catch (JMSException e)
                  {
                     throw new InternalException(e);
                  }

                  // rsauer: ensure model was bootstrapped before actually running daemon,
                  // as bootstrapping in daemon listener will fail because of a missing
                  // data source
                  try
                  {
                     bootstrapModelManager(carrier.getPartitionOid());
                  }
                  catch (Exception e)
                  {
                     trace.warn(e);
                  }

                  try
                  {
                     // (fh) we need an ActionRunner for the interceptor to kick in
                     ActionRunner runner = (ActionRunner) Proxy.newProxyInstance(
                           ActionRunner.class.getClassLoader(),
                           new Class[] {ActionRunner.class},
                           new InvocationManager(new ActionRunner()
                           {
                              public Object execute(@SuppressWarnings("rawtypes") Action action)
                              {
                                 return action.execute();
                              }
                           },
                           Arrays.asList(new MethodInterceptor[] {
                              new NonInteractiveSecurityContextInterceptor(),
                              new CallingInterceptor()})));
                     runner.execute(carrier.createAction());
                  }
                  catch (Exception e)
                  {
                     throw new InternalException(e);
                  }
                  finally
                  {
                     trace.info("Daemon action ended.");
                  }
               }
               else
               {
                  trace.warn("Unknown message type "
                        + messageType
                        + ", message will be lost.");
               }
            }
            else
            {
               trace.warn("JMS Message processed by message daemon is no map message,"
                     + " message will be lost.");
            }
            return null;
         }
      };
   }

}
