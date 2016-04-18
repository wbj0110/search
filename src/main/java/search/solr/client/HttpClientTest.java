package search.solr.client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import search.solr.client.entity.searchinterface.parameter.IndexParameter;
import search.solr.client.entity.searchinterface.parameter.SearchRequestParameter;
import search.solr.client.http.HttpClientUtil;
import search.solr.client.searchInterface.SearchInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by soledede on 2016/3/2.
 */
public class HttpClientTest {

    public static void main(String[] args) {
      //  testHttp();
       // testSearchByKeywords();
        //testGetCategoryIdsByKeyWords();
        //testPopularityKeyWords();
        //testSuggestByKeyWords();
        //testAttributeFilterSearchCatId(true);
        //testAttributeFilterSearchCatId(false);
        //testAttributeFilterSearch();
        //index();
        deleteIds();
    }

    public static void deleteIds() {
        String url = "http://192.168.51.118:8999/search/delete/ids";

        IndexParameter obj = new IndexParameter();
        obj.setCollection("mergescloud");
        List<String> list = new ArrayList<String>();
        list.add("23");
        obj.setIds(list);
        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }


    public static void index() {
        String url = "http://192.168.51.118:8999/search/index";


        IndexParameter obj = new IndexParameter();
        obj.setCollection("mergescloud");
        obj.setStartUpdateTime(1900990435L);
        obj.setEndUpdataTime(2343242L);
        obj.setTotalNum(4);

        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }



    public static void testAttributeFilterSearch() {
        String url = "http://192.168.51.118:8999/search/filter/search";


        LinkedHashMap<java.lang.String, java.lang.String> sorts = new java.util.LinkedHashMap<java.lang.String, java.lang.String>();
        sorts.put("price", "asc");

        Map<java.lang.String, java.lang.String> filters = new java.util.HashMap<java.lang.String, java.lang.String>();
        filters.put("da_89_s", "TIME/时代");
        // filters.put("t87_tf", "[0 TO *}");

        LinkedHashMap<java.lang.String, java.util.List<java.lang.String>> filterFieldsValues = new java.util.LinkedHashMap<java.lang.String, java.util.List<java.lang.String>>();
        filterFieldsValues.put("da_89_s", null);
        filterFieldsValues.put("da_2955_s", null);
        List rangeList = new java.util.ArrayList<String>();
        rangeList.add("[* TO 0}");
        rangeList.add("[0 TO 10}");
        rangeList.add("[10 TO 20}");
        rangeList.add("[20 TO 30}");
        rangeList.add("[30 TO *}");
        // filterFieldsValues.put("t87_tf", rangeList);



        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setAttrCollection("screencloud");
        obj.setKeyWords("超声波");
        obj.setCatagoryId(3180);
        obj.setCityId(321);
        obj.setSorts(sorts);
        obj.setFilters(filters);
        obj.setFilterFieldsValues(filterFieldsValues);
        obj.setStart(0);
        obj.setRows(2);
        obj.setComeFromSearch(true);

        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }

    public static void testAttributeFilterSearchCatId(Boolean catTouch) {
        String url = "http://192.168.51.118:8999/search/filter/cataid";


        LinkedHashMap<java.lang.String, java.lang.String> sorts = new java.util.LinkedHashMap<java.lang.String, java.lang.String>();
        sorts.put("price", "asc");

        Map<java.lang.String, java.lang.String> filters = new java.util.HashMap<java.lang.String, java.lang.String>();
        filters.put("da_89_s", "TIME/时代");
       // filters.put("t87_tf", "[0 TO *}");

        LinkedHashMap<java.lang.String, java.util.List<java.lang.String>> filterFieldsValues = new java.util.LinkedHashMap<java.lang.String, java.util.List<java.lang.String>>();
        filterFieldsValues.put("da_89_s", null);
        filterFieldsValues.put("da_2955_s", null);
        List rangeList = new java.util.ArrayList<String>();
        rangeList.add("[* TO 0}");
        rangeList.add("[0 TO 10}");
        rangeList.add("[10 TO 20}");
        rangeList.add("[20 TO 30}");
        rangeList.add("[30 TO *}");
       // filterFieldsValues.put("t87_tf", rangeList);



        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setAttrCollection("screencloud");
        obj.setKeyWords("超声波");
        obj.setCatagoryId(3180);
        obj.setCityId(321);
        obj.setSorts(sorts);
        obj.setFilters(filters);
        obj.setFilterFieldsValues(filterFieldsValues);
        obj.setStart(0);
        obj.setRows(2);
        obj.setCategoryTouch(catTouch);

        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }


    public static void testSearchBrandsByCatoryId() {
        String url = "http://192.168.51.118:8999/search/brands";

        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setCatagoryId(2660);
        obj.setCityId(321);

        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }

    public static void testSuggestByKeyWords() {
        String url = "http://192.168.51.118:8999/search/suggest/keywords";

        //  SearchInterface.searchByKeywords("mergescloud", "screencloud", "圆筒", 363, null, null, 0, 10);
        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setKeyWords("圆筒");
       obj.setCityId(321);

        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }


    public static void testPopularityKeyWords(){
        String url = "http://192.168.51.118:8999/search/popularity";

        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", null, null);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }



    public static void testGetCategoryIdsByKeyWords() {
        String url = "http://192.168.51.118:8999/search/catids";

        //  SearchInterface.searchByKeywords("mergescloud", "screencloud", "圆筒", 363, null, null, 0, 10);
        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setKeyWords("圆筒");
        obj.setCityId(321);

        Map filters = new java.util.HashMap<java.lang.String, java.lang.String>();
        filters.put("da_661_s", "36×20 -> 35×33");
        // filters.put("t87_tf", "[0 TO *}");
        //obj.setFilters(null);


        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }


    public static void testSearchByKeywords() {
        String url = "http://192.168.51.118:8999/search/keywords";

        //  SearchInterface.searchByKeywords("mergescloud", "screencloud", "圆筒", 363, null, null, 0, 10);
        SearchRequestParameter obj = new SearchRequestParameter();
        obj.setCollection("mergescloud");
        obj.setAttrCollection("screencloud");
        obj.setKeyWords("圆筒");
        obj.setCityId(321);
        LinkedHashMap sorts = new java.util.LinkedHashMap<java.lang.String, java.lang.String>();
        sorts.put("price", "asc");
        obj.setSorts(sorts);
        Map filters = new java.util.HashMap<java.lang.String, java.lang.String>();
        filters.put("da_661_s", "36×20 -> 35×33");
       // filters.put("t87_tf", "[0 TO *}");
        obj.setFilters(filters);
        obj.setStart(0);
        obj.setRows(3);


        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity =  httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }


    public static void testHttp() {
        String url = "http://218.244.132.8:8088/recommend/sku";
        Map parametersMap = new java.util.HashMap<String, java.lang.Object>();
        // parametersMap.put("userId", "null");
        // parametersMap.put("catagoryId", "null");
        parametersMap.put("brandId", "1421");
        parametersMap.put("number", Integer.valueOf(30));
        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
       // Function2 callback = new Function2<HttpContext, HttpResponse, BoxedUnit>(){});
        CloseableHttpResponse httpResp =  HttpClientUtil.requestHttpSyn(url, "post", parametersMap, headers);
        try {
            String sResponse = EntityUtils.toString(httpResp.getEntity());
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }

    }
}
