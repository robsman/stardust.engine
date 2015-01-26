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
package org.eclipse.stardust.engine.core.runtime.beans;


/**
 * Abstract class which provides the facility to transport initial values
 * to {@link DataValueBean} construction.
 *  
 * @author sborn
 * @version $Revision$
 */
public abstract class AbstractInitialDataValueProvider
{
   private boolean usedForInitialization = false;
   
   /**
    * This method is used in {@link DataValueBean} constructor to flag usage of 
    * initial value provided by {@link #getEvaluatedValue()}.
    */
   public void setUsedForInitialization()
   {
      usedForInitialization = true;
   }
   
   /**
    * @return Was the value provided by {@link #getEvaluatedValue()} used for initialization.  
    */
   public boolean isUsedForInitialization()
   {
      return usedForInitialization;
   }
   
   /**
    * @return Value which will be used as initial value of a newly created {@link DataValueBean}.
    */
   abstract public EvaluatedValue getEvaluatedValue();
   
   public static final class EvaluatedValue
   {
      private Object value;
      private boolean isModifiedHandle;
      
      public EvaluatedValue(Object value, boolean isModifiedHandle)
      {
         this.value = value;
         this.isModifiedHandle = isModifiedHandle;
      }

      public boolean isModifiedHandle()
      {
         return isModifiedHandle;
      }
      
      public Object getValue()
      {
         return value;
      }
   }
}
