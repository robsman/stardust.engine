
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Contains only important core information of a dynamic participant that is needed to identify a DynamicParticipant.
 * 	        
 * 
 * <p>Java-Klasse für DynamicParticipantInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DynamicParticipantInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DynamicParticipantInfo", propOrder = {
    "oid"
})
@XmlSeeAlso({
    UserInfoXto.class,
    UserGroupInfoXto.class
})
public class DynamicParticipantInfoXto
    extends ParticipantInfoXto
{

    protected long oid;

    /**
     * Ruft den Wert der oid-Eigenschaft ab.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Legt den Wert der oid-Eigenschaft fest.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
    }

}
