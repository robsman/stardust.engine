package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.util.Date;

public interface FileMessage extends Message
{
   public abstract String getCamelFileName();

   public abstract String getCamelFileNameOnly();

   public abstract boolean getCamelFileAbsolute();

   public abstract String getCamelFileAbsolutePath();

   public abstract String getCamelFilePath();

   public abstract String getCamelFileRelativePath();

   public abstract String getCamelFileParent();

   public abstract long getCamelFileLength();

   public abstract Date getCamelFileLastModified();
}