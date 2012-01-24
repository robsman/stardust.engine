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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.util.List;

/**
 * @author fherinean
 * @version $Revision$
 */
public class PortInfo
{
   private String name;
   private String SOAPAddress;
   private List operations;

   public PortInfo(String name, String SOAPAddress, List operations)
   {
      this.name = name;
      this.SOAPAddress = SOAPAddress;
      this.operations = operations;
   }

   public String getName()
   {
      return name;
   }

   public String getSOAPAddress()
   {
      return SOAPAddress;
   }

   public List getOperations()
   {
      return operations;
   }

   public String toString()
   {
      return name;
   }

   public OperationInfo findOperation(String name)
   {
      for (int i = 0; i < operations.size(); i++)
      {
         OperationInfo operation = (OperationInfo) operations.get(i);
         if (operation.getName().equals(name))
         {
            return operation;
         }
      }
      return null;
   }
}
