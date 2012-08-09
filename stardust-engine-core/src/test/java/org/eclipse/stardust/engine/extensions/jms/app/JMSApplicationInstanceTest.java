/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.jms.app;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.*;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactoryUtils;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ApplicationInvocationContext;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.eclipse.stardust.engine.extensions.jms.app.JMSApplicationInstance.JmsInvocationContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * This class tests the <i>JMS Application Type</i>
 * ({@link org.eclipse.stardust.engine.extensions.jms.app.JMSApplicationInstance})
 * and consists of the following test case scenarios (if appropriate) for every public method
 * of the class under test:
 * <ul>
 *    <li>Cached JMS Resources</li>
 *    <li>JMS Resources defined via properties</li>
 * </ul>
 * </p>
 * 
 * @author nicolas.werlein
 * @version $Revision$ 
 */
public class JMSApplicationInstanceTest
{
   private JMSApplicationInstance out;   
   
   @Mock
   private ActivityInstance ai;
   
   @Before
   public void setUp()
   {
      out = new JMSApplicationInstance();
      MockitoAnnotations.initMocks(this);
   }
   
   @Test(expected = NullPointerException.class)
   public void testBootstrapFailForNull()
   {
      /* stubbing */
      final ModelManager modelManager = mock(ModelManager.class);
      ModelManagerFactoryUtils.initRtEnvWithModelManager(modelManager);
      
      /* invoking the method under test */
      out.bootstrap(null);
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test
   public void testBootstrapCachedMessageProvider()
   {
      final MessageProvider msgProvider = new TestMessageProvider();
      final String cachedMsgProviderString = (String) Reflect.getStaticFieldValue(JMSApplicationInstance.class, "CACHED_MSG_PROVIDER");
      
      /* stubbing */
      final ModelManager modelManager = mock(ModelManager.class);
      ModelManagerFactoryUtils.initRtEnvWithModelManager(modelManager);

      final Activity activity = mock(Activity.class);
      final IActivity iActivity = mock(IActivity.class);
      final IApplication iApp = mock(IApplication.class);
      
      when(ai.getActivity()).thenReturn(activity);
      
      when(modelManager.findActivity(anyLong(), anyLong())).thenReturn(iActivity);
      
      when(iActivity.getApplication()).thenReturn(iApp);
      when(iApp.getRuntimeAttribute(cachedMsgProviderString)).thenReturn(msgProvider);
      
      /* invoking the method under test */
      final ApplicationInvocationContext jmsCtx = out.bootstrap(ai);
      
      /* verifying */
      assertNotNull(jmsCtx);
      final IApplication actualIApp = (IApplication) Reflect.getFieldValue(jmsCtx, "application");
      assertEquals(iApp, actualIApp);
      final MessageProvider actualMsgProvider = (MessageProvider) Reflect.getFieldValue(jmsCtx, "msgProvider");
      assertEquals(msgProvider, actualMsgProvider);
   }
   
   @Test
   public void testBootstrapMessageProviderProperty()
   {
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(PredefinedConstants.MESSAGE_PROVIDER_PROPERTY)).thenReturn(TestMessageProvider.class.getName());
      
      /* invoking the method under test */
      final ApplicationInvocationContext jmsCtx = out.bootstrap(ai);
      
      /* verifying */
      assertNotNull(jmsCtx);
      final MessageProvider actualMsgProvider = (MessageProvider) Reflect.getFieldValue(jmsCtx, "msgProvider");
      assertThat(actualMsgProvider, instanceOf(TestMessageProvider.class));
   }
   
   @Test
   public void testSendCachedJmsResources() throws Exception
   {
      final AdditionalStubbing stubbing = new AdditionalStubbing()
      {
         public void stub(final ApplicationInvocationContext ctx, final BpmRuntimeEnvironment rtEnv)
         {
            final IApplication app = mock(IApplication.class);
            final QueueConnectionFactory connFactory = mock(QueueConnectionFactory.class);
            final Queue queue = mock(Queue.class);
            
            when(PropertyLayerProviderInterceptor.getCurrent()).thenReturn(rtEnv);
            Reflect.setFieldValue(ctx, "application", app);
            when(app.getRuntimeAttribute((String) Reflect.getFieldValue(out, "CACHED_CONNECTION_FACTORY"))).thenReturn(connFactory);
            when(app.getRuntimeAttribute((String) Reflect.getFieldValue(out, "CACHED_QUEUE"))).thenReturn(queue);
         }
      };
      
      invokeSendTemplate(stubbing);
   }
   
   @Test
   public void testSendJmsResourcesProperties() throws Exception
   {
      final AdditionalStubbing stubbing = new AdditionalStubbing()
      {
         public void stub(final ApplicationInvocationContext ctx, final BpmRuntimeEnvironment rtEnv)
         {
            final Activity activity = mock(Activity.class);
            final Application app = mock(Application.class);
            final IJmsResourceProvider jmsResourceProvider = mock(IJmsResourceProvider.class);
            final QueueConnectionFactory connFactory = mock(QueueConnectionFactory.class);
            final Queue queue = mock(Queue.class);
            
            when(ai.getActivity()).thenReturn(activity);
            when(activity.getApplication()).thenReturn(app);
            when(rtEnv.getJmsResourceProvider()).thenReturn(jmsResourceProvider);
            when(jmsResourceProvider.resolveQueueConnectionFactory(anyString())).thenReturn(connFactory);
            when(jmsResourceProvider.resolveQueue(anyString())).thenReturn(queue);
         }
      };
      
      invokeSendTemplate(stubbing);
   }
   
