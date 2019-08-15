package javagrailsort;

/********* Grail sorting *********************************/
/*                                                       */
/* (c) 2013 by Andrey Astrelin                           */
/* Refactored by MusicTheorist                           */
/*                                                       */
/* Stable sorting that works in O(N*log(N)) worst time   */
/* and uses O(1) extra memory                            */
/*                                                       */
/* Define SortType / SortComparator                      */
/* and then call GrailSort() function                    */
/*                                                       */
/* For sorting w/ fixed external buffer (512 items)      */
/* use GrailSortWithBuffer()                             */
/*                                                       */
/* For sorting w/ dynamic external buffer (sqrt(length)) */
/* use GrailSortWithDynBuffer()                          */
/*                                                       */
/*********************************************************/

public class GrailSort {

    final private int grailStaticBufferLen = 512;

    private SortComparator grail;

    private static void grailSwap(SortType[] arr, int a, int b) {   
        SortType temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    private static void grailMultiSwap(SortType[] arr, int a, int b, int swapsLeft) {
        while(swapsLeft != 0) { 
            grailSwap(arr, a++, b++);
            swapsLeft--;
        }
    }

    private static void grailRotate(SortType[] array, int pos, int lenA, int lenB) {
        while(lenA != 0 && lenB != 0) {
            if(lenA <= lenB) {
                grailMultiSwap(array, pos, pos + lenA, lenA);
                pos += lenA;
                lenB -= lenA;
            } 
            else {
                grailMultiSwap(array, pos + (lenA - lenB), pos + lenA, lenB);
                lenA -= lenB;
            }
        }
    }
    
    private void grailInsertSort(SortType[] arr, int pos, int len) {
        for(int i = 1; i < len; i++) {
            int dist = pos + i - 1; 
            SortType item = arr[pos + i];

            while((dist - pos) >= 0 && this.grail.compare(arr[dist], item) > 0) {
                arr[dist + 1] = arr[dist--];
            }
            arr[dist + 1] = item;
        }
    }

    //boolean argument determines direction
    private int grailBinSearch(SortType[] arr, int pos, int len, int keyPos, boolean isLeft) {
        int left = -1, right = len;

        while(left < right - 1) {
            int mid = left + ((right - left) >> 1);

            if(isLeft) {
                if(this.grail.compare(arr[pos + mid], arr[keyPos]) >= 0) {
                    right = mid;
                } 
                else left = mid;
            }
            else {
                if(this.grail.compare(arr[pos + mid], arr[keyPos]) > 0) {
                    right = mid;
                } 
                else left = mid;
            }
        }
        return right;
    }

    // cost: 2 * len + numKeys^2 / 2
    private int grailFindKeys(SortType[] arr, int pos, int len, int numKeys) {
        int dist = 1;
        int foundKeys = 1, firstKey = 0;  // first key is always here

        while(dist < len && foundKeys < numKeys) {
            //Binary Search left
            int loc = grailBinSearch(arr, pos + firstKey, foundKeys, pos + dist, true);
            
            if(loc == foundKeys || this.grail.compare(arr[pos + dist], arr[pos + (firstKey + loc)]) != 0) {
                grailRotate(arr, pos + firstKey, foundKeys, dist - (firstKey + foundKeys));
                
                firstKey = dist - foundKeys;
                
                grailRotate(arr, pos + (firstKey + loc), foundKeys - loc, 1);
                foundKeys++;
            }
            dist++;
        }
        grailRotate(arr, pos, firstKey, foundKeys);

        return foundKeys;
    }

    // cost: min(len1, len2)^2 + max(len1, len2)
    private void grailMergeWithoutBuffer(SortType[] arr, int pos, int len1, int len2) {
        if(len1 < len2) {
            while(len1 != 0) {
                //Binary Search left
                int loc = grailBinSearch(arr, pos + len1, len2, pos, true);
            
                if(loc != 0) {
                    grailRotate(arr, pos, len1, loc);
                    
                    pos += loc;
                    len2 -= loc;
                }
                
                if(len2 == 0) break;
                
                do {
                    pos++;
                    len1--;
                } while(len1 != 0 && this.grail.compare(arr[pos], arr[pos + len1]) <= 0);
            }
        } 
        else {
            while(len2 != 0) {
                //Binary Search right
                int loc = grailBinSearch(arr, pos, len1, pos + (len1 + len2 - 1), false);
               
                if(loc != len1) {
                    grailRotate(arr, pos + loc, len1 - loc, len2);
                    len1 = loc;
                }
                
                if(len1 == 0) break;
                
                do {
                    len2--;
                } while(len2 != 0 && this.grail.compare(arr[pos + len1 - 1], arr[pos + len1 + len2 - 1]) <= 0);
            }
        }
    }

    // arr - starting array. arr[0 - blockLen..-1] - buffer (if havebuf).
    // blockLen - length of regular blocks. First blockCount blocks are stable sorted by 1st elements and key-coded
    // keysPos - arrays of keys, in same order as blocks. keysPos < midkey means stream A
    // aBlockCount are regular blocks from stream A.
    // lastLen is length of last (irregular) block from stream B, that should go before aBlockCount blocks.
    // lastLen = 0 requires aBlockCount = 0 (no irregular blocks). lastLen > 0, aBlockCount = 0 is possible.
    private void grailMergeBuffersLeft(SortType[] arr, int keysPos, int midkey, int pos, int blockCount, int blockLen,
                                              boolean havebuf, int aBlockCount, int lastLen) {

        if(blockCount == 0) {
            int aBlocksLen = aBlockCount * blockLen;
            
            if(havebuf) grailMergeLeft(arr, pos, aBlocksLen, lastLen, 0 - blockLen);
            else grailMergeWithoutBuffer(arr, pos, aBlocksLen, lastLen);
            
            return;
        }

        int leftOverLen, processIndex;
        leftOverLen = processIndex = blockLen;
        
        int leftOverFrag = this.grail.compare(arr[keysPos], arr[midkey]) < 0 ? 0 : 1;
        int restToProcess;

        for(int keyIndex = 1; keyIndex < blockCount; keyIndex++, processIndex += blockLen) {
            restToProcess = processIndex - leftOverLen;
            int nextFrag = this.grail.compare(arr[keysPos + keyIndex], arr[midkey]) < 0 ? 0 : 1;

            if(nextFrag == leftOverFrag) {
                if(havebuf) grailMultiSwap(arr, pos + restToProcess - blockLen, pos + restToProcess, leftOverLen);
                
                restToProcess = processIndex;
                leftOverLen = blockLen;
            } 
            else {
                if(havebuf) {
                    GrailState results = grailSmartMergeWithBuffer(arr, pos + restToProcess, leftOverLen, leftOverFrag, blockLen);
                 
                    leftOverLen = results.getLeftOverLen();
                    leftOverFrag = results.getLeftOverFrag();
                } 
                else {
                    GrailState results = grailSmartMergeWithoutBuffer(arr, pos + restToProcess, leftOverLen, leftOverFrag, blockLen);
                    
                    leftOverLen = results.getLeftOverLen();
                    leftOverFrag = results.getLeftOverFrag();
                }
            }
        }
        
        restToProcess = processIndex - leftOverLen;

        if(lastLen != 0) {
            if(leftOverFrag != 0) {
                if(havebuf) grailMultiSwap(arr, pos + restToProcess - blockLen, pos + restToProcess, leftOverLen);
                
                restToProcess = processIndex;
                leftOverLen = blockLen * aBlockCount;
                leftOverFrag = 0;
            } 
            else leftOverLen += blockLen * aBlockCount;

            if(havebuf) grailMergeLeft(arr, pos + restToProcess, leftOverLen, lastLen, 0 - blockLen);
            else grailMergeWithoutBuffer(arr, pos + restToProcess, leftOverLen, lastLen);
        } 
        else {
            if(havebuf) grailMultiSwap(arr, pos + restToProcess, pos + (restToProcess - blockLen), leftOverLen);
        }
    }

    // arr[dist..-1] - buffer, arr[0, leftLen - 1] ++ arr[leftLen, leftLen + rightLen - 1]
    // -> arr[dist, dist + leftLen + rightLen - 1]
    private void grailMergeLeft(SortType[] arr, int pos, int leftLen, int rightLen, int dist) {
        int left = 0, right = leftLen;

        rightLen += leftLen;

        while(right < rightLen) {
            if(left == leftLen || this.grail.compare(arr[pos + left], arr[pos + right]) > 0) {
                grailSwap(arr, pos + (dist++), pos + (right++));
            } 
            else grailSwap(arr, pos + (dist++), pos + (left++));
        }
        if(dist != left) grailMultiSwap(arr, pos + dist, pos + left, leftLen - left);
    }
    private void grailMergeRight(SortType[] arr, int pos, int leftLen, int rightLen, int dist) {
        int mergedPos = leftLen + rightLen + dist - 1;
        int right = leftLen + rightLen - 1, left = leftLen - 1;

        while(left >= 0) {
            if(right < leftLen || this.grail.compare(arr[pos + left], arr[pos + right]) > 0) {
                grailSwap(arr, pos + (mergedPos--), pos + (left--));
            } 
            else grailSwap(arr, pos + (mergedPos--), pos + (right--));
        }
        if(right != mergedPos) {
            while(right >= leftLen) grailSwap(arr, pos + (mergedPos--), pos + (right--));
        }
    }

    //returns the leftover length, then the leftover fragment
    private GrailState grailSmartMergeWithoutBuffer(SortType[] arr, int pos, int leftOverLen, int leftOverFrag, int regBlockLen) {
        if(regBlockLen == 0) return new GrailState(leftOverLen, leftOverFrag);

        int len1 = leftOverLen, len2 = regBlockLen;
        int typeFrag = 1 - leftOverFrag; //1 if inverted

        if(len1 != 0 && this.grail.compare(arr[pos + (len1 - 1)], arr[pos + len1]) - typeFrag >= 0) {
            while(len1 != 0) {
                int foundLen;
                
                //Binary search left, else search right
                if (typeFrag != 0) foundLen = grailBinSearch(arr, pos + len1, len2, pos, true);
                else foundLen = grailBinSearch(arr, pos + len1, len2, pos, false);
                
                if(foundLen != 0) {
                    grailRotate(arr, pos, len1, foundLen);
                    
                    pos += foundLen;
                    len2 -= foundLen;
                }
                
                if(len2 == 0) return new GrailState(len1, leftOverFrag);
                
                do {
                    pos++;
                    len1--;
                } while(len1 != 0 && this.grail.compare(arr[pos], arr[pos + len1]) - typeFrag < 0);
            }
        }
        return new GrailState(len2, typeFrag);
    }

    //returns the leftover length, then the leftover fragment
    private GrailState grailSmartMergeWithBuffer(SortType[] arr, int pos, int leftOverLen, int leftOverFrag, int blockLen) {
        int dist = 0 - blockLen;
        int left = 0, right = leftOverLen;
        int leftEnd = right, rightEnd = right + blockLen;
        int typeFrag = 1 - leftOverFrag;  // 1 if inverted

        while(left < leftEnd && right < rightEnd) {
            if(this.grail.compare(arr[pos + left], arr[pos + right]) - typeFrag < 0) {
                grailSwap(arr, pos + (dist++), pos + (left++));
            }
            else grailSwap(arr, pos + (dist++), pos + (right++));
        }
        
        int length, fragment = leftOverFrag;
        
        if(left < leftEnd) {
            length = leftEnd - left;
            
            while(left < leftEnd) grailSwap(arr, pos + (--leftEnd), pos + (--rightEnd));
        } 
        else {
            length = rightEnd - right;
            fragment = typeFrag;
        }
        return new GrailState(length, fragment);
    }


    /***** Sort With Extra Buffer *****/

    //returns the leftover length, then the leftover fragment
    private GrailState grailSmartMergeWithXBuf(SortType[] arr, int pos, int leftOverLen, int leftOverFrag, int blockLen) {
        int dist = 0 - blockLen;
        int left = 0, right = leftOverLen;
        int leftEnd = right, rightEnd = right + blockLen;
        int typeFrag = 1 - leftOverFrag;  // 1 if inverted

        while(left < leftEnd && right < rightEnd) {
            if(this.grail.compare(arr[pos + left], arr[pos + right]) - typeFrag < 0) {
                arr[pos + (dist++)] = arr[pos + (left++)];
            }
            else arr[pos + (dist++)] = arr[pos + (right++)];
        }
        
        int length, fragment = leftOverFrag;
        
        if(left < leftEnd) {
            length = leftEnd - left;
            
            while(left < leftEnd) arr[pos + (--rightEnd)] = arr[pos + (--leftEnd)];
        } 
        else {
            length = rightEnd - right;
            fragment = typeFrag;
        }
        return new GrailState(length, fragment);
    }

    // arr[dist..-1] - free, arr[0, leftEnd - 1] ++ arr[leftEnd, leftEnd + rightEnd - 1]
    // -> arr[dist, dist + leftEnd + rightEnd - 1]
    private void grailMergeLeftWithXBuf(SortType[] arr, int pos, int leftEnd, int rightEnd, int dist) {
        int left = 0, right = leftEnd;
        rightEnd += leftEnd;

        while(right < rightEnd) {
            if(left == leftEnd || this.grail.compare(arr[pos + left], arr[pos + right]) > 0) {
                arr[pos + (dist++)] = arr[pos + (right++)];
            }
            else arr[pos + (dist++)] = arr[pos + (left++)];
        }
        if(dist != left) {
            while(left < leftEnd) arr[pos + (dist++)] = arr[pos + (left++)];
        }
    }

    // arr - starting array. arr[0 - regBlockLen..-1] - buffer (if havebuf).
    // regBlockLen - length of regular blocks. First blockCount blocks are stable sorted by 1st elements and key-coded
    // keysPos - where keys are in array, in same order as blocks. keysPos < midkey means stream A
    // aBlockCount are regular blocks from stream A.
    // lastLen is length of last (irregular) block from stream B, that should go before aBlockCount blocks.
    // lastLen = 0 requires aBlockCount = 0 (no irregular blocks). lastLen > 0, aBlockCount = 0 is possible.
    private void grailMergeBuffersLeftWithXBuf(SortType[] arr, int keysPos, int midkey, int pos, int blockCount,
                                                      int regBlockLen, int aBlockCount, int lastLen) {

        if(blockCount == 0) {
            int aBlocksLen = aBlockCount * regBlockLen;
           
            grailMergeLeftWithXBuf(arr, pos, aBlocksLen, lastLen, 0 - regBlockLen);
            return;
        }

        int leftOverLen, processIndex;
        leftOverLen = processIndex = regBlockLen;
        
        int leftOverFrag = this.grail.compare(arr[keysPos], arr[midkey]) < 0 ? 0 : 1;
        int restToProcess;
        
        for(int keyIndex = 1; keyIndex < blockCount; keyIndex++, processIndex += regBlockLen) {
            restToProcess = processIndex - leftOverLen;
            int nextFrag = this.grail.compare(arr[keysPos + keyIndex], arr[midkey]) < 0 ? 0 : 1;

            if(nextFrag == leftOverFrag) {
                System.arraycopy(arr, pos + restToProcess, arr, pos + restToProcess - regBlockLen, leftOverLen);
                
                restToProcess = processIndex;
                leftOverLen = regBlockLen;
            } 
            else {
                GrailState results = grailSmartMergeWithXBuf(arr, pos + restToProcess, leftOverLen, leftOverFrag, regBlockLen);
                
                leftOverLen = results.getLeftOverLen();
                leftOverFrag = results.getLeftOverFrag();
            }
        }
        restToProcess = processIndex - leftOverLen;

        if(lastLen != 0) {
            if(leftOverFrag != 0) {
                System.arraycopy(arr, pos + restToProcess, arr, pos + restToProcess - regBlockLen, leftOverLen);
                
                restToProcess = processIndex;
                leftOverLen = regBlockLen * aBlockCount;
                leftOverFrag = 0;
            } 
            else leftOverLen += regBlockLen * aBlockCount;
            
            grailMergeLeftWithXBuf(arr, pos + restToProcess, leftOverLen, lastLen, 0 - regBlockLen);
        } 
        else {
            System.arraycopy(arr, pos + restToProcess, arr, pos + restToProcess - regBlockLen, leftOverLen);
        }
    }

    /***** End Sort With Extra Buffer *****/

    // build blocks of length buildLen
    // input: [-buildLen, -1] elements are buffer
    // output: first buildLen elements are buffer, blocks 2 * buildLen and last subblock sorted
    private void grailBuildBlocks(SortType[] arr, int pos, int len, int buildLen, SortType[] extbuf, int bufferPos, int extBufLen) {
        int buildBuf = buildLen < extBufLen ? buildLen : extBufLen;
        
        while((buildBuf & (buildBuf - 1)) != 0) {
            buildBuf &= buildBuf - 1;  // max power or 2 - just in case
        }

        int extraDist, part;
        
        if(buildBuf != 0) {
            System.arraycopy(arr, pos - buildBuf, extbuf, bufferPos, buildBuf);
            
            for(int dist = 1; dist < len; dist += 2) {
                extraDist = 0;
                if(this.grail.compare(arr[pos + (dist - 1)], arr[pos + dist]) > 0) extraDist = 1;
                
                arr[pos + dist - 3] = arr[pos + dist - 1 + extraDist];
                arr[pos + dist - 2] = arr[pos + dist - extraDist];
            }
            if(len % 2 != 0) arr[pos + len - 3] = arr[pos + len - 1];
            
            pos -= 2;

            for(part = 2; part < buildBuf; part *= 2) {
                int left = 0, right = len - 2 * part;
                
                while(left <= right) {
                    grailMergeLeftWithXBuf(arr, pos + left, part, part, 0 - part);
                    left += 2 * part;
                }
                
                int rest = len - left;

                if(rest > part) grailMergeLeftWithXBuf(arr, pos + left, part, rest - part, 0 - part);
                else {
                    while(left < len) arr[pos + left - part] = arr[pos + left++];
                }
                pos -= part;
            }
            System.arraycopy(extbuf, bufferPos, arr, pos + len, buildBuf);
        } 
        else {
            for(int dist = 1; dist < len; dist += 2) {
                extraDist = 0;
                if(this.grail.compare(arr[pos + (dist - 1)], arr[pos + dist]) > 0) extraDist = 1;
                
                grailSwap(arr, pos + (dist - 3), pos + (dist - 1 + extraDist));
                grailSwap(arr, pos + (dist - 2), pos + (dist - extraDist));
            }
            
            if(len % 2 != 0) grailSwap(arr, pos + (len - 1), pos + (len - 3));
            
            pos -= 2;
            part = 2;
        }

        while(part < buildLen) {
            int left = 0, right = len - 2 * part;
            
            while(left <= right) {
                grailMergeLeft(arr, pos + left, part, part, 0 - part);
                left += 2 * part;
            }
            
            int rest = len - left;
            
            if(rest > part) {
                grailMergeLeft(arr, pos + left, part, rest - part, 0 - part);
            } 
            else grailRotate(arr, pos + left - part, part, rest);
            
            pos -= part;
            part *= 2;
        }
        
        int restToBuild = len % (2 * buildLen);
        int leftOverPos = len - restToBuild;

        if(restToBuild <= buildLen) grailRotate(arr, pos + leftOverPos, restToBuild, buildLen);
        else grailMergeRight(arr, pos + leftOverPos, buildLen, restToBuild - buildLen, buildLen);

        while(leftOverPos > 0) {
            leftOverPos -= 2 * buildLen;
            grailMergeRight(arr, pos + leftOverPos, buildLen, buildLen, buildLen);
        }
    }

    // keys are on the left of arr. Blocks of length buildLen combined. We'll combine them into pairs
    // buildLen and keys are powers of 2. (2 * buildLen / regBlockLen) keys are guaranteed
    private void grailCombineBlocks(SortType[] arr, int keyPos, int pos, int len, int buildLen, int regBlockLen,
                                           boolean havebuf, SortType[] buffer, int bufferPos) {

        int combineLen = len / (2 * buildLen);
        int leftOver = len % (2 * buildLen);
        
        if(leftOver <= buildLen) {
            len -= leftOver;
            leftOver = 0;
        }

        if(buffer != null) System.arraycopy(arr, pos - regBlockLen, buffer, bufferPos, regBlockLen);

        for(int i = 0; i <= combineLen; i++) {
            if(i == combineLen && leftOver == 0) break;

            int blockPos = pos + i * 2 * buildLen;
            int blockCount = (i == combineLen ? leftOver : 2 * buildLen) / regBlockLen;

            grailInsertSort(arr, keyPos, blockCount + (i == combineLen ? 1 : 0));

            int midkey = buildLen / regBlockLen;

            for(int index = 1; index < blockCount; index++) {
                int leftIndex = index - 1;

                for(int rightIndex = index; rightIndex < blockCount; rightIndex++) {
                    int rightComp = this.grail.compare(arr[blockPos + leftIndex * regBlockLen], arr[blockPos + rightIndex * regBlockLen]);
                    
                    if(rightComp > 0 || (rightComp == 0 && this.grail.compare(arr[keyPos + leftIndex], arr[keyPos + rightIndex]) > 0)) {
                        leftIndex = rightIndex;
                    }
                }
                
                if(leftIndex != index - 1) {
                    grailMultiSwap(arr, blockPos + (index - 1) * regBlockLen, blockPos + leftIndex * regBlockLen, regBlockLen);
                    grailSwap(arr, keyPos + (index - 1), keyPos + leftIndex);
                    
                    if(midkey == index - 1 || midkey == leftIndex) {
                        midkey ^= (index - 1) ^ leftIndex;
                    }
                }
            }

            int aBlockCount, lastLen;
            aBlockCount = lastLen = 0;
            if(i == combineLen) lastLen = leftOver % regBlockLen;

            if(lastLen != 0) {
                while(aBlockCount < blockCount && this.grail.compare(arr[blockPos + blockCount * regBlockLen],
                      arr[blockPos + (blockCount - aBlockCount - 1) * regBlockLen]) < 0) {
                    
                    aBlockCount++;
                }
            }

            if(buffer != null) {
                grailMergeBuffersLeftWithXBuf(arr, keyPos, keyPos + midkey, blockPos, blockCount - aBlockCount, 
                                              regBlockLen, aBlockCount, lastLen);
            }
            else grailMergeBuffersLeft(arr, keyPos, keyPos + midkey, blockPos, blockCount - aBlockCount,
                                       regBlockLen, havebuf, aBlockCount, lastLen);
        }
        if(buffer != null) {
            for(int i = len - 1; i >= 0; i--) arr[pos + i] = arr[pos + i - regBlockLen];
            
            System.arraycopy(buffer, bufferPos, arr, pos - regBlockLen, regBlockLen);
        }
        else if(havebuf) {
            while(--len >= 0) grailSwap(arr, pos + len, pos + len - regBlockLen);
        }
    }

    private void grailLazyStableSort(SortType[] arr, int pos, int len) {
        for(int dist = 1; dist < len; dist += 2) {
            if(this.grail.compare(arr[pos + dist - 1], arr[pos + dist]) > 0) {
                grailSwap(arr, pos + (dist - 1), pos + dist);
            }
        }

        for(int part = 2; part < len; part *= 2) {
            int left = 0, right = len - 2 * part;

            while(left <= right) {
                grailMergeWithoutBuffer(arr, pos + left, part, part);
                left += 2 * part;
            }

            int rest = len - left;
            
            if(rest > part) grailMergeWithoutBuffer(arr, pos + left, part, rest - part);
        }
    }

    private void grailCommonSort(SortType[] arr, int pos, int len, SortType[] buffer, int bufferPos, int bufferLen) {
        if(len <= 16) {
            grailInsertSort(arr, pos, len);
            return;
        }
        
        int blockLen = 1;
        while(blockLen * blockLen < len) blockLen *= 2;     
        int numKeys = (len - 1) / blockLen + 1;
        int keyLength = numKeys + blockLen;
        
        grailCommonSort(arr, pos, keyLength, buffer, bufferPos, bufferLen);
        
        int keysFound = grailFindKeys(arr, pos, len, keyLength);
        
        boolean bufferEnabled = true;

        if(keysFound < keyLength) {
            if(keysFound < 4) {
                grailLazyStableSort(arr, pos, len);
                return;
            }
            
            numKeys = blockLen;
            while(numKeys > keysFound) numKeys /= 2;
            
            bufferEnabled = false;
            blockLen = 0;
        }

        int dist = blockLen + numKeys;
        int buildLen = bufferEnabled ? blockLen : numKeys;

        if(bufferEnabled) {
            grailBuildBlocks(arr, pos + dist, len - dist, buildLen, buffer, bufferPos, bufferLen);
        }
        else {
            grailBuildBlocks(arr, pos + dist, len - dist, buildLen, null, bufferPos, 0);
        }

        // 2 * buildLen are built
        while(len - dist > (buildLen *= 2)) {
            int regBlockLen = blockLen;
            boolean buildBufEnabled = bufferEnabled;

            if(!bufferEnabled) {
                if(numKeys > 4 && numKeys / 8 * numKeys >= buildLen) {
                    regBlockLen = numKeys / 2;
                    buildBufEnabled = true;
                } 
                else {
                    int calcKeys = 1;
                    int i = buildLen * keysFound / 2;
                    
                    while(calcKeys < numKeys && i != 0) {
                        calcKeys *= 2;
                        i /= 8;
                    }
                    regBlockLen = (2 * buildLen) / calcKeys;
                }
            }
            grailCombineBlocks(arr, pos, pos + dist, len - dist, buildLen, regBlockLen, buildBufEnabled,
                               buildBufEnabled && regBlockLen <= bufferLen ? buffer : null, bufferPos);
        }

        grailInsertSort(arr, pos, dist);
        grailMergeWithoutBuffer(arr, pos, dist, len - dist);
    }

    public void grailSortWithoutBuffer(SortType[] arr) {
        this.grail = new SortComparator();
        grailCommonSort(arr, 0, arr.length, null, 0, 0);
    }
    public void grailSortWithoutBuffer(SortType[] arr, SortComparator cmp) {
        this.grail = cmp;
        grailCommonSort(arr, 0, arr.length, null, 0, 0);
    }

    public void grailSortWithBuffer(SortType[] arr) {
        this.grail = new SortComparator();
        SortType[] ExtBuf = new SortType[this.grailStaticBufferLen];
        grailCommonSort(arr, 0, arr.length, ExtBuf, 0, this.grailStaticBufferLen);
    }
    public void grailSortWithBuffer(SortType[] arr, SortComparator cmp) {
        this.grail = cmp;
        SortType[] ExtBuf = new SortType[this.grailStaticBufferLen];
        grailCommonSort(arr, 0, arr.length, ExtBuf, 0, this.grailStaticBufferLen);
    }

    public void grailSortWithDynBuffer(SortType[] arr) {
        this.grail = new SortComparator();
        int tempLen = 1;
        while(tempLen * tempLen < arr.length) tempLen *= 2;
        SortType[] ExtBuf = new SortType[tempLen];
        grailCommonSort(arr, 0, arr.length, ExtBuf, 0, tempLen);
    }
    public void grailSortWithDynBuffer(SortType[] arr, SortComparator cmp) {
        this.grail = cmp;
        int tempLen = 1;
        while(tempLen * tempLen < arr.length) tempLen *= 2;
        SortType[] ExtBuf = new SortType[tempLen];
        grailCommonSort(arr, 0, arr.length, ExtBuf, 0, tempLen);
    }
}