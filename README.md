# Grail Sort for Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

Results are timed.

6/9/2019 -
Grail Sort for Java is now slightly faster as well, as rotating keys to their final position is now done through Insertion sort. This saves some extra writes and helps Grail Sort run quicker. Unfortunately I found a weird hard limit with making this a general case, which I discuss in the comments.

UPDATE - Unfortunately, there are edge cases in key rotating as well. Will rethink this optimization in the future.
2nd UPDATE - Using a conditional, I was able to attempt the grailShift method again. Seems to have helped with speed.

Grail Sort without grailShift, 1 million integers:
- Without buffer: 368.402ms average of 1,000 tests
- Static buffer: 356.029ms average of 1,000 tests
- Dynamic buffer: 327.712ms average of 1,000 tests

Grail Sort with grailShift, 1 million integers:
- Without buffer: 363.597ms average of 1,000 tests (1.304% faster)
- Static buffer: 346.888ms average of 1,000 tests (2.567% faster)
- Dynamic buffer: 318.45ms average of 1,000 tests (2.826% faster)
