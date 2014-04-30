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

import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetailsLevel;

/**
 * The client view of a workflow process.
 * <p>A process definition normally comprises a number of discrete activity steps,
 * with associated computer and/or human operations and rules governing the progression
 * of the process through the various activity steps.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ProcessDefinition extends ModelElement, EventAware
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

   /**
    * Gets all data paths defined for this process definition.
    *
    * @return a List of <code>{@link DataPath}</code> objects.
    */
   List getAllDataPaths();

   /**
    * Gets the specified data path.
    *
    * @param id the ID of the data path.
    *
    * @return the requested data path.
    */
   DataPath getDataPath(String id);

   /**
    * Retrieves the level of detail for process definition.
    *
    * @return the process instance details level.
    */
   ProcessDefinitionDetailsLevel getDetailsLevel();

   /**
    * Gets all activities defined for this process definition.
    *
    * @return a List of <code>{@link Activity}</code> objects.
    */
   List getAllActivities();

   /**
    * Gets the specified activity.
    *
    * @param id the ID of the activity.
    *
    * @return the requested activity.
    */
   Activity getActivity(String id);

   // @todo (france, ub): remove?
   /**
    * Gets all triggers defined for this process definition.
    *
    * @return a List of <code>{@link Trigger}</code> objects.
    */
   List getAllTriggers();

   /**
    * Gets the process interface implemented by this process definition.
    *
    * @return the ProcessInterface or null if this process do not implement an interface.
    */
   ProcessInterface getImplementedProcessInterface();

   /**
    * Gets the process interface implemented by this process definition.
    *
    * @return the ProcessInterface or null if this process do not implement an interface.
    */
   ProcessInterface getDeclaredProcessInterface();
}
