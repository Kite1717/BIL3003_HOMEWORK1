package com.DMHM1;




import java.io.*;
import java.util.*;

public class Apriori {

    private String[][] nominalData; // all nominal data list
    private ArrayList<ArrayList<String>> guidelines; // for rules
    private double minSupValue; // min supcount
    private String[] rows; // reduced line-by-line nominal data
    private ArrayList<HashMap<String,Integer>> frequentItemSets;
    private String criterion; // Confidence - Lift - Leverage
    private  double cllValue; // criterion value
    private ArrayList<String> allComb ; //
    private  ArrayList<String> fileData; // list to write to file
    private int ruleCount = 1; // count of valid rule

    /**
     *
     * Preparing part for the necessary start
     */
    public Apriori(String[][] nominalData, ArrayList<ArrayList<String>> guidelines, double minSupValue,String criterion,double cllValue) {
        this.nominalData = nominalData;
        this.guidelines = guidelines;
        this.minSupValue = minSupValue;
        this.frequentItemSets = new ArrayList<>();
        this.criterion = criterion;
        this.cllValue = cllValue;
        allComb = new ArrayList<>();
        fileData = new ArrayList<>();
        createRows();
    }

    /**
     *make each individual cell line a single line
     */
    private void createRows()
    {
        rows = new String[nominalData.length];
        for(int i = 0; i < nominalData.length ; i++)
        {
            StringBuilder sb = new StringBuilder();
            for(int j = 0; j < nominalData[i].length;j++)
            {
                sb.append(nominalData[i][j]);
            }
            rows[i] = sb.toString();
        }
    }

    /**
     * Step1 : create cn for nominal data and copy the cn key set for the next ln
     * Step2 : create ln using cn and min sup value
     * Step3 : copy the ln key set for the next cn and repeat
     */
      void  applyApriori()
    {

        //rate  count/dataSizeSet
        int dataSetSize = nominalData.length;

        System.out.println("MIN SUP COUNT : " + (minSupValue*(dataSetSize * 1.0)) + "\n");
        fileData.add("MIN SUP COUNT : " + (minSupValue*(dataSetSize * 1.0) + "\n"));

        //first step c1
       HashMap<String,Integer> map = new HashMap<>();
        Object[] temp;
        int l = 1;
        while (true) {

            if(l == 1) // c1
            {
                for(int i = 0; i < nominalData.length;i++)
                    for(int j = 0; j < nominalData[i].length ;j++)
                        map.put(nominalData[i][j],map.getOrDefault(nominalData[i][j],0) + 1);

                /*System.out.println("C "  + l + "   sup : " + (minSupValue*(dataSetSize * 1.0)));
                System.out.println(map  + "\n"); */

                temp = map.keySet().toArray();
            }
            else{
                //cn
                //Cartesian
                temp = map.keySet().toArray();
                map = new HashMap<>();
                for (int i = 0; i < temp.length - 1; i++) {

                    for (int j = i + 1; j < temp.length; j++) {

                        //searching if available
                        int count = appropriateAndSearching(temp[i].toString(), temp[j].toString(), l);
                        StringBuilder sb = new StringBuilder();

                        if (count != 0 )
                        {
                           //concat

                            String[] p1 = temp[i].toString().split("-");
                            String[] p2 = temp[j].toString().split("-");
                            HashSet<String> set = new HashSet<>(Arrays.asList(p1));
                            set.addAll(Arrays.asList(p2));

                            //format Category_Name1,Category_Name2,...
                            Object[] added = set.toArray();
                            for(int k = 0; k < added.length;k++)
                                if(k != added.length -1)
                                    sb.append(added[k]).append("-");
                                else sb.append(added[k]);


                            // Is there another same combination in ?
                            if(checkCombination(map,sb.toString()))
                            map.put(sb.toString(), count);
                        }
                    }
                }
                /*System.out.println("C "  + l + "   sup : " + (minSupValue*(dataSetSize * 1.0)));
                System.out.println(map  + "\n"); */

                if(map.keySet().size() == 0) break; // cn.size == 0 ?
                temp = map.keySet().toArray();
            }

            // create ln use min sup value
           for (Object aTemp : temp) {
               double rate = (1.0 * map.get(aTemp.toString())) / (dataSetSize * 1.0);
               if (rate < minSupValue)
                   map.remove(aTemp.toString());
           }



           if(map.keySet().size() == 0) break; // ln == 0
           else if(map.keySet().size() == 1) // ln == 1
           {
               printFrequentItemSet(map,l);
               frequentItemSets.add(map);
               break;
           }
           else frequentItemSets.add(map);

           printFrequentItemSet(map,l); // show on screen
            l++;
        }

        if(frequentItemSets.size() == 0)
        {
            System.out.println("No large item sets and rules founds!");
            fileData.add("No large item sets and rules founds!");
        }


    }

