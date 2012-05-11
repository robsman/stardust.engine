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
package org.eclipse.stardust.common;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ConcatenatedList<E> extends AbstractList<E> implements List<E>, Serializable
{
   private static final long serialVersionUID = 3656132074134717856L;

   private List<? extends E> b;
   private List<? extends E> a;

   public ConcatenatedList(List<? extends E> a, List<? extends E> b)
   {
      this.a = a;
      this.b = b;
   }
   public int size()
   {
      return a.size() + b.size();
   }

   public E get(int index)
   {
      return (E)(index < a.size() ? a.get(index) : b.get(index - a.size()));
   }
}
