package org.eclipse.stardust.engine.extensions.jms.app.spi;

import java.util.List;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;

@SPI(status = Status.Experimental, useRestriction = UseRestriction.Public)
public interface MultiMatchCapable
{

   /**
    * Indicates if the engine should try to find more matches for a given JMS message. By
    * default, exactly most one match is expected.
    *
    * @param matches
    * @return
    */
   boolean findMoreMatches(List<Match> matches);
}
