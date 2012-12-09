set terminal epslatex size 9,6.5 color 
set output 'prec.eps'

set title "Similarity Flooding vs Edge Confidence: Precision comparison\n OAEI Benchmark 1 suite" font ",36"
set xlabel "Test Number" font ",24"
set ylabel "Precision" font ",24"

set style data histogram
set style histogram cluster gap 1
set style fill solid border -1

set xtics border in scale 0,0 nomirror rotate by -90  offset character 0, 0.3, 0 autojustify font ",24"
#set xtics   ()

set boxwidth 0.9


f(x,y) = 2*((x*y)/(x+y))

plot './suite_1_new/results.txt' using 3:xtic(1) ti 'Edge Conf', \
     './suite_1_old/results.txt' using 3:xtic(1) ti 'Sim Flood'

set output
