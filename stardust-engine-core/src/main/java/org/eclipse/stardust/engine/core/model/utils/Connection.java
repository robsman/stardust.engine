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
package org.eclipse.stardust.engine.core.model.utils;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Connection extends ModelElement
{
   ModelElement getFirst();

   ModelElement getSecond();

   void setFirst(ModelElement first);

   void setSecond(ModelElement second);

   void connect(String leftRole, String rightRole);

   void attachEndPoint(ModelElement endPoint, String role);
}
