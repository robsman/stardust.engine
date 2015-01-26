/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;


/**
 * Represents a worklist in it's hierarchical structure.
 *
 * @author ubirkemeyer, rsauer
 * @version $Revision$
 */
public abstract class Worklist extends AbstractList implements QueryResult
{
   protected static final Logger trace = LogManager.getLogger(Worklist.class);
   
   private final WorklistQuery query;
   private final SubsetPolicy subsetPolicy;

   private final List items;
   private final boolean moreAvailable;
   
   private final Long totalCount;
   private final long totalCountThreshold;
   
   Worklist(WorklistQuery query, SubsetPolicy subset, List items, boolean moreAvailable,
         Long totalCount)
   {
      this(query, subset, items, moreAvailable, totalCount, Long.MAX_VALUE);
   }

   Worklist(WorklistQuery query, SubsetPolicy subset, List items, boolean moreAvailable,
         Long totalCount, long totalCountThreshold)
   {
      this.query = query;
      this.subsetPolicy = subset;

      this.items = new ArrayList(items);
      this.moreAvailable = moreAvailable;

      this.totalCount = totalCount;
      this.totalCountThreshold = totalCountThreshold;
   }
   
   /**
    * Retrieves the owning participant of this worklist.
    * 
    * @return The owning participant.
    */
   abstract public ParticipantInfo getOwner();

   /**
    * Indicates if the worklist is a user worklist or a participant worklist.
    *
    * <p>User worklists may contain nested sub-worklists, while participant worklists
    * can't.</p>
    *
    * @return <code>true</code> in case of a user worklist, <code>false</code> in case of
    *         a participant worklist.
    */
   public abstract boolean isUserWorklist();

   /**
    * The OID of the owner of this worklist.
    *
    * <p>If the worklist belongs to a participant not existing anymore in the current
    * model version the OID will be <code>0</code>.</p>
    *
    * @return The owner's OID.
    *
    * @see #getOwnerID
    */
   public abstract long getOwnerOID();

   /**
    * The human readable ID of the worklist owner.
    *
    * @return The owner's ID.
    *
    * @see #getOwnerOID
    */
   public abstract String getOwnerID();

   /**
    * The name of the owner of the worklist.
    *
    * <p>If the worklist belongs to a participant not existing anymore in the current
    * model version the name will be empty.</p>
    *
    * @return The owner's name.
    *
    * @see #getOwnerID
    */
   public abstract String getOwnerName();

   /**
    * Retrieves the query this worklist is based on.
    *
    * @return The {@link WorklistQuery} the worklist is based on.
    */
   public WorklistQuery getQuery()
   {
      return query;
   }

   /**
    * Retrieves an iterator over all contained sub-worklists.
    *
    * <p>Only user worklists may contain sub-worklists.</p>
    *
    * @return An iterator over all conatined sub-worklists. The elements of the iterator
    *         are in turn of type {@link Worklist}.
    *
    * @see #isUserWorklist
    */
   public abstract Iterator getSubWorklists();

   public Object get(int index)
   {
      return items.get(index);
   }

   /**
    * Retrieves the number of items belonging directly to this worklist.
    *
    * @return The number of items directly belonging to this worklist, possibly
    *         constrained by its {@link SubsetPolicy}. The size of possibly existing
    *         sub-worklist is not taken into account.
    *
    * @see #getCumulatedSize
    * @see QueryResult#iterator()
    */
   public int size()
   {
      return size(null);
   }

   private int size(List visited)
   {
      if(visited == null)
      {
         return items.size();
      }
      
      int counter = 0;
      for(Object item : items)
      {
         if(!visited.contains(item))
         {
            visited.add(item);
            counter++;            
         }
      }
      
      return counter;
   }   
   
   public long getTotalCount() throws UnsupportedOperationException
   {
      if (null == totalCount)
      {
         throw new UnsupportedOperationException("Total item count is not available.");
      }
      return totalCount.longValue();
   }

   /**
    * Retrieves the subset policy this worklist is based on.
    *
    * @return The {@link SubsetPolicy} the worklist is based on.
    */
   public SubsetPolicy getSubsetPolicy()
   {
      return subsetPolicy;
   }

   /**
    * Indicates if there are more worklist items available (maybe inherited from
    * participant contributions) in either this worklist or any of its participant
    * contributions than allowed by the contribution definition's {@link SubsetPolicy}.
    *
    * @return <code>true</code> if there are more items available in this worklist,
    *         <code>false</code> if not.
    *
    * @see #hasMorePrivateItems
    * @see #getSubsetPolicy
    */
   public boolean hasMore()
   {
      boolean hasMore = hasMorePrivateItems();

      for (Iterator i = getSubWorklists(); i.hasNext();)
      {
         hasMore |= ((Worklist) i.next()).hasMore();
      }

      return hasMore;
   }

   /**
    * Indicates if there are more noninherited items available in this worklist (i.e. the
    * user's private worklist or a specific participant's worklist) than allowed by its
    * {@link SubsetPolicy}.
    *
    * @return <code>true</code> if there are more noninherited items available in this
    *         worklist, <code>false</code> if not.
    *
    * @see #getSubsetPolicy
    */
   public boolean hasMorePrivateItems()
   {
      return moreAvailable;
   }

   /**
    * Retrieves the number of items belonging directly and indirectly to this worklist.
    *
    * @return The number of items directly and indirectly belonging to this worklist,
    *         possibly constrained by its {@link SubsetPolicy}. The size of possibly
    *         existing sub-worklist is taken into account.
    *
    * @see #size
    * @see #getCumulatedItems()
    */
   public int getCumulatedSize()
   {
      List visited = new ArrayList();
      return getCumulatedSize(visited);
   }

   private int getCumulatedSize(List visited)
   {
      boolean isMerged = true;
      WorklistLayoutPolicy worklistLayoutPolicy = (WorklistLayoutPolicy) query.getPolicy(WorklistLayoutPolicy.class);
      if(worklistLayoutPolicy != null)
      {
         isMerged = worklistLayoutPolicy.isMerged();
      }
      
      int totalSize = 0;
      if(!isMerged)
      {
         totalSize = size(visited);
      }
      else
      {
         totalSize = size();         
      }
      
      for (Iterator i = getSubWorklists(); i.hasNext();)
      {
         Worklist worklist = (Worklist) i.next();
         totalSize += worklist.getCumulatedSize(visited);
      }

      return totalSize;
   }
      
   /**
    * Retrieves a cumulated view of items belonging directly and indirectly to this
    * worklist.
    * <p />
    * As the cumulation takes place client-side any order criterion associated with the
    * query will not be applied to the cumulated view, thus resulting in a partially
    * ordered list. A globally ordered list may be obtained by an apropriate
    * {@link ActivityInstanceQuery}.
    * 
    * @return The items directly and indirectly belonging to this worklist, possibly
    *         constrained by its {@link SubsetPolicy}.
    *
    * @see #getCumulatedSize()
    */
   public List getCumulatedItems()
   {
      List result = Collections.unmodifiableList(items);

      for (Iterator i = getSubWorklists(); i.hasNext();)
      {
         result = new ConcatenatedList(result, (Worklist) i.next());
      }

      return result;
   }
   
   public long getTotalCountThreshold()
   {
      return totalCountThreshold;
   }
}
