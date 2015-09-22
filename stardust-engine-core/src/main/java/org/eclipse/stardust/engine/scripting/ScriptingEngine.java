package org.eclipse.stardust.engine.scripting;

import java.io.Reader;
import java.io.StringReader;

import javax.script.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.tools.shell.Global;

public class ScriptingEngine extends AbstractScriptEngine
      implements javax.script.Invocable
{
   static class ScriptingEngineContextFactory extends ContextFactory
   {
      protected Context makeContext()
      {
         Context cx = super.makeContext();
         cx.setOptimizationLevel(-1);
         cx.setLanguageVersion(Context.VERSION_1_7);

         return cx;
      }
   }

   static
   {
      ContextFactory.initGlobal(new ScriptingEngineContextFactory());
   }

   private ScriptEngineFactory factory;

   ScriptableObject topLevel;

   public ScriptingEngine(ScriptEngineFactory factory)
   {
      this.factory = factory;
      this.context = Context.enter();
      topLevel = new Global(this.context);
   }

   private Context context;

   public Object eval(String script, ScriptContext context) throws ScriptException
   {
      if (script == null)
      {
         throw new NullPointerException("null script");
      }
      return eval(new StringReader(script), context);
   }

   public Object eval(Reader reader, ScriptContext context) throws ScriptException
   {

      Scriptable runtimeScope = new ExternalScriptable(context);
      runtimeScope.setPrototype(topLevel);
      runtimeScope.put("context", runtimeScope, context);

      Object ret = null;
      Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
      for (String key : bindings.keySet())
      {

         Object wrappedObject = Context.javaToJS(bindings.get(key), runtimeScope);
         ScriptableObject.putProperty(runtimeScope, key, wrappedObject);
      }
      try
      {
         ret = this.context.evaluateReader(runtimeScope, reader, "IPP", 1, null);
      }
      catch (Exception e)
      {
         throw new ScriptException(e);
      }
      finally
      {
         Context.exit();
      }
      return unwrapReturnValue(ret);
   }

   Object unwrapReturnValue(Object result)
   {
      if (result instanceof Wrapper)
      {
         result = ((Wrapper) result).unwrap();
      }

      return result instanceof Undefined ? null : result;
   }

   public Bindings createBindings()
   {
      return new SimpleBindings();
   }

   @Override
   public Object invokeMethod(Object thiz, String name, Object... args)
         throws ScriptException, NoSuchMethodException
   {
      throw new ScriptException("Method not supported");
   }

   @Override
   public Object invokeFunction(String name, Object... args)
         throws ScriptException, NoSuchMethodException
   {
      throw new ScriptException("Method not supported");
   }

   @Override
   public <T> T getInterface(Class<T> clasz)
   {
      throw new RuntimeException("Method not supported");
   }

   @Override
   public <T> T getInterface(Object thiz, Class<T> clasz)
   {
      throw new RuntimeException("Method not supported");
   }

   @Override
   public ScriptEngineFactory getFactory()
   {

      return factory;
   }
}
