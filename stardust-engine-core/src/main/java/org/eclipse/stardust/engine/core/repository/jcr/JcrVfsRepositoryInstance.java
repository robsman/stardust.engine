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

import java.util.Iterator;
import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.extensions.dms.data.JcrSecurityUtils;
import org.eclipse.stardust.vfs.impl.utils.SessionUtils;

public class JcrVfsRepositoryInstance implements IRepositoryInstance
{
   private static final long serialVersionUID = -5256677063628195043L;

   protected String repositoryId;
   
   protected String providerId;

   protected String partitionId;

   protected String jndiName;

   protected JcrVfsRepositoryInstanceInfo repositoryInfo;

   protected Repository repository;

   protected boolean userLevelAuthorization;

   public JcrVfsRepositoryInstance(IRepositoryConfiguration configuration, String partitionId)
   {
      super();
      this.partitionId = partitionId;
      this.repositoryId = (String) configuration.getAttributes().get(IRepositoryConfiguration.REPOSITORY_ID);
      this.providerId = (String) configuration.getAttributes().get(IRepositoryConfiguration.PROVIDER_ID);
      this.jndiName = (String) configuration.getAttributes().get(JcrVfsRepositoryConfiguration.JNDI_NAME);
      this.userLevelAuthorization = JcrVfsRepositoryConfiguration.getBoolean(configuration, JcrVfsRepositoryConfiguration.USER_LEVEL_AUTHORIZATION, false);
            
      if (configuration.getAttributes().containsKey(
            JcrVfsRepositoryConfiguration.IS_DEFAULT_REPOSITORY))
      {
         // lookup default repository in parameters
         Object contentRepositoryRes = Parameters.instance().get(jndiName);
         if (contentRepositoryRes instanceof Repository)
         {
            this.repository = (Repository) contentRepositoryRes;
         }
         else
         {
            throw new RuntimeException("Default repository was not found in Parameters");
         }
      }
      else if (jndiName != null)
      {
         // lookup repository via jndi
         try
         {
            this.repository = (Repository) new InitialContext().lookup(jndiName);
         }
         catch (NamingException e)
         {
            throw new PublicException("Repository was not found for jndiName: "
                  + jndiName, e);
         }
      }
            
      initRepositoryInfo(configuration);
   }

   protected void initRepositoryInfo(IRepositoryConfiguration configuration)
   {
      this.repositoryInfo = new JcrVfsRepositoryInstanceInfo(repositoryId, repository, configuration);
   }

   @Override
   public String getRepositoryId()
   {
      return repositoryId;
   }
   
   @Override
   public String getProviderId()
   {
      return providerId;
   }
   
   public String getPartitionId()
   {
      return partitionId;
   }

   @Override
   public JcrVfsRepositoryInstanceInfo getRepositoryInstanceInfo()
   {
      return repositoryInfo;
   }
   
   @Override
   public IRepositoryService getService(User user)
   {
      synchronized (repository)
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         IRepositoryService service = rtEnv.getJcrService(this);

         if (service == null)
         {
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

            service = new JcrVfsRepositoryService(session, repository);
            rtEnv.registerJcrService(this, service);
         }
         return service;
      }
   }
   
   
   @Override
   public void close(IRepositoryService service)
   {
      if (service != null)
      {
         synchronized (repository)
         {
            closeJcrSessions(((JcrVfsRepositoryService) service).getSessions());
         }
      }
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

      return JcrSecurityUtils.getCredentialsIncludingParticipantHierarchy(user, jcrPassword);
   }

   public void closeJcrSessions(List<Session> jcrSessions)
   {
      if ( !jcrSessions.isEmpty())
      {
         for (Iterator i = jcrSessions.iterator(); i.hasNext();)
         {
            final javax.jcr.Session session = (javax.jcr.Session) i.next();
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
      }
   }

}
