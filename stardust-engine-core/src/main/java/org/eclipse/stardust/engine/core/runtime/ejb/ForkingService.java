package org.eclipse.stardust.engine.core.runtime.ejb;

import javax.ejb.Local;

@Local
public interface ForkingService extends ExecutorService
{
}
