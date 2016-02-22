package search.solr.client.entity.searchinterface;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by soledede on 2016/2/20.
 */
public class FilterAttribute implements Serializable {

    private String attrId;
    private String attrName;  //eg: 品牌
    private List<Map<String,Integer>> attrValues; //eg:List(Map(soledede->1) ,Map(百事达->2))
    private Boolean isRangeValue;  //if is range value eg:

    public FilterAttribute() {
    }

    public FilterAttribute(String attrId, String attrName, List<Map<String,Integer>> attrValues, Boolean isRangeValue) {
        this.attrId = attrId;
        this.attrName = attrName;
        this.attrValues = attrValues;
        this.isRangeValue = isRangeValue;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public List<Map<String, Integer>> getAttrValues() {
        return attrValues;
    }

    public void setAttrValues(List<Map<String, Integer>> attrValues) {
        this.attrValues = attrValues;
    }

    public Boolean getRangeValue() {
        return isRangeValue;
    }

    public void setRangeValue(Boolean rangeValue) {
        isRangeValue = rangeValue;
    }
}
