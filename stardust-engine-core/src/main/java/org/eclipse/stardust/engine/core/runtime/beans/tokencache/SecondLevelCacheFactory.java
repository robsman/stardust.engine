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
package org.eclipse.stardust.engine.core.runtime.beans.tokencache;


public class SecondLevelCacheFactory
{
   
   public static final NullTokenManager NULL_TOKEN_MANAGER = new NullTokenManager();
   
   public static ISecondLevelTokenCache createSecondLevelCache()
   {
      return NULL_TOKEN_MANAGER;
   }

}
