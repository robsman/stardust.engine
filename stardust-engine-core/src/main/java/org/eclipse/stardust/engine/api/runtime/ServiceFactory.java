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
package org.eclipse.stardust.engine.api.runtime;

import java.util.Map;

import org.eclipse.stardust.common.security.authentication.LoginFailedException;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;


/**
 * The <code>ServiceFactory</code> is the central point to retrieve CARNOT services. Based
 * on the CARNOT configuration it shields several aspects from the programmer:
 * <ul>
 *   <li>The authentication mechanism</li>
 *   <li>The 'transport protocol' (POJO, EJB)</li>
 *   <li>The pooling strategy.</li>
 * </ul>
 * <p />
 * By default a <code>ServiceFactory</code> is configured to use per thread caching of
 * services.
 * <p />
 * A typical usage pattern of using a ServiceFactory is
 * <pre>
 * ServiceFactory sf = ServiceFactoryLocator.get("user", "password");
 * WorkflowService ws = sf.getWorkflowService();
 * // use service methods
 *
 * QueryService qs = sf.getQueryService();
 * // use service methods
 *
 * // release all services and related resources
 * sf.close();
 * </pre>
 *
 * @see ServiceFactoryLocator
 * @see WorkflowService
 * @see AdministrationService
 * @see UserService
 * @see QueryService
 * @see DocumentManagementService
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ServiceFactory
{
   /**
    * Retrieves a service instance. Is provided for extensibility beyond the core service
    * set.
    *
    * @param type The class name of the service to be retrieved.
    *
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   Object getService(Class type)
         throws ServiceNotAvailableException, LoginFailedException;

   /**
    * Returns a WorkflowService.
    *
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   WorkflowService getWorkflowService()
         throws ServiceNotAvailableException, LoginFailedException;

   /**
    * Returns a UserService.
    *
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   UserService getUserService()
         throws ServiceNotAvailableException, LoginFailedException;

   /**
    * Returns an AdministrationService.
    *
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   AdministrationService getAdministrationService()
         throws ServiceNotAvailableException, LoginFailedException;

   /**
    * Returns a QueryService.
    *
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   QueryService getQueryService()
         throws ServiceNotAvailableException, LoginFailedException;

   /**
    * Returns a document management service.
    * 
    * @throws ServiceNotAvailableException In case the (possible remote) service could not
    *         be reached.
    * @throws LoginFailedException In case the authentication to the service fails.
    *
    * @return An instance of the requested service.
    */
   DocumentManagementService getDocumentManagementService()
         throws ServiceNotAvailableException, LoginFailedException;;

   /**
    * Provides explicit service resource management. May be used to release resources
    * associated with a service (like connection handles or locked instances) early.
    * <p />
    * Explicitly releasing a service may be help conserving resources but is not necessary
    * as the <code>ServiceFactory</code> provides automatic resource cleanup.
    *
    * @param service The service to be released.
    *
    * @see #close()
    */
   void release(Service service);

   /**
    * Releases all resources hold by the service factory and its single services. All
    * services retrieved from this ServiceFactory will be closed too.
    */
   void close();

   void setCredentials(Map credentials);

   void setProperties(Map properties);
   
   /**
    * Gets the user session id 
    * @return the user session id - may be null
    */
   String getSessionId();
}
