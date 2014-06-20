package org.eclipse.stardust.engine.core.spi.security;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;

/**
 * The {@link CredentialDeliveryStrategy} SPI provides an interface to implement
 * a custom strategy for delivering information that is required to reset a user
 * password to the user itself. </br> </br> To change the default
 * {@link MailbasedCredentialDeliveryStrategy} to a custom implementation
 * declared class in the file
 * <i>/META-INF/org.eclipse.stardust.engine.core.spi.security
 * .CredentialDeliveryStrategy</i> needs to be changed to the custom implementation
 * 
 * @author Thomas.Wolfram
 * 
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Public)
public interface CredentialDeliveryStrategy {

	/**
	 * 
	 * @param user - the {@link IUser} instance for which the password reset token has been generated for
	 * @param token - the generated reset token
	 */
	void deliverPasswordResetToken(IUser user, String token);

	/**
	 * 
	 * @param user - the {@link IUser} instance for which the password has been reseted
	 * @param password - the newly generated password for the user
	 */
	void deliverNewPassword(IUser user, String password);

}
