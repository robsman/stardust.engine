package org.eclipse.stardust.engine.core.spi.logging;

import java.util.Arrays;
import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LogType;

public class DefaultAuditTrailLoggingFilter implements IAuditTrailLoggingFilter
{

   public static final String PROCESS_INSTANCE_LOGGING_FILTER = "AuditTrail.Logging.ProcessInstanceLogCodes";

   private String[] FILTERABLE_LOGCODES = new String[] {
         LogCode.DATA.getName(), LogCode.APPLICATION.getName()};

   @Override
   public boolean filterLogEntry(LogCode code, LogType severity, Object context,
         long user, Object subject)
   {
      List<String> notfilteredEvents = Parameters.instance().getStrings(
            PROCESS_INSTANCE_LOGGING_FILTER, ",");
           
      if (Arrays.asList(FILTERABLE_LOGCODES).contains(code.getName()))
      {
         if (notfilteredEvents.size() > 0)
         {
            if (notfilteredEvents.contains(code.getName()))
            {
               return false;
            }
         }
         return true;
      }
      return false;
   }

}
