package search.solr.client.entity.searchinterface;

import java.io.Serializable;
import java.util.List;

/**
 * Created by soledede on 2016/2/20.
 */
public class FilterAttributeSearchResult implements Serializable {

    private List<FilterAttribute> filterAttributes; //all filteer attributes
    private SearchResult searchResult; //the searchResult

    public FilterAttributeSearchResult() {
    }

    public FilterAttributeSearchResult(List<FilterAttribute> filterAttributes, SearchResult searchResult) {
        this.filterAttributes = filterAttributes;
        this.searchResult = searchResult;
    }

    public List<FilterAttribute> getFilterAttributes() {
        return filterAttributes;
    }

    public void setFilterAttributes(List<FilterAttribute> filterAttributes) {
        this.filterAttributes = filterAttributes;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }
}
