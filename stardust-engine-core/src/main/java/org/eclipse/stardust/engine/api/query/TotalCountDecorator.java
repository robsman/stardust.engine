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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.core.persistence.ResultIterator;

/**
 * @author rsauer
 * @version $Revision$
 */
public class TotalCountDecorator implements ResultIterator
{
   private final long totalCount;
   private final ResultIterator target;

   public TotalCountDecorator(long totalCount, ResultIterator target)
   {
      this.totalCount = totalCount;
      this.target = target;
   }

   public int getStartIndex()
   {
      return target.getStartIndex();
   }

   public int getMaxSize()
   {
      return target.getMaxSize();
   }

   public boolean hasMore()
   {
      return target.hasMore();
   }

   public boolean hasTotalCount()
   {
      return true;
   }

   public long getTotalCount()
   {
      return totalCount;
   }

   public boolean hasNext()
   {
      return target.hasNext();
   }

   public Object next()
   {
      return target.next();
   }

   public void remove()
   {
      target.remove();
   }

   public void close()
   {
      target.close();
   }
}
