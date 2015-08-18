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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LogEntry;
import org.eclipse.stardust.engine.api.runtime.LogType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ILogEntry;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;


/**
 * <p/>
 * Many methods of the CARNOT EJBs return detail objects. Detail objects are
 * serializable helper objects passed by value to the client. They can, for
 * instance, pass the necessary information from the audit trail to the
 * embedding application in a dynamic way to guarantee an optimum of
 * performance.
 * </p>
 * <p/>
 * Instances of the class LogEntryDetails contain data of runtime
 * objects, e.g. their OID, timestamp, subject and context.
 * </p>
 */
public class LogEntryDetails implements LogEntry
{
   private static final long serialVersionUID = -5360260036339444028L;
   private long oid;
   private Date timeStamp;
   private String subject;
   private long activityInstanceOID;
   private long processInstanceOID;
   private int type;
   private int code;
   private long userOID;
   private User userDetails;
   private String context;

   LogEntryDetails(ILogEntry logEntry)
   {
      oid = logEntry.getOID();
      timeStamp = logEntry.getTimeStamp();

      activityInstanceOID = logEntry.getActivityInstanceOID();
      processInstanceOID = logEntry.getProcessInstanceOID();

      subject = logEntry.getSubject();
      type = logEntry.getType();
      code = logEntry.getCode();
      userOID = logEntry.getUserOID();
      
      PropertyLayer layer = null;      
      if (userOID != 0)
      {
         try
         {
            IUser user =  UserBean.findByOid(userOID);

            Map<String, Object> props = new HashMap<String, Object>();
            props.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
                        
            userDetails = (UserDetails) DetailsFactory.create(user,
                  IUser.class, UserDetails.class);
         }
         catch (ObjectNotFoundException e)
         {
            userDetails = null;
         }
         finally
         {
            if (null != layer)
            {
               ParametersFacade.popLayer();
            }
            
         }
      }

      if (0 != getActivityInstanceOID())
      {
         try
         {
            context = ActivityInstanceBean.findByOID(getActivityInstanceOID())
                  .toString();
         }
         catch (ObjectNotFoundException e)
         {
            context = "Unknown activity instance, oid = " + getActivityInstanceOID();
         }
      }
      else if (0 != getProcessInstanceOID())
      {
         try
         {
            context = ProcessInstanceBean.findByOID(getProcessInstanceOID()).toString();
         }
         catch (ObjectNotFoundException e)
         {
            context = "Unknown process instance, oid = " + getProcessInstanceOID();
         }
      }
      else
      {
         context = "Global";
      }
   }

   public long getOID()
   {
      return oid;
   }

   public Date getTimeStamp()
   {
      return timeStamp;
   }

   public String getSubject()
   {
      return subject;
   }

   public long getActivityInstanceOID()
   {
      return activityInstanceOID;
   }

   public long getProcessInstanceOID()
   {
      return processInstanceOID;
   }

   public long getUserOID()
   {
      return userOID;
   }

   public LogCode getCode()
   {
      return LogCode.getKey(code);
   }

   public LogType getType()
   {
      return LogType.getKey(type);
   }

   public User getUser()
   {
      return userDetails;
   }

   public String getContext()
   {
      return context;
   }
}
