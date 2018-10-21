/**
  * Copyright 2018 bejson.com 
  */
package entity;
import java.io.Serializable;
import java.util.List;

/**
 * Auto-generated: 2018-05-18 15:59:14
 *
 * Json数据 实体类
 */
public class JsonRootBean implements Serializable{

    private Sk sk;
    private Today today;
    private List<Future> future;
    public void setSk(Sk sk) {
         this.sk = sk;
     }
     public Sk getSk() {
         return sk;
     }

    public void setToday(Today today) {
         this.today = today;
     }
     public Today getToday() {
         return today;
     }

    public void setFuture(List<Future> future) {
         this.future = future;
     }
     public List<Future> getFuture() {
         return future;
     }

}