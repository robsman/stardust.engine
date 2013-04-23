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
package org.eclipse.stardust.engine.extensions.ejb;

import java.util.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.app.SessionBean20Validator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SessionBeanValidator implements ApplicationValidator
{
   public static final Logger trace = LogManager.getLogger(SessionBeanValidator.class);

   private static final String SESSION_BEAN_3_0_APPLICATION_VALIDATOR =
      "org.eclipse.stardust.engine.extensions.ejb.ejb3.app.SessionBean30Validator";

   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ApplicationValidator delegate = null;
      
      String style = (String) attributes.get(SessionBeanConstants.VERSION_ATT);
      if (style == null)
      {
         // old style app
         style = SessionBeanConstants.VERSION_2_X;
      }
      if (SessionBeanConstants.VERSION_3_X.equals(style))
      {
         try
         {
            Class clz = Reflect.getClassFromClassName(SESSION_BEAN_3_0_APPLICATION_VALIDATOR);
            delegate = (ApplicationValidator) clz.newInstance();
         }
         catch (Throwable t)
         {
            trace.debug("Failed to create SessionBean 3.0 validator.", t);
         }
      }
      if (delegate == null)
      {
         delegate = new SessionBean20Validator();
      }
      trace.debug("Created delegate: " + delegate);
      
      return delegate.validate(attributes, typeAttributes, accessPoints);
   }
}
