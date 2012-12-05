all:

compile:
	javac -d . -cp seals-omt-client-v4-1beta.jar:/Users/medinat/Projects/advDB_Fall12/GroupProjects/cs8470-ads/target/scala-2.9.2/cs8470-ads_2.9.2-1.0.jar MatcherBridge.java

jar: compile
	jar cvf MatcherBridge.jar de/unima/ki/demomatcher/seals/MatcherBridge.class

orig: jar
	cp MatcherBridge.jar ./SF_orig/bin/

new: jar
	cp MatcherBridge.jar ./SF_new/bin/
