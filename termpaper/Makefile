all: compile

compile:
	pdflatex -synctex=1 -interaction=nonstopmode *.tex
	bibtex *.aux
	pdflatex -synctex=1 -interaction=nonstopmode *.tex
	pdflatex -synctex=1 -interaction=nonstopmode *.tex

evince: compile
	evince *.pdf &

clean:
	rm -f *.log 
	rm -f *.aux 
	rm -f *.dvi 
	rm -f *.bbl 
	rm -f *.blg 
	rm -f *.ilg 
	rm -f *.toc 
	rm -f *.lof 
	rm -f *.lot 
	rm -f *.idx 
	rm -f *.ind 
	rm -f *.ps
	rm -f *.pdf
	rm -f *.synctex.gz
	rm -f *.out
	rm -f *~

