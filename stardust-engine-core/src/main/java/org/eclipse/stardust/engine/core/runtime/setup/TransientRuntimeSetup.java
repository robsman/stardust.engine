/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;



public class TransientRuntimeSetup extends RuntimeSetup
{
   private final String xml;

   public TransientRuntimeSetup(String xml)
   {
      this.xml = xml;
      parse(xml);
   }

   public String getXml()
   {
      return xml;
   }
}
