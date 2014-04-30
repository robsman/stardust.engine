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
package org.eclipse.stardust.engine.extensions.mail.app;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AsynchronousApplicationInstance;


/**
 *
 */
public class MailApplicationInstance implements AsynchronousApplicationInstance
{
   private static final Logger trace = LogManager
         .getLogger(MailApplicationInstance.class);

   // TODO Should be in base library

   private static final String MAIL_SERVER = "mailServer";

   private static final String JNDI_SESSION = "jndiSession";

   private static final String FROM_ADDRESS = "fromAddress";

   private static final String TO_ADDRESS = "toAddress";

   private static final String CC_ADDRESS = "ccAddress";

   private static final String BCC_ADDRESS = "bccAddress";

   private static final String MAIL_PRIORITY = "mailPriority";

   private static final String SUBJECT = "subject";

   private static final String TEMPLATE_VARIABLE = "templateVariable";

   // private static final String RETURN_VALUE = "returnValue";

   private List accessPointValues = new ArrayList();

   private List outDataMappingOrder = new ArrayList();

   private Application application;

   private String mailServer;

   private String jndiSession;

   private String urlPrefix;

   private String plainTextTemplate;

   private MessageFormat plainTextTemplateFormat;

   private boolean useHTML;

   private String htmlHeader;

   private String htmlTemplate;

   private MessageFormat htmlTemplateFormat;

   private String htmlFooter;

   private String defaultFromAddress;

   private String defaultToAddress;

   private String defaultCC;

   private String defaultBCC;

   private String defaultPriority;

   private String defaultSubject;

   private boolean createProcessHistoryLink;

   private boolean mailResponse;

   private int parameterCount;

   private Map/* <String, String> */outValueSetMap;

   private long processInstanceOID;

   private long activityInstanceOID;

   private String activityName;

   private String lastOutputValue;

   private boolean includeUniqueIdentified;

