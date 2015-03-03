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
package org.eclipse.stardust.engine.api.query;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.BusinessObject;

/**
 * Result of an {@link BusinessObjectQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.BusinessObject}.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class BusinessObjects extends AbstractQueryResult<BusinessObject>
{
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new execution result.
    *
    * @param query the original query.
    * @param result the results of the query.
    */
   public BusinessObjects(Query query, List<BusinessObject> result)
   {
      super(query, result, false, (long) result.size());
   }

   /**
    * Retrieves the original query.
    *
    * @return the business object query used to obtain these results.
    */
   public BusinessObjectQuery getQuery()
   {
      return (BusinessObjectQuery) query;
   }
}