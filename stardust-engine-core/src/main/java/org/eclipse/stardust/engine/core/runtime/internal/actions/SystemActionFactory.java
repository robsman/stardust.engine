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
package org.eclipse.stardust.engine.core.runtime.internal.actions;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.runtime.ISystemAction;



public class SystemActionFactory implements ISystemAction.Factory
{
   public String getId()
   {
      return "System";
   }

   public List<ISystemAction> createActions()
   {
      List<ISystemAction> actions = CollectionUtils.newList();
      actions.add(new PasswordNotifierAction());
      return actions;
   }
}