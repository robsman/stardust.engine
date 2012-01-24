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

public class IncomeProject extends IncomeElement
{
   public static final String LOAD_QUERY = "select pt.prot_pro_id, pt.prot_short_name from inc4_project_texts_t pt";

   public final static String ID_FIELD = "PROT_PRO_ID";

   public final static String NAME_FIELD = "PROT_SHORT_NAME";
   
   public IncomeProject(String id, String name, String description)
   {
      super(id, name, description);
   }

   public Object create(IProcessDefinition processDefinition)
   {
      return null;
   }

}
