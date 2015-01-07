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

import java.util.Iterator;

import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * Auxiliary class for getting the n:m relationship between user and
 * usergroups.
 * <p/>
 * Users and usergroups are managed by the runtime database.
 */
public class UserUserGroupLink extends IdentifiablePersistentBean
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__USER = "workflowUser";
   public static final String FIELD__USER_GROUP = "userGroup";

   public static final FieldRef FR__OID = new FieldRef(UserUserGroupLink.class, FIELD__OID);
   public static final FieldRef FR__USER = new FieldRef(UserUserGroupLink.class, FIELD__USER);
   public static final FieldRef FR__USER_GROUP = new FieldRef(UserUserGroupLink.class, FIELD__USER_GROUP);

   public static final String TABLE_NAME = "user_usergroup";
   public static final String DEFAULT_ALIAS = "uug";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "user_usergroup_seq";
   public static final String[] user_usergrp_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] user_usergrp_idx2_INDEX = new String[] {FIELD__USER};
   public static final String[] user_usergrp_idx3_INDEX = new String[] {FIELD__USER_GROUP};

   private UserBean workflowUser;
   private static final String workflowUser_EAGER_FETCH = "true";
   private static final String workflowUser_MANDATORY = "true";

   private UserGroupBean userGroup;
   private static final String userGroup_EAGER_FETCH = "true";
   private static final String userGroup_MANDATORY = "true";

   /**
    * Count all link objects for a given user group.
    */
   public static long countAllFor(long userGroupOID)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getCount(
                  UserUserGroupLink.class,
                  QueryExtension.where(
                        Predicates.isEqual(FR__USER_GROUP, userGroupOID)));
   }

   /**
    * Retrieves all link objects for a given user group.
    */
   public static Iterator findAllFor(long userGroupOID)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getVector(
            UserUserGroupLink.class,
            QueryExtension.where(Predicates.isEqual(FR__USER_GROUP, userGroupOID)))
            .iterator();
   }

   public static UserUserGroupLink find(long userGroupOid, long userOid)
   {
      return (UserUserGroupLink) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(UserUserGroupLink.class,
                  QueryExtension.where(
                        Predicates.andTerm(
                              Predicates.isEqual(FR__USER_GROUP, userGroupOid),
                              Predicates.isEqual(FR__USER, userOid))));
   }

   public UserUserGroupLink(IUser workflowUser, IUserGroup userGroup)
   {
      this.workflowUser = (UserBean) workflowUser;
      this.userGroup = (UserGroupBean) userGroup;
   }

   public UserUserGroupLink(long oid, IUser workflowUser, IUserGroup userGroup)
   {
      this.oid = oid;
      this.workflowUser = (UserBean) workflowUser;
      this.userGroup = (UserGroupBean) userGroup;
   }

   public UserUserGroupLink()
   {
   }

   public void delete()
   {
      ((UserBean) getUser()).userGroupLinks.remove(this);

      super.delete();
   }

   public IUser getUser()
   {
      fetchLink(FIELD__USER);

      return workflowUser;
   }

   public IUserGroup getUserGroup()
   {
      fetchLink(FIELD__USER_GROUP);
	  
      return userGroup;
   }
}
