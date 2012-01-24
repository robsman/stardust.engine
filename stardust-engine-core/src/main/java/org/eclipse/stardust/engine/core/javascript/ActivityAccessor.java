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

import java.util.HashMap;

import org.eclipse.stardust.engine.api.model.Activity;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class ActivityAccessor extends ScriptableObject
{

   private static final long serialVersionUID = -4033274720100629286L;

   private Activity activity;

   private HashMap<String, ActivityMethodCallable> callableCache;

   public ActivityAccessor(Activity activity)
   {
      this.activity = activity;
      this.callableCache = new HashMap<String, ActivityMethodCallable>();
   }

   public Object getDefaultValue(Class hint)
   {
      return null;
   }

   public boolean has(String name, Scriptable start)
   {
      return true;
   }

   public Object get(String name, Scriptable start)
   {
      ActivityMethodCallable callable = callableCache.get(name);
      if (callable == null)
      {
         callable = new ActivityMethodCallable(activity, name);
         callableCache.put(name, callable);
      }
      return callable;
   }

   public void put(String name, Scriptable start, Object value)
   {
      // do nothing, EnumValues values can not (yet) be changed by Javascript
   }

   public String getClassName()
   {
      return "ActivityMethodCallable.class";
   }

   protected Object equivalentValues(Object val)
   {
      return super.equivalentValues(val);
   }

}
