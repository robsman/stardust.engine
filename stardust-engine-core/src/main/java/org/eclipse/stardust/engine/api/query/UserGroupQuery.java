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

import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Query for fetching workflow user groups matching specific criteria. Most common filter
 * criteria will be the user group's attributes.
 *
 * @author rsauer
 * @version $Revision$
 */
public class UserGroupQuery extends Query
{
   public static final Attribute OID = new Attribute(UserGroupBean.FIELD__OID);
   public static final Attribute ID = new Attribute(UserGroupBean.FIELD__ID);
   public static final Attribute NAME = new Attribute(UserGroupBean.FIELD__NAME);
   public static final Attribute VALID_FROM = new Attribute(UserGroupBean.FIELD__VALID_FROM);
   public static final Attribute VALID_TO = new Attribute(UserGroupBean.FIELD__VALID_TO);
   public static final Attribute DESCRIPTION = new Attribute(UserGroupBean.FIELD__DESCRIPTION);

   private static final FilterVerifier FILTER_VERIFIER =
      new FilterScopeVerifier(
            new WhitelistFilterVerifyer(
                  new ParticipantAssociationFilterVerifier(
                        new ParticipantAssociationFilter.Kind[]
                        {
                           ParticipantAssociationFilter.FILTER_KIND_USER
                        }),
                        new Class[]
                        {
                           FilterTerm.class,
                           UnaryOperatorFilter.class,
                           BinaryOperatorFilter.class,
                           TernaryOperatorFilter.class,
                           CurrentPartitionFilter.class
                        }),
            UserGroupQuery.class);

   /**
    * Creates a query for finding all user groups.
    *
    * @return The readily configured query.
    */
   public static UserGroupQuery findAll()
   {
      return new UserGroupQuery();
   }

   /**
    * Creates a query for finding active user groups.
    *
    * <p>Active means having no expired {@link #VALID_TO} attribute.</p>
    *
    * @return The readily configured query.
    */
   public static UserGroupQuery findActive()
   {
      UserGroupQuery query = new UserGroupQuery();

      query.getFilter().addOrTerm().or(VALID_TO.greaterThan(TimestampProviderUtils.getTimeStampValue()))
            .or(VALID_TO.isEqual(0));

      return query;
   }

   /**
    * Creates a query for finding user group associated to a given user.
    * 
    * @return The readily configured query.
    */
   public static UserGroupQuery findAllForUser(String account)
   {
      UserGroupQuery query = findAll();
      
      query.getFilter().add(ParticipantAssociationFilter.forUser(account));
      
      return query;
   }
   
   /**
    * Creates a query for finding active user groups associated to a given user.
    * 
    * <p>
    * Active means having no expired {@link #VALID_TO} attribute.
    * </p>
    * 
    * @return The readily configured query.
    */
   public static UserGroupQuery findActiveForUser(String account)
   {
      UserGroupQuery query = findActive();
      
      query.getFilter().add(ParticipantAssociationFilter.forUser(account));

      return query;
   }
   
   /**
    * Initializes the query to find all users.
    *
    * @see #findAll()
    */
   public UserGroupQuery()
   {
      super(FILTER_VERIFIER);
      setPolicy(new ModelVersionPolicy(false));
   }

   /**
    * User attribute supporting filter operations.
    * <p />
    * Not for direct use.
    * 
    * @deprecated Use {@link FilterableAttribute} instead.
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String attribute)
      {
         super(UserGroupQuery.class, attribute);
      }
   }
}
