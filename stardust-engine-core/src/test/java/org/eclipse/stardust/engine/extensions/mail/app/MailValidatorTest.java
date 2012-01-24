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
package org.eclipse.stardust.engine.extensions.mail.app;

import java.util.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.ActivityBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.DataMappingBean;
import org.eclipse.stardust.engine.extensions.mail.app.MailConstants;
import org.eclipse.stardust.engine.extensions.mail.app.MailValidator;

import junit.framework.TestCase;


/**
 * @author fuhrmann
 * @version $Revision$
 */
public class MailValidatorTest extends TestCase
{
   private static final String VALID_MAIL_ADDRESS = "test@mail.com";

   private static final String INVALID_MAIL_ADDRESS = "te/st@mail.c";

   private List allDataMappings;

   private List dataMappingsWithOther;

   private List dataMappingsWithSenderDM;

   private List dataMappingsWithReceiverDM;

   private List emptyDataMappings;

   private Map allAttValues;

   private Map senderAttValues;

   private Map receiverAttValues;

   private Map emptyAttValues;

   private Map invalidAttValues;

   protected void setUp()
   {
      DataMappingBean fromMailDataMapping = new DataMappingBean("fromMail", "from Mail", null, null,
            Direction.IN, PredefinedConstants.FROM_ADDRESS);

      DataMappingBean toMailDataMapping = new DataMappingBean("toMail", "to Mail", null, null,
            Direction.IN, PredefinedConstants.TO_ADDRESS);

      DataMappingBean otherDataMapping = new DataMappingBean("bla", "bla", null, null,
            Direction.IN, null);

      allDataMappings = new ArrayList();
      allDataMappings.add(fromMailDataMapping);
      allDataMappings.add(toMailDataMapping);

      dataMappingsWithOther = new ArrayList();
      dataMappingsWithOther.add(fromMailDataMapping);
      dataMappingsWithOther.add(toMailDataMapping);
      dataMappingsWithOther.add(otherDataMapping);

      dataMappingsWithSenderDM = new ArrayList();
      dataMappingsWithSenderDM.add(fromMailDataMapping);

      dataMappingsWithReceiverDM = new ArrayList();
      dataMappingsWithReceiverDM.add(toMailDataMapping);

      emptyDataMappings = Collections.EMPTY_LIST;

      allAttValues = new HashMap();
      allAttValues.put(MailConstants.DEFAULT_MAIL_FROM, VALID_MAIL_ADDRESS);
      allAttValues.put(MailConstants.DEFAULT_MAIL_TO, VALID_MAIL_ADDRESS);

      invalidAttValues = new HashMap();
      invalidAttValues.put(MailConstants.DEFAULT_MAIL_FROM, INVALID_MAIL_ADDRESS);
      invalidAttValues.put(MailConstants.DEFAULT_MAIL_TO, VALID_MAIL_ADDRESS);

      senderAttValues = new HashMap();
      senderAttValues.put(MailConstants.DEFAULT_MAIL_FROM, VALID_MAIL_ADDRESS);

      receiverAttValues = new HashMap();
      receiverAttValues.put(MailConstants.DEFAULT_MAIL_TO, VALID_MAIL_ADDRESS);

      emptyAttValues = Collections.EMPTY_MAP;
   }

