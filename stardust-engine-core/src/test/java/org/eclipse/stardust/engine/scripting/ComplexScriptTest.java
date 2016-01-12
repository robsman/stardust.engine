package org.eclipse.stardust.engine.scripting;

import static org.eclipse.stardust.engine.scripting.Constants.*;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

public class ComplexScriptTest
{
   private ScriptEngineManager manager;

   private ScriptEngine engine;

   @Before
   public void before()
   {
      manager = new ScriptEngineManager();
      engine = manager.getEngineByName(ENGINE_NAME);
   }

   @After
   public void after()
   {

   }

   @Test
   public void testSimpleStringConcatenation() throws ScriptException, IOException
   {
      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      engine.put("input", "abc");
      engine.eval("var output='';\n output = 'Hello '+ input;", bindings);
      assertEquals("Hello abc", Context.jsToJava(engine.get("output"), String.class));
      assertEquals("abc", engine.get("input"));
   }

   @Test(expected = ScriptException.class)
   public void testInvalidScript() throws ScriptException, IOException
   {
      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      engine.put("input", "abc");
      engine.eval("var output='';\n output = 'Hello '+ intput;", bindings);
   }

   @Test
   public void testScriptFunctionsAndMethods()
         throws ScriptException, IOException, NoSuchMethodException
   {
      String script = "function hello(name) { return 'Hello, ' + name; } var output=hello('abc');";
      engine.eval(script);
      assertEquals("Hello, abc", Context.jsToJava(engine.get("output"), String.class));
   }

   @Test(expected = ScriptException.class)
   public void testScriptFunctionsAndMethods2()
         throws ScriptException, IOException, NoSuchMethodException
   {
      engine.eval("function getWelcomeMessage(name){return 'hello '+name;};");
      Object[] params = {"Wolrd"};
      Invocable invEngine = (Invocable) engine;
      invEngine.invokeFunction("getWelcomeMessage", params[0]);
   }
}
