Terrance Medina
# Notes

Some portions of this page may be copied verbatim or paraphrased from a 
textbook.

## Project 1

We need to test our concurrency control algorithms using actual threaded 
transactions. We also need to make sure that we're using a TransactionManager 
and a StorageManager. 

## Logging Rules

1. Must write to the LogBuffuer before the Cache.
2. 

## Other Notes

1. Commit Point occurs when the LogBuffer is flushed to the Log file.

## TransactionManager

Since transactions execute concurrently, the _TransactionManager_ must deal with
a merge of transaction schedules, which we refer to simply as a _schedule_.
The TransactionManager has the responsibility of servicing each arriving 
reqquest. However, doing so in the order of arrival might lead to incorrect
behavior. Hence, when a request arrives, a decision must me made as to whether 
to service it immediately. This decision is made by the manager's
_ConcurrencyControl_.

I propose the following:

1. TransactionManager class: This will provide the operations for transactions 
   and will extend a ConcurrencyControl trait.

2. ConcurrencyControl trait: This will provide a partially implemented interface
    for the concurrency control operations.

3. A trait that extends ConcurrencyControl for each concurrency control 
   scheme that we are to implement, overriding functions as neeeded.

This will help make testing easier. For example, when creating a
TransactionManager, we could do something like this (assuming we keep
the traits in a "concurrency" package):

```scala
import concurrency._
val tm = new TransactionManager() with ConflictSerializability
```

The TransactionManager class needs to provide the following public operations:

1. begin
2. read
3. write
4. rollback
5. commit

It will also need the following private operations:

1. shared lock
2. exclusive lock
3. upgrade lock
4. unlock

We need to determine our strategy for keeping track of the incoming operations.
Also need to figure out when we execute them.

## ConcurrencyControl

The goal of the ConcurrencyControl trait is to reorder request of different 
transactions in the arriving schedule so as to produce a correct schedule for
servicing by the TransactionManager class.

If ConcurrencyControl can determine that the (possibly interleaved) sequence of
operations that has arrived is the prefix of a serializable schedule no matter
what operations might be submitted later, it tells the TransactionManager to
execute the operations as they arrive. If it can't be certain of this, some
operations have to be delayed. Delay results in reordering.

There seem to be two models for concurrency control:

1. __immediate-update__: if a transaction's request to write _x_ is granted, the
   value of _x_ is immediately updated in the database; if its request to read 
   _x_ is granted, the value of _x_ in the database is returned.

2. __deferred-update__: if a transaction's request to write _x_ is granted, the
   value of _x_ in the database is not immediately updated. Instead, the new
   vaue is saved in the buffer maintained by the system for the transaction
   and called in its __intentions list__. If a transaction's request to read
   _x_ is granted, the system returns the value of _x_ in the database unless
   the transaction has previously written _x_, in which case the value of _x_ in
   its intentions list is returned. If and when the transaction commits, its
   intentions list is used to update the database.

How do we decide whether or not to grant a transaction's request? The kinds of
responses to a request include:

1. Grant the request.
2. Make the transaction wait until some other event occurs
3. Deny the request (and abort/rollback) the transaction.

Concurrency control, in general, can be characterized into the following 
categories: 

1. __pessimistic control__: whenever a transaction attempts to perform any
   operation, it must request permission. However, the transaction may commit
   at any time without requesting permission.

2. __optimistic control__: a transaction can perform any operation without
   requesting permission. However, the transaction must request permission to
   commit.

The most commonly implemented concurrency control is the immediate-update
pessimistic system. Based on the lectures from class, this seems like the way
to go.

### TimeStampOrdering (TSO)

I think this one is the easiest. If a transaction requests an operation and
another transaction already has operations in the TransactionManager, then the
transaction needs to wait for some random amount of time and assigned a new
timestamp.

### ConflictSerializability (CSR)

ConflictSerializability will be based on conflict equivalence. We know that two
operations are conflicting if they satisfy the following three conditions:

1. In different transactions
2. Operating on the same object
3. At least one operation is a write

We know how to build a PrecedenceGraph and determine if their is a cycle. 

We need to implement this serializable graph checking using a protocol similar
to the following:

1. Create a precedence graph (we'll need to modify it a little bit)

2. Using a synchronized hash table (probably HashMap), make a list for each 
   object.

3. Each list will keep track of all operations concerning that object from the
   second most recent write up until now.

3. When a transaction requests an operation on an object, we'll compare that
   operation will all other operations in the objects list using the conflict
   rules.

4. If there is a conflict, add it to the Precendence Graph.

5. After all comparisons are made, if the graph has a cycle, then rollback
   the (conflicting) transaction that requested the new operation.

* When a tranaction commits, we can remove all edges concerning that transaction
  from the precedence graph. 

### TwoPhaseLocking (2PL)


