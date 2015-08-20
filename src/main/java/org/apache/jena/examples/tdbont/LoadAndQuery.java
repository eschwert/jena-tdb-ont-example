/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jena.examples.tdbont;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class LoadAndQuery {
    public static final String STORE = "target/tdb";

    public static final Boolean USE_TDB_ASSEMBLER = true;
    public static final String STORE_ASSEMBLER = "src/main/resources/tdb-assembler.ttl";

    public static final String UNION_GRAPH = "urn:x-arq:UnionGraph";
    public static final String DEFAULT_GRAPH = "urn:x-arq:DefaultGraph";

    public static void main(String[] args) {
        // the base dataset
        TDB.getContext().set( TDB.symUnionDefaultGraph, true );
        Dataset dataset;
        //Dataset datasetFromAssembler = TDBFactory.assembleDataset(STORE_ASSEMBLER) ;

        OntModel model = null;
        if(USE_TDB_ASSEMBLER){
          dataset = TDBFactory.createDataset( STORE ) ;
          // now create a reasoning model using this base
          model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, dataset.getNamedModel( UNION_GRAPH ) );
        }else{
          dataset = TDBFactory.assembleDataset(STORE_ASSEMBLER) ;
          // now create a reasoning model using this base
          model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, dataset.getNamedModel( UNION_GRAPH ) );
        }

        dataset.begin(ReadWrite.WRITE) ;

        try {
//          //Model model = m ;
//          // API calls to a model in the dataset
//
//
//          Resource alice = model.createResource("http://example.com/alice");
//          Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
//          Resource bob = model.createResource("http://example.com/bob");
//
//          Individual aliceIndividual = model.createIndividual(alice);
//          Individual bobIndividiual = model.createIndividual(bob);
//          model.add(aliceIndividual, knows, bobIndividiual);
//
//          //model.add(model.createStatement(alice, Rex.hasOriginalText, bob));
//
//
          // A SPARQL query will see the new statement added.
          try (QueryExecution qExec = QueryExecutionFactory.create(
            "SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10",
            dataset)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
          }
//
          // ... perform a SPARQL Update

          String sparqlUpdateString = StrUtils.strjoinNL(
            "PREFIX : <http://example/>",
            "INSERT { :s :p ?now } WHERE { BIND(now() AS ?now) }"
          ) ;
////
          UpdateRequest request = UpdateFactory.create(sparqlUpdateString) ;
          UpdateProcessor proc = UpdateExecutionFactory.create(request, dataset) ;
          proc.execute() ;

          // Finally, commit the transaction.
          dataset.commit() ;
          // Or call .abort()
        } finally {
          dataset.end() ;
        }


        String query = MessageFormat.format("SELECT ?x  where '{' ?x <{0}> \"{1}\" '}'",
          Rex.hasOriginalText,
          "Fredrick Chopin");

        // Report results
        System.out.println(query);
        System.out.println("----");

        List<Resource> results = resourcesThatMatchQuery( query, "x", model );
        for (Resource r: results) {
            System.out.println( r );
        }

        System.out.println("----");
    }

    /**
     * Execute the given query against the given model, and return a list of the resources
     * which are the values of the bound variable <code>queryVar</code>
     *
     * @param queryString SPARQL query string
     * @param queryVar Result variable to extract
     * @param m Model to query
     * @return List of resources matching the query. Non-null, but may be empty
     */
    public static List<Resource> resourcesThatMatchQuery( String queryString, String queryVar, Model m ) {
        List<Resource> results = new ArrayList<Resource>();

        QueryExecution qexec = QueryExecutionFactory.create( queryString, m );
        try {
            ResultSet queryResults = qexec.execSelect();
            while (queryResults.hasNext()) {
                QuerySolution soln = queryResults.nextSolution();
                results.add( soln.getResource(queryVar) );
            }
        }
        finally {
            qexec.close();
        }
        return results;
    }


}
