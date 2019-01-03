package javagrailsort;

import javagrailsort.SortType.SortCmp;

public class GrailSort {

	/********* Grail sorting *********************************/
	/*                                                       */
	/* (c) 2013 by Andrey Astrelin                           */
	/*                                                       */
	/*                                                       */
	/* Stable sorting that works in O(N*log(N)) worst time   */
	/* and uses O(1) extra memory                            */
	/*                                                       */
	/* Define int and SORT_CMP                               */
	/* and then call GrailSort() function                    */
	/*                                                       */
        /*			                                 */
	/*                                                       */
	/* Also classic in-place merge sort is implemented       */
	/* under the name of RecStableSort()                     */
	/*                                                       */
	/*********************************************************/
	
	final private static int grailExternalBufferLength = 512;
	
	private static int LREST;
	private static int FREST;
	
	private static SortCmp javaGrailSort;
	
	private static void grailSwap1(SortType[] arr, int a, int b) {
		SortType c = arr[a];
		arr[a] = arr[b];
		arr[b] = c;
	}
	
	private static void grailSwapN(SortType[] arr, int a, int b, int n) {
		while(n != 0) { 
			grailSwap1(arr, a++, b++);
			n--;
		}
	}
	
	private static void grailRotate(SortType[] arr, int a, int l1, int l2) {
		while(l1 != 0 && l2 != 0) {
			if(l1 <= l2) {
				grailSwapN(arr, a, a + l1, l1);
				a += l1; 
				l2 -= l1;
			} 
			else {
				grailSwapN(arr, a + (l1 - l2), a + l1, l2);
				l1 -= l2;
			}
		}
	}
	
	private static void grailInsertSort(SortType[] arr, int arrPtr, int len) {
		int i, j;
		for(i = 1; i < len; i++){
			for(j = i - 1; j >= 0 && javaGrailSort.compare(arr[arrPtr + j + 1], arr[arrPtr + j]) < 0; j--) {
				grailSwap1(arr, arrPtr + j, arrPtr + j + 1);
			}
		}
	}

	private static int grailBinSearchLeft(SortType[] arr, int arrPtr, int len, int keyPtr) {
		int a = -1, b = len, c;
		while(a < b - 1) {
			c = a + ((b - a) >> 1);
			if(javaGrailSort.compare(arr[arrPtr + c], arr[keyPtr]) >= 0) {
				b = c;
			}
			else a = c;
		}
		return b;
	}
	private static int grailBinSearchRight(SortType[] arr, int arrPtr, int len, int keyPtr) {
		int a = -1, b = len, c;
		while(a < b - 1) {
			c = a + ((b - a) >> 1);
			if(javaGrailSort.compare(arr[arrPtr + c], arr[keyPtr]) > 0) {
				b = c;
			}
			else a = c;
		}
		return b;
	}

	// cost: 2 * len + nk^2 / 2
	private static int grailFindKeys(SortType[] arr, int arrPtr, int len, int nkeys) {
		int h = 1, h0 = 0;  // first key is always here
		int u = 1, r;
		while(u < len && h < nkeys) {
			r = grailBinSearchLeft(arr, arrPtr + h0, h, arrPtr + u);
			if(r == h || javaGrailSort.compare(arr[arrPtr + u], arr[arrPtr + (h0 + r)]) != 0) {
				grailRotate(arr, arrPtr + h0, h, u - (h0 + h));
				h0 = u - h;
				grailRotate(arr, arrPtr + (h0 + r), h - r, 1);
				h++;
			}
			u++;
		}
		grailRotate(arr, arrPtr, h0, h);
		return h;
	}

	// cost: min(L1 , L2)^2 + max(L1, L2)
	private static void grailMergeWithoutBuffer(SortType[] arr, int arrPtr, int len1, int len2) {
		int h;
		if(len1 < len2) {
			while(len1 != 0) {
				h = grailBinSearchLeft(arr, arrPtr + len1, len2, arrPtr);
				if(h != 0) {
					grailRotate(arr, arrPtr, len1, h);
					arrPtr += h;
					len2 -= h;
				}
				if(len2 == 0) break;
				do {
					arrPtr++;
					len1--;
				} while(len1 != 0 && javaGrailSort.compare(arr[arrPtr], arr[arrPtr + len1]) <= 0);
			}
		} else{
			while(len2 != 0) {
				h = grailBinSearchRight(arr, arrPtr, len1, arrPtr + (len1 + len2 - 1));
				if(h != len1) {
					grailRotate(arr, arrPtr + h, len1 - h, len2);
					len1 = h;
				}
				if(len1 == 0) break;
				do {
					len2--;
				} while(len2 != 0 && javaGrailSort.compare(arr[arrPtr + len1 - 1], arr[arrPtr + len1 + len2 - 1]) <= 0);
			}
		}
	}

