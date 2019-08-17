# Grail-Sorting-for-Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

GrailSort is a variant of Block Merge Sort (https://en.wikipedia.org/wiki/Block_sort), a stable, in-place, worst-case O(n log n) implementation of merge sort. The algorithm is adaptive, but not to the degree of TimSort. It is similar to Mike McFadden's WikiSort (https://github.com/BonzaiThePenguin/WikiSort), yet mainly differs by swapping blocks and their tags in parallel before merging, and shifting the position of an internal buffer used for locally merging/appending portions of an array.

Like WikiSort, extra memory can be allocated to an external buffer, potentially bypassing the need for an internal buffer and giving GrailSort a slight boost in speed. This implementation includes three options: 1) sorting without an external buffer -- O(1) space complexity, 2) sorting with a static buffer of 512 items -- O(512) space complexity, and 3) sorting with a dynamic buffer scaled to the square root of the input array's length -- O(sqrt(n)) space complexity.

EDIT: The results for GrailSort's runtime have been removed. I just discovered they were heavily skewed because the random number generator was not working properly. Should have compared Mr. Astrelin's results to mine, anyways. Oh dear. Will update this when I get the chance.

EDIT2: GrailSort's Insertion Sort is now a Binary Insertion Sort with a few tricks up its sleeves, making it asymptotically optimal. Because of that, I boosted the "small array" cutoff up to 32.

EDIT3: The previous trick with recursion ended up changing the space complexity (thanks for pointing this out, Morwenn!), and that was not
my goal. grailFindKeys is once again a bottleneck for this algorithm. Hopefully some day I'll figure out how to properly optimize it. For now, keeping the changes made to Insertion Sort as that is for sure optimal now.
