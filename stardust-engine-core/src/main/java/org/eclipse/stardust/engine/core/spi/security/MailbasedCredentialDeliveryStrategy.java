package org.eclipse.stardust.engine.core.spi.security;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.MailHelper;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;

/**
 *
 * @author Thomas.Wolfram
 *
 */
public class MailbasedCredentialDeliveryStrategy implements
		CredentialDeliveryStrategy {

	Logger trace = LogManager
			.getLogger(MailbasedCredentialDeliveryStrategy.class);


	@Override
	public void deliverPasswordResetToken(IUser user, String token) {
		if(!StringUtils.isEmpty(user.getEMail()))
		{
			try
			{
				String resetUrl = getResetServletUrl();
				String message = "Dear user '" + user.getAccount() + "'!\n\n" +
				"A password reset request has been made for your account. In order to complete the password reset request please follow the link below. \n" +
				"If you did not initiate a password reset please login as usual. This will abort the password reset request. \n\n";
				if(!StringUtils.isEmpty(resetUrl))
				{
					message += resetUrl + "?account=" + user.getAccount() + "&partition=" +user.getRealm().getPartition().getId() + "&realm=" +user.getRealm().getId() +"&token=" + token;
				}
				MailHelper.sendSimpleMessage(new String[] {user.getEMail()}, "Password reset token has been generated", message);
			}
			catch (PublicException e)
			{
				throw e;
			}
		}

	}

	@Override
	public void deliverNewPassword(IUser user, String password) {
	     if(!StringUtils.isEmpty(user.getEMail()))
	      {
	         try
	         {
	            String loginUrl = getLoginUrl();
	            String message = "Dear user '" + user.getAccount() + "'!\n\n" +
	               "Your password has been changed to \"" + password + "\". Please change your password or contact an Administrator.\n" +
	               "Login with your new password, the Dialog will force you next to change your password.\n\n";
	            if(!StringUtils.isEmpty(loginUrl))
	            {
	               message += loginUrl;
	            }

	            MailHelper.sendSimpleMessage(new String[] {user.getEMail()}, "Password has been changed!", message);
	         }
	         catch (PublicException e)
	         {
	            throw e;
	         }
	      }

	}

	private static String getLoginUrl() {
		return Parameters.instance().getString(SecurityUtils.LOGIN_DIALOG_URL, "").trim();
	}

	private static String getResetServletUrl()
	{
		return Parameters.instance().getString(SecurityUtils.RESET_SERVLET_URL, "").trim();
	}

}
