package com.sungard.infinity.integration.itext.imageprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import com.lowagie.text.DocListener;
import com.lowagie.text.Image;
import com.lowagie.text.html.simpleparser.ChainedProperties;
import com.lowagie.text.html.simpleparser.ImageProvider;

public class Base64ImageProvider implements ImageProvider
{

   @Override
   public Image getImage(String src, HashMap h, ChainedProperties cprops, DocListener doc)
   {
      Image img = null;
      if (src.startsWith("data:image/"))
      {
         try
         {
            final String base64Data = src.substring(src.indexOf(",") + 1);
            byte[] contentBytes = Base64.decodeBase64(base64Data.getBytes());
            BufferedImage buf=ImageIO.read(new ByteArrayInputStream(contentBytes));
            img = Image.getInstance(contentBytes);
            img.scaleAbsolute(buf.getWidth(), buf.getHeight());
            return img;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      return null;
   }

}
