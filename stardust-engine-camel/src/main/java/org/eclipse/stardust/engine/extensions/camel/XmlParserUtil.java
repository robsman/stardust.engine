package org.eclipse.stardust.engine.extensions.camel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Contains utility methods used to parse a spring xml file.
 * 
 * 
 */
public class XmlParserUtil
{
   private Document document;
   
   /**
    * <p>
    * This builds a document from the supplied input stream.
    * </p>
    *
    * @param in <code>inputFile</code> to read from
    * @throws JDOMException when errors occur in parsing
    * @throws IOException when an I/O error prevents a document from being fully parsed.
    */
public XmlParserUtil(InputStream inputFile) throws JDOMException, IOException
   {
      SAXBuilder builder = new SAXBuilder();
      document = builder.build(inputFile);
   }

	/**
	 * This will return the root <code>Element</code>
	 * for this <code>Document</code>
	 *
	 * @return <code>Element</code> - the document's root element
	 */
   public Element getRootElement()
   {
      return document.getRootElement();
   }

   /**
    * This returns a <code>List</code> of all the child elements
    * nested directly (one level deep) within "bean" element with the given
    * local name and belonging to the given root Namespace, returned as
    * <code>Element</code> objects.  If this target element has no nested
    * elements with the "bean" name in the given root Namespace, an empty List
    * is returned.  The returned list is "live" in document order
    * and changes to it affect the element's actual contents.
    *
    * @return all matching child elements
    */
   @SuppressWarnings("unchecked")
   public List<Element> getBeanDefinition()
   {
      Element root = getRootElement();
      return root.getChildren("bean", root.getNamespace());
   }


   /**
    * Return true if the bean is already defined in the spring application Context
    * 
    * @param beanName
    * @param applicationContext
    * @return whether a bean with the given name is defined in the application context
    */
   public boolean alreadyDefined(String beanName, GenericApplicationContext applicationContext)
   {
      return applicationContext.containsBean(beanName);
   }
}