	// arr[M..-1] - buffer, arr[0, L1 - 1] ++ arr[L1, L1 + L2 - 1] -> arr[M, M + L1 + L2 - 1]
	private static void grailMergeLeft(SortType[] arr, int arrPtr, int L1, int L2, int M) {
		int p0 = 0;
		int p1 = L1;
		
		L2 += L1;
		
		while(p1 < L2) {
			if(p0 == L1 || javaGrailSort.compare(arr[arrPtr + p0], arr[arrPtr + p1]) > 0) {
				grailSwap1(arr, arrPtr + (M++), arrPtr + (p1++));
			} else {
				grailSwap1(arr, arrPtr + (M++), arrPtr + (p0++));
			}
		}
		if(M != p0) grailSwapN(arr, arrPtr + M, arrPtr + p0, L1 - p0);
	}
	private static void grailMergeRight(SortType[] arr, int arrPtr, int L1, int L2, int M) {
		int p0 = L1 + L2 + M - 1;
		int p2 = L1 + L2 -1;
		int p1 = L1 - 1;

		while(p1 >= 0) {
			if(p2 < L1 || javaGrailSort.compare(arr[arrPtr + p1], arr[arrPtr + p2]) > 0) {
				grailSwap1(arr, arrPtr + (p0--), arrPtr + (p1--));
			} else {
				grailSwap1(arr, arrPtr + (p0--), arrPtr + (p2--));
			}
		}
		if(p2 != p0) {
			while(p2 >= L1) grailSwap1(arr, arrPtr + (p0--), arrPtr + (p2--));
		}
	}

	private static void grailSmartMergeWithBuffer(SortType[] arr, int arrPtr, int len2, int lkeys){
		int p0 = -lkeys, p1 = 0, p2 = LREST, q1 = p2, q2 = p2 + len2;
		int ftype = 1 - FREST;  // 1 if inverted
		
		while(p1 < q1 && p2 < q2) {
			if(javaGrailSort.compare(arr[arrPtr + p1], arr[arrPtr + p2]) - ftype < 0) {
				grailSwap1(arr, arrPtr + (p0++), arrPtr + (p1++));
			}
			else grailSwap1(arr, arrPtr + (p0++), arrPtr + (p2++));
		}
		if(p1 < q1) {
			LREST = q1 - p1;
			while(p1 < q1) grailSwap1(arr, arrPtr + (--q1), arrPtr + (--q2));
		} else {
			LREST = q2 - p2;
			FREST = ftype;
		}
	}
	private static void grailSmartMergeWithoutBuffer(SortType[] arr, int arrPtr, int Len2){
		int len1, len2, ftype, h;
		
		if(Len2 == 0) return;
		
		len1 = LREST;
		len2 = Len2;
		ftype = 1 - FREST;
		
		if(len1 != 0 && javaGrailSort.compare(arr[arrPtr + (len1 - 1)], arr[arrPtr + len1]) - ftype >= 0) {
			while(len1 != 0) {
				if (ftype != 0)
					h = grailBinSearchLeft(arr, arrPtr + len1, len2, arrPtr);
				else
					h = grailBinSearchRight(arr, arrPtr + len1, len2, arrPtr);
				if(h != 0) {
					grailRotate(arr, arrPtr, len1, h);
					arrPtr += h;
					len2 -= h;
				}
				if(len2 == 0){
					LREST = len1;
					return;
				}
				do {
					arrPtr++;
					len1--;
				} while(len1 != 0 && javaGrailSort.compare(arr[arrPtr], arr[arrPtr + len1]) - ftype < 0);
			}
		}
		LREST = len2;
		FREST = ftype;
	}

	/***** Sort With Extra Buffer *****/

