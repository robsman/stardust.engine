
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PredicateBase complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PredicateBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PredicateBase")
@XmlSeeAlso({
    PerformingParticipantFilterXto.class,
    UserStateFilterXto.class,
    PerformedByUserFilterXto.class,
    HavingDocumentFilterXto.class,
    ProcessStateFilterXto.class,
    ProcessInstanceLinkFilterXto.class,
    ProcessInstanceFilterXto.class,
    ParticipantAssociationFilterXto.class,
    ProcessDefinitionFilterXto.class,
    PerformingOnBehalfOfFilterXto.class,
    ActivityInstanceFilterXto.class,
    PerformingUserFilterXto.class,
    ActivityDefinitionFilterXto.class,
    StartingUserFilterXto.class,
    ProcessInstanceHierarchyFilterXto.class,
    ActivityStateFilterXto.class,
    UnaryPredicateXto.class,
    BinaryPredicateBaseXto.class,
    PredicateTermXto.class
})
public abstract class PredicateBaseXto {


}
