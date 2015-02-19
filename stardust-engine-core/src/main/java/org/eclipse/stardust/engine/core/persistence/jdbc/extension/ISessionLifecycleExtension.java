package org.eclipse.stardust.engine.core.persistence.jdbc.extension;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;

@SPI(status = Status.Stable, useRestriction = UseRestriction.Internal)
public interface ISessionLifecycleExtension
{

   void beforeSave(Session session);

   void afterSave(Session session);
}
