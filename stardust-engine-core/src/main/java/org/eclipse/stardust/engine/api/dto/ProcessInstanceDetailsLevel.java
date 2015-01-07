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

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * Represents the level of detail for a {@link ProcessInstance}.
 * 
 * @author born
 */
public class ProcessInstanceDetailsLevel extends IntKey
{
   private static final long serialVersionUID = 2L;

   /**
   * The process instance details only contain first level attributes of ProcessInstanceBean.
   */
   public static final int CORE = 1;

   /**
    * The process instance details contain same attributes as with CORE plus all extended attributes.
    */
   public static final int WITH_PROPERTIES = 2;

   /**
    * The process instance details contain same attributes as with WITH_PROPERTIES plus 
    * all extended attributes are resolved, i.e. contain further detail objects.
    */
   public static final int WITH_RESOLVED_PROPERTIES = 3;

   /**
    * The process instance details are fully initialized.
    */
   public static final int FULL = WITH_RESOLVED_PROPERTIES;

   /**
    * Alias for default details level.
    */
   public static final int DEFAULT = WITH_PROPERTIES;

   public static final ProcessInstanceDetailsLevel Core = new ProcessInstanceDetailsLevel(
         CORE, "Core");
   public static final ProcessInstanceDetailsLevel WithProperties = new ProcessInstanceDetailsLevel(
         WITH_PROPERTIES, "WithProperties");
   public static final ProcessInstanceDetailsLevel WithResolvedProperties = new ProcessInstanceDetailsLevel(
         WITH_RESOLVED_PROPERTIES, "WithResolvedProperties");
   public static final ProcessInstanceDetailsLevel Full = WithResolvedProperties;
   public static final ProcessInstanceDetailsLevel Default = WithProperties;

   /**
    * Factory method to get the ProcessInstanceState corresponding to the given code.
    *
    * @param value one of the ProcessInstanceState codes.
    *
    * @return one of the predefined ProcessInstanceStates or null if it's an invalid code.
    */
   public static ProcessInstanceDetailsLevel getlevel(int value)
   {
      return (ProcessInstanceDetailsLevel) getKey(ProcessInstanceDetailsLevel.class,
            value);
   }

   public static final String PRP_PI_DETAILS_LEVEL = "PROCESS_INSTANCE_DETAILS_LEVEL";

   protected Object readResolve()
   {
      return super.readResolve();
   }
   
   private ProcessInstanceDetailsLevel(int value, String name)
   {
      super(value, name);
   }
}
