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
package org.eclipse.stardust.engine.extensions.ejb.data;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public final class EntityBeanConstants
{
   public static final String VERSION_2_X = "entity20"; //$NON-NLS-1$
   public static final String VERSION_3_X = "entity30"; //$NON-NLS-1$
   public static final String VERSION_ATT = PredefinedConstants.ENGINE_SCOPE + "ejbVersion"; //$NON-NLS-1$

   public static final String CLASS_NAME_ATT = PredefinedConstants.CLASS_NAME_ATT;
   public static final String JNDI_PATH_ATT = PredefinedConstants.JNDI_PATH_ATT;
   public static final String ENTITY_MANAGER_SOURCE_ATT = PredefinedConstants.ENGINE_SCOPE + "emSource"; //$NON-NLS-1$
   public static final String JNDI_SOURCE = "JNDI";
   public static final String FACTORY_JNDI = "FactoryJNDI";
   public static final String UNIT_NAME = "UnitName";

   public static final String PRIMARY_KEY_ATT = PredefinedConstants.PRIMARY_KEY_ATT;
   public static final String PRIMARY_KEY_TYPE_ATT = PredefinedConstants.ENGINE_SCOPE + "primaryKeyType"; //$NON-NLS-1$
   public static final String PRIMARY_KEY_ELEMENTS_ATT = PredefinedConstants.ENGINE_SCOPE + "primaryKeyElements"; //$NON-NLS-1$

   public static final String ID_CLASS_PK = "IdClass";
   public static final String EMBEDDED_ID_PK = "EmbeddedId";
   public static final String ID_PK = "Id";

   public static final String FIELD_PREFIX = "field:";
   public static final String PROPERTY_PREFIX = "property:";

   private EntityBeanConstants() {};
}
