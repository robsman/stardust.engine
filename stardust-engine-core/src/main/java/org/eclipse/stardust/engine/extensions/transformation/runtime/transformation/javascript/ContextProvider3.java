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
package org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.javascript;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.eclipse.stardust.engine.extensions.transformation.javascript.JScriptManager3;
import org.eclipse.stardust.engine.extensions.transformation.model.MappingModelUtil;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty;



/**
 * This class is a workaround for a missing protocol for applications.
 * 
 * It initializes a shared context and a shared scope once per message transformation
 * application type and per JVM. Hereby, all type declarations, global function
 * definitions are already executed against the context and the mapping expressions and
 * statements are compiled for fast execution on application invocation later.
 * 
 * @author Marc Gille
 * 
 */
public class ContextProvider3
{
   private JScriptManager3 jsManager;

   private List externalClasses = CollectionUtils.newList();

   private List fieldMappings;

   /**
     * 
     */
   public ContextProvider3()
   {
   }

   /**
    * 
    * @param application
    * @param pathList
    * @param mappingExpressionList
    * @param mappingStatementsList
    * @param jsScriptMap
    * @return
    */
   public synchronized JScriptManager3 getOrCreateSharedContext(Application application)
   {
      inititializeModel(application);
      return jsManager;
   }

   private void inititializeModel(Application application)
   {
      this.jsManager = new JScriptManager3();

      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel model = modelManager.findModel(application.getModelOID());

      for (int i=0; i<application.getAllAccessPoints().size(); i++)
      {
         AccessPoint accessPoint = (AccessPoint)application.getAllAccessPoints().get(i);
       
         if (accessPoint.getDirection().equals(Direction.IN))
         {
            jsManager.registerInAccessPointType(model, accessPoint);
         }
         else
         {
            jsManager.registerOutAccessPointType(model, accessPoint);
         }
      }
      

      String xmlString = (String) application.getAttribute(Constants.TRANSFORMATION_PROPERTY);
      if (xmlString != null) {
         TransformationProperty trafoProp = (TransformationProperty) MappingModelUtil.transformXML2Ecore(xmlString);
         this.fieldMappings = trafoProp.getFieldMappings();
         this.externalClasses = trafoProp.getExternalClasses();
      }      
      else
      {
         throw new RuntimeException("Message transformation application is not configured properly");
      }
   }


   public List /*<FieldMapping>*/ getFieldMappings()
   {
      return this.fieldMappings;
   }
   
   public List getExternalClasses()
   {
      return this.externalClasses;
   }   

}
