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
package org.eclipse.stardust.engine.api.spring;

import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.SecondLevelTokenManager;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.TokenManagerRegistry;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class SpringSecondLevelTokenManager extends SecondLevelTokenManager
{

   public void registerTransaction(final Object transaction)
   {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
      {
         public void afterCompletion(int status)
         {
            TokenManagerRegistry.instance().unlockTokensForTransaction(transaction);
         }
      });
   }

}
