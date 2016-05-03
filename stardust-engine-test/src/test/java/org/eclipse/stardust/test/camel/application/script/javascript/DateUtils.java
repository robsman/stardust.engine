package org.eclipse.stardust.test.camel.application.script.javascript;

import java.util.Date;

public class DateUtils
{
   public static void echo(final Date date)
   {
      System.out.println("received Date" + date);
   }
}