	// arr[M..-1] - free, arr[0, L1 - 1] ++ arr[L1, L1 + L2 - 1] -> arr[M, M + L1 + L2 - 1]
	private static void grailMergeLeftWithXBuf(SortType[] arr, int arrPtr, int L1, int L2, int M) {
		int p0 = 0;
		int p1 = L1;
		L2 += L1;
		
		while(p1 < L2) {
			if(p0 == L1 || javaGrailSort.compare(arr[arrPtr + p0], arr[arrPtr + p1]) > 0) {
				arr[arrPtr + (M++)] = arr[arrPtr + (p1++)];
			}
			else arr[arrPtr + (M++)] = arr[arrPtr + (p0++)];
		}
		if(M != p0) {
			while(p0 < L1) arr[arrPtr + (M++)] = arr[arrPtr + (p0++)];
		}
	}

	private static void grailSmartMergeWithXBuf(SortType[] arr, int arrPtr, int len2, int lkeys) {
		int p0 = -lkeys, p1 = 0, p2 = LREST, q1 = p2, q2 = p2 + len2;
		int ftype = 1 - FREST;  // 1 if inverted
		
		while(p1 < q1 && p2 < q2) {
			if(javaGrailSort.compare(arr[arrPtr + p1], arr[arrPtr + p2]) - ftype < 0) {
				arr[arrPtr + (p0++)] = arr[arrPtr + (p1++)];
			}
			else arr[arrPtr + (p0++)] = arr[arrPtr + (p2++)];
		}
		if(p1 < q1) {
			LREST = q1 - p1;
			while(p1 < q1) arr[arrPtr + (--q2)] = arr[arrPtr + (--q1)];
		} else {
			LREST = q2 - p2;
			FREST = ftype;
		}
	}

	// arr - starting array. arr[-lblock..-1] - buffer (if havebuf).
	// lblock - length of regular blocks. First nblocks are stable sorted by 1st elements and key-coded
	// keys - arrays of keys, in same order as blocks. key < midkey means stream A
	// nblock2 are regular blocks from stream A. llast is length of last (irregular) block from stream B, that should go before nblock2 blocks.
	// llast = 0 requires nblock2 = 0 (no irregular blocks). llast > 0, nblock2 = 0 is possible.
	private static void grailMergeBuffersLeftWithXBuf(SortType[] arr, int keysPtr, int midkey, int arrPtr,
			                                      int nblock, int lblock, int nblock2, int llast) {
		
		int l, prest, lrest, frest, pidx, cidx, fnext;

		if(nblock == 0) {
			l = nblock2 * lblock;
			grailMergeLeftWithXBuf(arr, arrPtr, l, llast, -lblock);
			return;
		}

		lrest = lblock;
		frest = javaGrailSort.compare(arr[keysPtr], arr[midkey]) < 0 ? 0 : 1;
		pidx = lblock;
		
		for(cidx = 1; cidx < nblock; cidx++, pidx += lblock) {
			prest = pidx - lrest;
			fnext = javaGrailSort.compare(arr[keysPtr + cidx], arr[midkey]) < 0 ? 0 : 1;
			
			if(fnext == frest) {
				System.arraycopy(arr, arrPtr + prest, arr, arrPtr + prest - lblock, lrest);
				prest = pidx;
				lrest = lblock;
			} else {
				LREST = lrest;
				FREST = frest;
				grailSmartMergeWithXBuf(arr, arrPtr + prest, lblock, lblock);
				lrest = LREST; 
				frest = FREST;
			}
		}
		prest = pidx - lrest;
		
		if(llast != 0) {
			if(frest != 0) {
				System.arraycopy(arr, arrPtr + prest, arr, arrPtr + prest - lblock, lrest);
				prest = pidx;
				lrest = lblock * nblock2;
				frest = 0;
			} else {
				lrest += lblock * nblock2;
			}
			grailMergeLeftWithXBuf(arr, arrPtr + prest, lrest, llast, -lblock);
		} else {
			System.arraycopy(arr, arrPtr + prest, arr, arrPtr + prest - lblock, lrest);
		}
	}

	/***** End Sort With Extra Buffer *****/

