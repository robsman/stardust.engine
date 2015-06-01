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
package org.eclipse.stardust.engine.core.benchmark;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkResultDetails implements BenchmarkResult, Serializable
{

   private static final long serialVersionUID = 1L;

   long benchmarkOid;
   
   int category;
   
   Map<String, Serializable> properties;
   
   public BenchmarkResultDetails(long benchmakrOid, int category,
         Map<String, Serializable> properties)
   {
     this.benchmarkOid = benchmakrOid;
     this.category = category;
     this.properties = properties;
   }

   @Override
   public long getBenchmark()
   {
      return this.benchmarkOid;
   }

   @Override
   public int getCategory()
   {
      return this.category;
   }

   @Override
   public Map<String, Serializable> getProperties()
   {     
      return this.properties;
   }

}
