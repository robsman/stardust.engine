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
package org.eclipse.stardust.engine.core.spi.extensions.model;

import java.util.Iterator;
import java.util.Map;

/**
 * Provides an abstraction of an entity supporting external data reads or writes via
 * {@link AccessPoint}s.
 * <p />
 * Examples are JavaBeans providing properties or JMS messages providing various
 * attributes.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AccessPointProvider
{
   /**
    * Retrieves the list of {@link AccessPoint}s provided by this entity.
    * <p />
    * The implementation is expected to not throw any exceptions.
    * 
    * @param context Implementation specific context attributes.
    * @param typeAttributes Implementation specific static attributes.
    *
    * @return An {@link Iterator} over the provided access points.
    */
   Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes);
}
