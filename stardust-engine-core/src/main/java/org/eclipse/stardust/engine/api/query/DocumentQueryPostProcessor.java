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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;



public class DocumentQueryPostProcessor
{

   public static Documents findMatchingDocuments(DocumentQuery query,
         ResultIterator queryResult)
   {
      List<Document> results = new ArrayList<Document>();

      while (queryResult.hasNext())
      {
         results.add((Document) queryResult.next());
      }

      RawQueryResult<Document> rawQueryResult = new RawQueryResult(results,
            QueryUtils.getSubset(query), queryResult.hasMore(),
            queryResult.hasTotalCount() //
                  ? new Long(queryResult.getTotalCount())
                  : null);

      return new Documents(query, rawQueryResult);
   }

}
