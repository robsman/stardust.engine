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

import org.eclipse.stardust.common.Direction;

public class CaseDescriptorRef
{
   private int modelOid; // for convenience, not actually required
   private long caseOid;
   private int index;
   private String id;
   private String mappedType;

   public CaseDescriptorRef(int modelOid, long caseOid, int index, String id, String mappedType)
   {
      this.modelOid = modelOid;
      this.caseOid = caseOid;
      this.index = index;
      this.id = id;
      this.mappedType = mappedType;
   }

   public int getModelOID()
   {
      return modelOid;
   }

   public int getElementOID()
   {
      return -index;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return id;
   }

   public String getNamespace()
   {
      return PredefinedConstants.PREDEFINED_MODEL_ID;
   }

   public String getDescription()
   {
      return null;
   }

   public String getMappedType()
   {
      return mappedType;
   }

   public Direction getDirection()
   {
      return Direction.IN;
   }

   public boolean isKeyDescriptor()
   {
      return false;
   }

   public String getDataId()
   {
      return PredefinedConstants.CASE_DATA_ID;
   }

   public String getAccessPath()
   {
      return PredefinedConstants.CASE_DESCRIPTORS_ELEMENT + "[" + index + "]/value";
   }

   public String getProcessDefinitionId()
   {
      return PredefinedConstants.CASE_PROCESS_ID;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = prime + (int) (caseOid ^ (caseOid >>> 32));
      return prime * result + index;
   }

   @Override
   public boolean equals(Object obj)
   {
      return this == obj || obj instanceof CaseDescriptorRef &&
            caseOid == ((CaseDescriptorRef) obj).caseOid && index == ((CaseDescriptorRef) obj).index;
   }
}
