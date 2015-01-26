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

/**
 * The client view of a workflow application.
 * <p>Applications are software programs that interact with the Infinity process
 * engine handling the processing required to support a particular activity in
 * whole or in part. Multiple activities may use the same application but only one
 * application may be executed within an activity.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Application extends ModelElement
{
   /**
    * Gets the list of access points defined for this application.
    *
    * @return a List of AccessPoint objects
    */
   List getAllAccessPoints();

   /**
    * Gets a specified access point.
    *
    * @param id the ID of the access point.
    *
    * @return the AccessPoint requested.
    */
   AccessPoint getAccessPoint(String id);

   /**
    * Gets all attributes specified for this application type.
    *
    * @return a Map with all the type attributes.
    */
   Map getAllTypeAttributes();

   /**
    * Gets a specified attribute of the application type.
    *
    * @param name the name of the type attribute.
    *
    * @return the value of the attribute.
    */
   Object getTypeAttribute(String name);
}
