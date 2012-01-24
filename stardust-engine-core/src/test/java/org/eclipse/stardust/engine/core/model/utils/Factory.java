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

/**
 * @author ubirkemeyer
 * @version $Revision: 6165 $
 */
public class Factory
{
   public static final String O1 = "O1";
   public static final String R1 = "R1";
   public static final String O2 = "O2";
   public static final String R2 = "R2";
   public static final String O3 = "O3";
   public static final String O4 = "O4";
   public static final String P1 = "P1";
   public static final String A1 = "A1";
   public static final String T1 = "T1";
   public static final String A2 = "A2";
   public static final String D1 = "D1";
   public static final String D2 = "D2";
   public static final String A4 = "A4";
   public static final String A3 = "A3";
   public static final String T2 = "T2";
   public static final String DIA1 = "DIA1";
   public static final String SYM1 = "SYM1";
   public static final String SYM2 = "SYM2";
   public static final String CONN2 = "CONN2";
   public static String A1V="a1v";
   public static String A2V="a2v";
   public static String T1V="t1v";
   public static final String A5="a5";
   public static final String A6="a6";
   public static String P2V="p2v";
   public static String APP3V="a3v";
   public static final String APP1 = "APP1";

   public static Model createModel()
   {
      Model model = new Model();
      Application app1 = model.createApplication(APP1);
      ProcessDefinition p1 = model.createProcessDefinition(P1);
      Activity a1 = p1.createActivity(A1);
      Activity a2 = p1.createActivity(A2);
      Activity a3 = p1.createActivity(A3);
      Activity a4 = p1.createActivity(A4);
      p1.createActivity(A5);
      p1.createActivity(A6);
      a1.setApplication(app1);
      p1.createTransition(T1, a1, a2);
      p1.createTransition(T2, a1, a1);
      p1.createTransition("T3", a2, a1);
      p1.createTransition("T4", a2, a3);
      p1.createTransition("T5", a3, a4);
      Role r1 = model.createRole(R1);
      Role r2 = model.createRole(R2);
      Role r3 = model.createRole("R3");
      Organization o1 = model.createOrganization(O1);
      Organization o2 = model.createOrganization(O2);
      Organization o3 = model.createOrganization(O3);
      Organization o4 = model.createOrganization(O4);
      o1.addToParticipants(o1); 
      o1.addToParticipants(r2);
      o2.addToParticipants(r1);
      o2.addToParticipants(r2);
      o3.addToParticipants(r3);
      o3.addToParticipants(o2);
      o4.addToParticipants(o3);
      Data d1 = model.createData(D1);
      Data d2 = model.createData(D2);
      a1.createDataMapping("DM1", d1);
      a1.createDataMapping("DM2", d1);
      a1.createDataMapping("DM3", d2);
      a2.createDataMapping("DM4", d1);
      a2.createDataMapping("DM5", d2);

      Diagram dia1 = model.createDiagram(DIA1);
      DiagramSymbol sym1 = dia1.createSymbol(SYM1);
      DiagramSymbol sym2 = dia1.createSymbol(SYM2);
      DiagramSymbol sym3 = dia1.createSymbol("SYM3");
      DiagramSymbol sym4 = dia1.createSymbol("SYM4");
      DiagramSymbol sym5 = dia1.createSymbol("SYM5");
      DiagramConnection conn1 = dia1.createConnection("CONN1", sym1);
      conn1.setSecond(sym2);
      DiagramConnection conn2 = dia1.createConnection(CONN2, sym3, sym4);
      DiagramConnection conn3 = dia1.createConnection("CONN3", conn1);
      conn3.setSecond(conn2);
      dia1.createConnection("CONN4", conn3, sym5);
      dia1.createConnection("CONN5", conn3, sym2);
      model = (Model) model.deepCopy();
      return model;
   }
}
