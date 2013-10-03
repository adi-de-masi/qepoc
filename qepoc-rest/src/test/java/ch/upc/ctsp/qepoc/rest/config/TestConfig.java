/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import ch.upc.ctsp.qepoc.rest.config.RuleCollection.Reference;
import ch.upc.ctsp.qepoc.rest.impl.QueryBuilder;

/**
 * TODO: add type comment.
 * 
 */
public class TestConfig {
    @Test
    public void testGenerateConfigFile() throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(RuleSet.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();

        final RuleSet rootSet = new RuleSet();
        rootSet.setPath("hello");
        final RuleSet innerSet = new RuleSet();
        innerSet.setPath("modem/{mac}");
        final RuleCollection.Attribute attr = new RuleCollection.Attribute();
        attr.setName("hello");
        attr.setReference("world");
        innerSet.getAttribute().add(attr);
        final Reference reference = new Reference();
        reference.setName("refname");
        reference.setReference("ref");
        innerSet.getReference().add(reference);
        rootSet.getRuleSet().add(innerSet);

        marshaller.marshal(new ObjectFactory().createRuleSet(rootSet), new File("target/out.xml"));
    }

    @Test
    public void testLoadOfConfig() throws JAXBException {

        final QueryBuilder builder = new QueryBuilder();
        builder.appendRuleSet(ClassLoader.getSystemResourceAsStream("rules.xml"));
        builder.build();
    }
}
