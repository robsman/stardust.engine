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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class ActivityInstanceAccessor extends ScriptableObject
{

   private static final long serialVersionUID = -8320997129161381845L;

   private ActivityInstance activityInstance;

   private HashMap<String, ActivityInstanceMethodCallable> callableCache;

   public ActivityInstanceAccessor(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
      this.callableCache = new HashMap<String, ActivityInstanceMethodCallable>();
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
      ActivityInstanceMethodCallable callable = callableCache.get(name);
      if (callable == null)
      {
         callable = new ActivityInstanceMethodCallable(activityInstance, name);
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
      return "ActivityInstanceMethodCallable.class";
   }

   protected Object equivalentValues(Object val)
   {
      return super.equivalentValues(val);
   }

}
