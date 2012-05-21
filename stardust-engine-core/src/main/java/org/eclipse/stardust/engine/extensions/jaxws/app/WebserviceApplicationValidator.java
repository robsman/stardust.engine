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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;


/**
 * @author rsauer
 * @version $Revision$
 */
public class WebserviceApplicationValidator implements ApplicationValidator,
      ApplicationValidatorEx
{
   /**
    * This method checks the type mapping
    *  
    * @param inconsistencies List of Inconsisteny-entries to be filled by method
    * @param attribute Single attribute containing the tzpe mapping
    */
   private void checkTypeMapping(IApplication app, List inconsistencies, String key, String clazz)
   {
      String xmlType = key.substring(PredefinedConstants.WS_MAPPING_ATTR_PREFIX.length());
      if (StringUtils.isEmpty(clazz))
      {
         inconsistencies.add(new Inconsistency(app.getId() + ": No type mapping defined for xml type '" + xmlType + "'",
               app, Inconsistency.WARNING));
      }
      else
      {
         try
         {
            Reflect.getClassFromClassName(clazz);
         }
         catch (InternalException ex)
         {
            inconsistencies.add(new Inconsistency(app.getId() + ": Xml type '" + xmlType
                  + "' has an invalid type mapping: '" + clazz + "'",
                  app, Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            inconsistencies.add(new Inconsistency(app.getId() + ": Xml type '" + xmlType
                  + "' has an invalid type mapping: '" + clazz + "'",
                  app, Inconsistency.WARNING));
         }
      }
   }

   /**
    * This method checks the format of the XML template
    *  
    * @param inconsistencies List of Inconsisteny-entries to be filled by method
    * @param attribute Single attribute containing the XML template
    */
   private void checkXmlTemplate(IApplication app, List inconsistencies, String key, String xml)
   {
      String name = key.substring(PredefinedConstants.WS_TEMPLATE_ATTR_PREFIX.length());
      if (!StringUtils.isEmpty(xml))
      {
         try
         {
            XmlUtils.parseString(xml);
         }
         catch (Exception ex)
         {
            inconsistencies.add(new Inconsistency(app.getId() + ": Template for part '"
                  + name + "' is invalid" + ex.getMessage(),
                  app, Inconsistency.WARNING));
         }
      }
   }

   /**
    * This method checks the format of the URL
    *  
    * @param inconsistencies List of Inconsisteny-entries to be filled by method
    * @param attribute Single attribute containing the WSDL URL
    */
   private void checkWsdlUrl(IApplication app, ArrayList inconsistencies, String uri)
   {
      if ( !StringUtils.isEmpty(uri))
      {
         try
         {
            new URL(XmlUtils.resolveResourceUri(uri));
         }
         catch (Exception ex)
         {
            inconsistencies.add(new Inconsistency(app.getId() + ": WSDL URL '" + uri
                  + "' is invalid: " + ex.getMessage(),
                  app, Inconsistency.WARNING));
         }
      }
   }

   /**
    * This method checks the existence of a given property
    * 
    * @param inconsistencies List of Inconsisteny-entries to be filled by method
    * @param attributes All attributes of the application
    * @param name Name of the attribute being looked up 
    */
   private void checkProperty(IApplication app, ArrayList inconsistencies, String name)
   {
      Object property = app.getAttribute(name);
      if (property == null || property.toString().trim().length() == 0)
      {
         inconsistencies.add(new Inconsistency(app.getId() + ": Property '" + name + "' not set.",
               app, Inconsistency.ERROR));
      }
   }
   
   /**
    * This method implements consistency checks for a Web Service application.
    * Similar checks are done in ApplicationPropertiesDialog.validateSettings() and 
    * WebserviceApplicationPanel.validatePanel() 
    */
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   public List validate(IApplication application)
   {
      ArrayList inconsistencies = new ArrayList();
      
      checkProperty(application, inconsistencies, PredefinedConstants.WS_WSDL_URL_ATT);
      checkProperty(application, inconsistencies, PredefinedConstants.WS_SERVICE_NAME_ATT);
      checkProperty(application, inconsistencies, PredefinedConstants.WS_PORT_NAME_ATT);
      checkProperty(application, inconsistencies, PredefinedConstants.WS_OPERATION_NAME_ATT);
      
      Map<String, Object> attributes = application.getAllAttributes();
      Set<Map.Entry<String, Object>> entries = attributes.entrySet();
      for (Map.Entry<String, Object> entry : entries)
      {
         String key = entry.getKey();
         if (entry.getValue() instanceof String)
         {
            String value = (String) entry.getValue();
            if (key.startsWith(PredefinedConstants.WS_MAPPING_ATTR_PREFIX))
            {
               checkTypeMapping(application, inconsistencies, key, value);
            }
            else if (key.startsWith(PredefinedConstants.WS_TEMPLATE_ATTR_PREFIX))
            {
               checkXmlTemplate(application, inconsistencies, key, value);
            }
            else if (key.equals(PredefinedConstants.WS_WSDL_URL_ATT))
            {
               checkWsdlUrl(application, inconsistencies, value);
            }
         }
      }
      
      return inconsistencies;
   }
}
