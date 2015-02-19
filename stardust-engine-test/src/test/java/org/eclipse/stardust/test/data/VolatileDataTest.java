package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class VolatileDataTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "VolatileDataModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Test
   public void testShortPrimitiveVolatileDataDeleted()
   {
      ProcessInstance pi = sf.getWorkflowService().startProcess(
            "VolatileDataProcessForPrimitives", null, true);

      try
      {
         assertEquals(false, isVolatileDataOfProcessInstanceInDb(pi));
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Completed);
   }

   @Test
   public void testExtendedPrimitiveVolatileDataDeleted()
   {
      String randomString = generateString(new Random(), "ABCEFGHIJKLMNOPQRSTUVWXYZ", 500);

      Map<String, Object> processData = CollectionUtils.newHashMap();
      processData.put("BaseData", randomString);

      ProcessInstance pi = sf.getWorkflowService().startProcess(
            "VolatileDataProcessForPrimitives", processData, true);

      try
      {
         assertEquals(false, isVolatileDataOfProcessInstanceInDb(pi));
         assertEquals(false, isVolatileStringDataOfProcessInstanceInDb());
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSerializableVolatileDataDeleted()
   {
      SerializedObject object = new SerializedObject();

      Map<String, Object> processData = CollectionUtils.newHashMap();
      processData.put("BaseSerializable", object);

      ProcessInstance pi = sf.getWorkflowService().startProcess(
            "VolatileDataProcessForSerializables", processData, true);

      try
      {
         assertEquals(false, isVolatileDataOfProcessInstanceInDb(pi));
         assertEquals(false, isVolatileStringDataOfProcessInstanceInDb());
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Completed);
   }

   @Test
   public void testVolatileDataExistsInWaitingProcess()
   {
      ProcessInstance pi = sf.getWorkflowService().startProcess(
            "WaitingVolatileDataProcess", null, true);

      try
      {
         assertEquals(true, isVolatileDataOfProcessInstanceInDb(pi));
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Active);

      ActivityInstance ai = sf.getQueryService()
            .getAllActivityInstances(
                  ActivityInstanceQuery.findAlive(pi.getOID(), "ManualActivity1"))
            .get(0);

      if (ai != null)
      {
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      }
      else
      {
         fail("Waiting AI not found.");
      }

      try
      {
         assertEquals(false, isVolatileDataOfProcessInstanceInDb(pi));
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Completed);
   }

   @Test
   public void testStructuredVolatileDataDeleted()
   {

      Map<String, Object> processData = CollectionUtils.newHashMap();
      processData.put("BaseStructured", createStructuredData());

      ProcessInstance pi = sf.getWorkflowService().startProcess(
            "VolatileDataProcessForStructured", processData, true);

      try
      {
         assertEquals(false, isVolatileStructuredDataOfProcessInstanceInDb(pi));
         assertEquals(false, isVolatileClobDataOfProcessInstanceInDb(pi));
      }
      catch (SQLException sqlex)
      {
         fail(sqlex.getMessage());
      }

      assertEquals(sf.getWorkflowService()
            .getProcessInstance(pi.getOID())
            .getState(), ProcessInstanceState.Completed);
   }

   /**************** Helper Methods ****************/

   private boolean isVolatileDataOfProcessInstanceInDb(ProcessInstance pi)
         throws SQLException
   {
      Model model = sf.getQueryService().getModel(pi.getModelOID());
      List<Data> dataList = model.getAllData();

      for (Data data : dataList)
      {
         if (Boolean.valueOf((String) data.getAttribute("carnot:engine:volatile")))
         {

            Connection connection = testClassSetup.dataSource().getConnection();
            Statement stmt = null;
            final boolean result;

            try
            {
               stmt = connection.createStatement();
               final ResultSet rs = stmt.executeQuery("SELECT "
                     + "                                 DATA_VALUE.*,DATA.* "
                     + "                               FROM "
                     + "                                 PUBLIC.DATA_VALUE "
                     + "                               RIGHT JOIN "
                     + "                                 PUBLIC.DATA "
                     + "                               ON "
                     + "                                 DATA.OID = DATA_VALUE.DATA "
                     + "                               WHERE "
                     + "                                 PROCESSINSTANCE = "+ pi.getOID() + " "
                     + "                               AND "
                     + "                                 DATA.ID = '" + data.getId() + "'");

               result = rs.first();
            }
            finally
            {
               if (stmt != null)
               {
                  stmt.close();
               }
               if (connection != null)
               {
                  connection.close();
               }
            }

            if (result)
            {
               return result;
            }

         }
      }
      return false;
   }

   private boolean isVolatileStructuredDataOfProcessInstanceInDb(ProcessInstance pi)
         throws SQLException
   {
      Model model = sf.getQueryService().getModel(pi.getModelOID());
      List<Data> dataList = model.getAllData();

      for (Data data : dataList)
      {
         if (StructuredTypeRtUtils.isStructuredType(data.getTypeId())
               && Boolean.valueOf((String) data.getAttribute("carnot:engine:volatile")))
         {
            Connection connection = testClassSetup.dataSource().getConnection();
            Statement stmt = null;
            final boolean result;

            try
            {
               stmt = connection.createStatement();

               final ResultSet rs = stmt.executeQuery("SELECT "
                     + "                                 STRUCTURED_DATA_VALUE.*, STRUCTURED_DATA.*, DATA.* "
                     + "                               FROM "
                     + "                                 PUBLIC.STRUCTURED_DATA_VALUE "
                     + "                               LEFT JOIN "
                     + "                                 PUBLIC.STRUCTURED_DATA"
                     + "                               ON "
                     + "                                 STRUCTURED_DATA_VALUE.XPATH = STRUCTURED_DATA.OID"
                     + "                               LEFT JOIN "
                     + "                                 PUBLIC.DATA"
                     + "                               ON "
                     + "                                 DATA.OID = STRUCTURED_DATA.DATA"
                     + "                               WHERE DATA.ID = '" + data.getId() + "'");

               result = rs.next();
            }
            finally
            {
               if (stmt != null)
               {
                  stmt.close();
               }
               if (connection != null)
               {
                  connection.close();
               }
            }

            if (result)
            {            
               return result;
            }
         }
      }

      return false;
   }

   private boolean isVolatileClobDataOfProcessInstanceInDb(ProcessInstance pi) throws SQLException
   {

      Model model = sf.getQueryService().getModel(pi.getModelOID());
      List<Data> dataList = model.getAllData();

      for (Data data : dataList)
      {
         if (StructuredTypeRtUtils.isStructuredType(data.getTypeId())
               && Boolean.valueOf((String) data.getAttribute("carnot:engine:volatile")))
         {

            Connection connection = testClassSetup.dataSource().getConnection();
            Statement stmt = null;

            final boolean result;

            try
            {
               stmt = connection.createStatement();
               final ResultSet rs = stmt.executeQuery("SELECT "
                     + "                                 CLOB_DATA.* "
                     + "                               FROM "
                     + "                                 PUBLIC.CLOB_DATA"
                     + "                               LEFT JOIN "
                     + "                                 PUBLIC.DATA_VALUE"
                     + "                               ON "
                     + "                                 CLOB_DATA.OID = DATA_VALUE.NUMBER_VALUE"
                     + "                               LEFT JOIN "
                     + "                                 PUBLIC.DATA"
                     + "                               ON "
                     + "                                 DATA.OID = DATA_VALUE.DATA"
                     + "                               WHERE "
                     + "                                 DATA.ID = 'VolatileStructured'");

               result = rs.next();

            }
            finally
            {
               if (stmt != null)
               {
                  stmt.close();
               }
               if (connection != null)
               {
                  connection.close();
               }
            }

            if (result)
            {

               return result;
            }
         }
      }
      return false;
   }

   private boolean isVolatileStringDataOfProcessInstanceInDb() throws SQLException
   {

      Connection connection = testClassSetup.dataSource().getConnection();
      Statement stmt = null;
      final boolean result;

      try
      {
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT "
               + "                                 STRING_DATA.* "
               + "                               FROM "
               + "                                 PUBLIC.STRING_DATA "
               + "                               WHERE "
               + "                                 STRING_DATA.DATA_TYPE = 'data_value' "
               + "                               AND "
               + "                                 STRING_DATA.OBJECTID "
               + "                               NOT IN "
               + "                                 (SELECT "
               + "                                    DATA_VALUE.OID "
               + "                                  FROM "
               + "                                    PUBLIC.DATA_VALUE)");

         result = rs.next();
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }

      return result;

   }

   static String generateString(Random rng, String characters, int length)
   {
      char[] text = new char[length];
      for (int i = 0; i < length; i++ )
      {
         text[i] = characters.charAt(rng.nextInt(characters.length()));
      }
      return new String(text);
   }

   private Map<String, String> createStructuredData()
   {
      Map<String, String> struct = CollectionUtils.newMap();
      String firstName = "John";
      String lastName = "Doe";
      String randomString = generateString(new Random(), "ABCEFGHIJKLMNOPQRSTUVWXYZ", 500);
      struct.put("firstName", firstName);
      struct.put("lastName", lastName);
      struct.put("randomString", randomString);
      return struct;
   }

}
