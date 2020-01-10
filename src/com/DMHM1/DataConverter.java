package com.DMHM1;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class DataConverter implements  ISortable{

      private  static  ArrayList<String> logFile = new ArrayList<>();
     private static ArrayList<ArrayList<String>> fullData = new ArrayList<>(); // soft data
    private static ArrayList<ArrayList<String>> guidelines = new ArrayList<>(); // for write rules
    private static int columnsCount = 0;
     private static ArrayList<String> cls; //class
     private static String[] columnsName;



     /**
      * determines the number of columns required to place the data
      * it also determines the class column
      *
      * @throws IOException standard Ä±o exception for file read handle
      */
     static void determineNumberOfColumns() throws IOException {
         File file = new File("data.txt");
         BufferedReader br = new BufferedReader(new FileReader(file));

         //pieces of first row
         String[] pieces = br.readLine().split(",");
         columnsCount = pieces.length;

         for (int i = 0; i < columnsCount; i++)
             fullData.add(new ArrayList<>());

         cls = fullData.get(columnsCount - 1);
         logFile.add("Determine Columns Count : " + columnsCount);
     }

     /**
      * takes data line by line and places it in the required list
      *
      * @throws IOException standard Ä±o exception for file read handle
      */
     static void getData() throws IOException {
         File file = new File("data.txt");
         BufferedReader br = new BufferedReader(new FileReader(file));

         String temp;
          String cN =  br.readLine();
         columnsName = cN.split(",");

         while ((temp = br.readLine()) != null) {

             String[] pieces = temp.split(",");
             for (int i = 0; i < pieces.length; i++)
                 fullData.get(i).add(pieces[i]);

         }
         //debugging
     /*for (ArrayList<String> i : fullData) {
         System.out.println(i.size());
         for (String s : i)
             System.out.print(s + ", ");
         System.out.println();
     } */

     }

    /**
     * STEP1 :  nominal name creation and handle missing values
     * STEP2 : deleting duplicate rows
     * STEP3 : binning for numeric values and switch to the available category name for nominal values
     *
     */
     static void dataPreprocessing() {

         ArrayList<String> names = new ArrayList<>();
         char A = 'A';

         //handle to missing values and binning
         for (int i = 0; i < columnsCount; i++) {
             names.add(String.valueOf(A));

             A++;

             if (determineTypeOfData(fullData.get(i))) // is number
             {


                 HashSet<String> set = new HashSet<>(fullData.get(i));
                 int dist = set.size();
                 if(fullData.get(i).contains("?")) dist--;

                  //otherwise, the number of unnecessary different values â€‹â€‹increases
                 if(dist <= 10) fillMissingValuesWithMode(fullData.get(i));

                 else fillMissingValuesWithMean(fullData.get(i));
             }
             else //for nominal columns
                 fillMissingValuesWithMode(fullData.get(i));
         }


         // deleting duplicate rows
         deletingDuplicateRows();
          //print();

         //apply binning for numeric number data transformation
         for(int i = 0; i < columnsCount;i++)
         {
             if (determineTypeOfData(fullData.get(i)))
             {
                 applyBinning(fullData.get(i),names.get(i),columnsName[i]);

             }  // name converting process
             else createGuidelinesForDefaultNominalColumns(fullData.get(i),names.get(i),columnsName[i]);

         }

     }

     /**
      * determines whether a particular data is a number or string
      *
      * @param val data column
      * @return number or string
      */
     private static boolean determineTypeOfData(ArrayList<String> val) {
         int i = 0;
         while (val.get(i).equals("?"))
             i++;

         boolean isNumber;
         try {
             Double.parseDouble(val.get(i));
             isNumber = true;
         } catch (NumberFormatException e) {
             isNumber = false;
         }
         return isNumber;
     }

     /**
      * averages according to the class of data, if there is no data of the class of data,
      * it directly changes the average of all data to the value of that number.
      *
      * @param data column data containing numbers
      */
     private static void fillMissingValuesWithMean(ArrayList<String> data) {
         double sum = 0.0;
         int count = 0;

         for (int i = 0; i < data.size(); i++) {
             if (data.get(i).equals("?")) {
                 String curClass = cls.get(i); // determine class of value
                 //System.out.println(curClass);
                 //mean by class
                 for (int j = 0; j < data.size(); j++)
                     if (cls.get(j).equals(curClass) && !data.get(j).equals("?")) {
                         sum += Double.parseDouble(data.get(j));
                         count++;
                     }

                 // directly mean
                 if (sum == 0.0) {
                     count = 0;
                     for (String s : data)
                         if (!s.equals("?")) {
                             sum += Double.parseDouble(s);
                             count++;
                         }


                 }
                 data.set(i, String.valueOf(MatH.round((sum / count),6)));
                 logFile.add("Veri Düzenleme (Handle Missing value) index : " + i + " new value :" + String.valueOf(MatH.round((sum / count),6)));

             }
         }

     }

    /**
     * mode according to the class of data, if there is no data of the class of data,
     * it directly changes the mode of all data to the value of that number.
     * @param data column data containing string
     */
     private static void fillMissingValuesWithMode(ArrayList<String> data) {

         for (int i = 0; i < data.size(); i++) {
             if (data.get(i).equals("?")){

                 String curClass = cls.get(i);
                 HashMap<String, Integer> map = new HashMap<>();
                 // mode by class
                 for (int j = 0; j < data.size(); j++)
                     if (cls.get(j).equals(curClass) && !data.get(j).equals("?"))
                         map.put(data.get(j), map.getOrDefault(data.get(j), 0) + 1);


                 //directly mode
                 if (map.keySet().size() == 0)
                 {
                     map = new HashMap<>();
                     for (String s : data)
                         if (!s.equals("?"))
                             map.put(s, map.getOrDefault(s, 0) + 1);

                 }
                 Map<String, Integer> sorted = sortByValueReverseOrder(map);

                 data.set(i, (String) sorted.keySet().toArray()[0]);
                 logFile.add("Veri Düzenleme (Handle Missing value) index : " + i + " new value :" +  sorted.keySet().toArray()[0]);

             }
         }
     }


        /**
         * https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
         * @param wordCounts unsorted map
         * @return sorted map
         */
      private static Map<String, Integer> sortByValueReverseOrder(final Map<String, Integer> wordCounts) {
         return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    /**
     * eliminates repeated rows of data
     */
     private static void deletingDuplicateRows()
     {
         HashSet<String> set = new HashSet<>();
         ArrayList<Integer> deleteIndex = new ArrayList<>();

         for(int i = 0; i < fullData.get(0).size() ;i ++)
         {
             //going into a single line
             StringBuilder sb = new StringBuilder();
             for(ArrayList<String> arr : fullData)
                sb.append(arr.get(i));

             //getting indexes to delete
             if(!set.add(sb.toString()))
             {
                 deleteIndex.add(i);
                 logFile.add("Veri Düzenleme (Remove Duplicate Rows) index : " +i + "  value : " + sb );
             }

         }

          //System.out.println(set.size());
          //System.out.println(deleteIndex.size());

         for(int i = 0; i<fullData.get(0).size();i++)
         {

             if(deleteIndex.contains(i))
             {
                 //deleting each column
                 for(int j = 0 ; j < columnsCount ; j++)
                     fullData.get(j).remove(i);

                 //deleted index removed
                 deleteIndex.remove(Integer.valueOf(i));

                 //System.out.println(deleteIndex.size());
                 //subtract one from the indexes (shifting)
                 for(int k = 0; k < deleteIndex.size();k++)
                     deleteIndex.set(k,deleteIndex.get(k) - 1);
                 i=0;
             }
         }
        // System.out.println(fullData.get(0).size());
     }

    /**
     *  decide to have equal frequency or equal width according to the skew coefficient.
     * @param data numerical dataset
     * @param categoryName o reserved header name for the data set
     * @param columnName corresponding name in the first line of the data file
     * @throws IllegalArgumentException due to calculation exceptions
     */
   private static void  applyBinning(ArrayList<String> data,String categoryName,String columnName) throws  IllegalArgumentException
   {

       //
        double coefficientOfSkewness = Math.abs((3 * (MatH.mean(data) - MatH.median(data)))/ MatH.standardDeviation(data));
       //System.out.println(coefficientOfSkewness);

       //closer to zero (symmetric distribution)
       if(String.valueOf(coefficientOfSkewness).equals("NaN")
       || Math.abs(coefficientOfSkewness ) <  Math.abs(coefficientOfSkewness - 1))
       {

         //Equal-frequency partitioning
           equalFrequencyPartitioning(data,categoryName,columnName);
       }
       else {//closer to one (skewed distribution)
           //Equal-width partitioning

           equalWidthPartitioning(data,categoryName,columnName);
       }
   }

    /**
     * binning at equal frequency
     * @param data numerical dataset
     * @param name o reserved header name for the data set
     * @param columnName corresponding name in the first line of the data file
     */

   private  static void  equalFrequencyPartitioning(ArrayList<String> data , String name,String columnName) {

       int boxCount = findCountOfBox(data);
       ArrayList<Double> sorted = ISortable.convertAndSort(data);

       if(sorted.size() % boxCount !=0)
          // so that it can be more evenly divided

       boxCount = optimalBoxCount(sorted.size(),boxCount,boxCount-1,boxCount+1);

       //System.out.println(boxCount);
       int scale,quota;
       scale =  sorted.size() / boxCount; // max index for current range

       //[left,right)
       double left = sorted.get(0);
       double right = sorted.get(scale-1);


       int nameCounter = 1;  // for different group names
       int numCount = 0; // exit control

       guidelines.add(new ArrayList<>()); // for the guidance of this column
       while (true) {

           String catName = name + nameCounter;

           //for apriori rules
           guidelines.get(guidelines.size() -1).add(columnName + " = " +catName + " [" + MatH.round(left,6) + "," + MatH.round(right,6)+"]");
           for (int i = 0; i < data.size(); i++) {
               try {
                   double num = Double.parseDouble(data.get(i));
                   if (num >= left && num <= right) {
                       logFile.add("Veri dönüþtürme (binning equal frequency )  old value :" +data.get(i ) + " new value : " +catName);
                       data.set(i, catName);
                       numCount++;
                   }

               } catch (NumberFormatException ignored) { }

           }

           //System.out.println(catName + "  " + numCount);
           if (numCount == sorted.size()) break;


           nameCounter++; // next category
           quota = sorted.size() / boxCount;

           //change range
           if(scale + quota <= sorted.size())
           {
               left = right;
               scale += quota;
               right = sorted.get(scale-1);

           }
       }
   }

    /**
     * Do not prime the number of rows absolutely :)
     * @param size count of data rows
     * @param boxCount Box length sent for final check after measurement
     * @param left min box
     * @param right max box
     * @return optimal box
     */
    private  static  int  optimalBoxCount(int size ,int boxCount,int left ,int right)
   {
       if( size % boxCount == 0) return boxCount;
       else {

           if(boxCount - left == 1)
           {
               left --;
               return  optimalBoxCount(size,left + 1,left,right);

           }
           else {
               right ++;
               return  optimalBoxCount(size,right -1 ,left,right);
           }

       }
   }

    /**
     *binning at equal depth
     *
     */
    private  static  void  equalWidthPartitioning(ArrayList<String> data ,String name,String columnName) {
        int boxCount = findCountOfBox(data);
        double max = MatH.max(ISortable.convertAndSort(data)), min = MatH.min(ISortable.convertAndSort(data));
        double increaseAmount ;
        //System.out.println("Max : " + max + "Min : " + min);

        String[] nominal = new String[data.size()]; // nominal values for data
        //int ttCount=0; // total value count
        guidelines.add(new ArrayList<>());

        int OneCountCategory; //there can only be up to two one-element categories
        boolean exit = false;
        while (!exit) {

            //nominals to replace numeric values

             nominal = new String[data.size()];
              guidelines.remove(guidelines.size() -1);
              guidelines.add(new ArrayList<>());

              //change range
              increaseAmount  = (max - min ) / boxCount;
             double left = min , right = min + increaseAmount;
              OneCountCategory = 2;
              //ttCount = 0;

            for (int i = 0; i < boxCount; i++) {

                int catCount = 0;
                String catName = name + (i + 1);
                guidelines.get(guidelines.size() -1).add(columnName + " = " +catName + " [" + MatH.round(left,6) + "," + MatH.round(right,6)+"]");

                for (int j = 0; j < data.size(); j++) {
                    try {
                        double num = Double.parseDouble(data.get(j));
                        if (num >= left && num <= right) {
                            logFile.add("Veri dönüþtürme (binning equal depth )  old value :" +data.get(i ) + " new value : " +catName);
                            nominal[j] = catName;
                            catCount++;

                        }
                    } catch (NumberFormatException ignored) {}
                }
                if(catCount == 1) OneCountCategory --;

                if(catCount == 0 || OneCountCategory < 0){
                    boxCount--;
                    exit= false;
                    break;
                }
                else exit = true;
               //System.out.println("Left : " + left + " Right : " + right);
                //System.out.println(catName + "   " + catCount);
                //ttCount += catCount;

                left = right + 0.000001;
                right += increaseAmount;

                if(i == boxCount -2  && max != right)
                {
                    right = max;
                }
            }
        }

        //System.out.println(data.size() + "   " + ttCount);
        for(int i = 0; i < data.size();i++)
            data.set(i,nominal[i]);

    }

    /**
     *
     * @param data data set nominal or numeric
     * @return optimal count
     */
    private static  int  findCountOfBox(ArrayList<String> data)
    {
        ArrayList<Double> sorted = ISortable.convertAndSort(data);
        HashSet<Double> set = new HashSet<>(sorted);
        int distinct = set.size() , boxCount;

        // sqrt(n) approach for count of box
        if(Math.sqrt(distinct) < 2.0 ) boxCount = distinct;
        else if(Math.sqrt(distinct) >= 2.0 && Math.sqrt(distinct) <= 10.0) boxCount = (int)Math.floor(Math.sqrt(distinct));
        else boxCount =9;
        return boxCount;
    }


    /**
     *
     * we are automatically guiding values  nominal arrivals
     * we also change the name for apriori
     */
    private  static  void  createGuidelinesForDefaultNominalColumns(ArrayList<String> data,String name,String columnName)
    {
        HashSet<String> set = new HashSet<>(data);
        guidelines.add(new ArrayList<>());

        Object[] temp = set.toArray();
        //System.out.println(Arrays.toString(temp));

        for(int i = 0; i <temp.length;i++)
        {
            String catName = name +(i+1);

            for(int j = 0; j < data.size() ; j++)
            {

              if(data.get(j).equals(temp[i]))
              {
                  String gl = columnName + " = " +catName + " " + data.get(j);
                  if(!guidelines.get(guidelines.size() -1).contains(gl))
                      guidelines.get(guidelines.size() -1).add(gl);
                  data.set(j,catName);

              }
            }
        }
    }

    /**
     * return the data rows by rows
     * @return rows data
     */
      static  String[][]  getNominalData()
    {
        String[][] nmd = new String[fullData.get(0).size()][fullData.size()];
        for (int i = 0; i < fullData.get(0).size();i++)
            for(int j = 0; j < columnsCount ; j++)
                nmd[i][j] = fullData.get(j).get(i);
            return nmd;
    }

    /**
     * getting guid lines
     * @return guidelines for  writing apriori rules
     */
     static ArrayList<ArrayList<String>> getGuidelines() {

        return guidelines;
    }

    /**
     * shows all data
     */
     static  void print() {
         for (int i = 0; i < fullData.get(0).size();i++)
         {
             StringBuilder sb = new StringBuilder();
             for(int j = 0; j < columnsCount ; j++)
                 sb.append(fullData.get(j).get(i)).append(" , ");
             System.out.println(sb);
         }
         System.out.println("Row count : " + fullData.get(0).size());
         System.out.println("Column count : " + columnsCount);

    }
    static void  writeReport()
    {
        try {

            BufferedWriter out = new BufferedWriter(
                    new FileWriter("odev_rapor.txt", false));
            for(int i = 0; i < logFile.size();i++)
                out.write(logFile.get(i) + "\n");

            out.write("Gidiþ sýrasý : \n 1 - Verileri aldým" +
                                     "\n 2 - Verilerin kaç sütun olduðunu belirledim" +
                                      "\n 3 - Kayýp degerleri classa göre mod , mod ,ortalama ,classa göre ortalama ile doldurdum" +
                                        "\n 4 - Tekrar eden satýlarý sildim"+
                                        "\n 5 - Sayýsal deðerleri kategorileþtirdim ve nominal deðerler için algoritmanýn anlayacaðý þekilde kategori ismi ile deðiþtirdim "+
                                    "\n 6 - Dýþarýdan Uygun sup / conf / lif / lev deðerlerini aldým"+
                                     "\n 7 - sup deðerine uygun frequent itemsetleroluþturdum" +
                                       "\n 8 - en son kalan ln kullanarak alt kümeler ve iliþkilerini uygun c/l/l deðerine göre gösterdim");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}