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
package org.eclipse.stardust.engine.core.spi.dms;

import static org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils.getCurrentUser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.QueryUtils;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.RepositoryPolicy;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Documents;

/**
 * Handles execution of {@link DocumentQuery} across multiple {@link IRepositoryInstance}.
 * Results are merged into {@link Documents}.
 * 
 * @author Roland.Stamm
 *
 */
public class FederatedSearchHandler
{

   private RepositoryProviderManager manager;

   public FederatedSearchHandler(RepositoryProviderManager manager)
   {
      this.manager = manager;
   }

   public Documents findDocuments(final DocumentQuery query)
   {
      Map<String, Documents> results = CollectionUtils.newTreeMap();

      RepositoryPolicy repositoryPolicy = (RepositoryPolicy) query.getPolicy(RepositoryPolicy.class);
      if (repositoryPolicy != null)
      {
         List<String> repositoryIds = repositoryPolicy.getRepositoryIds();
         List<IRepositoryInstance> involvedInstances = CollectionUtils.newArrayList();
         if (repositoryIds.isEmpty())
         {
            // Search in all repositories.
            List<IRepositoryInstanceInfo> allInstanceInfos = manager.getAllInstanceInfos();
            for (IRepositoryInstanceInfo instanceInfo : allInstanceInfos)
            {
               IRepositoryInstance instance = manager.getInstance(instanceInfo.getRepositoryId());
               involvedInstances.add(instance);
            }
         }
         else if (repositoryIds.size() == 1)
         {
            // Optimization: If only one repository is selected no special handling is required.
            IRepositoryInstance instance = manager.getInstance(repositoryIds.get(0));
            IRepositoryService service = instance.getService(getCurrentUser());
            return service.findDocuments(query);
         }
         else
         {
            // Search in selected repositories in order.
            for (String repositoryId : repositoryIds)
            {
               IRepositoryInstance instance = manager.getInstance(repositoryId);
               involvedInstances.add(instance);
            }
         }
         
         // Disable subset skipping. It has to be done in post processing because total count
         // might not be known per subquery.
         SubsetPolicy originalSubset = QueryUtils.getSubset(query);
         int targetMaxCount = getTargetMaxCount(originalSubset);
         DocumentQuery modifiedQuery = modifySubsetPolicy(query, originalSubset);
         
         Iterator<IRepositoryInstance> involvedInstancesIter = involvedInstances.iterator();
         boolean targetReached = false;
         while ( !targetReached && involvedInstancesIter.hasNext())
         {
            IRepositoryInstance instance = involvedInstancesIter.next();

            IRepositoryService service = instance.getService(getCurrentUser());
            try
            {
               Documents foundDocuments = service.findDocuments(modifiedQuery);

               results.put(instance.getRepositoryId(), foundDocuments);

               targetMaxCount -= foundDocuments.size();
            }
            catch (UnsupportedOperationException e)
            {
               // Ignore query.
            }

            if (targetMaxCount <= 0)
            {
               targetReached = true;
            }
         }
         // Restore original subset policy.
         query.setPolicy(originalSubset);
      }
      return mergeResults(results, query);

   }

   private DocumentQuery modifySubsetPolicy(DocumentQuery query, SubsetPolicy subset)
   {
      if (subset != null && subset.getSkippedEntries() > 0)
      {
         // To allow paging over multiple queries without knowing the total count, the
         // maxSize has to include all skipped entries and skipping is done in post
         // processing.
         int maxSize = subset.getMaxSize() + subset.getSkippedEntries();
         // Handle eventual Integer overflow.
         if (maxSize < 0)
         {
            maxSize = Integer.MAX_VALUE;
         }
        query.setPolicy(new SubsetPolicy(maxSize, 0, subset.isEvaluatingTotalCount()));
      }
      return query;
   }

   private int getSkipCount(SubsetPolicy subset)
   {
      if (subset != null)
      {
         return subset.getSkippedEntries() > 0 ? subset.getSkippedEntries() : 0;
      }
      return 0;
   }

   private int getTargetMaxCount(SubsetPolicy subset)
   {
      if (subset != null)
      {
         return subset.getMaxSize() > 0 ? subset.getMaxSize() : Integer.MAX_VALUE;
      }
      return Integer.MAX_VALUE;
   }

   private Documents mergeResults(Map<String, Documents> results,
         DocumentQuery originalQuery)
   {
      SubsetPolicy originalSubset = QueryUtils.getSubset(originalQuery);
      int skipCount = getSkipCount(originalSubset);
      int maxCount = getTargetMaxCount(originalSubset);
      boolean hasMore = false;
      Long totalCount = null;
      List<Document> allDocuments = CollectionUtils.newArrayList();
      for (Map.Entry<String, Documents> entry : results.entrySet())
      {
         Documents subQueryDocuments = entry.getValue();
         // hasMore is true if any of the sub-queries has more results.
         hasMore |= subQueryDocuments.hasMore();

         // Total count handling
         try
         {
            long subQueryTotalCount = subQueryDocuments.getTotalCount();
            totalCount = totalCount == null
                  ? totalCount = subQueryTotalCount
                  : totalCount + subQueryTotalCount;
         }
         catch (UnsupportedOperationException e)
         {
            // Do not add to total count
         }

         // Merge all found documents
         for (Document document : subQueryDocuments)
         {
            // Skip according to subset policy
            if (skipCount <= 0)
            {
               // Ensure maxSize of subset policy
               if (allDocuments.size() < maxCount)
               {
                  // Prefix corresponding repositoryId to document
                  Document prefixedDocument = RepositoryIdUtils.addRepositoryId(document,
                        entry.getKey());
                  allDocuments.add(prefixedDocument);
               }
            }
            else
            {
               skipCount-- ;
            }
         }
      }
      return new Documents(originalQuery, new RawQueryResult<Document>(allDocuments,
            originalSubset, hasMore, totalCount));
   }

}
