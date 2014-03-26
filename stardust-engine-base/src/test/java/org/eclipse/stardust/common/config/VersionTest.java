package org.eclipse.stardust.common.config;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.ws.ServiceMode;

import org.junit.Before;
import org.junit.Test;

public class VersionTest
{
   ResourceBundle versionBundle;
   
   @Before
   public void tearUp()
   {
      versionBundle = ResourceBundle.getBundle(
            CurrentVersion.class.getPackage().getName() + ".version",
            Locale.getDefault(), CurrentVersion.class.getClassLoader());
   }
   
   @Test
   public void testCopyRight()
   {
      String copyRightMsg = versionBundle.getString("copyright.message");
      assertEquals(copyRightMsg, CurrentVersion.COPYRIGHT_MESSAGE);
   }
   
   @Test
   public void testVersion()
   {
      Version currentVersion = CurrentVersion.getVersion();
      String plainVersion = versionBundle.getString("version");
      plainVersion = plainVersion.replaceAll("-SNAPSHOT", "");
      Version createdVersion = new Version(plainVersion);
      assertEquals(createdVersion, currentVersion);
   }
}
