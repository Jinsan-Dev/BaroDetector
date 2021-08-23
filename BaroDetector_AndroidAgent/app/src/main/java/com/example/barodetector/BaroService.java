package com.example.barodetector;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.jar.Attributes;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class BaroService extends Service {

    // baro
    private static final int SLIDING_WINDOW_SIZE = 70;
    ArrayList<Double> MAF;
    double[] minMax;
    double[] absDiff;

    //weka
    private static final int NUM_OF_ATTRIBUTES = 9; // 8 features + label
    private static final int NUM_OF_INSTANCES = 2;
    ArrayList<Double> outcome;

    FastVector wekaAttributes = new FastVector(NUM_OF_ATTRIBUTES);
    Instances testSet;
    Instance insert;

    Attribute ROC = new Attribute("Rate of Change");
    Attribute MCR = new Attribute("Mean Crossing Rate");
    Attribute STD = new Attribute("Standard Deviation");
    Attribute Kur = new Attribute("Kurtosis");
    Attribute Irange = new Attribute("Interquartile Range");
    Attribute RSS = new Attribute("Root Sum Square");
    Attribute RMS = new Attribute("Root Mean Square");
    Attribute Diff = new Attribute("Difference");
    Attribute Status = new Attribute("Status");

    @Override
    public void onCreate(){
        super.onCreate();
        MAF = new ArrayList();
        outcome = new ArrayList();

        wekaAttributes.addElement(ROC);
        wekaAttributes.addElement(MCR);
        wekaAttributes.addElement(STD);
        wekaAttributes.addElement(Kur);
        wekaAttributes.addElement(Irange);
        wekaAttributes.addElement(RSS);
        wekaAttributes.addElement(RMS);
        wekaAttributes.addElement(Diff);
        wekaAttributes.addElement(Status);

        testSet = new Instances("Status", wekaAttributes, NUM_OF_INSTANCES);
        testSet.setClassIndex(8);

        insert = new DenseInstance(9);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //preProcessing(MainActivity.baroData);
        //wekaService();
        Log.d("baroservice","baroService.java is on");
        return super.onStartCommand(intent, flags, startId);
    }

    public void preProcessing(ArrayList<Double> list){
        //getMAF(list);
        minMax = new double[MAF.size()-SLIDING_WINDOW_SIZE];
        absDiff = new double[MAF.size()-SLIDING_WINDOW_SIZE];
        minMax = getMinmax(MAF);
        absDiff = getAbsDiff(MAF);
        featureExtraction(minMax);
    }
    
    public void getMAF (ArrayList<Double> list){
        int idxStart = 0;
        while(idxStart != list.size() - SLIDING_WINDOW_SIZE/2){
            if(idxStart < SLIDING_WINDOW_SIZE/2){
                idxStart++;
                continue;
            }
            double[] temp = new double[SLIDING_WINDOW_SIZE];
            for(int i=0;i<SLIDING_WINDOW_SIZE;i++){
                //temp[i] = list.get(i+idxStart-SLIDING_WINDOW_SIZE/2);
            }
            MAF.add(getAverage(temp));
            idxStart++;
        }
    }

    public double getAverage (double[] arr){
        double element = 0;
        for (Double i : arr){
            element+=i;
        }
        return element/arr.length;
    }

    public double[] getMinmax (ArrayList<Double> list){
        double[] temp = new double[list.size()-SLIDING_WINDOW_SIZE];
        double max = 0, min = 10000;

        for (int i = 0;i<list.size();i++){
            if(list.get(i)>max){
                max = list.get(i);
            }
            if(list.get(i)<min){
                min = list.get(i);
            }
        }

        for (int i = 0;i<temp.length;i++) {
            double normalized = (list.get(i)-min) / max-min;
            temp[i] = normalized;
        }

        return temp;
    }

    public double[] getAbsDiff (ArrayList<Double> list){
        double[] temp = new double[list.size()-SLIDING_WINDOW_SIZE];
        for(int i=0;i<list.size()-SLIDING_WINDOW_SIZE;i++){
            double max = 0;
            double min = 10000;
            for(int j=i;j<i+SLIDING_WINDOW_SIZE;j++){
                if(list.get(j)>max){
                    max = list.get(j);
                }
                if(list.get(j)<min){
                    min = list.get(j);
                }
            }
            temp[i] = max-min;
        }
        return temp;
    }

    public void featureExtraction (double[] minMax) {
        int idx = 0;
        while (idx != minMax.length - SLIDING_WINDOW_SIZE){
            double[] temp = new double[SLIDING_WINDOW_SIZE];
            for(int i = 0;i<SLIDING_WINDOW_SIZE;i++){
                temp[i] = minMax[idx+i];
            }
            insert.setValue(ROC,getRateOfChange(temp));
            insert.setValue(MCR,getMeanCrossingRate(temp));
            insert.setValue(STD,getStandardDeviation(temp));
            insert.setValue(Kur,getKurtosis(temp));
            insert.setValue(Irange,getIrange(temp));
            insert.setValue(RSS,getRootSumSquare(temp));
            insert.setValue(RMS,getRootMeanSquare(temp));
            insert.setValue(Diff,getAbsDiff(MAF)[idx]);
        }

    }

    public double getMeanCrossingRate(double[] arr){
        int numZC = 0;
        double mean = getAverage(arr);
        for(int i=0;i<arr.length-1;i++){
            if((arr[i] >= mean && arr[i+1] < mean) || (arr[i] < mean && arr[i+1] >= mean)){
                numZC++;
            }
        }
        return numZC/arr.length;
    }

    public double getRateOfChange(double[] arr){
        return Math.abs(arr[arr.length-1] - arr[0])/arr.length;
    }

    public double getKurtosis(double[] arr){
        double mean = getAverage(arr);
        double up = 0;
        double down = 0;

        for(int i=0;i<arr.length;i++){
            up += Math.pow(arr[i]-mean,4);
            down += Math.pow(arr[i]-mean,2);
        }
        return arr.length * up / Math.pow(down,2);
    }

    public double getIrange(double[] arr){
        Arrays.sort(arr);
        int one = arr.length/4;
        int three = one*3;
        return Math.abs(arr[three]-arr[one]);
    }

    public double getRootMeanSquare(double[] arr){
        double sum = 0;
        arr = getPower(arr);
        for (Double i : arr){
            sum += i;
        }
        return Math.sqrt(sum/arr.length);
    }

    public double getRootSumSquare(double[] arr){
        double sum = 0;
        arr = getPower(arr);
        for (Double i : arr){
            sum += i;
        }
        return Math.sqrt(sum);
    }

    public double[] getPower(double[] arr){
        for (int i=0;i<arr.length;i++){
            arr[i] = Math.pow(arr[i],2);
        }
        return arr;
    }

    public double getStandardDeviation(double[] arr){
        double powerSum1 = 0;
        double powerSum2 = 0;
        double stdev = 0;

        for(Double i : arr){
            powerSum1 += i;
            powerSum2 += Math.pow(i,2);
        }
        stdev = (Math.sqrt(arr.length*powerSum2-Math.pow(powerSum1,2))/arr.length);
        return stdev;
    }

    public void wekaService() {
        try {
            Intent intent = new Intent("Baro");

            testSet.add(insert);
            Classifier cls = (Classifier) weka.core.SerializationHelper.read(getAssets().open("weka.model"));
            for(int i=0;i<testSet.numInstances();i++){
                outcome.add(cls.classifyInstance(testSet.instance(i)));
            }

            for(int i=0;i<outcome.size();i++){
                double numPass = 0;
                for(int j=i;j<i+SLIDING_WINDOW_SIZE;j++){
                    if(outcome.get(j) == 1){
                        numPass++;
                    }
                }
                double rate = numPass / SLIDING_WINDOW_SIZE;
                if(rate >= 0.8){
                    //sliding window voting 이 80% 이상일
                    if(MainActivity.getSatelliteNum() > 6){
                        MainActivity.setUserState(false);
                    }else{
                        //smth
                        //intent.putExtra("name",value)
                    }
                }else {
                    continue;
                }
            }

            sendBroadcast(intent);
        }catch (Exception e){

        }
    }
}
