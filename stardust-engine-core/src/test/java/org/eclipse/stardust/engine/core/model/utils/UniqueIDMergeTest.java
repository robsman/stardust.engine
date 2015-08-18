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

import org.eclipse.stardust.engine.core.model.utils.Differences;
import org.eclipse.stardust.engine.core.model.utils.ModelOperations;
import org.eclipse.stardust.engine.core.model.utils.test.beans.Activity;
import org.eclipse.stardust.engine.core.model.utils.test.beans.Application;
import org.eclipse.stardust.engine.core.model.utils.test.beans.Model;
import org.eclipse.stardust.engine.core.model.utils.test.beans.ProcessDefinition;
import org.eclipse.stardust.engine.core.model.utils.test.beans.Transition;

import junit.framework.TestCase;

/**
 * @author ubirkemeyer
 * @version $Revision: 4351 $
 */
public class UniqueIDMergeTest extends TestCase
{
   private Model model;

   public void setUp()
   {
      model = Factory.createModel();
   }

   public void testMergeUnmodifiedModel()
   {
      // @todo (egypt): assert deep equality
   }

   /**
    * Test setting of a reference between two newly added elements
    */
   public void testIntrinsicReferenceBetweenAddedElements()
   {
      Model version = (Model) model.deepCopy();
      Application a3v = version.createApplication(Factory.APP3V);
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.createActivity(Factory.A1V);
      a1v.setApplication(a3v);

      merge(version);

      Application a3 = model.findApplication(Factory.APP3V);
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1V);
      assertEquals(a3, a1.getApplication());

   }

   /**
    * Test setting of a connections between two newly added elements
    */
   public void testIntrinsicConnectionBetweenAddedElements()
   {
      // @todo (egypt):
   }

   /**
    * Tests setting of a reference deeply inside an added element hierarchy.
    */
   public void testIntrinsicReferenceInAddedElement()
   {
      // @todo (egypt):
   }

   /**
    * Tests setting of a connection deeply inside an added element hierarchy.
    */
   public void testIntrinsicConnectionInAddedElement()
   {
      // @todo (egypt):
   }

   public void testExtrinsicReferenceFromAddedElement()
   {
      Model version = (Model) model.deepCopy();
      Application app1v = version.findApplication(Factory.APP1);
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.createActivity(Factory.A1V);
      a1v.setApplication(app1v);

      merge(version);

      Application app1 = model.findApplication(Factory.APP1);
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1V);
      assertEquals(app1, a1.getApplication());
   }

   public void testExtrinsicReferenceToAddedElement()
   {
      Model version = (Model) model.deepCopy();
      Application a3v = version.createApplication(Factory.APP3V);
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.findActivity(Factory.A1);
      a1v.setApplication(null);
      a1v.setApplication(a3v);

      merge(version);

      Application a3 = model.findApplication(Factory.APP3V);
      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1);
      assertEquals(a3, a1.getApplication());
   }

   public void testAddedConnectionWithIntrinsicEndPoints()
   {
      Model version = (Model) model.deepCopy();
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.createActivity(Factory.A1V);
      Activity a2v = p1v.createActivity(Factory.A2V);
      p1v.createTransition(Factory.T1V, a1v, a2v);
      merge(version);

      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A1V);
      Activity a2 = p1.findActivity(Factory.A2V);
      Transition t1 = p1.findTransition(Factory.T1V);

      assertEquals(a1, t1.getFromActivity());
      assertEquals(a2, t1.getToActivity());
      assertEquals(1, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
   }

   public void testAddedConnectionWithExtrinsicEndPoints()
   {
      Model version = (Model) model.deepCopy();
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.findActivity(Factory.A5);
      Activity a2v = p1v.findActivity(Factory.A6);
      p1v.createTransition(Factory.T1V, a1v, a2v);
      merge(version);

      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A5);
      Activity a2 = p1.findActivity(Factory.A6);
      Transition t1 = p1.findTransition(Factory.T1V);

      assertEquals(a1, t1.getFromActivity());
      assertEquals(a2, t1.getToActivity());
      assertEquals(1, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
   }

   public void testAddedConnectionWithMixedEndPoints()
   {
      Model version = (Model) model.deepCopy();
      ProcessDefinition p1v = version.findProcessDefinition(Factory.P1);
      Activity a1v = p1v.findActivity(Factory.A5);
      Activity a2v = p1v.createActivity(Factory.A2V);
      p1v.createTransition(Factory.T1V, a1v, a2v);
      merge(version);

      ProcessDefinition p1 = model.findProcessDefinition(Factory.P1);
      Activity a1 = p1.findActivity(Factory.A5);
      Activity a2 = p1.findActivity(Factory.A2V);
      Transition t1 = p1.findTransition(Factory.T1V);

      assertEquals(a1, t1.getFromActivity());
      assertEquals(a2, t1.getToActivity());
      assertEquals(1, a1.getOutTransitionsCount());
      assertEquals(1, a2.getInTransitionsCount());
   }

   public void testChangeElementId()
   {
      // @todo (egypt):
   }

   private void merge(Model version)
   {
      Differences diff = ModelOperations.compare(model, version);
      ModelOperations.merge(diff);
   }

}
