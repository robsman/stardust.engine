/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.preferences;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.stardust.engine.core.preferences.XmlPreferenceReader;
import org.eclipse.stardust.engine.core.preferences.XmlPreferenceWriter;
import org.junit.Test;

public class XmlPreferencesStorageTest
{
   private final XmlPreferenceReader prefsReader = new XmlPreferenceReader();

   private final XmlPreferenceWriter prefsWriter = new XmlPreferenceWriter();

   @Test
   public void serializedPreferencesMustBeReadFully() throws IOException
   {
      Map<String, Object> roundtrippedPrefs = prefsReader.readPreferences(getClass().getResourceAsStream("prefs.xml"));

      assertThat(roundtrippedPrefs, is(not(nullValue())));
      assertThat(roundtrippedPrefs.size(), is(not(0)));
      assertThat(roundtrippedPrefs, hasEntry("aString", (Object) "abcdefg"));
      assertThat(roundtrippedPrefs, hasEntry("trueValue", (Object) true));
      assertThat(roundtrippedPrefs, hasEntry("falseValue", (Object) false));
      assertThat(roundtrippedPrefs, hasEntry("aLong", (Object) 42L));
      assertThat(roundtrippedPrefs, hasEntry("anInt", (Object) 42));
      assertThat(roundtrippedPrefs, hasEntry("aFloat", (Object) 3.1415F));
      assertThat(roundtrippedPrefs, hasEntry("aDouble", (Object) 3.1415D));
      assertThat(roundtrippedPrefs,
            hasEntry("aListOfStrings", (Object) Arrays.asList("a", "b", "c")));
   }

   @Test
   public void preferencesSerializedWithVersionPre7MustBeReadFully() throws IOException
   {
      Map<String, Object> roundtrippedPrefs = prefsReader.readPreferences(getClass().getResourceAsStream("pre70prefs.xml"));

      assertThat(roundtrippedPrefs, is(not(nullValue())));
      assertThat(roundtrippedPrefs.size(), is(not(0)));
      assertThat(roundtrippedPrefs, hasEntry("aString", (Object) "abcdefg"));
      assertThat(roundtrippedPrefs, hasEntry("trueValue", (Object) true));
      assertThat(roundtrippedPrefs, hasEntry("falseValue", (Object) false));
      assertThat(roundtrippedPrefs, hasEntry("aLong", (Object) 42L));
      assertThat(roundtrippedPrefs, hasEntry("anInt", (Object) 42));
      assertThat(roundtrippedPrefs, hasEntry("aFloat", (Object) 3.1415F));
      assertThat(roundtrippedPrefs, hasEntry("aDouble", (Object) 3.1415D));
      assertThat(roundtrippedPrefs,
            hasEntry("aListOfStrings", (Object) Arrays.asList("a", "b", "c")));
   }

   @Test
   public void preferencesMustBeSerializableToXml() throws IOException
   {
      Map<String, Object> prefs = newHashMap();
      prefs.put("aString", "abcdefg");
      prefs.put("trueValue", true);
      prefs.put("falseValue", false);
      prefs.put("aLong", 42L);
      prefs.put("anInt", 42);
      prefs.put("aFloat", 3.1415F);
      prefs.put("aDouble", 3.1415D);
      prefs.put("aListOfStrings", Arrays.asList("a", "b", "c"));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      prefsWriter.writePreferences(baos, "unit-testing", "test-preferences", prefs);

      Map<String, Object> roundtrippedPrefs = prefsReader.readPreferences(new ByteArrayInputStream(
            baos.toString().getBytes()));

      assertThat(roundtrippedPrefs, is(not(nullValue())));
      assertThat(roundtrippedPrefs.size(), is(not(0)));
      assertThat(roundtrippedPrefs, hasEntry("aString", (Object) "abcdefg"));
      assertThat(roundtrippedPrefs, hasEntry("trueValue", (Object) true));
      assertThat(roundtrippedPrefs, hasEntry("falseValue", (Object) false));
      assertThat(roundtrippedPrefs, hasEntry("aLong", (Object) 42L));
      assertThat(roundtrippedPrefs, hasEntry("anInt", (Object) 42));
      assertThat(roundtrippedPrefs, hasEntry("aFloat", (Object) 3.1415F));
      assertThat(roundtrippedPrefs, hasEntry("aDouble", (Object) 3.1415D));
      assertThat(roundtrippedPrefs,
            hasEntry("aListOfStrings", (Object) Arrays.asList("a", "b", "c")));
   }

}
