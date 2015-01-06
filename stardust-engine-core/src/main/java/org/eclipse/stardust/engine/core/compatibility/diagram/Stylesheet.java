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
/**
 * @author Marc Gille
 */
package org.eclipse.stardust.engine.core.compatibility.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.removethis.DiagramProperties;


public class Stylesheet
{
    private static final Logger trace = LogManager.getLogger(Stylesheet.class);

    private static Stylesheet singleton;

    HashMap tags;
    boolean loaded;

    public synchronized static Stylesheet instance()
    {
        if (singleton == null)
        {
            singleton = new Stylesheet();
        }
        return singleton;
    }

    private Stylesheet()
    {
        tags = new HashMap();

        loadFromFile();
    }

    /**
     * Indicates, whether a stylesheet is loaded and style information is
     * available.
     *
     * @return
     */
    public boolean isLoaded()
    {
        return loaded;
    }

    public String getString(String tag, String name)
    {
        Properties properties = (Properties) tags.get(tag);

        if (properties == null)
        {
            return null;
        }

        return properties.getProperty(name);
    }

    public String getString(String tag, String name, String defaultValue)
    {
        String result = getString(tag, name);

        if (result != null)
        {
            return result;
        }

        return defaultValue;
    }

    public int getInteger(String tag, String name, int defaultValue)
    {
        String value = getString(tag, name);

        if (value == null)
        {
            return defaultValue;
        }

        value = value.trim();

        try
        {
            return (int)Double.parseDouble(value);
        }
        catch (NumberFormatException x)
        {
         throw new PublicException(
               BpmRuntimeError.DIAG_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER.raise(
                     value, name));
        }
    }

    /**
     *
     */
    public long getLong(String tag, String name, long defaultValue)
    {
        String value = getString(tag, name);

        if (value == null)
        {
            return defaultValue;
        }

        value = value.trim();

        try
        {
            return (new Long(value)).longValue();
        }
        catch (NumberFormatException x)
        {
           throw new PublicException(
                 BpmRuntimeError.DIAG_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER.raise(
                       value, name));
        }
    }

    /**
     *
     */
    public boolean getBoolean(String tag, String name, boolean defaultValue)
    {
        final String[] trueWords = {"true", "enabled", "on"};
        final String[] falseWords = {"false", "disabled", "off"};

        String value = getString(tag, name);

        if (value == null)
        {
            return defaultValue;
        }

        value = value.toLowerCase().trim();

        for (int i = 0; i < trueWords.length; ++i)
            if (trueWords[i].equals(value))
                return true;

        for (int i = 0; i < falseWords.length; ++i)
            if (falseWords[i].equals(value))
                return false;

      throw new PublicException(
            BpmRuntimeError.DIAG_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_TRUE_OR_FALSE
                  .raise(value, name));
    }

    /**
     *
     */
    public Color getColor(String tag, String name, Color defaultValue)
    {
        String value = getString(tag, name);

        if (value == null)
        {
            return defaultValue;
        }

        if (value.length() < 6)
        {
            StringBuffer buffer = new StringBuffer();

            for (int n = 0; n < 6 - value.length(); ++n)
            {
                buffer.append(' ');
            }

            buffer.append(value);

            value = buffer.toString();
        }

        try
        {
            return new Color(Integer.parseInt(value.substring(0, 2)),
                             Integer.parseInt(value.substring(2, 4)),
                             Integer.parseInt(value.substring(4, 6)));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
            //throw new PublicException("The entry '" + value + "' for the property '" + name + "'\n cannot be mapped to a color.");
        }
    }

    /**
     *
     */
    public Stroke getStroke(String tag, String name, Stroke defaultValue)
    {
        String widthString = getString(tag, name + "-width");
        String capString = getString(tag, name + "-cap");
        String joinString = getString(tag, name + "-join");
        String miterString = getString(tag, name + "-miter");
        String patternString = getString(tag, name + "-pattern");

        if (widthString == null)
        {
            return defaultValue;
        }

        try
        {
            return new BasicStroke(Float.parseFloat(widthString));
        }
        catch (NumberFormatException e)
        {
         throw new PublicException(
               BpmRuntimeError.DIAG_FAILED_TO_CREATE_STROKE_THE_PROPERTY_SET.raise(name));
        }
    }

    /**
     *
     */
    public Font getFont(String tag, String name, Font defaultValue)
    {
        String sizeString = getString(tag, name + "-size");
        String familyString = getString(tag, name + "-family", "SansSerif").trim();
        String styleString = getString(tag, name + "-style", "plain").trim();

        if (sizeString == null)
        {
            return defaultValue;
        }

        try
        {
            int style = Font.PLAIN;

            if (styleString.equalsIgnoreCase("bold"))
            {
                style = Font.BOLD;
            }
            else if (styleString.equalsIgnoreCase("italic"))
            {
                style = Font.ITALIC;
            }

            Font font =  new Font(familyString, style,  (int)Double.parseDouble(sizeString.trim()));

            return font;
        }
        catch (NumberFormatException e)
        {
         throw new PublicException(
               BpmRuntimeError.DIAG_FAILED_TO_CREATE_FONT_FOR_THE_PROPERTY_MALFORMED_SIZE_STRING
                     .raise(name, sizeString.trim()));
        }
    }

    private void loadFromFile()
    {
        InputStream inputStream = null;

        try
        {
            String resourcePath = Parameters.instance().getString(
                  DiagramProperties.STYLESHEET_PATH);

            if (resourcePath == null || resourcePath.length() == 0)
            {
                return;
            }

            inputStream = getClass().getResourceAsStream(resourcePath);

            StreamTokenizer streamTokenizer = new StreamTokenizer(new InputStreamReader(inputStream));
            Properties properties = null;
            String name = null;
            String value = null;

            int token;

            while ((token = streamTokenizer.nextToken()) != StreamTokenizer.TT_EOF)
            {
                switch (token)
                {
                    case StreamTokenizer.TT_WORD:
                        {
                            if (properties == null)
                            {
                                properties = new Properties();

                                tags.put(streamTokenizer.sval, properties);
                            }
                            else if (name == null)
                            {
                                name = streamTokenizer.sval;
                            }
                            else
                            {
                                value = streamTokenizer.sval;

                                trace.debug("Putting: " + name + " = " + value);

                                properties.put(name, value);

                                name = null;
                                value = null;
                            }

                            break;
                        }
                    case StreamTokenizer.TT_NUMBER:
                        {
                            value = "" + streamTokenizer.nval;

                            trace.debug("Putting: " + name + " = " + value);

                            properties.put(name, value);

                            name = null;
                            value = null;

                            break;
                        }
                    default:
                        {
                            if (token == '}')
                            {
                                properties = null;
                                name = null;
                                value = null;
                            }
                            else if (token == '{')
                            {
                                if (properties == null)
                                {
                                    throw new PublicException(BpmRuntimeError.DIAG_NO_TAG_SPECIFED_BEFORE_CURLY_BRACE.raise());
                                }
                            }
                            else if (token == ':')
                            {
                                if (name == null)
                                {
                                   throw new PublicException(BpmRuntimeError.DIAG_NO_TAG_SPECIFED_BEFORE_COLON.raise());
                                }
                            }

                            break;
                        }
                }

            }

            loaded = true;
        }
        catch (Exception e)
        {
            // Ignore exceptions, only log

            trace.warn("", e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    }
}

