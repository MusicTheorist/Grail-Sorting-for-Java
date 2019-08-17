# Grail-Sorting-for-Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

GrailSort is a variant of Block Merge Sort (https://en.wikipedia.org/wiki/Block_sort), a stable, in-place, worst-case O(n log n) implementation of merge sort. The algorithm is adaptive, but not to the degree of TimSort. It is similar to Mike McFadden's WikiSort (https://github.com/BonzaiThePenguin/WikiSort), yet mainly differs by swapping blocks and their tags in parallel before merging, and shifting the position of an internal buffer used for locally merging/appending portions of an array.

Like WikiSort, extra memory can be allocated to an external buffer, potentially bypassing the need for an internal buffer and giving GrailSort a slight boost in speed. This implementation includes three options: 1) sorting without an external buffer -- O(1) space complexity, 2) sorting with a static buffer of 512 items -- O(512) space complexity, and 3) sorting with a dynamic buffer scaled to the square root of the input array's length -- O(sqrt(n)) space complexity.

Grail Sorting 10 million integers, average of twenty runs
- Without buffer:                      415.198ms
- With static buffer (512 items):      416.547ms
- With dynamic buffer (Sqrt of items): 413.500ms

8/15/19: Using a tiny trick with recursion I found in Mr. Astrelin's other algorithm, Square Root Sort 
         (https://github.com/Mrrl/SqrtSort/blob/master/SqrtSort.h), the process of collecting keys is
         a bit faster now.

Before this update, a binary search and multiple swaps, or rotations, to find distinct values in the array.
If all the values in the input array are distinct, then this process would simplify to a variant of binary insertion sort,
more specifically optimized gnome sort (https://en.wikipedia.org/wiki/Gnome_sort#Optimization). The number of keys GrailSort            collects is a little over the square root of the array size. Unfortunately, that still turns out to be a bit inefficient,                especially for large arrays.
         
Instead, what if GrailSort recursively called itself on the same array yet only processing the length needed for said distinct
keys? Again, this is around the square root of the array size.
         
First of all, how far is the recursion depth? Let's take a pretty large array of 100,000,000 numbers.
- 1st call: Grail Sorting 100,000,000 numbers.
- 2nd call: Grail Sorting 10,000 numbers.
- 3rd call: Grail Sorting 100 numbers.
- 4th call: Grail Sorting 10 numbers.
         
Now we stop, because GrailSort devolves to Insertion Sort on arrays with 16 items or less. That's pretty fast for that case!
         
As we climb up the stack, most keys should already be sorted by the previous recursion. Even if they aren't,
and assuming the worst case of grailFindKeys, the distance items need to be swapped is much shorter.
         
Even better, we know that this method is still a stable sort, as Insertion Sort is stable, and GrailSort is stable. Voila, an
even faster version of Block Merge Sort!

How about some results?

Grail Sorting 10 million integers, average of twenty runs
(Recursive update)
- Without buffer:                      401.275ms (3.353% faster)
- With static buffer (512 items):      399.042ms (4.202% faster)
- With dynamic buffer (Sqrt of items): 395.788ms (4.283% faster)
