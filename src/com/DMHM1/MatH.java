package com.DMHM1;

import java.util.ArrayList;

class MatH  implements  ISortable{

    //calculate mean
      static double mean (ArrayList<String> data)
     {
         if(data.size() == 0) throw  new IllegalArgumentException();
         double sum = 0.0;
         ArrayList<Double> doubleType = ISortable.convertAndSort(data);

         for(Double s : doubleType)
             sum += s;
         return sum / (doubleType.size() * 1.0);

     }
     // calculate median
      static double median(ArrayList<String> data)
     {
         if(data.size() == 0) throw  new IllegalArgumentException();
         ArrayList<Double> doubleType = ISortable.convertAndSort(data);

         int mid = doubleType.size()/2;

         if(doubleType.size() % 2 == 0) // even
             return (doubleType.get(mid-1) + doubleType.get(mid)) / 2.0;

         else //odd
             return doubleType.get(mid); // 1075 / 2 + 0.5 = 537.5 + 0.5 = 538. element

     }
     //calculate standard deviation
      static  double standardDeviation(ArrayList<String> data)
     {
         if(data.size() == 0) throw  new IllegalArgumentException();
         double sum = 0.0;
         double mean = mean(data);
         ArrayList<Double> doubleType = ISortable.convertAndSort(data);

         for (Double i : doubleType)
             sum += Math.pow((i - mean), 2);
         return Math.sqrt( sum / ( doubleType.size() - 1 ) );
     }

     // double number for round
     static double round(double value, int places)  {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    // find max value
    static double max(ArrayList<Double> list)
    {
        if (list.size() <= 0) throw new IllegalArgumentException();
        double max = list.get(0);
        for(int i =  1 ; i < list.size() ;i++)
            if(max < list.get(i))
                max = list.get(i);
            return max;
    }
    // find min value
    static double min(ArrayList<Double> list)
    {
        if (list.size() <= 0) throw new IllegalArgumentException();
        double min = list.get(0);
        for(int i =  1 ; i < list.size() ;i++)
            if(min > list.get(i))
                min = list.get(i);
        return min;
    }


}
