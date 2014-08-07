package org.eclipse.stardust.engine.extensions.camel.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Util
{

   public static void createFile(String path, String fileName, String content)
         throws IOException
   {
      File dir = new File(path);
      String loc = dir.getCanonicalPath() + File.separator + fileName;
      FileWriter fstream = new FileWriter(loc, true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(content);
      out.close();
   }
}
