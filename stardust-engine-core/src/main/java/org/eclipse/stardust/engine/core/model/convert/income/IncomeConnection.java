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

public class IncomeConnection extends IncomeElement
{
   public static final String LOAD_QUERY = "select c.con_id, c.con_act_id, c.con_ost_id, c.con_type, c.con_sim_color from inc4_connections c";

   public final static String TYPE_FIELD = "CON_TYPE";

   public final static String ACTIVITY_ID_FIELD = "CON_ACT_ID";

   public final static String OBJECTSTORE_ID_FIELD = "CON_OST_ID";
   
   public final static String OBJECTTYPE_COLOR_FIELD = "CON_SIM_COLOR";

   public static final String IN_CONNECTION_TYPE = "IN";

   public static final String OUT_CONNECTION_TYPE = "OUT";

   private IncomeActivity activity;

   private IncomeObjectstore objectstore;

   private String type;

   private String condition;
   
   public IncomeConnection(String id, String name, String description, String type)
   {
      super(id, name, description);
      this.type = type;
   }

   public IncomeActivity getActivity()
   {
      return activity;
   }

   public void setActivity(IncomeActivity activity)
   {
      this.activity = activity;
      this.activity.addConnection(this);
   }

   public IncomeObjectstore getObjectstore()
   {
      return objectstore;
   }

   public void setObjectstore(IncomeObjectstore objectstore)
   {
      this.objectstore = objectstore;
      this.objectstore.addConnection(this);
   }

   public Object create(IProcessDefinition processDefinition)
   {
      return null;
   }

   public String getType()
   {
      return type;
   }

   public String getCondition()
   {
      return condition;
   }

   public void setCondition(String condition)
   {
      this.condition = condition;
   }

}
