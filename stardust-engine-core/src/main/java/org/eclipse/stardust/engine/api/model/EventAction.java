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

import java.util.Map;

/**
 * A client side view of a workflow event action. Each modeled event action contains a
 * specific set of attributes, depending on the type of the event action.
 */
public interface EventAction extends ModelElement
{
   /**
    * Gets all the attributes of the event action type.
    *
    * @return an unmodifiable Map containing the type attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets a specific type attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getTypeAttribute(String name);
}
