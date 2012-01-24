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
package org.eclipse.stardust.engine.core.javascript;

import java.util.List;

import org.eclipse.stardust.engine.core.struct.TypedXPath;


/**
 * @author sauer
 * @version $Revision$
 */
public class ListValuedXPathAdapter extends TypedXPath
{
   private static final long serialVersionUID = -6992350402326994456L;
   
   private final TypedXPath delegate;

   public ListValuedXPathAdapter(TypedXPath delegate)
   {
      super(delegate.getParentXPath(), delegate.getOrderKey(), delegate.getXPath(),
            delegate.getXsdTypeName(), delegate.getXsdTypeNs(), delegate.getType(),
            false, delegate.getAnnotations());
      
      this.delegate = delegate;
   }

   public TypedXPath getChildXPath(String name)
   {
      return delegate.getChildXPath(name);
   }

   public List getChildXPaths()
   {
      return delegate.getChildXPaths();
   }
   
}
