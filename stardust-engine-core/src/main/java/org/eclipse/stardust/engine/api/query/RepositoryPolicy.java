/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.Collections;
import java.util.List;

/**
 * By adding this policy multiple repositories can be target of a query.
 * The results from the different repositories are merged together.
 *
 * @author roland.stamm
 *
 */
public class RepositoryPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = -4922399462085360884L;

   private List<String> repositoryIds;

   public RepositoryPolicy()
   {
      repositoryIds = Collections.EMPTY_LIST;
   }

   /**
    * This creates a RepositoryPolicy that includes all available repositories into the search.
    *
    * @return The configured RepositoryPolicy.
    */
   public static RepositoryPolicy includeAllRepositories()
   {
      return new RepositoryPolicy(Collections.EMPTY_LIST);
   }

   /**
    * This creates a RepositoryPolicy that includes all selected repositories into the search.
    *
    * @param repositoryIds The repositorId of all repositories that should be included.
    * @return The configured RepositoryPolicy.
    */
   public static RepositoryPolicy includeRepositories(List<String> repositoryIds)
   {
      return new RepositoryPolicy(repositoryIds);
   }

   public RepositoryPolicy(List<String> repositoryIds)
   {
      this.repositoryIds = repositoryIds;
   }

   public List<String> getRepositoryIds()
   {
      return repositoryIds;
   }

}
