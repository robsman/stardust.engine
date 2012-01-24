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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent;


/**
 * @author born
 * @version $Revision: $
 */
public class PropertyUtils
{
   /**
    * Removes all properties for given attributed object whose property name starts with 
    * specified prefix.
    * 
    * @param persistent
    * @param prefix
    */
   public static void removePropertyWithPrefix(AttributedIdentifiablePersistent persistent,
         String prefix)
   {
      Map/*<String, AbstractProperty>*/ properties = persistent.getAllProperties();
      for (Iterator/*<String>*/ i = properties.keySet().iterator(); i.hasNext();)
      {
         String key = (String) i.next();
         if (key.startsWith(prefix))
         {
            persistent.removeProperty(key);
         }
      }
   }

   private PropertyUtils()
   {
   }
}
