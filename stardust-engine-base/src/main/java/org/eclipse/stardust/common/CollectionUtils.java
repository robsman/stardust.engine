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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CollectionUtils
{

   public static <E> boolean isEmpty(E[] a)
   {
      return (null == a) || (0 == a.length);
   }

   public static boolean isEmpty(Collection<?> c)
   {
      return (null == c) || c.isEmpty();
   }

   public static boolean isEmpty(Map<?, ?> m)
   {
      return (null == m) || m.isEmpty();
   }

   public static <E> boolean isNotEmpty(E[] a)
   {
      return !isEmpty(a);
   }

   public static boolean isNotEmpty(Collection<?> c)
   {
      return !isEmpty(c);
   }

   public static boolean isNotEmpty(Map<?, ?> m)
   {
      return !isEmpty(m);
   }

   public static <E> List<E> newList()
   {
      return newArrayList();
   }

   public static <E> List<E> newList(int initalCapacity)
   {
      return newArrayList(initalCapacity);
   }

   public static <E> List<E> newList(Collection<? extends E> template)
   {
      return newArrayList(template);
   }

   public static <E> List<E> newListFromElements(Collection<E> elements)
   {
      return newArrayListFromElements(elements);
   }

   public static <E> List<E> newListFromIterator(Iterator<E> iterator)
   {
      return newArrayListFromIterator(iterator);
   }

   /**
    * @deprecated Superseded by {@link #newList()}
    */
   public static <E> List<E> createList()
   {
      return newList();
   }

   public static <E> ArrayList<E> newArrayList()
   {
      return new ArrayList<E>();
   }

   public static <E> ArrayList<E> newArrayList(int initalCapacity)
   {
      return new ArrayList<E>(initalCapacity);
   }

   public static <E> List<E> newArrayList(Collection<? extends E> template)
   {
      return new ArrayList<E>(template);
   }

   public static <E> List<E> newArrayListFromElements(Collection<? extends E> elements)
   {
      return new ArrayList<E>(elements);
   }

   public static <E> List<E> newArrayListFromIterator(Iterator<? extends E> iterator)
   {
      ArrayList<E> result = newArrayList();
      while (iterator.hasNext())
      {
         result.add(iterator.next());
      }
      return result;
   }

   /**
    * @deprecated Superseded by {@link #newArrayList()}
    */
   public static <E> ArrayList<E> createArrayList()
   {
      return newArrayList();
   }

   public static <E> LinkedList<E> newLinkedList()
   {
      return new LinkedList<E>();
   }

   public static <E> List<E> newLinkedListFromElements(Collection<? extends E> elements)
   {
      return new LinkedList<E>(elements);
   }

   /**
    * @deprecated Superseded by {@link #newLinkedList()}
    */
   public static <E> LinkedList<E> createLinkedList()
   {
      return newLinkedList();
   }

   public static <E> List<E> copyList(List<? extends E> rhs)
   {
      return new ArrayList<E>(rhs);
   }

   public static <E> ArrayList<E> copyList(ArrayList<? extends E> rhs)
   {
      return new ArrayList<E>(rhs);
   }

   public static <E> LinkedList<E> copyList(LinkedList<? extends E> rhs)
   {
      return new LinkedList<E>(rhs);
   }

   public static <E> Set<E> newSet()
   {
      return newHashSet();
   }

   public static <E> Set<E> newSet(Collection<E> from)
   {
      return newHashSet(from);
   }

   public static <E> Set<E> newSetFromIterator(Iterator<E> iterator)
   {
      return newHashSetFromIterator(iterator);
   }

   /**
    * @deprecated Superseded by {@link #newSet()}
    */
   public static <E> Set<E> createSet()
   {
      return newSet();
   }

   public static <E> HashSet<E> newHashSet()
   {
      return new HashSet<E>();
   }

   public static <E> HashSet<E> newHashSet(Collection<E> from)
   {
      return new HashSet<E>(from);
   }

   public static <E> HashSet<E> newHashSetFromIterator(Iterator<E> iterator)
   {
      HashSet<E> result = newHashSet();
      while (iterator.hasNext())
      {
         result.add(iterator.next());
      }
      return result;
   }

   /**
    * @deprecated Superseded by {@link #newHashSet()}
    */
   public static <E> HashSet<E> createHashSet()
   {
      return newHashSet();
   }

   public static <E> TreeSet<E> newTreeSet()
   {
      return new TreeSet<E>();
   }

   /**
    * @deprecated Superseded by {@link #newTreeSet()}
    */
   public static <E> TreeSet<E> createTreeSet()
   {
      return newTreeSet();
   }

   public static <E> Set<E> copySet(Set<E> rhs)
   {
      return new HashSet<E>(rhs);
   }

   public static <E> HashSet<E> copySet(HashSet<? extends E> rhs)
   {
      return new HashSet<E>(rhs);
   }

   public static <E> TreeSet<E> copySet(TreeSet<? extends E> rhs)
   {
      return new TreeSet<E>(rhs);
   }

   public static <K, V> Map<K, V> newMap()
   {
      return newHashMap();
   }

   /**
    * @deprecated Superseded by {@link #newMap()}
    */
   public static <K, V> Map<K, V> createMap()
   {
      return newMap();
   }

   public static <K, V> SortedMap<K, V> newSortedMap()
   {
      return newTreeMap();
   }

   /**
    * @deprecated Superseded by {@link #newSortedMap()}
    */
   public static <K, V> SortedMap<K, V> createSortedMap()
   {
      return newSortedMap();
   }

   public static <K, V> HashMap<K, V> newHashMap()
   {
      return new HashMap<K, V>();
   }

   public static <K, V> HashMap<K, V> newHashMap(int initialCapacity)
   {
      return new HashMap<K, V>(initialCapacity);
   }

   /**
    * @deprecated Superseded by {@link #newHashMap()}
    */
   public static <K, V> HashMap<K, V> createHashMap()
   {
      return newHashMap();
   }

   public static <K, V> TreeMap<K, V> newTreeMap()
   {
      return new TreeMap<K, V>();
   }

   /**
    * @deprecated Superseded by {@link #newTreeMap()}
    */
   public static <K, V> TreeMap<K, V> createTreeMap()
   {
      return newTreeMap();
   }

   public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap()
   {
      return new ConcurrentHashMap<K, V>();
   }

   public static <K, V> Map<K, V> copyMap(Map<? extends K, ? extends V> rhs)
   {
      return new HashMap<K, V>(rhs);
   }

   public static <K, V> SortedMap<K, V> copyMap(SortedMap<? extends K, ? extends V> rhs)
   {
      return new TreeMap<K, V>(rhs);
   }

   public static <K, V> HashMap<K, V> copyMap(HashMap<? extends K, ? extends V> rhs)
   {
      return new HashMap<K, V>(rhs);
   }

   public static <K, V> TreeMap<K, V> copyMap(TreeMap<? extends K, ? extends V> rhs)
   {
      return new TreeMap<K, V>(rhs);
   }

   public static <S, T> void transform(Collection<T> target, Collection<? extends S> source, Functor<S, T> transformer)
   {
      for (Iterator<? extends S> iterator = source.iterator(); iterator.hasNext();)
      {
         target.add(transformer.execute(iterator.next()));
      }
   }

   /**
    * Returns a list of sublist containing content of source with each sublist size maximal chunkSize.
    *
    * @param source List of elements
    * @param chunkSize maximum size of sublists
    * @return List containing sublists
    */
   public static <E> List<List<E>> split(List<E> source, int chunkSize)
   {
      final List<List<E>> result = newList(source.size() / chunkSize + 1);
      if (source.size() < chunkSize)
      {
         result.add(source);
      }
      else
      {
         int fromIdx = 0;
         int toIdx = chunkSize;

         List<E> oidSubList = source.subList(fromIdx, toIdx);
         result.add(oidSubList);

         boolean hasMoreOids = source.size() > toIdx;
         while (hasMoreOids)
         {
            fromIdx = fromIdx + chunkSize;
            toIdx = source.size() >= toIdx + chunkSize
                  ? toIdx + chunkSize
                  : source.size();
            oidSubList = source.subList(fromIdx, toIdx);
            result.add(oidSubList);
            hasMoreOids = source.size() > toIdx;
         }
      }

      return result;
   }

   /**
    * Returns a list of sublist containing content of source with each sublist size maximal chunkSize.
    *
    * @param source Collection of elements
    * @param chunkSize maximum size of sublists
    * @return List containing sublists
    */
   public static <E> List<List<E>> split(Collection<E> source, int chunkSize)
   {
      if (source instanceof List)
      {
         return split((List<E>) source, chunkSize);
      }
      else
      {
         List<E> srcList = newArrayList(source);
         return split(srcList, chunkSize);
      }
   }
   public static <E> List<E> intersect(List<E> lhs, List<E> rhs)
   {
      if (isEmpty(lhs) || isEmpty(rhs))
      {
         return newList();
      }
      List<E> result = copyList(lhs);
      result.retainAll(rhs);
      return result;
   }



   public static <E> List<E> union(List<E> lhs, List<E> rhs)
   {
      List<E> result;

      if (Collections.emptyList().equals(lhs))
      {
         result = rhs;
      }
      else
      {
         lhs.addAll(rhs);
         result = lhs;
      }

      return result;
   }

   public static <E> List<E> union(List<E> lhs, List<E> rhs, boolean addToLhs)
   {
      if (addToLhs)
      {
         return union(lhs, rhs);
      }

      List<E> result;

      if (Collections.emptyList().equals(lhs))
      {
         result = rhs;
      }
      else if (Collections.emptyList().equals(rhs))
      {
         result = lhs;
      }
      else
      {
         result = newArrayList();
         result.addAll(lhs);
         result.addAll(rhs);
      }

      return result;
   }

   private CollectionUtils()
   {
      // utility class
   }

   public static Map<String, Object> convertToLegacyParameters(
         Map<String, ? extends Serializable> serializableMap)
   {
      return Collections.unmodifiableMap((Map<String, ?>) serializableMap);
   }

   @SuppressWarnings("unchecked")
   public static Map<String, ? extends Serializable> convertFromLegacyParameters(
         Map<String, Object> legacyParams)
   {
      if (legacyParams == null)
      {
         return null;
      }
      for (Object value : legacyParams.values())
      {
         if( !(value instanceof Serializable))
         {
            throw new IllegalArgumentException("Legacy map contains unexpected values");
         }
      }
      // (fh) double casting to avoid eclipse compiler errors
      return (Map<String, ? extends Serializable>) ((Map<String, ?>) legacyParams);
   }

   public static <K, V> void remove(Map<K, V> map, Set<K> set)
   {
      for (K k : set)
      {
         map.remove(k);
      }
   }
}
