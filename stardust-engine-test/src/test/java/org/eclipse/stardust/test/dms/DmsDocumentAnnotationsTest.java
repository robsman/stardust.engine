/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.dms;

import static org.junit.Assert.assertEquals;
import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_SYNC_MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.AnnotationUtils;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Highlight;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Note;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageBookmark;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageOrientation;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Stamp;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.TextStyle;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests synchronization of workflow document data and jcr documents.
 * </p>
 *
 * @author Roland.Stamm
 * @version $Revision$
 */
public class DmsDocumentAnnotationsTest
{
 private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DMS_SYNC_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   @Test
   public void testAssignDocumentAnnotations1()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      dms.removeDocument("/docAnnotations1.txt");
      
      DocumentInfo doc = new DmsDocumentBean();
      doc.setName("docAnnotations1.txt");
      doc.setContentType("text/plain");
      DocumentAnnotations printDocumentAnnotations = getPrintDocumentAnnotations1();
      doc.setDocumentAnnotations(printDocumentAnnotations);

      Document createdDocument = dms.createDocument("/", doc);

      DocumentAnnotations createdDocumentAnnotations = createdDocument.getDocumentAnnotations();

      Map<String, Serializable> map = AnnotationUtils.toMap(printDocumentAnnotations);
      Map<String, Serializable> map2 = AnnotationUtils.toMap(createdDocumentAnnotations);
      
