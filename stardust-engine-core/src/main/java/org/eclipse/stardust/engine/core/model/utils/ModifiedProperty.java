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

/**
 *
 */
public class ModifiedProperty
{
   private String fieldName;
   private String humanReadableName;
   private Object source;
   private Object target;
   private boolean confirmed;
   private boolean dynamic;

   public String getFieldName()
   {
      return fieldName;
   }

   /**
    *
    */
   public ModifiedProperty(String fieldName, String humanReadableName, Object source, Object target, boolean dynamic)
   {
      this.fieldName = fieldName;
      this.humanReadableName = humanReadableName;
      this.source = target;
      this.target = source;
      this.dynamic = dynamic;
      confirmed = true;
   }

   /**
    *
    */
   public String getHumanReadableName()
   {
      return humanReadableName;
   }

   /**
    *
    */
   public Object getSource()
   {
      return source;
   }

   /**
    *
    */
   public Object getTarget()
   {
      return target;
   }

   /**
    * Sets the confirmed flag of this modified object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the modification described by this object shall be applied.
    */
   public void setConfirmed(boolean confirmed)
   {
      this.confirmed = confirmed;
   }

   /**
    * Retrieves the confirmed flag of this modified object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the modification described by this object shall be applied.
    */
   public boolean getConfirmed()
   {
      return confirmed;
   }

   /**
    * Retrieves the confirmed flag of this modified object.
    * <p>
    * This flag is used during migration processes to decide, wether or not
    * the modification described by this object shall be applied.
    */
   public boolean isConfirmed()
   {
      return getConfirmed();
   }

   /**
    *
    */
   public String toString()
   {
      // @optimize .. use string constant instead of local objects to avoid garbage collection
      String _oldValue;
      String _newValue;

      if (source != null)
      {
         _oldValue = source.toString();
      }
      else
      {
         _oldValue = "NULL";
      }

      if (target != null)
      {
         _newValue = target.toString();
      }
      else
      {
         _newValue = "NULL";
      }

      // Adjust size for display

      if (_oldValue.length() > 15)
      {
         _oldValue = _oldValue.substring(0, 14) + " ...";
      }

      if (_newValue.length() > 15)
      {
         _newValue = _newValue.substring(0, 14) + " ...";
      }

      return humanReadableName + ": " + _oldValue + " > " + _newValue;
   }

   public boolean isDynamic()
   {
      return dynamic;
   }
}
