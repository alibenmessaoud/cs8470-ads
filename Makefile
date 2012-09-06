dest = classes
classpath = $(dest)
flags = -deprecation

all: LockTable Page PrecedenceGraph Record Schedule Transaction

classes:
	mkdir -p $(dest)

LockTable: classes LockTable.scala
	scalac $(flags) -cp $(classpath) -d $(dest) LockTable.scala

Page: classes Record Page.scala
	scalac $(flags) -cp $(classpath) -d $(dest) Page.scala

PrecedenceGraph: classes PrecedenceGraph.scala
	scalac $(flags) -cp $(classpath) -d $(dest) PrecedenceGraph.scala

Record: classes Record.scala
	scalac $(flags) -cp $(classpath) -d $(dest) Record.scala

Schedule: classes Schedule.scala
	scalac $(flags) -cp $(classpath) -d $(dest) Schedule.scala

Transaction: classes LockTable Transaction.scala
	scalac $(flags) -cp $(classpath) -d $(dest) Transaction.scala

clean:
	rm -rf $(dest)
