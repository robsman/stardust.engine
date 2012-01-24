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


/**
 *
 */
public interface IApplication
      extends IViewable, IdentifiableElement, AccessPointOwner, Typeable
{
   void checkConsistency(List inconsistencies);

   void setInteractive(boolean interactive);

   boolean isInteractive();

   IApplicationContext createContext(String type, int elementOID);

   void removeContext(String id);

   void removeAllContexts();

   Iterator getAllContexts();

   IApplicationContext findContext(String id);

   void setApplicationType(IApplicationType type);

   void addToPersistentAccessPoints(IAccessPoint ap);

   boolean isSynchronous();
}
