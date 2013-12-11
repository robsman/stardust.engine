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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author mgille
 */
public class MailHelper
{
   public static final Logger trace = LogManager.getLogger(MailHelper.class);

   private static final String PROP_MAIL_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port";

   private static final String PROP_MAIL_SMTP_SOCKETFACTORY_CLASS = "mail.smtp.socketFactory.class";

   private static final String PROP_MAIL_SMTP_AUTH_ENABLED = "mail.smtp.auth";

   private static final String PROP_MAIL_SMTP_PORT = "mail.smtp.port";

   private static final String PROP_MAIL_SMTP_USER = "mail.smtp.user";

   private static final String PROP_MAIL_SMTP_PASSWORD = "mail.smtp.password";

   /**
    * Sends all CARNOT workflow engine mails
    */
   public static void sendSimpleMessage(String[] receivers, String subject, String message)
   {
      String from = Parameters.instance().getString(EngineProperties.MAIL_SENDER);

      if (from == null)
      {
         throw new PublicException("No property '" + EngineProperties.MAIL_SENDER
               + "' specified.");
      }

      String host = Parameters.instance().getString(EngineProperties.MAIL_HOST);

      if (host == null)
      {
         throw new PublicException("No property 'Mail.Host' specified.");
      }

      boolean debug = Parameters.instance()
            .getBoolean(EngineProperties.MAIL_DEBUG, false);

      // Create properties and get the default session

      Properties properties = new Properties();

      properties.put("mail.smtp.host", host);
      properties.put("mail.debug", debug);

      Session session = null;

      boolean smtpAuth = Parameters.instance().getBoolean(PROP_MAIL_SMTP_AUTH_ENABLED,
            true);

      addSmtpProperties(properties);

      if (smtpAuth)
      {
         trace.info("SMTP Auth is set to :" + smtpAuth);
         final String smtpUsername = Parameters.instance().getString(PROP_MAIL_SMTP_USER);
         final String smtpPassword = Parameters.instance().getString(
               PROP_MAIL_SMTP_PASSWORD);

         session = Session.getInstance(properties, new javax.mail.Authenticator()
         {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication()
            {
               return new javax.mail.PasswordAuthentication(smtpUsername, smtpPassword);
            }
         });
      }
      else
      {
         session = Session.getInstance(properties);
      }
      session.setDebug(debug);

      try
      {
         // Create a message

         MimeMessage _message = new MimeMessage(session);

         _message.setFrom(new InternetAddress(from));

         // Separate nulls, If empty simply return.
         Collection _validaddresses = new LinkedList();

         for (int n = 0; n < receivers.length; ++n)
         {
            if (receivers[n] != null)
            {
               _validaddresses.add(new InternetAddress(receivers[n]));
               ;
            }
         }

         int _validAddressesCount = _validaddresses.size();

         if (_validAddressesCount == 0)
         {
            AuditTrailLogger.getInstance(LogCode.ENGINE).warn(
                  "No participant email avaliable as receipient for the message: "
                        + message);
            return;
         }

         InternetAddress[] _internetAddresses = new InternetAddress[_validAddressesCount];

         System.arraycopy(_validaddresses.toArray(), 0, _internetAddresses, 0,
               _validAddressesCount);

         _message.setRecipients(Message.RecipientType.TO, _internetAddresses);
         _message.setSubject(subject);
         _message.setSentDate(new Date());

         // Create and fill the first message part

         MimeBodyPart _mbp1 = new MimeBodyPart();

         _mbp1.setText(message);

         // Create the Multipart and its parts to it

         Multipart _mp = new MimeMultipart();

         _mp.addBodyPart(_mbp1);

         // Add the Multipart to the message

         _message.setContent(_mp);

         // Send the message

         Transport.send(_message);
      }
      catch (MessagingException x)
      {
         trace.warn("", x);
         throw new PublicException("Cannot send notification message: " + x);
      }
   }

   private static Properties addSmtpProperties(Properties properties)
   {
      String socketFactoryPort = Parameters.instance().getString(
            PROP_MAIL_SMTP_SOCKETFACTORY_PORT, "465");
      String socketFactoryClass = Parameters.instance().getString(
            PROP_MAIL_SMTP_SOCKETFACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
      String smtpAuth = Parameters.instance().getString(PROP_MAIL_SMTP_AUTH_ENABLED,
            "true");
      String smtpPort = Parameters.instance().getString(PROP_MAIL_SMTP_PORT, "465");
      // properties.put("mail.smtp.starttls.enable","true");

      properties.put("mail.smtp.socketFactory.port", socketFactoryPort);
      properties.put("mail.smtp.socketFactory.class", socketFactoryClass);
      properties.put("mail.smtp.auth", smtpAuth);
      /*
       * if(smtpAuth){ properties.put("mail.smtp.auth", "true"); } else{
       * properties.put("mail.smtp.auth", "false"); }
       */

      properties.put("mail.smtp.port", smtpPort);

      return properties;
   }
}
