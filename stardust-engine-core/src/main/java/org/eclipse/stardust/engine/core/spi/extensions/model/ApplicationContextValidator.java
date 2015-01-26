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

/**
 * Provides static validation of {@link org.eclipse.stardust.engine.api.model.ApplicationContext}
 * configurations. Will be used during modeling.
 *
 * @author ubirkemeyer
 * @version $Revision$
 *
 * @see org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationContextPropertiesPanel
 */
public interface ApplicationContextValidator
{
   /**
    * Performs static application context validation. An implementation is expected to
    * inspect the given context attributes and access points and indicate any problems
    * with an appropriate {@link org.eclipse.stardust.engine.api.model.Inconsistency}.
    *
    * @param attributes Implementation specific context attributes.
    * @param accessPoints Implemenatation specific {@link AccessPoint}s.
    *
    * @return The list of found {@link org.eclipse.stardust.engine.api.model.Inconsistency} instances.
    *
    * @see org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationContextPropertiesPanel#getAttributes()
    */
   List validate(Map attributes, Iterator accessPoints);
}
