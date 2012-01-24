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
package org.eclipse.stardust.engine.core.model.utils;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Connections extends Link
{
   private String rightRole;
   private String leftRole;

   public Connections(ModelElement owner, String name, String leftRole, String rightRole)
   {
      super(owner, name);
      this.leftRole = leftRole;
      this.rightRole = rightRole;
   }

   public void __add__(ModelElement element)
   {
      super.add(element);
      Connection connection = (Connection) element;
      connection.connect(leftRole, rightRole);
   }

   public void add(ModelElement element)
   {
      super.add(element);
      Connection connection = (Connection) element;
      connection.connect(leftRole, rightRole);
      connection.attachEndPoint(connection.getFirst(), leftRole);
      connection.attachEndPoint(connection.getSecond(), rightRole);
   }
}
