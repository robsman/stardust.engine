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
 * A client side view of a process trigger.
 * <p>A trigger is responsible for starting the process instance corresponding to the
 * process definition containing the trigger.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Trigger extends ModelElement
{
   /**
    * Gets the runtime OID of the model element.
    * <p>
    * Contrary to the element OID, runtime element OIDs are guaranteed to be stable over
    * model versions for model elements of same type and identical fully qualified IDs.
    * </p>
    * 
    * <p>
    * The fully qualified ID of a model element consists of the concatenation of the fully
    * qualified element ID of its parent element, if existent, and the element ID.
    * </p>
    * 
    * @return the runtime model element OID
    * 
    * @see ModelElement#getElementOID()
    */
   long getRuntimeElementOID();

   // @todo (france, ub): we don't want to navigate bottom up?!
   /**
    * Gets the process definition containing this trigger.
    *
    * @return the triggered process definition
    */
   ProcessDefinition getProcessDefinition();

   /**
    * Gets the type of trigger.
    *
    * @return the ID of the trigger type.
    */
   String getType();

   /**
    * Gets whether the trigger is synchronous or asynchronous.
    *
    * @return true if the triggered process instance is started synchronous.
    */
   boolean isSynchronous();

   /**
    * Gets all the attributes defined for the trigger.
    *
    * @return a Map with all the trigger's attributes.
    */
   Map getAllAttributes();

   /**
    * Gets all access points for the trigger.
    *
    * @return a List of {@link AccessPoint} objects.
    */
   List<AccessPoint> getAllAccessPoints();

   /**
    * Gets an AccessPointBean with the specified name.
    *
    * @param id the ID of the access point.
    *
    * @return the corresponding AccessPoint.
    */
   AccessPoint getAccessPoint(String id);

   /**
    * Gets all parameter mappings for the trigger.
    *
    * @return a List of {@link ParameterMapping} objects.
    */
   List<ParameterMapping> getAllParameterMappings();
}
