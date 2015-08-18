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
package org.eclipse.stardust.engine.core.struct.spi;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.sxml.Document;


public class DomTransfomer implements IStructuredDataTransformer
{

   public Object fromStructData(Document document,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter.convert(document,
            DOMImplementationThreadLocal.get()).getDocumentElement();
   }

   public Object toStructData(AccessPoint accessPointDefinition,
         Object accessPointInstance, String outPath,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      throw new UnsupportedOperationException();
   }
}
