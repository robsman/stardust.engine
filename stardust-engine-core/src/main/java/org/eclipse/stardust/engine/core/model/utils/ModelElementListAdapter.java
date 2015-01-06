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

import java.util.Iterator;
import java.util.List;

/**
 * @author sauer
 * @version $Revision$
 */
public class ModelElementListAdapter<T extends ModelElement> implements ModelElementList<T>
{
   private final List<T> delegate;

   public ModelElementListAdapter(List<T> delegate)
   {
      this.delegate = delegate;
   }
   
   public List<T> getDelegate()
   {
      return delegate;
   }

   public int size()
   {
      return delegate.size();
   }
   
   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }
   
   public T get(int index)
   {
      return delegate.get(index);
   }

   public Iterator<T> iterator()
   {
      return delegate.iterator();
   }
}