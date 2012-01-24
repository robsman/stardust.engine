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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SessionFactory implements SessionProperties
{
   private static final Logger trace = LogManager.getLogger(SessionFactory.class);
   
   private static final String KEY_DS_AUDIT_TRAIL_SESSION = DS_NAME_AUDIT_TRAIL
         + DS_SESSION_SUFFIX;
   
   private static final String JNDI_NAME_AUDIT_TRAIL_SESSION = "jdbc/"
         + DS_NAME_AUDIT_TRAIL + DS_DATA_SOURCE_SUFFIX;
   
   public static final String AUDIT_TRAIL = DS_NAME_AUDIT_TRAIL;
   public static final String PWH = DS_NAME_PWH;

   public static DataSource obtainDataSource(String name)
   {
      final Parameters params = Parameters.instance();

      DataSource result = (DataSource) params.get(
            "jdbc/" + name + DS_DATA_SOURCE_SUFFIX);
      if (null == result)
      {
         String driver = params.getString(name + DS_DRIVER_CLASS_SUFFIX);
         String url = params.getString(name + DS_URL_SUFFIX);
         String user = params.getString(name + DS_USER_SUFFIX);
         String password = params.getString(name + DS_PASSWORD_SUFFIX);

         result = new LocalDataSource(driver, url, user, password);
         params.set("jdbc/" + name + DS_DATA_SOURCE_SUFFIX, result);
      }
      return result;
   }

   public static Session createContainerSession(String name)
   {
      Session session = null;

      final String jndiName = DS_NAME_AUDIT_TRAIL.equals(name)
            ? JNDI_NAME_AUDIT_TRAIL_SESSION
            : ("jdbc/" + name + DS_DATA_SOURCE_SUFFIX);

      DataSource dataSource = (DataSource) Parameters.instance().get(jndiName);
      if (null != dataSource)
      {
         session = new Session(name);
         session.connect(dataSource);
      }

      return session;
   }

   public static Session createSession(String name)
   {
      Session  session = new Session(name);
      session.connect(obtainDataSource(name));
      return session;
   }

   public static Session createSession(String name, DataSource dataSource)
   {
      Session  session = new Session(name);
      session.connect(dataSource);

      return session;
   }

   /**
    * Creates a new session and binds it locally to this thread. Pushes a new thread local
    * property layer.
    * 
    * @deprecated Directly use {@link #createSession(String)} and bind yourself.
    */
   public static Session bindSession(String sessionName)
   {
      Map locals = new HashMap();

      Session session = SessionFactory.createSession(sessionName);
      locals.put(sessionName + SessionProperties.DS_SESSION_SUFFIX, session);

      ParametersFacade.pushLayer(locals);

      return session;
   }

   public static org.eclipse.stardust.engine.core.persistence.Session getSession(String name)
   {
      if (DS_NAME_AUDIT_TRAIL.equals(name))
      {
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         
         if ((null != rtEnv) && (null != rtEnv.getAuditTrailSession()))
         {
            return rtEnv.getAuditTrailSession();
         }
      }
      
      final Parameters parameters = Parameters.instance();
      if(trace.isDebugEnabled())
      {
         trace.debug("Parameters: " + parameters);
      }
      
      final String sessionKey = DS_NAME_AUDIT_TRAIL.equals(name)
            ? KEY_DS_AUDIT_TRAIL_SESSION
            : (name + DS_SESSION_SUFFIX);
      return (org.eclipse.stardust.engine.core.persistence.Session) parameters.get(sessionKey);
   }
   
   public static boolean isDebugSession()
   {
      return getSession(AUDIT_TRAIL) instanceof Session.NotJoinEnabled;
   }
}
