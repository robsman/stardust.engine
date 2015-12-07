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
import org.eclipse.stardust.engine.api.model.PredefinedConstants;

import com.google.gson.Gson;

public class BusinessObjectRelationship
{
   public static final String BUSINESS_OBJECT_RELATIONSHIPS = PredefinedConstants.ENGINE_SCOPE + "businessObjectRelationships";

   private static final BusinessObjectRelationship[] EMPTY = {};

   private static final Gson gson = new Gson();

   public static class BusinessObjectReference
   {
      public String id;
      public String modelId;
   }

   public static enum Cardinality
   {
      TO_ONE, TO_MANY;
   }

   public BusinessObjectReference otherBusinessObject;
   public String otherRole;
   public Cardinality otherCardinality;
   public String otherForeignKeyField;
   public String thisRole;
   public Cardinality thisCardinality;
   public String thisForeignKeyField;
   public Boolean propagateAccess;

   public static BusinessObjectRelationship[] fromJsonString(String json)
   {
      return StringUtils.isEmpty(json) ? EMPTY : gson.fromJson(json, BusinessObjectRelationship[].class);
   }
}
