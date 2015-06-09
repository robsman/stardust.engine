/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.benchmarks;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;

public class BenchmarkTestUtils
{

   private static final String BENCHMARK_ARTIFACT_TYPE_ID = BenchmarkDefinitionArtifactType.TYPE_ID;

   private BenchmarkTestUtils()
   {}

   public static void deployBenchmark(String benchmarkName,
         TestServiceFactory sf)
   {
      AdministrationService as = sf.getAdministrationService();
      as.deployRuntimeArtifact(getRuntimeArtifact(benchmarkName,
            getBenchmarkJson(benchmarkName)));

   }

   public static String getBenchmarkJson(String fileName)
   {
      final String filePath = "benchmarks/" + fileName;
      String content = BenchmarkTestUtils.readFile(filePath);
      return content;
   }

   private static String postProcess(String content)
   {
      if (content != null)
      {
         return content.replace("${currentDayStart}", getCurrentDayStart()).replace(
               "${currentDayEnd}", getCurrentDayEnd());
      }
      return content;
   }

   private static String getCurrentDayEnd()
   {
      Calendar now = TimestampProviderUtils.getCalendar();

      now.set(Calendar.HOUR_OF_DAY, 23);
      now.set(Calendar.MINUTE, 59);
      now.set(Calendar.MILLISECOND, 0);

      return Long.valueOf(now.getTime().getTime()).toString();
   }

   private static String getCurrentDayStart()
   {
      Calendar now = TimestampProviderUtils.getCalendar();

      now.set(Calendar.HOUR_OF_DAY, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.MILLISECOND, 0);

      return Long.valueOf(now.getTime().getTime()).toString();
   }

   public static void deployCalendar(String calendarName, ServiceFactory sf)
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

      final String parentFolder = "/business-calendars/timeOffCalendar";

      String content = getCalendarJson(calendarName);

      DocumentInfo document = DmsUtils.createDocumentInfo(calendarName);

      DmsUtils.ensureFolderHierarchyExists(parentFolder, dms);

      Document calDoc = dms.createDocument(parentFolder, document, content.getBytes(), null);

      byte[] checkContent = dms.retrieveDocumentContent(calDoc.getId());
      Assert.assertNotNull(checkContent);
      Assert.assertNotEquals(0, checkContent.length);
   }

   public static String getCalendarJson(String calendarName)
   {
      final String calendarFilePath = "binaryFiles/" + calendarName;
      String content = BenchmarkTestUtils.readFile(calendarFilePath);
      content = postProcess(content);
      return content;
   }

   public static String readFile(String filePath)
   {
      final InputStream is = RtEnvHome.class.getClassLoader().getResourceAsStream(
            filePath);
      BufferedReader br;
      try
      {
         br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

         StringBuilder sb = new StringBuilder();
         String read;

         read = br.readLine();
         while (read != null)
         {
            sb.append(read);
            read = br.readLine();
         }
         return sb.toString();
      }
      catch (UnsupportedEncodingException e1)
      {
         throw new RuntimeException(e1);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static RuntimeArtifact getRuntimeArtifact(String artifactId, String content)
   {
      return new RuntimeArtifact(BENCHMARK_ARTIFACT_TYPE_ID, artifactId, artifactId,
            content.getBytes(), new Date(1));
   }

}
