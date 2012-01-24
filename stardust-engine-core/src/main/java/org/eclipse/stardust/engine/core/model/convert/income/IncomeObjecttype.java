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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.IProcessDefinition;


public class IncomeObjecttype extends IncomeElement
{
   public static final String LOAD_QUERY = "select o.oty_id, ot.otyt_short_name, ot.otyt_description from inc4_objecttypes o, inc4_objecttype_texts ot where o.oty_pro_id = ? and o.oty_id = ot.otyt_oty_id";

   public final static String ID_FIELD = "OTY_ID";

   public final static String NAME_FIELD = "OTYT_SHORT_NAME";

   public final static String DESCRIPTION_FIELD = "OTYT_DESCRIPTION";
   
   private Set attributes;
   
   public IncomeObjecttype(String id, String name, String description)
   {
      super(id, name, description);
      this.attributes = new HashSet();
   }

   public Object create(IProcessDefinition processDefinition)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Set getAttributes()
   {
      return attributes;
   }
   
   public class IncomeAttribute extends IncomeElement
   {
      public static final String LOAD_QUERY = "select distinct ot.otdt_otd_id, ot.otdt_short_name, ot.otdt_description, od.otd_format from inc4_objecttype_details od, inc4_ot_detail_texts ot  where od.otd_oty_par_id = ? and od.otd_oty_par_id = ot.otdt_oty_id";

      public final static String ID_FIELD = "OTDT_OTD_ID";

      public final static String NAME_FIELD = "OTDT_SHORT_NAME";

      public final static String DESCRIPTION_FIELD = "OTDT_DESCRIPTION";
      
      public final static String DATA_TYPE_FIELD = "OTD_FORMAT";

      
      private Class dataType;
      
      public IncomeAttribute(String id, String name, String description, int dataType)
      {
         super(id, name, description);
         
         switch(dataType)
         {
            case 0 : this.dataType = String.class; break;
            case 3 : this.dataType = Integer.class; break;
         }
         
         attributes.add(this);
         
      }
      
      public Object create(IProcessDefinition processDefinition)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Class getDataType()
      {
         return dataType;
      }
   }

}
