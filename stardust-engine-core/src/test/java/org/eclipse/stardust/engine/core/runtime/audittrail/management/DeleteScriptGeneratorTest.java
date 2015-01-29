/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import static org.junit.Assert.*;

import java.io.*;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class DeleteScriptGeneratorTest
{
   static private DeleteScriptGenerator generator;
   
   @BeforeClass
   static public void createScript()
   {
      generator = new DeleteScriptGenerator();
   }

   @Test
   public void testGeneratorOutput()
   {
      // generateDeleteScripts can fail if someone has adjusted the @link ProcessInstanceUtils class
      generator.generateDeleteScripts();
      
      // Check if some delete statements was generated
      List<String> deleteScripts = generator.getDeleteScripts();
      int deleteStmtCount = deleteScripts.size();
      assertNotEquals("Generator hasn't created any Delete statements", 0, deleteStmtCount);
      
      StringWriter writer = new StringWriter();
      generator.writeDeleteScript(writer);
      String generatedScript = writer.getBuffer().toString();
      // Check if we get a script which is not empty
      assertTrue(generatedScript.length() > 0);
      
      BufferedReader reader = new BufferedReader(new StringReader(generatedScript));
      writer = new StringWriter();
      int deletedLines = 0;
      try
      {
         for (String line = reader.readLine(); null != line; line = reader.readLine())
         {
            if(!line.contains("DELETE FROM"))
            {
               writer.append(line).append("\n");
            }
            else
            {
               boolean foundLine = false;
               for (String script : deleteScripts)
               {
                  if(line.contains(script))
                  {
                     deletedLines++;
                     foundLine = true;
                     break;
                  }
               }
               assertTrue("Delete statement '" + line + "' not found in generated script", foundLine);
            }
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      
      // Check if the script contains all the delete statements...
      assertEquals(deleteStmtCount, deletedLines);
      // ...and that they're wrapped into a PL/SQL routine.
      String wrapperScript = writer.getBuffer().toString();
      assertTrue("Script cannot contain only delete statements. They must be wrapped in a procedure.", wrapperScript.length() > 0);
      assertTrue(wrapperScript.contains("CREATE OR REPLACE PACKAGE"));
   }
}
