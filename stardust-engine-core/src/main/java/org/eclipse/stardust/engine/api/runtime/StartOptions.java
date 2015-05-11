/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Map;

/**
 * Container class for options that control how a process instance is started
 * 
 * @author Thomas.Wolfram
 *
 */
public class StartOptions implements Serializable
{

   private static final long serialVersionUID = 1L;

   private Map<String, ? > data;

   private boolean synchronously;

   private long benchmarkReference;

   public Map<String, ? > getData()
   {
      return data;
   }

   public boolean isSynchronously()
   {
      return synchronously;
   }

   public long getBenchmarkReference()
   {
      return benchmarkReference;
   }

   public StartOptions(Map<String, ? > data, boolean synchronously)
   {
      this(data, synchronously, 0);
   }   
   
   public StartOptions(Map<String, ? > data, boolean synchronously,
         long benchmarkReference)
   {
      this.data = data;
      this.synchronously = synchronously;
      this.benchmarkReference = benchmarkReference;
   }

}
