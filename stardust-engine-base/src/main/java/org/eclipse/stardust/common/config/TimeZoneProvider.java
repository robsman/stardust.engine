package org.eclipse.stardust.common.config;

import java.util.TimeZone;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

@SPI(status = Status.Internal, useRestriction = UseRestriction.Internal)
public interface TimeZoneProvider
{
   TimeZone getTimeZone();
}
