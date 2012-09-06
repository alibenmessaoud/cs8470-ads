src = src
dest = classes
classpath = $(dest)
flags = -deprecation -cp $(classpath) -sourcepath $(src) -d $(dest)

all: LockTable Page PrecedenceGraph Record Schedule Transaction

classes:
	mkdir -p $(dest)

LockTable: classes LockTable.scala
	scalac $(flags) $(src)/LockTable.scala

Page: classes Record Page.scala
	scalac $(flags) $(src)/Page.scala

PrecedenceGraph: classes PrecedenceGraph.scala
	scalac $(flags) $(src)/PrecedenceGraph.scala

Record: classes Record.scala
	scalac $(flags) $(src)/Record.scala

Schedule: classes Schedule.scala
	scalac $(flags) $(src)/Schedule.scala

Transaction: classes LockTable Transaction.scala
	scalac $(flags) $(src)/Transaction.scala

clean:
	rm -rf $(dest)
