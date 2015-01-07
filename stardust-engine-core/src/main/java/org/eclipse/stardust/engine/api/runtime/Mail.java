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
package org.eclipse.stardust.engine.api.runtime;

import java.util.Date;
import java.util.List;

/**
 * A lightweight representation of a mail message. It provides simplified access to the
 * basic information contained in a mail message.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Mail
{
   /**
    * Gets the value of the Message-ID header, if available.
    * 
    * @return The message ID header, uniquely identifying the mail on the server.
    * 
    * @since 3.1
    */
   String getMessageId();

   /**
    * Gets the sender of this message.
    *
    * @return the sender of the message.
    */
   String getSender();

   /**
    * Gets the list of recipients.
    *
    * @return a List of strings representing recipients.
    */
   List getToRecipients();

   /**
    * Gets the list of CC recipients.
    *
    * @return a List of strings representing recipients.
    */
   List getCcRecipients();

   /**
    * Gets the list of Bcc recipients.
    *
    * @return a List of strings representing recipients.
    */
   List getBccRecipients();

   /**
    * Gets all recipients.
    *
    * @return the concatenated list of recipients, CC recipients and BCC recipients.
    */
   List getAllRecipients();

   /**
    * Gets the list of replyTo addresses.
    *
    * @return a List of strings containing the replyTo addresses.
    */
   List getReplyTo();

   /**
    * Gets the subject of this mail message.
    *
    * @return the subject of the mail.
    */
   String getSubject();

   /**
    * Gets the time when the mail message was sent.
    *
    * @return the sent time.
    */
   Date getSentDate();

   /**
    * Gets the time when the mail message was received.
    *
    * @return the receive time.
    */
   Date getReceivedDate();

   /**
    * Gets the number of lines contained in that message.
    *
    * @return the line count.
    */
   int getLineCount();

   /**
    * Gets the mime type of the content.
    *
    * @return the mime type of the content.
    */
   String getContentType();

   /**
    * Gets the content of this message.
    *
    * @return the content of the message.
    */
   Object getContent();

   /**
    * Gets the string content of this message.
    *
    * @return the string content of the message.
    */
   String getStringContent();

   /**
    * Gets the size of the content.
    *
    * @return the message content size.
    */
   int getSize();
}
