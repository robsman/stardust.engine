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
package org.eclipse.stardust.engine.core.persistence;

import java.util.Vector;

/**
 * This is an extension to vector class as we might
 * need both ordered as well as non-ordered collections.
 *
 * Boolean ordered determines what should be done.
 *
 * e.g, ModelManager will need ordered models holder vector,
 * under normal conditions it will be non ordered.
 */
public class OrderedVector extends Vector
{
   private boolean ordered;
   /**
    *
    */
   public OrderedVector(boolean ordered)
   {
      super();

      this.ordered  = ordered;
   }
   /**
    *
    */
   public boolean add(Object object)
   {
      if (ordered && (object instanceof Comparable))
      {
         int i, j = size();

         for (i =0; i < j && (((Comparable)(elementAt(i))).compareTo(object) < 0)
               ; i ++);

         insertElementAt(object, i);

         return true;
      }
      else
      {
         return super.add(object);
      }
   }
}
