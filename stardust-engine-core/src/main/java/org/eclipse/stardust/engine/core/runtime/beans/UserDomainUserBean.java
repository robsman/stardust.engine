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

import java.io.Serializable;
import java.util.Date;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;



/**
 *
 */
public class UserDomainUserBean extends IdentifiablePersistentBean implements
      IUserDomainUser, Serializable
{
   private static final Logger trace = LogManager.getLogger(UserDomainUserBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__DOMAIN = "domain";
   public static final String FIELD__WFUSER = "wfuser";
   public static final String FIELD__VALID_FROM = "validFrom";
   public static final String FIELD__VALID_TO = "validTo";

   public static final FieldRef FR__OID = new FieldRef(UserUserGroupLink.class, FIELD__OID);
   public static final FieldRef FR__DOMAIN = new FieldRef(UserDomainUserBean.class, FIELD__DOMAIN);
   public static final FieldRef FR__WFUSER = new FieldRef(UserDomainUserBean.class, FIELD__WFUSER);
   public static final FieldRef FR__VALID_FROM = new FieldRef(UserBean.class, FIELD__VALID_FROM);
   public static final FieldRef FR__VALID_TO = new FieldRef(UserBean.class, FIELD__VALID_TO);

   public static final String TABLE_NAME = "wfuser_domain";
   public static final String DEFAULT_ALIAS = "wfud";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "wfuser_domain_seq";
   public static final String[] wfuser_domain_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] wfuser_domain_idx2_UNIQUE_INDEX = new String[] {FIELD__DOMAIN, FIELD__WFUSER};

   private UserDomainBean domain;
   private UserBean wfuser;
   private Date validFrom;
   private Date validTo;

   public static UserDomainUserBean findByOID(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DOMAIN_LINK_OID.raise(0), 0);
      }

      UserDomainUserBean result = (UserDomainUserBean) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL).findByOID(
                  UserDomainUserBean.class, oid);

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DOMAIN_LINK_OID.raise(oid), oid);
      }

      return result;
   }

   public UserDomainUserBean()
   {
   }

   public UserDomainUserBean(UserDomainBean userDomain, UserBean user)
   {
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(//
            UserDomainUserBean.class, QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__DOMAIN, userDomain.getOID()),//
                  Predicates.isEqual(FR__WFUSER, user.getOID())))))
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_USER_DOMAIN_LINK_ALREADY_EXISTS.raise(userDomain,
                     user));
      }

      this.domain = userDomain;
      this.wfuser = user;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public IUserDomain getUserDomain()
   {
      return domain;
   }

   public void setUserDomain(IUserDomain domain)
   {
      this.domain = (UserDomainBean) domain;
   }

   public IUser getUser()
   {
      return wfuser;
   }

   public void setUser(IUser user)
   {
      this.wfuser = (UserBean) user;
   }

   /**
   *
   */
  public Date getValidFrom()
  {
     fetch();
     return validFrom;
  }

  public void setValidFrom(Date validFrom)
  {
     if ( !CompareHelper.areEqual(this.validFrom, validFrom))
     {
        markModified(FIELD__VALID_FROM);
        this.validFrom = validFrom;
     }
  }

  public Date getValidTo()
  {
     fetch();
     return validTo;
  }

  public void setValidTo(Date validTo)
  {
     if (!CompareHelper.areEqual(this.validTo, validTo))
     {
        markModified(FIELD__VALID_TO);
        this.validTo = validTo;
     }
  }

   public String toString()
   {
      return "UserDomainUserLink entry: " + domain + ", " + wfuser;
   }
}

