/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class Util
{
   public static void createFile(String path, String fileName, String content)
         throws IOException
   {
      File dir = new File(path);
      if(!dir.exists())
         dir.mkdirs();
      String loc = dir.getCanonicalPath() + File.separator + fileName;
      FileWriter fstream = new FileWriter(loc, true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(content);
      out.close();
   }
}
