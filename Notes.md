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

We need the following:
1. ```TransactionManager``` class
   * This will provide the operations for transactions and will extend a
     ```ConcurrencyControl``` trait.
2. ```ConcurrencyControl``` trait
   * This will provide a partially implemented interface for the concurrency
     control operations.
3. A trait that extends ```ConcurrencyControl``` for each concurrency control 
   scheme that we are to implement, overriding functions as neeeded.

This will help make testing easier. For example, when creating a
```TransactionManager```, we could do something like this (assuming we keep
the traits in a ```concurrency`` package):

```scala
import concurrency._
val tm = new TransactionManager() with ConflictSerializability
```
