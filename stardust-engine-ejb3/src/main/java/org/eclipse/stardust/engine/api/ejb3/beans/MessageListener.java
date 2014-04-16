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
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.ejb3.ForkingService;
import org.eclipse.stardust.engine.api.ejb3.WorkflowException;
import org.eclipse.stardust.engine.api.ejb3.interceptors.MDBInvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/queue/CarnotSystemQueue") })
public class MessageListener extends AbstractEjb3MessageListener implements javax.jms.MessageListener
{
   private static final Logger trace = LogManager.getLogger(MessageListener.class);

   public static final String MESSAGE_TYPE_TAG = "messageType";

    

   public MessageListener()
   {
   }
   

   public void onMessage(Message message)
   {
//      trace.info("Message received: " + message);
      
      final boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(Parameters.instance()
            .getString(JmsProperties.MESSAGE_LISTENER_FAILURE_MODE_PROPERTY,
                  JmsProperties.PROCESSING_FAILURE_MODE_FORGET));
      final int nRetries = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 20);
      final int tPause = Parameters.instance().getInteger(
            JmsProperties.MESSAGE_LISTENER_RETRY_PAUSE_PROPERTY, 500);

      Action action = (Action) Proxy.newProxyInstance(Action.class.getClassLoader(),
            new Class[] {Action.class}, new MDBInvocationManager(
                  JmsProperties.MDB_NAME_MESSAGE_LISTENER, new MyAction(message, getForkingService()),
                  context, nRetries, tPause, rollbackOnError));
      action.execute();
   }

   private class MyAction implements Action, Ejb3Service
   {
      private Message message;
      private ForkingService forkingService;

      public MyAction(Message message, ForkingService forkingService)
      {
         this.message = message;
         this.forkingService = forkingService;
      }

      public Object execute()
      {
         if (message instanceof MapMessage)
         {
            MapMessage mapMessage = (MapMessage) message;

            if (ActionCarrier.extractMessageType(mapMessage) == ActionCarrier
                  .SYSTEM_MESSAGE_TYPE_ID)
            {
               try
               {
                  String transportClassName = mapMessage.getStringProperty(ActionCarrier
                        .TRANSPORT_CLASS_TAG);
                  ActionCarrier transport = (ActionCarrier)
                        Reflect.createInstance(transportClassName);
                  transport.extract(mapMessage);


                     try {
						forkingService.run(transport.createAction(), forkingService);
					} catch (WorkflowException e) {
						trace.warn(e);
					}
      
               }
               catch (JMSException e)
               {
                  trace.warn("Failed handling system message. Recovery run may be required.", e);
               }
            }
            else
            {
               trace.warn("Unknown message type " + ActionCarrier.extractMessageType(mapMessage)
                     + ".");
            }
         }
         else
         {
            trace.warn("JMS Message processed by message daemon is no map message");
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
	public org.eclipse.stardust.engine.api.ejb3.ForkingService getForkingService() {
		return this.forkingService;
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		return null;
	}
   }

}