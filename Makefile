src = src
dest = classes
doc = doc
classpath = $(dest)
flags = -deprecation -unchecked -cp $(classpath) -sourcepath $(src) -d $(dest)

srcs = $(shell find ./$(src) -name *.scala)
objs = $(patsubst %.scala,%,$(srcs))

all: classes $(objs) doc

classes:
	mkdir -p $(dest)

$(objs): %: %.scala
	scalac $(flags) $< 

src/ads/Message: src/ads/Op

src/ads/Transaction: src/ads/Op src/ads/Message

src/ads/TransactionManager: src/ads/Op src/ads/Transaction src/ads/ConcurrencyControl src/ads/Message

doc: 
	mkdir -p $(doc)
	scaladoc -d $(doc) -doc-title "cs8470-ads" -doc-source-url 'https://github.com/mepcotterell/cs8470-ads/blob/master/€{FILE_PATH}.scala' -cp $(classpath) -deprecation -sourcepath $(src) $(srcs)

clean:
	rm -rf $(doc)
	rm -rf $(dest)
