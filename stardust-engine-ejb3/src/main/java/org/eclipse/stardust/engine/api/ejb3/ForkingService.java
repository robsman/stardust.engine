package org.eclipse.stardust.engine.api.ejb3;

import javax.ejb.Local;

import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;

@Local
public interface ForkingService extends ExecutorService
{
}
