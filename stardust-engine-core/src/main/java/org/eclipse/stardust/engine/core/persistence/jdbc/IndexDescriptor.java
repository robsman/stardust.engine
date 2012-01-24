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
package org.eclipse.stardust.engine.core.persistence.jdbc;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class IndexDescriptor
{
   private final String name;
   private final String[] columns;
   private final boolean unique;

   public IndexDescriptor(String name, String[] columns, boolean unique)
   {
      this.name = name;
      this.columns = columns;
      this.unique = unique;
   }

   public final String getName()
   {
      return name;
   }

   public final String[] getColumns()
   {
      return columns;
   }

   public final boolean isUnique()
   {
      return unique;
   }
}
