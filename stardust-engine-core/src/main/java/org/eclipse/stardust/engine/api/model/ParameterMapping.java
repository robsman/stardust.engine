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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.engine.api.model.ModelElement;

/**
 * The <code>ParameterMapping</code> class represents a mapping between a trigger access
 * point and a workflow data.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ParameterMapping extends ModelElement
{
   /**
    * Gets the ID of the mapped workflow data.
    *
    * @return the ID of the workflow data.
    */
   String getDataId();

   /**
    * Gets the trigger parameter used in this mapping.
    *
    * @return the mapping access point to the trigger.
    */
   AccessPoint getParameter();

   /**
    * Gets the access path that will be applied to the parameter.
    *
    * @return the access path that will be applied.
    */
   String getParameterPath();
}
