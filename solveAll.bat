
for %%i in (./input/*.txt) do (
  java -jar OMA_Greedy.jar -vs ./input/%%i >> ./output/outputfile.csv
)
