package search.solr.client.entity.searchinterface;

/**
 * Created by soledede on 2016/4/18.
 */
public class NiNi {
    private Integer costMillisecond;
    private Integer costSecond;
    private Object data;

    public NiNi() {
    }

    public NiNi(Integer costMillisecond, Integer costSecond, Object data) {
        this.costMillisecond = costMillisecond;
        this.costSecond = costSecond;
        this.data = data;
    }

    public Integer getCostMillisecond() {
        return costMillisecond;
    }

    public void setCostMillisecond(Integer costMillisecond) {
        this.costMillisecond = costMillisecond;
    }

    public Integer getCostSecond() {
        return costSecond;
    }

    public void setCostSecond(Integer costSecond) {
        this.costSecond = costSecond;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
