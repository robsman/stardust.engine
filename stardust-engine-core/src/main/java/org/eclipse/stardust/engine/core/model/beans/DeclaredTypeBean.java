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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.engine.api.model.IDeclaredType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;

/**
 * @author sauer
 * @version $Revision$
 */
public class DeclaredTypeBean implements IDeclaredType
{
   private final String id;
   private ITypeDeclaration parent;
   
   public DeclaredTypeBean(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public ITypeDeclaration getParent()
   {
      return parent;
   }

   public void setParent(ITypeDeclaration parent)
   {
      this.parent = parent;
   }
}
