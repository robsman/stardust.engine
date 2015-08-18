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
package org.eclipse.stardust.engine.api.ejb3.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.ejb3.beans.AbstractServiceImpl;
import org.eclipse.stardust.engine.core.runtime.ejb.Ejb3ManagedService;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.*;

/**
 * @author ubirkemeyer
 * @version $Revision: 70909 $
 */
public class CodeGen
{
   private static final String TAB = "    ";

   public String createEjb3ServiceBean(String longServiceName,
         String longPathJavaSourceName, String destPackage)
   {
      int dot = longServiceName.lastIndexOf(".");
      String javaSourceName = longServiceName.substring(dot + 1);
      return createEjb3ServiceBean(longServiceName, javaSourceName, "Remote" + javaSourceName,
            longPathJavaSourceName, destPackage, AbstractServiceImpl.class.getCanonicalName());
   }

   public String createEjb3ServiceBean(String longServiceName,
         String longLocalServiceName, String longRemoteServiceName,
         String longPathJavaSourceName, String destPackage, String superClassName)
   {
      int dot = longServiceName.lastIndexOf(".");
      String packageName = longServiceName.substring(0, dot);
      String javaSourceName = longServiceName.substring(dot + 1);

      StringBuffer createMethod = new StringBuffer();

      createMethod.append("\tpublic " + javaSourceName + "Impl()\n");
      createMethod.append("\t{\n");
      createMethod.append("      this.serviceType=")
            .append(longServiceName + ".class;\n");
      createMethod.append("      this.serviceTypeImpl=")
            .append(packageName.replace(".api.", ".core.")).append(".beans.")
            .append(javaSourceName).append("Impl.class;\n");
      createMethod.append("\t}");

      String[] additionalMethods = new String[] {createMethod.toString()};
      String[] imports = new String[] {
            Stateless.class.getCanonicalName(),
            TransactionAttribute.class.getCanonicalName(),
            TransactionAttributeType.class.getCanonicalName()};

      String newPackage = StringUtils.replace(destPackage.substring(1), "/", ".");

      return createSource(newPackage + javaSourceName + "Impl", longServiceName,
            longPathJavaSourceName, imports, superClassName,
            new String[] {longLocalServiceName, longRemoteServiceName},//
            new Ejb3ServiceBeanMethodGenerator(longServiceName), additionalMethods, true,
            false);
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
         boolean isClass, boolean isLocal)
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
         if (!StringUtils.isEmpty(revision))
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

