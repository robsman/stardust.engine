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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

public class JavaScriptWrapFactory extends WrapFactory
{

   public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType)
   {
      if (obj instanceof Character) {
         Character c = (Character)obj;
         return c.toString();         
      }
      return super.wrap(cx, scope, obj, staticType);
   }

}
