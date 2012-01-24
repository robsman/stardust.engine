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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.xsd.XSDSchema;
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
         inconsistencies.add(new Inconsistency("Duplicate ID for type declaration '" +
               getName() + "'.", this, Inconsistency.ERROR));
      }
      
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
                  inconsistencies.add(new Inconsistency(
                        "Type declaration is not allowed to contain variables: " + group,
                        this, Inconsistency.ERROR));
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
}
