/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.setup;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.rules.ExternalResource;

/**
 * <p>
 * A <code>ClientServiceFactory</code> is a Stardust Service Factory (see 
 * {@linkplain org.eclipse.stardust.engine.api.runtime.ServiceFactory}) that will be
 * created and initialized before every test execution (even before test setup, i.e.
 * before the methods annotated with <code>@Before</code> are executed) and released
 * afterwards (after the methods annotated with <code>@After</code> have been called)
 * if a field of this class is annotated with <code>@Rule</code> in the test class.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TestServiceFactory extends ExternalResource implements ServiceFactory
{
   private static final Log LOG = LogFactory.getLog(TestServiceFactory.class);
   
   private final UsernamePasswordPair userPwdPair;
   
   private ServiceFactory sf;
   
   /**
    * <p>
    * Sets up a client service factory with the given username and password.
    * The actual creation of the service factory will be done in
    * {@link TestServiceFactory#before()}.
    * </p>
    * 
    * @param username the username password pair to use
    */
   public TestServiceFactory(final UsernamePasswordPair userPwdPair)
   {
      if (userPwdPair == null)
      {
         throw new NullPointerException("Username password pair must not be null.");
      }

      this.userPwdPair = userPwdPair;
   }
   
   /**
    * <p>
    * Creates a newly initialized service factory.
    * </p>
    */
   @Override
   protected void before()
   {
      LOG.debug("Retrieving service factory for '" + userPwdPair.username() + ":" + userPwdPair.password() + "'.");      
      sf = ServiceFactoryLocator.get(userPwdPair.username(), userPwdPair.password());
   }
   
   /**
    * <p>
    * Releases the current service factory.
    * </p>
    */
   @Override
   protected void after()
   {
      LOG.debug("Releasing service factory.");
      close();
      sf = null;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#close()
    */
   @Override
   public void close()
   {
      ensureServiceFactoryIsInitialized();
      sf.close();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getAdministrationService()
    */
   @Override
   public AdministrationService getAdministrationService() throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getAdministrationService();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getDocumentManagementService()
    */
   @Override
   public DocumentManagementService getDocumentManagementService() throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getDocumentManagementService();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getQueryService()
    */
   @Override
   public QueryService getQueryService() throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getQueryService();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getService(java.lang.Class)
    */
   @Override
   public Object getService(@SuppressWarnings("rawtypes") final Class type) throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getService(type);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getSessionId()
    */
   @Override
   public String getSessionId()
   {
      ensureServiceFactoryIsInitialized();
      return sf.getSessionId();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getUserService()
    */
   @Override
   public UserService getUserService() throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getUserService();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#getWorkflowService()
    */
   @Override
   public WorkflowService getWorkflowService() throws ServiceNotAvailableException, LoginFailedException
   {
      ensureServiceFactoryIsInitialized();
      return sf.getWorkflowService();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#release(org.eclipse.stardust.engine.api.runtime.Service)
    */
   @Override
   public void release(final Service service)
   {
      ensureServiceFactoryIsInitialized();
      sf.release(service);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#setCredentials(java.util.Map)
    */
   @Override
   public void setCredentials(@SuppressWarnings("rawtypes") final Map credentials)
   {
      ensureServiceFactoryIsInitialized();
      sf.setCredentials(credentials);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ServiceFactory#setProperties(java.util.Map)
    */
   @Override
   public void setProperties(@SuppressWarnings("rawtypes") final Map properties)
   {
      ensureServiceFactoryIsInitialized();
      sf.setProperties(properties);
   }
   
   private void ensureServiceFactoryIsInitialized()
   {
      if (sf == null)
      {
         throw new IllegalStateException("Service factory is not properly initialized.");
      }
   }
}
