package search.solr.client.entity.searchinterface;

/**
 * Created by soledede on 2016/4/18.
 */
public class NiNi {
    private Float costMillisecond;
    private Float costSecond;
    private Object data;

    public NiNi() {
    }

    public NiNi(Float costMillisecond, Float costSecond, Object data) {
        this.costMillisecond = costMillisecond;
        this.costSecond = costSecond;
        this.data = data;
    }

    public Float getCostMillisecond() {
        return costMillisecond;
    }

    public void setCostMillisecond(Float costMillisecond) {
        this.costMillisecond = costMillisecond;
    }

    public Float getCostSecond() {
        return costSecond;
    }

    public void setCostSecond(Float costSecond) {
        this.costSecond = costSecond;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
