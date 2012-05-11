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
package org.eclipse.stardust.common.utils.ejb;

import org.eclipse.stardust.common.StringKey;

/**
 * @author rsauer
 * @version $Revision$
 */
public class J2eeContainerType extends StringKey
{
   public static final J2eeContainerType EJB = new J2eeContainerType("EJB", "EJB");
   public static final J2eeContainerType WEB = new J2eeContainerType("WEB", "WEB");
   public static final J2eeContainerType POJO = new J2eeContainerType("POJO", "POJO");

   protected J2eeContainerType(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
