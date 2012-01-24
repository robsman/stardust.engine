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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Wraps an xml CARNOT model as an upgradable item. The model is expected to
 * be in String form.
 *
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class ModelItem implements UpgradableItem
{
   public static final Logger trace = LogManager.getLogger(ModelItem.class);

   public static final String LEGACY_VERSION_ATT = "carnot_xml_version";
   public static final String VERSION_ATT = "carnotVersion";

   private String model;
   private Version version;
   private Element modelElement;
   private boolean changed = false;

   public ModelItem(String model)
   {
      this.model = model;
   }

   /**
    * Gets the version of the model directly from the xml string. If no model
    * version is found '1.0.0' is returned.
    */
   public Version getVersion()
   {
      if (version != null)
      {
         return version;
      }
      bootstrapDOM();
      String versionString = modelElement.getAttribute(VERSION_ATT);
      if (versionString == null || versionString.equals(""))
      {
         versionString = modelElement.getAttribute(LEGACY_VERSION_ATT);
      }
      if (versionString == null || versionString.equals(""))
      {
         version = new Version(1, 0, 0);
      }
      else
      {
         version = new Version(versionString);
      }
      return version;
   }

   /**
    * Provides the DOM for the document by getting the document element.
    * This method is reentrant.
    */
   private void bootstrapDOM()
   {
      if (modelElement != null)
      {
         return;
      }
      try
      {
         DocumentBuilder _domBuilder = XmlUtils.newDomBuilder(true);
         InputSource inputSource = new InputSource(new StringReader(model));
         // TODO ship 3.0 DTD
         final URL dtd = ModelItem.class.getResource("WorkflowModel.dtd");
         inputSource.setSystemId(dtd.toString());
         _domBuilder.setEntityResolver(new EntityResolver()
         {
            public InputSource resolveEntity(String publicId, String systemId)
                  throws SAXException, IOException
            {
               if ("http://www.carnot.ag/workflowmodel/3.0/WorkflowModel.dtd".equals(systemId))
               {
                  return new InputSource(dtd.openStream());
               }
               return null;
            }
         });
         Document document = _domBuilder.parse(inputSource);
         modelElement = document.getDocumentElement();
      }
      catch (SAXException e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
      catch (IOException e)
      {
         trace.warn("", e);
         throw new UpgradeException(e.getMessage());
      }
   }

   /**
    * Sets a new version for the model.
    */
   public void setVersion(Version version)
   {
      bootstrapDOM();
      if (version.compareTo(new Version(3, 0, 0)) < 0)
      {
         modelElement.setAttribute(LEGACY_VERSION_ATT, version.toString());
      }
      else
      {
         modelElement.setAttribute(VERSION_ATT, version.toString());
      }
      this.version = version;
      changed = true;
   }

   public String getDescription()
   {
      return "Model";
   }

   /**
    * Retrieves the model with the changes done (esp. the new version number)
    */
   public String getModel()
   {
      if (!changed)
      {
         return model;
      }

      /*    weird!?

            CharArrayWriter writer = new CharArrayWriter();
            OutputFormat _outputFormat = new OutputFormat();

            _outputFormat.setIndent(3);
            _outputFormat.setEncoding("ISO-8859-1");
            XmlUtils serializer = new XmlUtils(writer, _outputFormat);
      */
      changed = false;
      return model;
   }

   public Element getModelElement()
   {
      bootstrapDOM();
      return modelElement;
   }
}
