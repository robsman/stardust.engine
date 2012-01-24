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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;


/**
 * The <code>ApplicationContext</code> represents the execution context of an activity.
 * <p>An activity may have multiple execution context, depending on the implementation
 * type.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
// todo: (fh) write description.
public interface ApplicationContext extends ModelElement
{
   /**
    * Gets all data mappings defined for this application context.
    *
    * @return the List of all DataMapping objects.
    */
   List getAllDataMappings();

   /**
    * Gets all IN data mappings defined for this application context.
    *
    * @return a List containing all IN data mappings
    */
   List getAllInDataMappings();

   /**
    * Gets all OUT data mappings defined for this application context.
    *
    * @return a List containing all OUT data mappings
    */
   List getAllOutDataMappings();

   /**
    * Gets a specified data mapping. A data mapping has a unique id in the context of
    * the application context and direction.
    *
    * @param direction the direction of the data mapping.
    * @param id the id of the data mapping.
    *
    * @return the requested data mapping.
    */
   DataMapping getDataMapping(Direction direction, String id);

   /**
    * Gets all access points defined for this application context.
    *
    * @return a List with all access points.
    *
    * @see AccessPoint
    */
   List getAllAccessPoints();

   /**
    * Gets the specified access point.
    *
    * @param id the id of the access point to retrieve.
    *
    * @return the requested access point.
    */
   AccessPoint getAccessPoint(String id);

   /**
    * Gets all the attributes defined for this application context type.
    *
    * @return a Map with all the type attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets the specified type attribute.
    *
    * @param name the name of the type attribute.
    *
    * @return the value of the type attribute.
    */
   Object getTypeAttribute(String name);
}
