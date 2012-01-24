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

public class IncomeRole extends IncomeElement
{
   public static final String LOAD_QUERY = "select r.rol_id, rt.rolt_short_name, rt.rolt_description from inc4_roles r, inc4_role_texts rt where r.rol_pro_id = ? and r.rol_id = rt.rolt_rol_id";

   public final static String ID_FIELD = "ROL_ID";

   public final static String NAME_FIELD = "ROLT_SHORT_NAME";

   public final static String DESCRIPTION_FIELD = "ROLT_DESCRIPTION";
   
   public IncomeRole(String id, String name, String description)
   {
      super(id, name, description);
   }

   public Object create(IProcessDefinition processDefinition)
   {
      return null;
   }

}
