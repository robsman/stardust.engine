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
package org.eclipse.stardust.engine.core.model.test;

import java.lang.reflect.Field;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.utils.MultiHook;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;

import junit.framework.TestCase;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TestReferences extends TestCase
{
   private IModel model;
   public final String O1 = "O1";
   public final String R1 = "R1";
   public final String O2 = "O2";
   public final String R2 = "R2";
   public final String O3 = "O3";
   public final String O4 = "O4";
   public final String P1 = "P1";
   public final String A1 = "A1";
   public final String T1 = "T1";
   public final String A2 = "A2";
   public final String D1 = "D1";
   public final String D2 = "D2";
   public final String A4 = "A4";
   public final String A3 = "A3";
   public final String T2 = "T2";
   public final String DIA1 = "DIA1";
   public final String SYM1 = "SYM1";
   public final String SYM2 = "SYM2";
   public final String CONN2 = "CONN2";
   public final String M1 = "M1";
   public final String APP1 = "APP1";
   public final String T3 = "T3";
   public final String T4 = "T4";
   public final String T5 = "T5";
   public final String R3 = "R3";

   public TestReferences(String name)
   {
      super(name);
   }

   public void setUp()
   {
      model = new ModelBean(M1, M1, M1);
      IApplication app1 = model.createApplication(APP1, APP1, APP1, 0);
      org.eclipse.stardust.engine.api.model.IProcessDefinition p1 = model.createProcessDefinition(P1, P1, P1, false, 0);
      IActivity a1 = p1.createActivity(A1, A1, A1, 0);
      IActivity a2 = p1.createActivity(A2, A2, A2, 0);
      IActivity a3 = p1.createActivity(A3, A3, A3, 0);
      IActivity a4 = p1.createActivity(A4, A4, A4, 0);
      p1.createTransition(T1, T1, T1, a1, a2);
      p1.createTransition(T2, T2, T2, a1, a1);
      p1.createTransition(T3, T3, T3, a2, a1);
      p1.createTransition(T4, T4, T4, a2, a3);
      p1.createTransition(T5, T5, T5, a3, a4);
      a1.setApplication(app1);
      IRole r1 = model.createRole(R1, R1, R1, 0);
      IRole r2 = model.createRole(R2, R2, R2, 0);
      IRole r3 = model.createRole(R3, R3, R3, 0);
      IOrganization o1 = model.createOrganization(O1, O1, O1, 0);
      IOrganization o2 = model.createOrganization(O2, O2, O2, 0);
      IOrganization o3 = model.createOrganization(O3, O3, O3, 0);
      IOrganization o4 = model.createOrganization(O4, O4, O4, 0);
      o1.addToParticipants(r1);
      o1.addToParticipants(r2);
      o2.addToParticipants(r1);
      o2.addToParticipants(r2);
      o3.addToParticipants(r3);
      o3.addToParticipants(o2);
      o4.addToParticipants(o3);
      IData d1 = model.createData(D1,
            model.findDataType(PredefinedConstants.PRIMITIVE_DATA),
            D1, D1, false, 0,
            JavaDataTypeUtils.initPrimitiveAttributes(Type.Long, "1"));
      IData d2 = model.createData(D2,
            model.findDataType(PredefinedConstants.PRIMITIVE_DATA),
            D2, D2, false, 0,
            JavaDataTypeUtils.initPrimitiveAttributes(Type.Long, "1"));
      a1.createDataMapping("DM1", "DM1", d1, Direction.IN);
      a1.createDataMapping("DM2", "DM2", d1, Direction.IN);
      a1.createDataMapping("DM3", "DM3", d2, Direction.IN);
      a2.createDataMapping("DM4", "DM4", d1, Direction.IN);
      a2.createDataMapping("DM5", "DM5", d2, Direction.IN);

      Diagram dia1 = model.createDiagram(DIA1);
      model = (IModel) model.deepCopy();
   }

   private int getMultiHookCount(String fieldName, Object target)
   {
      Field field = Reflect.getField(target.getClass(), fieldName);
      try
      {
         MultiHook hook = (MultiHook) field.get(target);
         return hook.size();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   public void testModelSanity()
   {

   }

   public void testTransitionCardinality()
   {

   }

   public void testConnections()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);

      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("inTransitions", a1));
      assertEquals(2, getMultiHookCount("outTransitions", a1));
      assertEquals(1, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));
   }

   public void testDeleteConnection()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);
      ITransition t1 = p1.findTransition(T1);
      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("outTransitions", a1));
      assertEquals(1, getMultiHookCount("inTransitions", a2));

      t1.delete();

      assertEquals(4, getMultiHookCount("transitions", p1));
      assertEquals(1, getMultiHookCount("outTransitions", a1));
      assertEquals(0, getMultiHookCount("inTransitions", a2));
   }

   public void testDeleteConnectionEnd()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);

      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("outTransitions", a1));
      assertEquals(1, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));

      a1.delete();

      assertEquals(2, getMultiHookCount("transitions", p1));
      assertEquals(0, getMultiHookCount("outTransitions", a1));
      assertEquals(0, getMultiHookCount("inTransitions", a2));
      assertEquals(1, getMultiHookCount("outTransitions", a2));
   }

   public void testCrippledConnection()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);
      IData d1 = model.findData(D1);
      IData d2 = model.findData(D2);

      assertEquals(3, getMultiHookCount("dataMappings", a1));
      assertEquals(2, getMultiHookCount("dataMappings", a2));
      assertEquals(3, getMultiHookCount("dataMappings", d1));
      assertEquals(2, getMultiHookCount("dataMappings", d2));

      a1.delete();

      assertEquals(0, getMultiHookCount("dataMappings", a1));
      assertEquals(2, getMultiHookCount("dataMappings", a2));
      assertEquals(1, getMultiHookCount("dataMappings", d1));
      assertEquals(1, getMultiHookCount("dataMappings", d2));

      a2.delete();

      assertEquals(0, getMultiHookCount("dataMappings", d1));
      assertEquals(0, getMultiHookCount("dataMappings", d2));
   }

   public void testCrippledConnection2()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);
      IData d1 = model.findData(D1);
      IData d2 = model.findData(D2);

      assertEquals(3, getMultiHookCount("dataMappings", a1));
      assertEquals(2, getMultiHookCount("dataMappings", a2));
      assertEquals(3, getMultiHookCount("dataMappings", d1));
      assertEquals(2, getMultiHookCount("dataMappings", d2));

      d1.delete();

      assertEquals(1, getMultiHookCount("dataMappings", a1));
      assertEquals(1, getMultiHookCount("dataMappings", a2));
      assertEquals(0, getMultiHookCount("dataMappings", d1));
      assertEquals(2, getMultiHookCount("dataMappings", d2));

      d2.delete();

      assertEquals(0, getMultiHookCount("dataMappings", a1));
      assertEquals(0, getMultiHookCount("dataMappings", a2));
   }

   public void testLazyConnectionSetting()
   {
      //Diagram dia1 = model.findDiagram(DIA1);
      //DiagramSymbol sym1 = dia1.findSymbol(SYM1);
      //DiagramSymbol sym2 = dia1.findSymbol(SYM2);
      //DiagramSymbol conn2 = dia1.findConnection(CONN2);
      //assertEquals(5, dia1.getSymbolsCount());
      //assertEquals(5, dia1.getConnectionsCount());
      //assertEquals(2, sym2.getInConnectionsCount());
      //assertEquals(1, conn2.getInConnectionsCount());
   }

   public void testDeleteConnectionPropagation()
   {
      //Diagram dia1 = model.findDiagram(DIA1);
      //DiagramSymbol sym1 = dia1.findSymbol(SYM1);
      //sym1.delete();
      //assertEquals(4, dia1.getSymbolsCount());
      //assertEquals(1, dia1.getConnectionsCount());
   }

   public void testRerouteConnection()
   {
      IProcessDefinition p1 = model.findProcessDefinition(P1);
      IActivity a1 = p1.findActivity(A1);
      IActivity a2 = p1.findActivity(A2);
      IActivity a3 = p1.findActivity(A3);
      IActivity a4 = p1.findActivity(A4);
      ITransition t1 = p1.findTransition(T1);
      ITransition t2 = p1.findTransition(T2);
      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("inTransitions", a1));
      assertEquals(2, getMultiHookCount("outTransitions", a1));
      assertEquals(1, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));
      assertEquals(1, getMultiHookCount("inTransitions", a3));
      assertEquals(1, getMultiHookCount("outTransitions", a3));
      assertEquals(1, getMultiHookCount("inTransitions", a4));
      assertEquals(0, getMultiHookCount("outTransitions", a4));

      t1.setSecond(a4);

      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("inTransitions", a1));
      assertEquals(2, getMultiHookCount("outTransitions", a1));
      assertEquals(0, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));
      assertEquals(1, getMultiHookCount("inTransitions", a3));
      assertEquals(1, getMultiHookCount("outTransitions", a3));
      assertEquals(2, getMultiHookCount("inTransitions", a4));
      assertEquals(0, getMultiHookCount("outTransitions", a4));

      t1.setFirst(a3);

      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(2, getMultiHookCount("inTransitions", a1));
      assertEquals(1, getMultiHookCount("outTransitions", a1));
      assertEquals(0, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));
      assertEquals(1, getMultiHookCount("inTransitions", a3));
      assertEquals(2, getMultiHookCount("outTransitions", a3));
      assertEquals(2, getMultiHookCount("inTransitions", a4));
      assertEquals(0, getMultiHookCount("outTransitions", a4));

      t2.setFirst(a4);
      t2.setSecond(a4);

      assertEquals(5, getMultiHookCount("transitions", p1));
      assertEquals(1, getMultiHookCount("inTransitions", a1));
      assertEquals(0, getMultiHookCount("outTransitions", a1));
      assertEquals(0, getMultiHookCount("inTransitions", a2));
      assertEquals(2, getMultiHookCount("outTransitions", a2));
      assertEquals(1, getMultiHookCount("inTransitions", a3));
      assertEquals(2, getMultiHookCount("outTransitions", a3));
      assertEquals(3, getMultiHookCount("inTransitions", a4));
      assertEquals(1, getMultiHookCount("outTransitions", a4));
   }

   public void testSingleRef()
   {

   }

   public void testSingleRefEnd()
   {

   }

   public void testMultiRef()
   {

   }

   public void testMultiRefEnd()
   {

   }

   public void testOneToOne()
   {

   }

   public void testOneToMany()
   {

   }

   public void testManyToOne()
   {

   }

   public void testManyToMany()
   {
      IRole r1 = (IRole) model.findParticipant(R1);
      assertNotNull(r1.findOrganization(O1));
      assertNotNull(r1.findOrganization(O2));
      assertEquals(2, getMultiHookCount("organizations", r1));

      IOrganization o1 = (IOrganization) model.findParticipant(O1);
      assertNotNull(o1.findParticipant(R1));
      assertNotNull(o1.findParticipant(R2));
      assertEquals(2, getMultiHookCount("participants", o1));
   }

   public void testDeleteFromManyToMany()
   {
      assertEquals(7, getMultiHookCount("participants", model));
      IRole r2 = (IRole) model.findParticipant(R2);
      r2.delete();
      assertEquals(6, getMultiHookCount("participants", model));

      IRole r1 = (IRole) model.findParticipant(R1);
      assertNotNull(r1.findOrganization(O1));
      assertNotNull(r1.findOrganization(O2));
      assertEquals(2, getMultiHookCount("organizations", r1));

      IOrganization o1 = (IOrganization) model.findParticipant(O1);
      assertNotNull(o1.findParticipant(R1));
      assertNull(o1.findParticipant(R2));
      assertEquals(1, getMultiHookCount("participants", o1));

      IOrganization o2 = (IOrganization) model.findParticipant(O2);
      o2.delete();
      assertEquals(5, getMultiHookCount("participants", model));

      assertNotNull(r1.findOrganization(O1));
      assertNull(r1.findOrganization(O2));
      assertEquals(1, getMultiHookCount("organizations", r1));

      IOrganization o3 = (IOrganization) model.findParticipant(O3);
      o3.delete();
      assertEquals(4, getMultiHookCount("participants", model));

      IOrganization o4 = (IOrganization) model.findParticipant(O4);
      assertEquals(0, getMultiHookCount("organizations", o4));
      assertEquals(0, getMultiHookCount("participants", o4));
      assertEquals(0, getMultiHookCount("organizations", o2));

   }
}