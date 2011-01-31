Seam DVD Store Example
======================

example.name=dvdstore

This example demonstrates the use of Seam with jBPM pageflow and business
process management. It runs on JBoss AS as an EAR and Tomcat with Embedded
JBoss as a WAR.

JBoss AS 4.2 needs additional Hibernate libraries, use ant target jboss42 for deploying to it.

JBoss AS 6 needs new hibernate-search and hibernate-commons-annotations, and more source code
enhancement due to Hibernate Search and Apache Lucene API changes.
Therefore use ant target jboss6 for deploying to JBoss AS 6 M5 and later. For instance:

	ant -f build-jboss6.xml
