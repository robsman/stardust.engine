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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import org.w3c.dom.Document;

public class XmlPartHolder
{
   private final Document part;
   
   public XmlPartHolder(Document part)
   {
      this.part = part;
   }
   
   public Document getPart()
   {
      return this.part;
   }
}
