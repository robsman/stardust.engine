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
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface IEventHandler extends IdentifiableElement, Typeable, AccessPointOwner
{
   void setConditionType(IEventConditionType type);

   Iterator getAllEventActions();

   Iterator getAllBindActions();

   Iterator getAllUnbindActions();

   IEventAction createEventAction(String id, String name, IEventActionType type,
         int elementOID);

   IBindAction createBindAction(String id, String name, IEventActionType type,
         int elementOID);

   IUnbindAction createUnbindAction(String id, String name, IEventActionType type,
         int elementOID);

   boolean isAutoBind();

   boolean isUnbindOnMatch();

   void setAutoBind(boolean autoBind);

   void setUnbindOnMatch(boolean disableOnFire);

   boolean isLogHandler();

   void setLogHandler(boolean logHandler);

   boolean isConsumeOnMatch();

   void setConsumeOnMatch(boolean swallow);

   boolean hasBindActions();

   boolean hasUnbindActions();

   void removeFromEventActions(IEventAction action);

   void removeFromBindActions(IBindAction action);

   void removeFromUnbindActions(IUnbindAction action);

   void addToEventActions(IEventAction action);

   void addToBindActions(IBindAction action);

   void addToUnbindActions(IUnbindAction action);

   void checkConsistency(List inconsistencies);
}
