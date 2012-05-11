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
package org.eclipse.stardust.common.rt;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author rsauer
 * @version $Revision$
 */
public class TransactionUtils
{
   private static final Logger trace = LogManager.getLogger(TransactionUtils.class);
   
   public static final String KEY_TX_STATUS = TransactionUtils.class.getName()
         + ".TxStatus";
   
   public static final ITransactionStatus NO_OP_TX_STATUS = new NoOpTxStatus();

   public static ITransactionStatus getCurrentTxStatus()
   {
      return getCurrentTxStatus(Parameters.instance());
   }

   public static ITransactionStatus getCurrentTxStatus(Parameters params)
   {
      ITransactionStatus txStatus = (ITransactionStatus) params.get(KEY_TX_STATUS);
      
      if (null == txStatus)
      {
         txStatus = NO_OP_TX_STATUS;
      }
      
      return txStatus;
   }

   public static boolean isCurrentTxRollbackOnly()
   {
      return isCurrentTxRollbackOnly(Parameters.instance());
   }
   
   public static boolean isCurrentTxRollbackOnly(Parameters params)
   {
      ITransactionStatus txStatus = getCurrentTxStatus(params);
      
      return txStatus.isRollbackOnly();
   }

   public static void registerTxStatus(PropertyLayer props, ITransactionStatus txStatus)
   {
      props.setProperty(KEY_TX_STATUS, txStatus);
   }
   
   private TransactionUtils()
   {
      // utility class
   }

   private static class NoOpTxStatus implements ITransactionStatus
   {

      public boolean isRollbackOnly()
      {
         return false;
      }

      public void setRollbackOnly()
      {
         trace.warn("The current invocation context has no associated TX status,"
               + " ignoring request to rollback forcibly.");
      }
      
      public Object getTransaction()
      {
         trace.warn("The current invocation context has no associated TX status.");
         
         // Transaction object is unknown in NoOpTxStatus
         return null;
      }
      
   }
}
