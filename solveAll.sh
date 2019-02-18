#!/bin/bash

for filename in ./input/*.txt; do

	#execute the program one time for each instance, 
	#the output of the program is redirected to the output file
	java -jar ./OMA_Greedy.jar -vs $filename  >> ./output/outputfile.csv

done
