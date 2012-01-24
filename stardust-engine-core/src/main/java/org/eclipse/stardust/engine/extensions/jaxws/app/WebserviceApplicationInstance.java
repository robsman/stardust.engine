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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;


/**
 * @author fherinean
 * @version $Revision$
 */
public class WebserviceApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(WebserviceApplicationInstance.class);

   private static final String JAXWS_WEBSERVICE_APPLICATION_INSTANCE =
      "com.infinity.bpm.rt.integration.webservices.JaxwsWebserviceApplicationInstance";
   
   private SynchronousApplicationInstance delegate;

   public void bootstrap(ActivityInstance activityInstance)
   {
      createDelegate(activityInstance);
      delegate.bootstrap(activityInstance);
   }

   private void createDelegate(ActivityInstance activityInstance)
   {
      Application application = activityInstance.getActivity().getApplication();
      String style = (String) application.getAttribute(WSConstants.RUNTIME_ATT);
      if (style == null)
      {
         style = WSConstants.JAXWS_RUNTIME;
      }
      if (WSConstants.JAXWS_RUNTIME.equals(style))
      {
         try
         {
            Class clz = Class.forName(JAXWS_WEBSERVICE_APPLICATION_INSTANCE);
            delegate = (SynchronousApplicationInstance) clz.newInstance();
         }
         catch (Throwable t)
         {
            trace.info("Failed to create JAX-WS application instance.", t);
         }
      }
      trace.info("Created delegate: " + delegate);
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      return delegate.invoke(outDataTypes);
   }

   public void setInAccessPointValue(String name, Object value)
   {
      delegate.setInAccessPointValue(name, value);
   }

   public Object getOutAccessPointValue(String name)
   {
      return delegate.getOutAccessPointValue(name);
   }

   public void cleanup()
   {
      delegate.cleanup();
   }
}
