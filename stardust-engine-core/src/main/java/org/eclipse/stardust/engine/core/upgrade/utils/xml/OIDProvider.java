/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.common.log.ClientLogManager;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
//import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * @author rsauer
 * @version $Revision$
 */
public class OIDProvider
{
   private static final Logger trace = LogManager.getLogger(OIDProvider.class);

   private static final String OID_ATT = "oid";

   long lastOID;

   //@Test
   public static void testOidProvider(String[] args)
   {
      if (args.length < 1)
      {
         System.out.println("Usage: OIDProvider source");
         System.exit(-1);
      }
      ClientLogManager.bootstrap("pfffh");
      DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true);

      InputSource inputSource = null;
      try
      {
         inputSource = new InputSource(new FileReader(args[0]));
         System.out.println("Source: " + args[0]);
         trace.info("Source: " + args[0]);
      }
      catch (FileNotFoundException e)
      {
         System.out.println("File not found: " + args[0]);
         System.exit(-1);
      }
      Document source = null;
      try
      {
         source = domBuilder.parse(inputSource);
      }
      catch (Exception e)
      {
         System.out.println("Exception during parsing, exiting: " + e.getMessage());
         trace.warn("", e);
         System.exit(-1);
      }
      long oid = new OIDProvider(source).getOID();
      System.out.println("First available OID is " + oid);
      trace.warn("First available OID is " + oid);
   }

   public OIDProvider(Document dom)
   {
      reserveUsedOIDs(dom.getDocumentElement());
   }

   public void reserveOID(long oid)
   {
      this.lastOID = Math.max(this.lastOID, oid);
   }

   public long getOID()
   {
      // @todo/belgium check overflow

      this.lastOID++;

      return this.lastOID;
   }

   /**
    * Recursively reserves any OID set via the "oid" attribute of the node and all of it's
    * child-nodes.
    *
    * @param node The node to perform reservation on.
    */
   private void reserveUsedOIDs(Node node)
   {
      NamedNodeMap attributes = node.getAttributes();
      if (null != attributes)
      {
         Node oid = attributes.getNamedItem(OID_ATT);
         if (null != oid)
         {
            reserveOID(Long.parseLong(oid.getNodeValue()));
         }
      }

      NodeList childs = node.getChildNodes();
      int nChilds = childs.getLength();
      for (int i = 0; i < nChilds; ++i)
      {
         reserveUsedOIDs(childs.item(i));
      }
   }
}
