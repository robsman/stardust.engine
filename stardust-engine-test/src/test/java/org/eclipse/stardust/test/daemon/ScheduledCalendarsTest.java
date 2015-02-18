package org.eclipse.stardust.test.daemon;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.daemon.DaemonConstants.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.business_calendar.daemon.ScheduledCalendar;
import org.eclipse.stardust.engine.business_calendar.daemon.ScheduledCalendarFinder;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingUtils;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * <p>
 * This class tests scheduling functionality.
 * <p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class ScheduledCalendarsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   @Before
   public void setup()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);
      for (Document document : folder.getDocuments())
      {
         dms.removeDocument(document.getId());
      }      
   }
   
   @Test
   public void checkTimeoff() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document timeoffCalendar = getDocument(dms, folder, BLOCK_CALENDAR);
      JsonObject timeoffCalendarContent = getTimeoffCalendarContent(dateX, timeoffCalendar.getId());
      dms.updateDocument(timeoffCalendar,
            timeoffCalendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      importCalendars(calendarContent, timeoffCalendar.getId());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(0);
   }

   @Test
   public void checkTimeoffImportedCalendar() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      Date date = cal.getTime();

      cal.add(Calendar.MINUTE, 1);
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document importedCalendar = getDocument(dms, folder, IMPORTED_CALENDAR);
      JsonObject importedCalendarContent = getProcessingCalendarContent(date, importedCalendar.getId(), new QName(MODEL_NAME, "CustomerCreation").toString());
      dms.updateDocument(importedCalendar,
            importedCalendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      Document timeoffCalendar = getDocument(dms, folder, BLOCK_CALENDAR);
      JsonObject timeoffCalendarContent = getTimeoffCalendarContent(date, timeoffCalendar.getId());
      dms.updateDocument(timeoffCalendar,
            timeoffCalendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      importCalendars(calendarContent, importedCalendar.getId(), timeoffCalendar.getId());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(0);
   }
   
   @Test
   public void checkImportedCalendar() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      Date date = cal.getTime();

      cal.add(Calendar.MINUTE, 1);
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document importedCalendar = getDocument(dms, folder, IMPORTED_CALENDAR);
      JsonObject importedCalendarContent = getProcessingCalendarContent(date, importedCalendar.getId(), new QName(MODEL_NAME, "CustomerCreation").toString());
      dms.updateDocument(importedCalendar,
            importedCalendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      importCalendars(calendarContent, importedCalendar.getId());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(1);
   }
   
   @Test
   public void checkSelfImportedCalendar() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      importCalendars(calendarContent, calendar.getId());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(1);
   }

   @Test
   public void checkBusinessObjectWithRelationship() throws Exception
   {
      createBusinessObjects();

      Calendar cal = getAlignedCalendar();
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      JsonObject details = calendarContent.getAsJsonArray("events").get(0).getAsJsonObject().getAsJsonObject("eventDetails");
      details.add("relationship", json(
            property("otherBusinessObject", BO_FUND),
            property("otherForeignKeyField", "funds")));
      calendarContent.add("businessObjectInstance", json(
            property("modelId", MODEL_NAME),
            property("businessObjectId", "FundGroup"),
            property("primaryKey", "g1")));
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      int count = sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive()).size();
      ScheduledCalendar scheduledCalendar = checkCalendars(1).get(0);
      scheduledCalendar.execute();

      Assert.assertEquals("Started processes", 3, sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive()).size() - count);
   }
   
   @Test
   public void checkCorrectSlot() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      Date dateX = cal.getTime();

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(1);
   }

   @Test
   public void checkDifferentSlotAfter() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      checkDifferentSlot(cal, 1);
   }

   @Test
   public void checkDifferentSlotBefore() throws Exception
   {
      Calendar cal = getAlignedCalendar();
      checkDifferentSlot(cal, -1);
   }

   /* helper methods */   
   
   private void checkDifferentSlot(Calendar cal, int minutes) throws Exception,
         UnsupportedEncodingException
   {
      cal.add(Calendar.MINUTE, minutes);
      Date dateX = cal.getTime();
      cal.add(Calendar.MINUTE, -minutes); // revert change

      DocumentManagementService dms = sf.getDocumentManagementService();
      Folder folder = getCalendarFolder(dms);

      Document calendar = getDocument(dms, folder, CALENDAR);
      JsonObject calendarContent = getProcessingCalendarContent(dateX, calendar.getId(), new QName(MODEL_NAME, "OrderCreation").toString());
      dms.updateDocument(calendar,
            calendarContent.toString().getBytes("UTF-8"), "UTF-8",
            false, null, null, false);

      checkCalendars(0);
   }

   private void createBusinessObjects()
   {
      String groupId = "g1";
      List<String> ids = CollectionUtils.newList();
      for (int i = 1; i <= 3; i++)
      {
         String fundId = "f" + i;
         ids.add(fundId);

         Map<String, Object> fund = CollectionUtils.newMap();
         fund.put("id", fundId);
         fund.put("name", "Fund " + i);
         fund.put("account", i);
         fund.put("group", groupId);
         sf.getWorkflowService().createBusinessObjectInstance(BO_FUND, fund);
      }

      Map<String, Object> group = CollectionUtils.newMap();
      group.put("id", groupId);
      group.put("name", "Group 1");
      group.put("funds", ids);
      sf.getWorkflowService().createBusinessObjectInstance(BO_GROUP, group);
   }
      
   private List<ScheduledCalendar> checkCalendars(int expected)
   {
      ScheduledCalendarFinder finder = new ScheduledCalendarFinder(new Date(), sf.getDocumentManagementService())
      {
         @Override
         protected ScheduledCalendar createScheduledDocument(JsonObject documentJson,
               QName owner, String documentName, List<JsonObject> events)
         {
            return new ScheduledCalendar(documentJson, owner, documentName, events)
            {
               @Override
               protected WorkflowService getWorkflowService()
               {
                  return sf.getWorkflowService();
               }

               @Override
               protected QueryService getQueryService()
               {
                  return sf.getQueryService();
               }
            };
         }
      };

      List<ScheduledCalendar> calendars = finder.readAllDefinitions();
      Assert.assertEquals("Calendars", expected, calendars.size());
      return calendars;
   }
   
   private JsonObject getProcessingCalendarContent(Date date, String id, String processId) throws Exception
   {
      JsonObject json = json(
            property("pluginId", "processingCalendar"),
            property("uuid", id),
            property("events", array(
                  json(
                        property("active", Boolean.TRUE),
                        property("type", "processStartEvent"),
                        property("recurrenceInterval", "daily"),
                        property("dailyRecurrenceOptions", json(
                              property("daysRecurrence", "weekdays"))),
                        property("recurrenceRange", json(
                              property("startDate", getDateString(date)),
                              property("endMode", "noEnd"))),
                        property("eventDetails", json(
                              property("processDefinitionId", processId))),
                        //property("executionTime", getTimeSlot(date))))),
                        property("executionTime", SchedulingUtils.TIME_FORMAT.format(date)))))
      );
      return json;
   }
      
   private JsonObject getTimeoffCalendarContent(Date date, String id) throws Exception
   {
      JsonObject json = json(
            property("pluginId", "timeOffCalendar"),
            property("uuid", id),
            property("events", array(
                  json(
                        property("active", Boolean.TRUE),
                        property("type", "timeOff"),
                        property("recurrenceInterval", "daily"),
                        property("dailyRecurrenceOptions", json(
                              property("daysRecurrence", "weekdays"))),
                        property("recurrenceRange", json(
                              property("startDate", getDateString(date)),
                              property("endMode", "noEnd"))),
                        //property("executionTime", getTimeSlot(date))))),
                        property("executionTime", SchedulingUtils.TIME_FORMAT.format(date)))))
      );
      return json;
   }

   private JsonObject importCalendars(JsonObject json, String... imported) throws Exception
   {
      JsonArray imports = array();
      for (String uuid : imported)
      {
         imports.add(json(
               property("uuid", uuid)));
      }
      json.add("importedCalendars", imports);
      return json;
   }
   
   @SuppressWarnings({"unused", "deprecation"})
   private String getTimeSlot(Date date)
   {
      int time = date.getHours() * 2 + 1;
      if (date.getMinutes() >= 30)
      {
         time++;
      }
      String string = Integer.toString(time);
      return time < 10 ? "0" + string : string;
   }

   private String getDateString(Date date)
   {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      return format.format(date);
   }
   
   private Calendar getAlignedCalendar() throws InterruptedException
   {
      Date date = new Date();
      @SuppressWarnings("deprecation")
      int secs = date.getSeconds();
      if (secs > 30)
      {
         System.out.println("Sleeping: " + (62 - secs));
         Thread.sleep(62 - secs);
      }
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -1);
      return cal;
   }
      
   private Document getDocument(DocumentManagementService dms, Folder folder, String name)
   {
      Document calendar = dms.getDocument("/" + folder.getName() + "/" + name);
      if (calendar == null)
      {
         DocumentInfo info = DmsUtils.createDocumentInfo(name);
         calendar = dms.createDocument(folder.getId(), info , null, "UTF-8");
         System.out.println("Created document: " + calendar);
      }
      calendar.setOwner(sf.getUserService().getUser().getId());
      return calendar;
   }
      
   private Folder getCalendarFolder(DocumentManagementService dms)
   {
      Folder folder = dms.getFolder(FOLDER);
      if (folder == null)
      {
         FolderInfo info = DmsUtils.createFolderInfo(FOLDER.substring(1));
         folder = dms.createFolder("/", info);
      }
      return folder;
   }
   
   private static JsonObject json(Property... properties)
   {
      JsonObject jsonObject = new JsonObject();
      if (properties != null)
      {
         for (Property property : properties)
         {
            if (property.value instanceof Boolean)
            {
               jsonObject.addProperty(property.name, (Boolean) property.value);
            }
            else if (property.value instanceof Character)
            {
               jsonObject.addProperty(property.name, (Character) property.value);
            }
            else if (property.value instanceof Number)
            {
               jsonObject.addProperty(property.name, (Number) property.value);
            }
            else if (property.value instanceof String)
            {
               jsonObject.addProperty(property.name, (String) property.value);
            }
            else if (property.value instanceof JsonElement)
            {
               jsonObject.add(property.name, (JsonElement) property.value);
            }
         }
      }
      return jsonObject;
   }

   private static JsonArray array(Object... values)
   {
      JsonArray jsonObject = new JsonArray();
      if (values != null)
      {
         for (Object value : values)
         {
            if (value instanceof Boolean)
            {
               jsonObject.add(new JsonPrimitive((Boolean) value));
            }
            else if (value instanceof Character)
            {
               jsonObject.add(new JsonPrimitive((Character) value));
            }
            else if (value instanceof Number)
            {
               jsonObject.add(new JsonPrimitive((Number) value));
            }
            else if (value instanceof String)
            {
               jsonObject.add(new JsonPrimitive((String) value));
            }
            else if (value instanceof JsonElement)
            {
               jsonObject.add((JsonElement) value);
            }
         }
      }
      return jsonObject;
   }

   private static Property property(String name, Object value)
   {
      return new Property(name, value);
   }

   private static class Property
   {
      String name;
      Object value;

      public Property(String name, Object value)
      {
         super();
         this.name = name;
         this.value = value;
      }
   }
}