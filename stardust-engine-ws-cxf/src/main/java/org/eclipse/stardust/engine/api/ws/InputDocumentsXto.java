
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für InputDocuments complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="InputDocuments">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="inputDocument" type="{http://eclipse.org/stardust/ws/v2012a/api}InputDocument" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InputDocuments", propOrder = {
    "inputDocument"
})
public class InputDocumentsXto {

    protected List<InputDocumentXto> inputDocument;

    /**
     * Gets the value of the inputDocument property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inputDocument property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInputDocument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InputDocumentXto }
     * 
     * 
     */
    public List<InputDocumentXto> getInputDocument() {
        if (inputDocument == null) {
            inputDocument = new ArrayList<InputDocumentXto>();
        }
        return this.inputDocument;
    }

}
