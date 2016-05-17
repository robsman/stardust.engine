package org.eclipse.stardust.engine.extensions.itext.converter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
/**
 * Retrieve the content of the exchange body as byte[] and convert it to PDF;
 * the exchange body is then overriden by the pdf content.
 * 
 *
 */
public class PdfConverterProcessor implements Processor
{

   @Override
   public void process(Exchange exchange) throws Exception
   {
      String format = "text";
      if(exchange.getIn().getHeader("CamelTemplatingFormat")!=null)
         format = (String) exchange.getIn().getHeader("CamelTemplatingFormat");
      byte[] pdfFileContent = StringToPdfConverter.convertToPdf(format, exchange.getIn()
            .getBody(byte[].class));
      exchange.getIn().setBody(pdfFileContent);
   }

}
