package search.solr.client.entity.searchinterface;

import java.io.Serializable;
import java.util.List;

/**
 * Created by soledede on 2016/2/20.
 */
public class FilterAttribute implements Serializable {

    private String attrId;
    private String attrName;
    private List<String> attrValues;
    private Boolean isRangeValue;

    public FilterAttribute() {
    }

    public FilterAttribute(String attrId, String attrName, List<String> attrValues, Boolean isRangeValue) {
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

    public List<String> getAttrValues() {
        return attrValues;
    }

    public void setAttrValues(List<String> attrValues) {
        this.attrValues = attrValues;
    }

    public Boolean getRangeValue() {
        return isRangeValue;
    }

    public void setRangeValue(Boolean rangeValue) {
        isRangeValue = rangeValue;
    }
}
