package search.solr.client.producterTest;

import search.solr.client.product.Producter;

/**
 * Created by soledede on 2016/2/14.
 */
public class ProducterTest {

    public static void main(String[] args) {
        System.out.println("测试增量更新生产者通过kafka发送通知给消费者单条更新"+ Producter.send(System.currentTimeMillis(),68));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("测试增量更新生产者通过kafka发送通知给消费者，批量更新，传入更新时间段"+ Producter.send(System.currentTimeMillis(), System.currentTimeMillis(),167));
    }

}