      String packageName = splitBeforeLastWord('.', longServiceName);
      String newClassName = splitToLastWord('.', longServiceName);

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
      {
         result.append("@Stateless").append("\n");
         result.append("@TransactionAttribute(TransactionAttributeType.REQUIRED)")
               .append("\n");
         result.append("public class ").append(newClassName);
      }
      else
      {
         if (isLocal)
         {
            result.append("@Local").append("\n");
         }
         else
         {
            result.append("@Remote").append("\n");
         }
         result.append("public interface ").append(newClassName);
      }
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
         importList.add(splitToLastWord('.', origImports[i]));
         longNameList.add(origImports[i]);
      }

      String returnValue, shortReturnValue;
      for (int i = 0; i < methods.length; i++)
      {
         importList.add(methods[i].getName());
         longNameList.add(packageName + "." + methods[i].getName());
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
         result.append(methodGenerator.execute(methods[i], originalServiceName,
               importList, longNameList));
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
      String baseClass;

      Type mReturns = method.getReturns();

      JavaParameter[] parameters = method.getParameters();

      result.append("   public ").append(mReturns.toGenericString()).append(" ");
      result.append(method.getName()).append("(");

      for (int j = 0; j < parameters.length; j++)
      {
         JavaParameter parameter = parameters[j];
         result.append(parameter.getType().toGenericString().replace('$', '.')).append(
               " " + parameters[j].getName());
         if (j != parameters.length - 1)
         {
            result.append(", ");
         }
      }

      if (parameters.length != 0)
      {
         result.append(", ");
      }
      result.append("org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext");

      result.append(")");

      Type[] exceptions = method.getExceptions();

      result.append("throws org.eclipse.stardust.common.error.WorkflowException");

      if (exceptions.length > 0)
      {
         for (int i = 0; i < exceptions.length; i++)
         {
            Type exception = exceptions[i];
            baseClass = exception.getJavaClass().getSuperClass().getValue();
            if (!baseClass.toString().equals(
                  "org.eclipse.stardust.common.error.PublicException")
                  && !baseClass.toString().equals(
                        "org.eclipse.stardust.common.error.ResourceException"))
            {
               result.append(", " + exception.toString());
            }
         }
      }

      return result.toString();
   }

   public static void main(String[] args) throws IOException, ClassNotFoundException
   {
      String root = ".";
      String destPackage = "/org/eclipse/stardust/engine/api/ejb3/beans/";

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
         String destPackage, String serviceName) throws IOException,
         FileNotFoundException
   {
      String longServiceName = "org.eclipse.stardust.engine.api.runtime." + serviceName;
      String longPathJavaSourceName = root + "/org/eclipse/stardust/engine/api/runtime/"
            + serviceName + ".java";
      CodeGen generator = new CodeGen();

      String businessServiceImpl = generator.createEjb3ServiceBean(longServiceName,
            longPathJavaSourceName, destPackage);
      FileWriter writer = new FileWriter(new File(destinationRoot + destPackage
            + serviceName + "Impl.java"));
      writer.write(businessServiceImpl);
      writer.close();

      // Create Local Interfaces

      String localService = generator.createLocalInterface(longServiceName,
            longPathJavaSourceName, destPackage);
      writer = new FileWriter(new File(destinationRoot + destPackage + serviceName
            + ".java"));
      writer.write(localService);
      writer.close();

      // Create Remote Interfaces inherited from Local Interfaces

      String remoteService = generator.createRemoteInterface(longServiceName,
            longPathJavaSourceName, destPackage);
      writer = new FileWriter(new File(destinationRoot + destPackage + "Remote"
            + serviceName + ".java"));
      writer.write(remoteService);
      writer.close();

   }

   public interface Functor
   {
      Object execute(JavaMethod source, String longServiceName, List<String> importList,
            List<String> longNameList);
   }

   private class Ejb3ServiceBeanMethodGenerator implements Functor
   {
      private String service;

      public Ejb3ServiceBeanMethodGenerator(String service)
      {
         this.service = service;
      }

      public Object execute(JavaMethod method, String longServiceName,
            List<String> importList, List<String> longNameList)
      {
         Type mReturns = method.getReturns();
         Type[] exceptions = method.getExceptions();

         int tabReturnLength;

         StringBuffer result = new StringBuffer(getJavaDocMethodString(method,
               longServiceName, exceptions, importList, longNameList));

         result = result.append(splitLongLines(getMethodString(method), -1, 9, 0));

         result.append("\n").append(TAB).append("{\n");

         result.append("      java.util.Map<?, ?> __invocationContextBackup = null;\n");

         result.append("      try\n      {\n");
         tabReturnLength = 9;

         StringBuffer addString = new StringBuffer();

         for (int i = 0; i < tabReturnLength; i++)
         {
            addString.append(" ");
         }

         addString
               .append("__invocationContextBackup = initInvocationContext(__tunneledContext);");
         result.append(splitLongLines(addString.toString(), -1, 9, 0)).append("\n");

         addString = new StringBuffer();

         for (int i = 0; i < tabReturnLength; i++)
            addString.append(" ");
         if (!mReturns.isVoid())
         {
            addString.append("return ");
         }

         addString.append("((").append(service).append(") service).")
               .append(method.getName()).append("(");

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

         result.append(splitLongLines(addString.toString(), -1, 9, 3)).append("\n");

         result.append("      }\n");
         result.append(
               "      catch(org.eclipse.stardust.common.error.PublicException e)\n")
               .append("      {\n")
               .append(
                     "         throw new org.eclipse.stardust.common.error.WorkflowException(e);\n")
               .append("      }\n");
         result.append(
               "      catch(org.eclipse.stardust.common.error.ResourceException e)\n")
               .append("      {\n")
               .append(
                     "         throw new org.eclipse.stardust.common.error.WorkflowException(e);\n")
               .append("      }\n");

         result.append("      finally\n")
               .append("      {\n")
               .append(
                     "         clearInvocationContext(__tunneledContext, __invocationContextBackup);\n")
               .append("      }\n");

         result.append(TAB).append("}\n");
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
         if (!"version".equals(tags[i].getName()))
         {
            tagComment = splitLongLines(tags[i].getValue(), 2,
                  tags[i].getName().length() + 5);
            docString.append(" * @" + tags[i].getName() + " " + tagComment + "\n");
         }
         else
         {
            String revision = tags[i].getValue();
            if (!StringUtils.isEmpty(revision))
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
               docString
                     .append(" * @" + tags[i].getName() + " " + revision.trim() + "\n");
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
      buffer.append("   /**\n").append("    * @see ").append(longServiceName).append("#")
            .append(method.getName()).append(getParameterNameList(method)).append("\n")
            .append("    */\n");

      return buffer.toString();
   }

   private static String splitLongLines(String comment, int commentStarPos,
         int startTabLength)
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
      }
      while ((newLineLength + tabStartLength) > 90);

      String testSplitString = splitBeforeLastWord('(', oldLineString);

      if (newLineLength == 0)
      {
         newLineString = oldLineString;
         newLineLength = oldLineString.length();
      }

      int testSplitLength = testSplitString.length();

      if ((testSplitLength > 0) && (testSplitLength < newLineLength))
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

   private static String splitFromIndex(char splitChar, String splitString, int startIndex)
   {
      int splitIndex = splitString.indexOf(splitChar, startIndex);
      if (-1 != splitIndex)
      {
         return (splitString.substring(startIndex, splitIndex));
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
         return (splitString.substring(splitIndex + 1));
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

         Class< ? > testLinkClass = Class.forName(testPath);

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

   private static String splitLongLines(String comment, int commentStarPos,
         int startTabLength, int newLineTabShift)
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
               docString.append(createNewLine(addString, commentStarPos, startTabLength,
                     newLineTabShift));
            }
            else
            {
               docString.append(addString);
            }
            countCharPerLine = startTabLength;
            docString.append(newLine(commentStarPos, startTabLength, newLineTabShift));
         }
         else
         {
            addString = comment.substring(i);
            countCharPerLine = countCharPerLine + addString.length();
            if (countCharPerLine > 90)
            {
               addString = createNewLine(addString, commentStarPos, startTabLength,
                     newLineTabShift);
               countCharPerLine = startTabLength + addString.length();
            }
            docString.append(addString);
         }
         i = i + addString.length() + 1;
      }
      return docString.toString();
   }

   private static String createNewLine(String line, int commentStarPos,
         int tabStartLength, int newLineTabShift)
   {
      String newLineString = line;
      String oldLineString;
      String addString;

      int newLineLength;

      do
      {
         oldLineString = newLineString;
         newLineString = splitBeforeLastWord(' ', oldLineString);
         if (commentStarPos > -1)
         {
            newLineString = newLineString.trim();
         }
         newLineLength = newLineString.length();
      }
      while ((newLineLength + tabStartLength) > 90);

      String testSplitString = splitBeforeLastWord('(', oldLineString);

      if (newLineLength == 0)
      {
         newLineString = oldLineString;
         newLineLength = oldLineString.length();
      }

      int testSplitLength = testSplitString.length();

      if ((testSplitLength > 0) && (testSplitLength < newLineLength))
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
         addString = splitLongLines(addString, commentStarPos, tabStartLength,
               newLineTabShift);
      }
      if (addString.length() != 0)
      {
         newLineString = newLineString
               + newLine(commentStarPos, tabStartLength, newLineTabShift) + addString;
      }

      return newLineString;
   }

   private static String newLine(int commentStarPos, int tabLength, int newLineTabShift)
   {
      StringBuffer result = new StringBuffer();

      result.append("\n");
      for (int j = 0; j < tabLength; j++)
         result.append(" ");
      if (commentStarPos != -1)
      {
         for (int j = tabLength; j < commentStarPos; j++)
            result.append(" ");
         result.append("* ");
      }
      while (newLineTabShift > 0)
      {
         result.append(" ");
         newLineTabShift--;
      }

      return result.toString();
   }

   public String createLocalInterface(String longServiceName,
         String longPathJavaSourceName, String destPackage)
   {
      int dot = longServiceName.lastIndexOf(".");
      String javaSourceName = longServiceName.substring(dot + 1);

      List<String> superInterfaces = CollectionUtils.newList();
      superInterfaces.add(Ejb3ManagedService.class.getCanonicalName());

      String[] additionalMethods = StringUtils.EMPTY_STRING_ARRAY;
      String[] imports = new String[] {Local.class.getCanonicalName()};

      String newPackage = StringUtils.replace(destPackage.substring(1), "/", ".");

      final String longWrapperInterfaceName = newPackage + javaSourceName;
      return createSource(longWrapperInterfaceName, longServiceName,
            longPathJavaSourceName, imports, null,
            (String[]) superInterfaces.toArray(StringUtils.EMPTY_STRING_ARRAY),
            (Functor) new LocalServiceMethodGenerator(), additionalMethods, false, true);
   }

   public String createRemoteInterface(String longServiceName,
         String longPathJavaSourceName, String destPackage)
   {
      int dot = longServiceName.lastIndexOf(".");
      String javaSourceName = longServiceName.substring(dot + 1);

      List<String> superInterfaces = CollectionUtils.newList();
      superInterfaces.add(javaSourceName);

      String[] additionalMethods = StringUtils.EMPTY_STRING_ARRAY;
      String[] imports = new String[] {Remote.class.getCanonicalName()};

      String newPackage = StringUtils.replace(destPackage.substring(1), "/", ".");

      final String longWrapperInterfaceName = newPackage + "Remote" + javaSourceName;
      return createSource(longWrapperInterfaceName, longServiceName,
            longPathJavaSourceName, imports, null,
            (String[]) superInterfaces.toArray(StringUtils.EMPTY_STRING_ARRAY),
            (Functor) new RemoteServiceMethodGenerator(), additionalMethods, false, false);
   }

   private class RemoteServiceMethodGenerator implements Functor
   {

      @Override
      public Object execute(JavaMethod source, String longServiceName,
            List<String> importList, List<String> longNameList)
      {
         StringBuffer result = new StringBuffer();
         return result;
      }

   }

   private class LocalServiceMethodGenerator extends AbstractMethodGenerator
         implements Functor
   {
      public LocalServiceMethodGenerator()
      {
         super();
      }

      public Object execute(JavaMethod method, String longServiceName,
            List<String> importList, List<String> longNameList)
      {
         Type[] exceptions = method.getExceptions();
         StringBuffer result = new StringBuffer(getJavaDocMethodString(method,
               longServiceName, exceptions, importList, longNameList));
         StringBuffer addString = new StringBuffer(getMethodString(method));
         addString.append(";\n");
         result.append(splitLongLines(addString.toString(), -1, 9, 0));
         return result;
      }
   }

   private static abstract class AbstractMethodGenerator
   {

      public AbstractMethodGenerator()
      {

      }

      abstract Object execute(JavaMethod source, String longServiceName,
            List<String> importList, List<String> longNameList);

      protected String getMethodString(JavaMethod method)
      {
         StringBuffer result = new StringBuffer();
         String baseClass;

         Type mReturns = method.getReturns();

         JavaParameter[] parameters = method.getParameters();

         result.append(TAB).append("public ").append(mReturns.toGenericString())
               .append(" ");
         result.append(method.getName()).append("(");

         for (int j = 0; j < parameters.length; j++)
         {
            JavaParameter parameter = parameters[j];
            result.append(parameter.getType().toGenericString().replace('$', '.')).append(
                  " " + parameters[j].getName());
            if (j != parameters.length - 1)
            {
               result.append(", ");
            }
         }

         if (parameters.length != 0)
         {
            result.append(", ");
         }
         result.append("org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext");

         result.append(")\n");

         Type[] exceptions = method.getExceptions();

         result.append("throws org.eclipse.stardust.common.error.WorkflowException");
         if (exceptions.length > 0)
         {
            for (int i = 0; i < exceptions.length; i++)
            {
               Type exception = exceptions[i];
               baseClass = exception.getJavaClass().getSuperClass().getValue();
               if (!baseClass.toString().equals(
                     "org.eclipse.stardust.common.error.PublicException")
                     && !baseClass.toString().equals(
                           "org.eclipse.stardust.common.error.ResourceException"))
               {
                  result.append(", " + exception.toString());
               }
            }
         }
         return result.toString();
      }
   }
}
