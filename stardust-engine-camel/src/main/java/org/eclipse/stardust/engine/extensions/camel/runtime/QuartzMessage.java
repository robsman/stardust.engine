package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public interface QuartzMessage extends Message
{
   public abstract Calendar getCalendar();

   public abstract Date getFireTime();

   public abstract JobDetail getJobDetail();

   public abstract Job getJobInstance();

   public abstract long getJobRunTime();

   public abstract JobDataMap getMergedJobDataMap();

   public abstract Date getNetNextFireTime();

   public abstract Date getPreviousFireTime();

   public abstract int getRefireCount();

   public abstract Object getResult();

   public abstract Date getScheduledFireTime();

   public abstract Scheduler getScheduler();

   public abstract Trigger getTrigger();

   public abstract String getTriggerName();

   public abstract String getTriggerGroup();
}