	// build blocks of length K
	// input: [-K, -1] elements are buffer
	// output: first K elements are buffer, blocks 2 * K and last subblock sorted
	private static void grailBuildBlocks(SortType[] arr, int arrPtr, int L, int K, 
			                                 SortType[] extbuf, int bufPtr, int LExtBuf) {
		
		int m, u, h, p0, p1, rest, restk, p, kbuf;
		
		kbuf = K < LExtBuf ? K : LExtBuf;
		while((kbuf & (kbuf - 1)) != 0) kbuf &= kbuf - 1;  // max power or 2 - just in case

		if(kbuf != 0) {
			System.arraycopy(arr, arrPtr - kbuf, extbuf, bufPtr, kbuf);
			for(m = 1; m < L; m += 2) {
				u = 0;
				if(javaGrailSort.compare(arr[arrPtr + (m - 1)], arr[arrPtr + m]) > 0) u = 1;
				arr[arrPtr + m - 3] = arr[arrPtr + m - 1 + u];
				arr[arrPtr + m - 2] = arr[arrPtr + m - u];
			}
			if(L % 2 != 0) arr[arrPtr + L - 3] = arr[arrPtr + L - 1];
			arrPtr -= 2;
			
			for(h = 2; h < kbuf; h *= 2) {
				p0 = 0;
				p1 = L - 2 * h;
				while(p0 <= p1) {
					grailMergeLeftWithXBuf(arr, arrPtr + p0, h, h, -h);
					p0 += 2 * h;
				}
				rest = L - p0;
				
				if(rest > h) {
					grailMergeLeftWithXBuf(arr, arrPtr + p0, h, rest - h, -h);
				} else {
					for(; p0 < L; p0++)	arr[arrPtr + p0 - h] = arr[arrPtr + p0];
				}
				arrPtr -= h;
			}
			System.arraycopy(arr, arrPtr + L, extbuf, bufPtr, kbuf);
		} else {
			for(m = 1; m < L; m += 2) {
				u = 0;
				if(javaGrailSort.compare(arr[arrPtr + (m - 1)], arr[arrPtr + m]) > 0) u = 1;
				grailSwap1(arr, arrPtr + (m - 3), arrPtr + (m - 1 + u));
				grailSwap1(arr, arrPtr + (m - 2), arrPtr + (m - u));
			}
			if(L % 2 != 0) grailSwap1(arr, arrPtr + (L - 1), arrPtr + (L - 3));
			arrPtr -= 2;
			h = 2;
		}
		
		for(; h < K; h *= 2) {
			p0 = 0;
			p1 = L - 2 * h;
			while(p0 <= p1) {
				grailMergeLeft(arr, arrPtr + p0, h, h, -h);
				p0 += 2 * h;
			}
			rest = L - p0;
			if(rest > h) {
				grailMergeLeft(arr, arrPtr + p0, h, rest - h, -h);
			} else {
				grailRotate(arr, arrPtr + p0 - h, h, rest);
			}
			arrPtr -= h;
		}
		restk = L % (2 * K);
		p = L - restk;
		
		if(restk <= K) grailRotate(arr, arrPtr + p, restk, K);
		else grailMergeRight(arr, arrPtr + p, K, restk - K, K);
		
		while(p > 0){
			p -= 2 * K;
			grailMergeRight(arr, arrPtr + p, K, K, K);
		}
	}

