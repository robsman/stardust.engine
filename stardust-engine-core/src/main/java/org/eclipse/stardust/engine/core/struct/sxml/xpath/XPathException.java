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
package org.eclipse.stardust.engine.core.struct.sxml.xpath;

public class XPathException extends Exception
{
   private static final long serialVersionUID = 1L;

   public XPathException()
   {
      super();
   }

   public XPathException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public XPathException(String message)
   {
      super(message);
   }

   public XPathException(Throwable cause)
   {
      super(cause);
   }

}
