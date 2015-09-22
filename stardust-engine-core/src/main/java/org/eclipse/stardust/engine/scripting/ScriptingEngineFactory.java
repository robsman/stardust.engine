package org.eclipse.stardust.engine.scripting;

import static org.eclipse.stardust.engine.scripting.Constants.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.*;

public class ScriptingEngineFactory implements ScriptEngineFactory
{
   private static List<String> names;

   private static List<String> mimeTypes;

   private static List<String> extensions;

   static
   {
      names = new ArrayList<String>();
      names.add(ENGINE_NAME);
      names.add("stardust-rhino-nonjdk");
      // names.add("javascript");
      names = Collections.unmodifiableList(names);

      mimeTypes = new ArrayList<String>(4);
      mimeTypes.add("application/javascript");
      mimeTypes.add("application/ecmascript");
      mimeTypes.add("text/javascript");
      mimeTypes.add("text/ecmascript");
      mimeTypes = Collections.unmodifiableList(mimeTypes);

      extensions = new ArrayList<String>(1);
      extensions.add("js");
      extensions = Collections.unmodifiableList(extensions);
   }

   public String getEngineName()
   {
      return (String) getParameter(ScriptEngine.ENGINE);
   }

   public String getEngineVersion()
   {
      return (String) getParameter(ScriptEngine.ENGINE_VERSION);
   }

   public List<String> getExtensions()
   {
      return extensions;
   }

   public List<String> getMimeTypes()
   {

      return mimeTypes;
   }

   public List<String> getNames()
   {
      return names;
   }

   public String getLanguageName()
   {
      return (String) getParameter(ScriptEngine.LANGUAGE);
   }

   public String getLanguageVersion()
   {
      return (String) getParameter(ScriptEngine.LANGUAGE_VERSION);
   }

   public Object getParameter(String key)
   {
      if (key.equals(ScriptEngine.NAME))
      {
         return "ECMA Script";
      }
      else if (key.equals(ScriptEngine.ENGINE))
      {
         return "Stardust Script Engine";
      }
      else if (key.equals(ScriptEngine.ENGINE_VERSION))
      {
         return "1.7.R5";
      }
      else if (key.equals(ScriptEngine.LANGUAGE))
      {
         return "ECMAScript";
      }
      else if (key.equals(ScriptEngine.LANGUAGE_VERSION))
      {
         return "1.7";
      }
      else
      {
         throw new IllegalArgumentException("Invalid key");
      }
   }

   public String getMethodCallSyntax(String obj, String m, String... args)
   {
      StringBuilder out = new StringBuilder();
      out.append(obj + "." + m + "(");

      int len = args.length;
      if (len == 0)
      {
         out.append(")");
         return out.toString();
      }

      for (int i = 0; i < len; i++)
      {
         out.append(args[i]);
         if (i != len - 1)
         {
            out.append(",");
         }
         else
         {
            out.append(")");
         }
      }
      return out.toString();
   }

   public String getOutputStatement(String toDisplay)
   {
      return "print(" + toDisplay + ")";
   }

   public String getProgram(String... statements)
   {
      int len = statements.length;
      String ret = "";
      for (int i = 0; i < len; i++)
      {
         ret += statements[i] + ";";
      }

      return ret;
   }

   public ScriptEngine getScriptEngine()
   {
      return new ScriptingEngine(this);
   }

}
