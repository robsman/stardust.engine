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
package org.eclipse.stardust.engine.core.spi.cache;

public class CacheException extends Exception
{

   public CacheException() {
      super ();
  }

  public CacheException(String s) {
      super (s);
  }

  public CacheException(String s, Throwable cause) {
      super (s, cause);
  }

  public CacheException(Throwable cause) {
      super(cause);
  }

   
}
