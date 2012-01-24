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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;


/**
 * Auxiliary class for displaying and confirming a set of model elements. It
 * manages two sets of model elements, original model elements and
 * model elements whose containement in this set is explicitely confirmed.
 * <p>
 * This class is used for the analysis and migration of model differences.
 */
public class ModelElements
{
   public static final int ADDED = 0;
   public static final int REMOVED = 1;
   public static final int MODIFIED = 2;
   public static final int MODIFIED_ATTRIBUTES = 3;
   public static final int MODIFIED_DYNAMIC_ATTRIBUTES = 4;

   private int type;
   private List modelElements;
   private List confirmedModelElements;

   /**
    *
    */
   public ModelElements(int type)
   {
      this.type = type;

      modelElements = CollectionUtils.newList();
      confirmedModelElements = CollectionUtils.newList();
   }

   /**
    *
    */
   public void clear()
   {
      if (modelElements != null)
      {
         modelElements.clear();
      }
      if (confirmedModelElements != null)
      {
         confirmedModelElements.clear();
      }

   }

   /**
    *
    */
   public String toString()
   {
      StringBuffer result = new StringBuffer();

      switch (type)
      {
         case ADDED:
            {
               result.append("Added Elements (");

               break;
            }
         case REMOVED:
            {
               result.append("Removed Elements (");

               break;
            }
         case MODIFIED:
            {
               result.append("Modified Elements (");

               break;
            }
         case MODIFIED_ATTRIBUTES:
            result.append("Modified Attributes (");
            break;
         case MODIFIED_DYNAMIC_ATTRIBUTES:
            result.append("Modified Dynamic Attributes (");
            break;
         default:
            Assert.lineNeverReached();
      }

      result.append(count());
      result.append(", ");
      result.append(confirmedCount());
      result.append(" confirmed)");

      return result.toString();
   }

   /**
    *
    */
   public int getType()
   {
      return type;
   }

   /**
    *
    */
   public void addToModelElements(Object modelElement)
   {
      modelElements.add(modelElement);
      confirmedModelElements.add(modelElement);
   }

   /**
    *
    */
   public void removeFromModelElements(Object modelElement)
   {
      modelElements.remove(modelElement);
      confirmedModelElements.remove(modelElement);
   }

   /**
    * Returns the count of model elements.
    */
   public int count()
   {
      return modelElements.size();
   }

   /**
    *
    */
   public java.util.Iterator getAllModelElements()
   {
      return modelElements.iterator();
   }

   /**
    *
    */
   public void addToConfirmedModelElements(Object modelElement)
   {
      if (!confirmedModelElements.contains(modelElement))
      {
         confirmedModelElements.add(modelElement);
      }
   }

   /**
    *
    */
   public void removeFromConfirmedModelElements(Object modelElement)
   {
      confirmedModelElements.remove(modelElement);
   }

   /**
    *
    */
   public boolean containedInConfirmedModelElements(Object modelElement)
   {
      return confirmedModelElements.contains(modelElement);
   }

   /**
    *
    */
   public java.util.Iterator getAllConfirmedModelElements()
   {
      return confirmedModelElements.iterator();
   }

   /**
    * Returns the count of confirmed model elements.
    */
   public int confirmedCount()
   {
      return confirmedModelElements.size();
   }

   /**
    * Adds all model elements to the confirmed model elements or clears these.
    * <p>
    * If the model elements type is <tt>MODIFIED</tt>, this operation is
    * applied recursively against the whole hiararchy of subelements.
    */
   public void setConfirmed(boolean confirmed)
   {
      confirmedModelElements.clear();

      Iterator elements = modelElements.iterator();
      Object modelElement;

      while (elements.hasNext())
      {
         modelElement = elements.next();

         if (confirmed)
         {
            confirmedModelElements.add(modelElement);
         }

         if (modelElement instanceof Differences)
         {
            ((Differences) modelElement).setConfirmed(confirmed);
         }

         if (modelElement instanceof ModifiedProperty)
         {
            ((ModifiedProperty) modelElement).setConfirmed(confirmed);
         }
      }
   }
}
