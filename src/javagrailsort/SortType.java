package javagrailsort;

import java.util.Comparator;

class SortType {
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
}

class SortComparator implements Comparator<SortType> {
    @Override
    public int compare(SortType a, SortType b) {
        if (a.key < b.key) return -1;
        else if (a.key > b.key) return 1;
        else return 0;
    }
}

class GrailState {
    private int leftOverLen;
    private int leftOverFrag;
    
    public GrailState(int len, int frag) {
        this.leftOverLen = len;
        this.leftOverFrag = frag;
    }
    
    public int getLeftOverLen() {
        return leftOverLen;
    }
    
    public int getLeftOverFrag() {
        return leftOverFrag;
    }
}