package com.DMHM1;

import java.util.ArrayList;
import java.util.Collections;

public interface ISortable {

    /**
     *
     * @param data double data standing in string
     * @return data set translated to double values ​​without exception
     * @throws NumberFormatException if there is a nominal value
     */
      static ArrayList<Double> convertAndSort(ArrayList<String> data) throws  NumberFormatException{
          ArrayList<Double> ret = new ArrayList<>();
          for(String s : data)
              ret.add(Double.parseDouble(s));
          Collections.sort(ret);
          return ret;
      }
}
