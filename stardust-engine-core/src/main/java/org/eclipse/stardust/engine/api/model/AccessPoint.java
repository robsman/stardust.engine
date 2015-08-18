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

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.Direction;


/**
 * An AccessPoint is a modelling element where an Application provides access to it's
 * data. It is used as the endpoint for a data mapping. An AccessPoint can expose a java
 * type or a custom defined type to be the end point of a data mapping. Every application
 * keeps a map of all access points identified by the ID attribute.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AccessPoint extends Serializable
{
   /**
    * Gets the direction of the access point which can be IN, OUT or INOUT.
    *
    * @return the direction of the access point.
    */
   Direction getDirection();

   /**
    * Gets the fully qualified name of the
    * {@link org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator} used to evaluate access
    * paths applyied to this access point.
    *
    * @return the fully qualified class name of the access path evaluator.
    */
   String getAccessPathEvaluatorClass();

   /**
    * Gets the ID of this access point.
    *
    * @return the ID of the access point.
    */
   String getId();

   /**
    * Gets the human readable name of the access point.
    *
    * @return the name of the access point.
    */
   String getName();

   /**
    * Gets all the attributes defined for this access point.
    *
    * @return the Map containing all the attributes.
    */
   Map getAllAttributes();

   /**
    * Gets the specified attribute of the access point.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getAttribute(String name);
}
