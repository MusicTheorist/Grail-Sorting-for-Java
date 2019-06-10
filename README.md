# Grail Sort for Java
Refactoring of Grail Sort by Andrey Astrelin (https://github.com/Mrrl/GrailSort/blob/master/GrailSort.h) from C to Java.

Results are timed.

6/9/2019 -
Grail Sort for Java is now slightly faster as well, as rotating keys to their final position is now done through Insertion sort. This saves some extra writes and helps Grail Sort run quicker. Unfortunately I found a weird hard limit with making this a general case, which I discuss in the comments.

Grail Sort without KeyInsert, 1 million integers:
- Without buffer: 368.402ms average
- Static buffer: 356.029ms average
- Dynamic buffer: 327.712ms average

Grail Sort with KeyInsert, 1 million integers:
- Without buffer: 361.9ms average (1.76% faster)
- Static buffer: 350.225ms average (1.63% faster)
- Dynamic buffer: 321.445ms average (1.91% faster)