    /**
     *
     * Is there another combination in the same ln she is looking at it
     * @param map current ln
     * @param s current candidate
     * @return with or without
     */
    private  boolean checkCombination(HashMap<String,Integer> map ,String s)
    {
        HashSet<String> set;

        Object[] keys = map.keySet().toArray();
        for(Object k : keys)
        {
            set = new HashSet<>(Arrays.asList(s.split("-")));
            int size = set.size();
           set.addAll(Arrays.asList(k.toString().split("-")));
           //all duplicate
           if(size == set.size()) return  false;

        }
        return true;
    }

    /** is checking if it is suitable for cn
     * Formula = s1.distinct().size +   s2.distinct().size == l ?
     * @param l current l level
     * @return returns frequency if appropriate
     */
    private int  appropriateAndSearching(String s1 , String s2 ,int l)
    {
        //appropriate
        String[] p1 = s1.split("-");
        String[] p2 = s2.split("-");
        HashSet<String> set = new HashSet<>(Arrays.asList(p1));
        set.addAll(Arrays.asList(p2));

        //searching element by element
        int count = 0;
        if(l == set.size())
        {
            Object[] spe = set.toArray();
            for(String str : rows)
            {
                boolean flag = true;
                for(Object s : spe)
                {
                    if(!str.contains(s.toString())){
                        flag = false;
                        break;
                    }
                }
                if(flag) count++;
            }
        }
        return count;
    }


    /**
     * print frequent item set to the screen and saves the file list
     *
     */
   private  void  printFrequentItemSet(Map<String,Integer> map , int l)
    {
        StringBuilder sb = new StringBuilder();
        //frequent item set
        System.out.println("(L"  + l + ")");
        System.out.println(map  + "\n");

        fileData.add("(L"  + l + ")");
        sb.append("{ ");
        for(String s : map.keySet())
            sb.append(s).append("=").append(map.get(s)).append(", ");
        sb.append("}");

        fileData.add(sb.toString() + "\n");

    }

    /**
     *creates all rules using binary logic for all subsets
     */
      void  createRules()
    {
        if(frequentItemSets.size() == 1)
        {
            System.out.println("No rules found");
            fileData.add("No rules found");
            return;
        }
        //get last frequent item sets and all Subsets
       ArrayList<String> subSets =  createAllSubsets();
        //cleaning duplicate combination
        subSets = new ArrayList<>(new HashSet<>(subSets));


        //create rules

        for(int i = 0 ; i < subSets.size() ;i++)
        {
            //declare possible combination
            String[] p = subSets.get(i).split("-");
            // all  possible
            allComb = new ArrayList<>();
            generateAllBinaryStrings(p.length,new int[p.length],0);

            //deleting unnecessary combination
            allComb.remove(0);
            allComb.remove(allComb.size()-1);
            //System.out.println(allComb);

            //all combination
            for(int j = 0; j <allComb.size();j++)
            {
                //formula binary logic
                char[] instruction = allComb.get(j).toCharArray();

                StringBuilder zeroGroups = new StringBuilder();
                StringBuilder oneGroups = new StringBuilder();
                for(int k = 0; k < instruction.length;k++)
                {
                    if(instruction[k] == '1')
                        oneGroups.append(p[k]).append("-");
                    else zeroGroups.append(p[k]).append("-");
                }

                // I deleted the extra trailing character
                oneGroups.deleteCharAt(oneGroups.toString().length()-1);
                zeroGroups.deleteCharAt(zeroGroups.toString().length()-1);

                 //categories  ready for create rules
                calculateRulesValue(oneGroups.toString(),zeroGroups.toString(),searchCount(subSets.get(i)));
            }

        }

    }

