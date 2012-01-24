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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.mail.*;
import javax.mail.search.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.BatchedPullTriggerEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MailTriggerEvaluator implements BatchedPullTriggerEvaluator
{
   private static final Logger trace = LogManager.getLogger(MailTriggerEvaluator.class);
   
   public Iterator getMatches(ITrigger mailTrigger, long batchSize)
   {
      LinkedList result = new LinkedList();

      String user = (String) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_USER_ATT);
      String password = (String) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_PASSWORD_ATT);
      String server = (String) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_SERVER_ATT);

      MailProtocol protocol = (MailProtocol) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT);

      if (protocol == null)
      {
         protocol = MailProtocol.POP3;
      }
      
      MailTriggerMailboxAction action = (MailTriggerMailboxAction) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_MAILBOX_ACTION_ATT);

      if (action == null)
      {
         action = MailTriggerMailboxAction.REMOVE;
      }
      
      MailTriggerMailFlags flags = (MailTriggerMailFlags) mailTrigger.getAttribute(
            PredefinedConstants.MAIL_TRIGGER_FLAGS_ATT);

      if (flags == null)
      {
         flags = MailTriggerMailFlags.ANY;
      }
      
      Session session = Session.getDefaultInstance(System.getProperties(), null);

      Store store = null;
      try
      {
         store = session.getStore(protocol.getId());
         store.connect(server, user, password);
      }
      catch (MessagingException e)
      {
         trace.warn("server = " + server);
         trace.warn("user = " + user);
         trace.warn("protocol = " + protocol);
         trace.warn("", e);
         throw new UnrecoverableExecutionException(e.getMessage());
      }

      try
      {
         Folder folder = store.getFolder("INBOX");
         if (folder == null)
         {
            throw new InternalException("No default folder in server.");
         }

         List searchTerms = new ArrayList();
         boolean empty = true;
         if (MailTriggerMailFlags.NOT_SEEN.equals(flags))
         {
            empty = false;
            searchTerms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
         }
         else if (MailTriggerMailFlags.RECENT.equals(flags))
         {
            empty = false;
            searchTerms.add(new FlagTerm(new Flags(Flags.Flag.RECENT), true));
         }

         if (mailTrigger.getAllAttributes().containsKey(
               PredefinedConstants.MAIL_TRIGGER_PREDICATE_SENDER_ATT))
         {
            String attribute = (String) mailTrigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PREDICATE_SENDER_ATT);            
            if(!StringUtils.isEmpty(attribute))
            {
               empty = false;
               searchTerms.add(new FromStringTerm(attribute));
            }
         }
         if (mailTrigger.getAllAttributes().containsKey(
               PredefinedConstants.MAIL_TRIGGER_PREDICATE_SUBJECT_ATT))
         {
            String attribute = (String) mailTrigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PREDICATE_SUBJECT_ATT);            
            if(!StringUtils.isEmpty(attribute))
            {
               empty = false;
               searchTerms.add(new SubjectTerm(attribute));               
            }
         }
         if (mailTrigger.getAllAttributes().containsKey(
               PredefinedConstants.MAIL_TRIGGER_PREDICATE_BODY_ATT))
         {
            String attribute = (String) mailTrigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PREDICATE_BODY_ATT);            
            if(!StringUtils.isEmpty(attribute))
            {
               empty = false;
               searchTerms.add(new BodyTerm(attribute));               
            }            
         }

         SearchTerm filter = null;
         if(!empty)
         {
            if (1 == searchTerms.size())
            {
               filter = (SearchTerm) searchTerms.get(0);
            }
            else
            {
               filter = new AndTerm((SearchTerm[]) searchTerms.toArray(new SearchTerm[0]));
            }
         }

         folder.open(Folder.READ_WRITE);
         try
         {
            
            Message messages[];
            if(empty)
            {
               messages = folder.getMessages();
            }
            else
            {
               messages = folder.search(filter);               
            }

            for (int n = 0; (n < messages.length) && (n < batchSize); ++n)
            {
               result.add(new MailTriggerMatch(mailTrigger, messages[n]));

               if (MailTriggerMailboxAction.READ.equals(action))
               {
                  messages[n].setFlag(Flags.Flag.SEEN, true);
               }
               else if (MailTriggerMailboxAction.REMOVE.equals(action))
               {
                  messages[n].setFlag(Flags.Flag.DELETED, true);
               }
               else if ( !MailTriggerMailboxAction.LEAVE.equals(action))
               {
                  trace.warn("Unknown mail trigger mailbox action '" + action
                        + "' for trigger " + mailTrigger.getId() + " (oid: "
                        + mailTrigger.getOID() + ").");
               }
            }
         }
         finally
         {
            // Delete all messages marked deleted
            folder.close(true);
         }

         store.close();
      }
      catch (MessagingException e)
      {
         throw new InternalException(e);
      }
      return result.iterator();
   }
}