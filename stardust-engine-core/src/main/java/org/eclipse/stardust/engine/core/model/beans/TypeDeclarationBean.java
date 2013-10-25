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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IExternalPackage;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 * @author mgille
 * @version $Revision$
 */
public class TypeDeclarationBean extends IdentifiableElementBean implements ITypeDeclaration
{

   private final IXpdlType xpdlType;

   TypeDeclarationBean()
   {
      this.xpdlType = null;
   }

   public TypeDeclarationBean(String id, String name, String description, Map attributes,
         IXpdlType xpdlType)
   {
      super(id, name);

      setDescription(description);
      setAllAttributes(attributes);
      if (attributes != null && !attributes.containsKey(PredefinedConstants.BROWSABLE_ATT))
      {
         setAttribute(PredefinedConstants.BROWSABLE_ATT, Boolean.TRUE);
      }

      this.xpdlType = xpdlType;
   }

   public IXpdlType getXpdlType()
   {
      return xpdlType;
   }

   public String toString()
   {
      return "Type declaration: " + getName();
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the type declaration.
    */
   public void checkConsistency(List inconsistencies)
   {
      checkId(inconsistencies);



      // check for unique Id
      ITypeDeclaration td = ((IModel) getModel()).findTypeDeclaration(getId());
      if (td != null && td != this)
      {
         BpmValidationError error = BpmValidationError.SDT_DUPLICATE_ID_FOR_TYPE_DECLARATION.raise(getName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
      }

      validateElements(inconsistencies, td);
      validateParentReferences((IModel) getModel(), inconsistencies, td);

      // check for usage of variables
      if (xpdlType instanceof SchemaTypeBean)
      {
         SchemaTypeBean schemaType = (SchemaTypeBean) xpdlType;
         XSDSchema xsdSchema = schemaType.getSchema();
         Element element = xsdSchema.getElement();
         checkForVariables(inconsistencies, element);
      }
   }

   private void checkForVariables(List inconsistencies, Node node)
   {
      if (node instanceof Element)
      {
         Element element = (Element) node;

         String localName = element.getLocalName();
         if (StringUtils.isNotEmpty(localName))
         {
            String attribute = null;
            if ("enumeration".equals(localName))
            {
               attribute = element.getAttribute("value");
            }
            else if ("element".equals(localName))
            {
               attribute = element.getAttribute("name");
            }

            if (attribute != null)
            {
               Matcher variablesMatcher = ConfigurationVariableUtils
                     .getConfigurationVariablesMatcher(attribute);
               if (variablesMatcher.find())
               {
                  String group = variablesMatcher.group(0);
                  BpmValidationError error = BpmValidationError.SDT_TYPE_DECLARATION_NOT_ALLOWED_TO_CONTAIN_VARIABLES.raise(group);
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               }
            }
         }

         Node nextChild = element.getFirstChild();
         while (nextChild != null)
         {
            checkForVariables(inconsistencies, nextChild);
            nextChild = nextChild.getNextSibling();
         }
      }
   }

   private IModel getRefModel(IModel model, QName qname)
   {
      if (XMLConstants.NULL_NS_URI != qname.getNamespaceURI())
      {
         List<IExternalPackage> packs = model.getExternalPackages();
         if (packs != null)
         {
            for (Iterator<IExternalPackage> i = packs.iterator(); i.hasNext();)
            {
               IExternalPackage refPack = i.next();
               String modelHRef = refPack.getHref();
               if (modelHRef != null)
               {
                  if (modelHRef.equals(refPack.getHref()))
                  {
                     return refPack.getReferencedModel();
                  }
               }
            }
         }
      }
      return model;
   }

   private void validateElements(List inconsistencies, ITypeDeclaration declaration)
   {
      inconsistencies.addAll(ElementValidator.validateElements(declaration));
   }

   private void validateParentReferences(IModel model, List inconsistencies,
         ITypeDeclaration declaration)
   {
      if (xpdlType instanceof SchemaTypeBean)
      {
         SchemaTypeBean schemaType = (SchemaTypeBean) xpdlType;
         XSDSchema xsdSchema = schemaType.getSchema();
         for (Iterator<XSDTypeDefinition> i = xsdSchema.getTypeDefinitions().iterator(); i.hasNext();)
         {
            XSDTypeDefinition typeDefinition = i.next();
            if (typeDefinition instanceof XSDComplexTypeDefinition)
            {
               XSDComplexTypeDefinition complexType = (XSDComplexTypeDefinition) typeDefinition;
               XSDTypeDefinition baseType = complexType.getBaseType();
               if (baseType != null)
               {
                  String baseTypeNameSpace = baseType.getTargetNamespace();
                  XSDImport baseTypeImport = TypeDeclarationUtils.getImportByNamespace(
                        schemaType.getSchema(), baseTypeNameSpace);
                  if (baseTypeImport != null)
                  {
                     String location = ((XSDImport) baseTypeImport).getSchemaLocation();
                     String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
                     QName qname = QName.valueOf(typeId);
                     model = getRefModel(model, qname);
                     ModelElementList<TypeDeclarationBean> referedDeclarations = model.getTypeDeclarations();
                     if (getTypeDeclaration(referedDeclarations, qname.getLocalPart()) == null)
                     {
                        BpmValidationError error = BpmValidationError.SDT_REFERENCED_PARENT_TYPE_NOT_FOUND.raise(
                              declaration.getId(), typeId);
                        inconsistencies.add(new Inconsistency(error, declaration,
                              Inconsistency.ERROR));
                     }
                  }

               }
            }
         }
      }
   }

   private TypeDeclarationBean getTypeDeclaration(
         ModelElementList<TypeDeclarationBean> referedDeclarations, String localPart)
   {
      for (Iterator<TypeDeclarationBean> i = referedDeclarations.iterator(); i.hasNext();)
      {
         TypeDeclarationBean typeDeclaration = i.next();
         if (typeDeclaration.getId().equals(localPart))
         {
            return typeDeclaration;
         }
      }
      return null;
   }

}
