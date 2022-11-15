package project;

import java.rmi.RemoteException;
import java.util.*;

public class Woman extends Caller implements WomanRMI,Runnable{
    List<Integer> wPref;
    List<Integer> wRank;
    int partner;

    public Woman(int id, String[] peers, int[] ports, ArrayList<Integer> wpref){
        super(id, peers, ports);
        this.wPref = wpref;
        this.wRank = new ArrayList<>(this.wPref.size());
        for (int i = 0; i < wPref.size(); i++) {
            wRank.add(-1);
        }
        for (int i = 0; i < wpref.size(); i++){
            this.wRank.set(wpref.get(i), i);
        }
        partner = -1;
    }

    public void run(){
        // wait for proposals to come in
    }

    public int getId(){
        return this.me;
    }

    @Override
    public Response Proposal(Request req) throws RemoteException {
        if (partner == -1){
            partner = req.man;
            return new Response(true);
        }
        else if (wRank.get(req.man) < wRank.get(partner)){
            CallMan(Message.REJECT, new Request(getId(),partner), partner);
            partner = req.man;
            return new Response(true);
        }
        return new Response(false);
    }
}
