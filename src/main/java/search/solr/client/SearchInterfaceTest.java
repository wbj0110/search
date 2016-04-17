package search.solr.client;

import search.solr.client.entity.searchinterface.FilterAttributeSearchResult;
import search.solr.client.searchInterface.SearchInterface;

/**
 * Created by soledede on 2016/2/22.
 */
public class SearchInterfaceTest {

    public static void main(String[] args) {
        testSearchByKeywords();
    }



    public static void testSearchByKeywords() {
        FilterAttributeSearchResult re =   SearchInterface.searchByKeywords("mergescloud", "screencloud", "博世", 363, null, null, 0, 10);
        re =   SearchInterface.searchByKeywords("mergescloud", "screencloud", "螺丝刀", null, null, null, 0, 10);
        System.out.println(re);
    }

    public void testSearch() {
        // SearchInterface.searchByKeywords()
    }


}
