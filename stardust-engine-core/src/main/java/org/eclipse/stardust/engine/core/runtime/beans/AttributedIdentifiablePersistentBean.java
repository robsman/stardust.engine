/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.MultiAttribute;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


// @todo (france, ub): optimize

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AttributedIdentifiablePersistentBean
      extends IdentifiablePersistentBean
      implements AttributedIdentifiablePersistent
{
   private static final String[] EMPTY = new String[0];
   
   /**
    * Holds a cached version of all properties
    */
   private transient Map cachedProperties = null;

   protected Map getAllPropertiesFromSessionCache(Session session)
   {
      Map result = null;

      // if we find any property beans in the session cache, it is safe to assume we
      // have all as the property owner was not loaded from disk itself
      Collection cachedParts = session.getCache(getPropertyImplementationClass());
      if ( !isEmpty(cachedParts))
      {
         for (Iterator i = cachedParts.iterator(); i.hasNext();)
         {
            PersistenceController pc = (PersistenceController) i.next();
            
            if ( !((DefaultPersistenceController) pc).isDeleted())
            {
               AbstractProperty cachedProperty = (AbstractProperty) pc.getPersistent();
               if (cachedProperty.getObjectOID() == getOID())
               {
                  if (null == result)
                  {
                     // allocate properties map as lazy as possible, as for the majority
                     // of cases there will be no properties at all
                     result = newHashMap();
                     
                     // add transient attribute containers for supported multi-attributes.
                     for (String attributeName : supportedMultiAttributes())
                     {
                        result.put(attributeName, new MultiAttribute(attributeName));
                     }
                  }
                  
                  Object existingProperty = result.get(cachedProperty.getName());
                  if (existingProperty instanceof MultiAttribute)
                  {
                     MultiAttribute container = (MultiAttribute) existingProperty;
                     container.add(cachedProperty);
                  }
                  else
                  {
                     result.put(cachedProperty.getName(), cachedProperty);
                  }
               }
            }
         }
      }
      
      return result;
   }

   /**
    * This method returns a life copy from the audit trail of all properties
    * for this attributed bean
    */
   protected Map getAllPropertiesFromAuditTrail()
   {
      Iterator i = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            getPropertyImplementationClass(),
            QueryExtension.where(Predicates.isEqual(TypeDescriptor.get(
                  getPropertyImplementationClass()).fieldRef(
                  AbstractProperty.FIELD__OBJECT_OID), getOID())));
      Map result = new HashMap();
      
      // add transient attribute containers for supported multi attributes.
      for (int idx = 0; idx < supportedMultiAttributes().length; idx++)
      {
         String attributeName = supportedMultiAttributes()[idx];
         result.put(attributeName, new MultiAttribute(attributeName));
      }
      
      for (; i.hasNext();)
      {
         AbstractProperty property = (AbstractProperty) i.next();
         
         Object existingProperty = result.get(property.getName());
         if (existingProperty instanceof MultiAttribute)
         {
            MultiAttribute container = (MultiAttribute) existingProperty;
            container.add(property);
         }
         else
         {
            result.put(property.getName(), property);
         }
      }
      
      return result;
   }
   
   public Map getAllProperties()
   {
      if (null == cachedProperties)
      {
         if (getPersistenceController().isCreated()
               && (getPersistenceController().getSession() instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session))
         {
            cachedProperties = getAllPropertiesFromSessionCache((org.eclipse.stardust.engine.core.persistence.jdbc.Session) getPersistenceController().getSession());
         }
         else
         {
            cachedProperties = getAllPropertiesFromAuditTrail();
         }
      }
      
      return (null != cachedProperties) ? cachedProperties : emptyMap();
   }

   public Map getAllPropertyValues()
   {
      Map result = new HashMap();
      
      for (Iterator i = getAllProperties().entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         result.put(entry.getKey(), entry.getValue());
      }
      
      return result;
   }

   public void addPropertyValues(Map attributes)
   {
      Map existingProperties = getAllProperties();
      
      for (Iterator i = attributes.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         AbstractProperty existing = (AbstractProperty)
               existingProperties.get(entry.getKey());
         
         if (existing != null)
         {
            existing.setValue(entry.getValue());
         }
         else
         {
            AbstractProperty abstractProperty = createProperty((String) entry.getKey(),
                  (Serializable) entry.getValue());
            if (null == cachedProperties)
            {
               this.cachedProperties = newHashMap();
            }
            cachedProperties.put(abstractProperty.getName(), abstractProperty);
         }
      }
   }

   private Attribute getProperty(String name)
   {
      return (Attribute) getAllProperties().get(name);
   }

   public Serializable getPropertyValue(String name)
   {
      Attribute result = getProperty(name);

      if (result != null)
      {
         return (Serializable) result.getValue();
      }

      return null;
   }

   public void setPropertyValue(String name, Serializable value, boolean force)
   {
      Attribute property = getProperty(name);
      if (property == null)
      {
         if (null == cachedProperties)
         {
            this.cachedProperties = newHashMap();
         }
         
         property = createProperty(name, value);

         if (Arrays.asList(supportedMultiAttributes()).contains(name))
         {
            // properly wrap multi-attributes into container
            MultiAttribute container = new MultiAttribute(name);
            container.add(property);
            cachedProperties.put(name, container);
         }
         else
         {
            cachedProperties.put(name, property);
         }
      }
      else if (property instanceof MultiAttribute)
      {
         MultiAttribute container = (MultiAttribute) property;
         AbstractProperty abstractProperty = createProperty(name, value);
         container.add(abstractProperty);
      }
      else if (!CompareHelper.areEqual(property.getValue(), value) || force)
      {
         property.setValue(value);
      }
   }
   
   public void addProperty(AbstractProperty existingProperty)
   {
      String name = existingProperty.getName();
      Attribute property = getProperty(name);
      if (property == null)
      {
         if (null == cachedProperties)
         {
            this.cachedProperties = newHashMap();
         }
         
         property = existingProperty;

         if (Arrays.asList(supportedMultiAttributes()).contains(name))
         {
            // properly wrap multi-attributes into container
            MultiAttribute container = new MultiAttribute(name);
            container.add(property);
            cachedProperties.put(name, container);
         }
         else
         {
            cachedProperties.put(name, property);
         }
      }
      else if (property instanceof MultiAttribute)
      {
         MultiAttribute container = (MultiAttribute) property;
         container.add(existingProperty);
      }
   }   
   
   public void setPropertyValue(String name, Serializable value)
   {
      setPropertyValue(name, value, false);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent#removeProperty(java.lang.String)
    */
   public void removeProperty(String name)
   {
      Map/*<String, Attribute>*/properties = getAllProperties();
      Attribute attribute = (Attribute) properties.get(name);
      if (null != attribute)
      {
         if (attribute instanceof AbstractProperty)
         {
            AbstractProperty property = (AbstractProperty) attribute;
            property.delete();
         }
         else if (attribute instanceof MultiAttribute)
         {
            MultiAttribute container = (MultiAttribute) attribute;
            List list = (List) container.getValue();
            for (Iterator iterator = list.iterator(); iterator.hasNext();)
            {
               AbstractProperty property = (AbstractProperty) iterator.next();
               property.delete();
            }
            properties.remove(attribute.getName());
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent#removeProperty(java.lang.String, java.io.Serializable)
    */
   public void removeProperty(String name, Serializable value)
   {
      Map/*<String, Attribute>*/properties = getAllProperties();
      Attribute attribute = (Attribute) properties.get(name);
      if (null != attribute)
      {
         if (attribute instanceof AbstractProperty)
         {
            AbstractProperty property = (AbstractProperty) attribute;
            if (CompareHelper.areEqual(property.getValue(), value))
            {
               property.delete();
            }
         }
         else if (attribute instanceof MultiAttribute)
         {
            MultiAttribute container = (MultiAttribute) attribute;
            List list = (List) container.getValue();
            for (Iterator iterator = list.iterator(); iterator.hasNext();)
            {
               AbstractProperty property = (AbstractProperty) iterator.next();
               if (CompareHelper.areEqual(property.getValue(), value))
               {
                  property.delete();
                  iterator.remove();
               }
            }
         }
      }
   }
   
   public abstract AbstractProperty createProperty(String name, Serializable value);

   public abstract Class getPropertyImplementationClass();
   
   /**
    * @return the names of attributes/properties which are allowed to have more than one entry.
    */
   protected String[] supportedMultiAttributes()
   {
      return EMPTY;
   }
      
   protected static boolean propertyExists(Attribute properties)
   {
      boolean result = false;

      if (null != properties)
      {
         if (properties instanceof MultiAttribute)
         {
            MultiAttribute container = (MultiAttribute) properties;
            result = !((List) container.getValue()).isEmpty();
         }
         else
         {
            result = true;
         }
      }

      return result;
   }   
}