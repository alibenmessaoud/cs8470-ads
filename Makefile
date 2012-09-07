src = src
dest = classes
classpath = $(dest)
flags = -deprecation -unchecked -cp $(classpath) -sourcepath $(src) -d $(dest)

srcs = $(shell find ./$(src) -name *.scala)
objs = $(patsubst %.scala,%,$(srcs))

all: classes $(objs)

classes:
	mkdir -p $(dest)

$(objs): %: %.scala
	scalac $(flags) $< 

src/Message: src/Op

src/Transaction: src/Op src/Message

src/TransactionManager: src/Op src/Transaction src/ConcurrencyControl src/Message

clean:
	rm -rf $(dest)