   private void invokeSendTemplate(final AdditionalStubbing delegate) throws Exception
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      
      /* stubbing */
      final BpmRuntimeEnvironment rtEnv = mock(BpmRuntimeEnvironment.class);
      final QueueConnection connection = mock(QueueConnection.class);
      final QueueSession session = mock(QueueSession.class);
      final QueueSender sender = mock(QueueSender.class);
      final Message message = mock(Message.class);
      final MessageProvider msgProvider = mock(MessageProvider.class);
      
      delegate.stub(ctx, rtEnv);

      PropertyLayerProviderInterceptor.setCurrent(rtEnv);
      when(rtEnv.retrieveQueueConnection(any(QueueConnectionFactory.class))).thenReturn(connection);
      when(rtEnv.retrieveQueueSession(connection)).thenReturn(session);
      when(rtEnv.retrieveUnidentifiedQueueSender(session)).thenReturn(sender);
      Reflect.setFieldValue(ctx, "msgProvider", msgProvider);
      when(msgProvider.createMessage(same(session), same(ai), any(Map.class))).thenReturn(message);
      
      /* invoking the method under test */
      out.send(ctx);
      
      /* verifying */
      verify(rtEnv).retrieveQueueConnection(any(QueueConnectionFactory.class));
      verify(rtEnv).retrieveQueueSession(connection);
      verify(rtEnv).retrieveUnidentifiedQueueSender(session);
      verify(msgProvider).createMessage(same(session), same(ai), any(Map.class));
      verify(sender).send(any(Queue.class), same(message));
   }
   
   @Test
   public void testReceive()
   {
      final Map<?, ?> data = new HashMap<Object, Object>();
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final Map<?, ?> actualData = out.receive(null, data, null);
      
      /* verifying */
      assertSame(data, actualData);
   }
   
   @Test
   public void testIsSendingTrue()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(PredefinedConstants.TYPE_ATT)).thenReturn(JMSDirection.OUT);
      
      /* invoking the method under test */
      final boolean isSending = out.isSending(ctx);
      
      /* verifying */
      assertTrue(isSending);
   }
   
   @Test
   public void testIsSendingFalse()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(PredefinedConstants.TYPE_ATT)).thenReturn(JMSDirection.IN);
      
      /* invoking the method under test */
      final boolean isSending = out.isSending(ctx);
      
      /* verifying */
      assertFalse(isSending);
   }
   
   @Test
   public void testIsReceivingTrue()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(PredefinedConstants.TYPE_ATT)).thenReturn(JMSDirection.IN);
      
      /* invoking the method under test */
      final boolean isReceiving = out.isReceiving(ctx);
      
      /* verifying */
      assertTrue(isReceiving);
   }
   
   @Test
   public void testIsReceivingFalse()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(PredefinedConstants.TYPE_ATT)).thenReturn(JMSDirection.OUT);
      
      /* invoking the method under test */
      final boolean isReceiving = out.isReceiving(ctx);
      
      /* verifying */
      assertFalse(isReceiving);
   }
   
   @Test
   public void testSetInAccessPointValue()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      final String key = "<Key>";
      final String value = "<Value>";
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      
      /* invoking the method under test */
      out.setInAccessPointValue(ctx, key, value);
      
      /* verifying */
      final Map<?, ?> inAccessPointValues = (Map<?, ?>) Reflect.getFieldValue(ctx, "inAccessPointValues");
      final Object actualValue = inAccessPointValues.get(key);
      assertEquals(value, actualValue);
   }
   
   @Test
   public void testGetOutAccessPointValue()
   {
      final ApplicationInvocationContext ctx = new JmsInvocationContext(ai);
      final String key = "<Key>";
      final String value = "<Value>";

      Reflect.setFieldValue(ctx, "outAccessPointValues", Collections.singletonMap(key, value));
      
      /* stubbing */
      final Activity activity = mock(Activity.class);
      final Application app = mock(Application.class);
      
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getApplication()).thenReturn(app);
      
      /* invoking the method under test */
      final Object actualValue = out.getOutAccessPointValue(ctx, key);
      
      /* verifying */
      assertEquals(value, actualValue);
   }
   
   @Test
   public void testCleanupDoesNotThrowException()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.cleanup(new ApplicationInvocationContext(ai));
      
      /* verifying */
      /* nothing to do */
   }
   
   private static final class TestMessageProvider extends DefaultMessageProvider {}
   
   private static interface AdditionalStubbing
   {
      void stub(final ApplicationInvocationContext ctx, final BpmRuntimeEnvironment rtEnv);
   }
}
