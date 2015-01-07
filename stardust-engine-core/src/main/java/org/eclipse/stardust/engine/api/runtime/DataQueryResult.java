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
package org.eclipse.stardust.engine.api.runtime;

import java.util.List;

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.query.AbstractQueryResult;
import org.eclipse.stardust.engine.api.query.DataQuery;


/**
 * Result of an {@link DataQuery} execution. Retrieved items are instances of
 * {@link Data}.
 *
 * @author roland.stamm
 *
 */
public class DataQueryResult extends AbstractQueryResult<Data>
{
   private static final long serialVersionUID = 1L;

   public DataQueryResult(DataQuery query, List<Data> result)
   {
      super(query, result, false, (long) result.size());
   }

   public DataQuery getQuery()
   {
      return (DataQuery) query;
   }

}
