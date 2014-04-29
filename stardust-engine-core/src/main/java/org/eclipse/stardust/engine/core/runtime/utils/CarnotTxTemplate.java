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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.ejb.EjbProperties;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class CarnotTxTemplate
{
   public static final String SCOPE_CARNOT_TX_TEMPLATE = "CarnotTxTemplate";

   protected abstract Object executeInTx(Object[] args);

   public Object execute(Object[] args)
   {
      final Parameters params = Parameters.instance();

      final String containerType = (String) params.get(EjbProperties.CONTAINER_TYPE);
      final J2eeContainerType type = J2eeContainerType.EJB.getId().equalsIgnoreCase(
            containerType) //
            ? J2eeContainerType.EJB
            : J2eeContainerType.WEB.getId().equalsIgnoreCase(containerType)
                  ? J2eeContainerType.WEB
                  : J2eeContainerType.POJO;

      Map locals = CollectionUtils.newMap();
      locals.put(EjbProperties.CONTAINER_TYPE, type);

      boolean pushedJndiContext = false;
      try
      {
         BpmRuntimeEnvironment rtEnv = (BpmRuntimeEnvironment) ParametersFacade.pushLayer(
               params, PropertyLayerProviderInterceptor.BPM_RT_ENV_LAYER_FACTORY, locals);

         Session session;

         if ((null != type) && !J2eeContainerType.POJO.equals(type))
         {
            InitialContext ic = new InitialContext();
            Context environment = (Context) ic.lookup(EjbProperties.LOCAL_JNDI_ENV);
            ParametersFacade.pushContext(params, environment, SCOPE_CARNOT_TX_TEMPLATE);
            pushedJndiContext = true;

            session = SessionFactory.createContainerSession(SessionFactory.AUDIT_TRAIL);
         }
         else
         {
            session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
         }

         if (null == session)
         {
            throw new PublicException(
                  BpmRuntimeError.EJB_MISSING_DATA_SOURCE
                        .raise(SessionFactory.AUDIT_TRAIL));
         }

         rtEnv.setAuditTrailSession(session);
         rtEnv.setProperty(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX,
               session);

         final BpmRuntimeEnvironment previous = PropertyLayerProviderInterceptor.setCurrent(rtEnv);
         try
         {
            try
            {
               Object result = executeInTx(args);

               if (J2eeContainerType.POJO.equals(type))
               {
                  session.save();
               }
               else
               {
                  session.flush();
               }
               session.disconnect();

               return result;
            }
            catch (Exception e)
            {
               session.rollback();
               throw new RuntimeException(e);
            }
         }
         finally
         {
            PropertyLayerProviderInterceptor.setCurrent(previous);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         if (pushedJndiContext)
         {
            ParametersFacade.popContext(params);
         }
         ParametersFacade.popLayer();
      }

   }
}
