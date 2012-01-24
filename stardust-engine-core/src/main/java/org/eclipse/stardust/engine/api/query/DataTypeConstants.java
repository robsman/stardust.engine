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

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;


/**
 * Constants of data type ids used in the {@link Data} specified in a {@link Model}.
 * The data type of a data is assigned at modeling time.
 *
 * @author roland.stamm
 *
 */
public class DataTypeConstants
{

   public static final String PRIMITIVE_DATA = PredefinedConstants.PRIMITIVE_DATA;

   public static final String SERIALIZABLE_DATA = PredefinedConstants.SERIALIZABLE_DATA;

   public static final String ENTITY_BEAN_DATA = PredefinedConstants.ENTITY_BEAN_DATA;

   public static final String PLAIN_XML_DATA = PredefinedConstants.PLAIN_XML_DATA;

   public static final String HIBERNATE_DATA = PredefinedConstants.HIBERNATE_DATA;

   public static final String STRUCTURED_DATA = PredefinedConstants.STRUCTURED_DATA;

   public static final String DMS_DOCUMENT_DATA = DmsConstants.DATA_TYPE_DMS_DOCUMENT;

   public static final String DMS_DOCUMENT_LIST_DATA = DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST;

   public static final String DMS_FOLDER_DATA = DmsConstants.DATA_TYPE_DMS_FOLDER;

   public static final String DMS_FOLDER_LIST_DATA = DmsConstants.DATA_TYPE_DMS_FOLDER_LIST;

}
