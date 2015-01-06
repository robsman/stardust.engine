package org.eclipse.stardust.engine.extensions.camel.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyPropertyManager
{
   private static final transient Logger LOG = LoggerFactory.getLogger(DerbyPropertyManager.class);
   private String derbyLocksWaitTimeout;
   
   
   public DerbyPropertyManager()
   {
      super();
   }
   
   public void setDerbyProperties()
   {
      this.increaseDerbyLockTimeout();
   }
   
   
   private void increaseDerbyLockTimeout()
   {
      String userName = "carnot";
      String password= "ag";
      String host = "jdbc:derby:target/ipp-test-DB";
      try
      {
         Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
         Connection con = DriverManager.getConnection( host,userName, password);
         Statement s = con.createStatement();
         s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
               + "'derby.locks.waitTimeout', '" + derbyLocksWaitTimeout + "')");
         
         LOG.info("Setting Derby Locks Wait Time Out to value: "+derbyLocksWaitTimeout);
         
         s.close();
         con.close();
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   public String getDerbyLocksWaitTimeout()
   {
      return derbyLocksWaitTimeout;
   }

   public void setDerbyLocksWaitTimeout(String derbyLocksWaitTimeout)
   {
      this.derbyLocksWaitTimeout = derbyLocksWaitTimeout;
   }
}
