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

import java.io.Serializable;
import java.util.List;

/**
 * Result of a {@link Query} execution. Holds retrieved items, any applying
 * {@link SubsetPolicy} and a flag if more items would be available beyond that subset.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see Query
 * @see SubsetPolicy
 */
public interface QueryResult<T> extends  List<T>, Serializable
{
   /**
    * Gets the optionally evaluated count of all items satisfying the query this result is
    * based on.
    * <p />
    * As evaluating the total count is probably an expensive operation, it will only be
    * done on request. Requesting the total count has to be done by attaching an
    * appropriate {@link SubsetPolicy}.
    * <p />
    * If no subset policy is used, the total count of items is equal the size of the query
    * result. 
    * 
    * @return The total count of items satisfying the query, if requested by the subset
    *       policy used.
    * 
    * @throws UnsupportedOperationException if the evaluation of the total count was not
    *       requested
    *       
    * @see SubsetPolicy#isEvaluatingTotalCount()
    */
   public long getTotalCount() throws UnsupportedOperationException;

   /**
    * Retrieves the subset policy this result set is based on.
    *
    * @return The {@link SubsetPolicy} the result set is based on.
    */
   public SubsetPolicy getSubsetPolicy();

   /**
    * Indicates if there are more items available for this result set than allowed by its
    * {@link SubsetPolicy}.
    *
    * @return <code>true</code> if there are more items available, <code>false</code> if
    *         not.
    *
    * @see #getSubsetPolicy
    */
   public boolean hasMore();
}
