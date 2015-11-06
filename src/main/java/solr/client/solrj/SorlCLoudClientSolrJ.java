package solr.client.solrj;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * Created by soledede on 2015/11/6.
 */
public class SorlCLoudClientSolrJ {
   //121.199.42.48
   static String zkHostString = "121.199.42.48:9983";
    static String queryString = "http://121.199.42.48:8983/solr/gettingstarted/select?q=sku:mwl*+original:mwl*^10+text:mwl&q.op=or&start=1&rows=20&wt=json";

    public static void main(String[] args) {
        search();
    }
    public static void search() {

        CloudSolrClient server = new CloudSolrClient(zkHostString);
        server.setDefaultCollection("testehsyfacet");
        // server.setParser(new XMLResponseParser());
        SolrQuery query = new SolrQuery();
        query.setQuery(queryString);
       // query.setRequestHandler("/select");

       // query.set("fl", "category,title,price");
        //query.setFields("category", "title", "price");
        query.set("qt", "/select");
        query.set("q", "*:*");
        query.setRows(5);
        query.setStart(0);

        try {
            QueryResponse response = server.query(query);
            SolrDocumentList list = response.getResults();
            for(SolrDocument d: list){
                System.out.println(d.getFieldNames().toString());
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
