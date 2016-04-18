package search.solr.client.control;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import search.solr.client.entity.searchinterface.FilterAttributeSearchResult;
import search.solr.client.entity.searchinterface.NiNi;
import search.solr.client.searchInterface.SearchInterface;


@RestController
public class SearchController {

    @RequestMapping("/recommend")
    public NiNi recommend(@RequestParam(value = "userid", defaultValue = "null") String userid) {
        return new NiNi(1,23,SearchInterface.searchByKeywords("mergescloud", "screencloud", "圆筒", 363, null, null, 0, 10));
    }
}