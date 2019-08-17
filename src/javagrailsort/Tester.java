package javagrailsort;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import javagrailsort.SortType;
import javagrailsort.SortComparator;

public class Tester {
    private DecimalFormat formatter;
    private DecimalFormatSymbols symbols;

    private SortComparator test;

    private int seed;

    private double newArrayFinish;
    private double generateArrayFinish;
    private double noBufferFinish;
    private double staticBufferFinish;
    private double dynamicBufferFinish;

    private double noBuffAverage;
    private double statAverage;
    private double dynAverage;

    public Tester() {
        this.seed = 100000001;
        
        this.test = new SortComparator();
        
        this.formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        this.symbols = this.formatter.getDecimalFormatSymbols();
        
        this.symbols.setGroupingSeparator(',');
        this.formatter.setDecimalFormatSymbols(this.symbols);
    }
    
    /******** Tests *********/

    private int randomNumber(int k){
        this.seed = (this.seed * 1234565) + 1;
        return ((this.seed & 0x7fffffff) * k) >>> 31;
    }


    private void generateArray(SortType[] arr, int[] keyCenter, int Len, int NKey) {
        for(int i = 0; i < NKey; i++) keyCenter[i] = 0;

        for(int i = 0; i < Len; i++) {
            if(NKey != 0) {
                int key = randomNumber(NKey);
                arr[i].key = key;
                arr[i].value = keyCenter[key]++;
            } else {
                arr[i].key = randomNumber(1000000000);
                arr[i].value = 0;
            }
        }
    }

    private boolean testArray(SortType[] arr, int Len) {
        for(int i = 1; i < Len; i++) {
            int dk = this.test.compare(arr[i - 1], arr[i]);
            if(dk > 0) return false;
            if(dk == 0 && arr[i - 1].value > arr[i].value) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        GrailSort GrailSorter = new GrailSort();
        Tester GrailTest = new Tester();

        int NMax = 10000000;
        int NMaxKey = ((int) (2*Math.sqrt(NMax))) - 1;

        SortType[] arr = new SortType[NMax];
        long timeStart = System.nanoTime();
        for(int i = 0; i < arr.length; i++) {
            arr[i] = new SortType();
        }
        long timeFinish = System.nanoTime();
        System.out.println("Finished allocating memory for array");
        GrailTest.newArrayFinish = (timeFinish - timeStart) / 1e+6;
        int[] keys = new int[NMaxKey];
        
        int sortRuns = 20;
        
        for(int j = 0; j < sortRuns; j++) {    
            timeStart = System.nanoTime();
            GrailTest.generateArray(arr, keys, NMax, NMaxKey);
            timeFinish = System.nanoTime();
            System.out.println("Finished generating array");
            
            GrailTest.generateArrayFinish = (timeFinish - timeStart) / 1e+6;
            SortType[] staticBufferArray = Arrays.copyOf(arr, arr.length);
            SortType[] dynamicBufferArray = Arrays.copyOf(arr, arr.length);

            timeStart = System.nanoTime();
            GrailSorter.grailSortWithoutBuffer(arr);
            timeFinish = System.nanoTime();
            System.out.println("Finished Grail Sort w/o buffer");
            GrailTest.noBufferFinish = (timeFinish - timeStart) / 1e+6;
            
            timeStart = System.nanoTime();
            GrailSorter.grailSortWithBuffer(staticBufferArray);
            timeFinish = System.nanoTime();
            System.out.println("Finished Grail Sort w/ static buffer");
            GrailTest.staticBufferFinish = (timeFinish - timeStart) / 1e+6;

            timeStart = System.nanoTime();
            GrailSorter.grailSortWithDynBuffer(dynamicBufferArray);
            System.out.println("Finished Grail Sort w/ dynamic buffer");
            timeFinish = System.nanoTime();
            GrailTest.dynamicBufferFinish = (timeFinish - timeStart) / 1e+6;

            System.out.println(" ");
            System.out.println("New array of length " + GrailTest.formatter.format(NMax) + " in " + GrailTest.formatter.format(GrailTest.newArrayFinish) + " milliseconds.");
            System.out.println("Generated array of length " + GrailTest.formatter.format(NMax) + " in " + GrailTest.formatter.format(GrailTest.generateArrayFinish) + " milliseconds.");

            if(!GrailTest.testArray(arr, arr.length)) {
                System.out.println("Grail Sort without buffers DID NOT sort successfully.");
                System.exit(1);
            }
            else System.out.println("Grail Sorting " + GrailTest.formatter.format(NMax) + " numbers without buffers sorted successfully in " + GrailTest.formatter.format(GrailTest.noBufferFinish) + " milliseconds.");

            if(!GrailTest.testArray(staticBufferArray, staticBufferArray.length)) {
                System.out.println("Grail Sort with static buffer DID NOT sort successfully.");
                System.exit(1);
            }
            else System.out.println("Grail Sorting " + GrailTest.formatter.format(NMax) + " numbers with static buffer sorted successfully in " + GrailTest.formatter.format(GrailTest.staticBufferFinish) + " milliseconds.");

            if(!GrailTest.testArray(dynamicBufferArray, dynamicBufferArray.length)) {
                System.out.println("Grail Sort with dynamic buffer DID NOT sort successfully.");
                System.exit(1);
            }
            else System.out.println("Grail Sorting " + GrailTest.formatter.format(NMax) + " numbers with dynamic buffer sorted successfully in " + GrailTest.formatter.format(GrailTest.dynamicBufferFinish) + " milliseconds.");

            GrailTest.noBuffAverage += GrailTest.noBufferFinish;
            GrailTest.statAverage += GrailTest.staticBufferFinish;
            GrailTest.dynAverage += GrailTest.dynamicBufferFinish;
            
            if(NMaxKey == ((int) (2*Math.sqrt(NMax))) - 1) {
                NMaxKey = ((int) (Math.sqrt(NMax))) - 1;
            }
            else {
                NMaxKey = ((int) (2*Math.sqrt(NMax))) - 1;
            }
            
            System.out.println(" ");
            System.out.println("Test " + (j + 1) + " complete.");
            System.out.println(" ");
        }

        System.out.println(" ");
        System.out.println("Average time in ms without buffer: " + GrailTest.noBuffAverage / sortRuns);
        System.out.println("Average time in ms with static buffer: " + GrailTest.statAverage / sortRuns);
        System.out.println("Average time in ms with dynamic buffer: " + GrailTest.dynAverage / sortRuns);
    }
}