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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.AnnotatedField;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelOperations
{
   private static final Logger trace = LogManager.getLogger(ModelOperations.class);

   public static Differences compare(ModelElement target, ModelElement source)
   {
      return compare(target, source, new UniqueIdCollector());
   }

   public static Differences compare(ModelElement target, ModelElement source, Collector collector)
   {
      Differences differences = new Differences(source, target, collector);
      collector.collect(source, target);

      for (Iterator i = Reflect.getAnnotatedFields(target.getClass()).iterator(); i.hasNext();)
      {
         AnnotatedField field = (AnnotatedField) i.next();
         String simpleAttributeName = (String) field.getAnnotation("ATT");
         if (simpleAttributeName != null)
         {
            compareAttributes(field.getField(),
                  simpleAttributeName, differences, source, target);
            continue;
         }
      }
      for (Iterator i = Reflect.getFields(target.getClass()).iterator(); i.hasNext();)
      {
         Field field = (Field) i.next();
         if (Modifier.isStatic(field.getModifiers()) ||
             Modifier.isTransient(field.getModifiers()))
         {
            // static & transient fields are skipped
            continue;
         }
         if (Link.class.isAssignableFrom(field.getType()))
         {
            compareSubElements(field, source, target, differences, collector);
            continue;
         }
         if (Reference.class.isAssignableFrom(field.getType()))
         {
            compareReferences(field, source, target, differences, collector);
            continue;
         }
      }
      compareAttributeMaps(differences, source, target);

      return differences;
   }

   private static void compareSubElements(Field field, ModelElement source, ModelElement target,
         Differences differences, Collector collector)
   {
      String fieldName = field.getName();
      Link targetLink = (Link) getFieldValue(target, field);
      Link sourceLink = (Link) getFieldValue(source, field);

      for (Iterator sourceItr = sourceLink.iterator(); sourceItr.hasNext();)
      {
         ModelElement sourceEl = (ModelElement) sourceItr.next();
         ModelElement targetEl = findByLocalId(targetLink, collector, collector.getLocalId(sourceEl));
         if (targetEl == null)
         {
            differences.addToAddedModelElements(fieldName, sourceEl);
         }
         else
         {
            Differences diff = compare(targetEl, sourceEl, collector);
            if (!diff.isEmpty())
            {
               differences.addToModifiedModelElements(diff);
            }
         }
      }

      for (Iterator targetItr = targetLink.iterator(); targetItr.hasNext();)
      {
         ModelElement targetEl = (ModelElement) targetItr.next();
         ModelElement sourceEl = findByLocalId(sourceLink, collector, collector.getLocalId(targetEl));

         if (sourceEl == null)
         {
            differences.addToRemovedModelElements(fieldName, targetEl);
         }
      }
   }

   private static void compareReferences(Field field, ModelElement source, ModelElement target,
         Differences differences, Collector collector)
   {
      String fieldName = field.getName();
      Reference targetRef = (Reference) getFieldValue(target, field);
      Reference sourceRef = (Reference) getFieldValue(source, field);

      for (Iterator sourceItr = sourceRef.iterator(); sourceItr.hasNext();)
      {
         IdentifiableElement sourceEl = (IdentifiableElement) sourceItr.next();
         ModelElement targetEl = findByLocalId((Hook) targetRef, collector, collector.getLocalId(sourceEl));
         if (targetEl == null)
         {
            differences.addToAddedReferences(fieldName, sourceEl);
         }
      }
      for (Iterator targetItr = targetRef.iterator(); targetItr.hasNext();)
      {
         ModelElement targetEl = (ModelElement) targetItr.next();
         ModelElement sourceEl = findByLocalId((Hook) sourceRef, collector, collector.getLocalId(targetEl));

         if (sourceEl == null)
         {
            differences.addToRemovedReferences(fieldName, targetEl);
         }
      }
   }

   private static ModelElement findByLocalId(Hook link, Collector collector, Object localId)
   {
      for (Iterator i = link.iterator(); i.hasNext();)
      {
         ModelElement match = (ModelElement) i.next();
         if (collector.getLocalId(match).equals(localId))
         {
            return match;
         }
      }
      return null;
   }

   private static void compareAttributes(Field field, String humanReadableName, Differences differences, Object source, Object target)
   {
      try
      {
         field.setAccessible(true);
         Object oldValue = field.get(source);
         Object newValue = field.get(target);

         if (!CompareHelper.areEqual(oldValue, newValue))
         {
            differences.addToModifiedAttributes(field.getName(), humanReadableName, oldValue, newValue);
         }
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   private static void compareAttributeMaps(Differences differences,
         ModelElement source, ModelElement target)
   {
      Field field = Reflect.getField(source.getClass(), "attributes");
      Map sourceMap = (Map) getFieldValue(source, field);
      Map targetMap = (Map) getFieldValue(target, field);
      
      for (Iterator i = sourceMap.keySet().iterator(); i.hasNext();)
      {
         String sourceKey = (String) i.next();
         Object sourceVal = sourceMap.get(sourceKey);
         Object targetVal = targetMap.get(sourceKey);
         
         if (!CompareHelper.areEqual(targetVal, sourceVal))
         {
            differences.addToModifiedDynamicAttributes(sourceKey, sourceVal, targetVal);
         }
      }

      for (Iterator i = targetMap.keySet().iterator(); i.hasNext();)
      {
         String targetKey = (String) i.next();
         Object targetVal = targetMap.get(targetKey);
         
         if (!sourceMap.containsKey(targetKey) && null != targetVal)
         {
            differences.addToModifiedDynamicAttributes(targetKey, null, targetVal);
         }
      }
   }

   private static Object getFieldValue(ModelElement element, Field field)
   {
      try
      {
         field.setAccessible(true);
         return field.get(element);
      }
      catch (Exception e)
      {
         trace.warn("element = " + element);
         trace.warn("field = " + field);
         trace.warn("", e);
         return null;
      }
   }

   private static void setField(String name, Object value, String methodPrefix, ModelElement element)
   {
      try
      {
         String methodName = methodPrefix + Character.toUpperCase(name.charAt(0)) +
               name.substring(1);
         Method result = Reflect.getSetterMethod(element.getClass(), methodName, null);
         if (result != null)
         {
            result.invoke(element, new Object[]{value});
         }
         else
         {
            throw new InternalException(
                  "Couldn't find setter '" + methodName + "' on class '"
                  + element.getClass().getName() + ".");
         }
      }
      catch (Exception e)
      {
         trace.warn("name = " + name);
         trace.warn("value = " + value);
         trace.warn("element = " + element);
         trace.warn("methodPrefix = " + methodPrefix);
         trace.warn("", e);
      }
   }

   private static void mergeTree(Differences differences)
   {
      if (!differences.isConfirmed())
      {
         return;
      }

      ModelElement target = differences.getTargetModelElement();
      ModelElements addedElements = differences.getAllAddedModelElements();
      for (Iterator i = addedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         try
         {
            Object o = i.next();
            if (!(o instanceof AddedElement))
            {
               continue;
            }
            AddedElement added = (AddedElement) o;
            ModelElementBean sourceElement = (ModelElementBean) added.getModelElement();
            ModelElement targetElement = sourceElement.deepCopyI(target, false, differences.getCollector());
            added.setTargetElement(targetElement);

            // @todo (egypt):
            setField(added.getFieldName(), targetElement, "addTo", target);
            if (targetElement instanceof IdentifiableElement)
            {
               targetElement.register(0);
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }

      ModelElements removedElements = differences.getAllRemovedModelElements();
      for (Iterator i = removedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         try
         {

            Object o = i.next();
            if (!(o instanceof RemovedElement))
            {
               continue;
            }
            ((RemovedElement) o).getModelElement().delete();
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }

      ModelElements modifiedElements = differences.getAllModifiedModelElements();

      for (Iterator i = modifiedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         Differences subDifferences = (Differences) i.next();
         try
         {
            mergeTree(subDifferences);
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }

      Field field = Reflect.getField(target.getClass(), "attributes");
      Map targetMap = (Map) getFieldValue(target, field);

      ModelElements modifiedAttributes = differences.getAllModifiedAttributes();
      for (Iterator i = modifiedAttributes.getAllConfirmedModelElements(); i.hasNext();)
      {
         ModifiedProperty modifiedProperty = (ModifiedProperty) i.next();
         if (modifiedProperty.isConfirmed())
         {
            if (modifiedProperty.isDynamic())
            {
               Object value = modifiedProperty.getTarget();
               if (value == null)
               {
                  targetMap.remove(modifiedProperty.getFieldName());
               }
               else
               {
                  targetMap.put(modifiedProperty.getFieldName(), value);
               }
            }
            else
            {
               try
               {
                  Field att = Reflect.getField(target.getClass(), modifiedProperty.getFieldName());
                  att.set(target, modifiedProperty.getTarget());
                  // @todo (egypt):
                  //setField(modifiedProperty.getFieldName(), modifiedProperty.getTarget(),
                  //      "set", target);
               }
               catch (Exception e)
               {
                  trace.warn("", e);
               }
            }
         }
      }
   }

   private static void mergeReferences(Differences differences)
   {
      ModelElements addedElements = differences.getAllAddedModelElements();
      for (Iterator i = addedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         try
         {
            Object next = i.next();
            if ((next instanceof AddedElement))
            {
               AddedElement added = (AddedElement) next;
               ModelElement source = added.getModelElement();
               ModelElementBean target = (ModelElementBean) added.getTargetElement();
               // copying refs for added subtrees
               target.deepCopyII(source, differences.getCollector());
               // ... and adding the connection end points

               if (source instanceof Connection)
               {
                  ModelElement sourceFirst = ((Connection) source).getFirst();
                  if (sourceFirst != null)
                  {
                     ((Connection) target).setFirst(differences.getCollector().findInTarget(sourceFirst));
                  }
                  ModelElement sourceSecond = ((Connection) source).getSecond();
                  if (sourceSecond != null)
                  {
                     ((Connection) target).setSecond(differences.getCollector().findInTarget(sourceSecond));
                  }
               }
            }
            else if (next instanceof AddedReference)
            {
               AddedReference ref = (AddedReference) next;
               ModelElement source = ref.getModelElement();
               ModelElement match = differences.getCollector().findInTarget(source);
               if (match != null)
               {
                  ModelElement target = differences.getTargetModelElement();
                  Field field = Reflect.getField(target.getClass(), ref.getFieldName());
                  if (Reference.class.isAssignableFrom(field.getType()))
                  {
                     Reference reference = (Reference) getFieldValue(target, field);
                     reference.add(match);
                  }
                  else
                  {
                     Assert.lineNeverReached();
                  }
               }

            }
            else
            {
               Assert.lineNeverReached();
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }

      ModelElements removedElements = differences.getAllRemovedModelElements();
      for (Iterator i = removedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         Object next = i.next();
         if (next instanceof RemovedReference)
         {
            RemovedReference removed = (RemovedReference) next;
            ModelElement target = removed.getModelElement();
            Field field = Reflect.getField(differences.getTargetModelElement().getClass(), removed.getFieldName());
            Hook hook = (Hook) getFieldValue(differences.getTargetModelElement(), field);
            hook.remove(target);
         }
      }

      ModelElements modifiedElements = differences.getAllModifiedModelElements();

      for (Iterator i = modifiedElements.getAllConfirmedModelElements(); i.hasNext();)
      {
         Differences subDifferences = (Differences) i.next();
         try
         {
            mergeReferences(subDifferences);
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
      }

   }

   public static void merge(Differences diff)
   {
      mergeTree(diff);
      mergeReferences(diff);
   }
}
