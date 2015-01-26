/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert.Sauer (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *    Sven.Rottstock (SunGard CSA LLC) - adjusted to the latest sources and replaced JMock with Mockito
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import static org.eclipse.stardust.common.StringUtils.join;
import static org.eclipse.stardust.common.StringUtils.replace;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.NullWatcher;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemDescription;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLoader;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLocatorUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

public class DeleteScriptGenerator
{
   private static final Logger trace = LogManager.getLogger(DeleteScriptGenerator.class);

   private static final List<Long> PI_OID_SEQUENCE = Arrays.asList(192837465L, 918273645L);

   private static final long STRUCT_DATA_RT_OID_1 = -123456789L;
   private static final long DMS_DATA_RT_OID_2 = -987654321L;

   private final Map<PreparedStatement, String> preparedStatementRegistry = CollectionUtils.newHashMap();

   private final Map<PreparedStatement, List<List<Object>>> bindValueRegistry = CollectionUtils.newHashMap();

   private List<String> deleteScripts = CollectionUtils.newArrayList();

   private Session mockedSession;

   @Mock
   private IData mockStructData;

   @Mock
   private IData mockDmsData;
   
   @Mock
   private PreparedStatement delStmt;

   @Mock
   private PreparedStatement selStmt;
   
   public static void main(String[] args)
   {
      DeleteScriptGenerator generator = new DeleteScriptGenerator();
      generator.generateDeleteScripts();

      Writer writer = null;
      try
      {
         writer = new CharArrayWriter();
         generator.writeDeleteScript(writer);
   
         System.out.println();
         System.out.println("Generated DELETE scripts:");
         System.out.println();
         System.out.println(writer.toString());
      }
      finally
      {
         try
         {
            writer.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
   
   public void writeDeleteScript(Writer writer)
   {
      List<String> deleteScripts = getDeleteScripts();

      InputStream isTemplate = DeleteScriptGenerator.class.getResourceAsStream("ipp_tools-template.sql");
      try
      {
         BufferedReader reader = new BufferedReader(new InputStreamReader(isTemplate));
         for (String line = reader.readLine(); null != line; line = reader.readLine())
         {
            writer.append(line).append("\n");

            if (line.contains("BEGIN -- generated DELETE scripts"))
            {
               // insert generated scripts
               for (String script : deleteScripts)
               {
                  writer.append("    ").append(script).append(";\n");
               }
            }
         }
      }
      catch (IOException ioe)
      {
         trace.error("Failed processing PL/SQL template.", ioe);
      }
   }

   public List<String> getDeleteScripts()
   {
      return deleteScripts;
   }
   
   protected void replaceFakeDataInDeleteScripts()
   {
      List<String> properDeleteScripts = CollectionUtils.newArrayList();
      
      for (String script : deleteScripts)
      {
         script = replace(script, " IN (" + join(PI_OID_SEQUENCE.iterator(), ", ") + ")",
         " IN (SELECT oid FROM ipp_tools$pi_oids_to_delete)");

         script = replace(script, " AND dv.data IN (" + STRUCT_DATA_RT_OID_1 + ", "
               + DMS_DATA_RT_OID_2 + ")", " AND clb.ownerType = 'data_value' /* AND any dv.data */");

         if(script.startsWith("DELETE FROM clob_data "))
         {
            script = replace(script, " AND dv.data = 123", " AND clb.ownerType = 'data_value' /* AND any dv.data */");
         }
         
         properDeleteScripts.add(script);
      }
      this.deleteScripts = properDeleteScripts;
   }

   public void generateDeleteScripts()
   {
      MockitoAnnotations.initMocks(this);

      PluggableType type = newMockStructTypeDescriptor();
      when(this.mockStructData.getType()).thenReturn(type);
      
      type = newMockDmsTypeDescriptor();
      when(this.mockDmsData.getType()).thenReturn(type);

      try
      {
         doAnswer(storeTheBindVariableValue()).when(delStmt).setLong(anyInt(), anyLong());
         doAnswer(storeTheBindVariableValue()).when(delStmt).setString(anyInt(), anyString());
         doAnswer(advanceTheBindVariablesRow()).when(delStmt).addBatch();
         when(delStmt.executeUpdate()).thenAnswer(traceTheDeleteStatement());
         verify(delStmt, atMost(1)).close();
      }
      catch(SQLException e)
      {
         // need catch to keep compiler happy
      }
      
      try
      {
         doAnswer(storeTheBindVariableValue()).when(selStmt).setLong(anyInt(), anyLong());
         doAnswer(storeTheBindVariableValue()).when(selStmt).setString(anyInt(), anyString());
         doAnswer(advanceTheBindVariablesRow()).when(selStmt).addBatch();
         when(selStmt.executeQuery()).thenAnswer(traceTheSelectStatement());
         verify(selStmt, atMost(1)).close();
      }
      catch(SQLException e)
      {
         // need catch to keep compiler happy
      }
      
      this.mockedSession = SessionFactory.createSession(
            SessionProperties.DS_NAME_AUDIT_TRAIL, newMockDataSource());

      ParametersFacade.pushGlobals();
      try
      {
         // simulating bootstrapping, but without needing a license (see
         // EngineService.init())
         ItemLocatorUtils.registerDescription(
               ModelManagerFactory.ITEM_NAME,
               new ItemDescription(new MockModelManagerLoader(),
                     NullWatcher.class.getName()));

         Map<String, Object> config = CollectionUtils.newHashMap();
         config.put(SessionProperties.DS_NAME_AUDIT_TRAIL
               + SessionProperties.DS_SESSION_SUFFIX, mockedSession);
         config.put(SessionProperties.DS_NAME_AUDIT_TRAIL
               + SessionProperties.DS_USE_PREPARED_STATEMENTS_SUFFIX, "true");
         config.put(SessionProperties.DS_NAME_AUDIT_TRAIL
               + SessionProperties.DS_TYPE_SUFFIX, DBMSKey.ORACLE);

         ParametersFacade.pushLayer(config);
         try
         {
            ProcessInstanceUtils.deleteProcessInstances(PI_OID_SEQUENCE, mockedSession);
         }
         finally
         {
            ParametersFacade.popLayer();
         }
      }
      finally
      {
         ParametersFacade.popGlobals();
      }
      replaceFakeDataInDeleteScripts();
   }

   private Connection newMockConnection()
   {
      final Connection con = mock(Connection.class);
      
      try
      {
         when(con.getTransactionIsolation()).thenReturn(
               Connection.TRANSACTION_READ_COMMITTED);
         when(con.getAutoCommit()).thenReturn(false);
         when(con.createStatement()).thenAnswer(returnNewMockStatement());
         doAnswer(returnNewMockPreparedDeleteStatement()).when(con).prepareStatement(argThat(startsWith("DELETE ")));
         String arg = argThat(Matchers.<String>allOf(startsWith("SELECT "), anyOf(containsString("FROM property "),containsString("FROM structured_data "))));
         when(con.prepareStatement(arg)).thenAnswer(returnNewMockPreparedSelectStatement());
      }
      catch (SQLException se)
      {
         // need catch to keep compiler happy
      }

      return con;
   }

   private DataSource newMockDataSource()
   {
      final DataSource ds = mock(DataSource.class);

      try
      {
         Connection con = newMockConnection();
         stub(ds.getConnection()).toReturn(con);
      }
      catch (SQLException se)
      {
         // need catch to keep compiler happy
      }

      return ds;
   }

   private class MockModelManagerLoader implements ItemLoader
   {
      public Object load()
      {
         final ModelManager modelManager = mock(ModelManager.class);
         List<IModel> models = CollectionUtils.newArrayList();
         models.add(newMockModel());
         when(modelManager.getAllModels()).thenReturn(models.iterator());
         when(modelManager.getRuntimeOid(mockStructData)).thenReturn(STRUCT_DATA_RT_OID_1);
         when(modelManager.getRuntimeOid(mockDmsData)).thenReturn(DMS_DATA_RT_OID_2);
         
         return modelManager;
      }
   }

   private IModel newMockModel()
   {
      final IModel model = mock(IModel.class);
      List<IData> data = CollectionUtils.newArrayList();
      data.add(mockStructData);
      data.add(mockDmsData);
      when(model.getAllData()).thenReturn(data.iterator());
     
      return model;
   }

   private PluggableType newMockStructTypeDescriptor()
   {
      final PluggableType type = mock(PluggableType.class);

      when(type.getId()).thenReturn(StructuredDataConstants.STRUCTURED_DATA);
      when(type.getAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT)).thenReturn(null);
      
      return type;
   }

   private PluggableType newMockDmsTypeDescriptor()
   {
      final PluggableType type = mock(PluggableType.class);
      
      when(type.getId()).thenReturn(DmsConstants.DATA_TYPE_DMS_DOCUMENT);
      when(type.getAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT)).thenReturn(null);

      return type;
   }

   private Answer returnNewMockPreparedDeleteStatement()
   {
      return new Answer<Object>() /* "DELETE ... WHERE ?" */
      {
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            preparedStatementRegistry.put(delStmt, (String) invocation.getArguments()[0]);

            List<List<Object>> bindValues = CollectionUtils.newArrayList();
            bindValues.add(CollectionUtils.newArrayList());
            bindValueRegistry.put(delStmt, bindValues);

            return delStmt;
         }
      };
   }

   private Answer returnNewMockPreparedSelectStatement()
   {
      return new Answer<Object>() /* "SELECT ... WHERE ?" */
      {
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            preparedStatementRegistry.put(selStmt, (String) invocation.getArguments()[0]);

            List<List<Object>> bindValues = CollectionUtils.newArrayList();
            bindValues.add(CollectionUtils.newArrayList());
            bindValueRegistry.put(selStmt, bindValues);

            return selStmt;
         }
      };
   }

   private Answer<Statement> returnNewMockStatement()
   {
      return new Answer<Statement>() /* "JDBC statement" */
      {
         public Statement answer(InvocationOnMock invocation) throws Throwable
         {
            final Statement stmt = mock(Statement.class);
            
            try
            {
               when(stmt.executeQuery(argThat(startsWith("DELETE ")))).thenAnswer(traceTheDeleteStatement());
               when(stmt.executeQuery(argThat(Matchers.<String>allOf(startsWith("SELECT "), containsString(" FROM property "))))).thenReturn(emptyResultSet());
               when(stmt.executeQuery(argThat(Matchers.<String>allOf(startsWith("SELECT "), containsString(" FROM structured_data "))))).thenReturn(structResultSet());
               verify(stmt, atMost(1)).close();
            }
            catch (SQLException se)
            {
               // need catch to keep compiler happy
            }

            return stmt;
         }
      };
   }

   private Answer advanceTheBindVariablesRow()
   {
      return new Answer<Object>()
      {
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            List<List<Object>> bindValues = bindValueRegistry.get(invocation.getMock());
            bindValues.add(CollectionUtils.newArrayList());

            return null;
         }
      };
   }

   private Answer storeTheBindVariableValue()
   {
      return new Answer<Object>()
      {
         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable
         {
            if(invocation.getMethod().getName().startsWith("set"))
            {
               List<List<Object>> bindValues = bindValueRegistry.get(invocation.getMock());
   
               List<Object> rowValues = bindValues.get(bindValues.size() - 1);
   
               Object args[] = invocation.getArguments();
               int col = (Integer) args[0];
               Object value = args[1];
   
               while (col > rowValues.size())
               {
                  rowValues.add(null);
               }
               rowValues.set(col - 1, value);
            }
            return null;
         }
      };
   }

   private Answer<Integer> traceTheDeleteStatement()
   {
      return new Answer<Integer>() /* "DELETE ... statement" */
      {
         public Integer answer(InvocationOnMock invocation) throws Throwable
         {
            Object invokedObject = invocation.getMock();
            if (invokedObject instanceof PreparedStatement)
            {
               String sql = preparedStatementRegistry.get(invokedObject);
               List<List<Object>> bindValues = bindValueRegistry.get(invokedObject);

               for (List<Object> rowValues : bindValues)
               {
                  String boundSql = DeleteScriptUtils.applyBindVariables(sql, rowValues,
                        mockedSession.getDBDescriptor());

                  trace.debug("Handling DELETE statement: " + boundSql);

                  deleteScripts.add(boundSql);
               }

               return bindValues.size();
            }
            else if (invokedObject instanceof Statement)
            {
               String sql = (String) invocation.getArguments()[0];

               trace.debug("Handling DELETE statement: " + sql);

               deleteScripts.add(sql);

               return 1;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Invocation target must be some kind of JDBC statement.");
            }
         }
      };
   }

   private Answer<ResultSet> traceTheSelectStatement()
   {
      return new Answer() /* "SELECT ... statement" */
      {
         public ResultSet answer(InvocationOnMock invocation) throws Throwable
         {
            Object invokedObject = invocation.getMock();
            if (invokedObject instanceof PreparedStatement)
            {
               String sql = preparedStatementRegistry.get(invokedObject);
               List<List<Object>> bindValues = bindValueRegistry.get(invokedObject);

               for (List<Object> rowValues : bindValues)
               {
                  String boundSql = DeleteScriptUtils.applyBindVariables(sql, rowValues,
                        mockedSession.getDBDescriptor());

                  trace.debug("Handling SELECT statement: " + boundSql);
               }
               
               if(sql.startsWith("SELECT ") && sql.contains(" FROM structured_data"))
               {
                  return structResultSet();
               }

               return emptyResultSet();
            }
            else if (invokedObject instanceof Statement)
            {
               String sql = (String) invocation.getArguments()[0];

               trace.debug("Handling SELECT statement: " + sql);

               return emptyResultSet();
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Invocation target must be some kind of JDBC statement.");
            }
         }
      };
   }

   private ResultSet emptyResultSet()
   {
      final ResultSet rs = mock(ResultSet.class);

      try
      {
         when(rs.next()).thenReturn(Boolean.FALSE);
         verify(rs, atMost(1)).close();
      }
      catch (SQLException se)
      {
         // need catch to keep compiler happy
      }

      return rs;
   }
   
   private ResultSet structResultSet()
   {
      final ResultSet rs = mock(ResultSet.class);

      try
      {
         when(rs.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
         when(rs.getLong(1)).thenReturn(123L);
         verify(rs, atMost(1)).close();
      }
      catch (SQLException se)
      {
         // need catch to keep compiler happy
      }

      return rs;
   }
}

