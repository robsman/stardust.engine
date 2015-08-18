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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Assert;


/**
 * Joins can hold an arbitrarily number of <code>Join</code>s.
 * 
 * @author sborn
 * @version $Revision$
 */
public class Joins implements Iterable<Join>
{
   private List<Join> joins;
   
   public static Joins shallowCopy(Joins rhs)
   {
      return new Joins(rhs);
   }
   
   public static Joins newJoins(List<Join> joinList)
   {
      Joins joins = new Joins();

      for (Iterator<Join> iterator = joinList.iterator(); iterator.hasNext();)
      {
         Join join = (Join) iterator.next();
         joins.add(join);
      }

      return joins;
   }
   
   /**
    * Constructs an empty Joins container.
    */
   public Joins()
   {
      joins = new ArrayList<Join>();
   }
   
   protected Joins(Joins rhs)
   {
      this.joins = new ArrayList<Join>(rhs.joins);
   }
   
   public boolean contains(Join join)
   {
      Assert.isNotNull(join, "join argument is expected not be null");
      
      return joins.contains(join);
   }
   
   /**
    * Adds an <code>Join</code> item to this Joins container.
    * 
    * @param item The join description
    * 
    * @return this
    */
   public Joins add(Join item)
   {
      Assert.isNotNull(item, "item argument is expected not be null");
      
      joins.add(item);
      
      return this;
   }
   
   /**
    * Adds all <code>Join</code> items from the given Joins container to this Joins
    * container.
    * 
    * @param items The join description container
    * 
    * @return this
    */
   public Joins add(Joins items)
   {
      Assert.isNotNull(items, "items argument is expected not be null");

      if ( !items.isEmpty())
      {
         joins.addAll(items.joins);
      }

      return this;
   }
   
   /**
    * Returns an iterator for <code>Join</code>s in this container.
    * 
    * @return The iterator
    */
   public Iterator<Join> iterator()
   {
      return joins.iterator();
   }
   
   public boolean isEmpty()
   {
      return joins.isEmpty();
   }

   public int size()
   {
      return joins.size();
   }

   public Join get(int index)
   {
      return joins.get(index);
   }
}   
