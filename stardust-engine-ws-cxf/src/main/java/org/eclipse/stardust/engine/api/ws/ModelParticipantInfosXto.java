
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ModelParticipantInfos complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelParticipantInfos">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelparticipantInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}ModelParticipantInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelParticipantInfos", propOrder = {
    "modelparticipantInfo"
})
public class ModelParticipantInfosXto {

    protected List<ModelParticipantInfoXto> modelparticipantInfo;

    /**
     * Gets the value of the modelparticipantInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelparticipantInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelparticipantInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelParticipantInfoXto }
     * 
     * 
     */
    public List<ModelParticipantInfoXto> getModelparticipantInfo() {
        if (modelparticipantInfo == null) {
            modelparticipantInfo = new ArrayList<ModelParticipantInfoXto>();
        }
        return this.modelparticipantInfo;
    }

}
