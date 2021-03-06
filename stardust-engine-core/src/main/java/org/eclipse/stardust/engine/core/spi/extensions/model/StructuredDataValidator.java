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
package org.eclipse.stardust.engine.core.spi.extensions.model;

import java.util.List;

import org.eclipse.stardust.engine.api.model.IData;

/**
 * Provides Structured Data validation.
 */
public interface StructuredDataValidator
{
   /**
    * Performs a Structured Data validation. 
    * @param IData object 
    *
    * @return The list of found {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    */
   List validate(IData data);
}