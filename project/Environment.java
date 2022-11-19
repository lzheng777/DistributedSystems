package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Environment {
    public int n;
    public int m;
    public ArrayList<ArrayList<Integer>> mprefs;
    public ArrayList<ArrayList<Integer>> wprefs;
    public Man[] men;
    public Woman[] women;

    public void init(String inputfile){

        loadPrefs(inputfile);

        String host = "127.0.0.1";
        String[] mpeers = new String[n];
        String[] wpeers = new String[m];
        int[] mports = new int[n];
        int[] wports = new int[m];
        men = new Man[n];
        women = new Woman[m];
        for(int i = 0 ; i < n; i++){
            mpeers[i] = host;
            mports[i] = 1100+i;
        }
        for(int i = 0 ; i < m; i++){
            wpeers[i] = host;
            wports[i] = 1100+i+n;
        }
        for(int i = 0; i < n; i++){
            men[i] = new Man(i, mpeers, mports, wpeers, wports, mprefs.get(i));
        }
        for (int i = 0; i < m; i++) {
            women[i] = new Woman(i, mpeers, mports, wpeers, wports, wprefs.get(i));
        }
    }

    public void loadPrefs(String filename) {
        n = 0;
        m = 0;

        try {
            Scanner sc = new Scanner(new File(filename));
            String[] inputSizes = sc.nextLine().split(" ");

            m = Integer.parseInt(inputSizes[0]);
            n = Integer.parseInt(inputSizes[1]);
            wprefs = readPreferenceLists(sc, m);
            mprefs = readPreferenceLists(sc, n);
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find input file: " + filename);
        }
    }

    public ArrayList<ArrayList<Integer>> readPreferenceLists(Scanner sc, int x){
        ArrayList<ArrayList<Integer>> preferenceLists = new ArrayList<ArrayList<Integer>>(0);

        for (int i = 0; i < x; i++) {
            String line = sc.nextLine();
            String[] preferences = line.split(" ");
            ArrayList<Integer> preferenceList = new ArrayList<Integer>(0);
            for (String preference : preferences) {
                preferenceList.add(Integer.parseInt(preference));
            }
            preferenceLists.add(preferenceList);
        }

        return preferenceLists;
    }

    public long runStableMatching(){
        long startTime = 0, endTime = 0, distributedTime = 0;
        try {
            Thread[] womanThreads = new Thread[m];
            for (int i = 0; i < m; i++) {
                womanThreads[i] = new Thread(women[i]);
                womanThreads[i].start();     //send "initiate" to all women
            }
            Thread[] manThreads = new Thread[n];
            for (int i = 0; i < n; i++) {
                manThreads[i] = new Thread(men[i]);
            }
            startTime = System.currentTimeMillis();
            for (int i = 0; i < n; i++) {
                manThreads[i].start();     //send "initiate" to all men
            }
            for (int i = 0; i < n; i++) {
                manThreads[i].join();
            }
            for (int i = 0; i < n; i++) {
                manThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            endTime = System.currentTimeMillis();
            distributedTime = (endTime - startTime);

            for(int i = 0; i < n; i++){
                men[i].Kill();
            }
            for(int i = 0; i < m; i++){
                women[i].Kill();
            }
        }

//        ArrayList<Integer> finalMatching = new ArrayList<>();
//        for (Man man : men) {
//            finalMatching.add(man.getProposal());
//        }
//        System.out.println(finalMatching);

        return distributedTime;
    }

    public static void main(String[] args) {
        Environment env = new Environment();
        env.init(args[1]);
        env.runStableMatching();
    }
}
