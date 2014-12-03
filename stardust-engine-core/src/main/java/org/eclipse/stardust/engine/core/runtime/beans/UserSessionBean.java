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

import java.util.Date;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;



/**
 *
 */
public class UserSessionBean extends IdentifiablePersistentBean
      implements IUserSession
{
   private static final long serialVersionUID = 1L;
   
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__USER = "workflowUser";
   public static final String FIELD__CLIENT_ID = "clientId";
   public static final String FIELD__START_TIME = "startTime";
   public static final String FIELD__LAST_MODIFICATION_TIME = "lastModificationTime";
   public static final String FIELD__EXPIRATION_TIME = "expirationTime";
   
   public static final FieldRef FR__OID = new FieldRef(UserSessionBean.class, FIELD__OID);
   public static final FieldRef FR__USER = new FieldRef(UserSessionBean.class, FIELD__USER);
   public static final FieldRef FR__CLIENT_ID = new FieldRef(UserSessionBean.class, FIELD__CLIENT_ID);
   public static final FieldRef FR__START_TIME = new FieldRef(UserSessionBean.class, FIELD__START_TIME);
   public static final FieldRef FR__LAST_MODIFICATION_TIME = new FieldRef(UserSessionBean.class, FIELD__LAST_MODIFICATION_TIME);
   public static final FieldRef FR__EXPIRATION_TIME = new FieldRef(UserSessionBean.class, FIELD__EXPIRATION_TIME);

   public static final String TABLE_NAME = "wfuser_session";
   public static final String DEFAULT_ALIAS = "us";
   public static final String[] PK_FIELD = new String[] {FIELD__OID};
   protected static final String PK_SEQUENCE = TABLE_NAME + "_seq";

   public static final String[] wfusr_session_idx1_UNIQUE_INDEX =
         new String[]{FIELD__OID};
   public static final String[] wfusr_session_idx2_INDEX =
      new String[]{FIELD__USER, FIELD__START_TIME};

   private long workflowUser;
   
   private String clientId;

   private long startTime;

   private long lastModificationTime;
   
   private long expirationTime;

   public static UserSessionBean findByOid(long oid) throws ObjectNotFoundException
   {
      if (0 == oid)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_SESSION_OID.raise(0), 0);
      }
      
      UserSessionBean result = (UserSessionBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(UserSessionBean.class, oid);

      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_SESSION_OID.raise(oid), oid);
      }

      return result;
   }
   
   public UserSessionBean()
   {
   }

   public UserSessionBean(IUser user, String clientId)
   {
      this.workflowUser = user.getOID();
      
      this.clientId = clientId;

      this.startTime = TimestampProviderUtils.getTimeStamp().getTime();
      this.lastModificationTime = startTime;

      this.expirationTime = SessionManager.instance().getExpirationTime(
            lastModificationTime);
      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /*
    * Retrieves the session's user.
    */
   public long getUserOid()
   {
      fetch();

      return workflowUser;
   }

   /*
    * Retrieves the session's user.
    */
   public IUser getUser()
   {
      fetch();
      
      return UserBean.findByOid(workflowUser);
   }

   public String getClientId()
   {
      fetch();
      
      return clientId;
   }

   public Date getStartTime()
   {
      fetch();

      return new Date(startTime);
   }

   public Date getLastModificationTime()
   {
      fetch();

      return new Date(lastModificationTime);
   }

   public void setLastModificationTime(Date lastModificationTime)
   {
      markModified(FIELD__LAST_MODIFICATION_TIME);

      this.lastModificationTime = lastModificationTime.getTime();
   }

   public Date getExpirationTime()
   {
      fetch();

      return new Date(expirationTime);
   }

   public void setExpirationTime(Date expirationTime)
   {
      markModified(FIELD__EXPIRATION_TIME);

      this.expirationTime = expirationTime.getTime();
   }

}
