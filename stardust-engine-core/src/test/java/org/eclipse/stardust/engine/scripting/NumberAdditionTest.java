package org.eclipse.stardust.engine.scripting;

import static org.eclipse.stardust.engine.scripting.Constants.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.script.*;

/**
 * All numbers in javascript (ECMAScript) are double-precision.
 * 
 * The Number type has exactly 18437736874454810627 (that is, 264−253+3) values,
 * representing the double-precision 64-bit format IEEE 754 values as specified in the
 * IEEE Standard for Binary Floating-Point Arithmetic, except that the 9007199254740990
 * (that is, 253−2) distinct “Not-a-Number” values of the IEEE Standard are represented in
 * ECMAScript as a single special NaN value.
 *
 * 
 */
@RunWith(Parameterized.class)
public class NumberAdditionTest
{
   private String query;

   private double expectedResult;

   public NumberAdditionTest(String parameter, double expectedResult)
   {
      this.query = parameter;
      this.expectedResult = expectedResult;
   }

   @Parameterized.Parameters
   public static Collection< ? > primeNumbers()
   {
      return Arrays
            .asList(new Object[][] {{"2+2", 4d}, {"3+3", 6d}, {"4+4", 8d}, {"5+5", 10d}});
   }

   @Test
   public void testAddition() throws ScriptException
   {
      ScriptEngineManager factory = new ScriptEngineManager();
      ScriptEngine engine = factory.getEngineByName(ENGINE_NAME);
      assertEquals(this.expectedResult, engine.eval(this.query));
   }

}
