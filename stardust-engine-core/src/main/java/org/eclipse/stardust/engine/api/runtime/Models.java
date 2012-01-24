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

import org.eclipse.stardust.engine.api.query.AbstractQueryResult;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.Query;


/**
 * Result of an {@link DeployedModelQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.DeployedModelDescription}.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class Models extends AbstractQueryResult<DeployedModelDescription>
{  
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new Models object.
    * 
    * @param query the query performed.
    * @param result the result of the query.
    */
   public Models(Query query, List<DeployedModelDescription> result)
   {
      super(query, result, false, (long) result.size());
   }

   /**
    * Gets the query this result is based on.
    * 
    * @return The DeployedModelQuery this result is based on.
    */
   public DeployedModelQuery getQuery()
   {
      return (DeployedModelQuery) query;
   }
}
