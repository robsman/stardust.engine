package org.eclipse.stardust.engine.core.upgrade.utils.xml;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.stardust.engine.core.model.parser.info.IModelInfo;
import org.eclipse.stardust.engine.core.model.parser.info.ModelInfoRetriever;

public abstract class AbstractModelParser<T extends IModelInfo>
{
   protected JAXBContext instance;

   public T getIModelInfo(String xml) throws SAXException, JAXBException, IOException
   {
      InputSource inputSource = new InputSource(new StringReader(xml));
      SAXSource saxInputSource = ModelInfoRetriever.getModelSource(inputSource);

      Class<T> parameterClass = getParameterClass();
      JAXBContext context = JAXBContext
            .newInstance(parameterClass);
      Unmarshaller um = context.createUnmarshaller();
      return (T) um.unmarshal(saxInputSource);
   }

   protected abstract String[] getStopConditions();

   public Class<T> getParameterClass()
   {
      return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];

   }
}