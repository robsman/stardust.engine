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

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;


public class PreferenceQuery extends Query
{
   private static final long serialVersionUID = 1L;

   /**
    * Allows to define a filter on the preference scope.
    */
   public static final FilterableAttribute SCOPE = new Attribute("scope");

   /**
    * Allows to define a filter on the moduleId.
    */
   public static final FilterableAttribute MODULE_ID = new Attribute("moduleId");

   /**
    * Allows to define a filter on the preferencesId.
    */
   public static final FilterableAttribute PREFERENCES_ID = new Attribute("preferencesId");

   /**
    * Allows to define a filter on the user realm id. Administrator role is required for
    * execution!
    */
   public static final FilterableAttribute REALM_ID = new Attribute("realmId");

   /**
    * Allows to define a filter on the user user id. Administrator role is required for
    * execution!
    */
   public static final FilterableAttribute USER_ID = new Attribute("userId");

   private static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] { //
               FilterTerm.class, //
                     UnaryOperatorFilter.class, //
                     BinaryOperatorFilter.class, //
                     TernaryOperatorFilter.class, //
                     CurrentPartitionFilter.class}), //
         PreferenceQuery.class);

   /**
    * Creates a query for finding all preferences existing.
    *
    * The scope <code>PreferenceScope.DEFAULT</code> is not supported.
    *
    * @return The readily configured query.
    * @throws InvalidArgumentException
    *            if <tt>scope</tt> is null.
    * @throws PublicException
    *            if parameter <code>scope</code> is <code>PreferenceScope.DEFAULT</code>.
    */
   public static PreferenceQuery findAll(PreferenceScope scope)
   {
      if (scope == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("scope"));
      }

      PreferenceQuery query = new PreferenceQuery();

      query.getFilter().add(PreferenceQuery.SCOPE.isEqual(scope.name()));

      return query;
   }

   /**
    * Query for search of preferences.
    *
    * The scope <code>PreferenceScope.DEFAULT</code> is not supported.
    *
    * @param scope
    *           the scope to search in
    * @param moduleId
    *           the module id to search for (wildcard '*' is allowed)
    * @param preferencesId
    *           the preferences id to search for (wildcard '*' is allowed)
    * @return the readily configured query.
    *
    * @throws InvalidArgumentException
    *            if <tt>scope</tt> is null.
    * @throws InvalidArgumentException
    *            if <tt>moduleId</tt> is null.
    * @throws InvalidArgumentException
    *            if <tt>preferencesId</tt> is null.
    * @throws PublicException
    *            if parameter <code>scope</code> is <code>PreferenceScope.DEFAULT</code>.
    */
   public static PreferenceQuery findPreferences(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      if (scope == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("scope"));
      }
      if (null == moduleId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("moduleId"));
      }
      if (null == preferencesId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferencesId"));
      }

      PreferenceQuery query = new PreferenceQuery();

      query.getFilter()
            .add(PreferenceQuery.SCOPE.isEqual(scope.name()))
            .add(PreferenceQuery.MODULE_ID.like(moduleId))
            .add(PreferenceQuery.PREFERENCES_ID.like(preferencesId));

      return query;
   }

   /**
    * Query for search of user scoped preferences belonging to different users and user
    * realms.<br>
    * Users having the Administrator role may use wildcards for realmId and userId. For
    * users without Administrator role the results of the query are restricted to
    * preferences matching the own realmId and own userId.
    *
    * @param realmId
    *           the realmId to search for (wildcard '*' is allowed for administrators).
    *           Specifying <code>null</code> here is interpreted as '*'.
    * @param userId
    *           the userId to search for (wildcard '*' is allowed for administrators).
    *           Specifying <code>null</code> here is interpreted as '*'.
    * @param moduleId
    *           the module id to search for (wildcard '*' is allowed)
    * @param preferencesId
    *           the preferences id to search for (wildcard '*' is allowed)
    * @throws InvalidArgumentException
    *            if <tt>moduleId</tt> is null.
    * @throws InvalidArgumentException
    *            if <tt>preferencesId</tt> is null.
    * @return the readily configured query.
    */
   public static PreferenceQuery findPreferencesForUsers(String realmId, String userId,
         String moduleId, String preferencesId)
   {
      if (null == moduleId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("moduleId"));
      }
      if (null == preferencesId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferencesId"));
      }

      PreferenceQuery query = new PreferenceQuery();

      query.getFilter()
            .add(PreferenceQuery.SCOPE.isEqual(PreferenceScope.USER.name()))
            .add(PreferenceQuery.MODULE_ID.like(moduleId))
            .add(PreferenceQuery.PREFERENCES_ID.like(preferencesId))
            .add(PreferenceQuery.REALM_ID.like(realmId))
            .add(PreferenceQuery.USER_ID.like(userId));

      return query;
   }

   /**
    * Query for search of realm scoped preferences belonging to different realms.<br>
    * Users having the Administrator role may use wildcards for realmId. For users without
    * Administrator role the results of the query are restricted to preferences matching
    * the own realmId.
    *
    * @param realmId
    *           the realmId to search for (wildcard '*' is allowed for administrators).
    *           Specifying <code>null</code> here is interpreted as '*'.
    * @param moduleId
    *           the module id to search for (wildcard '*' is allowed)
    * @param preferencesId
    *           the preferences id to search for (wildcard '*' is allowed)
    * @throws InvalidArgumentException
    *            if <tt>moduleId</tt> is null.
    * @throws InvalidArgumentException
    *            if <tt>preferencesId</tt> is null.
    * @return the readily configured query.
    */
   public static PreferenceQuery findPreferencesForRealms(String realmId,
         String moduleId, String preferencesId)
   {
      if (null == moduleId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("moduleId"));
      }
      if (null == preferencesId)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferencesId"));
      }

      PreferenceQuery query = new PreferenceQuery();

      query.getFilter()
            .add(PreferenceQuery.SCOPE.isEqual(PreferenceScope.REALM.name()))
            .add(PreferenceQuery.MODULE_ID.like(moduleId))
            .add(PreferenceQuery.PREFERENCES_ID.like(preferencesId))
            .add(PreferenceQuery.REALM_ID.like(realmId));

      return query;
   }

   private PreferenceQuery()
   {
      super(FILTER_VERIFYER);
   }

   /**
    * Attribute supporting filter operations.
    * <p />
    * Not for direct use.
    *
    */
   private static final class Attribute extends FilterableAttributeImpl
   {
      private static final long serialVersionUID = 1L;

      private Attribute(String name)
      {
         super(PreferenceQuery.class, name);
      }
   }

}
