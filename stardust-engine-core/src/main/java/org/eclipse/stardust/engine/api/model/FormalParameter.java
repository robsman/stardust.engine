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
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.Nameable;


public interface FormalParameter extends Identifiable, Nameable, Serializable
{
   /**
    * Gets the ID of this model element.
    * <p>Model elements such as process definitions or roles are identified by their ID.
    * The IDs of model elements are Strings being unique
    * inside the containing scope of the model element and the model version.
    * The meaning of containing scope can vary for different model element types, but
    * is usually given by the corresponding factory method.</p>
    *
    * @return the ID of the model element.
    */
   String getId();
   
   /**
    * Gets the name of this model element.
    * <p>Model elements have names which can be used to identify them in visual user interfaces.</p>
    *
    * @return the name of the model element.
    */
   String getName();

   /**
    * Gets the parameter direction.
    *
    * @return the parameter direction.
    */
   Direction getDirection();

   /**
    * Retrieves the type of the parameter as defined in the model.
    * 
    * @return a string representing the type of the parameter
    */
   String getTypeId();

   /**
    * Gets all the attributes defined for this model element.
    *
    * @return a Map with name-value pairs containing the attributes defined at modelling time.
    */
   Map getAllAttributes();

   /**
    * Gets a specified attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getAttribute(String name);
}
