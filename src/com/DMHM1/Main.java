package com.DMHM1;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
private  static final Scanner scanner = new Scanner(System.in);
//list of outputs in the main function
private static final ArrayList<String> fileData = new ArrayList<>();
    public static void main(String[] args) {


        try {

            DataConverter.determineNumberOfColumns();
            DataConverter.getData();
           DataConverter.dataPreprocessing();
           // DataConverter.print();
            DataConverter.writeReport();
        } catch (IOException | IllegalArgumentException e)
        {
            e.printStackTrace();
        }


         fileData.add("Minimum Support deðerini giriniz:");
        double supValue = 0.05; // bundan daha az olursa aþýrý yavaþlýyor
        while (true)
        {
            System.out.println("Minimum Support deðerini giriniz:");
            String input = scanner.nextLine();
            try {
                 supValue = Double.parseDouble(input);
                 if(supValue >= 0.05 && supValue <=1.0)
                break;
            }
            catch (NumberFormatException ignored){}

        }
         fileData.add(String.valueOf(supValue));
        fileData.add("Confidence / Lift / Leverage deðerlerinden birini giriniz:");

        System.out.println();
        String cll;
        while (true)
        {
            System.out.println("Confidence / Lift / Leverage deðerlerinden birini giriniz:");
             cll = scanner.nextLine();
             if(cll.equals("Confidence") || cll.equals("Lift") || cll.equals("Leverage"))
                 break;

        }
        fileData.add(cll);
        double cllValue = getCllValue(cll);
        fileData.add("Seçtiðiniz ölçünün minimum deðeri giriniz:");
        fileData.add(String.valueOf(cllValue));
        fileData.add("------------------------\nSonuçlar:\nFrequent Item Sets:\n");

        System.out.println("------------------------\nSonuçlar:\nFrequent Item Sets:\n");

        Apriori ap = new Apriori(DataConverter.getNominalData(),DataConverter.getGuidelines(),supValue,cll,cllValue);
        ap.applyApriori();

        fileData.add("Apriori'ye göre oluþan kurallar:");
        System.out.println("Apriori'ye göre oluþan kurallar:");

        //writing process
        try {

            BufferedWriter out = new BufferedWriter(
                    new FileWriter("results.txt", false));
            for(String s : fileData)
                out.write(s + "\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        ap.createRules();
        ap.writeFileResults();


    }

    /**
     * getting cll value
     * @param cll Confidence / Lift /Leverage
     * @return cll value
     */
    private static double getCllValue(String cll)
    {

        double left = 0 ,right = 0;
            switch (cll)
            {
                case "Confidence" :
                {
                   left =0; right = 1.0;
                    break;
                }
                case "Lift" :
                {
                    left = 0; right = Double.MAX_VALUE;
                    break;
                }
                case "Leverage" :
                {
                    left = -0.25; right = 0.25;
                    break;
                }
            }

        double value ;
        boolean exit = false;
        while (!exit) {
            System.out.println("Seçtiðiniz ölçünün minimum deðeri giriniz:");
            String input = scanner.nextLine();
            try {
               value =  Double.parseDouble(input);
                if(value >= left && value <= right)
                 return value;
            }
            catch (NumberFormatException ignored){}

        }
        return 0;
    }
}
