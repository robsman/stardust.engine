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
package org.eclipse.stardust.engine.core.struct.spi;

import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.core.persistence.Join;


public class StructuredDataFilterContext {
   private String path;
   private int id;
   private Join join;
   private AbstractDataFilter dataFilter;
   
   public StructuredDataFilterContext(int id, AbstractDataFilter dataFilter)
   {
      this.path = dataFilter.getAttributeName();
      this.id = id;
      this.dataFilter = dataFilter;
   }
   public int getId()
   {
      return id;
   }
   public String getPath()
   {
      return path;
   }
   public void setJoin(Join join)
   {
      this.join = join;
   }
   public Join getJoin()
   {
      return this.join;
   }
   
   public AbstractDataFilter getDataFilter()
   {
      return this.dataFilter;
   }
   
}