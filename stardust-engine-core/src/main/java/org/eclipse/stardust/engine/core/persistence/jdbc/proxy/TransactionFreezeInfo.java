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
package org.eclipse.stardust.engine.core.persistence.jdbc.proxy;

import org.eclipse.stardust.common.error.TransactionFreezedException;


/**
 * @author sborn
 * @version $Revision$
 */
public class TransactionFreezeInfo
{
   TransactionFreezedException txFreezeException;

   public TransactionFreezeInfo()
   {
      txFreezeException = null;
   }

   public TransactionFreezedException getTxFreezeException()
   {
      return txFreezeException;
   }

   public void setTxFreezeException(TransactionFreezedException txFreezeException)
   {
      this.txFreezeException = txFreezeException;
   }
   
   public boolean isFreezed()
   {
      return txFreezeException == null ? false : true;
   }
}
