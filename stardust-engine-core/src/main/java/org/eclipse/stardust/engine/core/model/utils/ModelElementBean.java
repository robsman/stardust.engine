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
package org.eclipse.stardust.engine.core.model.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;

import org.eclipse.stardust.common.AttributeHolderImpl;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ModelElementBean extends AttributeHolderImpl implements ModelElement
{
   private static final long serialVersionUID = -1652269951404186738L;

   private static final Logger trace = LogManager.getLogger(ModelElementBean.class);

   protected ModelElement parent;
   //private Set references = new HashSet();
   private int elementOID;
   protected boolean isTransient;

   private static final String DESCRIPTION_ATT = "Description";
   private String description;

   private static final String PREDEFINED_ATT = "Predefined";
   private boolean predefined = false;
   
   // property for property layer to define if model is revalidated after deployment
   public static final String PRP_REVALIDATE_ELEMENTS = "PRP_REVALIDATE_ELEMENTS";

   public static int nextID(String prefix, int current, String id)
   {
      if (id != null && id.startsWith(prefix))
      {
         try
         {
            int candidate = Integer.parseInt(id.substring(prefix.length()));
            if (candidate >= current)
            {
               return candidate + 1;
            }
            else
            {
               return current;
            }
         }
         catch (NumberFormatException e)
         {
            return current;
         }
      }
      else
      {
         return current;
      }
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      markModified();
      this.description = description;
   }

   public boolean isPredefined()
   {
      return predefined;
   }

   public void setPredefined(boolean predefined)
   {
      markModified();
      this.predefined = predefined;
   }

   public String getUniqueId()
   {
      return Integer.toString(getElementOID());
   }

   public int getElementOID()
   {
      return elementOID;
   }

   public long getOID()
   {
      return ((long) getModel().getModelOID() << 32) + elementOID;
   }

   public void setElementOID(int elementOID)
   {
      markModified();
      this.elementOID = elementOID;
   }

   public void setTransient(boolean aTransient)
   {
      markModified();
      isTransient = aTransient;
   }

   public boolean isTransient()
   {
      return isTransient;
   }

   /**
    * Mark the bean as modified.
    */
   public void markModified()
   {
   }

   public void register(int oid)
   {
      if (isTransient)
      {
         elementOID = getModel().createTransientElementOID();
      }
      else if (oid == 0)
      {
         elementOID = getModel().createElementOID();
         getModel().register(this);
      }
      else
      {
         elementOID = oid;
         getModel().register(this);
      }
   }


   public void delete()
   {
      trace.debug("Deleting " + this);

      if (getModel() != null)
      {
         getModel().deregister(this);
      }

      Collection fields = Reflect.getFields(getClass());
      for (Iterator i = fields.iterator(); i.hasNext();)
      {
         Field field = (Field) i.next();
         if (Link.class.isAssignableFrom(field.getType()))
         {
            field.setAccessible(true);
            try
            {
               Link link = (Link) field.get(this);
               trace.debug("Propagating deletion to  reference " + field.getName());
               for (Iterator j = link.iterator(); j.hasNext();)
               {
                  ((ModelElement) j.next()).delete();
               }
            }
            catch (Exception e)
            {
               trace.warn("", e);
            }

         }
         else if (Collection.class.isAssignableFrom(field.getType()))
         {
            try
            {
               Collection end = (Collection) field.get(this);
               for (Iterator j = end.iterator(); j.hasNext();)
               {
                  ((ModelElement) j.next()).delete();
               }
            }
            catch (Exception e)
            {
               trace.warn("", e);
            }
         }
      }

      parent = null;
   }

   public ModelElement getParent()
   {
      return parent;
   }

   public void setParent(ModelElement parent)
   {
      markModified();
      this.parent = parent;
   }

   public boolean equals(Object obj)
   {
      boolean isEqual = (this == obj);

      if (!isEqual && (obj instanceof ModelElement))
      {
         ModelElement modelElement = (ModelElement) obj;
         if (null != getModel() && (null != modelElement.getModel()))
         {
            isEqual = (modelElement.getElementOID() == getElementOID())
                  && (modelElement.getModel().getModelOID() == getModel().getModelOID());
         }
      }
      return isEqual;
   }

   public RootElement getModel()
   {
      if (parent == null)
      {
         return null;
      }
      else
      {
         return parent.getModel();
      }
   }
   
   protected void checkConsistency(List<Inconsistency> inconsistencies)
   {
      checkForVariables(inconsistencies, this.description, "Description");
   }

   protected void checkForVariables(List<Inconsistency> inconsistencies, String text, String description)
   {
      if (StringUtils.isNotEmpty(text))
      {
         Matcher matcher = ConfigurationVariableUtils
               .getConfigurationVariablesMatcher(text);
         if (matcher.find())
         {
//            inconsistencies.add(new Inconsistency(description
//                  + " is not allowed to contain variables: " + text, this,
//                  Inconsistency.ERROR));
         }
      }
   }
}
