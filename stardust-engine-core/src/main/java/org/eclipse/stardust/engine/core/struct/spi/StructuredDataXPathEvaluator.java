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
package org.eclipse.stardust.engine.core.struct.spi;

import java.io.StringReader;
import java.util.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueLoader;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathException;

/**
 * Evaluates in- and out- datamappings for structured data
 *
 * @version $Revision$
 */
public class StructuredDataXPathEvaluator implements ExtendedAccessPathEvaluator, Stateless
{
   private static final Logger trace = LogManager.getLogger(StructuredDataXPathEvaluator.class);

   public boolean isStateless()
   {
      return true;
   }

   /**
    * Evaluates an out data path by applying the outPath expression against the given
    * accessPoint and returning the resulting value.
    *
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPointInstance
    *           The actual access point.
    * @param outPath
    *           The dereference path to be applied.
    * @return The resulting value.
    */
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      if (null == accessPointInstance)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("returning null for outPath '" + outPath + "'");
         }
         return null;
      }

      final IXPathMap xPathMap = DataXPathMap.getXPathMap(accessPointDefinition);
      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
      Document document;

      if (accessPointDefinition instanceof IData)
      {
         // data value is in the audit trail
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         IData data = (IData) accessPointDefinition;
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         long dataRtOid = modelManager.getRuntimeOid(data);
         long scopeProcessInstanceOid = accessPathEvaluationContext.getScopeProcessInstanceOID();
         document = getDocument(accessPointInstance, session, dataRtOid, scopeProcessInstanceOid, xPathMap);
      }
      else
      {
         Node [] nodes = converter.toDom(accessPointInstance, "", true);
         Assert.condition(nodes.length == 1);
         document = new Document((Element)nodes[0]);
      }
      boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);

      AccessPoint targetAccessPointDefinition = accessPathEvaluationContext
            .getTargetAccessPointDefinition();
      boolean vizRulesApplication = StructDataMappingUtils
            .isVizRulesApplication(accessPathEvaluationContext.getActivity());
      boolean needsToBeJaxbTransformed = vizRulesApplication
            && null != targetAccessPointDefinition
            && (StructuredDataXPathUtils.returnsSingleComplex(outPath, xPathMap)
                  /*|| StructuredDataXPathUtils.returnsListOfComplex(outPath, xPathMap)*/);
      if (needsToBeJaxbTransformed)
      {
         targetAccessPointDefinition.setAttribute(
               StructuredDataConstants.TRANSFORMATION_ATT,
               StructDataTransformerKey.BEAN);
      }

      StructuredDataTransformation transformation = StructuredDataTransformation.valueOf(
            outPath, targetAccessPointDefinition);
      Object returnValue;
      if (transformation.needsTransformation())
      {
         IStructuredDataTransformer transformer = getTransformator(transformation);

         List<Node> nodelist = converter.evaluateXPath(document.getRootElement(),
               transformation.getXPath(), namespaceAware);
         if (nodelist.size() == 0)
         {
            returnValue = null;
         }
         else if (nodelist.size() == 1)
         {
            Element evaluatedElement = (Element) nodelist.get(0);
            if (evaluatedElement != document.getRootElement())
            {
               evaluatedElement.detach();
               document = new Document(evaluatedElement);
            }

            returnValue = transformer.fromStructData(document,
                  accessPathEvaluationContext);
         }
         else
         {
            if (transformation.isToBean())
            {
               /*
               int idx = 0;
               Object[] array = new Object[nodelist.size()];

               for (Iterator iter = nodelist.iterator(); iter.hasNext();)
               {
                  Element evaluatedElement = (Element) iter.next();
                  Document doc = document;
                  if (evaluatedElement != document.getRootElement())
                  {
                     evaluatedElement.detach();
                     doc = new Document(evaluatedElement);
                  }
                  array[idx] = transformer.fromStructData(doc, accessPathEvaluationContext);
               }

               returnValue = array; */
               throw new RuntimeException(
                     "Only zero or one node can be transformed to BEAN");
            }
            else
            {
               throw new RuntimeException(
                     "Only zero or one node can be transformed to DOM");
            }
         }
      }
      else
      {
         // collection
         returnValue = converter.toCollection(document.getRootElement(), transformation.getXPath(),
               namespaceAware);
      }

      if (trace.isDebugEnabled())
      {
         if (null == returnValue)
         {
            trace.debug("returning null for outPath '" + outPath + "'");
         }
         else
         {
            trace.debug("returning returnValue of type '"
                  + returnValue.getClass().getName() + "' for outPath '" + outPath
                  + "'");
         }
      }

      return returnValue;
   }

   public static IStructuredDataTransformer getTransformator(
         StructuredDataTransformation transformation)
   {
      List<IStructuredDataTransformer.Factory> extensionProviders = ExtensionProviderUtils
            .getExtensionProviders(IStructuredDataTransformer.Factory.class);
      if (extensionProviders.isEmpty())
      {
         throw new RuntimeException("No transformer factory for struct data found.");
      }

      StructDataTransformerKey transformationType = transformation.getType();
      IStructuredDataTransformer transformator = null;
      for (int i = 0; i < extensionProviders.size(); i++ )
      {
         IStructuredDataTransformer.Factory factory = extensionProviders.get(i);
         transformator = factory.getTransformer(transformationType);
         if (null != transformator)
         {
            break;
         }
      }
      if (null == transformator)
      {
         throw new RuntimeException("No transformer for transformation \'"
               + transformationType.toString() + "\' found.");
      }
      return transformator;
   }

   private org.eclipse.stardust.engine.core.struct.sxml.Document getDocument(Object accessPointInstance, Session session, long dataRtOid, long scopeProcessInstanceOid, IXPathMap xPathMap)
   {
      final Long hugeStringOid = (Long) accessPointInstance;

      ClobDataBean hugeString = (ClobDataBean) session.findByOID(ClobDataBean.class, hugeStringOid.longValue());

      try
      {
         if (null == hugeString)
         {
            if (trace.isInfoEnabled())
            {
               // old data w/o CLOB
               // support old data do not have CLOB in the DB:
               trace.info("data value of data OID '"
                     + dataRtOid
                     + "' for scopeProcessInstanceOid '"
                     + scopeProcessInstanceOid
                     + "' contains no CLOB. assuming this is an old document. will read entries from structured data value table");
            }

            Set /*<IStructuredDataValue>*/ entries = this.getIndexes(scopeProcessInstanceOid, dataRtOid, session, xPathMap);
            StructuredDataReader structuredDataReader = new StructuredDataReader(xPathMap);

            return structuredDataReader.read(entries);
         }
         else
         {
            Document document = null;

            if (hugeString.getStringValueProvider() instanceof XmlDocumentHolder)
            {
               document = ((XmlDocumentHolder) hugeString.getStringValueProvider()).xmlDoc;
            }

            if (null == document)
            {
               document = DocumentBuilder.buildDocument(new StringReader(hugeString.getStringValue()));

               hugeString.setStringValueProvider(new XmlDocumentHolder(document), false);
            }

            return document;
         }
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Evaluates an in data path by applying the inPath expression parametrized with the
    * given value against the given accessPoint and returns the result, if appropriate.
    *
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPointInstance
    *           The actual access point.
    * @param inPath
    *           The dereference path to be used when applying the given value.
    * @param value
    *           The new value to be applied to the access point.
    * @return The new access point.
    */
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext,
         Object value)
   {
	  if (inPath != null)
	  {
		 if (inPath.startsWith("DOM()"))
		 {
			inPath = inPath.substring("DOM()".length());
		 }
	  }
      if (accessPointDefinition instanceof IData)
      {
         // data value is in the audit trail
         final ModelManager modelManager = ModelManagerFactory.getCurrent();
         final IData data = (IData) accessPointDefinition;
         final long dataRtOid = modelManager.getRuntimeOid(data);
         final IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
         final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         IProcessInstance scopeProcessInstance
            = accessPathEvaluationContext.getProcessInstance().getScopeProcessInstance();
         long scopeProcessInstanceOid = scopeProcessInstance.getScopeProcessInstanceOID();


         Document document;
         if (null == accessPointInstance)
         {
            // data value is being set for the first time, create it first
            if (value instanceof Long && StringUtils.isEmpty(inPath))
            {
               // we have received the oid of an existing data, make a duplicate
               document = getDocument(value, session, dataRtOid, scopeProcessInstanceOid, xPathMap);
            }
            else
            {
               document = new Document(StructuredDataXPathUtils.createElement(xPathMap.getRootXPath(), true));
            }
         }
         else
         {
            try
            {
               document = getDocument(accessPointInstance, session, dataRtOid, scopeProcessInstanceOid, xPathMap);
            }
            catch (Exception e)
            {
               throw new InternalException(e);
            }
         }
         boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);

         if (value instanceof Long && StringUtils.isEmpty(inPath))
         {
            // document is a copy of an existing one
            if (trace.isDebugEnabled())
            {
               trace.debug("document: " + document.toXML());
            }
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("document before change: " + document.toXML());
            }

            StructuredDataXPathUtils.putValue(document, xPathMap, inPath, value,
                  namespaceAware, accessPathEvaluationContext.isIgnoreUnknownValueParts());

            if (trace.isDebugEnabled())
            {
               trace.debug("document after change: " + document.toXML());
            }
         }

         // TODO (ab) what about combination indexed+non-persistent - this should be forbidden?

         // iterate through all XPaths to primitive fields, that are indexed
         // 1. for every field, ensure, that the entry for the value and other necessary
         // entries are in newEntries
         // 2. reuse entries from oldEntries
         // 3. perform update for oldEntries that have to be changed

         // oldEntries will be lazily retrieved only if index updates are required
         Set<IStructuredDataValue> oldEntries = null;
         Set<IStructuredDataValue> newEntries = null;
         Set<IStructuredDataValue> oldEntries_ = null;
         Set<IStructuredDataValue> newEntries_ = null;
         boolean indexesChanged = false;
         for (Iterator i = xPathMap.getAllXPaths().iterator(); i.hasNext();)
         {
            TypedXPath typedXPath = (TypedXPath) i.next();
            // check only for fields of non-complex type (primitive and enums)
            if (typedXPath.getAnnotations().isIndexed()
                  && typedXPath.getType() != BigData.NULL)
            {
               try
               {
                  List nodelist = (List) typedXPath.getCompiledXPath(namespaceAware).selectNodes(document.getRootElement());
                  for (Iterator n = nodelist.iterator(); n.hasNext();)
                  {
                     Node node = (Node) n.next();
                     String stringValue = StructuredDataXPathUtils.findNodeValue(node);
                     if (stringValue != null)
                     {
                        // lazy retrieval of oldEntries
                        if (null == oldEntries)
                        {
                           oldEntries = this.getIndexes(scopeProcessInstanceOid, dataRtOid,
                                 session, xPathMap);
                        }

                        if (null == newEntries)
                        {
                           newEntries = CollectionUtils.newHashSet();
                        }
                        indexesChanged |= setIndexValue(oldEntries, newEntries, node, typedXPath, xPathMap,
                              stringValue, scopeProcessInstance);
                     }
                     else
                     {
                        // lazy retrieval of oldEntries
                        if (null == oldEntries_)
                        {
                           oldEntries_ = this.getIndexes(scopeProcessInstanceOid, dataRtOid,
                                 session, xPathMap);
                        }

                        if (null == newEntries_)
                        {
                           newEntries_ = CollectionUtils.newHashSet();
                        }

                        /*indexesChanged |=*/ setIndexValue(oldEntries_, newEntries_, node, typedXPath, xPathMap,
                              stringValue, scopeProcessInstance);
                     }
                  }
               }
               catch (XPathException e)
               {
                  throw new InternalException(e);
               }
            }
         }

         // in this case we must remove the old entries
         if (null == oldEntries &&
               (value == null ||
                     value instanceof Map && ((Map) value).size() == 0 ||
                     value instanceof Map && ((Map) value).size() > 0 && oldEntries_ != null))
         {
            oldEntries = this.getIndexes(scopeProcessInstanceOid, dataRtOid,
                  session, xPathMap);
         }

         if (indexesChanged || (oldEntries != null && !oldEntries.isEmpty()))
         {
            // if some indexes changed or some must be deleted,
            // mark data value as modified in order to trigger a data cluster update
            ProcessInstanceBean processInstance = (ProcessInstanceBean) accessPathEvaluationContext.getProcessInstance();
            DataValueBean dataValue = (DataValueBean) processInstance.getCachedDataValue(data.getId());
            if (null != dataValue && !dataValue.getPersistenceController().isModified()
                  && !dataValue.getPersistenceController().isCreated())
            {
               dataValue.getPersistenceController().markModified();
            }
         }

         // delete entries remained in oldEntries
         if (oldEntries != null && !oldEntries.isEmpty())
         {
            List oldEntriesList = CollectionUtils.newList(oldEntries);
            deleteIndexesFromCache(scopeProcessInstanceOid, xPathMap, oldEntriesList);
            for (int i = 0; i < oldEntriesList.size(); ++i)
            {
               StructuredDataValueBean sdv = (StructuredDataValueBean) oldEntriesList.get(i);
               sdv.delete();
            }
         }

         if (accessPointInstance == null)
         {
            long dataValue = setDocument(document, session);
            accessPointInstance = Long.valueOf(dataValue);
         }
         else
         {
            accessPointInstance = updateDocument(document, accessPointInstance, session);
         }

         return accessPointInstance;
      }
      else
      {
         // data value is in accessPointInstance
         final IXPathMap xPathMap = DataXPathMap.getXPathMap(accessPointDefinition);

         // always operate with namespaceAware=true
         final boolean namespaceAware = true;

         StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
         Document document;
         if (accessPointInstance == null)
         {
            // data value is being set for the first time, create it first
            document = new Document(StructuredDataXPathUtils.createElement(xPathMap.getRootXPath(), namespaceAware));
         }
         else
         {
            Node [] nodes = converter.toDom(accessPointInstance, "", namespaceAware);
            Assert.condition(nodes.length == 1);
            document = new Document((Element)nodes[0]);
         }

         if (trace.isDebugEnabled())
         {
            trace.debug("document before change: " + document.toXML());
         }

         StructuredDataXPathUtils.putValue(document, xPathMap, inPath, value, namespaceAware,
               accessPathEvaluationContext.isIgnoreUnknownValueParts());

         if (trace.isDebugEnabled())
         {
            trace.debug("document after change: " + document.toXML());
         }

         // can be casted to map since XPath is empty (root XPath)
         Map newAccessPointInstance = (Map) converter.toCollection(document.getRootElement(), "", namespaceAware);
         if (accessPointInstance == null)
         {
            accessPointInstance = newAccessPointInstance;
         }
         else if (accessPointInstance instanceof Map)
         {
            // update accessPointInstance (do not just return another instance, try "refilling"
            // the existing one
            ((Map)accessPointInstance).clear();
            ((Map)accessPointInstance).putAll(newAccessPointInstance);
         }
         else
         {
            throw new InternalException("Unexpected accessPointInstance class: '"+accessPointInstance.getClass().getName()+"'");
         }

         return accessPointInstance;
      }
   }

   private boolean setIndexValue(Set /*<StructuredDataValueBean>*/ oldEntries, Set /*<StructuredDataValueBean>*/ newEntries,
         Node node, TypedXPath typedXPath, IXPathMap xPathMap, String stringValue, IProcessInstance scopeProcessInstance)
   {
      boolean modified = false;
      // find out the indexed XPath of the node
      String indexedXPath = StructuredDataXPathUtils.getIndexedNodeXPath(node, node.getDocument().getRootElement());

      // traverse the XPath from top to bottom and find/create SDVs for its parts
      StringTokenizer xPathParts = StructuredDataXPathUtils.getXPathPartTokenizer(indexedXPath);
      String currentXPathWithoutIndexes = "";
      StructuredDataValueBean parentEntry = findRootEntry(oldEntries, newEntries, scopeProcessInstance, xPathMap);
      IStructuredDataValueFactory structuredDataValueFactory = new StructuredDataValueFactory();

      while (xPathParts.hasMoreTokens())
      {
         String xPathPart = xPathParts.nextToken();
         if (currentXPathWithoutIndexes.length() > 0)
         {
            currentXPathWithoutIndexes += "/";
         }
         currentXPathWithoutIndexes += StructuredDataXPathUtils.getXPathPartNode(xPathPart);
         String index = StructuredDataXPathUtils.getXPathPartIndex(xPathPart);
         Long currentXPathOid = xPathMap.getXPathOID(currentXPathWithoutIndexes);
         TypedXPath currentXPath = xPathMap.getXPath(currentXPathWithoutIndexes);
         if (currentXPathOid == null)
         {
            throw new InternalException("Invalid XPath '"+indexedXPath+"'");
         }
         StructuredDataValueBean entry = findEntry(oldEntries, parentEntry, currentXPathOid.longValue(), index);
         if (entry == null)
         {
            entry = findEntry(newEntries, parentEntry, currentXPathOid.longValue(), index);
         }
         else
         {
            // move from oldEntries to newEntries
            if ( !newEntries.contains(entry))
            {
               oldEntries.remove(entry);
               newEntries.add(entry);
            }
         }
         if (entry == null)
         {
            // create a new SDV
            if (currentXPath.getXPath().equals(typedXPath.getXPath()))
            {
               // terminal node
               if (StructuredDataXPathUtils.isRootXPath(currentXPath.getXPath()))
               {
                  // special case: enumeration is a top-level element
                  // set the value directly in the root node
                  Object oldValue = parentEntry.getValue();
                  parentEntry.setValue(stringValue, false, false);
                  modified |= !CompareHelper.areEqual(oldValue, stringValue);
               }
               else
               {
                  entry = (StructuredDataValueBean) structuredDataValueFactory.createKeyedElementEntry(parentEntry.getProcessInstance(), parentEntry.getOID(),
                     currentXPathOid.longValue(), index, stringValue, typedXPath.getType());
                  newEntries.add(entry);
                  modified = true;
               }
            }
            else
            {
               // intermediate node
               entry = (StructuredDataValueBean) structuredDataValueFactory.createKeyedElementEntry(parentEntry.getProcessInstance(), parentEntry.getOID(),
                     currentXPathOid.longValue(), index, null, currentXPath.getType());
               newEntries.add(entry);
               modified = true;
            }
         }
         else
         {
            if (currentXPath.getXPath().equals(typedXPath.getXPath()))
            {
               // terminal node
               // set value of an existing entry
               Object objectValue = StructuredDataValueFactory.convertTo(typedXPath.getType(), stringValue);
               Object oldValue = entry.getValue();
               entry.setValue(objectValue, false, false);
               modified |= !CompareHelper.areEqual(oldValue, stringValue);
            }
            // otherwise do nothing, the intermediate entry already has the right index
         }
         parentEntry = entry;
      }

      return modified;
   }

   private StructuredDataValueBean findEntry(Collection /*<StructuredDataValueBean>*/ entries,
         StructuredDataValueBean parentEntry, long xPathOid, String index)
   {
      for (Iterator i = entries.iterator(); i.hasNext(); )
      {
         StructuredDataValueBean entry = (StructuredDataValueBean)i.next();
         if (entry.getParentOID() == parentEntry.getOID() && entry.getXPathOID() == xPathOid &&
               (StringUtils.isEmpty(index) && StringUtils.isEmpty(entry.getEntryKey()) || (index != null && index.equals(entry.getEntryKey()))))
         {
            // found, nothing else to do
            return entry;
         }
      }
      // not found
      return null;
   }

   private StructuredDataValueBean findRootEntry(Collection /*<StructuredDataValueBean>*/ oldEntries,
         Set /*<StructuredDataValueBean>*/ newEntries, IProcessInstance scopeProcessInstance, IXPathMap xPathMap)
   {
      // first search in new entries
      for (Iterator i = newEntries.iterator(); i.hasNext(); )
      {
         StructuredDataValueBean entry = (StructuredDataValueBean)i.next();
         if (entry.getParentOID() == IStructuredDataValue.NO_PARENT)
         {
            // found, nothing else to do
            return entry;
         }
      }

      // search in old entries
      for (Iterator i = oldEntries.iterator(); i.hasNext(); )
      {
         StructuredDataValueBean entry = (StructuredDataValueBean)i.next();
         if (entry.getParentOID() == IStructuredDataValue.NO_PARENT)
         {
            // move rootEntry to newEntries
            oldEntries.remove(entry);
            newEntries.add(entry);
            return entry;
         }
      }

      // no root entry exists, create it
      StructuredDataValueFactory structuredDataValueFactory = new StructuredDataValueFactory();
      StructuredDataValueBean rootEntry = (StructuredDataValueBean) structuredDataValueFactory.createRootElementEntry(scopeProcessInstance, xPathMap.getRootXPathOID().longValue(), "0", null);
      newEntries.add(rootEntry);
      return rootEntry;
   }

   private Object updateDocument(Document document, Object accessPointInstance, Session session)
   {
      final Long hugeStringOid = (Long) accessPointInstance;

      ClobDataBean hugeString = (ClobDataBean) session.findByOID(
            ClobDataBean.class, hugeStringOid.longValue());

      if (hugeString == null)
      {
         // old data w/o CLOB
         // support old data do not have CLOB in the DB:
         // create CLOBs and return the new accessPointInstance
         return Long.valueOf(setDocument(document, session));
      }
      else
      {
         // normal case: the CLOB is there, overwrite it
         // return unchanged accessPointInstance
         if ((null != hugeString.getStringValueProvider()))
         {
            ((XmlDocumentHolder) hugeString.getStringValueProvider()).xmlDoc = document;
            hugeString.markModified(ClobDataBean.FIELD__STRING_VALUE);
         }
         else
         {
            hugeString.setStringValueProvider(new XmlDocumentHolder(document), true);
         }
         return accessPointInstance;
      }
   }

   private long setDocument(Document document, Session session)
   {
      ClobDataBean hugeString = new ClobDataBean(0, DataValueBean.class, new XmlDocumentHolder(document));

      session.cluster(hugeString);

      return hugeString.getOID();
   }


   private void deleteIndexesFromCache(long processInstanceOid, IXPathMap xPathMap, List entriesToDelete)
   {
      PropertyLayer propertyLayer = (PropertyLayer) ParametersFacade.instance().get(PropertyLayerProviderInterceptor.PROPERTY_LAYER);
      if (propertyLayer == null)
      {
         return;
      }
      // contains sdv caches (piOid is the map key)
      Map /*<Long,Map>*/ sdvCacheByPiOid = (Map) propertyLayer.get(StructuredDataValueLoader.SDV_BY_PI_OID_CACHE);
      if (sdvCacheByPiOid == null)
      {
         return;
      }
      // contains lists of sdv (xPathOid is the map key)
      Map /*<Long,List>*/ sdvListByXPathOid = (Map) sdvCacheByPiOid.get(Long.valueOf(processInstanceOid));
      if (sdvListByXPathOid == null)
      {
         return;
      }

      // clear all sdv's for all xpaths of this data
      for (Iterator i = xPathMap.getAllXPaths().iterator(); i.hasNext(); )
      {
         TypedXPath typedXPath = (TypedXPath)i.next();

         List sdvList = (List) sdvListByXPathOid.get(xPathMap.getXPathOID(typedXPath.getXPath()));
         if (sdvList != null)
         {
            sdvList.removeAll(entriesToDelete);
            if (sdvList.isEmpty())
            {
               sdvListByXPathOid.remove(sdvList);
            }
         }
      }
      if (sdvListByXPathOid.isEmpty())
      {
         sdvCacheByPiOid.remove(Long.valueOf(processInstanceOid));
      }
   }

   private Set getIndexes(long processInstanceOid, long dataOid, Session session, IXPathMap xPathMap)
   {
      // check cache first (which will be populated from either a previous Audit Trail
      // query, or from previous INSERTs of new index values)
      Set /*<IStructuredDataValue>*/ entries = getIndexesFromCache(processInstanceOid, dataOid, xPathMap);

      if (entries != null)
      {
         // sdv beans were cached
         if (trace.isDebugEnabled())
         {
            trace.debug("retrieved structured data value for data <"+dataOid+"> from cache");
         }
         return entries;
      }

      entries = CollectionUtils.newHashSet();

      // only if the PI was loaded from the Audit Trail DB ...
      IProcessInstance scopePi = ProcessInstanceBean.findByOID(processInstanceOid);
      if ( !scopePi.getPersistenceController().isCreated())
      {
         // ... the DB needs to be scanned at least once for existing index entries
         // (otherwise all entries will have been found in the previous cache query)
      QueryExtension query = QueryExtension.where(Predicates.andTerm(
            Predicates.isEqual(StructuredDataValueBean.FR__PROCESS_INSTANCE, processInstanceOid),
            getXPathOidsTerm(new ArrayList(xPathMap.getAllXPathOids()))));

      ResultIterator entryIterator = session.getIterator(StructuredDataValueBean.class, query);
      while (entryIterator.hasNext())
      {
         StructuredDataValueBean entry = (StructuredDataValueBean) entryIterator.next();
         entries.add(entry);
      }
      entryIterator.close();
      }
      return entries;
   }

   private static PredicateTerm getXPathOidsTerm(List/*<Long>*/ oids)
   {
      final int chunkSize = 500;
      final FieldRef fieldRef = StructuredDataValueBean.FR__XPATH;
      final PredicateTerm piOidPredicate;

      final int listSize = oids.size();
      if (listSize <= chunkSize)
      {
         piOidPredicate = Predicates.inList(fieldRef, oids);
      }
      else
      {
         int fromIdx = 0;
         int toIdx = chunkSize;

         OrTerm orTerm = new OrTerm();

         do
         {
            List oidSubList = oids.subList(fromIdx, toIdx);
            orTerm.add(Predicates.inList(fieldRef, oidSubList));

            fromIdx = fromIdx + chunkSize;
            toIdx = listSize >= toIdx + chunkSize
                  ? toIdx + chunkSize
                  : listSize;
         }
         while (fromIdx < toIdx);

         piOidPredicate = orTerm;
      }
      return piOidPredicate;
   }


   private Set getIndexesFromCache(long processInstanceOid, long dataOid, IXPathMap xPathMap)
   {
      PropertyLayer propertyLayer = (PropertyLayer) ParametersFacade.instance().get(PropertyLayerProviderInterceptor.PROPERTY_LAYER);

      if (propertyLayer == null)
      {
         return null;
      }

      // contains sdv caches (piOid is the map key)
      Map /*<Long,Map>*/ sdvCacheByPiOid = (Map) propertyLayer.get(StructuredDataValueLoader.SDV_BY_PI_OID_CACHE);
      if (sdvCacheByPiOid == null)
      {
         return null;
      }
      // contains lists of sdv (xPathOid is the map key)
      Map /*<Long,List>*/ sdvListByXPathOid = (Map) sdvCacheByPiOid.get(Long.valueOf(processInstanceOid));
      if (sdvListByXPathOid == null)
      {
         return null;
      }

      // return all sdv's for all xpaths of this data
      Set /*<IStructuredDataValue>*/ entries = CollectionUtils.newHashSet();
      for (Iterator i = xPathMap.getAllXPaths().iterator(); i.hasNext(); )
      {
         TypedXPath typedXPath = (TypedXPath)i.next();
         List /*<IStructuredDataValue>*/ sdvList = (List) sdvListByXPathOid.get(xPathMap.getXPathOID(typedXPath.getXPath()));
         if (sdvList != null)
         {
            for (Iterator v = sdvList.iterator(); v.hasNext(); )
            {
               IStructuredDataValue sdv = (IStructuredDataValue)v.next();
               entries.add(sdv);
            }
         }
      }

      if (entries.size() == 0)
      {
         // this can be the case, when several structured datas can exist
         // for this processinstance
         // no entries means the data being searched for was not cached
         // the cached entries belong to the other data
         return null;
      }

      return entries;
   }

   /**
    * Initialize structured data instance. All Maps and Lists are to be created
    */
   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }

   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }

   private static class XmlDocumentHolder implements ClobDataBean.StringValueProvider
   {
      Document xmlDoc;

      public XmlDocumentHolder(Document xmlDoc)
      {
         this.xmlDoc = xmlDoc;
      }

      public String getStringValue()
      {
         return (null != xmlDoc) ? xmlDoc.toXML() : null;
      }
   }
}