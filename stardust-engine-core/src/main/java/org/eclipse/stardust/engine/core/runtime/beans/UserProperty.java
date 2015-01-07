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

import org.eclipse.stardust.engine.core.persistence.FieldRef;

/**
 *
 */
public class UserProperty extends AbstractProperty
{
   public static final String EMPTY_SCOPE = "";
   public static final String PROFILE_SCOPE = "profile";
   
   public static final String FIELD__OID = AbstractProperty.FIELD__OID;
   public static final String FIELD__OBJECT_OID = AbstractProperty.FIELD__OBJECT_OID;
   public static final String FIELD__NAME = AbstractProperty.FIELD__NAME;
   public static final String FIELD__TYPE_KEY = AbstractProperty.FIELD__TYPE_KEY;
   public static final String FIELD__NUMBER_VALUE = AbstractProperty.FIELD__NUMBER_VALUE;
   public static final String FIELD__STRING_VALUE = AbstractProperty.FIELD__STRING_VALUE;
   public static final String FIELD__LAST_MODIFICATION_TIME = AbstractProperty.FIELD__LAST_MODIFICATION_TIME;
   public static final String FIELD__SCOPE = "scope";
   
   public static final FieldRef FR__OID = new FieldRef(UserProperty.class, FIELD__OID);
   public static final FieldRef FR__OBJECT_OID = new FieldRef(UserProperty.class, FIELD__OBJECT_OID);
   public static final FieldRef FR__NAME = new FieldRef(UserProperty.class, FIELD__NAME);
   public static final FieldRef FR__TYPE_KEY = new FieldRef(UserProperty.class, FIELD__TYPE_KEY);
   public static final FieldRef FR__NUMBER_VALUE = new FieldRef(UserProperty.class, FIELD__NUMBER_VALUE);
   public static final FieldRef FR__STRING_VALUE = new FieldRef(UserProperty.class, FIELD__STRING_VALUE);
   public static final FieldRef FR__LAST_MODIFICATION_TIME = new FieldRef(UserProperty.class, FIELD__LAST_MODIFICATION_TIME);
   public static final FieldRef FR__SCOPE = new FieldRef(UserProperty.class, FIELD__SCOPE);
   
   public static final String TABLE_NAME = "user_property";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "user_property_seq";

   public static final String[] user_prp_idx1_INDEX = new String[]{FIELD__OBJECT_OID};
   public static final String[] user_prp_idx2_INDEX =
         new String[]{FIELD__TYPE_KEY, FIELD__NUMBER_VALUE};
   public static final String[] user_prp_idx3_INDEX =
         new String[]{FIELD__TYPE_KEY, FIELD__STRING_VALUE};
   public static final String[] user_prp_idx4_UNIQUE_INDEX = new String[]{FIELD__OID};
   
   private String scope;

   public UserProperty()
   {
   }

   public UserProperty(long user, String name, Object value)
   {
      super(user, name, value);
      this.scope = EMPTY_SCOPE;
   }

   public void setScope(String scope)
   {
      fetch();
      if ( !this.scope.equals(scope))
      {
         markModified(FIELD__SCOPE);
         this.scope = scope;
      }
   }

   public String getScope()
   {
      fetch();
      return scope == null ? EMPTY_SCOPE : scope;
   }
}
