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
package org.eclipse.stardust.common.log;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * ArchivingFileAppender extends FileAppender to archive the log files when
 * they reach a certain size.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public final class ArchivingFileAppender extends FileAppender
{
   /**
    * <p>A string constant used in naming the option for setting the format of
    * the timestamp that is appended to the name of an archived log
    * file.
    *
    * <p>Current value of this string constant is <b>TimestampFormat</b>.
    */
   public static final String TIMESTAMP_FORMAT_OPTION = "TimestampFormat";

   /** The default maximum file size is 8MB. */
   private long maxFileSize = 8 * 1024 * 1024;

   /** The formatter for the timestamp */
   private SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

   /**
    * The default constructor simply calls its {@link
    * FileAppender#FileAppender parents constructor}.
    */
   public ArchivingFileAppender()
   {
      super();
   }

   /**
    * Instantiate a ArchivingFileAppender and open the file designated by
    * <code>filename</code>. The opened filename will become the output
    * destination for this appender.
    *
    * <p>The file will be appended to.
    *
    * @param layout
    * @param fileName
    */
   public ArchivingFileAppender(Layout layout, String fileName) throws IOException
   {
      this(layout, fileName, true);
   }

   /**
    * Instantiate a ArchivingFileAppender and open the file designated by
    * <code>filename</code>. The opened filename will become the ouput
    * destination for this appender.
    *
    * <p>If the <code>append</code> parameter is true, the file will be
    * appended to. Otherwise, the file desginated by <code>filename</code> will
    * be truncated before being opened.
    *
    * @param layout
    * @param fileName
    * @param append
    */
   public ArchivingFileAppender(Layout layout, String fileName, boolean append)
         throws IOException
   {
      super(layout, fileName, append);

      activateOptions();
   }

   public void activateOptions()
   {
      super.activateOptions();

      if (getAppend() && (null != qw))
      {
         File f = new File(getFile());
         ((CountingQuietWriter) qw).setCount(f.length());
      }
   }

   /**
    * Set the maximum size that the output file is allowed to reach before
    * being archived.
    *
    * @param value
    */
   public void setMaxFileSize(String value)
   {
      maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
   }

   /**
    * @param timestampFormat
    */
   public synchronized void setTimestampFormat(String timestampFormat)
   {
      this.timestampFormatter = new SimpleDateFormat(timestampFormat);
   }

   /**
    *  Implements the usual archiving behaviour.
    */
   public synchronized void archive()
   {
      File target = new File(getFile() + "-" + timestampFormatter.format(new Date()));
      File file = new File(getFile());

      closeFile();

      file.renameTo(target);

         // This will also close the file. This is OK since multiple
         // close operations are safe.
      this.activateOptions();
   }

   protected void setQWForFiles(Writer writer)
   {
      this.qw = new CountingQuietWriter(writer, errorHandler);
   }

   /**
    * This method differentiates ArchivingFileAppender from its super class.
    *
    * @param event an event to log
    */
   protected void subAppend(LoggingEvent event)
   {
      super.subAppend(event);
      if ((fileName != null) &&
            ((CountingQuietWriter) qw).getCount() >= maxFileSize)
         this.archive();
   }

}