	// arr - starting array. arr[-lblock..-1] - buffer (if havebuf).
	// lblock - length of regular blocks. First nblocks are stable sorted by 1st elements and key-coded
	// keys - arrays of keys, in same order as blocks. key < midkey means stream A
	// nblock2 are regular blocks from stream A. llast is length of last (irregular) block from stream B, that should go before nblock2 blocks.
	// llast = 0 requires nblock2 = 0 (no irregular blocks). llast > 0, nblock2 = 0 is possible.
	private static void grailMergeBuffersLeft(SortType[] arr, int keysPtr, int midkey, int arrPtr, 
			                                      int nblock, int lblock, boolean havebuf, int nblock2, 
			                                      int llast) {
		
		int l, prest, lrest, frest, pidx, cidx, fnext;
		
		if(nblock == 0) {
			l = nblock2 * lblock;
			if(havebuf) grailMergeLeft(arr, arrPtr, l, llast, -lblock);
			else grailMergeWithoutBuffer(arr, arrPtr, l, llast);
			return;
		}

		lrest = lblock;
		frest = javaGrailSort.compare(arr[keysPtr], arr[midkey]) < 0 ? 0 : 1;
		pidx = lblock;
		
		for(cidx = 1; cidx < nblock; cidx++, pidx += lblock) {
			prest = pidx - lrest;
			fnext = javaGrailSort.compare(arr[keysPtr + cidx], arr[midkey]) < 0 ? 0 : 1;
			
			if(fnext == frest) {
				if(havebuf) grailSwapN(arr, arrPtr + prest - lblock, arrPtr + prest, lrest);
				prest = pidx;
				lrest = lblock;
			} else {
				if(havebuf){
					LREST = lrest;
					FREST = frest;
					grailSmartMergeWithBuffer(arr, arrPtr + prest, lblock, lblock);
					lrest = LREST;
					frest = FREST;
				} else{
					LREST = lrest;
					FREST = frest;
					grailSmartMergeWithoutBuffer(arr, arrPtr + prest, lblock);
					lrest = LREST;
					frest = FREST;
				}
			}
		}
		prest = pidx - lrest;
		
		if(llast != 0) {
			if(frest != 0) {
				if(havebuf) grailSwapN(arr, arrPtr + prest - lblock, arrPtr + prest, lrest);
				prest = pidx;
				lrest = lblock * nblock2;
				frest = 0;
			} else {
				lrest += lblock * nblock2;
			}
			if(havebuf) grailMergeLeft(arr, arrPtr + prest, lrest, llast, -lblock);
			else grailMergeWithoutBuffer(arr, arrPtr + prest, lrest, llast);
		} else {
			if(havebuf) grailSwapN(arr, arrPtr + prest, arrPtr + (prest - lblock), lrest);
		}
	}

	private static void grailLazyStableSort(SortType[] arr, int arrPtr, int L){
		int m, h, p0, p1, rest;
		
		for(m = 1; m < L; m += 2){
			if(javaGrailSort.compare(arr[arrPtr + m - 1], arr[arrPtr + m]) > 0) {
				grailSwap1(arr, arrPtr + (m - 1), arrPtr + m);
			}
		}
		
		for(h = 2; h < L; h *= 2){
			p0 = 0;
			p1 = L - 2 * h;
			
			while(p0 <= p1) {
				grailMergeWithoutBuffer(arr, arrPtr + p0, h, h);
				p0 += 2 * h;
			}
			
			rest = L - p0;
			if(rest > h) {
				grailMergeWithoutBuffer(arr, arrPtr + p0, h, rest - h);
			}
		}
	}

	// keys are on the left of arr. Blocks of length LL combined. We'll combine them in pairs
	// LL and nkeys are powers of 2. (2 * LL / lblock) keys are guaranteed
	private static void grailCombineBlocks(SortType[] arr, int keysPtr, int arrPtr, int len, int LL,
			                                    int lblock, boolean havebuf, SortType[] xbuf, int bufPtr){
		
		int M, b, NBlk, midkey, lrest, u, p, v, kc, nbl2, llast;
		int arr1;
		
		M = len / (2 * LL);
		lrest = len % (2 * LL);
		if(lrest <= LL){
			len -= lrest;
			lrest = 0;
		}
		
		if(xbuf != null) System.arraycopy(arr, arrPtr - lblock, xbuf, bufPtr, lblock);
		
		for(b = 0; b <= M; b++){
			if(b == M && lrest == 0) break;
			
			arr1 = arrPtr + b * 2 * LL;
			NBlk = (b == M ? lrest : 2 * LL) / lblock;
			
			grailInsertSort(arr, keysPtr, NBlk + (b == M ? 1 : 0));
			
			midkey = LL / lblock;
			
			for(u = 1; u < NBlk; u++){
				p = u - 1;
				
				for(v = u; v < NBlk; v++){
					kc = javaGrailSort.compare(arr[arr1 + p * lblock], arr[arr1 + v * lblock]);
					if(kc > 0 || (kc == 0 && javaGrailSort.compare(arr[keysPtr + p], arr[keysPtr + v]) > 0)) {
						p = v;
					}
				}
				if(p != u - 1){
					grailSwapN(arr, arr1 + (u - 1) * lblock, arr1 + p * lblock, lblock);
					grailSwap1(arr, keysPtr + (u - 1), keysPtr + p);
					if(midkey == u - 1 || midkey == p) {
						midkey ^= (u - 1) ^ p;
					}
				}
			}
			
			nbl2 = llast = 0;
			if(b == M) llast = lrest % lblock;
			
			if(llast != 0) {
				while(nbl2 < NBlk && javaGrailSort.compare(arr[arr1 + NBlk * lblock],
								arr[arr1 + (NBlk - nbl2 - 1) * lblock]) < 0) {
					nbl2++;
				}
			}
			
			if(xbuf != null) {
				grailMergeBuffersLeftWithXBuf(arr, keysPtr, keysPtr + midkey, arr1,
						                           NBlk - nbl2, lblock, nbl2, llast);
			}
			else grailMergeBuffersLeft(arr, keysPtr, keysPtr + midkey, arr1, 
					                     NBlk - nbl2, lblock, havebuf, nbl2, llast);
		}
		if(xbuf != null){
			for(p = len; --p >= 0;) arr[arrPtr + p] = arr[arrPtr + p - lblock];
			System.arraycopy(xbuf, bufPtr, arr, arrPtr - lblock, lblock);
		}
		else if(havebuf) {
			while(--len >= 0) grailSwap1(arr, arrPtr + len, arrPtr + len - lblock);
		}
	}