      assertNotNull(createdDocumentAnnotations);
      assertEquals(map, map2);

   }

   private static DocumentAnnotations getPrintDocumentAnnotations1()
   {
      PrintDocumentAnnotations a = new PrintDocumentAnnotationsImpl();

      PageBookmark pageBookmark = new PageBookmark();
      pageBookmark.setEndPage(2);
      pageBookmark.setStartPage(2);
      pageBookmark.setId("epm bookmark");
      a.addBookmark(pageBookmark);

      a.setDefaultBookmark("epm bookmark");

      Note note = new Note();
      note.setColor("#FFFFFF");
      note.setCreateDate(new Date());
      note.setId("epm note");
      note.setText("I'm a note <br>");
      note.setPageRelativeRotation(-90);
      note.setHeight(200);
      note.setWidth(200);
      note.setxCoordinate(100);
      note.setyCoordinate(100);
      note.setPageNumber(1);
      note.setCreatedByAuthor("me");
      note.setFontSize(48);
      note.setTextStyle(new HashSet<TextStyle>(Arrays.asList(TextStyle.BOLD,
            TextStyle.ITALIC, TextStyle.UNDERLINED)));
      a.addNote(note);

      Highlight highlight = new Highlight();
      highlight.setPageNumber(1);
      highlight.setId("epm highlight");
      highlight.setColor("#FFFFFF");
      highlight.setCreateDate(new Date());
      highlight.setCreatedByAuthor("me");
      highlight.setModificationDate(new Date());
      highlight.setModifiedByAuthor("me again");
      highlight.setHeight(200);
      highlight.setWidth(200);
      highlight.setxCoordinate(500);
      highlight.setyCoordinate(200);
      highlight.setPageRelativeRotation(90);
      a.addHighlight(highlight);

      PageOrientation pageOrientation = new PageOrientation();
      pageOrientation.setPageNumber(2);
      pageOrientation.setRotation(90);
      a.addPageOrientation(pageOrientation);

      Stamp stamp = new Stamp();
      stamp.setCreateDate(new Date());
      stamp.setCreatedByAuthor("me");
      stamp.setColor("#FFFFFF");
      stamp.setPageNumber(2);
      stamp.setStampDocumentId("/stamps/stamp1.stamp");
      stamp.setxCoordinate(500);
      stamp.setyCoordinate(200);
      stamp.setId("epm stamp");
      a.addStamp(stamp);

      a.setPageSequence(new ArrayList<Integer>(Arrays.asList(1, 4, 2, 3, 5)));

      return a;
   }

   @Test
   public void testAssignDocumentAnnotations2()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      dms.removeDocument("/docAnnotations2.txt");

      DocumentInfo doc = new DmsDocumentBean();

      doc.setName("docAnnotations2.txt");
      doc.setContentType("text/plain");

      DocumentAnnotations printDocumentAnnotations = getPrintDocumentAnnotations2();

      doc.setDocumentAnnotations(printDocumentAnnotations);

      Document createdDocument = dms.createDocument("/", doc);

      DocumentAnnotations createdDocumentAnnotations = createdDocument.getDocumentAnnotations();

      Map<String, Serializable> map = AnnotationUtils.toMap(printDocumentAnnotations);
      Map<String, Serializable> map2 = AnnotationUtils.toMap(createdDocumentAnnotations);
      
      assertNotNull(createdDocumentAnnotations);
      assertEquals(map, map2);

   }

   private static DocumentAnnotations getPrintDocumentAnnotations2()
   {
      PrintDocumentAnnotations a = new PrintDocumentAnnotationsImpl();

      PageBookmark pageBookmark = new PageBookmark();
      pageBookmark.setEndPage(2);
      pageBookmark.setStartPage(2);
      pageBookmark.setId("epm bookmark");
      a.addBookmark(pageBookmark);

      a.setDefaultBookmark("epm bookmark");

      Note note = new Note();
      note.setColor("#FFFFFF");
      note.setCreateDate(new Date());
      note.setId("epm note");
      note.setText("I'm a note <br>");
      a.addNote(note);

      Highlight highlight = new Highlight();
      highlight.setPageNumber(1);
      highlight.setId("epm highlight");
      highlight.setColor("#FFFFFF");
      highlight.setCreateDate(new Date());
      highlight.setCreatedByAuthor("me");
      highlight.setModificationDate(new Date());
      highlight.setModifiedByAuthor("me again");
      highlight.setHeight(200);
      highlight.setWidth(200);
      highlight.setxCoordinate(500);
      highlight.setyCoordinate(200);
      a.addHighlight(highlight);

      Highlight highlight2 = new Highlight();
      highlight2.setPageNumber(2);
      highlight2.setId("epm highlight 2");
      highlight2.setColor("#FFFFFF");
      highlight2.setCreateDate(new Date());
      highlight2.setCreatedByAuthor("me");
      highlight2.setModificationDate(new Date());
      highlight2.setModifiedByAuthor("me again");
      highlight2.setHeight(200);
      highlight2.setWidth(200);
      highlight2.setxCoordinate(500);
      highlight2.setyCoordinate(200);
      a.addHighlight(highlight2);

      PageOrientation pageOrientation = new PageOrientation();
      pageOrientation.setPageNumber(2);
      pageOrientation.setRotation(90);
      a.addPageOrientation(pageOrientation);

      PageOrientation pageOrientation2 = new PageOrientation();
      pageOrientation2.setPageNumber(1);
      pageOrientation2.setRotation( -90);
      a.addPageOrientation(pageOrientation2);

      Stamp stamp = new Stamp();
      stamp.setCreateDate(new Date());
      stamp.setCreatedByAuthor("me");
      stamp.setColor("#FFFFFF");
      stamp.setPageNumber(2);
      stamp.setStampDocumentId("/stamps/stamp1.stamp");
      stamp.setxCoordinate(500);
      stamp.setyCoordinate(200);
      stamp.setId("epm stamp");
      a.addStamp(stamp);

      a.setPageSequence(new ArrayList<Integer>(/* Arrays.asList(1, 4, 2, 3, 5) */));

      return a;
   }

   /**
    * Empty Annotations objects are translated to a map without any values.
    * Empty maps are saved and returned as null.
    */
   @Test
   public void testAssignDocumentAnnotationsEmpty()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      dms.removeDocument("/docAnnotationsEmpty.txt");

      DocumentInfo doc = new DmsDocumentBean();

      doc.setName("docAnnotationsEmpty.txt");
      doc.setContentType("text/plain");

      DocumentAnnotations printDocumentAnnotations = getPrintDocumentAnnotationsEmpty();

      doc.setDocumentAnnotations(printDocumentAnnotations);

      Document createdDocument = dms.createDocument("/", doc);

      DocumentAnnotations createdDocumentAnnotations = createdDocument.getDocumentAnnotations();

      Map<String, Serializable> map = AnnotationUtils.toMap(printDocumentAnnotations);
      Map<String, Serializable> map2 = AnnotationUtils.toMap(createdDocumentAnnotations);
      
      assertNotNull(createdDocumentAnnotations);
      assertEquals(map, map2);
      
   }

   private static DocumentAnnotations getPrintDocumentAnnotationsEmpty()
   {
      PrintDocumentAnnotations a = new PrintDocumentAnnotationsImpl();

      return a;
   }

   @Test
   public void testAssignDocumentAnnotationsNull()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      dms.removeDocument("/docAnnotationsNull.txt");

      DocumentInfo doc = new DmsDocumentBean();

      doc.setName("docAnnotationsNull.txt");
      doc.setContentType("text/plain");

      DocumentAnnotations printDocumentAnnotations = getPrintDocumentAnnotationsNull();

      doc.setDocumentAnnotations(printDocumentAnnotations);

      Document createdDocument = dms.createDocument("/", doc);

      DocumentAnnotations createdDocumentAnnotations = createdDocument.getDocumentAnnotations();

      Map<String, Serializable> map = AnnotationUtils.toMap(printDocumentAnnotations);
      Map<String, Serializable> map2 = AnnotationUtils.toMap(createdDocumentAnnotations);

      assertNull(createdDocumentAnnotations);
      assertEquals(map, map2);

   }


   private static DocumentAnnotations getPrintDocumentAnnotationsNull()
   {
      return null;
   }

   @Test
   public void testBookmarkOrder()
   {
      PrintDocumentAnnotations pda = new PrintDocumentAnnotationsImpl();

      PageBookmark pageBookmark = new PageBookmark();
      pageBookmark.setEndPage(2);
      pageBookmark.setStartPage(2);
      pageBookmark.setId("epm bookmark1");
      pda.addBookmark(pageBookmark);

      PageBookmark pageBookmark2 = new PageBookmark();
      pageBookmark2.setEndPage(2);
      pageBookmark2.setStartPage(2);
      pageBookmark2.setId("epm bookmark2");
      pda.addBookmark(pageBookmark2);

      PageBookmark pageBookmark3 = new PageBookmark();
      pageBookmark3.setEndPage(2);
      pageBookmark3.setStartPage(2);
      pageBookmark3.setId("epm bookmark3");
      pda.addBookmark(pageBookmark3);

      assertEquals(3, pda.getBookmarks().size());
      assertEquals(0, pda.getBookmark("epm bookmark1").getOrder());
      assertEquals(1, pda.getBookmark("epm bookmark2").getOrder());
      assertEquals(2, pda.getBookmark("epm bookmark3").getOrder());

      pda.moveBookmark(0, 1);

      assertEquals(3, pda.getBookmarks().size());
      assertEquals(0, pda.getBookmark("epm bookmark2").getOrder());
      assertEquals(1, pda.getBookmark("epm bookmark1").getOrder());
      assertEquals(2, pda.getBookmark("epm bookmark3").getOrder());

      pda.removeBookmark("epm bookmark2");

      assertEquals(2, pda.getBookmarks().size());
      assertEquals(0, pda.getBookmark("epm bookmark1").getOrder());
      assertEquals(1, pda.getBookmark("epm bookmark3").getOrder());
   }

   @Test
   public void testAssignDocumentAnnotationsCRNT20748()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      //Create a fresh doc
      dms.removeDocument("/docAnnotations1.txt");
      DocumentInfo doc = new DmsDocumentBean();
      doc.setName("docAnnotations1.txt");
      doc.setContentType("text/plain");
      Document newDoc = dms.createDocument("/", doc);

      //Add a single highlight annotation and update.
      newDoc.setDocumentAnnotations(getPrintDocumentAnnotations());
      dms.updateDocument(newDoc, false, "", null, false);

      Document readDoc = dms.getDocument(newDoc.getId());
      //This passes which means the annotation was saved correctly.
      assertEquals(1, ((PrintDocumentAnnotations) readDoc.getDocumentAnnotations()).getHighlights().size());

      ((PrintDocumentAnnotations) readDoc.getDocumentAnnotations()).removeHighlight("FirstHighlight");

      //This passes which indicating that the object was deleted correctly
      assertEquals(0, ((PrintDocumentAnnotations) readDoc.getDocumentAnnotations()).getHighlights().size());

      //Update the document.
      dms.updateDocument(readDoc, false, "", null, false);

      //Re-read the document
      Document readDocAgain = dms.getDocument(readDoc.getId());

      //This fails
      assertEquals(0, ((PrintDocumentAnnotations) readDocAgain.getDocumentAnnotations()).getHighlights().size());
   }

   private static DocumentAnnotations getPrintDocumentAnnotations()
   {
      PrintDocumentAnnotations a = new PrintDocumentAnnotationsImpl();
      Highlight highlight = new Highlight();
      highlight.setPageNumber(1);
      highlight.setId("FirstHighlight");
      highlight.setColor("#FFFFFF");
      highlight.setCreateDate(new Date());
      highlight.setCreatedByAuthor("me");
      highlight.setModificationDate(new Date());
      highlight.setModifiedByAuthor("me again");
      highlight.setHeight(200);
      highlight.setWidth(200);
      highlight.setxCoordinate(500);
      highlight.setyCoordinate(200);
      a.addHighlight(highlight);

      return a;
   }

   @Test
   public void testMovePages()
   {
      PrintDocumentAnnotations pda = new PrintDocumentAnnotationsImpl();

      ArrayList<Integer> inputList = new ArrayList<Integer>(Arrays.asList(1, 4, 2, 3, 5));
      pda.setPageSequence(inputList);

      assertEquals(inputList, pda.getPageSequence());

      pda.movePages(3, 2, 0);

      assertNotSame(inputList, pda.getPageSequence());

      pda.movePages(3, 0, 2);

      assertEquals(inputList, pda.getPageSequence());

      pda.movePages(4, 1, 0);

      assertNotSame(inputList, pda.getPageSequence());

      pda.movePages(4, 0, 1);

      assertEquals(inputList, pda.getPageSequence());
   }

   @Test
   public void testMovePage()
   {
      PrintDocumentAnnotations pda = new PrintDocumentAnnotationsImpl();

      ArrayList<Integer> inputList = new ArrayList<Integer>(Arrays.asList(1, 4, 2, 3, 5));
      pda.setPageSequence(inputList);

      assertEquals(inputList, pda.getPageSequence());

      pda.movePage(2, 1);
      pda.movePage(3, 2);

      assertNotSame(inputList, pda.getPageSequence());

      pda.movePage(2, 3);
      pda.movePage(1, 2);

      assertEquals(inputList, pda.getPageSequence());

      pda.movePage(0, 4);

      assertNotSame(inputList, pda.getPageSequence());

      pda.movePage(4, 0);

      assertEquals(inputList, pda.getPageSequence());

      pda.movePage(0, 0);
      pda.movePage(4, 4);

      assertEquals(inputList, pda.getPageSequence());

   }
}
