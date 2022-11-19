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
    int numMessages;

    public Request(int woman, int man, int numMessages){
        this.woman = woman;
        this.man = man;
        this.numMessages =numMessages;
    }
}
