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

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * An <code>AccessPoint</code> is a modelling element where an Application provides
 * access to it's data.
 * <p/>
 * It is used as the endpoint for a data mapping. Basically it exposes
 * a java type to be the end point of a data mapping. Every application keeps a map of all
 * access points identified by the <code>id</code> attribute.
 * 
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface IAccessPoint extends IdentifiableElement, AccessPoint
{
   void setDataType(IDataType type);
}
