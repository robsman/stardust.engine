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

import org.eclipse.stardust.common.error.PublicException;

/**
 * Evaluation Policy for specifying retrieval of only a subset of found data.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see Query#setPolicy
 */
public final class SubsetPolicy implements EvaluationPolicy
{
   /**
    * Predefined policy indicating an unrestricted subset. Implicitly includes evaluation
    * of the total item count, as all available items are fetched.
    */
   public static final SubsetPolicy UNRESTRICTED = new SubsetPolicy(Integer.MAX_VALUE,
         true);

   /**
    * Predefined policy indicating an counting only subset. No items are fetched but 
    * total count is evaluated.
    */
   public static final SubsetPolicy COUNT_ONLY = new SubsetPolicy(0, true);
   
   /**
    * Creates a new subset policy for retrieving the chunk of data following the one
    * specified by the given <code>subset</code>.
    *
    * <p>This chunk is defined as follows:
    * <ul>
    *    <li><code>maxSize = subset.maxSize</code></li>
    *    <li><code>skippedEntries = subset.skippedEntries + subset.maxSize</code></li>
    *    <li><code>isEvaluatingTotalCount = subset.isEvaluatingTotalCount</code></li>
    * </ul>
    * </p>
    *
    * @param subset The subset specification to find the next chunk for.
    * @return The requested subset policy, or <code>null</code> if the given
    *         <code>subset</code> was <code>null</code>.
    *
    * @see #previousChunk
    */
   public static final SubsetPolicy nextChunk(SubsetPolicy subset)
   {
      SubsetPolicy result = null;

      if (null != subset)
      {
         result = new SubsetPolicy(subset.getMaxSize(),
               (int) Math.min(
                     Integer.MAX_VALUE,
                     ((long) subset.getSkippedEntries() + (long) subset.getMaxSize())),
               subset.isEvaluatingTotalCount());
      }

      return result;
   }

   /**
    * Creates a new subset policy for retrieving the chunk of data before the one
    * specified by the given <code>subset</code>.
    *
    * <p>This chunk is defined as follows:
    * <ul>
    *    <li><code>maxSize = subset.maxSize</code></li>
    *    <li><code>skippedEntries = max(0, subset.skippedEntries - subset.maxSize)</code></li>
    *    <li><code>isEvaluatingTotalCount = subset.isEvaluatingTotalCount</code></li>
    * </ul>
    * </p>
    *
    * @param subset The subset specification to find the previous chunk for.
    * @return The requested subset policy, or <code>null</code> if the given
    *         <code>subset</code> was <code>null</code>.
    *
    * @see #nextChunk
    */
   public static final SubsetPolicy previousChunk(SubsetPolicy subset)
   {
      SubsetPolicy result = null;

      if (null != subset)
      {
         result = new SubsetPolicy(subset.getMaxSize(),
               Math.max(0, subset.getSkippedEntries() - subset.getMaxSize()),
               subset.isEvaluatingTotalCount());
      }

      return result;
   }

   private final int maxSize;
   private final int skippedEntries;

   private final boolean evaluatingTotalCount;

   /**
    * Same as {@link #SubsetPolicy(int, boolean)} with <code>evaluateTotalCount</code> set
    * to <code>false</code>.
    * 
    * @param maxSize The maximum number of items to be retrieved.
    */
   public SubsetPolicy(int maxSize)
   {
      this(maxSize, false);
   }

   /**
    * Same as {@link #SubsetPolicy(int, int, boolean)} with <code>skippedEntries</code>
    * set to <code>0</code>.
    * 
    * @param maxSize The maximum number of items to be retrieved.
    * @param evaluateTotalCount A flag indicating if the total number of items satisfying
    *       the query is to be evaluated or not.
    */
   public SubsetPolicy(int maxSize, boolean evaluateTotalCount)
   {
      this(maxSize, 0, evaluateTotalCount);
   }

   /**
    * Same as {@link #SubsetPolicy(int, int, boolean)} with
    * <code>evaluateTotalCount</code> set to <code>false</code>.
    * 
    * @param maxSize The maximum number of items to be retrieved.
    * @param skippedEntries The number of initial entries to be skipped before putting
    *       items to the query result. Must not be less than 0.
    *       
    * @throws PublicException if skippedEntries is less than 0
    */
   public SubsetPolicy(int maxSize, int skippedEntries)
   {
      this(maxSize, skippedEntries, false);
   }

   /**
    * Initializes a new subset policy with the explicit values given.
    *
    * @param maxSize The maximum number of items to be retrieved.
    * @param skippedEntries The number of initial entries to be skipped before putting
    *       items to the query result. Must not be less than 0.
    * @param evaluateTotalCount A flag indicating if the total number of items satisfying
    *       the query is to be evaluated or not.
    *       
    * @throws PublicException if skippedEntries is less than 0
    */
   public SubsetPolicy(int maxSize, int skippedEntries, boolean evaluateTotalCount)
   {
      this.maxSize = maxSize;
      if(skippedEntries < 0) 
      {
         throw new PublicException("The value of skippedEntries must not be less than 0.");
      }
      this.skippedEntries = skippedEntries;
      
      this.evaluatingTotalCount = evaluateTotalCount;
   }

   /**
    * Gets the maximum number of items to be included in a query result.
    *
    * @return The maximum item number
    */
   public final int getMaxSize()
   {
      return maxSize;
   }

   /**
    * Gets the number of initial items to be skipped before putting items to the query
    * result. Allows for paging of query results.
    *
    * @return The number of items to be skipped
    */
   public final int getSkippedEntries()
   {
      return skippedEntries;
   }

   /**
    * Indicates if the query result should additionally contain the total number of items
    * available, disregarding any result set size restriction. Allows for calculating
    * the total number of pages needed when using subsets for query result paging.
    *
    * @return <code>true</code> if the total number of items should be evaluated,
    *       <code>false</code> if not.
    */
   public boolean isEvaluatingTotalCount()
   {
      return evaluatingTotalCount;
   }
}
