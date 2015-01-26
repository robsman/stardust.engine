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

import org.eclipse.stardust.common.Assert;

/**
 * Manages all differences between model elements. These differences can be
 * <p>
 * <li>modified attributes, e.g. a name attribute</li>
 * <li>added subelements, e.g. new activities in a process definition</li>
 * <li>removed subelements, e.g. removed process definition in a model</li>
 * <li>modified subelements, recursively the above</li>
 */
public class Differences
{
   private static final String STRING_UNKNOWN = "<unknown>";
   public static final NullObject NULL = new NullObject();

   private ModelElement sourceModelElement;
   private ModelElement targetModelElement;
   /**
    * Stores modified attributes as <tt>ModifiedProperty</tt> containing
    * <p>
    * <li>the name of the attribute</li>
    * <li>the former value of the attribute as a (wrapper) object</li>
    * <li>the new value of the attribute as a (wrapper) object</li>
    */
   private ModelElements modifiedAttributes;
   private ModelElements addedModelElements;
   private ModelElements removedModelElements;
   private ModelElements modifiedModelElements;

   private boolean confirmed;
   private Collector collector;

   public Differences(ModelElement sourceModelElement,
         ModelElement targetModelElement, Collector collector)
   {
      this.sourceModelElement = sourceModelElement;
      this.targetModelElement = targetModelElement;
      this.collector = collector;

      modifiedAttributes = new ModelElements(ModelElements.MODIFIED_ATTRIBUTES);
      addedModelElements = new ModelElements(ModelElements.ADDED);
      removedModelElements = new ModelElements(ModelElements.REMOVED);
      modifiedModelElements = new ModelElements(ModelElements.MODIFIED);
      confirmed = true;
   }

   public String toString()
   {
      return sourceModelElement == null ? STRING_UNKNOWN : sourceModelElement.toString();
   }

   public ModelElements getAllModifiedAttributes()
   {
      return modifiedAttributes;
   }

   public ModelElements getAllAddedModelElements()
   {
      return addedModelElements;
   }

   public ModelElements getAllRemovedModelElements()
   {
      return removedModelElements;
   }

   public ModelElements getAllModifiedModelElements()
   {
      return modifiedModelElements;
   }

   public ModelElement getSourceModelElement()
   {
      return sourceModelElement;
   }

   public ModelElement getTargetModelElement()
   {
      return targetModelElement;
   }

   /**
    * Indicates, wether there are any differences between the source and
    * the target model element.
    */
   public boolean isEmpty()
   {
      return (modifiedAttributes.count() == 0
            && addedModelElements.count() == 0
            && removedModelElements.count() == 0
            && modifiedModelElements.count() == 0);
   }

   /**
    * Sets the confirmed flag of the difference object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the differences described by this object shall be applied as a whole.
    */
   public void setConfirmed(boolean confirmed)
   {
      this.confirmed = confirmed;

      modifiedAttributes.setConfirmed(confirmed);
      addedModelElements.setConfirmed(confirmed);
      removedModelElements.setConfirmed(confirmed);
      modifiedModelElements.setConfirmed(confirmed);
   }

   /**
    * Retrieves the confirmed flag of the difference object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the differences described by this object shall be applied as a whole.
    */
   public boolean getConfirmed()
   {
      return confirmed;
   }

   /**
    * Retrieves the confirmed flag of the difference object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the differences described by this object shall be applied as a whole.
    */
   public boolean isConfirmed()
   {
      return getConfirmed();
   }

   public void addToAddedModelElements(String name, ModelElement source)
   {
      addedModelElements.addToModelElements(new AddedElement(source, name));
   }

   public void addToModifiedModelElements(Differences diff)
   {
      modifiedModelElements.addToModelElements(diff);
   }

   public void addToRemovedModelElements(String name, ModelElement target)
   {
      removedModelElements.addToModelElements(new RemovedElement(target, name));

   }

   public void addToModifiedAttributes(String name, String humanReadableName, Object source, Object target)
   {
      modifiedAttributes.addToModelElements(
            new ModifiedProperty(name, humanReadableName, source, target, false));
   }

   public void addToModifiedDynamicAttributes(String name, Object source, Object target)
   {
      modifiedAttributes.addToModelElements(new ModifiedProperty(
            name, name, source, target, true));
   }

   public void removeAllModifiedAttributes()
   {
      modifiedAttributes.clear();
   }

   public void addToAddedReferences(String fieldName, IdentifiableElement source)
   {
      Assert.isNotNull(source);
      Assert.isNotNull(fieldName);
      addedModelElements.addToModelElements(new AddedReference(source, fieldName));
   }

   public void addToRemovedReferences(String fieldName, ModelElement target)
   {
      Assert.isNotNull(target);
      Assert.isNotNull(fieldName);
      removedModelElements.addToModelElements(new RemovedReference(target, fieldName));
   }

   public Collector getCollector()
   {
      return collector;
   }

   static class NullObject
   {
      public String toString()
      {
         return "NULL";
      }
   }

}
