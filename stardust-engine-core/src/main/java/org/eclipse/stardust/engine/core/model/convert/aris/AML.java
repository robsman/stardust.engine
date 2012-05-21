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
package org.eclipse.stardust.engine.core.model.convert.aris;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLWriter;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.builder.ModelBuilder;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.model.convert.Converter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author fherinean
 * @version $Revision$
 */
public class AML
{
   public static final String TAG_NAME = "AML";
   public static final String INFO_TAG = "Header-Info";
   public static final String CREATE_DATE_ATT = "CreateDate";
   public static final String CREATE_TIME_ATT = "CreateTime";
   public static final String DATABASE_NAME_ATT = "DatabaseName";
   public static final String VERSION_ATT = "ArisExeVersion";

   private static final DateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

   private Date creationDate;
   private String name;
   private String version;
   private Group root;
   private HashMap global;
   private ArrayList errors;
   private int mode;

   /**
    * Creates an AML import structure from an XML Document. This structure is able to
    * generate the corresponding Carnot model.
    *
    * The following items are processed in the modelling environment:
    * - Header-Info (1): contains optional information like the model (database) name,
    *       creation timestamp and aris version.
    * - Group (1): the root group containing the actual model definition. The groups will
    *       not be actually used during conversion, the converter is using a flat view of
    *       the exported model.
    * - Language (0..n): specifies available languages for the exported model.
    *       Not yet implemented. If more than 1 language present, should popup a message
    *       box asking which language will be imported if no language specified.
    * - FFTextDef (0..n): freeform text definition.
    *       Not yet implemented. Possible usage as annotations.
    * - User (0..n): Modelers. Must check the passwords.
    *       Not yet implemented.
    *
    * The following items are ignored during import because there is no Carnot equivalent.
    * - UserGroup (0..n): user groups.
    * - Prefix (0..n): database prefixes.
    * - Database (0..1): global attributes. As to my knowledge, specifies
    *       printing/formatting attributes not used by Carnot tools.
    * - OLEDef (0..n): OLE documents in the database.
    * - FontStyleSheet (0..n): definition of fonts used.
    * - Delete (0..n): elements to be deleted from the model.
    *
    * @param document the XML document to process
    */
   public AML(Document document)
   {
      global = new HashMap();
      parseDocument(document);
      resolveReferences();
   }

   private void resolveReferences()
   {
      for (Iterator i = global.values().iterator(); i.hasNext();)
      {
         ((ArisElement) i.next()).resolveReferences();
      }
   }

   private void parseDocument(Document document)
   {
      boolean infoCollected = false;
      Element aml = document.getDocumentElement();
      if (TAG_NAME.equals(aml.getTagName()))
      {
         NodeList nodes = aml.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Node child = nodes.item(i);
            if (child instanceof Element)
            {
               Element element = (Element) child;
               if (INFO_TAG.equals(element.getTagName()))
               {
                  if (infoCollected)
                  {
                     addError(new ConvertWarningException("Multiple Header Info present."));
                  }
                  else
                  {
                     getInfo(element);
                     infoCollected = true;
                  }
               }
               // todo Language
               // ignore Prefix
               // ignore Database
               // todo User
               // ignore UserGroup
               // ignore OLEDef
               // ignore Delete
               // ignore FontStyleSheet
               // todo FFTextDef
               else if (Group.TAG_NAME.equals(element.getTagName()))
               {
                  if (root != null)
                  {
                     addError(new ConvertWarningException(
                           "Multiple root groups present. Skipping group with ID: "
                           + element.getAttribute(Group.GROUP_ID_ATT)));
                  }
                  else
                  {
                     root = new Group(this, element);
                  }
               }
            }
         }
      }
      else
      {
         addError(new ConvertWarningException("Unsupported document type: "
               + aml.getTagName()));
      }
   }

   public void addError(ConvertWarningException e)
   {
      if (errors == null)
      {
         errors = new ArrayList();
      }
      errors.add(e);
   }

   private void getInfo(Element element)
   {
      String createDate = element.getAttribute(CREATE_DATE_ATT);
      String createTime = element.getAttribute(CREATE_TIME_ATT);
      try
      {
         creationDate = fmt.parse(createDate + " " + createTime);
      }
      catch (ParseException e)
      {
         System.out.println(e.getMessage());
         creationDate = new Date();
      }
      name = element.getAttribute(DATABASE_NAME_ATT);
      // ignore UserName
      version = element.getAttribute(VERSION_ATT);
   }

   public String toString()
   {
      return "Aris" + version + ": " + name + " (" + creationDate + ")\r\n" + root.indent("");
   }

   public IModel createModel(int mode)
   {
      this.mode = mode;
      ModelBuilder builder = DefaultModelBuilder.create();
      IModel model = builder.createModel("aris", name, null);
      // generate processes and diagrams
      for (Iterator i = global.values().iterator(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof Model)
         {
            ((Model) element).create(model);
         }
      }
      return model;
   }

   public List getErrors()
   {
      return errors;
   }

   public ArisElement getElement(String id)
   {
      return (ArisElement) global.get(id);
   }

   public void addGlobal(String id, ArisElement element)
   {
      if (global.containsKey(id))
      {
         addError(new ConvertWarningException("Duplicate id: " + id + " for element: "
               + element.getName()));
      }
      else
      {
         global.put(id, element);
      }
   }

   public static void main(String[] args)
   {
      if (args.length == 0)
      {
         System.out.println("java org.eclipse.stardust.engine.core.model.convert.aris.AML <filename>");
         return;
      }

      InputStream is = null;
      Document document = null;

      String fileName = "dat/XMLExport.XML";//args[0];
      try
      {
         FileInputStream stream = new FileInputStream(fileName);
         is = new BufferedInputStream(stream);
         DocumentBuilder domBuilder = org.eclipse.stardust.engine.core.runtime.utils.XmlUtils.newDomBuilder();
         InputSource source = new InputSource(is);
         document = domBuilder.parse(source);
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
         return;
      }
      catch (SAXException e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      AML aml = new AML(document);
//      System.out.println(aml);
      List errors = aml.getErrors();
      if (errors != null)
      {
         for (int i = 0; i < errors.size(); i++)
         {
            ConvertWarningException ex = (ConvertWarningException) errors.get(i);
            System.err.println(ex.getMessage());
         }
      }
      IModel model = aml.createModel(Converter.PROTOTYPE_MODE);
      new DefaultXMLWriter(true).exportAsXML(model, new File("dat/ARIS.XML"));

   }

   public String getVersion()
   {
      return version;
   }

   public boolean isPrototype()
   {
      return mode == Converter.PROTOTYPE_MODE;
   }
}
