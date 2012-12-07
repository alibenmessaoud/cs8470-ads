all: compile

compile:
	pdflatex -synctex=1 -interaction=nonstopmode anss2013-cotterell-medina.tex
	bibtex anss2013-cotterell-medina.aux
	pdflatex -synctex=1 -interaction=nonstopmode anss2013-cotterell-medina.tex
	pdflatex -synctex=1 -interaction=nonstopmode anss2013-cotterell-medina.tex

evince: compile
	evince anss2013-cotterell-medina.pdf &

emacs: 
	emacs anss2013-cotterell-medina.tex &

emacsnw: 
	emacs -nw anss2013-cotterell-medina.tex

clean:
	rm -f anss2013-cotterell-medina.log 
	rm -f anss2013-cotterell-medina.aux 
	rm -f anss2013-cotterell-medina.dvi 
	rm -f anss2013-cotterell-medina.bbl 
	rm -f anss2013-cotterell-medina.blg 
	rm -f anss2013-cotterell-medina.ilg 
	rm -f anss2013-cotterell-medina.toc 
	rm -f anss2013-cotterell-medina.lof 
	rm -f anss2013-cotterell-medina.lot 
	rm -f anss2013-cotterell-medina.idx 
	rm -f anss2013-cotterell-medina.ind 
	rm -f anss2013-cotterell-medina.ps
	rm -f anss2013-cotterell-medina.pdf
	rm -f anss2013-cotterell-medina.synctex.gz
	rm -f anss2013-cotterell-medina.out
	rm -f anss2013-cotterell-medina~

