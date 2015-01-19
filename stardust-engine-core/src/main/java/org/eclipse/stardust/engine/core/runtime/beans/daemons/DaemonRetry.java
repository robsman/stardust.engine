/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.MailHelper;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

public class DaemonRetry
{
   private int retries;

   private int delay;

   private int retriesLeft;

   private ForkingService service;

   private static final Logger trace = LogManager.getLogger(DaemonRetry.class);

   public DaemonRetry(ForkingService service)
   {
      this.service = service;
      retries = Parameters.instance().getInteger(DaemonProperties.DAEMON_RETRY_NUMBER, 3);
      delay = Parameters.instance().getInteger(DaemonProperties.DAEMON_RETRY_DELAY, 500);
      retries = retries >= 0 ? retries : 0;
      retriesLeft = retries;
   }

   public boolean hasRetriesLeft()
   {
      return retriesLeft >= 0;
   }

   public void handleException(Exception e) throws Exception
   {
      if (retriesLeft > 0)
      {
         trace.warn("Unexpected exception : " + e.getMessage());
         trace.warn("Retrying "
               + retriesLeft
               + ((1 < retriesLeft)
                     ? " times with " + delay + " ms delay."
                     : " time with " + delay + " ms delay."));
      }
      retriesLeft--;
      if (!hasRetriesLeft())
      {
         trace.warn("All " + retries + " retries failed.");
         throw e;
      }
   }

   public void delayRetry()
   {
      try
      {
         Thread.sleep(delay);
      }
      catch (InterruptedException e)
      {
      }
   }

   public void sendErrorMail(Exception e)
   {
      List<String> receivers = getAllAdminMailAddresses();
      if (!receivers.isEmpty() && mailPropertiesAvailable())
      {
         trace.warn("Daemon execution will be stopped now. Sending mail to all admin users.");
         MailHelper.sendSimpleMessage(receivers.toArray(new String[receivers.size()]),
               "All retries failed.",
               "All retries for daemon execution failed. It will be stopped now. " + e);
      }
   }

   private List<String> getAllAdminMailAddresses()
   {
      ResultIterator allUsers = (ResultIterator) service
            .isolate(new FindAdminUserAction());
      List<String> receivers = new ArrayList<String>();
      if (allUsers != null)
      {
         while (allUsers.hasNext())
         {
            UserBean user = (UserBean) allUsers.next();
            if (user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
            {
               String eMail = user.getEMail();
               if (StringUtils.isNotEmpty(eMail))
               {
                  receivers.add(eMail);
               }
            }
         }
      }
      return receivers;
   }

   private boolean mailPropertiesAvailable()
   {
      String from = Parameters.instance().getString(EngineProperties.MAIL_SENDER);
      String host = Parameters.instance().getString(EngineProperties.MAIL_HOST);
      return StringUtils.isNotEmpty(from) && StringUtils.isNotEmpty(host);
   }

   private class FindAdminUserAction implements Action
   {
      @Override
      public Object execute()
      {
         return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
               UserBean.class);
      }
   }

}
