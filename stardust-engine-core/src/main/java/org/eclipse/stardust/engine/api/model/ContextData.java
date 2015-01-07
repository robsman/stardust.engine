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
package org.eclipse.stardust.engine.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Value object that encapsulates a set of data values for a specific context.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ContextData implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String context;
   private Map<String, ?> data;

   /**
    * Creates a new ContextData object for the given context containing the specified data values.
    * 
    * @param context the id of the context.
    * @param data a map containing values for specific data ids.
    */
   public ContextData(String context, Map<String, ?> data)
   {
      this.context = context;
      this.data = data;
   }

   /**
    * Retrieves the context for which this ContextData was created.
    * 
    * @return the context id
    */
   public String getContext()
   {
      return context;
   }

   /**
    * Retrieves the live values for the data ids..
    * 
    * @return the map containing values for specific data ids.
    */
   public Map<String, ?> getData()
   {
      return data;
   }
}
