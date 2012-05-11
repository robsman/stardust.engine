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

/**
 *	Marks classes to implement getter and setter for named values
 */
public interface NamedValues
{
   /**
    * Retrieves the value named <code>name</code>.
    */
   public Object get(String name);

   /**
    * Sets the value named <code>name</code>.
    */
   public void set(String name, Object value);
}