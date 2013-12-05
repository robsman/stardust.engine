package org.eclipse.stardust.engine.extensions.camel.test;

import java.util.Map;

public class TestBean2
{

   @SuppressWarnings("unchecked")
   public String complete(String person)
   {
      StringBuffer address = new StringBuffer();

      address.append("<Address>");
      address.append("<addrLine1>addrLine1</addrLine1>");
      address.append("<addrLine2>addrLine2</addrLine2>");
      address.append("<zipCode>zipCode</zipCode>");
      address.append("<city>city</city>");
      address.append("</Address>");
      
      return address.toString();
   }
}
