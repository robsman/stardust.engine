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
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.Pair;


/**
 * @author sborn
 */
public class AttributeJoinDescriptor
      implements IAttributeJoinDescriptor, Serializable
{
   private static final long serialVersionUID = 1L;
   
   protected Class joinRhsType;
   protected List/*<Pair<String, String>>*/ joinFields;
   protected String rhsField;
   protected String joinAttributeName;

   public AttributeJoinDescriptor(Class joinRhsType, String lhsField, String rhsField,
         String joinAttributeName)
   {
      this(joinRhsType, new Pair[]{new Pair(lhsField, rhsField)}, joinAttributeName);
   }

   /**
    * @param joinRhsType
    * @param firstJoinFields instance of Pair(lhsField, rhsField)
    * @param secondJoinFields instance of Pair(lhsField, rhsField)
    * @param joinAttributeName
    */
   public AttributeJoinDescriptor(Class joinRhsType,
         Pair/*<String, String>*/firstJoinFields,
         Pair/*<String, String>*/secondJoinFields, String joinAttributeName)
   {
      this(joinRhsType, new Pair[] { firstJoinFields, secondJoinFields },
            joinAttributeName);
   }

   /**
    * @param joinRhsType
    * @param joinFields Array of instances of Pair(lhsField, rhsField)
    * @param joinAttributeName
    */
   public AttributeJoinDescriptor(Class joinRhsType, Pair/*<String, String>*/[] joinFields,
         String joinAttributeName)
   {
      super();
      this.joinRhsType = joinRhsType;
      this.joinFields = Arrays.asList(joinFields);
      this.joinAttributeName = joinAttributeName;
   }

   public String getJoinAttributeName()
   {
      return joinAttributeName;
   }

   public Class getJoinRhsType()
   {
      return joinRhsType;
   }
   
   public List/*<Pair<String, String>>*/ getJoinFields()
   {
      return Collections.unmodifiableList(joinFields);
   }
}