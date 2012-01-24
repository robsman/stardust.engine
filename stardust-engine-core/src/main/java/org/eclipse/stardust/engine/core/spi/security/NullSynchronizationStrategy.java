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
package org.eclipse.stardust.engine.core.spi.security;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class NullSynchronizationStrategy
      extends DynamicParticipantSynchronizationStrategy
{
   public boolean isDirty(IUser user)
   {
      return false;
   }

   public void setSynchronized(IUser user)
   {
   }

   public boolean isDirty(IUserGroup userGroup)
   {
      return false;
   }

   public void setSynchronized(IUserGroup userGroup)
   {
   }
}
