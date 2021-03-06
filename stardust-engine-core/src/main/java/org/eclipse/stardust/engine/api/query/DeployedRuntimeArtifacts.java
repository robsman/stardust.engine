/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;


/**
* Result of an {@link DeployedRuntimeArtifactQuery} execution. Retrieved items are instances of
* {@link DeployedRuntimeArtifact}.
*
*/
public class DeployedRuntimeArtifacts extends AbstractQueryResult<DeployedRuntimeArtifact>
{
   private static final long serialVersionUID = -4363538766320448954L;

   public DeployedRuntimeArtifacts(Query query, RawQueryResult<DeployedRuntimeArtifact> result)
   {
      super(query, result.getItemList(), result.hasMore(), result.hasTotalCount()
            ? result.getTotalCount()
            : null);
   }

   /**
    * Gets the query this result is based on.
    *
    * @return The query this result is based on.
    */
   public DeployedRuntimeArtifactQuery getQuery()
   {
      return (DeployedRuntimeArtifactQuery) query;
   }

}
