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
package org.eclipse.stardust.engine.extensions.web.jsp.contexts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationContextValidator;


public class JSPValidator implements ApplicationContextValidator
{
   public List validate(Map properties, Iterator accessPoints)
   {
      List inconsistencies = CollectionUtils.newList();
      String htmlPath = (String) properties.get(PredefinedConstants.HTML_PATH_ATT);
      if (StringUtils.isEmpty(htmlPath))
      {
         BpmValidationError error = BpmValidationError.APP_UNDEFINED_HTML_PATH_FOR_JSP_APPLICATION.raise();
         inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      return inconsistencies;
   }
}
