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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsOperationValidator implements ApplicationValidator
{

   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      // TODO (ab) validate dmsId and other properties (depending on operation)

      List inconsistencies = CollectionUtils.newLinkedList();

      if ( !attributes.containsKey(DmsConstants.PRP_OPERATION_NAME))
      {
         BpmValidationError error = BpmValidationError.VAL_DMS_OPERATION_NOT_SET.raise();
         inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
      }

      return inconsistencies;
   }

}
