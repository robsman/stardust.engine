/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.model.utils;

import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;

public abstract class ExclusionComputer<A, T>
{
   Map<T, Set<T>> paths = CollectionUtils.newMap();

   public final A getBlockingActivity(A activity)
   {
      if (isInclusiveJoin(activity))
      {
         for (T transition : getIn(activity))
         {
            Set<T> exclusionSet = getExclusionSet(transition);
            for (T other : exclusionSet)
            {
               A candidate = getTo(other);
               if (candidate != activity && isInclusiveJoin(candidate))
               {
                  for (T excl : getIn(candidate))
                  {
                     if (getExclusionSet(excl).contains(transition))
                     {
                        return candidate;
                     }
                  }
               }
            }
         }
      }
      return null;
   }

   public final Set<T> getExclusionSet(T transition)
   {
      Set<T> exclusionSet = CollectionUtils.newSet();
      A activity = getTo(transition);
      for (T other : getIn(activity))
      {
         if (other != transition)
         {
            exclusionSet.addAll(getPaths(other));
         }
      }
      exclusionSet.removeAll(getPaths(transition));
      return exclusionSet;
   }

   private Set<T> getPaths(T transition)
   {
      Set<T> result = paths.get(transition);
      if (result == null)
      {
         result = CollectionUtils.newSet();
         getParents(result, transition, getTo(transition));
         paths.put(transition, result);
      }
      return result;
   }

   private void getParents(Set<T> result, T transition, A stopAt)
   {
      if (!result.contains(transition))
      {
         result.add(transition);
         A from = getFrom(transition);
         if (from != stopAt)
         {
            for (T parent : getIn(from))
            {
               getParents(result, parent, stopAt);
            }
         }
      }
   }

   protected abstract A getFrom(T transition);

   protected abstract A getTo(T transition);

   protected abstract Iterable<T> getIn(A activity);

   protected abstract boolean isInclusiveJoin(A activity);
}
