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
package org.eclipse.stardust.engine.cli;

import java.awt.Component;
import java.util.*;

import javax.swing.JOptionPane;

import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.*;

/**
 * @user rsauer
 * @version $Revision$
 */
public class SwingToolServiceFactory implements ServiceFactory
{
   private final Component parent;

   private Map properties = Collections.EMPTY_MAP;

   private Set factories;
   private Map serviceFactoryMapping;

   private ServiceFactory currentFactory;

   public SwingToolServiceFactory(Component parent)
   {
      this.parent = parent;

      this.factories = new HashSet();
      this.serviceFactoryMapping = new HashMap();
   }

   public <T extends Service> T getService(Class<T> type) throws ServiceNotAvailableException, LoginFailedException
   {
      T service = null;

      int loginCount = 0;
      do
      {
         try
         {
            if (null == currentFactory)
            {
               currentFactory = ServiceFactoryLocator.get(CredentialProvider.SWING_LOGIN,
                     properties);
               factories.add(currentFactory);
            }

            service = currentFactory.getService(type);
            serviceFactoryMapping.put(service, currentFactory);
            return service;
         }
         catch (LoginFailedException e)
         {
            if (e.getReason() == LoginFailedException.LOGIN_CANCELLED)
            {
               throw e;
            }
            else
            {
               JOptionPane.showMessageDialog(parent, e.getMessage(),
                     "Error", JOptionPane.ERROR_MESSAGE);

               currentFactory = null;
               ++loginCount;
            }
         }
      }
      while (3 > loginCount);

      if (null == service)
      {
         JOptionPane.showMessageDialog(parent, "Maximum number of login trials exceeded.\nTerminating application.",
               "Error", JOptionPane.ERROR_MESSAGE);

         System.exit(-1);
      }

      return service;
   }

   public WorkflowService getWorkflowService() throws ServiceNotAvailableException, LoginFailedException
   {
      return (WorkflowService) getService(WorkflowService.class);
   }

   public UserService getUserService() throws ServiceNotAvailableException, LoginFailedException
   {
      return (UserService) getService(UserService.class);
   }

   public AdministrationService getAdministrationService() throws ServiceNotAvailableException
   {
      return (AdministrationService) getService(AdministrationService.class);
   }

   public QueryService getQueryService() throws ServiceNotAvailableException, LoginFailedException
   {
      return (QueryService) getService(QueryService.class);
   }

   public DocumentManagementService getDocumentManagementService() throws ServiceNotAvailableException, LoginFailedException
   {
      return (DocumentManagementService) getService(DocumentManagementService.class);
   }

   public void release(Service service)
   {
      ServiceFactory factory = (ServiceFactory) serviceFactoryMapping.get(service);
      if (null != factory)
      {
         factory.release(service);
      }
      serviceFactoryMapping.remove(service);
   }

   public void close()
   {
      currentFactory = null;

      for (Iterator i = factories.iterator(); i.hasNext();)
      {
         ServiceFactory factory = (ServiceFactory) i.next();
         factory.close();
         i.remove();
      }
   }

   public void setCredentials(Map credentials)
   {
      if (null != currentFactory)
      {
         currentFactory.setCredentials(credentials);
      }
   }

   public void setProperties(Map properties)
   {
      this.properties = ((null == properties) || properties.isEmpty())
            ? Collections.EMPTY_MAP
            : new HashMap(properties);

      if (null != currentFactory)
      {
         currentFactory.setProperties(this.properties);
      }
   }

   public String getSessionId()
   {
      return null;
   }
}
