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
package org.eclipse.stardust.engine.core.runtime.command;

import java.io.Serializable;
import java.util.Map;

/**
 * Generic interface for objects that supports client-side configuration.
 * For details of the supported options please check the relevant Service API methods.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface Configurable extends Serializable
{
   /**
    * Retrieves the parameters associated with this object.
    * 
    * @return a {@link Map} of {name,value} pairs.
    */
   Map<String, Object> getOptions();
}