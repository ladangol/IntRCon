# IntRCon
Temporal repair construction based on time intervals
<br>
Input files: a csv file and a file for temporal functional dependencies.
<br>
I assume the last two attributes are the start point and end points of the temporal facts.
Therefore you do not need to mention them in the temporal functional dependencies (tFDs), I add them in the program
for example if your schema is E(name, company, startpoint, endpoint)
and no one can work at the same time in two companies. your tFD is
<br>
1 -> 2
<br>
This means attribute 1 together with the time determines the value of attribute 2.
<br>
For more than on tFDs, put each on a separate line. Separate the attributes by comma.<br>
For example: <br>
1,4 -> 5,6,7
<br>
3 -> 2,4, 8
<br>
If the first line of your csv file is the name of the attributes uncomment the line 
<br>
//bf.readLine();  //skipping the header

