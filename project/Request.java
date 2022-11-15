package project;
import java.io.Serializable;

/**
 *
 *
 *
 *
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    int woman;
    int man;

    public Request(int woman, int man){
        this.woman = woman;
        this.man = man;
    }
}
