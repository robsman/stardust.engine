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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.utils.JavaApplicationTypeHelper;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.extensions.ejb.SessionBeanConstants;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.app.SessionBean20AccessPointProvider;


/**
 * @author ubirkemeyer
 * @version $Revision: 52518 $
 */
public class SessionBeanAccessPointProvider implements AccessPointProvider
{
   private static final Logger trace = LogManager
         .getLogger(SessionBeanAccessPointProvider.class);

   private static final String SESSION_BEAN_3_0_ACCESS_POINT_PROVIDER =
      "org.eclipse.stardust.engine.extensions.ejb.ejb3.app.SessionBean30AccessPointProvider";

   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      AccessPointProvider delegate = null;
      
      String style = (String) context.get(SessionBeanConstants.VERSION_ATT);
      if (style == null)
      {
         // old style app
         style = SessionBeanConstants.VERSION_2_X;
      }
      if (SessionBeanConstants.VERSION_3_X.equals(style))
      {
         try
         {
            Class clz = Reflect.getClassFromClassName(SESSION_BEAN_3_0_ACCESS_POINT_PROVIDER);
            delegate = (AccessPointProvider) clz.newInstance();
         }
         catch (Throwable t)
         {
            trace.debug("Failed to create SessionBean 3.0 access point provider.", t);
         }
      }
      if (delegate == null)
      {
         delegate = new SessionBean20AccessPointProvider();
      }
      trace.debug("Created delegate: " + delegate);
      
      return delegate.createIntrinsicAccessPoints(context, typeAttributes);
   }
}
