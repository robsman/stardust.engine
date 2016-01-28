/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.utils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Spy;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;

public class ModelElementBeanTest
{
   @ClassRule
   public static SpyLogger spyLogger = new SpyLogger();

   @Test
   public void testNextIDWithNullId()
   {
      // Arrange
      int currentId = 50;
      
      // Act
      int nextId = ModelElementBean.nextID("Application", currentId, null);
      
      // Assert
      assertThat(nextId, is(equalTo(currentId)));
   }

   @Test
   public void testNextIDWithNullPrefix()
   {
      // Arrange
      int currentId = 50;
      int nextId = -1;
      
      // Act
      try
      {
         nextId = ModelElementBean.nextID(null, currentId, "Application");
         // Assert
         fail("null prefix should throw an illegal argument exception");
      }
      catch(IllegalArgumentException e)
      {
         assertThat(e.getMessage(), allOf(
               containsString("prefix"), 
               containsString("null")));
         assertThat(nextId, is(equalTo(-1)));
      }
      
   }

   @Test
   public void testNextIDWithDifferentPrefix()
   {
      // Arrange
      int currentId = 1;

      // Act
      int nextId = ModelElementBean.nextID("Application", currentId, "App");
      
      // Assert
      assertThat(nextId, is(equalTo(currentId)));
   }

   @Test
   public void testNextIDWithLettersAsId()
   {
      // Arrange
      int currentId = 1;
      String prefix = "Application";
      
      // Act
      int nextId = ModelElementBean.nextID(prefix, currentId, prefix + "ABC");
      
      // Assert
      assertThat(nextId, is(equalTo(currentId)));
   }

   @Test
   public void testNextIDWithSameId()
   {
      // Arrange
      int currentId = 1;
      String prefix = "Application";
      
      // Act
      int nextId = ModelElementBean.nextID(prefix, currentId, prefix);
      
      // Assert
      assertThat(nextId, is(equalTo(currentId)));
   }

   @Test
   public void testNextIDWithSameCurrentParam()
   {
      // Arrange
      int currentId = 1;
      String prefix = "Application";
      
      // Act
      int nextId0 = ModelElementBean.nextID(prefix, currentId, prefix + "00");
      int nextId1 = ModelElementBean.nextID(prefix, currentId, prefix + "01");
      int nextId2 = ModelElementBean.nextID(prefix, currentId, prefix + "02");
      int nextId3 = ModelElementBean.nextID(prefix, currentId, prefix + "03");
      
      // Assert
      assertThat(nextId0, is(equalTo(currentId)));
      assertThat(nextId1, is(equalTo(2)));
      assertThat(nextId2, is(equalTo(3)));
      assertThat(nextId3, is(equalTo(4)));
   }

   @Test
   public void testNextIDWithNegativeIDs()
   {
      // Arrange
      int currentId = 1;
      String prefix = "Application";
      
      // Act
      int nextId0 = ModelElementBean.nextID(prefix, currentId, prefix + "-00");
      int nextId1 = ModelElementBean.nextID(prefix, currentId, prefix + "-01");
      int nextId2 = ModelElementBean.nextID(prefix, currentId, prefix + "-02");
      int nextId3 = ModelElementBean.nextID(prefix, currentId, prefix + "-03");
      
      // Assert
      assertThat(nextId0, is(equalTo(currentId)));
      assertThat(nextId1, is(equalTo(1)));
      assertThat(nextId2, is(equalTo(1)));
      assertThat(nextId3, is(equalTo(1)));
   }

   @Test
   public void testGetDescription()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      bean.setDescription("dummy");
      bean.setUnmodified();
      
