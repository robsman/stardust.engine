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
package org.eclipse.stardust.engine.extensions.xml.data;

import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


// @todo (france, ub): isn't it sufficient to implement DataType?

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class XMLDataType extends IdentifiableElementBean implements IDataType
{
   public XMLDataType()
   {
      setAttribute(PredefinedConstants.ACCESSPATH_EDITOR_ATT,
            PredefinedConstants.PLAINXML_ACCESSPATH_EDITOR_CLASS);
      setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT,
            PredefinedConstants.PLAINXML_EVALUATOR_CLASS);
      setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            PredefinedConstants.PLAINXML_VALIDATOR_CLASS);
      setAttribute(PredefinedConstants.ICON_ATT,
            PredefinedConstants.PLAINXML_ICON_LOCATION);
   }

   public String getId()
   {
      return PredefinedConstants.PLAIN_XML_DATA;
   }
}
