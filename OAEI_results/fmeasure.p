set terminal epslatex size 9,6.5 color
set output 'fmeasure.eps'

set title "Similarity Flooding vs Edge Confidence: F-measure comparison\n OAEI Benchmark 1 suite"
set xlabel "Test Number"
set ylabel "F-measure"
#unset key
#set key left

set style data histogram
set style histogram cluster gap 1
set style fill solid border -1

set xtics border in scale 0,0 nomirror rotate by -90  offset character 0, 0.3, 0 autojustify
set xtics  norangelimit font ",10"
set xtics   ()

set boxwidth 0.9

f(x,y) = (x+y) > 0 ? 2*((x*y)/(x+y)) : 0


plot './suite_1_new/results.txt' using (f($3,$4)):xtic(1) title 'Edge Conf', \
     './suite_1_old/results.txt' using (f($3,$4)):xtic(1) title 'Sim Flood'

set output


