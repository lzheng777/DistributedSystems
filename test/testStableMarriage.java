package test;

import project.Environment;
import project.Man;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class testStableMarriage {
    public boolean isStableMatching(Matching marriage) {
        int[][] locXemp=new int[marriage.getLocationCount()][marriage.getEmployeeCount()];
        for (int i=0;i<marriage.getLocationCount();i++){
            ArrayList<Integer> prefs=marriage.getLocationPreference().get(i);
            for (int j=0;j<marriage.getEmployeeCount();j++){
                locXemp[i][prefs.get(j)]=j;
            }
        }
        int [][] empXloc=new int[marriage.getEmployeeCount()][marriage.getLocationCount()];
        for (int i=0;i<marriage.getEmployeeCount();i++){
            ArrayList<Integer> prefs=marriage.getEmployeePreference().get(i);
            for (int j=0;j<marriage.getLocationCount();j++){
                empXloc[i][prefs.get(j)]=j;
            }
        }
        ArrayList<ArrayList<Integer>> locationMatching=new ArrayList<>();
        for(int i=0;i<marriage.getLocationCount();i++)
            locationMatching.add(new ArrayList<>());
        ArrayList<Integer> match=marriage.getEmployeeMatching();
        for(int i=0;i<marriage.getEmployeeCount();i++){
            int loc=match.get(i);
            if(loc>-1)
                locationMatching.get(loc).add(i);
        }
        //Data Structures
        //matrix of location: x employee = [preference]
        //matrix of employee: x location = [preference]
        //ArrayList of Arraylist of Integer: location = {employees}

        for(int i=0;i<marriage.getLocationCount();i++) { //i is location
            int lowestPref=-1;
            for(int j=0;j<locationMatching.get(i).size();j++){//j is the index employee
                int pref=locXemp[i][locationMatching.get(i).get(j)];
                if(pref>lowestPref)
                    lowestPref=pref;
            }
            for (int j=0;j<lowestPref;j++){//j is the preference
                int emp=marriage.getLocationPreference().get(i).get(j);
                int loc=marriage.getEmployeeMatching().get(emp);
                if(loc==-1)
                    return false;
                if(i!=loc){
                    if(empXloc[emp][i]<empXloc[emp][loc])
                        return false;
                }
            }
        }
        return true;
    }

    public void stableMarriageGaleShapley_sequential(Matching marriage) {
        int[][] locXemp=new int[marriage.getLocationCount()][marriage.getEmployeeCount()];
        for (int i=0;i<marriage.getLocationCount();i++){
            ArrayList<Integer> prefs=marriage.getLocationPreference().get(i);
            for (int j=0;j<marriage.getEmployeeCount();j++){
                locXemp[i][prefs.get(j)]=j;
            }
        }
        ArrayList<Integer> locationMatching=new ArrayList<>();
        for(int i=0;i<marriage.getLocationCount();i++)
            locationMatching.add(-1);
        ArrayList<Integer> employeeMatching=new ArrayList<>();
        for(int i=0;i<marriage.getEmployeeCount();i++)//default employee location is -1
            employeeMatching.add(-1);
        //Data Structures
        //matrix of location: x employee = [preference]
        //matrix of employee: x location = [preference]
        //ArrayList of Arraylist of Integer: location = {employees}

        LinkedList<Integer> queue=new LinkedList<>();
        for(int i=0;i<marriage.getEmployeeCount();i++){//queue all employees
            queue.add(i);
        }
        while (!queue.isEmpty()){
            int emp=queue.remove();
            int pref=0;
            do {
                int loc=marriage.getEmployeePreference().get(emp).get(pref);
                int emp2=locationMatching.get(loc);
                if(emp2 == -1){
                    //accept
                    employeeMatching.set(emp,loc);
                    locationMatching.set(loc,emp);
                    break;
                }
                if(locXemp[loc][emp]<locXemp[loc][emp2]){
                    //accept and reject
                    int rejectEmp = emp2;
                    employeeMatching.set(rejectEmp,-1);
                    queue.add(rejectEmp);
                    employeeMatching.set(emp,loc);
                    locationMatching.set(loc,emp);
                    break;
                }
                pref++;
                if(pref>=marriage.getLocationCount()) {
                    //loc=-1
                    break;
                }
            }while (true);
        }

        marriage.setMatching(employeeMatching);
    }

    public void TestBasic(String filepath){
        Environment env = new Environment();
        env.init(filepath);

        Matching sequential = new Matching(env.m, env.n, env.wprefs, env.mprefs);
        final long startTime = System.currentTimeMillis();
        stableMarriageGaleShapley_sequential(sequential);
        final long endTime = System.currentTimeMillis();
        final long sequentialTime = (endTime - startTime);

        final long distributedTime = env.runStableMatching();
        ArrayList<Integer> finalMatching = new ArrayList<>();
        for (Man man : env.men) {
            finalMatching.add(man.getProposal());
        }
        Matching distributed = new Matching(env.m, env.n, env.wprefs, env.mprefs, finalMatching);

        assertTrue(isStableMatching(sequential));
        assertTrue(isStableMatching(distributed));
        assertTrue(sequential.equals(distributed));
//        assertTrue("Distributed Time: "+distributedTime+"\t Sequential Time: "+sequentialTime, distributedTime<sequentialTime);
        System.out.println("Distributed Time: "+distributedTime+"\t Sequential Time: "+sequentialTime);
        System.out.println(finalMatching);
    }

    @Test
    public void Test1(){
        TestBasic("src/data/small_inputs/1-5-5.in");
    }

    @Test
    public void Test2(){
        TestBasic("src/data/small_inputs/1-10-5.in");
    }

    @Test
    public void Test3(){
        TestBasic("src/data/small_inputs/3-3-3.in");
    }

    @Test
    public void Test4(){
        TestBasic("src/data/small_inputs/3-10-3.in");
    }

    @Test
    public void Test5(){
        TestBasic("src/data/large_inputs/40.in");
    }

    @Test
    public void Test6(){
        TestBasic("src/data/large_inputs/80.in");
    }

    @Test
    public void Test7(){
        TestBasic("src/data/large_inputs/160.in");
    }

    @Test
    public void Test8(){
        TestBasic("src/data/large_inputs/320.in");
    }

    @Test
    public void ClassDemo(){
        TestBasic("src/data/ClassDemo.in");
    }
}

