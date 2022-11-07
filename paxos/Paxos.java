package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    int np;
    int na;
    Object va;
    List<Task> tasks = new LinkedList<>();

    protected class Task{
        int seq;
        Object val;
        State state;
        boolean assigned;

        public Task(int seq, Object val, State state, boolean assigned){
            this.seq = seq;
            this.val = val;
            this.state = state;
            this.assigned = assigned;
        }
    }

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.np = 0;
        this.na = 0;
        this.va = 0;

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
        // Your code here
        Task task = new Task(seq,value,State.Pending,false);
        mutex.lock();
        tasks.add(task);
        mutex.unlock();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){
        //Your code here
        Task myTask = null;
        mutex.lock();
        try {
            while (myTask == null) {
                for (Task task : this.tasks) {
                    if (!task.assigned) {
                        myTask = task;
                        myTask.assigned = true;
                        break;
                    }
                }
                if (myTask == null)
                    Thread.yield();
            }
        }finally {
            mutex.unlock();
        }

        int seq = myTask.seq;
        Object val = myTask.val;
        int numberOfSystems = peers.length;

        while (myTask.state == State.Pending) {                         //while not decided, do:
            int n = Max() + 1 + me;                                     //choose n, higher than any seen so far, and unique
            Response[] prepareResponses = new Response[numberOfSystems];
            int majorityCount = 0;
            for (int i=0; i<numberOfSystems; i++) {                     //send prepare(n) to all servers including self
                String peer = peers[i];
                int port = ports[i];
                Response response = Call("Prepare", new Request(seq, val, n), port);
                prepareResponses[i] = response;
                if(response.ok)
                    majorityCount+=1;
            }
            if(majorityCount >= (numberOfSystems/2)){                   //if prepare_ok(n,na,va) from majority then
                int naMax = prepareResponses[this.me].na;
                Object vPrime = prepareResponses[this.me].va;
                for (int i=0; i<numberOfSystems; i++){                  //v'=va with the highest na; choose own v otherwise
                    if(prepareResponses[i].na > naMax)
                        vPrime = prepareResponses[i].va;
                }
                Response[] acceptResponses = new Response[numberOfSystems];
                majorityCount = 0;
                for (int i=0; i<numberOfSystems; i++){                  //send accept(n,v') to all servers
                    String peer = peers[i];
                    int port = ports[i];
                    Response response = Call("Accept", new Request(seq, vPrime, n), port);
                    acceptResponses[i] = response;
                    if(response.ok)
                        majorityCount+=1;
                }
                if(majorityCount >= (numberOfSystems/2)){               //if accept_ok(n) from majority then
                    for (int i=0; i<numberOfSystems; i++){              //send decide(v') to all
                        String peer = peers[i];
                        int port = ports[i];
                        Call("Decide", new Request(seq, vPrime, n), port);
                    }
                    myTask.state = State.Decided;
                }
            }
        }
    }

    // RMI handler
    public Response Prepare(Request req){
        // your code here
        mutex.lock();
        try {
            if (req.n > this.np) {                                            //if n > np
                this.np = req.n;                                            //np = n
                return new Response(this.np, this.na, this.va, true);   //reply prepare_ok(n,na,va)
            }                                                               //else
            return new Response(this.np, this.na, this.va, false);      //reply prepare_reject
        }finally {
            mutex.unlock();
        }
    }

    public Response Accept(Request req){
        // your code here
        mutex.lock();
        try {
            if (req.n >= this.np) {                                           //if n >= np
                this.np = req.n;                                            //np = n
                this.na = req.n;                                            //na = n
                this.va = req.val;                                          //va = v
                return new Response(this.np, this.na, this.va, true);   //reply accept_ok(n)
            }                                                               //else
            return new Response(this.np, this.na, this.va, false);      //reply accept_reject
        }finally {
            mutex.unlock();
        }
    }

    public Response Decide(Request req){
        // your code here

    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
        mutex.lock();
        try{
            for (Task task :
                    this.tasks) {
                if(task.seq == seq){
                    task.state = State.Forgotten;
                    break;
                }
            }
        }finally {
            mutex.unlock();
        }
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
        return this.np;
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here

    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
        mutex.lock();
        try{
            for (Task task :
                    this.tasks) {
                if (task.seq == seq) {
                    return new retStatus(task.state, task.val);
                }
            }
            return null;
        }finally {
            mutex.unlock();
        }
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


}
