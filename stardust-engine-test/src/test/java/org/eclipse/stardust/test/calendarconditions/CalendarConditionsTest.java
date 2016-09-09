/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.calendarconditions;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.calendarconditions.BusinessCalendarUtil.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Scripting;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.javascript.CalendarWrapper;
import org.eclipse.stardust.engine.core.javascript.ConditionEvaluator;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class tests Calendar Conditions functionality.
 * <p>
 *
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class CalendarConditionsTest
{
   private static final String MODEL = "CalendarConditionsModel";   
   private static final String PROCESS = new QName(MODEL, "WorkdayProcess").toString();
      
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void testGet()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.get(" + Calendar.DAY_OF_WEEK + ")==" + now.get(Calendar.DAY_OF_WEEK);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testAfter()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      Calendar newOne = (Calendar) now.clone();
      newOne.add(Calendar.HOUR_OF_DAY, 1);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      symbolTable.registerSymbol("NEW_DATE", newOne);
      String condition = "CURRENT_DATE.after(NEW_DATE)";
      Assert.assertFalse(now.after(newOne));
      Assert.assertEquals(now.after(newOne), ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testBefore()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      Calendar newOne = (Calendar) now.clone();
      newOne.add(Calendar.HOUR_OF_DAY, 1);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      symbolTable.registerSymbol("NEW_DATE", newOne);
      String condition = "CURRENT_DATE.before(NEW_DATE)";
      Assert.assertTrue(now.before(newOne));
      Assert.assertEquals(now.before(newOne), ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testDayOfWeek()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.day==" + now.get(Calendar.DAY_OF_WEEK);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testDayOfMonth()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.date==" + now.get(Calendar.DAY_OF_MONTH);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testMonth()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.month==" + (now.get(Calendar.MONTH) + 1);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testYear()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.year==" + now.get(Calendar.YEAR);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testWeekOfYear()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance();
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.week==" + now.get(Calendar.WEEK_OF_YEAR);
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testBusinessDate()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(5, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.businessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(21, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.businessDate==15";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testBusinessDay()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(5, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.businessDay";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(18, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.businessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(24, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.businessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(16, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.businessDay";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testNextBusinessDate()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(14, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.nextBusinessDate==15";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(1, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==2";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(2, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==5";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(3, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==5";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(4, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==5";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(5, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==6";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(23, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==26";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(29, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==30";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(30, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.nextBusinessDate==-1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testPreviousBusinessDate()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(14, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.previousBusinessDate==13";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(27, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==26";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(26, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==23";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(25, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==23";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(24, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==23";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(23, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==22";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(1, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==-1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(3, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==-1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(2, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==-1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(1, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==-1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(4, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.previousBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testFirstBusinessDay()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(1, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(2, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(3, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(4, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(31, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testLastBusinessDay()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(1, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(2, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(3, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(4, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(31, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDay";
      Assert.assertFalse(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testFirstBusinessDate()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(1, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.firstBusinessDate==1";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(1, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(2, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(3, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(4, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(30, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.firstBusinessDate==3";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testLastBusinessDate()
   {
      DebugSymbolTable symbolTable = new DebugSymbolTable();

      Calendar now = getInstance(1, 9, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      String condition = "CURRENT_DATE.lastBusinessDate==30";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(21, 10, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.lastBusinessDate==31";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(1, 7, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.lastBusinessDate==29";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(29, 7, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.lastBusinessDate==29";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(30, 7, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.lastBusinessDate==29";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));

      now = getInstance(31, 7, 2016);
      symbolTable.registerSymbol("CURRENT_DATE", now);
      condition = "CURRENT_DATE.lastBusinessDate==29";
      Assert.assertTrue(ConditionEvaluator.isEnabled(newModelElement(), symbolTable, condition));
   }

   @Test
   public void testEngineEvaluation() throws Exception
   {
      Calendar start = Calendar.getInstance();
      start.set(2016, Calendar.SEPTEMBER, 1);
      setStartOfDay(start);

      Calendar end = Calendar.getInstance();
      end.set(2016, Calendar.SEPTEMBER, 30);
      setEndOfDay(end);

      Calendar exec = Calendar.getInstance();
      exec.add(Calendar.MINUTE, 1);

      JsonObject timeOffJson = timeOffCalendar("Florin's Timeoff", null,
         timeOffEvent("Weekend", weeklySchedule(exec, true, start, end, Calendar.SATURDAY, Calendar.SUNDAY))
      );
      Document timeOffCalendar = createTimeOffCalendar(sf.getDocumentManagementService(), "FlorinTimeOff.json", timeOffJson, sf.getUserService().getUser().getId());

      JsonObject processingJson = processingCalendar("General Process Start", importCalendars(timeOffCalendar),
         startEvent("Dailies", PROCESS,
            dailySchedule(exec, true, start, end))
      );
      Document processingCalendar = createProcessingCalendar(sf.getDocumentManagementService(), "generalProcessStart.json", processingJson, sf.getUserService().getUser().getId());

      Calendar workday = Calendar.getInstance();
      workday.set(2016, Calendar.SEPTEMBER, 12);

      StartOptions options = new StartOptions(Collections.singletonMap(PredefinedConstants.BUSINESS_DATE, workday), true);
      options.setProperty(StartOptions.BUSINESS_CALENDAR, processingCalendar.getPath());

      ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS, options);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.where(WorklistQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      @SuppressWarnings("unchecked")
      List<ActivityInstance> ais1 = sf.getWorkflowService().getWorklist(query).getCumulatedItems();
      Assert.assertEquals(1, ais1.size());
      ActivityInstance ai = ais1.get(0);
      Assert.assertEquals("WorkDay", ai.getActivity().getId());
      complete(ai);

      Calendar freeday = Calendar.getInstance();
      freeday.set(2016, Calendar.SEPTEMBER, 11);

      options = new StartOptions(Collections.singletonMap(PredefinedConstants.BUSINESS_DATE, freeday), true);
      options.setProperty(StartOptions.BUSINESS_CALENDAR, processingCalendar.getPath());

      pi = sf.getWorkflowService().startProcess(PROCESS, options);

      query = WorklistQuery.findCompleteWorklist();
      query.where(WorklistQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      @SuppressWarnings("unchecked")
      List<ActivityInstance> ais2 = sf.getWorkflowService().getWorklist(query).getCumulatedItems();
      Assert.assertEquals(1, ais2.size());
      ai = ais2.get(0);
      Assert.assertEquals("FreeDay", ai.getActivity().getId());
      complete(ai);
   }
   
   protected ActivityInstance complete(ActivityInstance ai)
   {
      WorkflowService ws = sf.getWorkflowService();
      if (ai.getState() != ActivityInstanceState.Application)
      {
         ai = ws.activate(ai.getOID());
      }
      ai = ws.complete(ai.getOID(), null, null);
      return ai;
   }
   
   public static ModelElement newModelElement()
   {
      ModelElement element = new ModelElementBean()
      {
         private static final long serialVersionUID = 1L;

         ModelBean modelBean = new ModelBean("Test", "Test", null)
         {
            private static final long serialVersionUID = 1L;

            private Scripting scripting = new Scripting(Scripting.ECMA_SCRIPT, null, null);

            public Scripting getScripting()
            {
               return scripting;
            }

         };

         public RootElement getModel()
         {
            return modelBean;
         }
      };
      return element;
   }
   
   static class DebugSymbolTable implements SymbolTable
   {
      private Map<String, Object> values = CollectionUtils.newMap();
      private Map<String, AccessPoint> types = CollectionUtils.newMap();

      public AccessPoint lookupSymbolType(String name)
      {
         return types.get(name);
      }

      public Object lookupSymbol(String name)
      {
         return values.get(name);
      }

      void registerSymbol(String name, Object value)
      {
         values.put(name, value);
         types.put(name, new JavaAccessPoint(name, name, Direction.IN_OUT));
      }
   }
      
   public static class TestCalendarWrapper extends CalendarWrapper
   {
      private static final long serialVersionUID = 1L;

      public TestCalendarWrapper(Calendar calendar)
      {
         super(null, calendar);
      }

      protected boolean isBusinessDay(Calendar calendar)
      {
         int day = calendar.get(Calendar.DAY_OF_WEEK);
         return day != Calendar.SATURDAY && day != Calendar.SUNDAY;
      }
   }

   public static TestCalendarWrapper getInstance()
   {
      return new TestCalendarWrapper(Calendar.getInstance());
   }

   public static TestCalendarWrapper getInstance(int date, int month, int year)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, month - 1, date);
      return new TestCalendarWrapper(calendar);
   }   
}