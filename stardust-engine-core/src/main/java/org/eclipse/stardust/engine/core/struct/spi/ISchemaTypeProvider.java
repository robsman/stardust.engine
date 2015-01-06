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

import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;



/**
 * @author rsauer
 * @version $Revision$
 */
public interface ISchemaTypeProvider
{

   Set /*<TypedXPath>*/ getSchemaType(AccessPoint accessPoint);
   Set /*<TypedXPath>*/ getSchemaType(String dataTypeId, Map parameters);
   
   interface Factory
   {
      ISchemaTypeProvider getSchemaTypeProvider(String dataTypeId);
      ISchemaTypeProvider getSchemaTypeProvider(AccessPoint accessPoint);
   }
}
