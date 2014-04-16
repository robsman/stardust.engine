/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.repository.jcr;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.JcrSecurityUtils;
import org.eclipse.stardust.vfs.impl.utils.SessionUtils;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;

public class LocalSessionFactory implements ISessionFactory
{
   private Session session;

   private boolean userLevelAuthorization;

   public LocalSessionFactory(Session session)
   {
      this.session = session;
   }

   public LocalSessionFactory(Repository repository, boolean userLevelAuthorization)
   {
      this.userLevelAuthorization = userLevelAuthorization;
      Session session = null;
      try
      {
         session = repository.login(getJcrCredentials());
      }
      catch (LoginException e)
      {
         throw new PublicException(e);
      }
      catch (RepositoryException e)
      {
         throw new PublicException(e);
      }

      this.session = session;
   }

   @Override
   public Session getSession() throws RepositoryException
   {
      return session;
   }

   @Override
   public void releaseSession(Session session)
   {
      // is controlled externally
   }

   public void closeJcrSessions()
   {
      try
      {
         session.save();
      }
      catch (RepositoryException e)
      {
         throw new PublicException(e);
      }

      // This call checks if JTA handles exist and ensures no logout is performed if
      // no handles exist. (Prevents stuck sessions on Weblogic)
      SessionUtils.logout(session);
   }

   private boolean isUserAuthorizationEnabled()
   {
      return userLevelAuthorization || "{JCR_SECURITY}".equals(getJcrUserProperty());
   }

   private String getJcrUserProperty()
   {
      final Parameters params = Parameters.instance();
      return params.getString("ContentRepository.User", "ipp-jcr-user");
   }

   private String getJcrPasswordProperty()
   {
      final Parameters params = Parameters.instance();
      return params.getString("ContentRepository.Password", "ipp-jcr-password");
   }

   /**
    * Supplies the credentials for login into a jcrSession. The ipp userId is used as
    * jcrUsername by default. The password always is "ipp-jcr-password" and is not used by
    * the current jcr-security implementation. Both can be modified by using the
    * properties 'ContentRepository.User' and 'ContentRepository.Password'. <br>
    * Ipp users with Administrator role get the "administrators" group assigned to join
    * the jcr-security group for administrators.
    * 
    * @return the SimpleCredentials for login into a jcrSession.
    */
   public Credentials getJcrCredentials()
   {
      SimpleCredentials credentials;
      String jcrUser = getJcrUserProperty();
      String jcrPassword = getJcrPasswordProperty();

      if (isUserAuthorizationEnabled())
      {
         credentials = getIppCredentials(jcrPassword);
      }
      else
      {
         credentials = new SimpleCredentials(jcrUser, jcrPassword.toCharArray());
      }

      return credentials;
   }

   public static SimpleCredentials getIppCredentials(String jcrPassword)
   {
      IUser user = SecurityProperties.getUser();

      return JcrSecurityUtils.getCredentialsIncludingParticipantHierarchy(user,
            jcrPassword);
   }

}
