/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.model.beans;

import static org.eclipse.stardust.engine.core.model.beans.XMLConstants.NS_XPDL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.eclipse.stardust.engine.api.model.ILoopCharacteristics;
import org.eclipse.stardust.engine.api.model.IMultiInstanceLoopCharacteristics;
import org.eclipse.stardust.engine.api.model.IStandardLoopCharacteristics;
import org.eclipse.stardust.engine.core.model.parser.adapters.InternedStringAdapter;
import org.eclipse.stardust.engine.core.model.parser.filters.ConfigurationVariablesFilter;
import org.eclipse.stardust.engine.core.model.parser.filters.NamespaceFilter;
import org.w3c.dom.Element;

@XmlRootElement(name="Loop", namespace=NS_XPDL)
final class Loop
{
   static final String NS_XPDL_EXT = "http://www.carnot.ag/workflowmodel/3.1/xpdl/extensions";

   static enum LoopType
   {
      Standard, MultiInstance;

      boolean accept(ILoopCharacteristics characteristics)
      {
         switch (this)
         {
         case Standard: return characteristics instanceof IStandardLoopCharacteristics;
         case MultiInstance: return characteristics instanceof IMultiInstanceLoopCharacteristics;
         }
         return false;
      }
   }

   @XmlAttribute(name="LoopType")
   LoopType loopType;

   @XmlElementRef
   LoopCharacteristics loopCharacteristics;

   @XmlSeeAlso({
      Loop.StandardLoopCharacteristics.class,
      Loop.MultiInstanceLoopCharacteristics.class
   })
   static abstract class LoopCharacteristics
                   implements ILoopCharacteristics
   {}

   @XmlRootElement(name="LoopStandard", namespace=NS_XPDL)
   static class StandardLoopCharacteristics
          extends LoopCharacteristics
          implements IStandardLoopCharacteristics
   {
      static enum TestTime
      {
         Before, After
      }

      @XmlAttribute(name="TestTime")
      TestTime testTime;

      @XmlJavaTypeAdapter(InternedStringAdapter.class)
      @XmlElement(name="LoopCondition", namespace=NS_XPDL)
      String loopCondition;

      StandardLoopCharacteristics()
      {}

      StandardLoopCharacteristics(boolean before, String condition)
      {
         if (before)
         {
            testTime = TestTime.Before;
         }
         loopCondition = condition;
      }

      public boolean testBefore()
      {
         return testTime == TestTime.Before;
      }

      public String getLoopCondition()
      {
         return loopCondition;
      }

      @Override
      public String toString()
      {
         return "Standard loop " + (testBefore() ? "while " : "until ") + loopCondition;
      }
   }

   @XmlRootElement(name="LoopMultiInstance", namespace=NS_XPDL)
   static class MultiInstanceLoopCharacteristics extends LoopCharacteristics implements IMultiInstanceLoopCharacteristics
   {
      static enum Ordering
      {
         Sequential, Parallel
      }

      @XmlRootElement(name="LoopDataRef", namespace=NS_XPDL_EXT)
      static class LoopDataRef
      {
         @XmlElement(name="InputItemRef", namespace=NS_XPDL_EXT)
         String inputItem;

         @XmlElement(name="OutputItemRef", namespace=NS_XPDL_EXT)
         String outputItem;

         @XmlElement(name="LoopCounterRef", namespace=NS_XPDL_EXT)
         String loopCounter;
      }

      @XmlAttribute(name="MI_Ordering")
      Ordering ordering;

      @XmlAttribute(name="MI_FlowCondition")
      FlowCondition flowCondition;

      @XmlElementRef
      LoopDataRef loopDataRef;

      public boolean isSequential()
      {
         return ordering == Ordering.Sequential;
      }

      public FlowCondition getFlowCondition()
      {
         return flowCondition == null ? FlowCondition.All : flowCondition;
      }

      public String getInputParameterId()
      {
         return loopDataRef == null ? null : loopDataRef.inputItem;
      }

      public String getOutputParameterId()
      {
         return loopDataRef == null ? null : loopDataRef.outputItem;
      }

      public String getCounterParameterId()
      {
         return loopDataRef == null ? null : loopDataRef.loopCounter;
      }

      @Override
      public String toString()
      {
         return (isSequential() ? Ordering.Sequential : Ordering.Parallel) + " loop flow after "
               + getFlowCondition().toString().toLowerCase() + " (" + (loopDataRef == null ? " "
                     : "input to: '" + loopDataRef.inputItem
                     + "', output from: '" + loopDataRef.outputItem
                     + "', counter to: '" + loopDataRef.loopCounter + "'")
               + ")";
      }
   }

   static JAXBContext context;

   static JAXBContext getContext() throws JAXBException
   {
      if (context == null)
      {
         context = JAXBContext.newInstance(Loop.class);
      }
      return context;
   }

   static ILoopCharacteristics loadLoopCharacteristics(Element element, IConfigurationVariablesProvider confVariablesProvider)
         throws XMLStreamException, JAXBException
   {
      XMLStreamReader filter = NamespaceFilter.createXMLStreamReader(new DOMSource(element),
            XMLConstants.NS_XPDL_2_1, XMLConstants.NS_XPDL_1_0, XMLConstants.NS_XPDL_2_0);
      if (confVariablesProvider != null)
      {
         filter = ConfigurationVariablesFilter.createXMLStreamReader(confVariablesProvider, filter);
      }

      JAXBContext context = getContext();
      Unmarshaller um = context.createUnmarshaller();
      Loop loop = (Loop) um.unmarshal(filter);
      if (loop != null)
      {
         LoopType loopType = loop.loopType == null ? LoopType.Standard : loop.loopType;
         if (loopType.accept(loop.loopCharacteristics))
         {
            return loop.loopCharacteristics;
         }
      }
      return null;
   }

   static ILoopCharacteristics createStandardLoopCharacteristics(boolean before, String condition)
   {
      return new StandardLoopCharacteristics(before, condition);
   }
}
