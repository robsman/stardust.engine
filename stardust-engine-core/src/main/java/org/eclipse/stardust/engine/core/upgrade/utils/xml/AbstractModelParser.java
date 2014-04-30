package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.io.StringReader;
import java.lang.reflect.ParameterizedType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.eclipse.stardust.engine.core.model.parser.info.IModelInfo;

public abstract class AbstractModelParser<T extends IModelInfo>
{
   protected JAXBContext instance;

   public T getIModelInfo(String xml) throws SAXException, JAXBException
   {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      SAXSource source = new SAXSource(xmlReader, new InputSource(new StringReader(xml)));
      Class<T> parameterClass = getParameterClass();

      JAXBContext context = JAXBContext
            .newInstance(parameterClass);
      Unmarshaller um = context.createUnmarshaller();
      return (T) um.unmarshal(source);
   }

   protected abstract String[] getStopConditions();

   public Class<T> getParameterClass()
   {
      return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];

   }
}
