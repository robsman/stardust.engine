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

public class DocumentFilter implements FilterCriterion
{

   private static final long serialVersionUID = 987920689518801181L;

   private String documentId;

   private final String modelId;

   public DocumentFilter(String documentId, String modelId)
   {
      this.documentId = documentId;
      this.modelId = modelId;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   public String getDocumentId()
   {
      return documentId;
   }

   public String getModelId()
   {
      return modelId;
   }

}
