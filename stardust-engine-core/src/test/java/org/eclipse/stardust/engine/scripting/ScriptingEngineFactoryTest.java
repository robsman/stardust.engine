package org.eclipse.stardust.engine.scripting;

import static org.junit.Assert.*;
import javax.script.ScriptEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScriptingEngineFactoryTest
{
   private ScriptingEngineFactory fact;

   @Before
   public void setup()
   {
      this.fact = new ScriptingEngineFactory();
   }

   @After
   public void destroy()
   {
      this.fact = null;
   }

   @Test
   public void testEngineName()
   {
      assertEquals("ECMA Script", fact.getParameter(ScriptEngine.NAME));
   }

   @Test
   public void testEngine()
   {
      assertEquals("Stardust Script Engine", fact.getParameter(ScriptEngine.ENGINE));
   }

   @Test
   public void testEngineVersion()
   {
      assertEquals("1.7.R5", fact.getParameter(ScriptEngine.ENGINE_VERSION));
   }

   @Test
   public void testLanguage()
   {
      assertEquals("ECMAScript", fact.getParameter(ScriptEngine.LANGUAGE));
   }

   @Test
   public void testLanguageVersion()
   {
      assertEquals("1.7", fact.getParameter(ScriptEngine.LANGUAGE_VERSION));
   }
}