   public void bootstrap(ActivityInstance activityInstance)
   {
      if (trace.isDebugEnabled())
      {
         trace.info("Bootstrapping mail application instance.");
      }

      this.processInstanceOID = activityInstance.getProcessInstanceOID();
      this.activityInstanceOID = activityInstance.getOID();
      this.activityName = activityInstance.getActivity().getName();
      this.application = activityInstance.getActivity().getApplication();

      this.mailServer = (String) application
            .getAttribute(MailConstants.DEFAULT_MAIL_SERVER);

      this.jndiSession = (String) application
      .getAttribute(MailConstants.DEFAULT_JNDI_SESSION);

      this.urlPrefix = (String) application.getAttribute(MailConstants.URL_PREFIX);
      if (StringUtils.isEmpty(this.urlPrefix))
      {
         this.urlPrefix = "http://localhost";
      }

      this.defaultFromAddress = (String) application
            .getAttribute(MailConstants.DEFAULT_MAIL_FROM);
      this.defaultToAddress = (String) application
            .getAttribute(MailConstants.DEFAULT_MAIL_TO);
      this.defaultCC = (String) application.getAttribute(MailConstants.DEFAULT_MAIL_CC);
      this.defaultBCC = (String) application.getAttribute(MailConstants.DEFAULT_MAIL_BCC);
      this.defaultSubject = (String) application
            .getAttribute(MailConstants.DEFAULT_MAIL_SUBJECT);
      if (StringUtils.isEmpty(defaultSubject))
      {
         this.defaultSubject = "";
      }
      Object attUniqueIdentified = application
            .getAttribute(MailConstants.SUBJECT_INCLUDE_UNIQUE_IDENTIFIED);
      boolean subjectWithDetails = Parameters.instance().getBoolean(
            MailConstants.MAIL_SUBJECT_ENHANCED_WITH_DETAILS, true);
      // If attUniqueIdentified is null then it will be checked if there is
      // Mail.SubjectEnhancedWithDetails (deprecated property) set in carnot.properties.
      // If attUniqueIdentified is null and no property in carnot.properties then
      // includeUniqueIdentified is set to true.
      this.includeUniqueIdentified = attUniqueIdentified == null
            ? subjectWithDetails
            : Boolean.TRUE.equals(attUniqueIdentified);

      this.defaultPriority = (String) application
            .getAttribute(MailConstants.DEFAULT_MAIL_PRIORITY);

      this.plainTextTemplate = (String) application
            .getAttribute(MailConstants.PLAIN_TEXT_TEMPLATE);
      if (StringUtils.isEmpty(plainTextTemplate))
      {
         this.plainTextTemplate = "";
      }

      this.useHTML = Boolean.TRUE
            .equals(application.getAttribute(MailConstants.USE_HTML));
      this.htmlHeader = (String) application.getAttribute(MailConstants.HTML_HEADER);
      this.htmlTemplate = (String) application.getAttribute(MailConstants.HTML_TEMPLATE);
      if (StringUtils.isEmpty(htmlTemplate))
      {
         this.htmlTemplate = "";
      }

      this.htmlFooter = (String) application.getAttribute(MailConstants.HTML_FOOTER);

      this.createProcessHistoryLink = Boolean.TRUE.equals(application
            .getAttribute(MailConstants.CREATE_PROCESS_HISTORY_LINK));

      this.mailResponse = Boolean.TRUE.equals(application
            .getAttribute(MailConstants.MAIL_RESPONSE));

      this.plainTextTemplateFormat = new MessageFormat(plainTextTemplate);
      htmlTemplateFormat = new MessageFormat(htmlTemplate);

      this.parameterCount = plainTextTemplateFormat.getFormats().length;
      if (htmlTemplateFormat.getFormats().length > parameterCount)
      {
         parameterCount = htmlTemplateFormat.getFormats().length;
      }

      List/* <OutputValue> */outputValues = new ArrayList();

      int n = 0;
      while (true)
      {
         if (null == application.getAttribute(MailConstants.OUTPUT_VALUES + "[" + n
               + "].name"))
         {
            break;
         }

         outputValues.add(new OutputValue((String) application
               .getAttribute(MailConstants.OUTPUT_VALUES + "[" + n + "].name"),
               (String) application.getAttribute(MailConstants.OUTPUT_VALUES + "[" + n
                     + "].value")));

         ++n;
      }

      this.outValueSetMap = new HashMap();

      for (int m = 0; m < outputValues.size(); ++m)
      {
         OutputValue outputValue = (OutputValue) outputValues.get(m);

         outValueSetMap.put(outputValue.getValue(), outputValue.getName());
      }

      for (Iterator i = activityInstance.getActivity().getApplicationContext(
            PredefinedConstants.APPLICATION_CONTEXT).getAllOutDataMappings().iterator(); i
            .hasNext();)
      {
         DataMapping mapping = (DataMapping) i.next();

         trace.debug(mapping.getApplicationAccessPoint());
         trace.debug(mapping.getApplicationAccessPoint().getId());
         trace.debug(outDataMappingOrder);

         outDataMappingOrder.add(mapping.getApplicationAccessPoint().getId());
      }
   }

   public void setInAccessPointValue(String name, Object value)
   {
      Pair param = findAccessPointValue(name);

      if (null != param)
      {
         accessPointValues.remove(param);
      }

      accessPointValues.add(new Pair(name, value));
   }

   public Object getOutAccessPointValue(String name)
   {
      // hint: no returnValue access-point here allowed because the
      // getOutAccessPointValue
      // method is only for processing in data mappings.

      try
      {
         return doGetOutAccessPointValue(name, false);
      }
      catch (InvocationTargetException e)
      {
         throw new InternalException(e.getMessage(), e.getTargetException());
      }
   }

   public void cleanup()
   {}

   public boolean isSending()
   {
      return true;
   }

   public boolean isReceiving()
   {
      // do not wait for a response if no output values are modeled at all
      return !outValueSetMap.isEmpty();
   }

