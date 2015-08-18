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
package org.eclipse.stardust.engine.extensions.mail.trigger;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.MailDetails;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.runtime.Mail;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.TriggerMatch;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author ubirkemeyer, rsauer
 * @version $Revision$
 */
public class MailTriggerMatch implements TriggerMatch
{
   private static final Logger trace = LogManager.getLogger(MailTriggerMatch.class);

   private static final String PLAIN_MESSAGE_TAG = "text/plain";
   private static final String HTML_MESSAGE_TAG = "text/html";
   private static final String MULTIPART_MESSAGE_TAG = "multipart/mixed";
   private static final String MULTIPART_ALTERNATIVE_MESSAGE_TAG = "multipart/alternative";

   private final ITrigger trigger;

   private final String messageId;
   private final String sender;
   private final String[] replyTo;
   private final String[] receiver;
   private final String[] ccReceiver;
   private final String[] bccReceiver;
   private final Calendar sentDate;
   private final Calendar receivedDate;
   private final String subject;
   private final String body;
   private final String contentType;
   private final int lineCount;
   private final int size;

   /**
    * Processes a mail message which maps to the provided mail trigger.
    */
   public MailTriggerMatch(ITrigger trigger, Message message)
   {
      this.trigger = trigger;

      sentDate = TimestampProviderUtils.getCalendar();
      receivedDate = TimestampProviderUtils.getCalendar();

      try
      {
         String[] ids = message.getHeader("MESSAGE-ID");
         if (ids != null && 1 <= ids.length)
         {
            this.messageId = ids[0];
            if (1 < ids.length)
            {
               trace.debug("Ignoring all but first message ID header: " + ids);
            }
         }
         else
         {
            trace.info("Triggered by a mail which is missing a message ID header.");
            this.messageId = "";
         }
         
         StringBuffer buffer = new StringBuffer();

         this.contentType = message.getContentType();
         if (message.getContentType().toLowerCase().startsWith(PLAIN_MESSAGE_TAG) ||
               message.getContentType().toLowerCase().startsWith(HTML_MESSAGE_TAG))
         {
            buffer.append((String) message.getContent());
         }
         else if (message.getContentType().toLowerCase().startsWith(MULTIPART_MESSAGE_TAG) ||
               message.getContentType().toLowerCase().startsWith(MULTIPART_ALTERNATIVE_MESSAGE_TAG))
         {
            extractMultipart((Multipart) message.getContent(), buffer);
         }
         else
         {
            buffer.append("<" + message.getContentType() + ">");
         }
         
         this.lineCount = message.getLineCount();
         this.size = message.getSize();

         this.sender = (0 < message.getFrom().length)
               ? message.getFrom()[0].toString()
               : "";

         this.replyTo = extractAdresses(message.getReplyTo());
         this.receiver = extractAdresses(message.getRecipients(RecipientType.TO));
         this.ccReceiver = extractAdresses(message.getRecipients(RecipientType.CC));
         this.bccReceiver = extractAdresses(message.getRecipients(RecipientType.BCC));

         try
         {
            sentDate.setTime(message.getSentDate());
         }
         catch (NullPointerException e)
         {
            trace.warn("Unable to extract when sent.", e);
         }

         if (message.getReceivedDate() != null)
         {
            receivedDate.setTime(message.getReceivedDate());
         }

         subject = message.getSubject();
         body = buffer.toString();
      }
      catch (Exception x)
      {
         throw new PublicException(x);
      }
   }

   public String getMessageId()
   {
      return messageId;
   }

   public Map getData()
   {
      // todo: (france, fh) provides the complete list of the receiver, cc and bcc.
      Mail mailDetails = new MailDetails(messageId, sender, receiver, ccReceiver, bccReceiver,
            replyTo, subject, sentDate.getTime().getTime(), receivedDate.getTime()
                  .getTime(), body, contentType, lineCount, size);

      Map data = new HashMap();
      Iterator apItr = trigger.getAllAccessPoints();
      while (apItr.hasNext())
      {
         IAccessPoint ap = (IAccessPoint) apItr.next();
         if (null != ap)
         {
            data.put(ap.getId(), mailDetails);
         }
      }

      return data;
   }

   private String[] extractAdresses(Address[] addresses)
   {
      String[] result = new String[(null != addresses) ? addresses.length : 0];
      for (int i = 0; i < result.length; i++)
      {
         result[i] = (addresses[i] instanceof InternetAddress)
               ? ((InternetAddress) addresses[i]).getAddress()
               : addresses[i].toString();
      }
      return result;
   }

   /**
    * Extracts the content of a mulitpart mail and puts the content in the
    * string buffer provided with <tt>buffer</tt>.
    */
   private void extractMultipart(Multipart multipart, StringBuffer buffer)
   {
      try
      {
         for (int i = 0; i < multipart.getCount(); ++i)
         {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.getContentType().toLowerCase().startsWith(PLAIN_MESSAGE_TAG) ||
                  bodyPart.getContentType().toLowerCase().startsWith(HTML_MESSAGE_TAG))
            {
               buffer.append((String) bodyPart.getContent());
            }
            else if (bodyPart.getContentType().toLowerCase().startsWith(MULTIPART_MESSAGE_TAG) ||
                  bodyPart.getContentType().toLowerCase().startsWith(MULTIPART_ALTERNATIVE_MESSAGE_TAG))
            {
               extractMultipart((Multipart) bodyPart.getContent(), buffer);
            }
            else
            {
               buffer.append("<" + bodyPart.getContentType() + ">");
            }
         }
      }
      catch (MessagingException x)
      {
         throw new InternalException(x);
      }
      catch (IOException x)
      {
         throw new InternalException(x);
      }
   }
}