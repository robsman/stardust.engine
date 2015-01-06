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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * The <code>ResourceInfo</code> interface keeps information common to
 * both existing and not yet existing JCR resources (document or folder).
 * 
 * @author sauer
 * @version $Revision$
 */
public interface ResourceInfo extends Serializable
{

   /**
    * Get the name of the JCR resource. This property is mandatory and
    * must be always set.
    * 
    * @return the name of the JCR resource.
    */
   String getName();

   /**
    * Sets the name of the JCR resource. This property is mandatory and
    * must be always set.
    * 
    * @param name the name of the JCR resource.
    */
   void setName(String name);

   /**
    * Gets the description of the JCR resource.
    * 
    * @return the description of the JCR resource.
    */
   String getDescription();

   /**
    * Sets the description of the JCR resource.
    * 
    * @param description the description of the JCR resource.
    */
   void setDescription(String description);

   /**
    * Gets the owner of the JCR resource.
    * 
    * @return the owner of the JCR resource.
    */
   String getOwner();

   /**
    * Sets the owner of the JCR resource.
    * 
    * @param owner the owner of the JCR resource.
    */
   void setOwner(String owner);

   /**
    * Gets the date of creation of the JCR resource.
    * 
    * @return the date of creation of the JCR resource.
    */
   Date getDateCreated();

   /**
    * Gets the date of last modification of the JCR resource.
    * 
    * @return the date of last modification of the JCR resource.
    */
   Date getDateLastModified();

   /**
    * Gets all custom properties of the JCR resource. The properties are represented 
    * by a <code>java.util.Map</code> where the property name (<code>String</code>) 
    * is the key.  
    * 
    * @return the custom properties of the JCR resource.
    */
   Map /*<String,Serializable>*/ getProperties();

   /**
    * Sets all custom properties of the JCR resource. The properties should be organized 
    * in a <code>java.util.Map</code> where the property name (<code>String</code>) 
    * is the key.  
    * 
    * @param properties the custom properties of the JCR resource.
    */
   void setProperties(Map properties);

   /**
    * Gets a single custom property of the JCR resource.
    * 
    * @param name the property name.
    * @return the property value.
    */
   Serializable getProperty(String name);

   /**
    * Sets a single custom property of the JCR resource.
    * 
    * @param name the property name.
    * @param value the property value.
    */
   void setProperty(String name, Serializable value);

}
