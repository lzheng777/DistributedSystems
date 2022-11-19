package project;
import java.io.Serializable;

/**
 *
 *
 *
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    boolean accept;
    int numMessages;

    public Response(boolean accept, int numMessages){
        this.accept = accept;
        this.numMessages = numMessages;
    }
}
