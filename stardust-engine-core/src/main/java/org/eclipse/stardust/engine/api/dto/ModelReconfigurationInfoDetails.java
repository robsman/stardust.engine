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
package org.eclipse.stardust.engine.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;


/**
 * @author stephan.born
 * @version $Revision: 43208 $
 */
public class ModelReconfigurationInfoDetails implements ModelReconfigurationInfo
{
   private static final long serialVersionUID = 1L;
   
   private int modelOID;
   private String id;
   private List<Inconsistency> errors = new ArrayList<Inconsistency>();
   private List<Inconsistency> warnings = new ArrayList<Inconsistency>();
   private boolean success;

   public ModelReconfigurationInfoDetails(String id)
   {
      super();
      
      this.id = id;
   }

   public ModelReconfigurationInfoDetails(IModel model)
   {
      super();
      
      this.modelOID = model.getModelOID();
      this.id = model.getId();
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public String getId()
   {
      return id;
   }

   public List<Inconsistency> getWarnings()
   {
      return Collections.unmodifiableList(warnings);
   }

   public List<Inconsistency> getErrors()
   {
      return Collections.unmodifiableList(errors);
   }

   public boolean isValid()
   {
      return errors.isEmpty() && warnings.isEmpty();
   }

   public boolean hasWarnings()
   {
      return warnings.size() > 0;
   }

   public boolean hasErrors()
   {
      return errors.size() > 0;
   }

   public boolean success()
   {
      return success;
   }

   public void addInconsistency(Inconsistency inconsistency)
   {
      if (inconsistency.getSeverity() == Inconsistency.ERROR)
      {
         errors.add(inconsistency);
      }
      else
      {
         warnings.add(inconsistency);
      }
   }

   public void addInconsistencies(List<Inconsistency> inconsistencies)
   {
      for (Inconsistency inconsistency : inconsistencies)
      {
         addInconsistency(inconsistency);
      }
   }
   
   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }
   
   public void setSuccess(boolean success)
   {
      this.success = success;
   }
}
