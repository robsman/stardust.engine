/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.ejb2.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.*;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CodeGen
{
   private static final String TAG_EMPTY = "";
   private static final String TAG_SEE = "see";
   private static final String TAG_THROWS = "throws";

   private static final String TAB = "    ";

   private static final String TUNNELING_PREFIX = "Tunneling";

   public String createRemoteServiceImpl(String longServiceName,
         String longPathJavaSourceName, String destPackage, boolean tunneling)
   {
      int dot = longServiceName.lastIndexOf(".");
      String packageName = longServiceName.substring(0, dot);
      String javaSourceName = longServiceName.substring(dot + 1);

      StringBuffer createMethod = new StringBuffer();

      createMethod.append(TAB).append("public void ejbCreate() throws javax.ejb.CreateException\n");
      createMethod.append(TAB).append("{\n");
      createMethod.append("      super.init(").append(longServiceName + ".class,\n");
      createMethod.append("            ").append(packageName.replace(".api.", ".core.")).append(".beans.").
         append(javaSourceName).append("Impl.class);\n");
      createMethod.append(TAB).append("}");

      String[] additionalMethods = new String[] {createMethod.toString()};

      String interfacePackage = StringUtils.replace(destPackage.substring(1),"/",".");
      if (tunneling)
      {
         interfacePackage += "tunneling.";
      }
      String implPackage = interfacePackage + "beans";

      final String longWrapperImplName = implPackage + "."
            + (tunneling ? TUNNELING_PREFIX : "Remote") + javaSourceName + "Impl";

      String baseClass = tunneling
            ? "org.eclipse.stardust.engine.api.ejb2.tunneling.beans.AbstractTunnelingServiceImpl"
            : "org.eclipse.stardust.engine.api.ejb2.beans.RemoteServiceImpl";

      return createSource(longWrapperImplName, longServiceName, longPathJavaSourceName,
            null, baseClass, new String[] {},
            new RemoteServiceImplMethodGenerator(longServiceName, tunneling),
            additionalMethods, true);
   }

   public String createRemoteService(String longServiceName, String longPathJavaSourceName,
         String destPackage, boolean tunneling)
   {
      int dot = longServiceName.lastIndexOf(".");
      String javaSourceName = longServiceName.substring(dot + 1);

      List<String> superInterfaces = CollectionUtils.newList();
      superInterfaces.add("javax.ejb.EJBObject");

      String[] additionalMethods = StringUtils.EMPTY_STRING_ARRAY;
      if (tunneling)
      {
         superInterfaces.add("org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingRemoteService");
      }
      else
      {
         additionalMethods = new String[] {
               TAB + "void login(java.lang.String userId, java.lang.String password)\n         "
                     + "throws org.eclipse.stardust.common.error.WorkflowException, java.rmi.RemoteException;",
               TAB + "void login(java.lang.String userId, java.lang.String password, java.util.Map properties)\n         "
                     + "throws org.eclipse.stardust.common.error.WorkflowException, java.rmi.RemoteException;",
               TAB + "void logout() throws java.rmi.RemoteException;" };
      }

      String newPackage = StringUtils.replace(destPackage.substring(1),"/",".");

      String tunnelingPrefix = (tunneling ? "tunneling.Tunneling" : "");
      final String longWrapperInterfaceName = newPackage + tunnelingPrefix
            + "Remote" + javaSourceName;
      return createSource(longWrapperInterfaceName, longServiceName,
            longPathJavaSourceName, null, null,
            (String[]) superInterfaces.toArray(StringUtils.EMPTY_STRING_ARRAY),
            new RemoteServiceMethodGenerator(tunneling), additionalMethods, false);
   }

   public String createLocalService(String longServiceName, String longPathJavaSourceName,
         String destPackage, boolean tunneling)
   {
      int dot = longServiceName.lastIndexOf(".");
      String javaSourceName = longServiceName.substring(dot + 1);

      List<String> superInterfaces = CollectionUtils.newList();
      superInterfaces.add("javax.ejb.EJBLocalObject");

      String[] additionalMethods = StringUtils.EMPTY_STRING_ARRAY;
      if (tunneling)
      {
         superInterfaces.add("org.eclipse.stardust.engine.api.ejb2.tunneling.TunnelingLocalService");
      }
      else
      {
         additionalMethods = new String[] {
               TAB + "void login(java.lang.String userId, java.lang.String password)\n         "
                     + "throws org.eclipse.stardust.common.error.WorkflowException;",
               TAB + "void login(java.lang.String userId, java.lang.String password, java.util.Map properties)\n         "
                     + "throws org.eclipse.stardust.common.error.WorkflowException;",
               TAB + "void logout();" };
      }

      String newPackage = StringUtils.replace(destPackage.substring(1),"/",".");

      String tunnelingPrefix = (tunneling ? "tunneling.Tunneling" : "");
      final String longWrapperInterfaceName = newPackage + tunnelingPrefix
            + "Local" + javaSourceName;
      return createSource(longWrapperInterfaceName, longServiceName,
            longPathJavaSourceName, null, null,
            (String[]) superInterfaces.toArray(StringUtils.EMPTY_STRING_ARRAY),
            new LocalServiceMethodGenerator(tunneling), additionalMethods, false);
   }

   private StringBuffer createFileHeader(String revision)
   {
      StringBuffer result = new StringBuffer("/*\n * Generated from  " + revision);
      result.append("\n */\n");
      return result;
   }

   public String createSource(String longServiceName, String originalServiceName,
         String longPathJavaSourceName, String[] imports, String superClass,
         String[] superInterfaces, AbstractMethodGenerator methodGenerator, String[] additionalMethods,
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
         e.printStackTrace();
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

      /**
       * The value for the serialVersionUID should be changed if implementation will change in a incompatible way.
       */
      if (isClass)
      {
         result.append("   private static final long serialVersionUID = 1L;\n");
      }

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
         returnValue = methods[i].getReturns().getValue();
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

   public static void main(String[] args) throws IOException, ClassNotFoundException
   {
      String root = ".";
      String destPackage = "/org/eclipse/stardust/engine/api/ejb2/";

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
      createSources(root, destinationRoot, destPackage, serviceName, false);
      createSources(root, destinationRoot, destPackage, serviceName, true);
   }

   private static void createSources(String root, String destinationRoot,
         String destPackage, String serviceName, boolean tunneling) throws IOException,
         FileNotFoundException
   {
      String longServiceName = "org.eclipse.stardust.engine.api.runtime." + serviceName;
      String longPathJavaSourceName = root + "/org/eclipse/stardust/engine/api/runtime/" + serviceName
            + ".java";
      CodeGen generator = new CodeGen();

      String remoteServiceImpl = generator.createRemoteServiceImpl(longServiceName,
            longPathJavaSourceName, destPackage, tunneling);
      FileWriter writer = new FileWriter(new File(destinationRoot + destPackage
             + (tunneling ? "tunneling/beans/Tunneling" : "beans/Remote") + serviceName + "Impl.java"));
      writer.write(remoteServiceImpl);
      writer.close();

      String tunnelingPrefix = (tunneling ? "tunneling/Tunneling" : "");

      String remoteService = generator.createRemoteService(longServiceName,
            longPathJavaSourceName, destPackage, tunneling);
      writer = new FileWriter(new File(destinationRoot + destPackage
            + tunnelingPrefix + "Remote" + serviceName + ".java"));
      writer.write(remoteService);
      writer.close();

      // Local-interfaces using the same service implementation as remote interfaces.
      // Therefore no "createLocalService" is necessary.

      String localService = generator.createLocalService(longServiceName,
            longPathJavaSourceName, destPackage, tunneling);
      writer = new FileWriter(new File(destinationRoot + destPackage
            + tunnelingPrefix + "Local" + serviceName + ".java"));
      writer.write(localService);
      writer.close();
   }

   private static abstract class AbstractMethodGenerator
   {
      private final boolean tunneling;

      public AbstractMethodGenerator(boolean tunneling)
      {
         this.tunneling = tunneling;
      }

      abstract Object execute(JavaMethod source, String longServiceName, List<String> importList,
            List<String> longNameList);

      boolean isTunneling()
      {
         return tunneling;
      }

      protected String getMethodString(JavaMethod method)
      {
         StringBuffer result = new StringBuffer();
         String baseClass;

         Type mReturns = method.getReturns();

         JavaParameter[] parameters = method.getParameters();

         result.append(TAB).append("public ").append(mReturns.toGenericString()).append(" ");
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

         if (isTunneling())
         {
            if (parameters.length != 0)
            {
               result.append(", ");
            }
            result.append("org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext __tunneledContext");
         }

         result.append(")\n");

         Type[] exceptions = method.getExceptions();

         result.append("throws org.eclipse.stardust.common.error.WorkflowException");
         if (exceptions.length > 0)
         {
            for (int i = 0; i < exceptions.length; i++)
            {
               Type exception = exceptions[i];
               baseClass = exception.getJavaClass().getSuperClass().getValue();
               if ( !baseClass.toString().equals("org.eclipse.stardust.common.error.PublicException")
                     && !baseClass.toString().equals("org.eclipse.stardust.common.error.ResourceException"))
               {
                  result.append(", " + exception.toString());
               }
            }
         }
         return result.toString();
      }
   }

   private class RemoteServiceImplMethodGenerator extends AbstractMethodGenerator
   {
      private String service;

      public RemoteServiceImplMethodGenerator(String service, boolean tunneling)
      {
         super(tunneling);

         this.service = service;
      }

      public Object execute(JavaMethod method, String longServiceName, List<String> importList,
            List<String> longNameList)
      {
         Type mReturns = method.getReturns();
         Type[] exceptions = method.getExceptions();

         int tabReturnLength;

         StringBuffer result = new StringBuffer(getJavaDocMethodString(method,
               longServiceName, exceptions, importList, longNameList));

         result = result.append(splitLongLines(getMethodString(method), -1, 9, 0));

         result.append("\n").append(TAB).append("{\n");

         if (isTunneling())
         {
            result.append("      java.util.Map __invocationContextBackup = null;\n");
         }

         result.append("      try\n      {\n");
         tabReturnLength = 9;

         StringBuffer addString = new StringBuffer();

         if (isTunneling())
         {
            for (int i = 0; i < tabReturnLength; i++)
            {
               addString.append(" ");
            }

            addString.append("__invocationContextBackup = initInvocationContext(__tunneledContext);");
            result.append(splitLongLines(addString.toString(), -1, 9, 0)).
            append("\n");

            addString = new StringBuffer();
         }

         for (int i = 0; i < tabReturnLength; i++) addString.append(" ");
         if (!mReturns.isVoid())
         {
            addString.append("return ");
         }

         addString.append("((").append(service).append(") service).").append(
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

         result.append(splitLongLines(addString.toString(), -1, 9, 3)).
         append("\n");

         result.append("      }\n");
         result.append("      catch(org.eclipse.stardust.common.error.PublicException e)\n")
               .append("      {\n")
               .append("         throw new org.eclipse.stardust.common.error.WorkflowException(e);\n")
               .append("      }\n");
         result.append("      catch(org.eclipse.stardust.common.error.ResourceException e)\n")
               .append("      {\n")
               .append("         throw new org.eclipse.stardust.common.error.WorkflowException(e);\n")
               .append("      }\n");

         if(isTunneling())
         {
            result.append("      finally\n")
                  .append("      {\n")
                  .append("         clearInvocationContext(__tunneledContext, __invocationContextBackup);\n")
                  .append("      }\n");
         }

         result.append(TAB).append("}\n");
         return result;
      }

   }

   private class RemoteServiceMethodGenerator extends AbstractMethodGenerator
   {

      public RemoteServiceMethodGenerator(boolean tunneling)
      {
         super(tunneling);
      }

      public Object execute(JavaMethod method, String longServiceName, List<String> importList,
            List<String> longNameList)
      {
         Type[] exceptions = method.getExceptions();
         StringBuffer result = new StringBuffer(getJavaDocMethodString(method,
               longServiceName, exceptions, importList, longNameList));
         StringBuffer addString = new StringBuffer(getMethodString(method));
         addString.append(", ");
         addString.append("java.rmi.RemoteException;\n");
         result.append(splitLongLines(addString.toString(), -1, 9, 0));
         return result;
      }
   }

   private class LocalServiceMethodGenerator extends AbstractMethodGenerator
   {
      public LocalServiceMethodGenerator(boolean tunneling)
      {
         super(tunneling);
      }

      public Object execute(JavaMethod method, String longServiceName, List<String> importList,
            List<String> longNameList)
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

   private String getJavaDocClassString(JavaClass cls)
   {
      StringBuffer docString = new StringBuffer();
      docString.append("/**\n");
      String tagComment;

      String comment = cls.getComment();
      if (comment != null)
      {
         comment = splitLongLines(comment, 1, 1, 0);
         docString.append(" * " + comment + "\n *\n");
      }
      DocletTag[] tags = cls.getTags();
      for (int i = 0; i < tags.length; i++)
      {
         if ( !"version".equals(tags[i].getName()))
         {
            tagComment = splitLongLines(tags[i].getValue(), 1, 1, 0);
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
      StringBuffer docString = new StringBuffer();
      docString.append(TAB).append("/**\n");
      String tagComment;
      String linkString = "";
      String oldLinkString = "";
      String throwTag;
      String shortEName = "";
      String tagName, tagValue;
      String oldTagName = TAG_EMPTY;
      String tagParameter = "";

      int splitIndexBlank, splitIndexLink;

      List<String> exceptionList = new ArrayList<String>();

      for (int i = 0; i < exceptions.length; i++)
      {
         shortEName = splitToLastWord('.', exceptions[i].getValue());

         exceptionList.add(shortEName);
      }
      String comment = method.getComment();
      if (comment != null)
      {
         String newComment = splitLongLines(comment, 5, 4, 0);
         docString.append(TAB).append(" * " + newComment + "\n").append(TAB).append(" *\n");
      }
      boolean needsDefaultThrowsFragment = true;
      DocletTag[] tags = method.getTags();
      for (int i = 0; i < tags.length; i++)
      {
         tagName = tags[i].getName();
         tagValue = tags[i].getValue();

         splitIndexLink = tagValue.indexOf("{@link");
         if (-1 != splitIndexLink)
         {
            splitIndexBlank = tagValue.indexOf(' ', splitIndexLink);
            if ((-1 != splitIndexBlank) && (splitIndexBlank < tagValue.length()))
            {
               oldLinkString = tagValue.substring(splitIndexBlank + 1,
                     tagValue.indexOf('}', splitIndexBlank));
               if (importList.contains(oldLinkString))
               {
                  linkString = longNameList.get(importList.indexOf(oldLinkString))
                        .toString();
                  tagValue = StringUtils.replace(tagValue, oldLinkString, linkString);
               }
            }
         }
         if (tagName.equals(TAG_THROWS))
         {
            if (!oldTagName.equals(TAG_THROWS))
            {
               docString.append(TAB).append(" *\n");
            }
            tagParameter = splitFromIndex(' ', tagValue, 0);
            if (tagParameter.length() == 0)
            {
               tagParameter = tagValue;
            }
            // "tagParameter = tags[i].getParameters()[0];" Does not work.
            if (exceptionList.contains(tagParameter))
            {
               throwTag = exceptions[exceptionList.indexOf(tagParameter)].getValue();
            }
            else
            {
               throwTag =
                  lookForClassInPackage("org.eclipse.stardust.engine.api.runtime.", tagParameter);
            }
            tagValue = StringUtils.replace(tagValue, tagParameter, throwTag);

            docString.append(TAB).append(" * @throws ");
            docString.append(splitLongLines(tagValue, 5, 4, 4));
            docString.append("\n").append(TAB).append(" *     ");
            tagValue = "<em>Instances of {@link " + throwTag
                  + "} will be wrapped inside "
                  + "{@link org.eclipse.stardust.common.error.WorkflowException}.</em>";

         }
         else
         {
            if (tagName.equals(TAG_SEE))
            {
               if (needsDefaultThrowsFragment
                     && (oldTagName.equals(TAG_THROWS) || (oldTagName.equals(TAG_EMPTY))))
               {
                  appendDefaultThrowsFragment(docString);
                  needsDefaultThrowsFragment = false;
               }
               String methodLinkName = splitFromIndex('#', tagValue, 0);
               if (methodLinkName.length() > 0)
               {
                  String newMethodLinkName = lookForClassInPackage(
                        "org.eclipse.stardust.engine.api.runtime.", methodLinkName);
                  tagValue = StringUtils.replace(tagValue, methodLinkName,
                              newMethodLinkName);
               }
            }
            if (!oldTagName.equals(tagName) && (i > 0))
            {
               docString.append(TAB).append(" *\n");
            }
            docString.append(TAB).append(" * @" + tagName + " ");
         }

         docString.append(splitLongLines(tagValue, 5, 4, 4) + "\n");
         oldTagName = tagName;
      }

      if (needsDefaultThrowsFragment)
      {
         if ( !oldTagName.equals(TAG_THROWS) && !oldTagName.equals(TAG_EMPTY))
         {
            docString.append(TAB).append(" *\n");
         }
         appendDefaultThrowsFragment(docString);
         if ( oldTagName.equals(TAG_EMPTY))
         {
            docString.append(TAB).append(" *\n");
         }
         needsDefaultThrowsFragment = false;
      }

      if (!(oldTagName.equals(TAG_SEE)||(oldTagName.equals(TAG_EMPTY))))
      {
         docString.append(TAB).append(" *\n");
      }
      tagComment = longServiceName + "#" + method.getName()
         + getParameterNameList(method);
      docString.append(TAB).append(" * @see " + splitLongLines(tagComment, 5, 4, 4));
      docString.append("\n").append(TAB).append(" */\n");

      return docString.toString();

   }

   private static void appendDefaultThrowsFragment(StringBuffer docString)
   {
      docString
            .append(TAB).append(
                  " * @throws org.eclipse.stardust.common.error.WorkflowException ")
            .append("as a wrapper for\n").append(TAB).append(" *         ")
            .append("org.eclipse.stardust.common.error.PublicExceptions and org.eclipse.stardust.common.error.ResourceExceptions\n");
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
               docString.append(createNewLine(
                     addString, commentStarPos, startTabLength, newLineTabShift));
            }
            else
            {
               docString.append(addString);
            }
            countCharPerLine = startTabLength;
            docString.append(newLine(
                  commentStarPos, startTabLength, newLineTabShift));
         }
         else
         {
            addString = comment.substring(i);
            countCharPerLine = countCharPerLine + addString.length();
            if (countCharPerLine > 90)
            {
               addString = createNewLine(
                     addString, commentStarPos, startTabLength, newLineTabShift);
               countCharPerLine = startTabLength + addString.length();
            }
            docString.append(addString);
         }
       i = i + addString.length() + 1;
      }
      return docString.toString();
   }

   private static String createNewLine(String line, int commentStarPos, int tabStartLength, int newLineTabShift)
   {
      String newLineString = line;
      String oldLineString;
      String addString;

      int newLineLength;

      do
      {
         oldLineString = newLineString;
         newLineString = splitBeforeLastWord(' ', oldLineString);
         if(commentStarPos > -1)
         {
            newLineString = newLineString.trim();
         }
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
         addString = splitLongLines(addString, commentStarPos, tabStartLength, newLineTabShift);
      }
      if (addString.length() != 0)
      {
         newLineString = newLineString + newLine(commentStarPos, tabStartLength, newLineTabShift)
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
      while(newLineTabShift > 0)
      {
         result.append(" ");
         newLineTabShift--;
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