package org.eclipse.stardust.engine.scripting;
import static org.eclipse.stardust.engine.scripting.Constants.*;
import static org.junit.Assert.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.NativeObject;

public class JavascriptJsonTest
{
   private ScriptEngineManager manager;

   private ScriptEngine engine;

   @Before
   public void before()
   {
      manager = new ScriptEngineManager();
      engine = manager.getEngineByName(ENGINE_NAME);
   }
   @Test
   public void marshall() throws ScriptException{
      StringBuilder script=new StringBuilder();
      script.append("var person={};\n");
      script.append("person.firstName=\"abc\"\n");
      script.append("person.lastName=\"efg\"\n");
      script.append("var jsonString=JSON.stringify(person);\n");
      engine.eval(script.toString());
      assertEquals("{\"firstName\":\"abc\",\"lastName\":\"efg\"}",engine.get("jsonString"));
   }
   @Test
   public void unmarshall() throws ScriptException{
      StringBuilder script=new StringBuilder();
      script.append("var text = '{ \"employees\" : [' +");
      script.append("'{ \"firstName\":\"John\" , \"lastName\":\"Doe\" },' +");
      script.append("'{ \"firstName\":\"Anna\" , \"lastName\":\"Smith\" },' +");
      script.append("'{ \"firstName\":\"Peter\" , \"lastName\":\"Jones\" } ]}';");
      script.append("var obj = JSON.parse(text);");
      engine.eval(script.toString());
      Object obj=engine.get("obj");
      assertNotNull(obj);
      assertTrue(obj instanceof NativeObject);

      //assertEquals("abc", );
   }
}
