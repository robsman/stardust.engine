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
package org.eclipse.stardust.engine.core.model.beans;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.*;
import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ParsedDeploymentUnit;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError.Args;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataType;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemDescription;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLoader;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLocatorUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataFilterExtension;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXMLValidator;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;

public class ModelParticipantBeanTest
{

   @Test
   public void testModelParticipantBean()
   {
      // Act
      TestModelParticipantBean bean = new TestModelParticipantBean();
      
      // Assert
      assertThat(bean.getId(), is(nullValue()));
      assertThat(bean.getName(), is(nullValue()));
      assertThat(bean.getDescription(), is(nullValue()));
   }

   @Test
   public void testModelParticipantBeanWithIdNameDescription()
   {
      // Arrange
      final String id = "id_1";
      final String name = "name_1";
      final String desc = "description_1";
      
      // Act
      TestModelParticipantBean bean = new TestModelParticipantBean(id, name, desc);
      
      // Assert
      assertThat(bean.getId(), is(equalTo(id)));
      assertThat(bean.getName(), is(equalTo(name)));
      assertThat(bean.getDescription(), is(equalTo(desc)));
   }

   @Test
   public void testToString()
   {
      // Arrange
      final String id = "id_1";
      final String name = "name_1";
      TestModelParticipantBean bean = new TestModelParticipantBean(id, name, null);
      
      // Act
      final String toString = bean.toString();
      
      // Assert
      assertThat(toString, is(equalTo("Participant: " + name)));
   }

   @Test
   public void testFindOrganizationInEmptyList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      
      // Act
      IOrganization org = bean.findOrganization("empty");
      
