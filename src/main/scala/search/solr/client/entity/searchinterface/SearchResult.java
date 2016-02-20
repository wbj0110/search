package search.solr.client.entity.searchinterface;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by soledede on 2016/2/20.
 */
public class SearchResult implements Serializable {
    private List<Map<String, Object>> result; // goods list   eg:List(Map(sku->"sdsd))
    private Map<String, Map<String, Object>> highlighting; //highlighting list eg:Map(1->Map(title->"<span class='red_searched_txt'>防护口罩</span>))
    private Map<String, Object> spellChecks;  //misspellingsAndCorrections  eg:Map("防护口罩"->"防尘口罩")
    private Msg msg;

    public SearchResult() {
    }

    public SearchResult(List<Map<String, Object>> result, Map<String, Map<String, Object>> highlighting, Map<String, Object> spellChecks, Msg msg) {
        this.result = result;
        this.highlighting = highlighting;
        this.spellChecks = spellChecks;
        this.msg = msg;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, Object>> result) {
        this.result = result;
    }

    public Map<String, Map<String, Object>> getHighlighting() {
        return highlighting;
    }

    public void setHighlighting(Map<String, Map<String, Object>> highlighting) {
        this.highlighting = highlighting;
    }

    public Map<String, Object> getSpellChecks() {
        return spellChecks;
    }

    public void setSpellChecks(Map<String, Object> spellChecks) {
        this.spellChecks = spellChecks;
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }
}
