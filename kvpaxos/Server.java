package kvpaxos;
import paxos.Paxos;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    // Your definitions here
    int nextSeq;

    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        // Your initialization code here
        nextSeq = 0;

        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    // RMI handlers
    public Response Get(Request req){
        // Your code here
        // get Paxos instance max sequence number
        mutex.lock();
        int seq = nextSeq < px.Max() ? px.Max() : nextSeq;
        seq++;
        nextSeq = seq;
        mutex.unlock();

        // Start Paxos agreement for request, wait for agreement, clear memory
        Op op = new Op(req.op, seq, req.key, req.value);
        px.Start(seq, op);
        Op res = wait(seq);
        px.Done(seq);

        return new Response(res);
    }

    public Response Put(Request req){
        // Your code here
        mutex.lock();
        int seq = nextSeq < px.Max() ? px.Max() : nextSeq;
        seq++;
        nextSeq = seq;
        mutex.unlock();

        Op op = new Op(req.op, seq, req.key, req.value);
        px.Start(seq, op);
        Op res = wait(seq);

        return new Response(res);
    }

    public Op wait(int seq){
        int to = 10;
        while (true){
            Paxos.retStatus ret = this.px.Status(seq) ;
            if (ret.state == State.Decided){
                return Op.class.cast(ret.v);
            }
            try{
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace() ;
            }
            if (to < 1000){
                to = to * 2 ;
            }
        }
    }
}