      // Act
      String description = bean.getDescription();
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(false)));
      assertThat(description, is(equalTo("dummy")));
   }

   @Test
   public void testGetNullDescription()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      String description = bean.getDescription();
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(false)));
      assertThat(description, is(nullValue()));
   }

   @Test
   public void testSetDescription()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      bean.setDescription("dummy");
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getDescription(), is(equalTo("dummy")));
   }

   @Test
   public void testSetNullDescription()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      bean.setDescription("dummy");
      bean.setUnmodified();
      
      // Act
      bean.setDescription(null);
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getDescription(), is(nullValue()));
   }

   @Test
   public void testDefaultValueOfIsPredefined()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      boolean predefined = bean.isPredefined();
      
      // Assert
      assertThat(predefined, is(equalTo(false)));
   }

   @Test
   public void testSetPredefined()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      bean.setPredefined(true);
      
      // Assert
      assertThat(bean.isPredefined(), is(equalTo(true)));
      assertThat(bean.isModified(), is(equalTo(true)));
   }

   @Test
   public void testSetPredefinedBackToDefault()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      bean.setPredefined(true);
      bean.setUnmodified();
      
      // Act
      bean.setPredefined(false);
      
      // Assert
      assertThat(bean.isPredefined(), is(equalTo(false)));
      assertThat(bean.isModified(), is(equalTo(true)));
   }

   @Test
   public void testGetUniqueIdWhenElementOidIsSet()
   {
      // Arrange
      final int elementOID = 512;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      String uniqueId = bean.getUniqueId();
      
      // Assert
      assertThat(uniqueId, is(equalTo(Integer.toString(elementOID))));
   }

   @Test
   public void testGetUniqueIdWhenElementOidIsNotSet()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      String uniqueId = bean.getUniqueId();
      
      // Assert
      assertThat(uniqueId, is(equalTo(Integer.toString(0))));
   }

   @Test
   public void testGetDefaultElementOID()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      int elementOID = bean.getElementOID();
      
      // Assert
      assertThat(elementOID, is(equalTo(0)));
   }

   @Test
   public void testGetElementOID()
   {
      // Arrange
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      int innerElementOID = bean.getElementOID();
      
      // Assert
      assertThat(innerElementOID, is(equalTo(elementOID)));
   }

   @Test
   public void testGetOID()
   {
      // Arrange
      final int modelOID = 10;
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      root.setModelOID(modelOID);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      long oid = bean.getOID();
      
      // Assert
      long mask = 0xFFFFFFFF00000000l;
      assertThat((int)((oid & mask) >> 32), is(equalTo(modelOID)));
      assertThat((int)((oid & ~mask)), is(equalTo(elementOID)));
   }
   
   @Test
   public void testGetOIDWithNegativModelOID()
   {
      // Arrange
      final int modelOID = -10;
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      root.setModelOID(modelOID);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      long oid = bean.getOID();
      
      // Assert
      long mask = 0xFFFFFFFF00000000l;
      assertThat((int)((oid & mask) >> 32), is(equalTo(modelOID)));
      assertThat((int)((oid & ~mask)), is(equalTo(elementOID)));
   }

   @Test
   public void testGetOIDWithNoModelOID()
   {
      // Arrange
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      long oid = bean.getOID();
      
      // Assert
      long mask = 0xFFFFFFFF00000000l;
      assertThat((int)((oid & mask) >> 32), is(equalTo(0)));
      assertThat((int)((oid & ~mask)), is(equalTo(elementOID)));
   }

   @Test
   public void testSetElementOID()
   {
      // Arrange
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      bean.setElementOID(elementOID);
      
      // Assert
      assertThat(bean.getElementOID(), is(equalTo(elementOID)));
      assertThat(bean.isModified(), is(equalTo(true)));
   }

   @Test
   public void testSetTransient()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      bean.setTransient(true);
      
      // Assert
      assertThat(bean.isTransient(), is(equalTo(true)));
      assertThat(bean.isModified(), is(equalTo(true)));
   }

   @Test
   public void testDefaultTransientValue()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      boolean bTransient = bean.isTransient();
      
      // Assert
      assertThat(bTransient, is(equalTo(false)));
      assertThat(bean.isModified(), is(equalTo(false)));
   }

   @Test
   public void testIsTransient()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      boolean defaultTransient = bean.isTransient();
      bean.setTransient(true);
      boolean newTransient = bean.isTransient();
      
      // Assert
      assertThat(defaultTransient, is(equalTo(false)));
      assertThat(newTransient, is(equalTo(true)));
   }

   @Test
   public void testRegisterWithTransient()
   {
      // Arrange
      final int elementOID = 563;
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      RootElement root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.setTransient(true);
      
      // Act
      bean.register(elementOID);
      
      // Assert
      final int innerElementOID = bean.getElementOID();
      assertThat(innerElementOID, is(not(equalTo(elementOID))));
      assertThat(innerElementOID, is(lessThan(0)));
   }

   @Test
   public void testRegisterWithZeroOID()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      final int currentElementOID = 15000; 
      root.setCurrentElementOID(currentElementOID);
      ModelBean rootSpy = spy(root);
      when(parent.getModel()).thenReturn(rootSpy);
      bean.setParent(parent);
      
      // Act
      bean.register(0);
      
      // Assert
      final int innerElementOID = bean.getElementOID();
      assertThat(innerElementOID, is(equalTo(currentElementOID+1)));
      verify(rootSpy).createElementOID();
      verify(rootSpy).register(bean);
   }

   @Test
   public void testRegisterWithPositiveOID()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      final int elementOID = 15000; 
      ModelBean rootSpy = spy(root);
      when(parent.getModel()).thenReturn(rootSpy);
      bean.setParent(parent);
      
      // Act
      bean.register(elementOID);
      
      // Assert
      final int innerElementOID = bean.getElementOID();
      assertThat(innerElementOID, is(equalTo(elementOID)));
      verify(rootSpy).register(bean);
   }

   @Test
   public void testRegisterWithDuplicatedOIDs()
   {
      // Arrange
      final int elementOID = 15000; 
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean.setParent(parent);
      bean.register(elementOID);
      
      bean = new TestModelElementBean();
      bean.setParent(parent);
      
      // Act
      try
      {
         bean.register(elementOID);
         fail("Duplicated elementOIDs in the same model should not be allowed");
      }
      catch(InternalException e)
      {
         // Assert
         assertThat(e.getMessage(), allOf(
               containsString(Integer.toString(elementOID)),
               containsString("already in use")
               ));
      }
   }

   @Test
   public void testRegisterWithDuplicatedOIDsButSameInstance()
   {
      // Arrange
      final int elementOID = 15000; 
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      ModelBean rootSpy = spy(root);
      when(parent.getModel()).thenReturn(rootSpy);
      bean.setParent(parent);
      bean.register(elementOID);
      
      // Act
      bean.register(elementOID);

      // Assert
      final int innerElementOID = bean.getElementOID();
      assertThat(innerElementOID, is(equalTo(elementOID)));
      verify(rootSpy, times(2)).register(bean);
   }

   @Test
   public void testDeleteWithNoModel()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(null);
      bean.setParent(parent);
      ModelElement element1 = mock(ModelElement.class);
      Connection element2 = mock(Connection.class);
      ModelElement element3 = mock(ModelElement.class);
      bean.link.add(element1);
      bean.conn.add(element2);
      bean.modelElements.add(element3);
      
      // Act
      bean.delete();

      // Assert
      verify(element1).delete();
      verify(element2).delete();
      verify(element3).delete();
      assertThat(bean.getParent(), is(nullValue()));
   }

   @Test
   public void testDeleteWhereModelElementThrowsAnException()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(null);
      bean.setParent(parent);
      ModelElement element1 = mock(ModelElement.class);
      ModelElement element2 = mock(ModelElement.class);
      RuntimeException re = new RuntimeException("delete not allowed");
      doThrow(re).when(element1).delete();
      doThrow(re).when(element2).delete();
      bean.link.add(element1);
      bean.modelElements.add(element2);
      
      // Act
      bean.delete();
      
      // Assert
      verify(element1).delete();
      verify(element2).delete();
      assertThat(bean.getParent(), is(nullValue()));
      verify(spyLogger.getSpy(), times(2)).warn(anyString(), same(re));
   }

   @Test
   public void testDeleteWithAttachedModel()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      ModelBean rootSpy = spy(root);
      when(parent.getModel()).thenReturn(rootSpy);
      bean.setParent(parent);
      ModelElement element1 = mock(ModelElement.class);
      bean.link.add(element1);
      
      // Act
      bean.delete();
      
      // Assert
      verify(element1).delete();
      verify(rootSpy).deregister(same(bean));
      assertThat(bean.getParent(), is(nullValue()));
   }

   @Test
   public void testGetParent()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      bean.setParent(parent);
      
      // Act
      ModelElement innerParent = bean.getParent();
      
      // Assert
      assertThat(innerParent, is(sameInstance(parent)));
   }

   @Test
   public void testSetParent()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      
      // Act
      bean.setParent(parent);
      
      // Assert
      ModelElement innerParent = bean.getParent();
      assertThat(innerParent, is(sameInstance(parent)));
      assertThat(bean.isModified(), is(equalTo(true)));
   }

   @Test
   public void testEqualsWithSameObject()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      boolean equal = bean.equals(bean);
      
      // Assert
      assertThat(equal, is(equalTo(true)));
   }

   @Test
   public void testEqualsWithObjectFromDifferentClass()
   {
      // Arrange
      TestModelElementBean bean = new TestModelElementBean();
      
      // Act
      boolean equal = bean.equals(new Object());
      
      // Assert
      assertThat(equal, is(equalTo(false)));
   }

   @Test
   public void testEqualsWithAnotherModelElementWhereBothModelsAreNull()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      TestModelElementBean bean2 = new TestModelElementBean();
      
      // Act
      boolean equal1 = bean1.equals(bean2);
      boolean equal2 = bean2.equals(bean1);
      
      // Assert
      assertThat(equal1, is(equalTo(false)));
      assertThat(equal2, is(equalTo(false)));
   }

   @Test
   public void testEqualsWithAnotherModelElementWhereOneModelsIsNull()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean1.setParent(parent);
      
      TestModelElementBean bean2 = new TestModelElementBean();
      
      // Act
      boolean equal1 = bean1.equals(bean2);
      boolean equal2 = bean2.equals(bean1);
      
      // Assert
      assertThat(equal1, is(equalTo(false)));
      assertThat(equal2, is(equalTo(false)));
   }

   @Test
   public void testEqualsWithAnotherModelElementButWithSameParent()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      when(parent.getModel()).thenReturn(root);
      bean1.setParent(parent);
      bean1.register(512);
      
      TestModelElementBean bean2 = new TestModelElementBean();
      bean2.setParent(parent);
      bean2.register(513);
      
      // Act
      boolean equal1 = bean1.equals(bean2);
      boolean equal2 = bean2.equals(bean1);
      
      // Assert
      assertThat(equal1, is(equalTo(false)));
      assertThat(equal2, is(equalTo(false)));
   }

   @Test
   public void testEqualsWithAnotherModelElementInDifferentModel()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      root.setModelOID(1);
      when(parent.getModel()).thenReturn(root);
      bean1.setParent(parent);
      bean1.register(512);
      
      TestModelElementBean bean2 = new TestModelElementBean();
      parent = mock(ModelElement.class);
      root = new ModelBean("model2", "model2", null);
      root.setModelOID(2);
      when(parent.getModel()).thenReturn(root);
      bean2.setParent(parent);
      bean2.register(bean1.getElementOID());
      
      // Act
      boolean equal1 = bean1.equals(bean2);
      boolean equal2 = bean2.equals(bean1);
      
      // Assert
      assertThat(equal1, is(equalTo(false)));
      assertThat(equal2, is(equalTo(false)));
   }

   @Test
   public void testEqualsWithAnotherModelElementInSameModel()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean root = new ModelBean("model1", "model1", null);
      root.setModelOID(1);
      when(parent.getModel()).thenReturn(root);
      bean1.setParent(parent);
      bean1.register(512);
      
      TestModelElementBean bean2 = new TestModelElementBean();
      parent = mock(ModelElement.class);
      // create a new model in order to register a second object with the same elementOID
      root = new ModelBean("model2", "model2", null);
      root.setModelOID(2);
      when(parent.getModel()).thenReturn(root);
      bean2.setParent(parent);
      bean2.register(bean1.getElementOID());
      // bean2 has the same elementOID as bean1 now; if we change the parent to the one
      // of bean1 then both beans are identically
      bean2.setParent(bean1.getParent());
      
      // Act
      boolean equal1 = bean1.equals(bean2);
      boolean equal2 = bean2.equals(bean1);
      
      // Assert
      assertThat(equal1, is(equalTo(true)));
      assertThat(equal2, is(equalTo(true)));
   }

   @Test
   public void testGetModelIfParentIsNull()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      
      // Act
      RootElement model = bean1.getModel();
      
      // Assert
      assertThat(model, is(nullValue()));
   }

   @Test
   public void testGetModelIfParentIsNotNull()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelElement parent = mock(ModelElement.class);
      bean1.setParent(parent);
      
      // Act
      RootElement model = bean1.getModel();
      
      // Assert
      assertThat(model, is(nullValue()));
   }

   @Test
   public void testGetModelInstance()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      ModelBean root = new ModelBean("model1", "model1", null);
      root.setModelOID(1);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(root);
      bean1.setParent(parent);
      
      // Act
      RootElement model = bean1.getModel();
      
      // Assert
      assertThat(model, is(instanceOf(ModelBean.class)));
      assertThat((ModelBean)model, is(sameInstance(root)));
   }

   @Test
   public void testCheckConsistencyWithDescription()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      bean1.setDescription("non-empty");
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckConsistencyWithoutDescription()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckForVariablesWithEmptyText()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean1.checkForVariables(inconsistencies, "", null);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckForVariablesWithNonEmptyText()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean1.checkForVariables(inconsistencies, "non-empty", null);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   @Test
   public void testCheckForVariablesWithVariable()
   {
      // Arrange
      TestModelElementBean bean1 = new TestModelElementBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean1.checkForVariables(inconsistencies, "${model}element", null);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   @SuppressWarnings("serial")
   static class TestModelElementBean extends ModelElementBean
   {
      boolean modified = false;
      
      @Spy
      private Link link = new Link(this, "Link");
      @Spy
      private Connections conn = new Connections(this, "Connection", "role1", "role2");
      @Spy
      private List<ModelElement> modelElements = new ArrayList<ModelElement>(); 
      
      @Override
      public void markModified()
      {
         super.markModified();
         modified = true;
      }
      
      public void setUnmodified()
      {
         modified = false;
      }
      
      public boolean isModified()
      {
         return modified;
      }
   }

}
