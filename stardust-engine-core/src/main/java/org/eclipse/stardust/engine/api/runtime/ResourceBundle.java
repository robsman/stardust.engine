/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * This serializable object contains resources in form of a map and its related locale.
 * 
 * @author Roland.Stamm
 *
 */
public interface ResourceBundle extends Serializable
{

   /**
    * The locale of the ResourceBundle. <p>
    * Please note that the locale might be different from the requested one.
    * 
    * @return The locale for the retrieved ResourceBundle
    */
   public Locale getLocale();

   /**
    * The resources of the resource bundle.<br>
    * 
    * 
    * @return The resources of the resource bundle.
    */
   public Map<String, Serializable> getResources();

}
