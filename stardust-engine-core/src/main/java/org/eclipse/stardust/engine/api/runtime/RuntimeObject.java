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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * The <code>RuntimeObject</code> class is the base class for all snapshots of runtime objects.
 * <p>Runtime Objects are workflow relevant objects that are created during workflow execution
 * or administration. Runtime objects have a persistent representation in the audit
 * trail database backed up by a dedicated database table. e.g. An
 * <code>ActivityInstance</code> object represents a row in the <code>activity_instance</code>
 * table of the audit trail database.<p/>
 * <p>Snapshots of CARNOT runtime objects are exposed to a client as
 * readonly detail objects which contain a copy of the state of the corresponding server
 * object and maybe additional useful information known to the runtime.<p/>
 * 
 * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance
 * @see org.eclipse.stardust.engine.api.runtime.User
 * @see org.eclipse.stardust.engine.api.runtime.ProcessInstance
 * @see org.eclipse.stardust.engine.api.runtime.LogEntry
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface RuntimeObject extends Serializable
{
   /**
    * Gets the OID of this runtime object.
    * <p>Runtime objects such as process instances, activity instances or
    * users are identified by their object ID (OID). The OIDs of runtime
    * objects are 64-bit integers being unique inside the scope of the runtime
    * object. OID can be used in many places in the API to select an object of
    * a specific type.<p/>
    * <p>Example:
    * <pre>
    * ActivityInstance activityInstance = ...
    * User user = ...
    * WorkflowService wfService = ...
    * wfService.delegateToUser(activityInstance.getOID(), user.getOID());
    * </pre></p>
    *
    * @return The OID of this runtime object
    */
   long getOID();

   /**
    * Returns the OID of the model the runtime object's definition belongs to.
    *
    * @return The OID of this runtime object's definition's model.
    *
    * @see #getModelElementID()
    */
   int getModelOID();

   /**
    * Returns the OID of the runtime object's definition.
    *
    * @return The OID of this runtime object's definition.
    *
    * @see #getModelOID()
    * @see #getModelElementID()
    */
   int getModelElementOID();

   /**
    * Returns the ID of the runtime object's definition.
    *
    * @return The ID of this runtime object's definition.
    *
    * @see #getModelOID()
    * @see #getModelElementOID()
    */
   String getModelElementID();
}
