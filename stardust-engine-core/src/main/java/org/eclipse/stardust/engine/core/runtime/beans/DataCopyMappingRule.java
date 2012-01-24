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

import org.eclipse.stardust.engine.api.model.IData;

public class DataCopyMappingRule
{

   private IData sourceData;

   private IData targetData;

   private boolean removeMetaData;

   private boolean mergeListTypeData = false;

   public DataCopyMappingRule(IData sourceData, IData targetData, boolean removeMetaData, boolean mergeListTypeData)
   {
      super();
      this.mergeListTypeData = mergeListTypeData;
      this.sourceData = sourceData;
      this.targetData = targetData;
      this.removeMetaData = removeMetaData;
   }

   public IData getSourceData()
   {
      return sourceData;
   }

   public IData getTargetData()
   {
      return targetData;
   }

   public boolean isRemoveMetaData()
   {
      return removeMetaData;
   }

   public boolean isMergeListTypeData()
   {
      return mergeListTypeData;
   }





}