class Matching{
    private final Integer m;
    private final Integer n;
    private final ArrayList<ArrayList<Integer>> woman_preference;
    private final ArrayList<ArrayList<Integer>> man_preference;
    private ArrayList<Integer> matching;
    public Matching(
            Integer m,
            Integer n,
            ArrayList<ArrayList<Integer>> woman_preference,
            ArrayList<ArrayList<Integer>> man_preference) {
        this.m = m;
        this.n = n;
        this.woman_preference = woman_preference;
        this.man_preference = man_preference;
    }
    public Matching(
            Integer m,
            Integer n,
            ArrayList<ArrayList<Integer>> woman_preference,
            ArrayList<ArrayList<Integer>> man_preference,
            ArrayList<Integer> matching) {
        this.m = m;
        this.n = n;
        this.woman_preference = woman_preference;
        this.man_preference = man_preference;
        this.matching = matching;
    }
    public Integer getLocationCount() {
        return m;
    }
    public Integer getEmployeeCount() {
        return n;
    }
    public ArrayList<ArrayList<Integer>> getLocationPreference() {
        return woman_preference;
    }
    public ArrayList<ArrayList<Integer>> getEmployeePreference() {
        return man_preference;
    }
    public ArrayList<Integer> getEmployeeMatching() {
        return matching;
    }
    public void setMatching(ArrayList<Integer> matching){ this.matching = matching; }
    public boolean equals(Matching input) {
        if(this.matching.size() != input.matching.size())
            return false;
        for (int i = 0; i < this.matching.size(); i++) {
            int j = this.matching.get(i);
            int k = input.matching.get(i);
            if (j!=k) {
                return false;
            }
        }
        return true;
    }
}
