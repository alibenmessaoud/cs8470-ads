set terminal epslatex color
set output 'recall.eps'

set title "Similarity Flooding vs Edge Confidence: Recall comparison\n OAEI Benchmark 1 suite"
set xlabel "Test Number"
set ylabel "Recall"

set style data histogram
set style histogram cluster gap 1
set style fill solid border -1

set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify
set xtics  norangelimit font ",12"
set xtics   ()

set boxwidth 0.9

f(x,y) = 2*((x*y)/(x+y))

plot './suite_1_new/results.txt' using 4:xtic(1) ti 'Edge Conf', \
     './suite_1_old/results.txt' using 4:xtic(1) ti 'Sim Flood'

set output
