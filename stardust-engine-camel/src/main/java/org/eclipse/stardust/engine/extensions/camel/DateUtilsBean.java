package org.eclipse.stardust.engine.extensions.camel;

import java.text.ParseException;
import java.util.Date;

import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

public class DateUtilsBean
{
   private String dateString = "17.04.1833";
   
   public String getDateString() {
      return dateString;
   }
   
   public Date getDate() throws ParseException {
      return KeyValueList.getDateFormat().parse(dateString);
   }
}
