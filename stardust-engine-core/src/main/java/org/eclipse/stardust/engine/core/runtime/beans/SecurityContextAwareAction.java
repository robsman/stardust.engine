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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.Action;

public abstract class SecurityContextAwareAction<T> implements Action<T>
{
   private short partitionOid;
   private long userDomainOid;

   protected SecurityContextAwareAction(ActionCarrier carrier)
   {
      this.partitionOid = carrier.getPartitionOid();
      this.userDomainOid = carrier.getUserDomainOid();
   }

   public short getPartitionOid()
   {
      return partitionOid;
   }

   public long getUserDomainOid()
   {
      return userDomainOid;
   }

   protected void setPartitionOid(short partitionOid)
   {
      this.partitionOid = partitionOid;
   }

   protected void setUserDomainOid(long userDomainOid)
   {
      this.userDomainOid = userDomainOid;
   }
   
   public static SecurityContextBoundAction actionDefinesSecurityContext(
         SecurityContextAwareAction action)
   {
      return new SecurityContextBoundAction(action);
   }
   
   public static class SecurityContextBoundAction implements Action
   {
      
      private final SecurityContextAwareAction action;

      public SecurityContextBoundAction(SecurityContextAwareAction delegate)
      {
         this.action = delegate;
      }

      public SecurityContextAwareAction getAction()
      {
         return action;
      }

      public Object execute()
      {
         return action.execute();
      }
      
      public String toString()
      {
    	 return action.toString();
      }
   }
}