      // Assert
      assertThat(org, is(nullValue()));
   }

   @Test
   public void testFindNonExistingOrganizationInList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      bean.addToOrganizations(new OrganizationBean("org1", "org 1", null));
      bean.addToOrganizations(new OrganizationBean("org2", "org 2", null));
      
      // Act
      IOrganization org = bean.findOrganization("empty");
      
      // Assert
      assertThat(org, is(nullValue()));
   }

   @Test
   public void testFindExistingOrganizationInList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization orgToFind = new OrganizationBean("org2", "org 2", null);
      bean.addToOrganizations(new OrganizationBean("org1", "org 1", null));
      bean.addToOrganizations(orgToFind);
      
      // Act
      IOrganization org = bean.findOrganization(orgToFind.getId());
      
      // Assert
      assertThat(org, is(sameInstance(orgToFind)));
   }

   @Test
   public void testFindNullIdInOrganizationList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization orgToFind = new OrganizationBean("org2", "org 2", null);
      bean.addToOrganizations(new OrganizationBean("org1", "org 1", null));
      bean.addToOrganizations(orgToFind);
      
      // Act
      IOrganization org = bean.findOrganization(null);
      
      // Assert
      assertThat(org, is(nullValue()));
   }

   @Test
   public void testGetAllOrganizationsFromEmptyList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      
      // Act
      Iterator orgIter = bean.getAllOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      assertThat(orgIter.hasNext(), is(equalTo(false)));
   }
   
   @Test
   public void testGetAllOrganizations()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization 
         org1 = new OrganizationBean("org1", "org 1", null),
         org2 = new OrganizationBean("org2", "org 2", null);
      bean.addToOrganizations(org1);
      bean.addToOrganizations(org2);
      
      // Act
      Iterator orgIter = bean.getAllOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      List<IOrganization> orgs = new ArrayList<IOrganization>(2);
      while(orgIter.hasNext())
      {
         orgs.add((IOrganization)orgIter.next());
      }
      assertThat(orgs.size(), is(equalTo(2)));
      assertThat(orgs, containsInAnyOrder(org1, org2));
   }

   @Test
   public void testGetAllOrganizationsWithSubOrgs()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization 
         org1 = new OrganizationBean("org1", "org 1", null),
         org2 = new OrganizationBean("org2", "org 2", null);
      bean.addToOrganizations(org1);
      bean.addToOrganizations(org2);
      org1.addToSubOrganizations(new OrganizationBean("org3", "org 3", null));
      
      // Act
      Iterator orgIter = bean.getAllOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      List<IOrganization> orgs = new ArrayList<IOrganization>(2);
      while(orgIter.hasNext())
      {
         orgs.add((IOrganization)orgIter.next());
      }
      assertThat(orgs.size(), is(equalTo(2)));
      assertThat(orgs, containsInAnyOrder(org1, org2));
   }

   @Test
   public void testGetAllTopLevelOrganizationsFromEmptyList()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      
      // Act
      Iterator orgIter = bean.getAllTopLevelOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      assertThat(orgIter.hasNext(), is(equalTo(false)));
   }

   @Test
   public void testGetAllTopLevelOrganizationsWhereNoSubOrgExists()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization 
         org1 = new OrganizationBean("org1", "org 1", null),
         org2 = new OrganizationBean("org2", "org 2", null);
      bean.addToOrganizations(org1);
      bean.addToOrganizations(org2);
      
      // Act
      Iterator orgIter = bean.getAllTopLevelOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      List<IOrganization> orgs = new ArrayList<IOrganization>(2);
      while(orgIter.hasNext())
      {
         orgs.add((IOrganization)orgIter.next());
      }
      assertThat(orgs.size(), is(equalTo(2)));
      assertThat(orgs, containsInAnyOrder(org1, org2));
   }

   @Test
   public void testGetAllTopLevelOrganizationsWhereSubOrgsExists()
   {
      // Arrange
      IOrganization 
         org1 = new OrganizationBean("org1", "org 1", null),
         org2 = new OrganizationBean("org2", "org 2", null),
         org3 = new OrganizationBean("org3", "org 3", null),
         org4 = new OrganizationBean("org4", "org 4", null),
         org5 = new OrganizationBean("org5", "org 5", null),
         org6 = new OrganizationBean("org6", "org 6", null),
         org7 = new OrganizationBean("org7", "org 7", null);
      
      org1.addToSubOrganizations(org3);
      org1.addToSubOrganizations(org4);
      org2.addToSubOrganizations(org4);
      org2.addToSubOrganizations(org5);
      org4.addToSubOrganizations(org7);
      org5.addToSubOrganizations(org7);
      org5.addToSubOrganizations(org6);
      
      // Act
      Iterator orgIter = org7.getAllTopLevelOrganizations();
      
      // Assert
      assertThat(orgIter, is(notNullValue()));
      List<IOrganization> orgs = new ArrayList<IOrganization>(2);
      while(orgIter.hasNext())
      {
         orgs.add((IOrganization)orgIter.next());
      }
      assertThat(orgs.size(), is(equalTo(2)));
      assertThat(orgs, containsInAnyOrder(org1, org2));
   }

   @Test
   public void testCheckConsistencyWithNoModel()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         // Model element without an attached model should not be possible and therefore
         // checkConsistency should add an inconsistency here... 
         bean.checkConsistency(inconsistencies);
         fail("An exception is expected because the model wasn't set");
      }
      catch(InternalException e)
      {
         // ...but unfortunately this case is not correctly handled by checkConsistency.
         // Instead of that a NullPointerException is thrown which is wrapped in an
         // InternalException

         // Assert
         assertThat(inconsistencies.size(), is(equalTo(1)));
         assertThat(inconsistencies.get(0).getErrorID(), 
               is(equalTo(errorId(BpmValidationError.VAL_HAS_NO_ID))));
      }
   }

   @Test
   public void testCheckConsistencyWithNoId()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      bean.setParent(parent);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      bean.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.VAL_HAS_NO_ID))));
   }
   
   @Test
   public void testCheckConsistencyWithDuplicatedParticipantIds()
   {
      // Arrange
      IOrganization org1 = new OrganizationBean("part1", null, null);
      IOrganization org2 = new OrganizationBean("part1", null, null);
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      model.addParticipant(0, org2);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_DUPLICATE_ID))));
   }
   
   @Test
   public void testCheckConsistencyWithTooLongId()
   {
      // Arrange
      String id = generateId("part1", AuditTrailParticipantBean.getMaxIdLength() + 1);
      IOrganization org1 = new OrganizationBean(id, null, null);
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_ID_EXCEEDS_MAXIMUM_LENGTH))));
   }

   @Test
   public void testCheckConsistencyIfParticipantIsNotRegisteredInTheModel()
   {
      // Arrange
      IOrganization org1 = new OrganizationBean("part1", null, null);
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      org1.setParent(parent);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      // Seems that this case is not correctly handled in checkConsistency
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckConsistencyIfParticipantHasNonExiustingUUID()
   {
      // Arrange
      IOrganization org1 = new OrganizationBean("part1?uuid=12345&", null, null);
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      org1.setParent(parent);
      model.addToParticipants(org1);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_ASSOCIATED_ORGANIZATION_SET_FOR_PARTICIPANT_DOES_NOT_EXIST))));
   }

   @Test
   public void testCheckConsistencyIfScopedOrganizationReferencesNonExistingData()
   {
      // Arrange
      IOrganization org1 = new OrganizationBean("part1", null, null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, "data1");
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_DATA_FOR_SCOPED_ORGANIZATION_MUST_EXIST))));
   }

   @Test
   public void testCheckConsistencyIfScopedOrganizationWithNoDataBindingId()
   {
      // Arrange
      IOrganization org1 = new OrganizationBean("part1", null, null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, null);
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL))));
   }

   @Test
   public void testCheckConsistencyWithScopedOrganizationAndNonPrimOrStructDataType()
   {
      // Arrange
      IData data = new DataBean("data1", new JavaDataType(), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.CLASS_NAME_ATT, String.class.getName());
      IOrganization org1 = new OrganizationBean("part1", null, null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      ModelElement parent = mock(ModelElement.class);
      ModelBean model = new ModelBean("model1", "model 1", null);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      model.addToData(data);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_DATA_OF_SCOPED_ORGANIZATION_CAN_ONLY_BE_PRIM_OR_STRUCT))));
   }

   @Test
   public void testCheckConsistencyWithScopedOrganizationAndByteDataType()
   {
      // Arrange
      ModelBean model = new ModelBean("model1", "model 1", null);
      DefaultModelBuilder.createPredefinedDataTypes(model);
      IData data = new DataBean("data1", model.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.Byte);
      IOrganization org1 = new OrganizationBean("part1", null, null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      model.addToData(data);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      org1.checkConsistency(inconsistencies);
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), 
            is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), 
            is(equalTo(errorId(BpmValidationError.PART_TYPE_OF_DATA_OF_SCOPED_ORGANIZATION_IS_NOT))));
   }
   
   @Test
   public void testCheckConsistencyWithScopedOrganizationStringDataTypeAndSuperOrg()
   {
      // Arrange
      registerModelManager();
      ModelBean model = new ModelBean("model1", "model 1", null);
      DefaultModelBuilder.createPredefinedDataTypes(model);
      IData data = new DataBean("data1", model.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      IOrganization org1 = new OrganizationBean("part1", null, null);
      IOrganization superOrg = new OrganizationBean("superOrg", "Super Org", null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(model);
      model.addToData(data);
      model.addParticipant(0, org1);
      model.addParticipant(0, superOrg);
      org1.addToOrganizations(superOrg);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }

   @Test
   public void testCheckConsistencyWithScopedOrganizationAndStructDataType()
   {
      // Arrange
      registerModelManager();
      ModelBean model = new ModelBean("model1", "model 1", null);
      DefaultModelBuilder.createPredefinedDataTypes(model);
      createStructuredDataType(model);
      createTypeDeclaration("myStructType", model);
      IData data = new DataBean("data1", model.findDataType(PredefinedConstants.STRUCTURED_DATA), 
            null, null, false, null);
      data.setAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT, "myStructType");
      IOrganization org1 = new OrganizationBean("part1", null, null);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      org1.setAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT, "field1");
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(model);
      model.setModelOID(1);
      model.addParticipant(0, org1);
      model.addToData(data);
      data.setParent(parent);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getRuntimeOid(Mockito.any(IData.class), eq("field1"))).thenReturn(15l);
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   @Test
   public void testCheckConsistencyWithMultipleSuperOrgs()
   {
      // Arrange
      registerModelManager();
      ModelBean model = new ModelBean("model1", "model 1", null);
      IOrganization org1 = new OrganizationBean("org1", null, null);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(model);
      model.addParticipant(0, org1);
      org1.addToOrganizations(new OrganizationBean("org2", "org 2", null));
      org1.addToOrganizations(new OrganizationBean("org3", "org 3", null));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MULTIPLE_SOPER_ORGANIZATIONS_ARE_NOT_ALLOWED))));
   }
   
   @Test
   public void testCheckConsistencyWithUnchangedOrganizationsBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(16);
      IOrganization org1 = new OrganizationBean("part1", "part 1", null);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      newModel.addParticipant(0, org1);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(15);
      IOrganization org1Clone = clone(org1);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      oldModel.addParticipant(0, org1Clone);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   @Test
   public void testCheckConsistencyIfScopedOrganizationIsRemovedBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      oldModel.addParticipant(0, org);
      
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      IData data = new DataBean("data1", oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org = new OrganizationBean("partOld", "part (old)", null);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   @Test
   public void testCheckConsistencyIfScopedOrganizationBecomesUnscopedBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      IData data = new DataBean("data1", oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_ORGANIZATION_IS_UNSCOPED_BUT_IN_AUDITTRAIL_SCOPED))));
   }
   
   @Test
   public void testCheckConsistencyIfUncopedOrganizationBecomesScopedBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      newModel.addToData(data);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      oldModel.addParticipant(0, org);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_ORGANIZATION_IS_SCOPED_BUT_IN_AUDITTRAIL_UNSCOPED))));
   }
   
   @Test
   public void testCheckConsistencyIfScopedOrganizationBecomesDifferentDataIdBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      newModel.addToData(data);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      oldModel.addParticipant(0, org);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean("data2", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      oldModel.addToData(data);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_ID_IN_AUDIT_TRAIL))));
   }

   @Test
   public void testCheckConsistencyWhenDataPathIsAddedInScopedOrganizationBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      newModel.addToData(data);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      org1.setAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT, "toString()");
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      oldModel.addParticipant(0, org);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      oldModel.addToData(data);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_PATH_IN_AUDIT_TRAIL))));
   }
   
   @Test
   public void testCheckConsistencyIfScopedOrganizationBecomesDifferentDataPathBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      newModel.addToData(data);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      org1.setAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT, "toString()");
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);

      ModelBean oldModel = new ModelBean("model1", "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      oldModel.addParticipant(0, org);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      oldModel.addToData(data);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      org.setAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT, "toString().toString()");
      
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq("model1"))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_PATH_IN_AUDIT_TRAIL))));

   }

   @Test
   public void testCheckConsistencyIfScopedOrganizationHasRemovedTeamleadBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      IRole teamLead = new RoleBean("teamLead", "Team Lead", null);
      newModel.addParticipant(0, teamLead);
      org1.addToParticipants(teamLead);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      teamLead = new RoleBean(teamLead.getId(), teamLead.getName(), null);
      oldModel.addParticipant(0, teamLead);
      org.setTeamLead(teamLead);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_MANAGER_OF_ASSOCIATION_THAN_DEPLOYED_MODEL))));
   }
   
   @Test
   public void testCheckConsistencyIfScopedOrganizationHasDifferentTeamleadBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      IRole teamLead = new RoleBean("teamLead", "Team Lead", null);
      newModel.addParticipant(0, teamLead);
      org1.setTeamLead(teamLead);
      org1.addToParticipants(teamLead);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      teamLead = new RoleBean("teamLead1", teamLead.getName(), null);
      oldModel.addParticipant(0, teamLead);
      org.setTeamLead(teamLead);
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_MANAGER_OF_ASSOCIATION_THAN_DEPLOYED_MODEL))));
   }

   @Test
   public void testCheckConsistencyIfScopedOrganizationHasRemovedRoleBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      IRole role = new RoleBean("role1", "Role1", null);
      newModel.addParticipant(0, role);
      org.addToParticipants(role);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL))));
   }

   @Test
   public void testCheckConsistencyIfScopedOrganizationHasRemovedOrgBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      IOrganization subOrg = new OrganizationBean("subOrg", "Sub Org", null);
      newModel.addParticipant(0, subOrg);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      subOrg = new OrganizationBean("subOrg", "Sub Org", null);
      oldModel.addParticipant(0, subOrg);
      org.addToParticipants(subOrg);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL))));
   }
   

   @Test
   public void testCheckConsistencyIfScopedOrganizationHasAddedOrgBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      IOrganization subOrg = new OrganizationBean("subOrg", "Sub Org", null);
      newModel.addParticipant(0, subOrg);
      org1.addToParticipants(subOrg);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      subOrg = new OrganizationBean("subOrg", "Sub Org", null);
      oldModel.addParticipant(0, subOrg);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.size(), is(equalTo(1)));
      Inconsistency inconsistency = inconsistencies.get(0);
      assertThat(inconsistency.getSeverity(), is(equalTo(Inconsistency.ERROR)));
      assertThat(inconsistency.getErrorID(), is(equalTo(errorId(
            BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL))));
   }
   
   @Test
   public void testCheckConsistencyWithTwoScopedParticipantTreesBetweenModelVersions()
   {
      // Arrange
      registerModelManager();
      ModelBean newModel = new ModelBean("model1", "model 1 (new)", null);
      newModel.setModelOID(15);
      IOrganization org1 = new OrganizationBean("partNew", "part (new)", null);
      newModel.addParticipant(0, org1);
      DefaultModelBuilder.createPredefinedDataTypes(newModel);
      IData data = new DataBean("data1", newModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org1.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org1.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      newModel.addToData(data);
      IOrganization subOrg1 = new OrganizationBean("subOrg1", "Sub Org 1", null);
      newModel.addParticipant(0, subOrg1);
      org1.addToParticipants(subOrg1);
      IOrganization subOrg3 = new OrganizationBean("subOrg3", "Sub Org 3", null);
      newModel.addParticipant(0, subOrg3);
      org1.addToParticipants(subOrg3);
      IOrganization subOrg2 = new OrganizationBean("subOrg2", "Sub Org 2", null);
      newModel.addParticipant(0, subOrg2);
      subOrg3.addToParticipants(subOrg2);
      ModelElement parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(newModel);
      
      ModelBean oldModel = new ModelBean(newModel.getId(), "model 1 (old)", null);
      oldModel.setModelOID(16);
      parent = mock(ModelElement.class);
      when(parent.getModel()).thenReturn(oldModel);
      IOrganization org = clone(org1);
      
      DefaultModelBuilder.createPredefinedDataTypes(oldModel);
      data = new DataBean(data.getId(), oldModel.findDataType(PredefinedConstants.PRIMITIVE_DATA), 
            null, null, false, null);
      data.setAttribute(PredefinedConstants.TYPE_ATT, Type.String);
      org.setAttribute(PredefinedConstants.BINDING_ATT, Boolean.TRUE);
      org.setAttribute(PredefinedConstants.BINDING_DATA_ID_ATT, data.getId());
      oldModel.addParticipant(0, org);
      oldModel.addToData(data);
      subOrg1 = new OrganizationBean("subOrg1", "Sub Org 1", null);
      oldModel.addParticipant(0, subOrg1);
      org.addToParticipants(subOrg1);
      subOrg3 = new OrganizationBean("subOrg3", "Sub Org 3", null);
      oldModel.addParticipant(0, subOrg3);
      org.addToParticipants(subOrg3);
      subOrg2 = new OrganizationBean("subOrg2", "Sub Org 2", null);
      oldModel.addParticipant(0, subOrg2);
      subOrg3.addToParticipants(subOrg2);
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      when(modelManager.getModelsForId(eq(oldModel.getId()))).thenReturn(
            Arrays.<IModel>asList(oldModel));
      
      List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
      
      // Act
      try
      {
         org1.checkConsistency(inconsistencies);
      }
      finally
      {
         unregisterModelManager();
      }
      
      // Assert
      assertThat(inconsistencies.isEmpty(), is(equalTo(true)));
   }
   
   private IOrganization clone(IOrganization org)
   {
      return new OrganizationBean(org.getId(), org.getName(), org.getDescription());
   }
   
   private void registerModelManager()
   {
      ItemLocatorUtils.registerDescription(ModelManagerFactory.ITEM_NAME, 
            new ItemDescription(new ItemLoader()
            {
               @Override
               public Object load()
               {
                  ModelManager mm = mock(ModelManager.class);
                  return mm;
               }
            }));
   }
   
   private void unregisterModelManager()
   {
      ItemLocatorUtils.unregisterDescription(ModelManagerFactory.ITEM_NAME);
   }
   
   private void createStructuredDataType(IModel model)
   {
      IDataType structType = model.createDataType(
            PredefinedConstants.STRUCTURED_DATA, "Structured Data", false, 0);
      structType.setAttribute(PredefinedConstants.EVALUATOR_CLASS_ATT, 
            StructuredDataXPathEvaluator.class.getName());
      structType.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
            StructuredDataXMLValidator.class.getName());
      structType.setAttribute(PredefinedConstants.DATA_FILTER_EXTENSION_ATT,
            StructuredDataFilterExtension.class.getName());
      structType.setAttribute(PredefinedConstants.DATA_LOADER_ATT,
            StructuredDataLoader.class.getName());
   }
   
   private void createTypeDeclaration(String id, IModel model)
   {
      XSDSchema schema = mock(XSDSchema.class);
      XSDElementDeclaration xsdElemDecl = mock(XSDElementDeclaration.class);
      XSDComplexTypeDefinition xsdTypeDefinition = mock(XSDComplexTypeDefinition.class);
      XSDParticle part = mock(XSDParticle.class);
      XSDParticle elemPart = mock(XSDParticle.class);
      XSDElementDeclaration elemDecl = mock(XSDElementDeclaration.class);
      XSDModelGroup xsdGroup = mock(XSDModelGroup.class);
      EList<XSDElementDeclaration> elementDecls = new BasicEList<XSDElementDeclaration>();
      EList<XSDAttributeGroupContent> attributeContents = new BasicEList<XSDAttributeGroupContent>();
      EList<XSDParticle> partList = new BasicEList<XSDParticle>();
      elementDecls.add(xsdElemDecl);
      partList.add(elemPart);
      when(schema.getElementDeclarations()).thenReturn(elementDecls);
      when(xsdElemDecl.getName()).thenReturn(id);
      when(xsdElemDecl.getType()).thenReturn(xsdTypeDefinition);
      when(xsdTypeDefinition.getName()).thenReturn(id);
      when(xsdTypeDefinition.getAttributeContents()).thenReturn(attributeContents);
      when(xsdTypeDefinition.getContent()).thenReturn(part);
      when(part.getTerm()).thenReturn(xsdGroup);
      when(xsdGroup.getContents()).thenReturn(partList);
      when(elemPart.getTerm()).thenReturn(elemDecl);
      XSDSimpleTypeDefinition elemStringTypeDef = mock(XSDSimpleTypeDefinition.class); 
      when(elemDecl.getTypeDefinition()).thenReturn(elemStringTypeDef);
      when(elemDecl.getName()).thenReturn("field1");
      when(elemStringTypeDef.getName()).thenReturn("string");
      IXpdlType xpdlType = new SchemaTypeBean(schema);
      ITypeDeclaration typeDeclaration = 
            model.createTypeDeclaration(id, id, null, Collections.emptyMap(), xpdlType);
      xpdlType.setParent(typeDeclaration);
      
   }
   
   private String generateId(String pattern, int length)
   {
      StringBuilder id = new StringBuilder(length);
      for(int pos = 0, patternPos = 0; pos < length; pos++, patternPos++)
      {
         id.append(pattern.charAt(patternPos));
         if(patternPos == (pattern.length()-1))
         {
            patternPos = -1;
         }
      }
      return id.toString();
   }

   static private String errorId(Args errorArg)
   {
      return errorArg.raise("").getId();
   }

   @Test
   public void testAddToOrganizations()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      IOrganization org1 = new OrganizationBean("org1", "org 1", null);
  
      // Act
      bean.addToOrganizations(org1);
      
      // Assert
      Iterator orgIter = bean.getAllOrganizations();
      assertThat(orgIter, is(notNullValue()));
      assertThat(orgIter.hasNext(), is(equalTo(true)));
      Object orgCandidate = orgIter.next();
      assertThat(orgCandidate, is(instanceOf(IOrganization.class)));
      assertThat((IOrganization)orgCandidate, is(sameInstance(org1)));
      assertThat(orgIter.hasNext(), is(equalTo(false)));
   }

   @Test
   public void testAddNullOrganization()
   {
      // Arrange
      TestModelParticipantBean bean = new TestModelParticipantBean();
      
      // Act
      bean.addToOrganizations(null);
      
      // Assert
      Iterator orgIter = bean.getAllOrganizations();
      assertThat(orgIter, is(notNullValue()));
      assertThat(orgIter.hasNext(), is(equalTo(true)));
      Object orgCandidate = orgIter.next();
      assertThat(orgCandidate, is(nullValue()));
      assertThat(orgIter.hasNext(), is(equalTo(false)));
   }

   @SuppressWarnings("serial")
   static class TestModelParticipantBean extends ModelParticipantBean
   {
      boolean userAuthorized = false;
      boolean participantAuthorized = false;
      boolean userGroupAuthorized = false;
      
      List<IModelParticipant> participants;
      
      TestModelParticipantBean()
      {
         super();
      }
      
      TestModelParticipantBean(String id, String name, String description)
      {
         super(id, name, description);
      }
      
      @Override
      public boolean isAuthorized(IModelParticipant participant)
      {
         return participantAuthorized;
      }

      @Override
      public boolean isAuthorized(IUser user)
      {
         return userAuthorized;
      }

      @Override
      public boolean isAuthorized(IUserGroup userGroup)
      {
         return userGroupAuthorized;
      }
      
      public void setAuthorization(boolean user, boolean participant, boolean userGroup)
      {
         this.userAuthorized = user;
         this.participantAuthorized = participant;
         this.userGroupAuthorized = userGroup;
      }
      
      public void setParticipants(List<IModelParticipant> participants)
      {
         participants = Collections.unmodifiableList(participants);
      }

      @Override
      public Iterator getAllParticipants()
      {
         return participants !=null ? participants.iterator() : 
            Collections.emptyList().iterator();
      }

      @Override
      public int getCardinality()
      {
         return 0;
      }
      
   }
}
