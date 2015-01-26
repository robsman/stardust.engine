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
package org.eclipse.stardust.engine.core.runtime.interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AuditTrailPropertiesInterceptor implements MethodInterceptor
{
   public static final Logger trace = LogManager.getLogger(AuditTrailPropertiesInterceptor.class);

   private static final String PROP_CACHE_ENTRY_NAME = "org.eclipse.stardust.engine.core.runtime.auditTrailProperties";

   protected final String sessionName;

   public AuditTrailPropertiesInterceptor(String sessionName)
   {
      this.sessionName = sessionName;
   }

   // double synchronization check
   protected Map getAuditTrailProperties(Parameters params)
   {
      Map auditTrailProps = (Map) params.get(PROP_CACHE_ENTRY_NAME);

      if (null == auditTrailProps)
      {
         auditTrailProps = bootstrapAuditTrailProperties(params, sessionName);
      }

      return auditTrailProps.isEmpty() ? null : auditTrailProps;
   }

   /**
    * Bootstrap audit trail properties in a thread safe way. The properties will be loaded
    * to a map, which is cached inside the JVM global parameter registry.
    */
   private synchronized static Map bootstrapAuditTrailProperties(Parameters parameters,
         String sessionName)
   {
      // ensure nobody has already added the properties during the synchronization cycle
      // before the method could be entered
      Map auditTrailProps = (Map) parameters.get(PROP_CACHE_ENTRY_NAME);
      if (null == auditTrailProps)
      {
         auditTrailProps = Collections.EMPTY_MAP;
         try
         {
            Session session = SessionFactory.getSession(sessionName);
            if (null != session)
            {
               ResultIterator i = PropertyPersistor.findAll(session, null);
               try
               {
                  auditTrailProps = new HashMap();
                  while (i.hasNext())
                  {
                     PropertyPersistor prop = (PropertyPersistor) i.next();
                     auditTrailProps.put(prop.getName(), prop.getValue());
                  }
               }
               finally
               {
                  i.close();
               }
            }
            else
            {
               trace.warn("Failed bootstrapping runtime properties from audit trail as no session '"
                     + sessionName + "' could be found.");
            }
         }
         catch (Exception e)
         {
            trace.warn("Failed bootstrapping runtime properties from audit trail.", e);
         }

         parameters.set(PROP_CACHE_ENTRY_NAME, auditTrailProps);
      }
      return auditTrailProps;
   }
}
