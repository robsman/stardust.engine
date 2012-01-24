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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Connection extends ModelElement implements Symbol
{
   private String name;
   private int firstOID;
   private int secondOID;

   public Connection(String name, int firstID, int secondID)
   {
      this.name = name;
      this.firstOID = firstID;
      this.secondOID = secondID;
   }

   public int getFirstOID()
   {
      return firstOID;
   }

   public String getName()
   {
      return name;
   }

   public int getSecondOID()
   {
      return secondOID;
   }
}