    /**
     * Creates all subsets of all the smallest two elements for ln for the last frequent item set
     * @return all subset
     */
    private ArrayList<String> createAllSubsets()
    {

        ArrayList<String> subSets = new ArrayList<>();

        Map<String,Integer> map = frequentItemSets.get(frequentItemSets.size() -1);

        Object[] keys = map.keySet().toArray();

        for(int k = 0; k < keys.length;k++) {

            String[] p = keys[k].toString().split("-");
            int n = p.length;
            int added;
            StringBuilder sb;

            // Run a loop for printing all 2^n
            // subsets one by obe
            for (int i = 0; i < (1 << n); i++) {
                sb = new StringBuilder();
                added=0;
                // AddSubSet
                for (int j = 0; j < n; j++)
                {

                    // (1<<j) is a number with jth bit 1
                    // so when we 'and' them with the
                    // subset number we get which numbers
                    // are present in the subset and which
                    // are not
                    if ((i & (1 << j)) > 0) {
                        added++;
                        sb.append(p[j]).append("-");
                    }
                }
                if(added >= 2 && !subSets.contains(sb.toString()))
                {
                    sb.deleteCharAt(sb.toString().length()-1);
                    subSets.add(sb.toString());
                }
            }

        }
        return subSets;

    }

    /**
     * calculate Confidence / Lift /Leverage
     * @param ones Combinations with ones
     * @param zeros Combinations with zeros
     * @param allCount frequency of both
     */
    private void  calculateRulesValue(String ones ,String zeros,int allCount)
    {
        String phrase;
       if(criterion.equals("Confidence"))
       {
           int onesCount = searchCount(ones);
           int zerosCount = searchCount(zeros);
        double conf =  ((1.0 * allCount) / nominalData.length) /  ((onesCount * 1.0) / nominalData.length);
        if(conf >= cllValue && ((onesCount * 1.0) / nominalData.length) != 0 )
        {
            phrase = printRule(ones,zeros,onesCount,zerosCount);
            phrase +="  Support : "+ MatH.round(((1.0 * allCount) / nominalData.length),2) + "  Confidence : " + conf;
            System.out.println(ruleCount + " - " +phrase);
            fileData.add(ruleCount + " - " + phrase);
            ruleCount ++;

        }

       }
       else if(criterion.equals("Lift"))
       {
           int onesCount = searchCount(ones);
           int zerosCount = searchCount(zeros);
          double lift = ((1.0 * allCount) / nominalData.length) /(((onesCount * 1.0) / nominalData.length) * ((zerosCount * 1.0) / nominalData.length));
           if(lift >= cllValue)
           {
               phrase = printRule(ones,zeros,onesCount,zerosCount);
               phrase +=" Support : "+ MatH.round(((1.0 * allCount) / nominalData.length),2) +  "  Lift : " + lift;
               System.out.println(ruleCount + " - " +phrase);
               fileData.add(ruleCount + " - " +phrase);
               ruleCount ++;

           }
       }
       else { //Leverage

           int onesCount = searchCount(ones);
           int zerosCount = searchCount(zeros);
           double leverage = ((1.0 * allCount) / nominalData.length) - (((onesCount * 1.0) / nominalData.length) * ((zerosCount * 1.0) / nominalData.length));
           if(leverage >= cllValue && leverage <= 0.25)
           {

               phrase = printRule(ones,zeros,onesCount,zerosCount);
               phrase += " Support : "+ MatH.round(((1.0 * allCount) / nominalData.length),2) + "  Leverage : " + leverage;
               System.out.println(ruleCount + " - " + phrase);
               fileData.add(ruleCount + " - " + phrase);
               ruleCount ++;
           }
       }

    }

