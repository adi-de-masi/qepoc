/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * TODO: add type comment.
 * 
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        namespace = "http://www.upc-cablecom.ch/query-engine/config")
public class Reference {
    @XmlAttribute(
            required = true)
    private String name;
    @XmlAttribute(
            required = true)
    private String reference;
    @XmlAttribute
    private Scope  scope = Scope.PRIVATE;
}
