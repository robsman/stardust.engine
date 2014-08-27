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
package org.eclipse.stardust.engine.spring.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.spring.AbstractSpringServiceBean;


import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CodeGen
{
   public String createSpringServiceBean(String longServiceName,
         String longPathJavaSourceName, String destPackage)
   {
      int dot = longServiceName.lastIndexOf(".");
      String packageName = longServiceName.substring(0, dot);
      String javaSourceName = longServiceName.substring(dot + 1);

      StringBuffer createMethod = new StringBuffer();

      createMethod.append("\tpublic " + javaSourceName + "Bean()\n");
      createMethod.append("\t{\n");
      createMethod.append("      super(").append(longServiceName + ".class,\n");
      createMethod.append("            ").append(packageName.replace(".api.", ".core.")).append(".beans.").
         append(javaSourceName).append("Impl.class);\n");
      createMethod.append("\t}");

      String[] additionalMethods = new String[] {createMethod.toString()};
      String newPackage = StringUtils.replace(destPackage.substring(1), "/", ".");

      return createSource(newPackage + javaSourceName + "Bean", longServiceName,
            longPathJavaSourceName, null,
            AbstractSpringServiceBean.class.getName(),
            new String[]
            {
               "I" + javaSourceName
            },//
            new SpringServiceBeanMethodGenerator(longServiceName), additionalMethods,
            true);
   }

   private StringBuffer createFileHeader(String revision)
   {
      StringBuffer result = new StringBuffer("/*\n * Generated from " + revision);
      result.append("\n */\n");
      return result;
   }

   public String createSource(String longServiceName, String originalServiceName,
         String longPathJavaSourceName, String[] imports, String superClass,
         String[] superInterfaces, Functor methodGenerator, String[] additionalMethods,
         boolean isClass)
   {
      List<String> importList = new ArrayList<String>();
      List<String> longNameList = new ArrayList<String>();

      JavaDocBuilder builder = new JavaDocBuilder();

      try
      {
         builder.addSource(new FileReader(longPathJavaSourceName));
      }
      catch (Exception e)
      {
      }
      JavaSource src = builder.getSources()[0];

      JavaClass cls = src.getClasses()[0];

      DocletTag versiontag;
      String revision;
      if ((versiontag = cls.getTagByName("version")) != null)
      {
         revision = versiontag.getValue();
         if ( !StringUtils.isEmpty(revision))
         {
            revision = revision.trim();
            if (revision.startsWith("$"))
            {
               revision = revision.substring(1);
            }
            if (revision.endsWith("$"))
            {
               revision = revision.substring(0, revision.length() - 1);
            }
         }
      }
      else
      {
         revision = "";
      }
      StringBuffer result = createFileHeader(revision);

      String packageName = splitBeforeLastWord('.',longServiceName);
      String newClassName = splitToLastWord('.',longServiceName);

      result.append("package ").append(packageName).append(";\n\n");
      if (imports != null && imports.length > 0)
      {
         for (int i = 0; i < imports.length; i++)
         {
            String importString = imports[i];
            result.append("import ").append(importString).append(";\n");
         }
         result.append("\n");
      }
      result.append(getJavaDocClassString(cls));

      if (isClass)
         result.append("public class ").append(newClassName);
      else
         result.append("public interface ").append(newClassName);
      if (superClass != null)
      {
         result.append(" extends ").append(superClass);
      }
      if (superInterfaces != null && superInterfaces.length > 0)
      {
         if (isClass)
            result.append(" implements ");
         else
            result.append(" extends ");
         for (int i = 0; i < superInterfaces.length; i++)
         {
            String superInterface = superInterfaces[i];
            result.append(superInterface);
            if (i != superInterfaces.length - 1)
            {
               result.append(", ");
            }
         }
      }
      result.append("\n{\n");

      JavaMethod[] methods = cls.getMethods();

      String[] origImports = src.getImports();

      for (int i = 0; i < origImports.length; i++)
      {
         importList.add(splitToLastWord('.',origImports[i]));
         longNameList.add(origImports[i]);
      }

      String returnValue, shortReturnValue;
      for (int i = 0; i < methods.length; i++)
      {
         importList.add(methods[i].getName());
         longNameList.add(packageName+"."+methods[i].getName());
         returnValue = methods[i].getReturns().getGenericValue();
         shortReturnValue = splitToLastWord('.', returnValue);
         if (shortReturnValue.length() > 0)
         {
            if (!importList.contains(shortReturnValue))
            {
               importList.add(shortReturnValue);
               longNameList.add(returnValue);
            }
         }
      }

      for (int i = 0; i < methods.length; i++)
      {
         result.append("\n");
         result.append(methodGenerator.execute(methods[i], originalServiceName, importList,
               longNameList));
      }

      if (additionalMethods != null && additionalMethods.length > 0)
      {
         for (int i = 0; i < additionalMethods.length; i++)
         {
            String additionalMethod = additionalMethods[i];
            result.append("\n").append(additionalMethod).append("\n");
         }
      }
      result.append("}");
      return result.toString();

   }

   public static String getMethodString(JavaMethod method)
   {
      StringBuffer result = new StringBuffer();

      Type mReturns = method.getReturns();

      JavaParameter[] parameters = method.getParameters();

      result.append("   public ").append(mReturns.toGenericString()).append(" ");
      result.append(method.getName()).append("(");

      for (int j = 0; j < parameters.length; j++)
      {
         JavaParameter parameter = parameters[j];
         result.append(parameter.getType().toString().replace('$', '.')).append(
               " " + parameters[j].getName());
         if (j != parameters.length - 1)
         {
            result.append(", ");
         }
      }
      result.append(")");

      Type[] exceptions = method.getExceptions();

      if (exceptions.length > 0)
      {
         result.append("\nthrows ");
         for (int i = 0; i < exceptions.length; i++)
         {
            if (0 < i)
            {
               result.append(", ");
            }

            Type exception = exceptions[i];
            result.append(exception.toString());
         }
      }
      return result.toString();
   }

   public static void main(String[] args) throws IOException, ClassNotFoundException
   {
      String root = ".";
      String destPackage = "/org/eclipse/stardust/engine/api/spring/";

      if (args.length > 0)
      {
         root = args[0];
      }

      String destinationRoot = root;

      if (args.length > 1)
      {
         destinationRoot = args[1];
      }

     if (args.length > 2)
      {
         for (int i = 2; i < args.length; i++)
         {
            createSources(root, destinationRoot, destPackage, args[i]);
         }
      }
      else
      {
         createSources(root, destinationRoot, destPackage, "WorkflowService");
         createSources(root, destinationRoot, destPackage, "UserService");
         createSources(root, destinationRoot, destPackage, "AdministrationService");
         createSources(root, destinationRoot, destPackage, "QueryService");
         createSources(root, destinationRoot, destPackage, "DocumentManagementService");
      }
   }

   private static void createSources(String root, String destinationRoot,
         String destPackage, String serviceName) throws IOException, FileNotFoundException
   {
      String longServiceName = "org.eclipse.stardust.engine.api.runtime." + serviceName;
      String longPathJavaSourceName = root + "/org/eclipse/stardust/engine/api/runtime/"
            + serviceName + ".java";
      CodeGen generator = new CodeGen();

      String remoteServiceImpl = generator.createSpringServiceBean(longServiceName,
            longPathJavaSourceName, destPackage);
      FileWriter writer = new FileWriter(new File(destinationRoot + destPackage
            + serviceName + "Bean.java"));
      writer.write(remoteServiceImpl);
      writer.close();
   }

   public interface Functor
   {
      Object execute(JavaMethod source, String longServiceName, List<String> importList,
            List<String> longNameList);
   }

   private class SpringServiceBeanMethodGenerator implements Functor
   {
      private String service;

      public SpringServiceBeanMethodGenerator(String service)
      {
         this.service = service;
      }

      public Object execute(JavaMethod method, String longServiceName, List<String> importList,
            List<String> longNameList)
      {
         Type mReturns = method.getReturns();
         Type[] exceptions = method.getExceptions();

         StringBuffer result = new StringBuffer(getJavaDocMethodString(method,
               longServiceName, exceptions, importList, longNameList));

         result = result.append(splitLongLines(getMethodString(method), -1, 9));

         result.append("\n   {\n");
         int tabReturnLength = 6;

         StringBuffer addString = new StringBuffer();

         for (int i = 0; i < tabReturnLength; i++) addString.append(" ");
         if (!"void".equals(mReturns.getValue()))
         {
            addString.append("return ");
         }

         addString.append("((").append(service).append(") serviceProxy).").append(
               method.getName()).append("(");

         JavaParameter[] parameters = method.getParameters();

         for (int j = 0; j < parameters.length; j++)
         {
            addString.append(parameters[j].getName());
            if (j != parameters.length - 1)
            {
               addString.append(", ");
            }
         }

         addString.append(");");

         result.append(splitLongLines(addString.toString(), -1, tabReturnLength + 6)).
            append("\n");

         result.append("   }\n");
         return result;
      }
   }

   private String getJavaDocClassString(JavaClass cls)
   {
      StringBuffer docString = new StringBuffer();
      docString.append("/**\n");
      String tagComment;

      String comment = cls.getComment();
      if (comment != null)
      {
         comment = splitLongLines(comment, 2, 3);
         docString.append(" * " + comment + "\n *\n");
      }
      DocletTag[] tags = cls.getTags();
      for (int i = 0; i < tags.length; i++)
      {
         if ( !"version".equals(tags[i].getName()))
         {
            tagComment = splitLongLines(tags[i].getValue(), 2,
                  tags[i].getName().length() + 5);
            docString.append(" * @" + tags[i].getName() + " " + tagComment + "\n");
         }
         else
         {
            String revision = tags[i].getValue();
            if ( !StringUtils.isEmpty(revision))
            {
               revision = revision.trim();
               if (revision.startsWith("$Revision:"))
               {
                  revision = revision.substring("$Revision:".length());
               }
               if (revision.endsWith("$"))
               {
                  revision = revision.substring(0, revision.length() - 1);
               }
               docString.append(" * @" + tags[i].getName() + " " + revision.trim() + "\n");
            }
         }
      }

      docString.append(" */\n");
      return docString.toString();

   }

   private String getJavaDocMethodString(JavaMethod method, String longServiceName,
         Type[] exceptions, List<String> importList, List<String> longNameList)
   {
      StringBuffer buffer = new StringBuffer(200);
      buffer.append("   /**\n")
            .append("    * @see ").append(longServiceName).append("#").append(method.getName()).append(getParameterNameList(method)).append("\n")
            .append("    */\n");

      return buffer.toString();
   }

   private static String splitLongLines(String comment, int commentStarPos, int startTabLength)
   {
      StringBuffer docString = new StringBuffer();
      String addString;

      comment = comment.replaceAll("\\r", "");

      int commentLength = comment.length();
      int countCharPerLine;

      countCharPerLine = startTabLength;

      int i = 0;

      while (i < commentLength)
      {
         addString = splitFromIndex('\n', comment, i);
         if (addString != "")
         {
            countCharPerLine = countCharPerLine + addString.length();
            if (countCharPerLine > 90)
            {
               docString.append(createNewLine(addString, commentStarPos, startTabLength));
            }
            else
            {
               docString.append(addString);
            }
            countCharPerLine = startTabLength;
            docString.append(newLine(commentStarPos, startTabLength));
         }
         else
         {
            addString = comment.substring(i);
            countCharPerLine = countCharPerLine + addString.length();
            if (countCharPerLine > 90)
            {
               addString = createNewLine(addString, commentStarPos, startTabLength);
               countCharPerLine = startTabLength + addString.length();
            }
            docString.append(addString);
         }
       i = i + addString.length() + 1;
      }
      return docString.toString();
   }

   private static String createNewLine(String line, int commentStarPos, int tabStartLength)
   {
      String newLineString = line;
      String oldLineString;
      String addString;

      int newLineLength;

      do
      {
         oldLineString = newLineString;
         newLineString = splitBeforeLastWord(' ', oldLineString);
         newLineLength = newLineString.length();
      } while ((newLineLength + tabStartLength) > 90);

      String testSplitString = splitBeforeLastWord('(',oldLineString);

      if (newLineLength == 0)
      {
         newLineString = oldLineString;
         newLineLength = oldLineString.length();
      }

      int testSplitLength = testSplitString.length();

      if ((testSplitLength > 0)&&(testSplitLength < newLineLength))
      {
         addString = splitToLastWord('(', line);
         newLineString = testSplitString + "(";
      }
      else
      {
         addString = line.substring(newLineLength + 1, line.length());
      }
      if ((addString.length() + tabStartLength) > 90)
      {
         addString = splitLongLines(addString, commentStarPos, tabStartLength);
      }
      if (addString.length() != 0)
      {
         newLineString = newLineString + newLine(commentStarPos, tabStartLength)
               + addString;
      }

      return newLineString;
   }

   private StringBuffer getParameterNameList(JavaMethod method)
   {
      JavaParameter[] parameters = method.getParameters();
      JavaParameter parameter;
      StringBuffer result = new StringBuffer("(");
      Type ptype;

      for (int j = 0; j < parameters.length; j++)
      {
         parameter = parameters[j];
         ptype = parameter.getType();
         result.append(ptype.toString()).append(" " + parameters[j].getName());
         if (j != parameters.length - 1)
         {
            result.append(", ");
         }
      }
      result.append(")");
      return result;
   }

   private static String newLine(int commentStarPos, int tabLength)
   {
      StringBuffer result = new StringBuffer();

      result.append("\n");
      for (int j = 0; j < tabLength; j++)
         result.append(" ");
      if (commentStarPos != -1)
      {
         result.setCharAt(commentStarPos, '*');
      }

      return result.toString();
   }

   private static String splitFromIndex(char splitChar, String splitString,
         int startIndex)
   {
      int splitIndex = splitString.indexOf(splitChar, startIndex);
      if (-1 != splitIndex)
      {
         return(splitString.substring(startIndex, splitIndex));
      }
      else
      {
         return "";
      }
   }

   private static String splitToLastWord(char splitChar, String splitString)
   {
      int splitIndex = splitString.lastIndexOf(splitChar);
      if (-1 != splitIndex)
      {
         return(splitString.substring(splitIndex + 1));
      }
      else
      {
         return "";
      }
   }

   private static String splitBeforeLastWord(char splitChar, String splitString)
   {
      int splitIndex = splitString.lastIndexOf(splitChar);
      if (splitIndex != -1)
      {
         return (splitString.substring(0, splitIndex));
      }
      else
      {
         return "";
      }
   }

   public static String lookForClassInPackage(String packageName, String className)
   {
      try
      {
         String testPath = packageName + className;

         Class<?> testLinkClass = Class.forName(testPath);

         if (testLinkClass != null)
         {
            return testPath;
         }
         else
         {
            return className;
         }
      }
      catch (ClassNotFoundException cnfe)
      {
         return className;
      }
   }


}
