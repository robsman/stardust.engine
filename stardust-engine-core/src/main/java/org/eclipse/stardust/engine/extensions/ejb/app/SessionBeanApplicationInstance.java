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
package org.eclipse.stardust.engine.extensions.ejb.app;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.ejb.SessionBeanConstants;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.app.SessionBean20ApplicationInstance;


/**
 * ApplicationInstance implementation for the SessionBeanApplicationInstance
 * 
 * @author jmahmood, ubirkemeyer
 * @version $Revision: 52518 $
 */
public class SessionBeanApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(SessionBeanApplicationInstance.class);

   private static final String SESSION_BEAN_3_0_APPLICATION_INSTANCE =
      "org.eclipse.stardust.engine.extensions.ejb.ejb3.app.SessionBean30ApplicationInstance";
   
   private SynchronousApplicationInstance delegate;

   public void bootstrap(ActivityInstance activityInstance)
   {
      createDelegate(activityInstance);
      delegate.bootstrap(activityInstance);
   }

   private void createDelegate(ActivityInstance activityInstance)
   {
      Application application = activityInstance.getActivity().getApplication();
      String style = (String) application.getAttribute(SessionBeanConstants.VERSION_ATT);
      if (style == null)
      {
         // old style app
         style = SessionBeanConstants.VERSION_2_X;
      }
      if (SessionBeanConstants.VERSION_3_X.equals(style))
      {
         try
         {
            Class clz = Class.forName(SESSION_BEAN_3_0_APPLICATION_INSTANCE);
            delegate = (SynchronousApplicationInstance) clz.newInstance();
         }
         catch (Throwable t)
         {
            trace.debug("Failed to create SessionBean 3.0 application instance.", t);
         }
      }
      if (delegate == null)
      {
         delegate = new SessionBean20ApplicationInstance();
      }
      trace.debug("Created delegate: " + delegate);
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
