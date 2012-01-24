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
package org.eclipse.stardust.engine.extensions.ejb.ejb3.data;

import java.lang.reflect.Method;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.extensions.ejb.data.EntityBeanConstants;
import org.eclipse.stardust.engine.extensions.ejb.data.IEntityPKEvaluator;


/**
 * @author fherinean
 * @version $Revision$
 */
public class Entity30PKEvaluator implements IEntityPKEvaluator
{
   private static final String SET_PREFIX = "set";
   private static final String GET_PREFIX = "get";
   private static final String IS_PREFIX = "is";
   private static final Logger trace = LogManager.getLogger(Entity30PKEvaluator.class);

   @SuppressWarnings("unchecked")
   public Object getEntityBeanPK(Map attributes, Object value)
   {
      Object pk = null;

      String className = (String) attributes.get(PredefinedConstants.CLASS_NAME_ATT);
      Class targetClass = null;
      try
      {
         targetClass = Class.forName(className);
      }
      catch (Exception ex)
      {
         // ignore
      }
      if (value != null && targetClass != null && targetClass.isAssignableFrom(value.getClass()))
      {
         // we have the object itself.
         pk = getPrimaryKey(attributes, value);
         EntityManager em = getEntityManager(attributes);
         if (pk == null)
         {
            em.persist(value);
            pk = getPrimaryKey(attributes, value);
         }
         else
         {
            Object existing = findEntityByPK(em, attributes, pk);
            if (existing == null)
            {
               em.persist(value);
            }
            else
            {
               em.merge(value);
            }
         }
      }
      else
      {
         // assume the value is the PK itself
         pk = value;
      }
      
      return pk;
   }

   @SuppressWarnings("unchecked")
   private Object getPrimaryKey(Map attributes, Object value)
   {
      Object key = null;
      String pkClassName = (String) attributes.get(
            EntityBeanConstants.PRIMARY_KEY_ATT);
      String keyType = (String) attributes.get(
            EntityBeanConstants.PRIMARY_KEY_TYPE_ATT);
      String keyElements = (String) attributes.get(
            EntityBeanConstants.PRIMARY_KEY_ELEMENTS_ATT);
  
      if (EntityBeanConstants.ID_CLASS_PK.equals(keyType))
      {
         key = createIdClass(pkClassName, keyElements, value);
      }
      else if (EntityBeanConstants.EMBEDDED_ID_PK.equals(keyType))
      {
         key = getEmbeddedId(pkClassName, keyElements, value);
      }
      else if (EntityBeanConstants.ID_PK.equals(keyType))
      {
         key = getId(keyElements, value);
      }
      return key;
   }

   private Object getId(String keyElement, Object value)
   {
      return getValue(value, keyElement);
   }

   private Object getEmbeddedId(String pkClassName, String keyElement, Object value)
   {
      // TODO check in a real server-side example if that is true.
      return getValue(value, keyElement);
   }

   private Object createIdClass(String pkClassName, String keyElements, Object instance)
   {
      Object idClass = Reflect.createInstance(pkClassName);
      String[] entries = keyElements.split(",");
      for (int i = 0; i < entries.length; i++)
      {
         Object value = getValue(instance, entries[i]);
         setValue(idClass, entries[i], value);
      }
      return idClass;
   }

   private Object getValue(Object instance, String entry)
   {
      if (entry.startsWith(EntityBeanConstants.FIELD_PREFIX))
      {
         String fieldName = entry.substring(EntityBeanConstants.FIELD_PREFIX.length());
         return Reflect.getFieldValue(instance, fieldName);
      }
      if (entry.startsWith(EntityBeanConstants.PROPERTY_PREFIX))
      {
         String getterName = entry.substring(EntityBeanConstants.PROPERTY_PREFIX.length());
         try
         {
            Method method = instance.getClass().getMethod(getterName);
            return method.invoke(instance);
         }
         catch (Exception e)
         {
            throw new PublicException("Invalid Id access: " + entry, e);
         }
      }
      throw new PublicException("Invalid Id access: " + entry);
   }

   private void setValue(Object instance, String entry, Object value)
   {
      if (entry.startsWith(EntityBeanConstants.FIELD_PREFIX))
      {
         String fieldName = entry.substring(EntityBeanConstants.FIELD_PREFIX.length());
         Reflect.setFieldValue(instance, fieldName, value);
      }
      else if (entry.startsWith(EntityBeanConstants.PROPERTY_PREFIX))
      {
         String setterName = null;
         String getterName = entry.substring(EntityBeanConstants.PROPERTY_PREFIX.length());
         if (getterName.startsWith(IS_PREFIX))
         {
            setterName = SET_PREFIX + getterName.substring(IS_PREFIX.length());
         }
         else if (getterName.startsWith(GET_PREFIX))
         {
            setterName = SET_PREFIX + getterName.substring(GET_PREFIX.length());
         }
         else
         {
            setterName = SET_PREFIX + getterName;
         }
         try
         {
            Method getterMethod = instance.getClass().getMethod(getterName);
            Method setterMethod = instance.getClass().getMethod(setterName, getterMethod.getReturnType());
            setterMethod.invoke(instance, value);
         }
         catch (Exception e)
         {
            throw new PublicException("Invalid Id access: " + entry, e);
         }
      }
      else
      {
         throw new PublicException("Invalid Id access: " + entry);
      }
   }

   @SuppressWarnings("unchecked")
   public Object findEntityByPK(Map attributes, Object pk)
   {
      if (null == pk)
      {
         return null;
      }
      EntityManager em = getEntityManager(attributes);
      return findEntityByPK(em, attributes, pk);
   }

   @SuppressWarnings("unchecked")
   private Object findEntityByPK(EntityManager em, Map attributes, Object pk)
   {
      try
      {
         String className = (String) attributes.get(
               EntityBeanConstants.CLASS_NAME_ATT);
         Class<?> entityClass = Class.forName(className);
         return em.find(entityClass, pk);
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException("Failed retrieving entity bean.", e);
      }
   }

   @SuppressWarnings("unchecked")
   private EntityManager getEntityManager(Map attributes)
   {
      String jndiPath = (String) attributes.get(
            EntityBeanConstants.JNDI_PATH_ATT);
      String emSource = (String) attributes.get(
            EntityBeanConstants.ENTITY_MANAGER_SOURCE_ATT);
      
      EntityManager em = null;
      if (EntityBeanConstants.JNDI_SOURCE.equals(emSource))
      {
         em = (EntityManager) EJBUtils.getJndiObject(jndiPath, EntityManager.class);
         trace.debug("EntityManager from JNDI: " + em);
      }
      else if (EntityBeanConstants.FACTORY_JNDI.equals(emSource))
      {
         EntityManagerFactory factory = (EntityManagerFactory)
            EJBUtils.getJndiObject(jndiPath, EntityManagerFactory.class);
         em = factory.createEntityManager();
         trace.debug("EntityManager from FactoryJNDI: " + em);
      }
      else if (EntityBeanConstants.UNIT_NAME.equals(emSource))
      {
         EntityManagerFactory factory = Persistence.createEntityManagerFactory(jndiPath);
         em = factory.createEntityManager();
         trace.debug("EntityManager from UnitName: " + em);
      }
      
      if (em == null)
      {
         throw new PublicException("No EntityManager could be retrieved.");
      }
      return em;
   }
}
