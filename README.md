# Grail-Sorting-for-Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

GrailSort is a variant of Block Merge Sort (https://en.wikipedia.org/wiki/Block_sort), a stable, in-place, worst-case O(n log n) implementation of merge sort. The algorithm is adaptive, but not to the degree of TimSort. It is similar to Mike McFadden's WikiSort (https://github.com/BonzaiThePenguin/WikiSort), yet mainly differs by swapping blocks and their tags in parallel before merging, and shifting the position of an internal buffer used for locally merging/appending portions of an array.

Like WikiSort, extra memory can be allocated to an external buffer, potentially bypassing the need for an internal buffer and giving GrailSort a slight boost in speed. This implementation includes three options: 1) sorting without an external buffer -- O(1) space complexity, 2) sorting with a static buffer of 512 items -- O(512) space complexity, and 3) sorting with a dynamic buffer scaled to the square root of the input array's length -- O(sqrt(n)) space complexity.

8/15/19: Using a tiny trick with recursion I found in Mr. Astrelin's other algorithm, Square Root Sort 
         (https://github.com/Mrrl/SqrtSort/blob/master/SqrtSort.h), the process of collecting keys is
         a bit faster now.

Before this update, a binary search and multiple swaps, or rotations, were solely used to find distinct values, or keys, in the array. Keys are used in Block Merge Sort to maintain stability. If all the values in the input array are distinct, then this process simplifies to a variant of binary insertion sort, more specifically optimized gnome sort with a O(log n) worst-case search (https://en.wikipedia.org/wiki/Gnome_sort#Optimization). The number of keys GrailSort aims to collect is 2*sqrt(length) - 1. Unfortunately, this worst-case scenario turns out to be a bit inefficient, especially for large arrays.
         
Instead, what if GrailSort recursively called itself on the same array yet only processing the length needed for said distinct
keys? Again, this is around the square root of the array size.
         
First of all, how far is the recursion depth? Let's take a pretty large array of 100,000,000 numbers.
- 1st call: Grail Sorting 100,000,000 numbers.
- 2nd call: Grail Sorting 19,999 numbers.
- 3rd call: Grail Sorting 281 numbers.
- 4th call: Grail Sorting 32 numbers.
- 5th call: Grail Sorting 10 numbers.
         
Now we stop, because GrailSort devolves to Insertion Sort on arrays with 16 items or less. That's pretty fast for that case! As we climb up the stack, the number of keys were already sorted by the last call, either by an optimal use of insertion sort or GrailSort itself! Even better, we know that this method is still a stable sort, as Insertion Sort is stable, and GrailSort is stable. Voila, an even faster version of Block Merge Sort!

EDIT: The results for GrailSort's runtime have been removed. I just discovered they were heavily skewed because the random number generator was not working properly. Should have compared Mr. Astrelin's results to mine, anyways. Oh dear. Will update this when I get the chance.
