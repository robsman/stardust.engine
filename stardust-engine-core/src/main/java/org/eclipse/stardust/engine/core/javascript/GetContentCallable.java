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

import java.util.Map;

import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 * Resposible for executing the "getContent()" method call on a 
 * composite type 
 */
public class GetContentCallable implements Callable
{

   private Map map;

   public GetContentCallable(Map map)
   {
      this.map = map;
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj,
         Object[] args)
   {
      // assume this is only called on getContent()

      // check argument count
      if (args.length != 0)
      {
         throw new RuntimeException("The method call to getContent() must contain zero arguments");
      } 
      else
      {
         return this.map.get(StructuredDataConverter.NODE_VALUE_KEY);
      }
   }

}
