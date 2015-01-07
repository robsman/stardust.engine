
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessInstanceDetailsOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceDetailsOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processInstanceDetailsOption" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsOption" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceDetailsOptions", propOrder = {
    "processInstanceDetailsOption"
})
public class ProcessInstanceDetailsOptionsXto {

    protected List<ProcessInstanceDetailsOptionXto> processInstanceDetailsOption;

    /**
     * Gets the value of the processInstanceDetailsOption property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processInstanceDetailsOption property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessInstanceDetailsOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessInstanceDetailsOptionXto }
     * 
     * 
     */
    public List<ProcessInstanceDetailsOptionXto> getProcessInstanceDetailsOption() {
        if (processInstanceDetailsOption == null) {
            processInstanceDetailsOption = new ArrayList<ProcessInstanceDetailsOptionXto>();
        }
        return this.processInstanceDetailsOption;
    }

}
