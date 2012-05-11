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

import java.util.Date;

/**
 *
 */
public interface Attribute
{
   /**
    *
    */
   public String getName();

   /**
    *
    */
   public Object getValue();

   /**
    *
    */
   public void setValue(Object object);

   /**
    *
    */
   public String getStringifiedValue();

   /**
    * @return the date of last modification, or null if the date is unknown.
    */
   public Date getLastModificationTime();
}