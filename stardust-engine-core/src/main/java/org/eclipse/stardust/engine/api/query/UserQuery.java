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
package org.eclipse.stardust.engine.api.query;


import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Query for fetching workflow users matching specific criteria. Most common filter
 * criteria will be the user's attributes.
 * <p />
 * A more advanced use case is retrieving users having a specific role or organization
 * membership by using {@link ParticipantGrantFilter}.
 *
 * @author rsauer
 * @version $Revision$
 */
public class UserQuery extends Query
{
   private static final long serialVersionUID = 7241298334346759237L;

   private static final String FIELD__REALM_ID = UserBean.FIELD__REALM + "_id";
   
   public static final Attribute OID = new Attribute(UserBean.FIELD__OID);
   public static final Attribute ACCOUNT = new Attribute(UserBean.FIELD__ACCOUNT);
   public static final Attribute FIRST_NAME = new Attribute(UserBean.FIELD__FIRST_NAME);
   public static final Attribute LAST_NAME = new Attribute(UserBean.FIELD__LAST_NAME);
   public static final Attribute EMAIL = new Attribute(UserBean.FIELD__EMAIL);
   public static final Attribute VALID_FROM = new Attribute(UserBean.FIELD__VALID_FROM);
   public static final Attribute VALID_TO = new Attribute(UserBean.FIELD__VALID_TO);
   public static final Attribute DESCRIPTION = new Attribute(UserBean.FIELD__DESCRIPTION);
   public static final Attribute FAILED_LOGIN_COUNT = new Attribute(UserBean.FIELD__FAILED_LOGIN_COUNT);
   public static final Attribute LAST_LOGIN_TIME = new Attribute(UserBean.FIELD__LAST_LOGIN_TIME);
   public static final FilterableAttribute REALM_ID = new ReferenceAttribute(
         new Attribute(FIELD__REALM_ID), UserRealmBean.class, UserBean.FIELD__REALM,
         UserRealmBean.FIELD__OID, UserRealmBean.FIELD__ID);
   
   private static final FilterVerifier FILTER_VERIFIER =
         new FilterScopeVerifier(
               new WhitelistFilterVerifyer(
                     new ParticipantAssociationFilterVerifier(
                           new ParticipantAssociationFilter.Kind[]
                           {
                              ParticipantAssociationFilter.FILTER_KIND_MODEL_PARTICIPANT,
                              ParticipantAssociationFilter.FILTER_KIND_USER_GROUP,
                              ParticipantAssociationFilter.FILTER_KIND_TEAM_LEADER,
                              ParticipantAssociationFilter.FILTER_KIND_DEPARTMENT
                           }),
                           new Class[]
                           {
                              FilterTerm.class, 
                              UnaryOperatorFilter.class, 
                              BinaryOperatorFilter.class,
                              TernaryOperatorFilter.class,
                              CurrentPartitionFilter.class,
                              UserStateFilter.class
                           }),
               UserQuery.class);

   /**
    * Creates a query for finding all users.
    * 
    * @return The readily configured query.
    */
   public static UserQuery findAll()
   {
      return new UserQuery();
   }

   /**
    * Creates a query for finding active users.
    * 
    * <p>
    * Active means having no expired {@link #VALID_TO} attribute.
    * </p>
    * 
    * @return The readily configured query.
    */
   public static UserQuery findActive()
   {
      UserQuery query = new UserQuery();

      query.getFilter().addOrTerm().or(VALID_TO.greaterThan(TimestampProviderUtils.getTimeStampValue()))
            .or(VALID_TO.isEqual(0));

      return query;
   }

   /**
    * Creates a query for finding users associated to a given user group.
    * 
    * @return The readily configured query.
    */
   public static UserQuery findAllForUserGroup(String id)
   {
      UserQuery query = findAll();

      query.getFilter().add(ParticipantAssociationFilter.inUserGroup(id));

      return query;
   }

   /**
    * Creates a query for finding active users associated to a given user group.
    * 
    * <p>
    * Active means having no expired {@link #VALID_TO} attribute.
    * </p>
    * 
    * @return The readily configured query.
    */
   public static UserQuery findActiveForUserGroup(String id)
   {
      UserQuery query = findActive();

      query.getFilter().add(ParticipantAssociationFilter.inUserGroup(id));

      return query;
   }

   /**
    * Initializes the query to find all users.
    *
    * @see #findAll()
    */
   public UserQuery()
   {
      super(FILTER_VERIFIER);
      setPolicy(new ModelVersionPolicy(false));
   }

   /**
    * User attribute supporting filter operations.
    * <p />
    * Not for direct use.
    * 
    */
   public static final class Attribute extends FilterableAttributeImpl implements
         FilterableAttribute
   {
      private Attribute(String attribute)
      {
         super(UserQuery.class, attribute);
      }
   }
}