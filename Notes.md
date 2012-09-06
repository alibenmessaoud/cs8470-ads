# Notes

## Project 1

We need to test our concurrency control algorithms using actual threaded transactions. We also need to make
sure that we're using a TransactionManager and a StorageManager. 

## Logging Rules

1. Must write to the LogBuffuer before the Cache.
2. 

## Other Notes

1. Commit Point occurs when the LogBuffer is flushed to the Log file.