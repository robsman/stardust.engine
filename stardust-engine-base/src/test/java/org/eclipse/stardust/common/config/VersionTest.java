package org.eclipse.stardust.common.config;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.BeforeClass;
import org.junit.Test;

public class VersionTest
{
   protected static String SNAPSHOT_ALIAS_VAL;
   protected static String BASE_VERSION_VAL;
   protected static String BUILD_VAL;
   protected static String COPYRIGHT_VAL;
   protected static String VENDOR_VAL;
   protected static String PRODUCT_VAL;
   
   @BeforeClass
   public static void loadPropertyValues()
   {
      // Take care that the version.properties of the src/test/resources folder is loaded
      ResourceBundle versionBundle = ResourceBundle.getBundle(
            CurrentVersion.class.getPackage().getName() + ".version",
            Locale.getDefault(), CurrentVersion.class.getClassLoader());
      PRODUCT_VAL = versionBundle.getString("product.name");
      VENDOR_VAL = versionBundle.getString("vendor.name");
      COPYRIGHT_VAL = versionBundle.getString("copyright.message");
      BUILD_VAL = versionBundle.getString("build");
      SNAPSHOT_ALIAS_VAL = versionBundle.getString("snapshot.alias");
      BASE_VERSION_VAL = versionBundle.getString("base.version");
   }
      
   @Test
   public void testCopyRight()
   {
      assertEquals(COPYRIGHT_VAL,
            CurrentVersion.COPYRIGHT_MESSAGE);
   }
   
   @Test
   public void testVersion()
   {
      Version createdVersion = new Version(BASE_VERSION_VAL + "-" + SNAPSHOT_ALIAS_VAL);
      assertEquals(createdVersion, CurrentVersion.getVersion());
   }
   
   @Test
   public void testBuildVersionName()
   {
      String version = BASE_VERSION_VAL + "." + BUILD_VAL + "-" + SNAPSHOT_ALIAS_VAL.replace("-SNAPSHOT", "");
      assertEquals(version, CurrentVersion.getBuildVersionName());
   }
   
   @Test
   public void testProductName()
   {
      assertEquals(PRODUCT_VAL, 
            CurrentVersion.getProductName()); 
   }
   
   @Test
   public void testVendorName()
   {
      assertEquals(VENDOR_VAL, 
            CurrentVersion.getVendorName()); 
   }
}
