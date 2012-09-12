src = src
dest = classes
doc = doc
classpath = $(dest)
flags = -deprecation -unchecked -cp $(classpath) -sourcepath $(src) -d $(dest)
test_cases = ads.test.TransactionTest

srcs = $(shell find ./$(src) -name *.scala)
objs = $(patsubst %.scala,%,$(srcs))

all: test

build: classes
	scalac $(flags) $(srcs)

test: build
	scalac -classpath ./lib/scalatest_2.9.0-1.8.jar:./classes -d ./test/ads ./test/ads/*.scala
	scala -classpath ./lib/scalatest_2.9.0-1.8.jar:./classes:./test/ads org.scalatest.run $(test_cases)

classes:
	mkdir -p $(dest)

doc: 
	mkdir -p $(doc)
	scaladoc -d $(doc) -doc-title "cs8470-ads" -doc-source-url 'https://github.com/mepcotterell/cs8470-ads/blob/master/€{FILE_PATH}.scala' -cp $(classpath) -deprecation -sourcepath $(src) $(srcs)

clean:
	rm -rf $(doc)
	rm -rf $(dest)
	rm ./tests/*.class
