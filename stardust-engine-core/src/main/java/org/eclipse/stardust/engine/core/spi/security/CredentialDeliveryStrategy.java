package org.eclipse.stardust.engine.core.spi.security;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public interface CredentialDeliveryStrategy {

	/**
	 * 
	 * @param user
	 * @param token
	 */
	void deliverPasswordResetToken(IUser user, String token);
	
	/**
	 * 
	 * @param user
	 * @param password
	 */
	void deliverNewPassword(IUser user, String password);
	
}
