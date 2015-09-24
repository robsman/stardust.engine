package org.eclipse.stardust.engine.core.spi.logging;

import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LogType;

public interface IAuditTrailLoggingFilter
{

   boolean filterLogEntry(LogCode code, LogType severity, Object context, long user, Object subject);
   
}
