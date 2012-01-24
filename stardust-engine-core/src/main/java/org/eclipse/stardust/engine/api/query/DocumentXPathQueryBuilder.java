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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.thirdparty.encoding.ISO9075;

import com.sungard.infinity.bpm.vfs.MetaDataLocation;



public class DocumentXPathQueryBuilder
{
   private Query query;

   private DocumentXPathQueryFilterEvaluator filterVisitor;

   private DocumentXPathQueryOrderEvaluator orderVisitor;

   public DocumentXPathQueryBuilder(Query query, EvaluationContext context,
         MetaDataLocation metaDataLocation)
   {
      this.query = query;
      this.filterVisitor = new DocumentXPathQueryFilterEvaluator(metaDataLocation);
      this.orderVisitor = new DocumentXPathQueryOrderEvaluator(metaDataLocation);
   }

   public String build(String partitionPrefixPath)
   {
      String filterTerm = filterVisitor.buildFilterTerm(query);
      String orderByTerm = orderVisitor.buildOrderTerm(query);
      String restrictionPath = "/";

      if ( !StringUtils.isEmpty(partitionPrefixPath))
      {
         restrictionPath = partitionPrefixPath + "/";
      }
      return "/jcr:root" + ISO9075.encodePath(restrictionPath) + "/element(*, nt:file)" + filterTerm
            + orderByTerm;
   }

}
