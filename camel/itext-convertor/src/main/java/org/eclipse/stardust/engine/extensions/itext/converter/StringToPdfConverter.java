package org.eclipse.stardust.engine.extensions.itext.converter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.stardust.engine.extensions.itext.imageprovider.Base64ImageProvider;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;

public class StringToPdfConverter
{
   private final static String HTML = "html";

   private final static String TXT = "text";

   public static byte[] convertToPdf(String format, byte[] content)
         throws DocumentException, IOException, InvalidFormatException
   {
      if ((format == null || format.equalsIgnoreCase(""))
            && ((!format.equalsIgnoreCase(HTML)) && (!format.equalsIgnoreCase(TXT))))
         throw new InvalidFormatException(
               "The provided format is invalid only html, txt are supported.");
      if (format.equalsIgnoreCase(HTML))
         return convertHtmlToPdf(content);

      return convertTextToPdf(new String(content));
   }

   private static byte[] convertTextToPdf(String in) throws IOException, DocumentException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document document = new Document();
      PdfWriter.getInstance(document, out);
      document.open();
      document.add(new Paragraph(in));
      document.close();
      out.close();
      return out.toByteArray();
   }

   private static byte[] convertHtmlToPdf(byte[] in) throws DocumentException, IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document document = new Document();
      StyleSheet st = new StyleSheet();
      PdfWriter writer = PdfWriter.getInstance(document, out);
      document.setMarginMirroring(true);
      document.setMarginMirroringTopBottom(true);
      document.open();
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("img_provider", new Base64ImageProvider());
      BufferedReader reader = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(in)));
      ArrayList< ? > p = HTMLWorker.parseToList(reader, st, map);
      for (int k = 0; k < p.size(); ++k)
      {
         document.add((Element) p.get(k));
      }
      document.close();
      writer.close();
      return out.toByteArray();
   }
}
