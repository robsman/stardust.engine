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
package org.eclipse.stardust.common;

import java.util.Map;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AttributeHolder
{
   void markModified();

   Map<String, Object> getAllAttributes();

   <V extends Object> void setAllAttributes(Map<String, V> attributes);

   // @todo (france, ub): let this return Attribute and attach the interface
   // to IUser and IActivityInstance
   Object getAttribute(String name);

   void setAttribute(String name, Object value);

   void removeAllAttributes();

   void removeAttribute(String name);

   boolean getBooleanAttribute(String name);

   long getLongAttribute(String name);

   int getIntegerAttribute(String name);

   float getFloatAttribute(String name);

   String getStringAttribute(String name);
}
