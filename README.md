# Grail-Sorting-for-Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

8/15/19: Using a tiny trick with recursion I found in Mr. Astrelin's other algorithm, Square Root Sort 
         (https://github.com/Mrrl/SqrtSort/blob/master/SqrtSort.h), the process of collecting keys is
         a bit faster now.
         
Grail Sorting 10 million integers, average of twenty runs
- Without buffer:                      401.275ms
- With static buffer (512 items):      399.042ms
- With dynamic buffer (Sqrt of items): 395.788ms
