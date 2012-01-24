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
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.engine.api.runtime.Mail;


/**
 * Lightweight representation of a mail message
 * 
 * @author kwinkler
 * @version $Revision$
 */
public class MailDetails implements Mail, Serializable
{
   /**
    * Serial version UID excluding the message ID field, to preserve backward
    * compatibility.
    */
   private static final long serialVersionUID = 9147772717266265430L;

   private String sender;

   private final List replyTo;

   private final List toRecipients;
   private final List cCRecipients;
   private final List bCCRecipients;

   private long sentDate;
   private long receivedDate;

   private String subject;
   private String content;
   private String contentType;
   private int lineCount;
   private int size;
   
   // not included in serialVersionUID to preserve backward compatibility
   private String messageId;

   public MailDetails()
   {
      this.toRecipients = Collections.EMPTY_LIST;
      this.cCRecipients = Collections.EMPTY_LIST;
      this.bCCRecipients = Collections.EMPTY_LIST;

      this.replyTo = Collections.EMPTY_LIST;
   }

   public MailDetails(String messageId, String sender, String[] recipientsTo,
         String[] recipientsCc, String[] recipientsBcc, String[] replyTo, String subject,
         long sentDate, long receivedDate, String content, String contentType,
         int lineCount, int size)
   {
      this.messageId = messageId;
      
      this.sender = sender;
      this.subject = subject;
      this.sentDate = sentDate;
      this.content = content;
      this.contentType = contentType;
      this.lineCount = lineCount;
      this.size = size;

      this.receivedDate = receivedDate;
      this.toRecipients = toList(recipientsTo);
      this.cCRecipients = toList(recipientsCc);
      this.bCCRecipients = toList(recipientsBcc);

      this.replyTo = toList(replyTo);
   }

   public String getMessageId()
   {
      return messageId;
   }

   public String getSender()
   {
      return sender;
   }

   public List getToRecipients()
   {
      return Collections.unmodifiableList(toRecipients);
   }

   public List getCcRecipients()
   {
      return Collections.unmodifiableList(cCRecipients);
   }

   public List getBccRecipients()
   {
      return Collections.unmodifiableList(bCCRecipients);
   }

   public List getAllRecipients()
   {
      return new ConcatenatedList(
            new ConcatenatedList(toRecipients, cCRecipients),bCCRecipients);
   }

   public List getReplyTo()
   {
      return Collections.unmodifiableList(replyTo);
   }

   public String getSubject()
   {
      return subject;
   }

   public Date getSentDate()
   {
      return new Date(sentDate);
   }

   public Date getReceivedDate()
   {
      return new Date(receivedDate);
   }

   public int getLineCount()
   {
      return lineCount;
   }

   public String getContentType()
   {
      return contentType;
   }

   public Object getContent()
   {
      return content;
   }

   public String getStringContent()
   {
      return content;
   }

   public int getSize()
   {
      return size;
   }

   private List toList(String[] array)
   {
      if (null == array)
      {
         return Collections.EMPTY_LIST;
      }
      return new ArrayList(Arrays.asList(array));
   }
}