    /**
     * finds which value is in which group and prints with the group
     * @param ones Combinations with ones
     * @param zeros Combinations with zeros
     * @param oneCount Combinations with ones frequency
     * @param zeroCount Combinations with zeros frequency
     * @return ID IF ... AND ... THEN ... SUP COUNT  C/L/L
     */
    private String  printRule(String ones,String zeros,int oneCount,int zeroCount)
    {
      StringBuilder sb = new StringBuilder();

       sb.append("IF  ");

       //adding ones
      String[] p1 = ones.split("-");
      int andCount = p1.length -1;
      boolean find;
      for(int i = 0; i < p1.length ; i++)
      {
          find = false;
          for(ArrayList<String> arr : guidelines)
          {
              for(String str : arr)
              {
                  if(str.contains(p1[i]))
                  {
                      sb.append(str);
                      if(andCount != 0)
                      {
                          sb.append("  AND  ");
                          andCount--;

                      }
                      find = true;
                      break;
                  }
              }
              if(find) break;
          }
      }

      sb.append("  ").append(oneCount).append("   =>  ");

      //adding zeros
         p1 = zeros.split("-");
         andCount = p1.length -1;
        for(int i = 0; i < p1.length ; i++)
        {
            find = false;
            for(ArrayList<String> arr : guidelines)
            {
                for(String str : arr)
                {
                    if(str.contains(p1[i]))
                    {
                        sb.append(str);
                        if(andCount != 0)
                        {
                            sb.append("  AND  ");
                            andCount--;
                        }
                        find = true;
                        break;
                    }

                }
                if(find) break;
            }
        }
        sb.append("  ").append(zeroCount);
        return sb.toString();
    }

    /**
     * find the frequency of the group
     * @param s group
     * @return frequency count
     */
    private  int searchCount(String s)
    {
        int count = 0;
        for(Map<String,Integer> map : frequentItemSets)
        {
            if(map.containsKey(s))
            {
                count = map.get(s);
                break;
            }
        }
        return count;
    }

    /**
     * adds any place combinations that may occur for the current set
     */
   private  void addAllCombination(int arr[], int n)
    {
        StringBuilder sb = new StringBuilder();
       for(int i = 0; i < n ;i++)
       {
           sb.append(arr[i]);
       }
       allComb.add(sb.toString());
    }

    /**    1 = > 0
     * finds all combinations of places that can occur for the current set
     *
     * 001
     * 010
     * 110
     * ...
     * @param n
     * @param arr
     * @param i
     */
    // Function to generate all binary strings
   private   void generateAllBinaryStrings(int n,
                                         int arr[], int i)
    {
        if (i == n)
        {
            addAllCombination(arr, n);
            return;
        }

        // First assign "0" at ith position
        // and try for all other permutations
        // for remaining positions
        arr[i] = 0;
        generateAllBinaryStrings(n, arr, i + 1);

        // And then assign "1" at ith position
        // and try for all other permutations
        // for remaining positions
        arr[i] = 1;
        generateAllBinaryStrings(n, arr, i + 1);
    }

    /**
     * prints the list
     */
    public  void writeFileResults()
    {
        try {

            BufferedWriter out = new BufferedWriter(
                    new FileWriter("results.txt", true));
           for(int i = 0; i < fileData.size();i++)
                out.write(fileData.get(i) + "\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}




