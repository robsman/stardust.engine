/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import org.eclipse.stardust.common.StringUtils;

import com.google.gson.Gson;

public class BusinessObjectRelationship
{
   private static BusinessObjectRelationship[] EMPTY = {};

   private static Gson gson = new Gson();

   public static class BusinessObjectReference
   {
      public String id;
      public String modelId;
   }

   public BusinessObjectReference otherBusinessObject;
   public String otherRole;
   public String otherCardinality; // TODO: (fh) make it an enumeration
   public String otherForeignKeyField;
   public String thisRole;
   public String thisCardinality; // TODO: (fh) make it an enumeration
   public String thisForeignKeyField;

   public static BusinessObjectRelationship[] fromJsonString(String json)
   {
      return StringUtils.isEmpty(json) ? EMPTY : gson.fromJson(json, BusinessObjectRelationship[].class);
   }
}
