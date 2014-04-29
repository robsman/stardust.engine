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
package org.eclipse.stardust.engine.core.runtime.internal.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.MailHelper;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserProperty;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonProperties;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.core.spi.runtime.ISystemAction;



public class PasswordNotifierAction implements ISystemAction
{
   private static String ACTION_ID = "PasswordNotifier";
   private static int DEFAULT_PERIODICITY_DAYS = 1; // in days

   private static final Logger trace = LogManager.getLogger(PasswordNotifierAction.class);
   private static Date lastPass;

   public void run()
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      Date now = new Date();
      int periodicityDays = Parameters.instance().getInteger(AdministrationService.SYSTEM_DAEMON + "." + ACTION_ID + DaemonProperties.DAEMON_PERIODICITY_SUFFIX, DEFAULT_PERIODICITY_DAYS);
      int distance = periodicityDays * 24 * 60 * 60 * 1000;
      if(lastPass != null && lastPass.getTime() + distance > now.getTime())
      {
         return;
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Running " + ACTION_ID);
      }

      for (Iterator iter = AuditTrailPartitionBean.findAll(); iter.hasNext();)
      {
    	  AuditTrailPartitionBean next = (AuditTrailPartitionBean) iter.next();
    	  short oid = next.getOID();
          PasswordRules rules = SecurityUtils.getPasswordRules(oid);

          if(rules != null && rules.isForcePasswordChange() && rules.getNotificationMails() > 0)
          {
        	  QueryDescriptor sqlQuery = QueryDescriptor.from(UserBean.class).select(new Column[] {
        			  UserBean.FR__OID,
        			  UserBean.FR__ACCOUNT,
        			  UserBean.FR__EMAIL,
        			  UserProperty.FR__LAST_MODIFICATION_TIME
        	  }).where(
        			  Predicates.andTerm(
        					  Predicates.orTerm(
        							  Predicates.greaterOrEqual(UserBean.FR__VALID_FROM, System.currentTimeMillis()),
        							  Predicates.isEqual(UserBean.FR__VALID_FROM, 0)),
        					  Predicates.orTerm(
        							  Predicates.greaterOrEqual(UserBean.FR__VALID_FROM, System.currentTimeMillis()),
        							  Predicates.isEqual(UserBean.FR__VALID_FROM, 0)),
  							  Predicates.isEqual(UserProperty.FR__NAME, SecurityUtils.LAST_PASSWORDS),
   							  Predicates.isEqual(UserRealmBean.FR__PARTITION, oid)));

        	  // this join requires that we must have an entry
        	  sqlQuery.innerJoin(UserProperty.class).on(UserBean.FR__OID, UserProperty.FIELD__OBJECT_OID);
        	  sqlQuery.innerJoin(UserRealmBean.class).on(UserBean.FR__REALM, UserRealmBean.FIELD__OID);

        	  ResultSet rsCheckPreconditions = session.executeQuery(sqlQuery);

        	  try
        	  {
        		  while (rsCheckPreconditions.next())
        		  {
        			  Long userOid = rsCheckPreconditions.getLong(1);
        			  String account = rsCheckPreconditions.getString(2);
        			  String email = rsCheckPreconditions.getString(3);
        			  long lastModified = rsCheckPreconditions.getLong(4);

        			  Calendar expires = Calendar.getInstance();
        			  expires.setTimeInMillis(lastModified);
        			  expires.add(Calendar.DAY_OF_YEAR, rules.getExpirationTime());

        			  Calendar disabled = Calendar.getInstance();
        			  disabled.setTimeInMillis(expires.getTimeInMillis());
        			  disabled.add(Calendar.DAY_OF_YEAR, rules.getDisableUserTime());

        			  Calendar notification = Calendar.getInstance();
        			  notification.setTimeInMillis(lastModified);
        			  notification.add(Calendar.DAY_OF_YEAR, rules.getExpirationTime());
        			  notification.add(Calendar.DAY_OF_YEAR, - rules.getNotificationMails());

        			  UserBean user_ = UserBean.findByOid(userOid);
        			  // not administrator role
        			  if(!user_.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE)
        					  && rules.isForcePasswordChange()
        					  && rules.getNotificationMails() != 0
        					  && notification.getTime().getTime() <= now.getTime())
        			  {
        				  if(!StringUtils.isEmpty(email))
        				  {
        					  try
        					  {
        						  int disableUser = rules.getDisableUserTime();
        						  if(disableUser != -1 && disabled.getTime().getTime() <= now.getTime())
        						  {
        							  MailHelper.sendSimpleMessage(new String[] {email}, "Account got disabled!",
        									  "Dear user '" + account + "'!\n\n" +
        									  "Your account got deactivated on " + disabled.getTime() + ". Please contact an Administrator.");
        						  }
        						  else if(expires.getTime().getTime() <= now.getTime())
        						  {
        							  String message = "Dear user '" + account + "'!\n\n" +
        							  		"Your password has expired on " + expires.getTime() + ". Please change your password or contact an Administrator.";
        							  if(disableUser != -1)
        							  {
        								  message += "\nYour password must be changed, otherwise your account will be deactivated on " + disabled.getTime() + ".";
        							  }
        							  MailHelper.sendSimpleMessage(new String[] {email}, "Password has expired!", message);
        						  }
        						  else
        						  {
        							  MailHelper.sendSimpleMessage(new String[] {email}, "Password will expire!",
        									  "Dear user '" + account + "'!\n\n" +
        									  "Your password will expire on " + expires.getTime() + ". Please change your password or contact an Administrator.");
        						  }
        					  }
        					  catch (Exception e)
        					  {
        						  // do nothing
        					  }
        				  }
        				  else
        				  {
        					  trace.info("User '" + account + "' has no email address defined!");
        				  }
        			  }
        		  }
        	  }
        	  catch (SQLException sqle)
        	  {
               throw new PublicException(
                     BpmRuntimeError.BPMRT_FAILED_VERIFIYING_PRECONDITIONS.raise(), sqle);
        	  }
        	  finally
        	  {
        		  lastPass = new Date();
        		  QueryUtils.closeResultSet(rsCheckPreconditions);
        	  }
          }
      }
   }

   public String getId()
   {
      return ACTION_ID;
   }
}