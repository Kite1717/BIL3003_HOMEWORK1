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


         fileData.add("Minimum Support de�erini giriniz:");
        double supValue = 0.05; // bundan daha az olursa a��r� yava�l�yor
        while (true)
        {
            System.out.println("Minimum Support de�erini giriniz:");
            String input = scanner.nextLine();
            try {
                 supValue = Double.parseDouble(input);
                 if(supValue >= 0.05 && supValue <=1.0)
                break;
            }
            catch (NumberFormatException ignored){}

        }
         fileData.add(String.valueOf(supValue));
        fileData.add("Confidence / Lift / Leverage de�erlerinden birini giriniz:");

        System.out.println();
        String cll;
        while (true)
        {
            System.out.println("Confidence / Lift / Leverage de�erlerinden birini giriniz:");
             cll = scanner.nextLine();
             if(cll.equals("Confidence") || cll.equals("Lift") || cll.equals("Leverage"))
                 break;

        }
        fileData.add(cll);
        double cllValue = getCllValue(cll);
        fileData.add("Se�ti�iniz �l��n�n minimum de�eri giriniz:");
        fileData.add(String.valueOf(cllValue));
        fileData.add("------------------------\nSonu�lar:\nFrequent Item Sets:\n");

        System.out.println("------------------------\nSonu�lar:\nFrequent Item Sets:\n");

        Apriori ap = new Apriori(DataConverter.getNominalData(),DataConverter.getGuidelines(),supValue,cll,cllValue);
        ap.applyApriori();

        fileData.add("Apriori'ye g�re olu�an kurallar:");
        System.out.println("Apriori'ye g�re olu�an kurallar:");

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
            System.out.println("Se�ti�iniz �l��n�n minimum de�eri giriniz:");
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
