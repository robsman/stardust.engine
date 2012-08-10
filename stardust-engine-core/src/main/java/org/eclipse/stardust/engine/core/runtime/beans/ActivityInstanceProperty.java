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
public class ActivityInstanceProperty extends AbstractPropertyWithUser
{
   public static final String FIELD__OID = AbstractProperty.FIELD__OID;
   public static final String FIELD__OBJECT_OID = AbstractProperty.FIELD__OBJECT_OID;
   public static final String FIELD__NAME = AbstractProperty.FIELD__NAME;
   public static final String FIELD__TYPE_KEY = AbstractProperty.FIELD__TYPE_KEY;
   public static final String FIELD__NUMBER_VALUE = AbstractProperty.FIELD__NUMBER_VALUE;
   public static final String FIELD__STRING_VALUE = AbstractProperty.FIELD__STRING_VALUE;
   public static final String FIELD__LAST_MODIFICATION_TIME = AbstractProperty.FIELD__LAST_MODIFICATION_TIME;
   public static final String FIELD__WORKFLOWUSER = AbstractPropertyWithUser.FIELD__WORKFLOWUSER;

   public static final FieldRef FR__OID = new FieldRef(ActivityInstanceProperty.class, FIELD__OID);
   public static final FieldRef FR__OBJECT_OID = new FieldRef(ActivityInstanceProperty.class, FIELD__OBJECT_OID);
   public static final FieldRef FR__NAME = new FieldRef(ActivityInstanceProperty.class, FIELD__NAME);
   public static final FieldRef FR__TYPE_KEY = new FieldRef(ActivityInstanceProperty.class, FIELD__TYPE_KEY);
   public static final FieldRef FR__NUMBER_VALUE = new FieldRef(ActivityInstanceProperty.class, FIELD__NUMBER_VALUE);
   public static final FieldRef FR__STRING_VALUE = new FieldRef(ActivityInstanceProperty.class, FIELD__STRING_VALUE);
   public static final FieldRef FR__LAST_MODIFICATION_TIME = new FieldRef(ProcessInstanceProperty.class, FIELD__LAST_MODIFICATION_TIME);
   public static final FieldRef FR__WORKFLOWUSER = new FieldRef(ProcessInstanceProperty.class, FIELD__WORKFLOWUSER);

   public static final String TABLE_NAME = "act_inst_property";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "act_inst_property_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] act_inst_prp_idx1_INDEX =
         new String[]{FIELD__OBJECT_OID};
   public static final String[] act_inst_prp_idx2_INDEX =
         new String[]{FIELD__TYPE_KEY, FIELD__NUMBER_VALUE};
   public static final String[] act_inst_prp_idx3_INDEX =
         new String[]{FIELD__TYPE_KEY, FIELD__STRING_VALUE};
   public static final String[] act_inst_prp_idx4_UNIQUE_INDEX =
         new String[]{FIELD__OID};

   public ActivityInstanceProperty()
   {
   }

   public ActivityInstanceProperty(long activityInstance, String name,Object value)
   {
      super(activityInstance, name, value);
   }
}
