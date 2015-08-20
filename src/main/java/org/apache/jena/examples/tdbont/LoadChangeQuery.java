package org.apache.jena.examples.tdbont;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.*;

/**
 * Created by eschwert on 20.08.15.
 */
public class LoadChangeQuery {

  public static void main(String[] args) {

    //TDB.getContext().set( TDB.symUnionDefaultGraph, true );
    //Location location = "target/tdb2" ;
    String directory = "target/tdb";
    Dataset dataset = TDBFactory.createDataset(directory);

    dataset.begin(ReadWrite.WRITE);

    try
    {
      Model model = dataset.getDefaultModel();
      // API calls to a model in the dataset

      Resource alice = model.createResource("http://example.com/alice");
      Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
      Resource bob = model.createResource("http://example.com/bob");

      //Individual aliceIndividual = model.createIndividual(alice);
      //Individual bobIndividiual = model.createIndividual(bob);
      //model.add(alice, knows, bob);

      model.add(model.createStatement(alice, knows, bob));

      // A SPARQL query will see the new statement added.
      try (
        QueryExecution qExec = QueryExecutionFactory.create(
          "SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10",
          dataset))
      {
        ResultSet rs = qExec.execSelect();
        ResultSetFormatter.out(rs);
      }

      try (
        QueryExecution qExec = QueryExecutionFactory.create(
                "SELECT ?s ?p ?o { ?s ?p ?o} LIMIT 100",
                dataset))
      {
        ResultSet rs = qExec.execSelect();
        ResultSetFormatter.out(rs);
      }

      // ... perform a SPARQL Update
      String sparqlUpdateString = StrUtils.strjoinNL(
        "PREFIX ex: <http://example.com/>",
        "INSERT { ex:s ex:p ?now } WHERE { BIND(now() AS ?now) }"

      );

      UpdateRequest request = UpdateFactory.create(sparqlUpdateString);
      UpdateProcessor proc = UpdateExecutionFactory.create(request, dataset);
      proc.execute();

      // Finally, commit the transaction.
      dataset.commit();
      // Or call .abort()
    }
    finally
    {
      dataset.end();
    }
  }

}
