/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.test.staticmethods;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.core.model.beans.QNameUtil;
import org.junit.Test;

/**
 * <p>
 * Tests QNameUtil methods.
 * </p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class QNameUtilTest
{
   /**
    * <p>
    * Tests if Model Id will be extracted.
    * </p>
    */
   @Test
   public void testParseModelId1()
   {
      String documentTypeId = "{http://www.infinity.com/bpm/model/Liberty/AccountInfo}DocumentMetaData";
      
      QName qName = QName.valueOf(documentTypeId);            
      String namespaceURI = qName.getNamespaceURI();
      String modelId = QNameUtil.parseModelId(namespaceURI);
            
      assertEquals("Liberty", modelId);
   }
   
   /**
    * <p>
    * Tests if Model Id will be extracted.
    * </p>
    */
   @Test
   public void testParseModelId2()
   {
      String documentTypeId = "{+http://www.infinity.com/bpm/model/Liberty/AccountInfo}DocumentMetaData";
      
      QName qName = QName.valueOf(documentTypeId);            
      String namespaceURI = qName.getNamespaceURI();
      String modelId = QNameUtil.parseModelId(namespaceURI);
            
      assertEquals(null, modelId);
   }   
}