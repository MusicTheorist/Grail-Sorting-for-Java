package wip;

import java.util.Arrays;

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
    /*							 							 */
	/*                                                       */
	/* Also classic in-place merge sort is implemented       */
	/* under the name of RecStableSort()                     */
	/*                                                       */
	/*********************************************************/

	final private static int GRAIL_EXT_BUFFER_LENGTH = 512;

	private static int SORT_CMP(double a, double b){
		if(a < b) return -1;
		if(a > b) return 1;
		return 0;
	}
	
	private static void grail_swap1(double[] arr, int a, int b){
		double c = arr[a];
		arr[a] = arr[b];
		arr[b] = c;
	}
	
	private static void grail_swapN(double[] arr, int a, int b, int n){
		while(n != 0) { 
			grail_swap1(arr,a++,b++);
			n--;
		}
	}
	
	private static void grail_rotate(double[] arr, int a, int l1, int l2){
		while(l1 != 0 && l2 != 0){
			if(l1 <= l2){
				grail_swapN(arr, a, a+l1, l1);
				a += l1; 
				l2 -= l1;
			} 
			else {
				grail_swapN(arr, a + (l1 - l2), a + l1, l2);
				l1 -= l2;
			}
		}
	}
	
	public static void grailSet(double[] arr, int i, double j) {
        arr[i] = j;
    }
	
	private static void grail_SortIns(double[] arr,int arrPtr,int len){
		int i,j;
		for(i=1;i<len;i++){
			for(j=i-1;j>=0 && SORT_CMP(arr[arrPtr+j+1],arr[arrPtr+j])<0;j--) grail_swap1(arr,arrPtr+j,arrPtr+j+1);
		}
	}

	private static int grail_BinSearchLeft(double[] arr,int arrPtr,int len,int key){
		int a=-1,b=len,c;
		while(a<b-1){
			c=a+((b-a)>>1);
			if(SORT_CMP(arr[arrPtr+c],arr[key])>=0) b=c;
			else a=c;
		}
		return b;
	}
	private static int grail_BinSearchRight(double[] arr,int arrPtr,int len,int key){
		int a=-1,b=len,c;
		while(a<b-1){
			c=a+((b-a)>>1);
			if(SORT_CMP(arr[arrPtr+c],arr[key])>0) b=c;
			else a=c;
		}
		return b;
	}

	// cost: 2*len+nk^2/2
	private static int grail_FindKeys(double[] arr,int arrPtr,int len,int nkeys){
		int h=1,h0=0;  // first key is always here
		int u=1,r;
		while(u<len && h<nkeys){
			r=grail_BinSearchLeft(arr,arrPtr+h0,h,arrPtr+u);
			if(r==h || SORT_CMP(arr[arrPtr+u],arr[arrPtr+(h0+r)])!=0){
				grail_rotate(arr,arrPtr+h0,h,u-(h0+h));
				h0=u-h;
				grail_rotate(arr,arrPtr+(h0+r),h-r,1);
				h++;
			}
			u++;
		}
		grail_rotate(arr,arrPtr,h0,h);
		return h;
	}

	// cost: min(L1,L2)^2+max(L1,L2)
	private static void grail_MergeWithoutBuffer(double[] arr,int arrPtr,int len1,int len2){
		int h;
		if(len1<len2){
			while(len1!=0){
				h=grail_BinSearchLeft(arr,arrPtr+len1,len2,arrPtr);
				if(h!=0){
					grail_rotate(arr,arrPtr,len1,h);
					arrPtr+=h;
					len2-=h;
				}
				if(len2==0) break;
				do{
					arrPtr++; len1--;
				} while(len1!=0 && SORT_CMP(arr[arrPtr],arr[arrPtr+len1])<=0);
			}
		} else{
			while(len2!=0){
				h=grail_BinSearchRight(arr,arrPtr,len1,arrPtr+(len1+len2-1));
				if(h!=len1){
					grail_rotate(arr,arrPtr+h,len1-h,len2);
					len1=h;
				}
				if(len1==0) break;
				do{
					len2--;
				} while(len2!=0 && SORT_CMP(arr[arrPtr+len1-1],arr[arrPtr+len1+len2-1])<=0);
			}
		}
	}

	// arr[M..-1] - buffer, arr[0,L1-1]++arr[L1,L1+L2-1] -> arr[M,M+L1+L2-1]
	private static void grail_MergeLeft(double[] arr,int arrPtr,int L1,int L2,int M){
		int p0=0;
		int p1=L1;
		L2+=L1;
		
		while(p1<L2){
			if(p0==L1 || SORT_CMP(arr[arrPtr+p0],arr[arrPtr+p1])>0){
				grail_swap1(arr,arrPtr+(M++),arrPtr+(p1++));
			} else{
				grail_swap1(arr,arrPtr+(M++),arrPtr+(p0++));
			}
		}
		if(M!=p0) grail_swapN(arr,arrPtr+M,arrPtr+p0,L1-p0);
	}
	private static void grail_MergeRight(double[] arr,int arrPtr,int L1,int L2,int M){
		int p0=L1+L2+M-1;
		int p2=L1+L2-1;
		int p1=L1-1;

		while(p1>=0){
			if(p2<L1 || SORT_CMP(arr[arrPtr+p1],arr[arrPtr+p2])>0){
				grail_swap1(arr,arrPtr+(p0--),arrPtr+(p1--));
			} else{
				grail_swap1(arr,arrPtr+(p0--),arrPtr+(p2--));
			}
		}
		if(p2!=p0) while(p2>=L1) grail_swap1(arr,arrPtr+(p0--),arrPtr+(p2--));
	}

	private static void grail_SmartMergeWithBuffer(double[] arr,int arrPtr,int alen1,int atype,int len2,int lkeys){
		int p0=-lkeys,p1=0,p2=alen1,q1=p2,q2=p2+len2;
		int ftype=1-atype;  // 1 if inverted
		while(p1<q1 && p2<q2){
			if(SORT_CMP(arr[arrPtr+p1],arr[arrPtr+p2])-ftype<0) grail_swap1(arr,arrPtr+(p0++),arrPtr+(p1++));
			else grail_swap1(arr,arrPtr+(p0++),arrPtr+(p2++));
		}
		if(p1<q1){
			alen1=q1-p1;
			while(p1<q1) grail_swap1(arr,arrPtr+(--q1),arrPtr+(--q2));
		} else{
			alen1=q2-p2;
			atype=ftype;
		}
	}
	private static void grail_SmartMergeWithoutBuffer(double[]arr,int arrPtr,int alen1,int atype,int _len2){
		int len1,len2,ftype,h;
		
		if(_len2 == 0) return;
		len1=alen1;
		len2=_len2;
		ftype=1-atype;
		if(len1 != 0 && SORT_CMP(arr[arrPtr+(len1-1)],arr[arrPtr+len1])-ftype>=0){
			while(len1!=0){
				if (ftype!=0)
					h = grail_BinSearchLeft(arr,arrPtr+len1,len2,arrPtr);
				else
					h = grail_BinSearchRight(arr,arrPtr+len1,len2,arrPtr);
				if(h!=0){
					grail_rotate(arr,arrPtr,len1,h);
					arrPtr+=h;
					len2-=h;
				}
				if(len2==0){
					alen1=len1;
					return;
				}
				do{
					arrPtr++; len1--;
				} while(len1!=0 && SORT_CMP(arr[arrPtr],arr[arrPtr+len1])-ftype<0);
			}
		}
		alen1=len2; atype=ftype;
	}

	/***** Sort With Extra Buffer *****/

	// arr[M..-1] - free, arr[0,L1-1]++arr[L1,L1+L2-1] -> arr[M,M+L1+L2-1]
	private static void grail_MergeLeftWithXBuf(double[] arr,int arrPtr,int L1,int L2,int M){
		int p0=0,p1=L1; L2+=L1;
		while(p1<L2){
			if(p0==L1 || SORT_CMP(arr[arrPtr+p0],arr[arrPtr+p1])>0) grailSet(arr,arrPtr+(M++),arrPtr+(p1++));
			else grailSet(arr,arrPtr+(M++),arrPtr+(p0++));
		}
		if(M!=p0) while(p0<L1) grailSet(arr,arrPtr+(M++),arrPtr+(p0++));
	}

	private static void grail_SmartMergeWithXBuf(double[] arr,int arrPtr,int alen1,int atype,int len2,int lkeys){
		int p0=-lkeys,p1=0,p2=alen1,q1=p2,q2=p2+len2;
		int ftype=1-atype;  // 1 if inverted
		while(p1<q1 && p2<q2){
			if(SORT_CMP(arr[arrPtr+p1],arr[arrPtr+p2])-ftype<0) grailSet(arr,arrPtr+(p0++),arrPtr+(p1++));
			else grailSet(arr,arrPtr+(p0++),arrPtr+(p2++));
		}
		if(p1<q1){
			alen1=q1-p1;
			while(p1<q1) grailSet(arr,arrPtr+(--q2),arrPtr+(--q1));
		} else{
			alen1=q2-p2;
			atype=ftype;
		}
	}

	// arr - starting array. arr[-lblock..-1] - buffer (if havebuf).
	// lblock - length of regular blocks. First nblocks are stable sorted by 1st elements and key-coded
	// keys - arrays of keys, in same order as blocks. key<midkey means stream A
	// nblock2 are regular blocks from stream A. llast is length of last (irregular) block from stream B, that should go before nblock2 blocks.
	// llast=0 requires nblock2=0 (no irregular blocks). llast>0, nblock2=0 is possible.
	static void grail_MergeBuffersLeftWithXBuf(double[] arr,int keysPtr,int midkey,int arrPtr,int nblock,int lblock,int nblock2,int llast){
		int l,prest,lrest,frest,pidx,cidx,fnext;

		if(nblock==0){
			l=nblock2*lblock;
			grail_MergeLeftWithXBuf(arr,arrPtr,l,llast,-lblock);
			return;
		}

		lrest=lblock;
		frest=SORT_CMP(arr[keysPtr],arr[midkey])<0 ? 0 : 1;
		pidx=lblock;
		for(cidx=1;cidx<nblock;cidx++,pidx+=lblock){
			prest=pidx-lrest;
			fnext=SORT_CMP(arr[keysPtr+cidx],arr[midkey])<0 ? 0 : 1;
			if(fnext==frest){
				for(int i = 0; i < lrest; i++) {
					grailSet(arr,arrPtr+prest-lblock,arrPtr+prest);
				}
				prest=pidx;
				lrest=lblock;
			} else{
				grail_SmartMergeWithXBuf(arr,arrPtr+prest,lrest,frest,lblock,lblock);
			}
		}
		prest=pidx-lrest;
		if(llast!=0){
			if(frest!=0){
				for(int i = 0; i < lrest; i++) {
					grailSet(arr,arrPtr+prest-lblock,arrPtr+prest);
				}
				prest=pidx;
				lrest=lblock*nblock2;
				frest=0;
			} else{
				lrest+=lblock*nblock2;
			}
			grail_MergeLeftWithXBuf(arr,arrPtr+prest,lrest,llast,-lblock);
		} else{
			for(int i = 0; i < lrest; i++) {
				grailSet(arr,arrPtr+prest-lblock,arrPtr+prest);
			}
		}
	}

	/***** End Sort With Extra Buffer *****/

	// build blocks of length K
	// input: [-K,-1] elements are buffer
	// output: first K elements are buffer, blocks 2*K and last subblock sorted
	static void grail_BuildBlocks(double[] arr,int arrPtr,int L,int K,double[] extbuf,int bufPtr,int LExtBuf){
		int m,u,h,p0,p1,rest,restk,p,kbuf;
		kbuf=K<LExtBuf ? K : LExtBuf;
		while((kbuf&(kbuf-1)) != 0) kbuf&=kbuf-1;  // max power or 2 - just in case

		if(kbuf != 0){
			System.arraycopy(arr, arrPtr-kbuf, extbuf, bufPtr, kbuf);
			for(m=1;m<L;m+=2){
				u=0;
				if(SORT_CMP(arr[arrPtr+(m-1)],arr[arrPtr+m])>0) u=1;
				grailSet(arr,arrPtr+m-3,arrPtr+m-1+u);
				grailSet(arr,arrPtr+m-2,arrPtr+m-u);
			}
			if(L%2!=0) grailSet(arr,arrPtr+L-3,arrPtr+L-1);
			arrPtr-=2;
			for(h=2;h<kbuf;h*=2){
				p0=0;
				p1=L-2*h;
				while(p0<=p1){
					grail_MergeLeftWithXBuf(arr,arrPtr+p0,h,h,-h);
					p0+=2*h;
				}
				rest=L-p0;
				if(rest>h){
					grail_MergeLeftWithXBuf(arr,arrPtr+p0,h,rest-h,-h);
				} else {
					for(;p0<L;p0++)	arr[p0-h]=arr[p0];
				}
				arrPtr-=h;
			}
			System.arraycopy(arr, arrPtr+L, extbuf, bufPtr, kbuf);
		} else{
			for(m=1;m<L;m+=2){
				u=0;
				if(SORT_CMP(arr[arrPtr+(m-1)],arr[arrPtr+m])>0) u=1;
				grail_swap1(arr,arrPtr+(m-3),arrPtr+(m-1+u));
				grail_swap1(arr,arrPtr+(m-2),arrPtr+(m-u));
			}
			if(L%2!=0) grail_swap1(arr,arrPtr+(L-1),arrPtr+(L-3));
			arrPtr-=2;
			h=2;
		}
		for(;h<K;h*=2){
			p0=0;
			p1=L-2*h;
			while(p0<=p1) {
				grail_MergeLeft(arr,arrPtr+p0,h,h,-h);
				p0+=2*h;
			}
			rest=L-p0;
			if(rest>h){
				grail_MergeLeft(arr,arrPtr+p0,h,rest-h,-h);
			} else{
				grail_rotate(arr,arrPtr+p0-h,h,rest);
			}
			arrPtr-=h;
		}
		restk=L%(2*K);
		p=L-restk;
		if(restk<=K) grail_rotate(arr,arrPtr+p,restk,K);
		else grail_MergeRight(arr,arrPtr+p,K,restk-K,K);
		while(p>0){
			p-=2*K;
			grail_MergeRight(arr,arrPtr+p,K,K,K);
		}
	}

	// arr - starting array. arr[-lblock..-1] - buffer (if havebuf).
	// lblock - length of regular blocks. First nblocks are stable sorted by 1st elements and key-coded
	// keys - arrays of keys, in same order as blocks. key<midkey means stream A
	// nblock2 are regular blocks from stream A. llast is length of last (irregular) block from stream B, that should go before nblock2 blocks.
	// llast=0 requires nblock2=0 (no irregular blocks). llast>0, nblock2=0 is possible.
	private static void grail_MergeBuffersLeft(double[] arr,int keysPtr,int midkey,int arrPtr,int nblock,int lblock,boolean havebuf,int nblock2,int llast){
		int l,prest,lrest,frest,pidx,cidx,fnext;
		
		if(nblock==0){
			l=nblock2*lblock;
			if(havebuf) grail_MergeLeft(arr,arrPtr,l,llast,-lblock);
			else grail_MergeWithoutBuffer(arr,arrPtr,l,llast);
			return;
		}

		lrest=lblock;
		frest=SORT_CMP(arr[keysPtr],arr[midkey])<0 ? 0 : 1;
		pidx=lblock;
		for(cidx=1;cidx<nblock;cidx++,pidx+=lblock){
			prest=pidx-lrest;
			fnext=SORT_CMP(arr[keysPtr+cidx],arr[midkey])<0 ? 0 : 1;
			if(fnext==frest){
				if(havebuf) grail_swapN(arr,arrPtr+prest-lblock,arrPtr+prest,lrest);
				prest=pidx;
				lrest=lblock;
			} else{
				if(havebuf){
					grail_SmartMergeWithBuffer(arr,arrPtr+prest,lrest,frest,lblock,lblock);
				} else{
					grail_SmartMergeWithoutBuffer(arr,arrPtr+prest,lrest,frest,lblock);
				}

			}
		}
		prest=pidx-lrest;
		if(llast!=0){
			if(frest!=0){
				if(havebuf) grail_swapN(arr,arrPtr+prest-lblock,arrPtr+prest,lrest);
				prest=pidx;
				lrest=lblock*nblock2;
				frest=0;
			} else{
				lrest+=lblock*nblock2;
			}
			if(havebuf) grail_MergeLeft(arr,arrPtr+prest,lrest,llast,-lblock);
			else grail_MergeWithoutBuffer(arr,arrPtr+prest,lrest,llast);
		} else{
			if(havebuf) grail_swapN(arr,arrPtr+prest,arrPtr+(prest-lblock),lrest);
		}
	}

	private static void grail_LazyStableSort(double[] arr,int arrPtr,int L){
		int m,h,p0,p1,rest;
		for(m=1;m<L;m+=2){
			if(SORT_CMP(arr[arrPtr+m-1],arr[arrPtr+m])>0) grail_swap1(arr,arrPtr+(m-1),arrPtr+m);
		}
		for(h=2;h<L;h*=2){
			p0=0;
			p1=L-2*h;
			while(p0<=p1){
				grail_MergeWithoutBuffer(arr,arrPtr+p0,h,h);
				p0+=2*h;
			}
			rest=L-p0;
			if(rest>h) grail_MergeWithoutBuffer(arr,arrPtr+p0,h,rest-h);
		}
	}

	// keys are on the left of arr. Blocks of length LL combined. We'll combine them in pairs
	// LL and nkeys are powers of 2. (2*LL/lblock) keys are guarantied
	private static void grail_CombineBlocks(double[] arr,int keysPtr,int arrPtr,int len,int LL,int lblock,boolean havebuf,double[] xbuf, int bufPtr){
		int M,b,NBlk,midkey,lrest,u,p,v,kc,nbl2,llast;
		int arr1;
		
		M=len/(2*LL);
		lrest=len%(2*LL);
		if(lrest<=LL){
			len-=lrest;
			lrest=0;
		}
		if(xbuf!=null) System.arraycopy(arr, arrPtr-lblock, xbuf, bufPtr, lblock);
		for(b=0;b<=M;b++){
			if(b==M && lrest==0) break;
			arr1=arrPtr+b*2*LL;
			NBlk=(b==M ? lrest : 2*LL)/lblock;
			grail_SortIns(arr,keysPtr,NBlk+(b==M ? 1 : 0));
			midkey=LL/lblock;
			for(u=1;u<NBlk;u++){
				p=u-1;
				for(v=u;v<NBlk;v++){
					kc=SORT_CMP(arr[arr1+p*lblock],arr[arr1+v*lblock]);
					if(kc>0 || (kc==0 && SORT_CMP(arr[keysPtr+p],arr[keysPtr+v])>0)) p=v;
				}
				if(p!=u-1){
					grail_swapN(arr,arr1+(u-1)*lblock,arr1+p*lblock,lblock);
					grail_swap1(arr,keysPtr+(u-1),keysPtr+p);
					if(midkey==u-1 || midkey==p) midkey^=(u-1)^p;
				}
			}
			nbl2=llast=0;
			if(b==M) llast=lrest%lblock;
			if(llast!=0){
				while(nbl2<NBlk && SORT_CMP(arr[arr1+NBlk*lblock],arr[arr1+(NBlk-nbl2-1)*lblock])<0) nbl2++;
			}
			if(xbuf!=null) grail_MergeBuffersLeftWithXBuf(arr,keysPtr,keysPtr+midkey,arr1,NBlk-nbl2,lblock,nbl2,llast);
			else grail_MergeBuffersLeft(arr,keysPtr,keysPtr+midkey,arr1,NBlk-nbl2,lblock,havebuf,nbl2,llast);
		}
		if(xbuf!=null){
			for(p=len;--p>=0;) grailSet(arr,arrPtr+p,arrPtr+p-lblock);
			System.arraycopy(xbuf, bufPtr, arr, arrPtr-lblock, lblock);
		}else if(havebuf) while(--len>=0) grail_swap1(arr,arrPtr+len,arrPtr+len-lblock);
	}

	private static void grail_commonSort(double[] arr,int arrPtr,int Len,double[]extbuf,int bufPtr,int LExtBuf){
		int lblock,nkeys,findkeys,ptr,cbuf,lb,nk;
		boolean havebuf,chavebuf;
		int s;
		
		if(Len<16){
			grail_SortIns(arr,arrPtr,Len);
			return;
		}

		lblock=1;
		while(lblock*lblock<Len) lblock*=2;
		nkeys=(Len-1)/lblock+1;
		findkeys=grail_FindKeys(arr,arrPtr,Len,nkeys+lblock);
		havebuf=true;
		if(findkeys<nkeys+lblock){
			if(findkeys<4){
				grail_LazyStableSort(arr,arrPtr,Len);
				return;
			}
			nkeys=lblock;
			while(nkeys>findkeys) nkeys/=2;
			havebuf=false;
			lblock=0;
		}
		ptr=lblock+nkeys;
		cbuf=havebuf ? lblock : nkeys;
		if(havebuf) {
			grail_BuildBlocks(arr,arrPtr+ptr,Len-ptr,cbuf,extbuf,bufPtr,LExtBuf);
		}
		else{
			grail_BuildBlocks(arr,arrPtr+ptr,Len-ptr,cbuf,null,bufPtr,0);
		}

		// 2*cbuf are built
		while(Len-ptr>(cbuf*=2)){
			lb=lblock;
			chavebuf=havebuf;
			if(!havebuf){
				if(nkeys>4 && nkeys/8*nkeys>=cbuf){
					lb=nkeys/2;
					chavebuf=true;
				} else{
					nk=1;
					s=cbuf*findkeys/2;
					while(nk<nkeys && s!=0){
						nk*=2; s/=8;
					}
					lb=(2*cbuf)/nk;
				}
			}
			grail_CombineBlocks(arr,arrPtr,arrPtr+ptr,Len-ptr,cbuf,lb,chavebuf,chavebuf && lb<=LExtBuf ? extbuf : null,bufPtr);
		}
		grail_SortIns(arr,arrPtr,ptr);
		grail_MergeWithoutBuffer(arr,arrPtr,ptr,Len-ptr);
	}

	private static void grailSort(double[] arr){
		grail_commonSort(arr,0,arr.length,null,0,0);
	}

	//private static void GrailSortWithBuffer(int[] arr){
	//	int[] ExtBuf = new int[GRAIL_EXT_BUFFER_LENGTH];
	//	grail_commonSort(arr,0,arr.length,ExtBuf,0,GRAIL_EXT_BUFFER_LENGTH);
	//}
	
	//private static void GrailSortWithDynBuffer(int[] arr){
	//	int L=1;
	//	while(L*L<arr.length) L*=2;
	//	int[] ExtBuf = new int[L];
	//	grail_commonSort(arr,0,arr.length,ExtBuf,0,L);
	//}
	
	public static void main(String[] args){
		boolean working = true;
		
		double[] numbers = new double[2000];
		for(int i = 0; i < numbers.length; i++){
			numbers[i] = Math.random()*numbers.length;
		}
		
		grailSort(numbers);
		
		for(int i = 1; i < numbers.length; i++){
			if(numbers[i] < numbers[i - 1]){
				working = false;
			}
		}
		
		System.out.println(Arrays.toString(numbers));
		
		if(!working) System.out.println("Did not work...");
		else System.out.println("Sorted!");
	}
}
