/**********************************************************************************
 * Copyright (c) 2012, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

/**
 * <p>
 * This utility class allows for
 * <ul>
 *    <li>creating users,</li>
 *    <li>adding grants to existing users, and</li>
 *    <li>removing grants from existing users</li>
 * </ul>
 * for testing purposes.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class UserHome
{
   /**
    * <p>
    * Creates a new user with the given user ID and initializes the created user with the given grants.
    * </p>
    *
    * @param sf a service factory needed for creating the user
    * @param userId the ID the user's <i>account</i>, <i>first name</i>, <i>last name</i>, <i>description</i> and <i>password</i> will be initialized with
    * @param grants the grants the user should be initialized with
    * @return the created user
    */
   public static User create(final ServiceFactory sf, final String userId, final ModelParticipantInfo ... grants)
   {
      return create(sf, new UserCredentials(userId, userId), grants);
   }

   /**
    * <p>
    * Creates a new user with the given user ID and initializes the created user with the given grants.
    * </p>
    *
    * @param sf a service factory needed for creating the user
    * @param credentials the user's credentials. The user's <i>account</i>, <i>first name</i>, <i>last name</i>, <i>description</i> will be initialized with userId. The <i>password</i> will be initialized with password.
    * @param grants the grants the user should be initialized with
    * @return the created user
    */
   public static User create(final ServiceFactory sf, final UserCredentials credentials, final ModelParticipantInfo ... grants)
   {
      if (credentials == null)
      {
         throw new NullPointerException("UserCredentials object must not be null.");
      }
      if (credentials.getUserId() == null)
      {
         throw new NullPointerException("User ID must not be null.");
      }
      if (credentials.getUserId().isEmpty())
      {
         throw new IllegalArgumentException("User ID must not be empty.");
      }

      final List<ModelParticipantInfo> grantList;
      if (grants == null || grants.length == 0)
      {
         grantList = Collections.emptyList();
      }
      else
      {
         grantList = newArrayList();
         Collections.addAll(grantList, grants);
      }

      final CreateUserWithGrantsCommand createUserCmd = new CreateUserWithGrantsCommand(credentials, grantList);
      final User user = (User) sf.getWorkflowService().execute(createUserCmd);
      return user;
   }

   /**
    * <p>
    * Creates a new user with the given user ID and initializes the created user with the given grants.
    * </p>
    *
    * @param userId an ID the user's <i>account<>/i>, <i>first name</i>, <i>last name</i>, <i>description</i> and <i>password</i> will be initialized with
    * @param sf a service factory needed for creating the user
    * @param grants the IDs of the grants the user should be initialized with
    * @return the created user
    */
   public static User create(final ServiceFactory sf, final String userId, final String ... grants)
   {
      final ModelParticipantInfo[] mpGrants = resolveModelParticipants(sf, grants);
      return create(sf, userId, mpGrants);
   }

   /**
    * <p>
    * Creates a new user with the given user ID and does not initializes the created user with any grants.
    * </p>
    *
    * @param userId an ID the user's <i>account</i>, <i>first name</i>, <i>last name</i>, <i>description</i> and <i>password</i> will be initialized with
    * @param sf a service factory needed for creating the user
    * @return the created user
    */
   public static User create(final ServiceFactory sf, final String userId)
   {
      return create(sf, userId, new ModelParticipantInfo[0]);
   }

   public static User modify(final ServiceFactory sf, final User changes)
   {
      return sf.getUserService().modifyUser(changes);
   }

   /**
    * <p>
    * Adds the specified grants to the already existing user.
    * </p>
    *
    * @param sf a service factory needed for adding the grants
    * @param user the already created user
    * @param grants the grants that should be applied to the user
    * @return the user with the specified grants added
    */
   public static User addGrants(final ServiceFactory sf, final User user, final ModelParticipantInfo ... grants)
   {
      for (final ModelParticipantInfo m : grants)
      {
         user.addGrant(m);
      }
      return sf.getUserService().modifyUser(user);
   }

   /**
    * <p>
    * Adds the specified grants to the already existing user.
    * </p>
    *
    * @param sf a service factory needed for adding the grants
    * @param user the already created user
    * @param grants the IDs of the grants that should be applied to the user
    * @return the user with the specified grants added
    */
   public static User addGrants(final ServiceFactory sf, final User user, final String ... grants)
   {
      final ModelParticipantInfo[] mpGrants = resolveModelParticipants(sf, grants);
      return addGrants(sf, user, mpGrants);
   }

   /**
    * <p>
    * Removes the specified grants from the already existing user.
    * </p>
    *
    * @param sf a service factory needed for removing the grants
    * @param user the already created user
    * @param grants the grants that should be removed from the user
    * @return the user with the specified grants removed
    */
   public static User removeGrants(final ServiceFactory sf, final User user, final ModelParticipantInfo ... grants)
   {
      for (final ModelParticipantInfo m : grants)
      {
         user.removeGrant(m);
      }
      return sf.getUserService().modifyUser(user);
   }

   /**
    * <p>
    * Removes the specified grants from the already existing user.
    * </p>
    *
    * @param sf a service factory needed for removing the grants
    * @param user the already created user
    * @param grants the IDs of the grants that should be removed from the user
    * @return the user with the specified grants removed
    */
   public static User removeGrants(final ServiceFactory sf, final User user, final String ... grants)
   {
      final ModelParticipantInfo[] mpGrants = resolveModelParticipants(sf, grants);
      return removeGrants(sf, user, mpGrants);
   }

   /**
    * <p>
    * Removes all grants from the given already existing user.
    * </p>
    *
    * @param sf a service factory needed for removing the grants
    * @param user the already created user
    * @return the user with all grants removed
    */
   public static User removeAllGrants(final ServiceFactory sf, final User user)
   {
      user.removeAllGrants();
      return sf.getUserService().modifyUser(user);
   }

   private static ModelParticipantInfo[] resolveModelParticipants(final ServiceFactory sf, final String ... grants)
   {
      final Set<ModelParticipantInfo> mpGrants = newHashSet();
      if (grants != null)
      {
         final QueryService queryService = sf.getQueryService();
         for (final String s : grants)
         {
            final Participant participant = queryService.getParticipant(s);
            if ( !(participant instanceof ModelParticipantInfo))
            {
               throw new IllegalArgumentException("'" + s + "' is not a model participant.");
            }
            mpGrants.add((ModelParticipantInfo) participant);
         }
      }
      return mpGrants.toArray(new ModelParticipantInfo[mpGrants.size()]);
   }

   private UserHome()
   {
      /* utility class; do not allow the creation of an instance */
   }

   public static final class UserCredentials
   {
      private final String userId;
      private final String password;

      public UserCredentials(String userId, String password)
      {
         this.userId = userId;
         this.password = password;
      }

      public String getPassword()
      {
         return password;
      }

      public String getUserId()
      {
         return userId;
      }
   }

   private static final class CreateUserWithGrantsCommand implements ServiceCommand
   {
      private static final long serialVersionUID = -7717434128443697514L;

      private final UserCredentials credentials;
      private final List<ModelParticipantInfo> grants;

      public CreateUserWithGrantsCommand(final UserCredentials credentials,
            final List<ModelParticipantInfo> grants)
      {
         this.credentials = credentials;
         this.grants = grants;
      }

      @Override
      public User execute(final ServiceFactory sf)
      {
         final UserService userService = sf.getUserService();

         final String userId = credentials.getUserId();
         final String password = credentials.getPassword();

         User user = userService.createUser(userId, userId, userId, userId, password, null, null, null);
         if ( !grants.isEmpty())
         {
            for (final ModelParticipantInfo m : grants)
            {
               user.addGrant(m);
            }
            user = userService.modifyUser(user);
         }
         return user;
      }
   }
}
