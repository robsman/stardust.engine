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
package org.eclipse.stardust.engine.extensions.mail.app;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;


/**
 * <p>
 * This class tests the <i>Mail Application Type</i>
 * ({@link org.eclipse.stardust.engine.extensions.mail.app.MailApplicationInstance}).
 * </p>
 * 
 * @author nicolas.werlein
 * @version $Revision$ 
 */
@PrepareForTest( { MailAssembler.class, MailApplicationInstance.class } )
public class MailApplicationInstanceTest
{
   private static final long PI_OID = 18;
   private static final long AI_OID = 81;
   private static final String ACTIVITY_NAME = "<Activity Name>";
   private static final String MAIL_SERVER = "<Default Mail Server>";
   private static final String URL_PREFIX = "<URL Prefix>";
   private static final String FROM_ADDRESS = "<From Address>";
   private static final String TO_ADDRESS = "<To Address>";
   private static final String CC_ADDRESS = "<CC Address>";
   private static final String BCC_ADDRESS = "<BCC Address>";
   private static final String SUBJECT = "<Subject>";
   private static final boolean SUBJECT_INCLUDE_UNIQUE_IDENTIFIED = true;
   private static final String MAIL_PRIORITY = "Highest";
   private static final String PLAIN_TEXT_TEMPLATE = "This is a template:  {0}, {1}";
   private static final boolean USE_HTML = false;
   private static final String HTML_HEADER = "<HTML Header>";
   private static final String HTML_TEMPLATE = "This is a template:  {0}, {1}, {2}";
   private static final String HTML_FOOTER = "<HTML Footer>";
   private static final boolean CREATE_PROCESS_HISTORY_LINK = true;
   private static final boolean MAIL_RESPONSE = false;
   private static final int PARAMETER_COUNT = 3;
   private static final String FIRST_OUTPUT_VALUE_NAME = "<First Output Value's Name>";
   private static final String FIRST_OUTPUT_VALUE = "<First Output Value>";
   private static final String SECOND_OUTPUT_VALUE_NAME = "<Second Output Value's Name>";
   private static final String SECOND_OUTPUT_VALUE = "<Second Output Value>";
   
   private MailApplicationInstance out;
   
   @Mock
   private ActivityInstance ai;
   
   @Mock
   private Application app;
   
   @Rule
   public final PowerMockRule rule = new PowerMockRule();
   
   static
   {
      PowerMockAgent.initializeIfNeeded();
   }
   
   @Before
   public void setUp()
   {
      out = new MailApplicationInstance();
      MockitoAnnotations.initMocks(this);
   }
   
   @Test(expected = NullPointerException.class)
   public void testBootstrapFailForNull()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final MailApplicationInstance out = new MailApplicationInstance();
      out.bootstrap(null);
      
