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
package org.eclipse.stardust.engine.core.model.convert.income;

import org.eclipse.stardust.engine.api.model.IProcessDefinition;

public class IncomeDocument extends IncomeElement
{
   public static final String LOAD_QUERY = "select doc_id, doc_title, doc_comment, doc_reference from inc4_documents where doc_element_type = 0 and doc_id = ?";

   public final static String ID_FIELD = "DOC_ID";
   
   public final static String NAME_FIELD = "DOC_TITLE";

   public final static String DESCRIPTION_FIELD = "DOC_COMMENT";
   
   public final static String REFERENCE_FIELD = "DOC_REFERENCE";
   
   private String reference;
   
   public IncomeDocument(String id, String name, String description, String reference)
   {
      super(id, name, description);
      this.reference = reference;
   }

   public Object create(IProcessDefinition processDefinition)
   {
      return null;
   }

   public String getReference()
   {
      return reference;
   }

   public void setReference(String reference)
   {
      this.reference = reference;
   }

}
