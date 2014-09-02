package org.eclipse.stardust.engine.core.runtime.interceptor;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

@SPI(status = Status.Stable, useRestriction = UseRestriction.Internal)
public interface TransactionPolicyAdvisor
{
   boolean mustRollback(MethodInvocation invocation, Throwable Exception);
}
