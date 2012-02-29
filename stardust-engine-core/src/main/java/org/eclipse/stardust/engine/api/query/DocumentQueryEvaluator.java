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

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsVfsConversionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IQueryResult;
import org.eclipse.stardust.vfs.MetaDataLocation;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;



public class DocumentQueryEvaluator implements QueryEvaluator
{
   private static final Logger trace = LogManager.getLogger(DocumentQueryEvaluator.class);

   private EvaluationContext context;

   private DocumentQuery query;

   private IDocumentRepositoryService vfs;

   public DocumentQueryEvaluator(DocumentQuery query, EvaluationContext context,
         IDocumentRepositoryService vfs)
   {
      this.query = query;
      this.context = context;
      this.vfs = vfs;
   }

   public long executeCount()
   {
      SubsetPolicy subset = QueryUtils.getSubset(query);

      return doQuery(query, context, subset, vfs).getResult().size();
   }

   public ResultIterator executeFetch()
   {
      SubsetPolicy subset = QueryUtils.getSubset(query);

      IQueryResult result = doQuery(query, context, subset, vfs);

      List<Document> documents = DmsVfsConversionUtils.fromVfsDocumentList(
            result.getResult(), getPartitionPrefix());

      return new DocumentResultIterator(documents, subset.getSkippedEntries(),
            subset.getMaxSize(), result.getTotalSize());
   }

   private IQueryResult doQuery(DocumentQuery query, EvaluationContext context,
         SubsetPolicy subset, IDocumentRepositoryService vfs)
   {
      long startTime = System.currentTimeMillis();
      String xPathQuery = new DocumentXPathQueryBuilder(query, context,
            MetaDataLocation.LOCAL).build(getPartitionPrefix());

      long limit = subset.getMaxSize();
      long offset = subset.getSkippedEntries();

      IQueryResult result = null;
      try
      {
         result = vfs.findFiles(xPathQuery, limit, offset);
      }
      catch (RepositoryOperationFailedException e)
      {
         throw e;
      }

      if (trace.isDebugEnabled())
      {
         long time = System.currentTimeMillis() - startTime;
         trace.info((result == null ? "0" : result.getResult().size())
               + " Document(s) found in " + time + "ms with query (" + xPathQuery
               + ") limit=" + limit + " offset=" + offset);
      }
      return result;
   }

   private String getPartitionPrefix()
   {
      return DocumentRepositoryFolderNames.REPOSITORY_ROOT_FOLDER
            + DocumentRepositoryFolderNames.PARTITIONS_FOLDER
            + SecurityProperties.getPartition().getId();
   }

}
