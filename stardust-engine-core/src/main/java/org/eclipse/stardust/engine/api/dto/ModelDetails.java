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
package org.eclipse.stardust.engine.api.dto;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Internal;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocator;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelDetails extends DeployedModelDescriptionDetails implements DeployedModel
{
   private static final long serialVersionUID = 8197470080265242255L;

   final List processes;
   final List roles;
   final List organizations;
   final List topLevelRoles;
   final List topLevelOrganizations;
   final List data;
   final List<TypeDeclaration> typeDeclarations;

   private final List unmodifiableProcesses;
   private final List unmodifiableRoles;
   private final List unmodifiableOrganizations;
   private final List unmodifiableTopLevelRoles;
   private final List unmodifiableTopLevelOrganizations;
   private final List unmodifiableData;
   private final List<TypeDeclaration> unmodifiableTypeDeclarations;

   final Map indexedPDs;
   final Map indexedRoles;
   final Map indexedOrgs;
   final Map indexedData;
   final Map<String, TypeDeclaration> indexedTypeDecls;

   private Set<QualityAssuranceCode> qualityAssuranceCodes;
   private Map<String, Long> externalPackages;

   private final Boolean alive;

   private transient volatile SchemaLocatorAdapter schemaLocatorAdapter = new SchemaLocatorAdapter(this);

   public ModelDetails(IModel model)
   {
      super(model);

      processes = CollectionUtils.newList();
      roles = CollectionUtils.newList();
      organizations = CollectionUtils.newList();
      topLevelRoles = CollectionUtils.newList();
      topLevelOrganizations = CollectionUtils.newList();
      data = CollectionUtils.newList();
      typeDeclarations = CollectionUtils.newList();

      indexedPDs = new LinkedHashMap();
      indexedRoles = new LinkedHashMap();
      indexedOrgs = new LinkedHashMap();
      indexedData = new LinkedHashMap();
      indexedTypeDecls = new LinkedHashMap();

      qualityAssuranceCodes = CollectionUtils.newSet();

      alive = null;

      for (IProcessDefinition process : model.getProcessDefinitions())
      {
         ProcessDefinition processDefinition = (ProcessDefinition) DetailsFactory.create(process,
               IProcessDefinition.class, ProcessDefinitionDetails.class);
         processes.add(processDefinition);
         indexedPDs.put(composeId(processDefinition.getId()), processDefinition);
      }

      ModelElementList<IModelParticipant> participants = model.getParticipants();
      for (IModelParticipant participant : participants)
      {
         if (participant instanceof IRole)
         {
            Role details = (Role) DetailsFactory.create(participant,
                  IRole.class, RoleDetails.class);
            roles.add(details);
            indexedRoles.put(composeId(details.getId()), details);
            if (details.getAllSuperOrganizations().size() == 0)
            {
               topLevelRoles.add(details);
            }
         }
         else if (participant instanceof IOrganization)
         {
            Organization details = (Organization) DetailsFactory.create(participant,
                  IOrganization.class, OrganizationDetails.class);
            organizations.add(details);
            indexedOrgs.put(composeId(details.getId()), details);
            if (details.getAllSuperOrganizations().size() == 0)
            {
               topLevelOrganizations.add(details);
            }
         }
      }

      for (IData data : model.getData())
      {
         Data dataDetails = (Data) DetailsFactory.create(data,
               IData.class, DataDetails.class);
         this.data.add(dataDetails);
         indexedData.put(composeId(dataDetails.getId()), dataDetails);
      }

      for (ITypeDeclaration typeDeclaration : model.getTypeDeclarations())
      {
         TypeDeclaration typeDeclarationDetails = (TypeDeclaration) DetailsFactory.create(typeDeclaration,
               ITypeDeclaration.class, TypeDeclarationDetails.class);
         typeDeclarations.add(typeDeclarationDetails);
         indexedTypeDecls.put(composeId(typeDeclarationDetails.getId()), typeDeclarationDetails);
      }

      IQualityAssurance qualityAssurance = model.getQualityAssurance();
      if (qualityAssurance != null)
      {
         for (IQualityAssuranceCode code : qualityAssurance.getAllCodes())
         {
            QualityAssuranceCode qualityAssuranceCodeDetails = DetailsFactory.create(code,
                  IQualityAssuranceCode.class, QualityAssuranceCodeDetails.class);
            qualityAssuranceCodes.add(qualityAssuranceCodeDetails);
         }
      }

      List<IExternalPackage> packages = model.getExternalPackages();
      if (!packages.isEmpty())
      {
         Map<String, Long> externalPackages = CollectionUtils.newMap();
         for (IExternalPackage pkg : packages)
         {
            IModel referencedModel = pkg.getReferencedModel();
            if (referencedModel != null)
            {
               externalPackages.put(pkg.getId(), (long) referencedModel.getModelOID());
            }
         }
         if (!externalPackages.isEmpty())
         {
            this.externalPackages = externalPackages;
         }
      }

      unmodifiableProcesses = Collections.unmodifiableList(processes);
      unmodifiableRoles = Collections.unmodifiableList(roles);
      unmodifiableData  = Collections.unmodifiableList(data);
      unmodifiableOrganizations = Collections.unmodifiableList(organizations);
      unmodifiableTopLevelOrganizations = Collections.unmodifiableList(topLevelOrganizations);
      unmodifiableTopLevelRoles = Collections.unmodifiableList(topLevelRoles);
      this.unmodifiableTypeDeclarations = Collections.unmodifiableList(typeDeclarations);
   }

   protected ModelDetails(ModelDetails template, Boolean alive)
   {
      super(template);

      processes = template.processes;
      roles = template.roles;
      organizations = template.organizations;
      topLevelRoles = template.topLevelRoles;
      topLevelOrganizations = template.topLevelOrganizations;
      data = template.data;
      typeDeclarations = template.typeDeclarations;

      indexedPDs = template.indexedPDs;
      indexedRoles = template.indexedRoles;
      indexedOrgs = template.indexedOrgs;
      indexedData = template.indexedData;
      indexedTypeDecls = template.indexedTypeDecls;

      unmodifiableProcesses = Collections.unmodifiableList(processes);
      unmodifiableRoles = Collections.unmodifiableList(roles);
      unmodifiableData  = Collections.unmodifiableList(data);
      unmodifiableOrganizations = Collections.unmodifiableList(organizations);
      unmodifiableTopLevelOrganizations = Collections.unmodifiableList(topLevelOrganizations);
      unmodifiableTopLevelRoles = Collections.unmodifiableList(topLevelRoles);
      unmodifiableTypeDeclarations = template.unmodifiableTypeDeclarations;

      qualityAssuranceCodes = template.qualityAssuranceCodes;
      this.externalPackages = template.externalPackages;

      this.alive = alive;
   }

   public boolean isAlive()
   {
      if (null == alive)
      {
         throw new UnsupportedOperationException("Aliveness status is not available.");
      }
      return alive.booleanValue();
   }

   public List getAllParticipants()
   {
      return new ConcatenatedList(roles, organizations);
   }

   public Participant getParticipant(String id)
   {
      id = composeDefaultId(id);
      if (indexedRoles.containsKey(id))
      {
         return (Participant) indexedRoles.get(id);
      }
      else
      {
         return (Participant) indexedOrgs.get(id);
      }
   }

   /*
    * Returns an iterator with detail objects (<code>RoleDetails</code>)
    * for all roles of the model this details object refers to.
    */
   public List getAllOrganizations()
   {
      return unmodifiableOrganizations;
   }

   public Organization getOrganization(String id)
   {
      return (Organization) indexedOrgs.get(composeDefaultId(id));
   }

   /*
    * Returns an iterator with detail objects (<code>OrganizationDetails</code>)
    * for all organizations of the model this details object refers to.
    */
   public List getAllRoles()
   {
      return unmodifiableRoles;
   }

   public Role getRole(String id)
   {
      return (Role) indexedRoles.get(composeDefaultId(id));
   }

   /*
    * Returns an iterator with detail objects (<code>OrganizationDetails</code>)
    * for all top level organizations of the model this details object refers to.
    */
   public List getAllTopLevelOrganizations()
   {
      return unmodifiableTopLevelOrganizations;
   }

   /*
    * Returns an iterator with detail objects (<code>RoleDetails</code>)
    * for all top level roles of the model this details object refers to.
    *
    * @see RoleDetails
    */
   public List getAllTopLevelRoles()
   {
      return unmodifiableTopLevelRoles;
   }

   public List getAllProcessDefinitions()
   {
      return unmodifiableProcesses;
   }

   public ProcessDefinition getProcessDefinition(String id)
   {
      return (ProcessDefinition) indexedPDs.get(composeDefaultId(id));
   }

   public List /*<Data>*/ getAllData()
   {
      return unmodifiableData;
   }

   public Data getData(String id)
   {
      return (Data) indexedData.get(composeDefaultId(id));
   }

   private String composeDefaultId(String id)
   {
      return id == null ? null : id.startsWith("{") ? id : composeId(id);
   }

   private String composeId(String id)
   {
      return "{" + getId() + "}" + id;
   }

   public List<TypeDeclaration> getAllTypeDeclarations()
   {
      for (TypeDeclaration decl : unmodifiableTypeDeclarations)
      {
         updateSchemaResource(decl);
      }
      return unmodifiableTypeDeclarations;
   }

   public TypeDeclaration getTypeDeclaration(String id)
   {
      TypeDeclaration decl = indexedTypeDecls.get(composeDefaultId(id));
      updateSchemaResource(decl);
      return decl;
   }

   private void updateSchemaResource(TypeDeclaration decl)
   {
      XpdlType type = decl.getXpdlType();
      if (type instanceof SchemaTypeDetails && !((SchemaTypeDetails) type).isResolved())
      {
         ((SchemaTypeDetails) type).setResource((Internal) schemaLocatorAdapter.createResource());
      }
   }

   public TypeDeclaration getTypeDeclaration(DocumentType documentType)
   {
      TypeDeclaration typeDeclaration = null;

      String typeDeclarationId = getTypeDeclarationId(documentType);

      if (null != typeDeclarationId)
      {
         typeDeclaration = getTypeDeclaration(typeDeclarationId);
      }
      return typeDeclaration;
   }

   private static String getTypeDeclarationId(DocumentType documentType)
   {
      if (documentType != null)
      {
         String documentTypeId = documentType.getDocumentTypeId();
         if (documentTypeId != null)
         {
            return QName.valueOf(documentTypeId).getLocalPart();
         }
      }
      return null;
   }

   public void setSchemaLocatorAdapter(SchemaLocatorAdapter schemaLocatorAdapter)
   {
      this.schemaLocatorAdapter = schemaLocatorAdapter == null ? new SchemaLocatorAdapter(this) : schemaLocatorAdapter;
   }

   public static class SchemaLocatorAdapter implements Adapter, XSDSchemaLocator
   {
      private Notifier target;
      private Model model;
      private ResourceSetImpl resourceSet;

      public SchemaLocatorAdapter(Model model)
      {
         this.model = model;
         resourceSet = new ResourceSetImpl();
      }

      public Resource createResource()
      {
         Resource resource = new XSDResourceImpl(URI.createURI(XMLConstants.NS_CARNOT_WORKFLOWMODEL_31));
         resource.eAdapters().add(this);
         resourceSet.getResources().add(resource);
         return resource;
      }

      public Notifier getTarget()
      {
         return target;
      }

      public boolean isAdapterForType(Object type)
      {
         return type == XSDSchemaLocator.class;
      }

      public void notifyChanged(Notification notification)
      {
         // ignore
      }

      public void setTarget(Notifier newTarget)
      {
         target = newTarget;
      }

      public XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI,
            String rawSchemaLocationURI, String resolvedSchemaLocationURI)
      {
         if (rawSchemaLocationURI.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
         {
            String typeId = rawSchemaLocationURI.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
            QName qname = QName.valueOf(typeId);
            Model model = getModel(qname.getNamespaceURI());
            if (model != null)
            {
               typeId = qname.getLocalPart();
               for (TypeDeclaration declaration : model.getAllTypeDeclarations())
               {
                  if (typeId.equals(declaration.getId()))
                  {
                     XpdlType type = declaration.getXpdlType();
                     if (type instanceof SchemaTypeDetails)
                     {
                        return ((SchemaTypeDetails) type).getSchema();
                     }
                     if (type instanceof ExternalReferenceDetails)
                     {
                        return ((ExternalReferenceDetails) type).getSchema(model);
                     }
                     return null;
                  }
               }
            }
         }
         return null;
      }

      private Model getModel(String id)
      {
         if (id.isEmpty() || id.equals(model.getId()))
         {
            return model;
         }
         Long oid = model.getResolvedModelOid(id);
         return oid == null ? null : getModel(oid);
      }

      protected Model getModel(long oid)
      {
         return null;
      }
   }

   public Set<QualityAssuranceCode> getAllQualityAssuranceCodes()
   {
      return qualityAssuranceCodes;
   }

   @Override
   public Set<String> getExternalPackages()
   {
      return externalPackages == null ? Collections.<String>emptySet() : externalPackages.keySet();
   }

   @Override
   public Long getResolvedModelOid(String externalPackageId)
   {
      return externalPackages == null ? null : externalPackages.get(externalPackageId);
   }
}