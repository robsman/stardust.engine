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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * 
 */
public class MailAccessPointProvider implements AccessPointProvider
{
   private static final String MAIL_SERVER = "mailServer";

   private static final String FROM_ADDRESS = "fromAddress";

   private static final String TO_ADDRESS = "toAddress";

   private static final String BCC_ADDRESS = "bccAddress";

   private static final String CC_ADDRESS = "ccAddress";

   private static final String MAIL_PRIORITY = "mailPriority";

   private static final String SUBJECT = "subject";

   private static final String TEMPLATE_VARIABLE = "templateVariable";

   private static final String RETURN_VALUE = "returnValue";

   private static final String RESPONSE_MAIL = "responseMail";

   private static final Logger trace = LogManager
         .getLogger(MailAccessPointProvider.class);

   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      Map/* <String, AccessPoint> */result = new HashMap();

      Boolean mailResponseObject = (Boolean) context.get(MailConstants.MAIL_RESPONSE);
      boolean mailResponse = mailResponseObject == null ? false : mailResponseObject
            .booleanValue();

      // IN Data Mapping

      result.put(MAIL_SERVER, JavaDataTypeUtils.createIntrinsicAccessPoint(MAIL_SERVER,
            MAIL_SERVER + ": " + String.class.getName(),
            java.lang.String.class.getName(), Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));
      result.put(FROM_ADDRESS, JavaDataTypeUtils.createIntrinsicAccessPoint(FROM_ADDRESS,
            FROM_ADDRESS + ": " + String.class.getName(), java.lang.String.class
                  .getName(), Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));
      result.put(TO_ADDRESS, JavaDataTypeUtils.createIntrinsicAccessPoint(TO_ADDRESS,
            TO_ADDRESS + ": " + String.class.getName(), java.lang.String.class.getName(),
            Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));

      result.put(CC_ADDRESS, JavaDataTypeUtils.createIntrinsicAccessPoint(CC_ADDRESS,
            CC_ADDRESS + ": " + String.class.getName(), java.lang.String.class.getName(),
            Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));

      result.put(BCC_ADDRESS, JavaDataTypeUtils.createIntrinsicAccessPoint(BCC_ADDRESS,
            BCC_ADDRESS + ": " + String.class.getName(),
            java.lang.String.class.getName(), Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));

      result.put(MAIL_PRIORITY, JavaDataTypeUtils.createIntrinsicAccessPoint(
            MAIL_PRIORITY, MAIL_PRIORITY + ": " + String.class.getName(),
            java.lang.String.class.getName(), Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));

      result.put(SUBJECT, JavaDataTypeUtils.createIntrinsicAccessPoint(SUBJECT, SUBJECT
            + ": " + String.class.getName(), java.lang.String.class.getName(),
            Direction.IN, false, new String[] {
                  JavaAccessPointType.PARAMETER.getId(),
                  JavaAccessPointType.class.getName()}));

      result.put(PredefinedConstants.ATTACHMENTS, JavaDataTypeUtils.createIntrinsicAccessPoint(
              PredefinedConstants.ATTACHMENTS, PredefinedConstants.ATTACHMENTS + ": "
                    + java.util.List.class.getName(), java.util.List.class.getName(),
              Direction.IN, false, new String[] {
                    JavaAccessPointType.PARAMETER.getId(),
                    JavaAccessPointType.class.getName()}));

      int count = getTemplateVariablesCount(context);

      for (int i = 0; i < count; i++)
      {
         result.put(TEMPLATE_VARIABLE + i, JavaDataTypeUtils.createIntrinsicAccessPoint(
               TEMPLATE_VARIABLE + i, TEMPLATE_VARIABLE + i + ": "
                     + java.lang.String.class.getName(),
               java.lang.String.class.getName(), Direction.IN, false, new String[] {
                     JavaAccessPointType.PARAMETER.getId(),
                     JavaAccessPointType.class.getName()}));
      }

      if (mailResponse)
      {
         result.put(RESPONSE_MAIL, JavaDataTypeUtils.createIntrinsicAccessPoint(
               RESPONSE_MAIL, RESPONSE_MAIL + ": ag.carnot.mail.Mail",
               "ag.carnot.mail.Mail", Direction.OUT, true, new String[] {
                     JavaAccessPointType.RETURN_VALUE.getId(),
                     JavaAccessPointType.class.getName()}));
      }
      else
      {
         result.put(RETURN_VALUE, JavaDataTypeUtils.createIntrinsicAccessPoint(
               RETURN_VALUE, RETURN_VALUE + ": " + String.class.getName(),
               java.lang.String.class.getName(), Direction.OUT, true, new String[] {
                     JavaAccessPointType.RETURN_VALUE.getId(),
                     JavaAccessPointType.class.getName()}));
      }

      return result.values().iterator();
   }

   private int getTemplateVariablesCount(Map context)
   {
      String[] templates = new String[] {
            (String) context.get(MailConstants.PLAIN_TEXT_TEMPLATE),
            (String) context.get(MailConstants.HTML_TEMPLATE)
      };

      int count = 0;
      for (int i = 0; i < templates.length; i++)
      {
         String template = templates[i];
         if (template != null)
         {
            MessageFormat templateFormat = new MessageFormat(template);
            count = Math.max(count, templateFormat.getFormats().length);
         }
      }
      return count;
   }
}
