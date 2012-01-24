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
package org.eclipse.stardust.common.config;

import java.util.Map;
import java.util.Stack;

import javax.naming.Context;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ParametersFacade extends Parameters
{
   private ContextParameters contexts;
   private LayerParameters layers;
   private Stack<GlobalParameters> globals;

   public ParametersFacade()
   {
      this.contexts = new ContextParameters();
      this.layers = new LayerParameters();
      this.globals = new Stack<GlobalParameters>();
   }

   public Object get(String name)
   {
      Object value = layers.get(name);
      if (value == null)
      {
         value = contexts.get(name);
         if (value == null)
         {
            GlobalParameters params = getGlobals();
            value = params.get(name);
         }
      }
      return value;
   }

   public void set(String name, Object value)
   {
      GlobalParameters globals = getGlobals();
      globals.set(name, value);
   }

   public void flush()
   {
      GlobalParameters globals = getGlobals();
      globals.flush();
   }

   public void addProperties(String fileName)
   {
      GlobalParameters globals = getGlobals();
      globals.addProperties(fileName);
   }

   public static void pushGlobals()
   {
      ParametersFacade facade = (ParametersFacade) Parameters.instance();

      GlobalParameters globals = GlobalParameters.acquire();
      facade.globals.push(globals);
   }

   public static void popGlobals()
   {
      ParametersFacade facade = (ParametersFacade) Parameters.instance();

      // keep thread binding of current instance for the duration of cleanup to prevent
      // unnecessary bootstrapping
      GlobalParameters globals = facade.globals.peek();
      GlobalParameters.release(globals);
      facade.globals.pop();
   }
   
   protected GlobalParameters getGlobalsFromFacade()
   {
      return !globals.isEmpty() ? (GlobalParameters) globals.peek() : null;
   }

   public static PropertyLayer pushLayer(Map<String, ?> props)
   {
      return pushLayer(Parameters.instance(), props);
   }
   
   public static PropertyLayer pushLayer(Parameters params, Map<String, ?> props)
   {
      return pushLayer(params, null, props);
   }

   public static PropertyLayer pushLayer(Parameters params,
         PropertyLayerFactory layerFactory, Map<String, ?> props)
   {
      ParametersFacade facade = (ParametersFacade) params;
      return facade.layers.pushLayer(layerFactory, props);
   }

   public static void popLayer()
   {
      popLayer(Parameters.instance());
   }
   
   public static void popLayer(Parameters params)
   {
      ParametersFacade facade = (ParametersFacade) params;
      facade.layers.popLayer();
   }

   public static void setGlobalContext(Context context, String scope)
   {
      ((ParametersFacade) Parameters.instance()).contexts.setGlobalContext(context, scope);
   }

   public static ContextCache getCachedContext(Parameters params, String scope)
   {
      return ((ParametersFacade) params).contexts.getCachedContext(params, scope);
   }

   public static void pushContext(Parameters params, Context context, String scope)
   {
      ((ParametersFacade) params).contexts.pushContext(context, scope);
   }

   public static void pushContext(Parameters params, ContextCache context)
   {
      ((ParametersFacade) params).contexts.pushContext(context);
   }

   public static void popContext(Parameters params)
   {
      ((ParametersFacade) params).contexts.popContext();
   }

   private GlobalParameters getGlobals()
   {
      return globals.isEmpty() ? GlobalParameters.globals() :
            (GlobalParameters) globals.peek();
   }
}
