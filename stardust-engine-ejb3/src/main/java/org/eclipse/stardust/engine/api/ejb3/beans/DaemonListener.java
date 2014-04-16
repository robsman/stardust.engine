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
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ejb3.ForkingService;
import org.eclipse.stardust.engine.api.ejb3.interceptors.MDBInvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.NonInteractiveSecurityContextInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;


/**
 * @author ubirkemeyer
 * @version $Revision: 52592 $
 */

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/CarnotDaemonQueue") })
public class DaemonListener extends AbstractEjb3MessageListener implements MessageListener
{
   public static final Logger trace = LogManager.getLogger(DaemonListener.class);
 
   
   public void onMessage(Message message)
   {
      String failureMode = Parameters.instance().getString(
            JmsProperties.DAEMON_LISTENER_FAILURE_MODE_PROPERTY, JmsProperties.PROCESSING_FAILURE_MODE_FORGET);
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(failureMode);
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.DAEMON_LISTENER_RETRY_PAUSE_PROPERTY, 500);
      
      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MDBInvocationManager(
                  JmsProperties.MDB_NAME_DAEMON_LISTENER, new MyAction(message, getForkingService()), context,
                  nRetries, tPause, rollbackOnError));
      action.execute();
   }

   private class MyAction implements Action, Ejb3Service
   {
      private Message message;
      private ForkingService forkingSerivce;

      public MyAction(Message message, ForkingService forkingService)
      {
         this.message = message;
         this.forkingSerivce = forkingService;
      }
      
      public String toString()
      {
    	 return "Daemon Action";
      }

      public Object execute()
      {
         if (message instanceof MapMessage)
         {
            MapMessage mapMessage = (MapMessage) message;
            if (ActionCarrier.extractMessageType(mapMessage) == ActionCarrier
                  .DAEMON_MESSAGE_TYPE_ID)
            {
               trace.info("Start Daemon message received.");

               // rsauer: ensure model was bootstrapped before actually running daemon,
               // as bootstrapping in daemon listener will fail because of a missing
               // data source
   
               try
               {
                  forkingSerivce.run(new Action()
                  {
                     public Object execute()
                     {
                        ModelManagerFactory.getCurrent().findActiveModel();
                        return null;
                     }
                  }, forkingSerivce);
               }
               catch (Exception e)
               {
            	   trace.warn(e);
               }

               try
               {
                  DaemonCarrier carrier = DaemonCarrier.extract(mapMessage);
                  // (fh) we need an ActionRunner for the interceptor to kick in
                  ActionRunner runner = (ActionRunner) Proxy.newProxyInstance(
                        ActionRunner.class.getClassLoader(),
                        new Class[] {ActionRunner.class},
                        new InvocationManager(new ActionRunner()
                        {
                           public Object execute(Action action)
                           {
                              return action.execute();
                           }
                        },
                        Arrays.asList(new MethodInterceptor[] {
                              new NonInteractiveSecurityContextInterceptor(),
                              new CallingInterceptor()})));
                  runner.execute(carrier.createAction());
               }
               catch (JMSException e)
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
               trace.warn("Unknown message type " + ActionCarrier.extractMessageType(
                     mapMessage) + ", message will be lost.");
            }
         }
         else
         {
            trace.warn("JMS Message processed by message daemon is no map message, message "
                  + "will be lost.");
         }
         return null;
      }

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LoggedInUser login(String username, String password, Map properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataSource getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForkingService getForkingService() {
		return this.forkingSerivce;
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		return null;
	}


   }
}
