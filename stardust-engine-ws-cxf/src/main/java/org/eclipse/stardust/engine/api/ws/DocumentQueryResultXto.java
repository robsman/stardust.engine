
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an DocumentQuery execution.
 * 			
 * 
 * <p>Java class for DocumentQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DocumentQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="documents" type="{http://eclipse.org/stardust/ws/v2012a/api}Documents" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentQueryResult", propOrder = {
    "documents"
})
public class DocumentQueryResultXto
    extends QueryResultXto
{

    protected DocumentsXto documents;

    /**
     * Gets the value of the documents property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentsXto }
     *     
     */
    public DocumentsXto getDocuments() {
        return documents;
    }

    /**
     * Sets the value of the documents property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentsXto }
     *     
     */
    public void setDocuments(DocumentsXto value) {
        this.documents = value;
    }

}
