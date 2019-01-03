package javagrailsort;

import java.util.Comparator;

public class SortType {
	public int key;
	public int value;

	public SortType() {
		this.key = 0;
		this.value = 0;
	}

	public SortType(int key, int value) {
		this.key = key;
		this.value = value;
	}
	
	public static class SortCmp implements Comparator<SortType> {
		public int compare(SortType a, SortType b) {
			if (a.key < b.key) return -1;
			if (a.key > b.key) return 1;
			return 0;
		}
	}
}