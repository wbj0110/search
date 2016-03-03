package search.solr.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import scala.Function1;
import scala.Function2;
import scala.Tuple2;
import scala.runtime.BoxedUnit;
import search.solr.client.http.HttpClientUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Created by soledede on 2016/3/2.
 */
public class HttpClientTest {

    public static void main(String[] args) {
        testHttp();
    }


    public static void testHttp() {
        String url = "http://218.244.132.8:8088/recommend/sku";
        Map parametersMap = new java.util.HashMap<String, java.lang.Object>();
        parametersMap.put("userId", "null");
       parametersMap.put("catagoryId", "null");
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
