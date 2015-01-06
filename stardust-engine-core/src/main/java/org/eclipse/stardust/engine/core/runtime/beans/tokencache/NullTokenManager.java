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
package org.eclipse.stardust.engine.core.runtime.beans.tokencache;

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;

public class NullTokenManager implements ISecondLevelTokenCache
{
   public boolean removeToken(TransitionTokenBean token)
   {
      return false;
   }

   public void flush()
   {
   }

   public TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      return null;
   }

   public boolean hasCompleteInformation()
   {
      return false;
   }

   public TransitionTokenBean lockFirstAvailableToken(ITransition transition)
   {
      return null;
   }

   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
   }

   public void unlockTokens(List tokens)
   {
   }

   public void registerPersistenceControllers(List tokens)
   {
   }

   public void unlockForTransaction(Object transaction)
   {
   }

   public void registerProcessInstance()
   {
   }

   public void registerTransaction(Object transaction)
   {
   }

   public long getUnconsumedTokenCount()
   {
      throw new RuntimeException("Should not be called since hasCompleteInformation always returns false");
   }

   public void addLocalToken(TransitionTokenBean token)
   {
   }

   @Override
   public TransitionTokenBean lockSourceAndOtherToken(TransitionTokenBean token)
   {
      return null;
   }

   @Override
   public boolean hasUnconsumedTokens(Set<ITransition> transitions)
   {
      return false;
   }
}
