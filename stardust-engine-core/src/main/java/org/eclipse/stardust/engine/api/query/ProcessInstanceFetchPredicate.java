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

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ProcessInstanceFetchPredicate implements FetchPredicate
{
   private static final Logger trace = LogManager.getLogger(ProcessInstanceFetchPredicate.class);

   private static final FieldRef[] REFERENCED_PI_FIELDS = new FieldRef[] {ProcessInstanceBean.FR__OID};

   private final Set processOIDs;

   private int oidColumnIndex = -1;
   private int oidColumnIndexImplicit = 0;
   private String oidColumnName = null;
   private String oidColumnNameQualified = null;

   public ProcessInstanceFetchPredicate(Set processOIDs)
   {
      this.processOIDs = processOIDs;

      TypeDescriptor typeDesc = TypeDescriptor.get(ProcessInstanceBean.class);
      Field[] pkFields = typeDesc.getPkFields();
      
      Assert.condition(1 == pkFields.length, "Expecting one PK field for type "
            + ProcessInstanceBean.class + ".");
      
      oidColumnName = pkFields[0].getName();
      // fallback option for the case that the field name is qualified. 
      FieldRef fr = typeDesc.fieldRef(pkFields[0].getName());
      oidColumnNameQualified = fr.toString();
      
      oidColumnIndexImplicit = 1 + typeDesc.getFieldColumnIndex(pkFields[0]);
   }

   public FieldRef[] getReferencedFields()
   {
      return REFERENCED_PI_FIELDS;
   }

   public boolean accept(Object o)
   {
      if (o instanceof ResultSet)
      {
         ResultSet result = (ResultSet) o;
         try
         {
            if (oidColumnIndex < 0)
            {
               try
               {
                  oidColumnIndex = result.findColumn(oidColumnName);
               }
               catch (Exception ex)
               {
                  try
                  {
                     oidColumnIndex = result.findColumn(oidColumnNameQualified);
                  }
                  catch (Exception ex2)
                  {
                     oidColumnIndex = oidColumnIndexImplicit;
                  }
               }
            }
            return processOIDs.contains(new Long(result.getLong(oidColumnIndex)));
         }
         catch (Exception e)
         {
            trace.warn("", e);
            return false;
         }
      }
      else if (o instanceof IProcessInstance)
      {
         return processOIDs.contains(new Long(((IProcessInstance) o).getOID()));
      }
      else
      {
         return false;
      }
   }
}