   public void send() throws InvocationTargetException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("About to send mail...");
      }

      try
      {
         String actualMailServer = retrieveParam(MAIL_SERVER, mailServer);
         String actualJndiSession = retrieveParam(JNDI_SESSION, jndiSession);
         String actualFromAddress = retrieveParam(FROM_ADDRESS, defaultFromAddress);
         String actualToAddress = retrieveParam(TO_ADDRESS, defaultToAddress);
         String actualCC = retrieveParam(CC_ADDRESS, defaultCC);
         String actualBCC = retrieveParam(BCC_ADDRESS, defaultBCC);
         String actualSubject = retrieveParam(SUBJECT, defaultSubject);
         String actualPriority = retrieveParam(MAIL_PRIORITY, defaultPriority);

         Object obj = findAccessPointValue(PredefinedConstants.ATTACHMENTS);
         if (obj != null)
         {
            obj = ((Pair) obj).getSecond();
         }
         List attList = Collections.EMPTY_LIST;
         if (obj instanceof List)
         {
            attList = (List) obj;
         }

         Object[] inValues = new Object[parameterCount];

         for (int n = 0; n < parameterCount; ++n)
         {
            inValues[n] = retrieveParam(TEMPLATE_VARIABLE + n, null);
         }

         actualSubject = includeUniqueIdentified ? "[" + processInstanceOID + "#"
               + activityInstanceOID + "] " + actualSubject + "(Activity " + activityName
               + ")" : actualSubject;

         MailAssembler assembler = newMailAssembler(actualMailServer, actualJndiSession, actualFromAddress,
               actualToAddress, actualCC, actualBCC,
               actualPriority, actualSubject, inValues,
               attList);

         assembler.sendMail();

         if (trace.isDebugEnabled())
         {
            trace.debug("Mail sent.");
         }
      }
      catch (MessagingException me)
      {
         trace.debug("Failed sending mail.", me);

         throw new InvocationTargetException(me);
      }
   }

   /* package-private */ MailAssembler newMailAssembler(final String actualMailServer,
         final String actualJndiSession,
         final String actualFromAddress,
         final String actualToAddress,
         final String actualCC,
         final String actualBCC,
         final String actualPriority,
         final String actualSubject,
         final Object[] inValues,
         final List<?> attList)
   {
      return new MailAssembler(actualMailServer, actualJndiSession, actualFromAddress,
            actualToAddress, actualCC, actualBCC, actualPriority, actualSubject,
            plainTextTemplate, useHTML, htmlHeader, htmlTemplate, htmlFooter,
            createProcessHistoryLink, mailResponse, inValues, outValueSetMap,
            urlPrefix + "/mail-confirmation", processInstanceOID, activityInstanceOID,
            attList);
   }

   public Map receive(Map data, Iterator outDataTypes)
   {
      // receiving is currently handled at servlet
      return null;
   }

   private String retrieveParam(String key, String defaultValue)
   {
      Object paramObject = retrieveParamObject(key, defaultValue);
      return paramObject != null ? String.valueOf(paramObject) : null;
   }

   private Object retrieveParamObject(String key, Object defaultValue)
   {
      Pair parameter = findAccessPointValue(key);

      if (parameter != null) {
        return parameter.getSecond();
      } else {
        return defaultValue;
      }
   }

   /**
    *
    * @param outDataTypes
    * @return
    * @throws InvocationTargetException
    */
   /*
    * private Map doGetOutAccessPointValues(Set outDataTypes) throws
    * InvocationTargetException { Map result = new HashMap();
    *
    * for (Iterator i = outDataMappingOrder.iterator(); i.hasNext();) { String name =
    * (String) i.next();
    *
    * if (outDataTypes.contains(name)) { result.put(name, doGetOutAccessPointValue(name,
    * true)); } }
    *
    * return result; }
    */

   /**
    *
    * @param name
    * @param allowReturnValue
    * @return
    * @throws InvocationTargetException
    */
   private Object doGetOutAccessPointValue(String name, boolean allowReturnValue)
         throws InvocationTargetException
   {
      return lastOutputValue;
   }

   /**
    *
    * @param name
    * @return
    */
   private Pair findAccessPointValue(String name)
   {
      Pair result = null;

      for (Iterator i = accessPointValues.iterator(); i.hasNext();)
      {
         Pair entry = (Pair) i.next();
         if (name.equals(entry.getFirst()))
         {
            result = entry;
            break;
         }
      }

      return result;
   }
}