package javagrailsort;

import static javagrailsort.GrailSort.grailSortWithBuffer;
import static javagrailsort.GrailSort.grailSortWithDynBuffer;
import static javagrailsort.GrailSort.grailSortWithoutBuffer;

import javagrailsort.SortType.SortCmp;

public class Tester {
	
	public static SortCmp javaGrailSort; 
	
	static int seed = 100000001;
	
	/******** Tests *********/
	
	private static int randomNumber(int k){
		seed = seed * 1234565 + 1;
		return seed & 0x7fffffff * k >> 31;
	}


	private static void generateArray(SortType[] arr, int[] keyCenter, int Len, int NKey){
		
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

	private static boolean testArray(SortType[] arr, int Len){
		for(int i = 1; i < Len; i++) {
			int dk = javaGrailSort.compare(arr[i - 1], arr[i]);
			if(dk > 0) return false;
			if(dk == 0 && arr[i - 1].value > arr[i].value) return false;
		}
		return true;
	}
	
	public static void main(String[] args){
		javaGrailSort = new SortCmp();
		
		int NMax = 10000;
		int NMaxKey = 10000;
		SortType[] arr = new SortType[NMax];
		for(int i = 0; i < arr.length; i++) {
			arr[i] = new SortType();
		}
		int[] keys = new int[NMaxKey];
		
		generateArray(arr, keys, NMax, 0);
		
		double timeStart = System.currentTimeMillis();
		grailSortWithoutBuffer(arr, javaGrailSort);
		double timeFinish = System.currentTimeMillis();
		if(!testArray(arr, arr.length)) System.out.println("Grail Sort without buffer DID NOT sort successfully.");
		else System.out.println("Grail Sort without buffer sorted successfully in " + (timeFinish - timeStart) + " milliseconds.");
		
      		generateArray(arr, keys, NMax, 0);
		
                timeStart = System.currentTimeMillis();
		grailSortWithBuffer(arr, javaGrailSort);
		timeFinish = System.currentTimeMillis();
		if(!testArray(arr, arr.length)) System.out.println("Grail Sort with static buffer DID NOT sort successfully.");
		else System.out.println("Grail Sort with static buffer sorted successfully in " + (timeFinish - timeStart) + " milliseconds.");
		
                generateArray(arr, keys, NMax, 0);
		
                timeStart = System.currentTimeMillis();
		grailSortWithDynBuffer(arr, javaGrailSort);
		timeFinish = System.currentTimeMillis();
		if(!testArray(arr, arr.length)) System.out.println("Grail Sort with dynamic buffer DID NOT sort successfully.");
		else System.out.println("Grail Sort with dynamic buffer sorted successfully in " + (timeFinish - timeStart) + " milliseconds.");
	}
}
