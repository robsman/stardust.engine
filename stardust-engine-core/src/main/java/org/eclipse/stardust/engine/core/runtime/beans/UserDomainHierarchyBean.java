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
public class UserDomainHierarchyBean extends IdentifiablePersistentBean implements
      IUserDomainHierarchy, Serializable
{
   private static final Logger trace = LogManager.getLogger(UserDomainHierarchyBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__SUPERDOMAIN = "superDomain";
   public static final String FIELD__SUBDOMAIN = "subDomain";

   public static final FieldRef FR__OID = new FieldRef(UserDomainHierarchyBean.class, FIELD__OID);
   public static final FieldRef FR__SUPERDOMAIN = new FieldRef(UserDomainHierarchyBean.class, FIELD__SUPERDOMAIN);
   public static final FieldRef FR__SUBDOMAIN = new FieldRef(UserDomainHierarchyBean.class, FIELD__SUBDOMAIN);

   public static final String TABLE_NAME = "domain_hierarchy";
   public static final String DEFAULT_ALIAS = "dmh";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "domain_hierarchy_seq";
   public static final String[] domain_hier_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] domain_hier_idx2_UNIQUE_INDEX = new String[] {FIELD__SUPERDOMAIN, FIELD__SUBDOMAIN};

   private UserDomainBean superDomain;
   private UserDomainBean subDomain;

   public static UserDomainHierarchyBean findByOID(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DOMAIN_HIERARCHY_OID.raise(0), 0);
      }
      
      UserDomainHierarchyBean result = (UserDomainHierarchyBean) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL).findByOID(
                  UserDomainHierarchyBean.class, oid);
      
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DOMAIN_HIERARCHY_OID.raise(oid), oid);
      }
      
      return result;
   }

   public UserDomainHierarchyBean()
   {
   }

   public UserDomainHierarchyBean(UserDomainBean superDomain, UserDomainBean subDomain)
   {
      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(//
            UserDomainHierarchyBean.class, QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__SUPERDOMAIN, superDomain.getOID()),//
                  Predicates.isEqual(FR__SUBDOMAIN, subDomain.getOID())))))
      {
         throw new PublicException("Domain hierarchy entry for '" + superDomain
               + "' and '" + subDomain + "' already exists.");
      }

      this.superDomain = superDomain;
      this.subDomain = subDomain;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public String toString()
   {
      return "Domain hierarchy entry: " + superDomain + ", " + subDomain;
   }

   public IUserDomain getSuperDomain()
   {
      fetchLink(FIELD__SUPERDOMAIN);
      return superDomain;
   }
   
   public void setSuperDomain(UserDomainBean superDomain)
   {
      fetchLink(FIELD__SUPERDOMAIN);
      
      if (this.superDomain != superDomain)
      {
         markModified(FIELD__SUPERDOMAIN);
         this.superDomain = superDomain;
      }
   }

   public IUserDomain getSubDomain()
   {
      fetchLink(FIELD__SUBDOMAIN);
      return subDomain;
   }
   
   public void setSubDomain(UserDomainBean subDomain)
   {
      fetchLink(FIELD__SUBDOMAIN);
      
      if (this.subDomain != subDomain)
      {
         markModified(FIELD__SUBDOMAIN);
         this.subDomain = subDomain;
      }
   }
}

