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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;

import org.eclipse.stardust.common.AttributeHolderImpl;
import org.eclipse.stardust.common.IAttributeManager;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
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

   private IAttributeManager runtimeAttributes;

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
    * Mark the bean as modified. If the bean is an instance of ModelPart and
    * instance of ModelElement the method fireMofelElementChanged in the model
    * is called to propagate the changes.
    *
    * @see org.eclipse.stardust.engine.core.model.utils.ModelElement
    * @see RootElement#fireModelElementChanged
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

      RootElement modelCopy = getModel();

      /*for (Iterator i = new HashSet(references).iterator(); i.hasNext();)
      {
         Hook r = (Hook) i.next();
         r.remove(this);
      }*/

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

   public void removeReference(Hook reference)
   {
      //references.remove(reference);
   }

   public void addReference(Hook reference)
   {
      //markModified();
      //references.add(reference);
   }

   public ModelElement deepCopyI(ModelElement newParent, boolean keepOIDs,
         Collector collector)
   {
      ModelElementBean result;
      try
      {
         Constructor ctor = getClass().getDeclaredConstructor(new Class[]{});
         ctor.setAccessible(true);
         result = (ModelElementBean) ctor.newInstance(new Object[]{});
         result.parent = newParent;
         if (keepOIDs)
         {
            int oid = getElementOID();
            if (oid == 0 )
            {
               if (! (this instanceof RootElement))
               {
                  trace.warn("Found an Identifiable with oid 0;" + this);
               }
            }
            else
            {
               result.register(oid);
            }
         }
         else
         {
            result.register(0);
         }
         Collection fields = Reflect.getFields(getClass());
         for (Iterator i = fields.iterator(); i.hasNext();)
         {
            Field field = (Field) i.next();
            if (Modifier.isTransient(field.getModifiers()))
            {
               continue;
            }
            if (Link.class.isAssignableFrom(field.getType()))
            {
               Link source = (Link) field.get(this);
               Link target = (Link) field.get(result);
               for (Iterator j = source.iterator(); j.hasNext();)
               {
                  ModelElementBean child = (ModelElementBean) j.next();
                  if (Connections.class.isAssignableFrom(field.getType()))
                  {
                     ((Connections) target).__add__(
                           child.deepCopyI(result, keepOIDs, collector));
                  }
                  else
                  {
                     target.add(child.deepCopyI(result, keepOIDs, collector));
                  }
               }
               continue;
            }
            if (Hook.class.isAssignableFrom(field.getType())
                  || ModelElement.class.isAssignableFrom(field.getType())
                  || Map.class.isAssignableFrom(field.getType())
                  || Collection.class.isAssignableFrom(field.getType())
                  || "elementOID".equals(field.getName()))
            {
               continue;
            }
            field.set(result, field.get(this));
         }
         result.setAllAttributes(getAllAttributes());
      }
      catch (Exception e)
      {
         throw new InternalException(toString() + ": ", e);
      }
      if (collector != null)
      {
         collector.collect(this, result);
      }
      return result;
   }

   public void deepCopyII(ModelElement source, Collector collector)
   {
      try
      {
         Collection fields = Reflect.getFields(getClass());
         for (Iterator i = fields.iterator(); i.hasNext();)
         {
            Field field = (Field) i.next();
            if (Modifier.isTransient(field.getModifiers()))
            {
               continue;
            }
            if (Link.class.isAssignableFrom(field.getType()))
            {
               Link sourceLink = (Link) field.get(source);
               Link targetLink = (Link) field.get(this);
               Iterator k = sourceLink.iterator();
               for (Iterator j = targetLink.iterator(); j.hasNext();)
               {
                  ModelElementBean sourceChild = (ModelElementBean) k.next();
                  ModelElementBean targetChild = (ModelElementBean) j.next();
                  if (Connections.class.isAssignableFrom(field.getType()))
                  {
                     ModelElement sourceFirst = ((Connection) sourceChild).getFirst();
                     if (sourceFirst != null)
                     {
                        ((Connection) targetChild).setFirst(collector.findInTarget(sourceFirst));
                     }
                     ModelElement sourceSecond = ((Connection) sourceChild).getSecond();
                     if (sourceSecond != null)
                     {
                        ((Connection) targetChild).setSecond(collector.findInTarget(sourceSecond));
                     }
                  }
                  targetChild.deepCopyII(sourceChild, collector);
               }
               continue;
            }
            if (Reference.class.isAssignableFrom(field.getType()))
            {
               Reference reference = (Reference) field.get(this);
               Reference sourceRef = (Reference) field.get(source);
               for (Iterator j = sourceRef.iterator(); j.hasNext();)
               {
                  ModelElement sourceElement = (ModelElement) j.next();
                  ModelElement element = collector.findInTarget(sourceElement);
                  if (element != null)
                  {
                     reference.add(element);
                  }
                  else
                  {
                     trace.debug("Couldn't set reference " + field.getName()
                           + ". No Element with oid " + sourceElement.getElementOID());
                  }
               }
               continue;
            }
            // @todo (france, ub): ??
            /*            if (SingleRef.class.isAssignableFrom(field.getType()))
                        {
                           SingleRef reference = ((SingleRef) field.get(this));
                           SingleRef sourceRef = ((SingleRef) field.get(source));
                           ModelElement sourceElement = (ModelElement) sourceRef.getElement();
                           if (sourceElement != null)
                           {
                              ModelElement element = collector.findInTarget(sourceElement);
                              if (element != null)
                              {
                                 reference.setElement(element);
                              }
                              else
                              {
                                 trace.debug("Couldn't set reference " + field.getName()
                                       + ". No Element with oid " + sourceElement.getElementOID());
                              }
                           }

                        }
                        */
         }
      }
      catch (Exception e)
      {
         trace.info(toString());
         throw new InternalException(e);
      }
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
