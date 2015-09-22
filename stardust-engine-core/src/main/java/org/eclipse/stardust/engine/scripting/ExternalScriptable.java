package org.eclipse.stardust.engine.scripting;

import java.util.ArrayList;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

public class ExternalScriptable implements Scriptable
{
   private ScriptContext context;

   private Scriptable prototype;

   private Scriptable parent;

   private boolean isEmpty(String name)
   {
      return name.equals("");
   }

   public ExternalScriptable(ScriptContext context)
   {
      this.context = context;
   }

   @Override
   public void delete(String name)
   {
      synchronized (context)
      {
         int scope = context.getAttributesScope(name);
         if (scope != -1)
         {
            context.removeAttribute(name, scope);
         }
      }
   }

   @Override
   public void delete(int index)
   {
      // nothing todo
   }

   @Override
   public Object get(String name, Scriptable statr)
   {
      if (isEmpty(name))
      {
         return NOT_FOUND;
      }

      synchronized (context)
      {
         int scope = context.getAttributesScope(name);
         if (scope != -1)
         {
            Object value = context.getAttribute(name, scope);
            return Context.javaToJS(value, this);
         }
         else
         {
            return NOT_FOUND;
         }
      }
   }

   @Override
   public Object get(int arg0, Scriptable arg1)
   {
      return NOT_FOUND;
   }

   @Override
   public String getClassName()
   {
      return "Global";
   }

   @Override
   public Object getDefaultValue(Class< ? > typeHint)
   {
      for (int i = 0; i < 2; i++)
      {
         boolean tryToString;
         if (typeHint == ScriptRuntime.StringClass)
         {
            tryToString = (i == 0);
         }
         else
         {
            tryToString = (i == 1);
         }

         String methodName;
         Object[] args;
         if (tryToString)
         {
            methodName = "toString";
            args = ScriptRuntime.emptyArgs;
         }
         else
         {
            methodName = "valueOf";
            args = new Object[1];
            String hint;
            if (typeHint == null)
            {
               hint = "undefined";
            }
            else if (typeHint == ScriptRuntime.StringClass)
            {
               hint = "string";
            }
            else if (typeHint == ScriptRuntime.ScriptableClass)
            {
               hint = "object";
            }
            else if (typeHint == ScriptRuntime.FunctionClass)
            {
               hint = "function";
            }
            else if (typeHint == ScriptRuntime.BooleanClass || typeHint == Boolean.TYPE)
            {
               hint = "boolean";
            }
            else if (typeHint == ScriptRuntime.NumberClass
                  || typeHint == ScriptRuntime.ByteClass || typeHint == Byte.TYPE
                  || typeHint == ScriptRuntime.ShortClass || typeHint == Short.TYPE
                  || typeHint == ScriptRuntime.IntegerClass || typeHint == Integer.TYPE
                  || typeHint == ScriptRuntime.FloatClass || typeHint == Float.TYPE
                  || typeHint == ScriptRuntime.DoubleClass || typeHint == Double.TYPE)
            {
               hint = "number";
            }
            else
            {
               throw Context.reportRuntimeError(
                     "Invalid JavaScript value of type " + typeHint.toString());
            }
            args[0] = hint;
         }
         Object v = ScriptableObject.getProperty(this, methodName);
         if (!(v instanceof Function))
            continue;
         Function fun = (Function) v;
         Context cx = Context.enter();
         try
         {
            v = fun.call(cx, fun.getParentScope(), this, args);
         }
         finally
         {
            Context.exit();
         }
         if (v != null)
         {
            if (!(v instanceof Scriptable))
            {
               return v;
            }
            if (typeHint == ScriptRuntime.ScriptableClass
                  || typeHint == ScriptRuntime.FunctionClass)
            {
               return v;
            }
            if (tryToString && v instanceof Wrapper)
            {
               // Let a wrapped java.lang.String pass for a primitive
               // string.
               Object u = ((Wrapper) v).unwrap();
               if (u instanceof String)
                  return u;
            }
         }
      }
      // fall through to error
      String arg = (typeHint == null) ? "undefined" : typeHint.getName();
      throw Context.reportRuntimeError("Cannot find default value for object " + arg);
   }

   @Override
   public Object[] getIds()
   {
      ArrayList<String> list = new ArrayList<String>();
      synchronized (context)
      {
         for (int scope : context.getScopes())
         {
            Bindings bindings = context.getBindings(scope);
            if (bindings != null)
            {
               list.ensureCapacity(bindings.size());
               for (String key : bindings.keySet())
               {
                  list.add(key);
               }
            }
         }
      }
      String[] res = new String[list.size()];
      list.toArray(res);
      return res;
   }

   /**
    * Get the parent scope of the object.
    * 
    * @return the parent scope
    */
   @Override
   public Scriptable getParentScope()
   {
      return parent;
   }

   /**
    * Set the parent scope of the object.
    * 
    * @param parent
    *           the parent scope to set
    */
   @Override
   public void setParentScope(Scriptable parent)
   {
      this.parent = parent;
   }

   /**
    * Get the prototype of the object.
    * 
    * @return the prototype
    */
   @Override
   public Scriptable getPrototype()
   {
      return prototype;
   }

   /**
    * Set the prototype of the object.
    * 
    * @param prototype
    *           the prototype to set
    */
   @Override
   public void setPrototype(Scriptable prototype)
   {
      this.prototype = prototype;
   }

   @Override
   public boolean has(String name, Scriptable start)
   {
      if (isEmpty(name))
      {
         return false;
      }
      synchronized (context)
      {
         return context.getAttributesScope(name) != -1;
      }
   }

   @Override
   public boolean has(int index, Scriptable start)
   {
      return false;
   }

   @Override
   public boolean hasInstance(Scriptable instance)
   {
      Scriptable proto = instance.getPrototype();
      while (proto != null)
      {
         if (proto.equals(this))
            return true;
         proto = proto.getPrototype();
      }
      return false;
   }

   @Override
   public void put(String name, Scriptable start, Object value)
   {
      if (start == this)
      {
         synchronized (this)
         {
            if (!isEmpty(name))
            {
               synchronized (context)
               {
                  int scope = context.getAttributesScope(name);
                  if (scope == -1)
                  {
                     scope = ScriptContext.ENGINE_SCOPE;
                  }
                  context.setAttribute(name, jsToJava(value), scope);
               }
            }
         }
      }
      else
      {
         start.put(name, start, value);
      }

   }

   @Override
   public void put(int index, Scriptable start, Object value)
   {
      // not supported

   }

   private Object jsToJava(Object jsObj)
   {
      if (jsObj instanceof Wrapper)
      {
         Wrapper njb = (Wrapper) jsObj;
         /*
          * importClass feature of ImporterTopLevel puts NativeJavaClass in global scope.
          * If we unwrap it, importClass won't work.
          */
         if (njb instanceof NativeJavaClass)
         {
            return njb;
         }

         /*
          * script may use Java primitive wrapper type objects (such as java.lang.Integer,
          * java.lang.Boolean etc) explicitly. If we unwrap, then these script objects
          * will become script primitive types. For example,
          * 
          * var x = new java.lang.Double(3.0); print(typeof x);
          * 
          * will print 'number'. We don't want that to happen.
          */
         Object obj = njb.unwrap();
         if (obj instanceof Number || obj instanceof String || obj instanceof Boolean
               || obj instanceof Character)
         {
            // special type wrapped -- we just leave it as is.
            return njb;
         }
         else
         {
            // return unwrapped object for any other object.
            return obj;
         }
      }
      else
      { // not-a-Java-wrapper
         return jsObj;
      }
   }
}
