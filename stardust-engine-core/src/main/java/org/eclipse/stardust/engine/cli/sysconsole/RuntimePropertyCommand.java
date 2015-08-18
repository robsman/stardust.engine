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
package org.eclipse.stardust.engine.cli.sysconsole;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RuntimePropertyCommand extends AuditTrailCommand
{
   public static final String PROP_LIST = "list";

   public static final String PROP_GET = "get";

   public static final String PROP_SET = "set";

   public static final String PROP_DEL = "delete";

   public static final String PROP_VAL = "value";

   public static final String PROP_LOCALE = "locale";

   private static final Options argTypes = new Options();

   static
   {
      argTypes.register("-" + PROP_LIST, "-l", PROP_LIST,
            "Lists existing runtime properties.", false);
      argTypes.register("-" + PROP_GET, "-g", PROP_GET,
            "Retrieves the current value of a runtime property.", true);
      argTypes.register("-" + PROP_SET, "-s", PROP_SET,
            "Sets the (new) value of a runtime property.", true);
      argTypes.register("-" + PROP_DEL, "-d", PROP_DEL, "Deletes a runtime property.",
            true);
      argTypes.register("-" + PROP_VAL, "-v", PROP_VAL,
            "Specifies the new value of a runtime property.", true);
      argTypes.register("-" + PROP_LOCALE, null, PROP_LOCALE,
            "Specifies the locale of the runtime property.", true);

      argTypes.addExclusionRule(new String[] {PROP_LIST, PROP_GET, PROP_SET, PROP_DEL},
            true);
      argTypes.addInclusionRule(PROP_SET, PROP_VAL);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int doRun(Map options)
   {
      if (options.containsKey(PROP_LIST))
      {
         for (Iterator i = SchemaHelper.listAuditTrailProperties().entrySet().iterator(); i.hasNext();)
         {
            Map.Entry element = (Map.Entry) i.next();
            print(element.getKey() + " = " + element.getValue());
         }
      }
      else if (options.containsKey(PROP_GET))
      {
         String propName = (String) options.get(PROP_GET);
         print(propName + " = " + SchemaHelper.getAuditTrailProperty(propName));
      }
      else if (options.containsKey(PROP_SET))
      {
         String propName = (String) options.get(PROP_SET);
         String propVal = (String) options.get(PROP_VAL);
         SchemaHelper.setAuditTrailProperty(propName, propVal);
         print("Property '" + propName + "' was set to value '" + propVal + "'.");
      }
      else if (options.containsKey(PROP_DEL))
      {
         String propName = (String) options.get(PROP_DEL);
         SchemaHelper.deleteAuditTrailProperty(propName);
         print("Property '" + propName + "' was deleted.");
      }

      return 0;
   }

   public void printCommand(Map options)
   {
      print("Accessing audit trail properties:\n");
   }

   public String getSummary()
   {
      return "Inspect and change audit trail properties.";
   }
}
