
package Tools;

import java.io.Serializable;

public class Tuple<V1,V2> implements Serializable {
    private final V1 value1;
    private final V2 value2;

    public Tuple(){
        this.value1 = null;
        this.value2 = null;
    }
    public Tuple(V1 value1, V2 value2){
        this.value1 = value1;
        this.value2 = value2;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }
}
