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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

// @todo (france, ub): split for contexts/applications?!
// @todo (france, ub): unify with DataValidator

/**
 * Provides static validation of {@link org.eclipse.stardust.engine.api.model.Application}
 * configurations. Will be used during modeling.
 *
 * @author ubirkemeyer
 * @version $Revision$
 *
 */
public interface ApplicationValidator
{
   /**
    * Performs static application validation. An implementation is expected to
    * inspect the given application and type attributes and access points and indicate any
    * problems with an appropriate {@link org.eclipse.stardust.engine.api.model.Inconsistency}.
    *
    * @param attributes Implementation specific context attributes.
    * @param typeAttributes Implementation specific application type attributes
    * @param accessPoints Implementation specific {@link AccessPoint}s.
    *
    * @return The list of found {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    *
    */
   List validate(Map attributes, Map typeAttributes, Iterator accessPoints);
}
