package org.apache.jena.examples.tdbont;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.Filter;
import org.apache.jena.vocabulary.OWL;

import java.io.InputStream;
import java.util.function.Predicate;

/**
 * Created by eschwert on 10.11.15.
 */
public class PathFinder {

    static  String NS = "http://example.com/test#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    static OntModel m_model;

    static OntClass m_a;
    static OntClass m_b;
    static OntClass m_c;
    static OntClass m_d;
    static OntClass m_e;
    static OntClass m_f;
    static OntClass m_g;
    static OntClass m_top;

    static final Filter<Statement> ANY = Filter.any();


    public static void main(String[] args) {
        FindJenaDistance();
        //testShortestPath1();

    }

    public static void testShortestPath1() {

        m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m_a = m_model.createClass( NS + "A" );
        m_b = m_model.createClass( NS + "B" );
        m_c = m_model.createClass( NS + "C" );
//        m_d = m_model.createClass( NS + "D" );
//        m_e = m_model.createClass( NS + "E" );
//        m_f = m_model.createClass( NS + "F" );
//        m_g = m_model.createClass( NS + "G" );
        m_top = m_model.createClass( OWL.Thing.getURI() );

        Property p = m_model.createProperty( NS + "p" );
        m_a.addProperty( p, m_b );
        m_b.addProperty( p, m_c );

        OntTools.Path path = OntTools.findShortestPath( m_model, m_a, m_c, ANY );

        RDFDataMgr.write(System.out, m_model, Lang.RDFXML) ;

        System.out.println("Path: " + path.toString());
    }

    public static void FindJenaDistance() {

        // Jena implementation

        long startTime = System.currentTimeMillis();

        // this file needs to be created by doing "Save As.." and "RDF/XML" for a 'normal' OWL file. Otherwise we get Jena parse errors
        String inputFileName = "src/main/resources/siemens_healthcare_protege.rdf";
        String inputFileName2 = "src/main/resources/pizza.owl";

        String ns = "http://www.atos.org/ontologies/2015/8/moscito#";

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        InputStream in = FileManager.get().open(inputFileName);
        model.read(in, "RDF/XML");

        //RDFDataMgr.write(System.out, model, Lang.RDFXML) ;

        System.out.format("Ontology load time: (%7.2f sec)%n%n", (System.currentTimeMillis() - startTime) / 1000.0);

        Individual fromSubClass = model.getIndividual("http://www.atos.org/ontologies/2015/8/moscito#_1788964");
        Individual toSuperClass = model.getIndividual("http://www.atos.org/ontologies/2015/8/moscito#OSX-01223970");

//        OntClass fromSubClass = model.getOntClass("http://www.atos.org/ontologies/2015/8/moscito#Contract");
//        OntClass toSuperClass = model.getOntClass("http://www.atos.org/ontologies/2015/8/moscito#OperatingSystem");

//        OntClass fromSubClass = model.getOntClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#American");
//        OntClass toSuperClass = model.getOntClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#Country");

//        Predicate<Statement> onPath = new Predicate<Statement>() {
//            @Override
//            public boolean test(Statement statement) {
//                return false;
//            }
//        };

        OntTools.Path path = OntTools.findShortestPath(model, fromSubClass, toSuperClass,  Filter.any());

        if (path != null){
            int superClasses = 0;
            for (Statement s: path) {
                if (s.getObject().toString().startsWith(ns)) {
                    // filter out OWL Classes
                    superClasses++;
                    System.out.println(s.getObject());
                }
            }
            System.out.println("Shortest distance from " + fromSubClass + " to " + toSuperClass + " = " + superClasses);
        }else if (fromSubClass == toSuperClass){
            System.out.println("Same node");
        }else {
            System.out.println("No path from " + fromSubClass + " to " + toSuperClass);
        }

        System.out.format("\nProcessing time: (%7.2f sec)%n%n", (System.currentTimeMillis() - startTime) / 1000.0);

    }
}
