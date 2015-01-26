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

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ProcessInstanceQueryEvaluator extends RuntimeInstanceQueryEvaluator
{
   public ProcessInstanceQueryEvaluator(ProcessInstanceQuery query, EvaluationContext context)
   {
      super(query, ProcessInstanceBean.class, context);
   }

   @Override
   public ParsedQuery parseQuery()
   {
      ParsedQuery parsedQuery = super.parseQuery();

      ParsedQueryProcessor queryProcessor = ExtensionProviderUtils.getFirstExtensionProvider(ParsedQueryProcessor.class);
      if (null != queryProcessor)
      {
         parsedQuery = queryProcessor.processQuery(parsedQuery);
      }

      return parsedQuery;
   }

   @SPI(status = Status.Experimental, useRestriction = UseRestriction.Internal)
   public static interface ParsedQueryProcessor
   {
      ParsedQuery processQuery(ParsedQuery query);
   }
}
