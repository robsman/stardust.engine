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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;


// @todo (france, ub): finally remove this interface and use attributeholder instead
/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AttributedIdentifiablePersistent extends IdentifiablePersistent
{
   void addPropertyValues(Map map);

   Map getAllPropertyValues();

   Map getAllProperties();

   Serializable getPropertyValue(String name);

   void setPropertyValue(String name, Serializable value);
   
   void setPropertyValue(String name, Serializable value, boolean force);   

   void removeProperty(String name);
   
   /**
    * Removes the property with given name if the current value is the same.
    *  
    * @param name
    * @param value
    */
   void removeProperty(String name, Serializable value);
   
   AbstractProperty createProperty(String name, Serializable value);
}