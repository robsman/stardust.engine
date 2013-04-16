/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.struct.emfxsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;

public abstract class CustomURIConverter implements URIConverter
{
   private Map customMap;

   public Map getCustomMap()
   {
      return customMap;
   }

   public void setCustomMap(Map customMap)
   {
      this.customMap = customMap;
   }

   @Override
   public Map getURIMap()
   {
      return URIConverter.URI_MAP;
   }

   @Override
   public URI normalize(URI uri)
   {
      // no normalization implemented
      return uri;
   }

   @Override
   public Map<String, ? > contentDescription(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      return null;
   }

   @Override
   public InputStream createInputStream(URI uri) throws IOException
   {
      return createInputStream(uri, null);
   }
   
   @Override
   public OutputStream createOutputStream(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public OutputStream createOutputStream(URI uri) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public void delete(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public boolean exists(URI arg0, Map< ? , ? > arg1)
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public Map<String, ? > getAttributes(URI arg0, Map< ? , ? > arg1)
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public EList<ContentHandler> getContentHandlers()
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public URIHandler getURIHandler(URI arg0)
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public EList<URIHandler> getURIHandlers()
   {
      throw new RuntimeException("Not supported.");
   }

   @Override
   public void setAttributes(URI arg0, Map<String, ? > arg1, Map< ? , ? > arg2) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }
}