      /* verifying */
      /* nothing to do */
   }
   
   @Test
   public void testBootstrap()
   {
      /* stubbing */
      defineBootstrapMocks();
      
      /* invoking the method under test */
      out.bootstrap(ai);
      
      /* verifying */
      verifyBootstrap(out);
   }

   private void defineBootstrapMocks()
   {
      final Activity activity = mock(Activity.class);
      final ApplicationContext appCtx = mock(ApplicationContext.class);
      
      when(ai.getProcessInstanceOID()).thenReturn(PI_OID);
      when(ai.getOID()).thenReturn(AI_OID);
      when(ai.getActivity()).thenReturn(activity);
      when(activity.getName()).thenReturn(ACTIVITY_NAME);
      when(activity.getApplication()).thenReturn(app);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_SERVER)).thenReturn(MAIL_SERVER);
      when(app.getAttribute(MailConstants.URL_PREFIX)).thenReturn(URL_PREFIX);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_FROM)).thenReturn(FROM_ADDRESS);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_TO)).thenReturn(TO_ADDRESS);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_CC)).thenReturn(CC_ADDRESS);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_BCC)).thenReturn(BCC_ADDRESS);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_SUBJECT)).thenReturn(SUBJECT);
      when(app.getAttribute(MailConstants.SUBJECT_INCLUDE_UNIQUE_IDENTIFIED)).thenReturn(SUBJECT_INCLUDE_UNIQUE_IDENTIFIED);
      when(app.getAttribute(MailConstants.DEFAULT_MAIL_PRIORITY)).thenReturn(MAIL_PRIORITY);
      when(app.getAttribute(MailConstants.PLAIN_TEXT_TEMPLATE)).thenReturn(PLAIN_TEXT_TEMPLATE);
      when(app.getAttribute(MailConstants.USE_HTML)).thenReturn(USE_HTML);
      when(app.getAttribute(MailConstants.HTML_HEADER)).thenReturn(HTML_HEADER);
      when(app.getAttribute(MailConstants.HTML_TEMPLATE)).thenReturn(HTML_TEMPLATE);
      when(app.getAttribute(MailConstants.HTML_FOOTER)).thenReturn(HTML_FOOTER);
      when(app.getAttribute(MailConstants.CREATE_PROCESS_HISTORY_LINK)).thenReturn(CREATE_PROCESS_HISTORY_LINK);
      when(app.getAttribute(MailConstants.MAIL_RESPONSE)).thenReturn(MAIL_RESPONSE);
      when(app.getAttribute(MailConstants.OUTPUT_VALUES + "[0].name")).thenReturn(FIRST_OUTPUT_VALUE_NAME);
      when(app.getAttribute(MailConstants.OUTPUT_VALUES + "[0].value")).thenReturn(FIRST_OUTPUT_VALUE);
      when(app.getAttribute(MailConstants.OUTPUT_VALUES + "[1].name")).thenReturn(SECOND_OUTPUT_VALUE_NAME);
      when(app.getAttribute(MailConstants.OUTPUT_VALUES + "[1].value")).thenReturn(SECOND_OUTPUT_VALUE);
      when(activity.getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)).thenReturn(appCtx);
   }

   private void verifyBootstrap(final MailApplicationInstance out)
   {
      final long actualPiOid = (Long) Reflect.getFieldValue(out, "processInstanceOID");
      assertEquals(PI_OID, actualPiOid);
      final long actualAiOid = (Long) Reflect.getFieldValue(out, "activityInstanceOID");
      assertEquals(AI_OID, actualAiOid);
      final String actualActivityName = (String) Reflect.getFieldValue(out, "activityName");
      assertEquals(ACTIVITY_NAME, actualActivityName);
      final Application actualApp = (Application) Reflect.getFieldValue(out, "application");
      assertEquals(app, actualApp);
      final String actualMailServer = (String) Reflect.getFieldValue(out, "mailServer");
      assertEquals(MAIL_SERVER, actualMailServer);
      final String actualUrlPrefix = (String) Reflect.getFieldValue(out, "urlPrefix");
      assertEquals(URL_PREFIX, actualUrlPrefix);
      final String actualFromAddress = (String) Reflect.getFieldValue(out, "defaultFromAddress");
      assertEquals(FROM_ADDRESS, actualFromAddress);
      final String actualToAddress = (String) Reflect.getFieldValue(out, "defaultToAddress");
      assertEquals(TO_ADDRESS, actualToAddress);
      final String actualCcAddress = (String) Reflect.getFieldValue(out, "defaultCC");
      assertEquals(CC_ADDRESS, actualCcAddress);
      final String actualBccAddress = (String) Reflect.getFieldValue(out, "defaultBCC");
      assertEquals(BCC_ADDRESS, actualBccAddress);
      final String actualSubject = (String) Reflect.getFieldValue(out, "defaultSubject");
      assertEquals(SUBJECT, actualSubject);
      final boolean actualSubjectIncludeUniqueIdentified = (Boolean) Reflect.getFieldValue(out, "includeUniqueIdentified");
      assertEquals(SUBJECT_INCLUDE_UNIQUE_IDENTIFIED, actualSubjectIncludeUniqueIdentified);
      final String actualMailPriority = (String) Reflect.getFieldValue(out, "defaultPriority");
      assertEquals(MAIL_PRIORITY, actualMailPriority);
      final String actualPlainTextTemplate = (String) Reflect.getFieldValue(out, "plainTextTemplate");
      assertEquals(PLAIN_TEXT_TEMPLATE, actualPlainTextTemplate);
      final boolean actualUseHtml = (Boolean) Reflect.getFieldValue(out, "useHTML");
      assertEquals(USE_HTML, actualUseHtml);
      final String actualHtmlHeader = (String) Reflect.getFieldValue(out, "htmlHeader");
      assertEquals(HTML_HEADER, actualHtmlHeader);
      final String actualHtmlTemplate = (String) Reflect.getFieldValue(out, "htmlTemplate");
      assertEquals(HTML_TEMPLATE, actualHtmlTemplate);
      final String actualHtmlFooter = (String) Reflect.getFieldValue(out, "htmlFooter");
      assertEquals(HTML_FOOTER, actualHtmlFooter);
      final boolean actualCreateProcessHistoryLink = (Boolean) Reflect.getFieldValue(out, "createProcessHistoryLink");
      assertEquals(CREATE_PROCESS_HISTORY_LINK, actualCreateProcessHistoryLink);
      final boolean actualMailResponse = (Boolean) Reflect.getFieldValue(out, "mailResponse");
      assertEquals(MAIL_RESPONSE, actualMailResponse);
      final int actualParameterCount = (Integer) Reflect.getFieldValue(out, "parameterCount");
      assertEquals(PARAMETER_COUNT, actualParameterCount);
      final Map<String, String> actualOutputValues = (Map<String, String>) Reflect.getFieldValue(out, "outValueSetMap");
      assertEquals(FIRST_OUTPUT_VALUE_NAME, actualOutputValues.get(FIRST_OUTPUT_VALUE));
      assertEquals(SECOND_OUTPUT_VALUE_NAME, actualOutputValues.get(SECOND_OUTPUT_VALUE));
   }
   
   @Test
   public void testSetInAccessPointValue()
   {
      final String name = "<Name>";
      final String oldValue = "<Old Value>";
      final String newValue = "<New Value>";
      
      final Pair oldPair = new Pair(name, oldValue);
      final Pair newPair = new Pair(name, newValue);
      
      final List<Pair> accessPointValues = newArrayList();
      accessPointValues.add(oldPair);
      Reflect.setFieldValue(out, "accessPointValues", accessPointValues);
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.setInAccessPointValue(name, newValue);
      
      /* verifying */
      final List<Pair> actualAccessPointValues = (List<Pair>) Reflect.getFieldValue(out, "accessPointValues");
      final Pair actualPair = actualAccessPointValues.get(0);
      assertEquals(newPair, actualPair);
   }
   
   @Test
   public void testGetOutAccessPointValueAlwaysReturnsLastOutputValue()
   {
      final String lastOutputValue = "<last output value>";
      Reflect.setFieldValue(out, "lastOutputValue", lastOutputValue);
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final Object result = out.getOutAccessPointValue(null);
      
      /* verifying */
      assertEquals(lastOutputValue, result);
   }
   
   @Test
   public void testIsSending()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final boolean isSending = out.isSending();
      
      /* verifying */
      assertTrue(isSending);
   }
   
   @Test
   public void testIsReceivingTrue()
   {
      final Map<String, String> nonEmptyMap = newHashMap();
      nonEmptyMap.put(FIRST_OUTPUT_VALUE, FIRST_OUTPUT_VALUE_NAME);
      Reflect.setFieldValue(out, "outValueSetMap", nonEmptyMap);
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final boolean isReceiving = out.isReceiving();
      
      /* verifying */
      assertTrue(isReceiving);
   }
   
   @Test
   public void testIsReceivingFalse()
   {
      final Map<String, String> emptyMap = Collections.emptyMap();
      Reflect.setFieldValue(out, "outValueSetMap", emptyMap);
      
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final boolean isReceiving = out.isReceiving();
      
      /* verifying */
      assertFalse(isReceiving);
   }
   
   @Test
   public void testSend() throws Exception
   {
      final String newMailServer = "<New Mail Server>";
      final String newToAddress = "<New To Address>";
      final String newBccAddress = "<New BCC Address>";
      final String newPriority = "Lowest";
      final String actualSubject = "[" + PI_OID + "#" + AI_OID + "] " + SUBJECT + "(Activity " + ACTIVITY_NAME + ")";
      final Map<String, String> outputValues = newHashMap();
      final String templateOne = "<Template #1>";
      final String templateTwo = "<Template #2>";
      final Object[] inValues = { templateOne, templateTwo, null };
      final List<String> attachmentList = Collections.singletonList("PROCESS_ATTACHMENT");
      outputValues.put(FIRST_OUTPUT_VALUE, FIRST_OUTPUT_VALUE_NAME);
      outputValues.put(SECOND_OUTPUT_VALUE, SECOND_OUTPUT_VALUE_NAME);
      
      /* stubbing */
      final MailAssembler assembler = PowerMockito.mock(MailAssembler.class);
      PowerMockito.whenNew(MailAssembler.class).withArguments( any(), any(), any(), any(), any(),
                                                               any(), any(), any(), anyBoolean(), any(),
                                                               any(), any(), anyBoolean(), anyBoolean(), any(),
                                                               any(), any(), anyLong(), anyLong(), any())
                                                               .thenReturn(assembler);
      defineBootstrapMocks();
      
      /* invoking the method under test */
      out.bootstrap(ai);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "MAIL_SERVER"), newMailServer);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "TO_ADDRESS"), newToAddress);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "BCC_ADDRESS"), newBccAddress);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "MAIL_PRIORITY"), newPriority);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "TEMPLATE_VARIABLE") + "0", templateOne);
      out.setInAccessPointValue((String) Reflect.getStaticFieldValue(MailApplicationInstance.class, "TEMPLATE_VARIABLE") + "1", templateTwo);
      out.setInAccessPointValue(PredefinedConstants.ATTACHMENTS, attachmentList);
      out.send();
      
      /* verifying */
      PowerMockito.verifyNew(MailAssembler.class).withArguments(eq(newMailServer), eq(FROM_ADDRESS), eq(newToAddress), eq(CC_ADDRESS),
                                                                  eq(newBccAddress), eq(newPriority), eq(actualSubject), eq(PLAIN_TEXT_TEMPLATE),
                                                                  eq(USE_HTML), eq(HTML_HEADER), eq(HTML_TEMPLATE), eq(HTML_FOOTER),
                                                                  eq(CREATE_PROCESS_HISTORY_LINK), eq(MAIL_RESPONSE), eq(inValues),
                                                                  eq(outputValues), eq(URL_PREFIX + "/mail-confirmation"), eq(PI_OID), eq(AI_OID),
                                                                  eq(attachmentList));
      verify(assembler).sendMail();
   }
   
   @Test
   public void testReceiveAlwaysReturnsNull()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      final Map<?, ?> result = out.receive(null, null);
      
      /* verifying */
      assertNull(result);
   }
   
   @Test
   public void testCleanupDoesNotThrowException()
   {
      /* stubbing */
      /* nothing to do */
      
      /* invoking the method under test */
      out.cleanup();
      
      /* verifying */
      /* nothing to do */
   }
}