	private static void grailCommonSort(SortType[] arr, int arrPtr, int Len, 
			                             SortType[] extbuf, int bufPtr, int LExtBuf) {
		
		int lblock, nkeys, findkeys, ptr, cbuf, lb, nk;
		boolean havebuf, chavebuf;
		int s;
		
		if(Len < 16){
			grailInsertSort(arr, arrPtr, Len);
			return;
		}

		lblock = 1;
		while(lblock * lblock < Len) lblock *= 2;
		
		nkeys = (Len - 1) / lblock + 1;
		findkeys = grailFindKeys(arr, arrPtr, Len, nkeys + lblock);
		havebuf = true;
		
		if(findkeys < nkeys + lblock) {
			if(findkeys < 4){
				grailLazyStableSort(arr, arrPtr, Len);
				return;
			}
			nkeys = lblock;
			while(nkeys > findkeys) nkeys /= 2;
			havebuf = false;
			lblock = 0;
		}
		
		ptr = lblock + nkeys;
		cbuf = havebuf ? lblock : nkeys;
		
		if(havebuf) {
			grailBuildBlocks(arr, arrPtr + ptr, Len - ptr, cbuf, extbuf, bufPtr, LExtBuf);
		}
		else{
			grailBuildBlocks(arr, arrPtr + ptr, Len - ptr, cbuf, null, bufPtr, 0);
		}

		// 2 * cbuf are built
		while(Len - ptr > (cbuf *= 2)) {
			lb = lblock;
			chavebuf = havebuf;
			
			if(!havebuf){
				if(nkeys > 4 && nkeys / 8 * nkeys >= cbuf) {
					lb = nkeys / 2;
					chavebuf = true;
				} else {
					nk = 1;
					s = cbuf * findkeys / 2;
					while(nk < nkeys && s != 0){
						nk *= 2;
						s /= 8;
					}
					lb = (2 * cbuf) / nk;
				}
			}
			grailCombineBlocks(arr, arrPtr, arrPtr + ptr, Len - ptr, cbuf, lb, chavebuf, 
					                chavebuf && lb <= LExtBuf ? extbuf : null, bufPtr);
		}
		
		grailInsertSort(arr, arrPtr, ptr);
		grailMergeWithoutBuffer(arr, arrPtr, ptr, Len - ptr);
	}

	public static void grailSortWithoutBuffer(SortType[] arr, SortCmp compareType){
		javaGrailSort = compareType;
		
		grailCommonSort(arr, 0, arr.length, null, 0, 0);
	}
	
	public static void grailSortWithBuffer(SortType[] arr, SortCmp compareType){
		javaGrailSort = compareType;
		
		SortType[] ExtBuf = new SortType[grailExternalBufferLength];
		grailCommonSort(arr, 0, arr.length, ExtBuf, 0, grailExternalBufferLength);
	}
	
	public static void grailSortWithDynBuffer(SortType[] arr, SortCmp compareType){
		javaGrailSort = compareType;
		
		int L = 1;
		while(L * L < arr.length) L *= 2;
		SortType[] ExtBuf = new SortType[L];
		grailCommonSort(arr, 0, arr.length, ExtBuf, 0, L);
	}
}
