/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2010 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.ws;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.web.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.command.impl.RetrieveModelDetailsCommand;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class WebServiceEnv
{
   private static final ThreadLocal<WebServiceEnvBuilder> CURRENT_WEB_ENV_BUILDER = new ThreadLocal<WebServiceEnvBuilder>();
   private static final ThreadLocal<WebServiceEnv> CURRENT_WEB_ENV = new ThreadLocal<WebServiceEnv>();

   private static final ServiceFactoryCache sfCache;
   private static final ModelCache modelCache;

   static
   {
      if (Parameters.instance().getBoolean("Carnot.WebService.UseSessionCache", true))
      {
         sfCache = new ServiceFactoryCache();
      }
      else
      {
         sfCache = null;
      }

      if (Parameters.instance().getBoolean("Carnot.WebService.UseModelCache", true))
      {
         modelCache = new ModelCache();
      }
      else
      {
         modelCache = null;
      }
   }

   private final ServiceFactory serviceFactory;

   public static WebServiceEnv currentWebServiceEnvironment()
   {
      WebServiceEnv webServiceEnv = CURRENT_WEB_ENV.get();
      if (webServiceEnv == null)
      {
         final WebServiceEnvBuilder builder = CURRENT_WEB_ENV_BUILDER.get();
         if (builder == null)
         {
            throw new WebServiceEnvException(
                  BpmRuntimeError.IPPWS_ENV_CREATION_FAILED_ALL_NULL.raise());
         }

         webServiceEnv = builder.build();
         CURRENT_WEB_ENV.set(webServiceEnv);
         CURRENT_WEB_ENV_BUILDER.remove();
      }
      return webServiceEnv;
   }

   public static void setCurrentCredentials(final String username, final String password)
   {
      WebServiceEnvBuilder builder = CURRENT_WEB_ENV_BUILDER.get();
      if (builder == null)
      {
         builder = new WebServiceEnvBuilder();
      }
      builder.username(username);
      builder.password(password);
      CURRENT_WEB_ENV_BUILDER.set(builder);
   }

   public static void setCurrentSessionProperties(
         final Map<String, ? extends Serializable> sessionProperties)
   {
      WebServiceEnvBuilder builder = CURRENT_WEB_ENV_BUILDER.get();
      if (builder == null)
      {
         builder = new WebServiceEnvBuilder();
      }
      builder.sessionProperties(sessionProperties);
      CURRENT_WEB_ENV_BUILDER.set(builder);
   }

   public static void removeCurrent()
   {
      final WebServiceEnv current = CURRENT_WEB_ENV.get();
      if (current != null)
      {
         CURRENT_WEB_ENV.remove();

         if (null != current.serviceFactory)
         {
            if (null == sfCache)
            {
               current.getServiceFactory().close();
            }
            else
            {
               sfCache.release(current.serviceFactory);
            }
         }
      }
      else if (CURRENT_WEB_ENV_BUILDER.get() != null)
      {
         CURRENT_WEB_ENV_BUILDER.remove();
      }
   }

   protected WebServiceEnv(String user, String password,
         Map<String, ? extends Serializable> properties)
   {
      if (null == sfCache)
      {
         this.serviceFactory = ServiceFactoryLocator.get(user, password, properties);
      }
      else
      {
         this.serviceFactory = sfCache.getServiceFactory(user, password, properties);
      }
   }

   public Model getActiveModel()
   {
      return getModel(PredefinedConstants.ACTIVE_MODEL);
   }

   public Model getModel(int modelOid)
   {
      Model model = null;

      if (null != modelCache)
      {
         model = modelCache.getModel(modelOid);
      }

      if (null == model)
      {
         DeployedModel deployedModel = (DeployedModel) serviceFactory
               .getWorkflowService().execute(
                     RetrieveModelDetailsCommand.retrieveModelByOid(modelOid));

         if ((null != deployedModel) && (null != modelCache))
         {
            modelCache.putModel(deployedModel);
         }

         model = modelCache.getModel(modelOid);
      }

      return model;
   }

   public void clearModelCache()
   {
      if (null != modelCache)
      {
         modelCache.reset();
      }
   }

   public ServiceFactory getServiceFactory()
   {
      return serviceFactory;
   }

   /**
    * @author nicolas.werlein
    * @version $Revision: $
    */
   private static final class WebServiceEnvBuilder
   {
      private String username;

      private String password;

      private Map<String, ? extends Serializable> sessionProperties;

      public void username(final String username)
      {
         this.username = username;
      }

      public void password(final String password)
      {
         this.password = password;
      }

      public void sessionProperties(
            final Map<String, ? extends Serializable> sessionProperties)
      {
         this.sessionProperties = sessionProperties;
      }

      public WebServiceEnv build()
      {
         if (username == null)
         {
            throw new WebServiceEnvException(
                  BpmRuntimeError.IPPWS_ENV_CREATION_FAILED_USER_NULL.raise());
         }
         if (sessionProperties == null)
         {
            throw new NullPointerException(
                  BpmRuntimeError.IPPWS_ENV_CREATION_FAILED_SESSION_PROPS_NULL.raise()
                        .toString());
         }

         return new WebServiceEnv(username, password, sessionProperties);
      }
   }

   private static final class WebServiceEnvException extends PublicException
   {
      private static final long serialVersionUID = 4369362171414845059L;

      public WebServiceEnvException(final ErrorCase errorCase)
      {
         super(errorCase);
      }
   }
}
