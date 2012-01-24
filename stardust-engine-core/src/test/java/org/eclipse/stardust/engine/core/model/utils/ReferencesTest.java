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
package org.eclipse.stardust.engine.core.model.utils;

import org.eclipse.stardust.engine.core.model.utils.test.beans.*;

import junit.framework.TestCase;


/**
 * @author ubirkemeyer
 * @version $Revision: 6165 $
 */
public class ReferencesTest extends TestCase
{
   private Model model;

   public ReferencesTest(String name)
   {
      super(name);
   }

   public void setUp()
   {
      model = Factory.createModel();
   }

   public void testTransitionCardinality()
   {

   }

   /**
    * Tests whether connection endpoints are correctly set.
    */
   public void testConnections()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);

      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getInTransitionsCount());
      assertEquals(2, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());
   }

   /**
    * Tests whether connection endpoints are correctly deleted.
    */
   public void testDeleteConnection()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);
      Transition t1 = p1.findTransition(Factory.T1);
      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());

      t1.delete();

      assertEquals(4, p1.getTransitionsCount());
      assertEquals(1, a1.getOutTransitionsCount());
      assertEquals(0, a2.getInTransitionsCount());
   }

   /**
    * Tests whether connections are correctly deleted if an endpoint is deleted.
    */
   public void testDeleteConnectionEnd()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);

      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());

      a1.delete();

      assertEquals(2, p1.getTransitionsCount());
      assertEquals(0, a1.getOutTransitionsCount());
      assertEquals(0, a2.getInTransitionsCount());
      assertEquals(1, a2.getOutTransitionsCount());
   }

   /**
    * Tests deletion functionality if the connection is crippled i.e. one endpoint
    * equals the owning class. Deletion of the 'normal' endpoint is tested here.
    */
   public void testCrippledConnection()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);
      Data d1 = model.findData(Factory.D1);
      Data d2 = model.findData(Factory.D2);

      assertEquals(3, a1.getDataMappingsCount());
      assertEquals(2, a2.getDataMappingsCount());
      assertEquals(3, d1.getDataMappingsCount());
      assertEquals(2, d2.getDataMappingsCount());

      a1.delete();

      assertEquals(0, a1.getDataMappingsCount());
      assertEquals(2, a2.getDataMappingsCount());
      assertEquals(1, d1.getDataMappingsCount());
      assertEquals(1, d2.getDataMappingsCount());

      a2.delete();

      assertEquals(0, d1.getDataMappingsCount());
      assertEquals(0, d2.getDataMappingsCount());
   }

   /**
    * Tests deletion functionality if the connection is crippled i.e. one endpoint
    * equals the owning class. Deletion of the crippled endpoint is tested here.
    */
   public void testCrippledConnection2()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);
      Data d1 = model.findData(Factory.D1);
      Data d2 = model.findData(Factory.D2);

      assertEquals(3, a1.getDataMappingsCount());
      assertEquals(2, a2.getDataMappingsCount());
      assertEquals(3, d1.getDataMappingsCount());
      assertEquals(2, d2.getDataMappingsCount());

      d1.delete();

      assertEquals(1, a1.getDataMappingsCount());
      assertEquals(1, a2.getDataMappingsCount());
      assertEquals(0, d1.getDataMappingsCount());
      assertEquals(2, d2.getDataMappingsCount());

      d2.delete();

      assertEquals(0, a1.getDataMappingsCount());
      assertEquals(0, a2.getDataMappingsCount());
   }

   /**
    * Tests connection end point settings if one endpoint is lazily set.
    */
   public void testLazyConnectionSetting()
   {
      Diagram dia1 = model.findDiagram(Factory.DIA1);
      dia1.findSymbol(Factory.SYM1);
      DiagramSymbol sym2 = dia1.findSymbol(Factory.SYM2);
      DiagramConnection conn2 = dia1.findConnection(Factory.CONN2);
      assertEquals(5, dia1.getSymbolsCount());
      assertEquals(5, dia1.getConnectionsCount());
      assertEquals(2, sym2.getInConnectionsCount());
      assertEquals(1, conn2.getInConnectionsCount());
   }

   /**
    * Tests deletion of a whole hierarchy of connections.
    */
   public void testDeleteConnectionPropagation()
   {
      Diagram dia1 = model.findDiagram(Factory.DIA1);
      DiagramSymbol sym1 = dia1.findSymbol(Factory.SYM1);
      sym1.delete();
      assertEquals(4, dia1.getSymbolsCount());
      assertEquals(1, dia1.getConnectionsCount());
   }

   /**
    * Tests correct rerouting of a connection.
    */
   public void testRerouteConnection()
   {
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      Activity a2 = p1.findActivity(Factory.A2);
      Activity a3 = p1.findActivity(Factory.A3);
      Activity a4 = p1.findActivity(Factory.A4);
      Transition t1 = p1.findTransition(Factory.T1);
      Transition t2 = p1.findTransition(Factory.T2);
      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getInTransitionsCount());
      assertEquals(2, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());
      assertEquals(1, a3.getInTransitionsCount());
      assertEquals(1, a3.getOutTransitionsCount());
      assertEquals(1, a4.getInTransitionsCount());
      assertEquals(0, a4.getOutTransitionsCount());

      t1.setSecond(a4);

      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getInTransitionsCount());
      assertEquals(2, a1.getOutTransitionsCount());
      assertEquals(0, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());
      assertEquals(1, a3.getInTransitionsCount());
      assertEquals(1, a3.getOutTransitionsCount());
      assertEquals(2, a4.getInTransitionsCount());
      assertEquals(0, a4.getOutTransitionsCount());

      t1.setFirst(a3);

      assertEquals(5, p1.getTransitionsCount());
      assertEquals(2, a1.getInTransitionsCount());
      assertEquals(1, a1.getOutTransitionsCount());
      assertEquals(0, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());
      assertEquals(1, a3.getInTransitionsCount());
      assertEquals(2, a3.getOutTransitionsCount());
      assertEquals(2, a4.getInTransitionsCount());
      assertEquals(0, a4.getOutTransitionsCount());

      t2.setFirst(a4);
      t2.setSecond(a4);

      assertEquals(5, p1.getTransitionsCount());
      assertEquals(1, a1.getInTransitionsCount());
      assertEquals(0, a1.getOutTransitionsCount());
      assertEquals(0, a2.getInTransitionsCount());
      assertEquals(2, a2.getOutTransitionsCount());
      assertEquals(1, a3.getInTransitionsCount());
      assertEquals(2, a3.getOutTransitionsCount());
      assertEquals(3, a4.getInTransitionsCount());
      assertEquals(1, a4.getOutTransitionsCount());
   }

   public void testOneToOne()
   {
     // @todo (egypt):
   }

   public void testOneToMany()
   {
     // @todo (egypt): activity -- application
   }

   public void testManyToOne()
   {

   }

   /**
    * Tests correct many to many handling.
    */
   public void testManyToMany()
   {
      Role r1 = (Role) model.findParticipant(Factory.R1);
      assertNotNull(r1.findOrganization(Factory.O1));
      assertNotNull(r1.findOrganization(Factory.O2));
      assertEquals(2, r1.getOrganizationsCount());

      Organization o1 = (Organization) model.findParticipant(Factory.O1);
      assertNotNull(o1.findParticipant(Factory.R1));
      assertNotNull(o1.findParticipant(Factory.R2));
      assertEquals(2, o1.getParticipantsCount());
   }

   /**
    * Tests correct deletion of endpoints in many to many references.
    */
   public void testDeleteFromManyToMany()
   {
      assertEquals(7, model.getParticipantCount());
      Role r2 = (Role) model.findParticipant(Factory.R2);
      r2.delete();
      assertEquals(6, model.getParticipantCount());

      Role r1 = (Role) model.findParticipant(Factory.R1);
      assertNotNull(r1.findOrganization(Factory.O1));
      assertNotNull(r1.findOrganization(Factory.O2));
      assertEquals(2, r1.getOrganizationsCount());

      Organization o1 = (Organization) model.findParticipant(Factory.O1);
      assertNotNull(o1.findParticipant(Factory.R1));
      assertNull(o1.findParticipant(Factory.R2));
      assertEquals(1, o1.getParticipantsCount());

      Organization o2 = (Organization) model.findParticipant(Factory.O2);
      o2.delete();
      assertEquals(5, model.getParticipantCount());

      assertNotNull(r1.findOrganization(Factory.O1));
      assertNull(r1.findOrganization(Factory.O2));
      assertEquals(1, r1.getOrganizationsCount());

      Organization o3 = (Organization) model.findParticipant(Factory.O3);
      o3.delete();
      assertEquals(4, model.getParticipantCount());

      Organization o4 = (Organization) model.findParticipant(Factory.O4);
      assertEquals(0, o4.getOrganizationsCount());
      assertEquals(0, o4.getParticipantsCount());
      assertEquals(0, o2.getOrganizationsCount());
   }
}
