# IntRCon
Temporal repair construction based on time intervals

Input files: a csv file and a file for temporal functional dependencies.
I assume the last two attributes are the start point and end points of the temporal facts.
Therefore you do not need to mention them in the temporal functional dependencies (tFDs), I add them in the program
for example if your schema is E(name, company, startpoint, endpoint)
and no one can work at the same time in two companies. your tFD is
1 -> 2
This means attribute 1 together with the time determines the value of attribute 2.
For more than on tFDs, put each on a separate line. Separate the attributes by comma.
For example:
1,4 -> 5,6,7
3 -> 3,4, 8

If the first line of your csv file is the name of the attributes uncomment the line 
//bf.readLine();  //skipping the header

