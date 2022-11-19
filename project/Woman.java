package project;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Woman extends Caller implements WomanRMI,Runnable{
    List<Integer> wPref;
    List<Integer> wRank;
    int partner;

    WomanRMI stub;
    ReentrantLock eventLock;

    public Woman(int id, String[] mpeers, int[] mports, String[] wpeers, int[] wports, ArrayList<Integer> wpref){
        super(id, mpeers, mports);
        this.wPref = wpref;
        this.wRank = new ArrayList<>(this.wPref.size());
        for (int i = 0; i < wPref.size(); i++) {
            wRank.add(-1);
        }
        for (int i = 0; i < wpref.size(); i++){
            this.wRank.set(wpref.get(i), i);
        }
        partner = -1;

        try{
            System.setProperty("java.rmi.server.hostname", wpeers[this.me]);
            registry = LocateRegistry.createRegistry(wports[this.me]);
            stub = (WomanRMI) UnicastRemoteObject.exportObject(this, wports[this.me]);
            registry.rebind("Woman", stub);
        } catch(Exception e){
            e.printStackTrace();
        }finally {
            eventLock = new ReentrantLock();
        }
    }

    public void run(){
        // wait for proposals to come in
    }

    public int getId(){
        return this.me;
    }

    @Override
    public Response Proposal(Request req) throws RemoteException {
        eventLock.lock();
        boolean accept = false;
        boolean reject = false;
//        System.out.println("Woman " + this.me + "receives proposal from Man " + req.man);
        int oldPartner = this.partner;
        int numMessages = req.numMessages;
        if (oldPartner == -1) {
            this.partner = req.man;
            accept = true;
        } else if (wRank.get(req.man) < wRank.get(oldPartner)) {
//          System.out.println("Woman " + this.me + "call Reject to Man " + partner);
            this.partner = req.man;
            reject = true;
            accept = true;
        }
        eventLock.unlock();

        if(reject){
            Response response = CallMan(Message.REJECT, new Request(getId(), oldPartner, numMessages + 1), oldPartner);
            numMessages += response.numMessages;
        }
        return new Response(accept, numMessages);
    }
}