   public void testValidateApplicationWithAllAtt()
   {
      IActivity activity = createActivity(emptyDataMappings);
      List activities = new ArrayList();
      activities.add(activity);
      IApplication application = createApplication(allAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationWithAllValues()
   {
      IActivity activity = createActivity(allDataMappings);
      List activities = new ArrayList();
      activities.add(activity);

      IApplication application = createApplication(allAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationWithInvalidValues()
   {
      IActivity activity = createActivity(allDataMappings);
      List activities = new ArrayList();
      activities.add(activity);

      IApplication application = createApplication(invalidAttValues, activities);
      MailValidator validator = new MailValidator();
      assertEquals("invalid sender mail address: expected 1 inconsistency", 1, validator
            .validate(application).size());
   }

   public void testValidateApplicationWithNoValues()
   {
      IActivity activity = createActivity(emptyDataMappings);
      List activities = new ArrayList();
      activities.add(activity);

      IApplication application = createApplication(emptyAttValues, activities);
      MailValidator validator = new MailValidator();
      assertEquals(
            "no data mappings and no attribute values specified: expected 2 inconsistencies",
            2, validator.validate(application).size());
   }

   public void testValidateApplicationNoMailToAttAndDM()
   {
      IActivity activity = createActivity(dataMappingsWithSenderDM);
      List activities = new ArrayList();
      activities.add(activity);

      IApplication application = createApplication(senderAttValues, activities);
      MailValidator validator = new MailValidator();
      assertEquals("no receiver address: expected 1 inconsistency", 1, validator
            .validate(application).size());
   }

   public void testValidateApplicationWithOtherDM()
   {
      IActivity activity = createActivity(dataMappingsWithOther);
      List activities = new ArrayList();
      activities.add(activity);

      IApplication application = createApplication(emptyAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationMultipleActivitiesWithAllValues()
   {
      IActivity activityAllValues = createActivity(allDataMappings);
      IActivity activityAllValues2 = createActivity(allDataMappings);

      List activities = new ArrayList();
      activities.add(activityAllValues);
      activities.add(activityAllValues2);

      IApplication application = createApplication(allAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationMultipleActivitiesWithAllValuesAsDM()
   {
      IActivity activityAllValues = createActivity(allDataMappings);
      IActivity activityAllValues2 = createActivity(allDataMappings);

      List activities = new ArrayList();
      activities.add(activityAllValues);
      activities.add(activityAllValues2);

      IApplication application = createApplication(emptyAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationMultipleActivitiesWithAllValuesAsAtt()
   {
      IActivity activityNoValues = createActivity(emptyDataMappings);
      IActivity activityNoValues2 = createActivity(emptyDataMappings);

      List activities = new ArrayList();
      activities.add(activityNoValues);
      activities.add(activityNoValues2);

      IApplication application = createApplication(allAttValues, activities);
      MailValidator validator = new MailValidator();
      assertTrue("valid mail application: expected no inconsistency", validator.validate(
            application).isEmpty());
   }

   public void testValidateApplicationMultipleActivitiesNotAllValues()
   {
      IActivity activityAllValues = createActivity(allDataMappings);
      IActivity activityNotAllValues = createActivity(dataMappingsWithReceiverDM);

      List activities = new ArrayList();
      activities.add(activityAllValues);
      activities.add(activityNotAllValues);

      IApplication application = createApplication(emptyAttValues, activities);
      MailValidator validator = new MailValidator();
      assertEquals(
            "no sender address in one of the executed activities: expected 1 inconsistency",
            1, validator.validate(application).size());
   }

   private IApplication createApplication(final Map attributeMap, final List activities)
   {
      IApplication application = new ApplicationBean(null, null, null)
      {
         private static final long serialVersionUID = -2552898197050692800L;

         public Map getAllAttributes()
         {
            return attributeMap;
         }

         public PluggableType getType()
         {
            return new ApplicationTypeBean();
         }
      };
      application.setAllAttributes(attributeMap);
      return application;
   }

   private IActivity createActivity(final List dataMappings)
   {
      final IActivity activity = new ActivityBean(null, null, null)
      {
         private static final long serialVersionUID = 5042195007520625233L;

         public ImplementationType getImplementationType()
         {
            return ImplementationType.Application;
         }

         public Iterator getAllInDataMappings()
         {
            return dataMappings.iterator();
         }
      };
      Iterator<IDataMapping> iter = dataMappings.iterator();
      while (iter.hasNext()) {
    	  activity.addToDataMappings(iter.next());
      }
      return activity;
   }
}