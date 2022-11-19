package project;

import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Caller {
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]
    Registry registry;

    public Caller(int id, String[] peers, int[] ports){
        this.me = id;
        this.peers = peers;
        this.ports = ports;
    }

    public Response CallWoman(Message rmi, Request req, int id){
        Response callReply = null;

        WomanRMI stub;
        try{
            Registry remoteRegistry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(WomanRMI) remoteRegistry.lookup("Woman");
            if(rmi.getAction().equals("Propose"))
                callReply = stub.Proposal(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return callReply;
    }

    public Response CallMan(Message rmi, Request req, int id){
        Response callReply = null;

        ManRMI stub;
        try{
            Registry remoteRegistry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(ManRMI) remoteRegistry.lookup("Man");
            if(rmi.getAction().equals("Advance"))
                callReply = stub.Advance(req);
            else if(rmi.getAction().equals("Reject"))
                callReply = stub.Reject(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return callReply;
    }
    public void Kill(){
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }
}

enum Message{
    PROPOSE("Propose"), ADVANCE("Advance"), REJECT("Reject");

    private String action;

    private Message(String action)
    {
        this.action = action;
    }

    public String getAction(){
        return this.action;
    }
}
