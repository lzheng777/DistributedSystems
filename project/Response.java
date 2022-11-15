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

    public Response(boolean accept){
        this.accept = accept;
    }
}
