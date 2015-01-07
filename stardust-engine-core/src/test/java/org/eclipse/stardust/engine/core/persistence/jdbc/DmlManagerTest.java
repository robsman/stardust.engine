package org.eclipse.stardust.engine.core.persistence.jdbc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class DmlManagerTest
{
   private DBDescriptor dbDescriptor;

   @Mock
   PreparedStatement statement;

   @Before
   public void setup()
   {
      dbDescriptor = new DerbyDbDescriptor();
   }

   @Test
   public void testGetDoubleSQLValue()
   {
      assertEquals(new String("-2.225E-307"), getSQLValue(-2.225E-307));
      assertEquals(new String("2.225E-307"), getSQLValue(2.225E-307));
      assertEquals(new String("0.0"), getSQLValue(-439E-324));
      assertEquals(new String("0.0"), getSQLValue(439E-324));
      assertEquals(new String("-2.225E-300"), getSQLValue(-2.225E-300));
      assertEquals(new String("2.225E-300"), getSQLValue(2.225E-300));
      assertEquals(new String("-0.4567"), getSQLValue(-0.4567));
      assertEquals(new String("0.4567"), getSQLValue(0.4567));
      assertEquals(new String("25.123"), getSQLValue(25.123));
      assertEquals(new String("-25.123"), getSQLValue(-25.123));
   }

   private String getSQLValue(Object value)
   {
      return DmlManager.getSQLValue(Double.class, value, dbDescriptor);
   }

   @Test
   public void testSetSQLValuePreparedStatement() throws SQLException
   {
      assertSetSQLValue(-2.225E-307, -2.225E-307);
      assertSetSQLValue(2.225E-307, 2.225E-307);
      assertSetSQLValue(-2.225E-308, 0.0);
      assertSetSQLValue(2.225E-308, 0.0);
      assertSetSQLValue(-439E-324, 0.0);
      assertSetSQLValue(439E-324, 0.0);
      assertSetSQLValue(-2.225E-300, -2.225E-300);
      assertSetSQLValue(2.225E-300, 2.225E-300);
      assertSetSQLValue(-0.4567, -0.4567);
      assertSetSQLValue(0.4567, 0.4567);
      assertSetSQLValue(25.123, 25.123);
      assertSetSQLValue(-25.123, -25.123);
   }

   private void assertSetSQLValue(Double value, Double expected) throws SQLException
   {
      statement = mock(PreparedStatement.class);
      DmlManager.setSQLValue(statement, 1, Double.class, value, dbDescriptor);
      verify(statement).setDouble(1, expected);
   }

}
