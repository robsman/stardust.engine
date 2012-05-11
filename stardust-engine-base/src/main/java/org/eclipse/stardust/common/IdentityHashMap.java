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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * HashMap using this identity instead of .equals(), provided for JDK 1.3
 * compatibility.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class IdentityHashMap extends HashMap
{
   private static final long serialVersionUID = -1691596568858816760L;

   public IdentityHashMap(int initialCapacity, float loadFactor)
   {
      super(initialCapacity, loadFactor);
   }

   public IdentityHashMap(int initialCapacity)
   {
      super(initialCapacity);
   }

   public IdentityHashMap()
   {
      super();
   }

   public IdentityHashMap(Map t)
   {
      super(t);
   }

   /**
    * @see Map#get(Object)
    */
   public Object get(Object key)
   {
      return super.get(new ThisKey(key));
   }

   /**
    * @see Map#put(Object, Object)
    */
   public Object put(Object key, Object value)
   {
      return super.put(new ThisKey(key), value);
   }

   /**
    * adds an object to the Map. new Identity(obj) is used as key
    */
   public Object add(Object value)
   {
      Object key = new ThisKey(value);
      if (!super.containsKey(key))
      {
         return super.put(key, value);
      }
      else
         return null;

   }

   /**
    * @see Map#remove(Object)
    */
   public Object remove(Object key)
   {
      return super.remove(new ThisKey(key));
   }

   /**
    * @see Map#containsKey(Object)
    */
   public boolean containsKey(Object key)
   {
      return super.containsKey(new ThisKey(key));
   }
   
   public Set keySet()
   {
      return new ThisKeySetDecorator(super.keySet());
   }

   public Set entrySet()
   {
      return new ThisKeySetDecorator(super.entrySet());
   }

   /**
    * Wrap an identity key (System.identityHashCode())
    */
   private static final class ThisKey
   {
      private final Object wrappedThis;

      private final int hashCode;

      public ThisKey(Object value)
      {
         // This is the Object hashcode
         this.hashCode = System.identityHashCode(value);
         // There have been some cases that return the
         // same identity hash code for different objects. So
         // the value is also added to disambiguate these cases.
         this.wrappedThis = value;
      }

      public int hashCode()
      {
         return hashCode;
      }

      public boolean equals(Object rhs)
      {
         if (!(rhs instanceof ThisKey))
         {
            return false;
         }
         ThisKey rhsKey = (ThisKey) rhs;
         if (hashCode != rhsKey.hashCode)
         {
            return false;
         }
         // Note that identity equals is used.
         return wrappedThis == rhsKey.wrappedThis;
      }
   }
   
   private static final class ThisKeySetDecorator implements Set
   {
      private final Set keySet;
      
      private ThisKeySetDecorator(Set keySet)
      {
         this.keySet = keySet;
      }

      public int size()
      {
         return keySet.size();
      }

      public void clear()
      {
         keySet.clear();
      }

      public boolean isEmpty()
      {
         return keySet.isEmpty();
      }

      public Object[] toArray()
      {
         throw new UnsupportedOperationException();
      }

      public boolean add(Object o)
      {
         return keySet.add(new ThisKey(o));
      }

      public boolean contains(Object o)
      {
         return keySet.contains(new ThisKey(o));
      }

      public boolean remove(Object o)
      {
         return keySet.remove(new ThisKey(o));
      }

      public boolean addAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean containsAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean removeAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public boolean retainAll(Collection c)
      {
         throw new UnsupportedOperationException();
      }

      public Iterator iterator()
      {
         return new TransformingIterator(keySet.iterator(), new Functor()
         {
            public Object execute(Object source)
            {
               return source instanceof ThisKey
                     ? ((ThisKey) source).wrappedThis
                     : source instanceof Map.Entry
                     ? new ThisKeyEntry((Map.Entry) source)
                     : source;
            }
         });
      }

      public Object[] toArray(Object[] a)
      {
         throw new UnsupportedOperationException();
      }
   }

   private static final class ThisKeyEntry implements Map.Entry
   {
      private Map.Entry source;

      public ThisKeyEntry(Map.Entry source)
      {
         this.source = source;
      }

      public Object getKey()
      {
         return source.getKey() instanceof ThisKey
               ? ((ThisKey) source.getKey()).wrappedThis
               : source.getKey();
      }

      public Object getValue()
      {
         return source.getValue();
      }

      public Object setValue(Object value)
      {
         return source.setValue(value);
      }
   }
}