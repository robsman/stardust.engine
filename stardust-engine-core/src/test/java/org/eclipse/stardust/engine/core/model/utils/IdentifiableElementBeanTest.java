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
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError.Args;
import org.eclipse.stardust.engine.core.model.beans.*;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataType;

public class IdentifiableElementBeanTest
{

   @Test
   public void testGetUniqueIdWithEmptyId()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean();
      
      // Act
      String uniqueId = bean.getUniqueId();
      
      // Assert
      assertThat(uniqueId, is(equalTo(TestIdentifiableElementBean.class.getName() + ":null")));
   }

   @Test
   public void testGetUniqueIdWithId()
   {
      // Arrange
      final String id = "idBean";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(id, null);
      
      // Act
      String uniqueId = bean.getUniqueId();
      
      // Assert
      assertThat(uniqueId, is(equalTo(TestIdentifiableElementBean.class.getName() + ":" + id)));
   }

   @Test
   public void testSetParent()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean("id", null);
      ModelElement parent = mock(ModelElement.class);
      // initializes the qualifiedId field so that it is not null
      bean.getQualifiedId();
      
      // Act
      bean.setParent(parent); // which sets qualifiedId to null
      
      // Assert
      assertThat(bean.getParent(), is(sameInstance(parent)));
      // Lets initialize the qualifiedId field again...
      bean.getQualifiedId();
      // ...for this the getModel() method must be invoked
      verify(parent).getModel();
   }

   @Test
   public void testIdentifiableElementBean()
   {
      // Act
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean();
      
      // Assert
      assertThat(bean.getId(), is(nullValue()));
      assertThat(bean.getName(), is(nullValue()));
      assertThat(bean.getQualifiedId(), is(nullValue()));
   }

   @Test
   public void testIdentifiableElementBeanWithIdAndName()
   {
      // Arrange
      final String id = "id";
      final String name = "name";
      
      // Act
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(id, name);
      
      // Assert
      assertThat(bean.getId(), is(equalTo(id)));
      assertThat(bean.getName(), is(equalTo(name)));
      assertThat(bean.getQualifiedId(), is(equalTo(id)));
   }

   @Test
   public void testSetDifferentName()
   {
      // Arrange
      final String iName = "name";
      final String suffix = "_1";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(null, iName);
      
      // Act
      bean.setName(iName+suffix);
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getName(), is(equalTo(iName+suffix)));
   }

   @Test
   public void testSetNullName()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(null, "name");
      
      // Act
      bean.setName(null);
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getName(), is(nullValue()));
   }

   @Test
   public void testSetDifferentId()
   {
      // Arrange
      final String iId = "id";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(iId, null);
      final String iQualifiedId = bean.getQualifiedId();
      final String newId = iId + "_1";
      
      // Act
      bean.setId(newId);
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getId(), is(equalTo(newId)));
      assertThat(iQualifiedId, is(equalTo(iId)));
      assertThat(bean.getQualifiedId(), is(equalTo(newId)));
   }

   @Test
   public void testSetNullId()
   {
      // Arrange
      final String iId = "id";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(iId, null);
      final String iQualifiedId = bean.getQualifiedId();
      
      // Act
      bean.setId(null);
      
      // Assert
      assertThat(bean.isModified(), is(equalTo(true)));
      assertThat(bean.getId(), is(nullValue()));
      assertThat(iQualifiedId, is(equalTo(iId)));
      assertThat(bean.getQualifiedId(), is(nullValue()));
   }

   @Test
   public void testGetQualifiedId()
   {
      // Arrange
      final String iId = "id";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(iId, null);
      ModelElement parent = mock(ModelElement.class);
      bean.setParent(parent);
      
      // Act
      final String iQualifiedId = bean.getQualifiedId();
      
      // Assert
      assertThat(iQualifiedId, is(equalTo(iId)));
      verify(parent, times(1)).getModel();
   }

   @Test
   public void testGetCachedQualifiedId()
   {
      // Arrange
      final String iId = "id";
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean(iId, null);
      ModelElement parent = mock(ModelElement.class);
      bean.setParent(parent);
      
      // Act
      final String qualifiedId1 = bean.getQualifiedId();
      final String qualifiedId2 = bean.getQualifiedId();
      
      // Assert
      assertThat(qualifiedId1, is(equalTo(iId)));
      assertThat(qualifiedId2, is(equalTo(iId)));
      verify(parent, times(1)).getModel();
   }

   @Test
   public void testCheckIdWithNullId()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();

      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.VAL_HAS_NO_ID))));
   }

   @Test
   public void testCheckIdWithEmptyId()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean("", null);
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.VAL_HAS_NO_ID))));
   }

   @Test
   public void testCheckIdWithNonEmptyId()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean("myId", null);
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckIdWithValidDataBeanId()
   {
      // Arrange
      DataBean bean = new DataBean("data1", null, null, null, false, null);
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckIdWithInvalidDataBeanId()
   {
      // Arrange
      DataBean bean = new DataBean("1data", null, null, null, false, null);
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.WARNING)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.VAL_HAS_INVALID_ID))));
   }
   
   @Test
   public void testCheckIdWithValidAccessPointBeanId()
   {
      // Arrange
      ApplicationBean app = new ApplicationBean(null, null, null);
      ModelElement parent = mock(ModelElement.class);
      app.setParent(parent);
      RootElement root = new ModelBean("model1", null, null);
      when(parent.getModel()).thenReturn(root );
      AccessPointBeanAdapter bean = new AccessPointBeanAdapter(app.createAccessPoint(
            "accesPoint", null, Direction.IN_OUT, new JavaDataType(), 0));
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckIdWithInvalidAccessPointBeanId()
   {
      // Arrange
      ApplicationBean app = new ApplicationBean(null, null, null);
      ModelElement parent = mock(ModelElement.class);
      app.setParent(parent);
      RootElement root = new ModelBean("model1", null, null);
      when(parent.getModel()).thenReturn(root );
      AccessPointBeanAdapter bean = new AccessPointBeanAdapter(app.createAccessPoint(
            "1accesPoint", null, Direction.IN_OUT, new JavaDataType(), 0));
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkId(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.WARNING)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.VAL_HAS_INVALID_ID))));
   }
   
   @Test
   public void testCheckConsistency()
   {
      // Arrange
      TestIdentifiableElementBean bean = new TestIdentifiableElementBean("myId", null);
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   static private String errorId(Args errorArg)
   {
      return errorArg.raise("").getId();
   }
   
   @SuppressWarnings("serial")
   static class TestIdentifiableElementBean extends IdentifiableElementBean
   {
      boolean modified = false;
      
      TestIdentifiableElementBean()
      {
         super();
      }
      
      TestIdentifiableElementBean(String id, String name)
      {
         super(id, name);
      }
      
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
