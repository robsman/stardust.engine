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
 * Decorate a list valued XPath to enable element access.
 *
 * @author sauer
 * @version $Revision$
 */
public class ListValuedXPathAdapter extends TypedXPath
{
   private static final long serialVersionUID = -6992350402326994456L;
   
   private final TypedXPath delegate;

   public ListValuedXPathAdapter(TypedXPath delegate)
   {
      super(delegate);
      
      this.delegate = delegate;
   }
   @Override
   public boolean isList()
   {
      return false;
   }

   public TypedXPath getChildXPath(String name)
   {
      // delegate to ensure we keep up with latest child list
      return delegate.getChildXPath(name);
   }

   public List getChildXPaths()
   {
      // delegate to ensure we keep up with latest child list
      return delegate.getChildXPaths();
   }
   
}
