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
package org.eclipse.stardust.engine.api.runtime;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.PrefStoreAwareConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

/**
 * TODO: document for internal API
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ParsedDeploymentUnit implements Serializable
{
   private static final long serialVersionUID = 1L;

   private IModel model;
   private byte[] content;
   private int referencedModelOid;
   
   public ParsedDeploymentUnit(DeploymentElement deploymentElement, int referencedModelOid)
   {
      this(deploymentElement.getContent(), referencedModelOid);
   }

   private ParsedDeploymentUnit(byte[] content, int referencedModelOid)
   {
      this.content = content;
      this.referencedModelOid = referencedModelOid;
      if (ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
      {
         // convert to CWM format
         String encoding = Parameters.instance().getObject(
               PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         this.content = XpdlUtils.convertXpdl2Carnot(content, encoding);
      }
      model = new DefaultXMLReader(false,
            new PrefStoreAwareConfigurationVariablesProvider())
            .importFromXML(new ByteArrayInputStream(this.content));
   }

   public byte[] getContent()
   {
      return content;
   }

   public int getReferencedModelOid()
   {
      return referencedModelOid;
   }

   public void setReferencedModelOid(int referencedModelOid)
   {
      this.referencedModelOid = referencedModelOid;
   }

   public IModel getModel()
   {
      return model;
   }

   @Override
   public String toString()
   {
      return "ParsedDeploymentUnit [" + model + ", referenced model oid: " + referencedModelOid + ']';
   }
}
