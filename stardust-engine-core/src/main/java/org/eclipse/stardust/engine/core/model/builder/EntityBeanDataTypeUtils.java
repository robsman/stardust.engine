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
package org.eclipse.stardust.engine.core.model.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EntityBeanDataTypeUtils
{
   public static Map initEntityBeanAttributes(String remoteClassName,
         String homeClassName, String pkClassName, String jndiName, boolean localBinding)
   {
      Map attributes = new HashMap();

      attributes.put(PredefinedConstants.REMOTE_INTERFACE_ATT, remoteClassName);
      attributes.put(PredefinedConstants.JNDI_PATH_ATT, jndiName);
      attributes.put(PredefinedConstants.IS_LOCAL_ATT, localBinding
            ? Boolean.TRUE : Boolean.FALSE);
      attributes.put(PredefinedConstants.HOME_INTERFACE_ATT, homeClassName);
      attributes.put(PredefinedConstants.PRIMARY_KEY_ATT, pkClassName);

      return attributes;
   }

   private EntityBeanDataTypeUtils()
   {
      // utility class
   }
}
