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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.JcrSecurityUtils;
import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.impl.utils.SessionUtils;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;


/**
 * @author sauer
 * @version $Revision$
 */
public class EjbDocumentRepositoryService extends JcrDocumentRepositoryService
      implements ISessionFactory
{
   private static final Logger trace = LogManager.getLogger(EjbDocumentRepositoryService.class);

   private Repository repository;
   
   // TODO per user session management via BpmRuntimeEnvironment
   private Map<String, javax.jcr.Session> jcrSessions = Collections.emptyMap();

   public Repository getRepository()
   {
      return repository;
   }

   public void setRepository(Repository repository)
   {
      this.repository = repository;
   }

   public ISessionFactory getSessionFactory()
   {
      return this;
   }

   public Session getSession() throws RepositoryException
   {
      return retrieveJcrSession(repository);
   }
   
   private boolean isJcrSecurityEnabled()
   {
      return "{JCR_SECURITY}".equals(getJcrUserProperty());
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
   
   public javax.jcr.Session retrieveJcrSession(Repository repository)
         throws LoginException, RepositoryException
   {
      javax.jcr.Session session = null;

      String key;
      if (isJcrSecurityEnabled())
      {
         key = repository.hashCode() + SecurityProperties.getUser().getId();
      }
      else
      {
         key = "" + repository.hashCode();
      }

      if ( !jcrSessions.isEmpty())
      {
         session = (javax.jcr.Session) jcrSessions.get(key);
         try
         {
            if (session != null && !session.isLive())
            {
               session.logout();
               jcrSessions.remove(key);
            }
         }
         catch (Throwable e)
         {
            trace.warn("Could not logout existing jcr session. Cause: " + e.getMessage());
            jcrSessions.remove(key);
            session = null;
         }
         finally
         {

         }
      }

      if (null == session)
      {

         session = repository.login(getJcrCredentials());

         if (jcrSessions.isEmpty())
         {
            this.jcrSessions = Collections.singletonMap(key, session);
         }
         else
         {
            if (1 == jcrSessions.size())
            {
               this.jcrSessions = CollectionUtils.copyMap(jcrSessions);
            }
            jcrSessions.put(key, session);
         }
      }

      return session;
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

      if (isJcrSecurityEnabled())
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

      return JcrSecurityUtils.getCredentialsIncludingParticipantHierarchy(user, jcrPassword);
   }
   
   public void closeJcrSessions()
   {
      if ( !jcrSessions.isEmpty())
      {
         for (Iterator i = jcrSessions.values().iterator(); i.hasNext();)
         {
            final javax.jcr.Session sender = (javax.jcr.Session) i.next();
            // This call checks if JTA handles exist and ensures no logout is performed if
            // no handles exist. (Prevents stuck sessions on Weblogic)
            SessionUtils.logout(sender);
         }

         this.jcrSessions = Collections.EMPTY_MAP;
      }
   }

   public void releaseSession(Session session)
   {
      // ignore, session will be automatically released by closeJcrSessions at end of ejb call.
   }

}
