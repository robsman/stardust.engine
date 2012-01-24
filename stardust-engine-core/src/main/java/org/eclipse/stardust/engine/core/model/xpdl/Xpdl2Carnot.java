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
package org.eclipse.stardust.engine.core.model.xpdl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.xml.sax.InputSource;


/**
 * @author rsauer
 * @version $Revision$
 */
public class Xpdl2Carnot
{
   public static void main(String[] args)
   {
      List<String> srcFile = CollectionUtils.newList();
      List<String> tgtFile = CollectionUtils.newList();
      
      for (int i = 0; i < args.length; i++ )
      {
         if ("-xpdl".equals(args[i]))
         {
            srcFile.add(args[++i]);
         }
         else if ("-carnot".equals(args[i]))
         {
            tgtFile.add(args[++i]);
         }
      }
      
/*      if (srcFile.isEmpty())
      {
         //srcFile.add("C:\\development\\trunk\\runtime-New_configuration\\webprj\\models\\Crnt15765.xpdl");
         srcFile.add("C:\\development\\branches\\b_dev_5_2_surge_poc\\runtime-New_configuration\\test\\models\\NewWorkflowModel3.xpdl");
      }
      
      if (tgtFile.isEmpty())
      {
         //tgtFile.add("C:\\development\\branches\\b_dev_5_2_surge_poc\\runtime-New_configuration\\test\\models\\TestModel1.cwm");
         tgtFile.add("C:\\development\\branches\\b_dev_5_2_surge_poc\\runtime-New_configuration\\test\\models\\TestModel2.cwm");
      }*/
      
      for (int i = 0; i < srcFile.size(); i++)
      {
         String src = srcFile.get(i);
         String tgt = i < tgtFile.size() ? tgtFile.get(i) : null;
         if (!StringUtils.isEmpty(src) && !StringUtils.isEmpty(tgt))
         {
            try
            {
               Source source = new SAXSource(new InputSource(new FileInputStream(src)));
               Result result = new StreamResult(new FileOutputStream(tgt));
               XpdlUtils.convertXpdl2Carnot(source, result);
            }
            catch (FileNotFoundException ex)
            {
               ex.printStackTrace();
            }
         }
      }
   }
}
