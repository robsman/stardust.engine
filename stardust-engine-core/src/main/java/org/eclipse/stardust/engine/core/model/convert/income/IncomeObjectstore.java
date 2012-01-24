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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.IProcessDefinition;


public class IncomeObjectstore extends IncomeElement
{
   public static final String LOAD_QUERY = "select o.ost_id, ott.ostt_short_name, ott.ostt_description, oa.ota_oty_id from inc4_objectstores o, inc4_objectstore_texts ott, inc4_objecttype_assigns oa where o.ost_dia_id = ? and o.ost_id = ott.ostt_ost_id and o.ost_id = oa.ota_ost_id (+)";

   public final static String ID_FIELD = "OST_ID";

   public final static String NAME_FIELD = "OSTT_SHORT_NAME";

   public final static String DESCRIPTION_FIELD = "OSTT_DESCRIPTION";
   
   public final static String OBJECTTYPE_FIELD = "OTA_OTY_ID";

   private Set inConnections;

   private Set outConnections;
   
   private Set objecttypes;

   public IncomeObjectstore(String id, String name, String description)
   {
      super(id, name, description);
      this.inConnections = new HashSet();
      this.outConnections = new HashSet();
      this.objecttypes = new HashSet();
   }

   public Set getInConnections()
   {
      return inConnections;
   }

   public Set getOutConnections()
   {
      return outConnections;
   }

   public void addConnection(IncomeConnection connection)
   {
      if (connection.getType().equals(IncomeConnection.IN_CONNECTION_TYPE))
      {
         this.outConnections.add(connection);
      }
      else if (connection.getType().equals(IncomeConnection.OUT_CONNECTION_TYPE))
      {
         this.inConnections.add(connection);
      }
      else
      {
         throw new RuntimeException("Unsupported connection type.");
      }
   }

   public Object create(IProcessDefinition processDefinition)
   {
      return null;
   }

   public void addObjecttype(IncomeObjecttype objecttype)
   {
      this.objecttypes.add(objecttype);
   }

   public Set getObjecttypes()
   {
      return objecttypes;
   }
   
   public boolean isProcessStartActivity()
   {
      return this.inConnections.isEmpty();
   }
   
   public boolean hasLeftConnectionWithActivity(String activityID)
   {
      for (Iterator _iterator = this.getInConnections().iterator(); _iterator.hasNext();)
      {
         IncomeConnection connection = (IncomeConnection) _iterator.next();
         if (connection.getActivity().getId().equals(activityID))
         {
            return true;
         }
      }
      return false;
   }
   
   public boolean hasRightConnectionWithActivity(String activityID)
   {
      for (Iterator _iterator = this.getOutConnections().iterator(); _iterator.hasNext();)
      {
         IncomeConnection connection = (IncomeConnection) _iterator.next();
         if (connection.getActivity().getId().equals(activityID))
         {
            return true;
         }
      }
      return false;
   }

}
