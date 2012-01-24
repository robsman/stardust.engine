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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author rsauer
 * @version $Revision$
 */
public class LoggedInUser implements Serializable
{
   private final String account;

   private final Map properties;

   public LoggedInUser(String account, Map properties)
   {
      this.account = account;
      this.properties = Collections.unmodifiableMap(properties);
   }

   public String getUserId()
   {
      return account;
   }

   public Map getProperties()
   {
      return properties;
   }
}