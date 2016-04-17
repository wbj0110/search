package search.solr.client.control;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import search.solr.client.entity.searchinterface.FilterAttributeSearchResult;


@RestController
public class SearchController {

    @RequestMapping("/recommend")
    public FilterAttributeSearchResult recommend(@RequestParam(value = "userid", defaultValue = "null") String userid) {
        return null;
    }
}