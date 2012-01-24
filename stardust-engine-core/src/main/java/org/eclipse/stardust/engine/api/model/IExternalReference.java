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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.xsd.XSDSchema;
import org.w3c.dom.Element;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface IExternalReference extends IXpdlType
{
   String getLocation();
   
   String getNamespace();
   
   String getXref();
   
   Element getExternalAnnotations();
   
   XSDSchema getSchema(IModel model);
}
