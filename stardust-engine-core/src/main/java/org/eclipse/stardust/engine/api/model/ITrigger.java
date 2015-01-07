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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;


public interface ITrigger extends IViewable, IdentifiableElement, AccessPointOwner, Typeable
{
   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the
    * Trigger.
    */
   void checkConsistency(List inconsistencies);

   boolean isSynchronous();

   /**
    * @deprecated Use of {@link #getParameterMappings()} allows for more efficient iteration.
    */
   Iterator getAllParameterMappings();

   ModelElementList getParameterMappings();
   
   IParameterMapping createParameterMapping(IData data, String dataPath, String parameter, String parameterPath, int elementOID);

   void setType(ITriggerType type);

   void removeFromParameterMappings(IParameterMapping mapping);

   void addToParameterMappings(IParameterMapping mapping);

   void addToPersistentAccessPoints(IAccessPoint ap);
}
