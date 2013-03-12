
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        Contains all the bind actions registered on the event handler.
 * 	        
 * 
 * <p>Java class for BindActionDefinitions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BindActionDefinitions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bindAction" type="{http://eclipse.org/stardust/ws/v2012a/api}EventActionDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BindActionDefinitions", propOrder = {
    "bindAction"
})
public class BindActionDefinitionsXto {

    protected List<EventActionDefinitionXto> bindAction;

    /**
     * Gets the value of the bindAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bindAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBindAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventActionDefinitionXto }
     * 
     * 
     */
    public List<EventActionDefinitionXto> getBindAction() {
        if (bindAction == null) {
            bindAction = new ArrayList<EventActionDefinitionXto>();
        }
        return this.bindAction;
    }

}
