# Analyze Flight data of 22 years 
In this project, I developed an Oozie workflow to process and analyze a large volume of flight data. I developed three map reduce jobs to analyze the flight data: 
1. the 3 airlines with the highest and lowest probability, respectively, for being on schedule
2. the 3 airports with the longest and shortest average taxi time per flight (both in and out) respectively
3. the most common reason for flight cancellations.

# Input: 
Data Set used:  the Airline On-time Performance data set (flight data set) from the period of
October 1987 to April 2008 on the Statistical Computing website: http://statcomputing.org/dataexpo/2009/the-data.html

# Output: 
1. top 3 airlines with highest and lowest probability for being on schedule
2. top 3 airports with longest and shortest average taxi time per flight 
3. most common reason for flight cancellations

# Algorithm: 
## 1. Calculation of 3 airlines with highest and lowest Probability for being on schedule
### 1.1 Mapper Phase:
1. Read input files line by line.
2. Split line based on comma and store all fields in an array
3. Fetch the values for fields corresponding to airlines, arrival delay and departure delay
4. We are considering two counts here. One for counting the airlines whose arrival delay and departure delay is 10 min and another for counting total number of airlines. These two counts are divided later to find the probability
5. Write to context<airlines, 1> and context<airlines_total, 1>
### 1.2 Combiner Phase:
1. Read context. Each Combiner will receive all the values corresponding to a key.
2. Calculate sum of all the airlines(filtered airlines which are delayed) and airlines_total(all airlines)
### 1.3 Reducer Phase:
1. Check whether the current key is airlines_total
a. if yes, then get the total count for airlines and airlines_total. Then divide these values to find probability
b. if no, then assign current key with airlines and get the total count of the airlines (filtered)
2. After finding the probability, the probability values are inserted into a tree map, where a custom comparator is written in order to sort the values based on probability
3. We have two tree maps. One for highest probability and another for lowest probability. We are removing the last and first values if the size of the tree map is greater than 3.
4. Write the output to context.

## 2. Calculation of 3 airports with longest and shortest average Taxi time (In and Out)
### 2.1 Mapper Phase:
1. Read input files line by line.
2. Split line based on comma and store all fields in an array
3. Fetch the values for fields corresponding to origin, destination, taxiIn and taxiOut
4. Write to context<origin, taxiOut> and context<destination, taxiIn>
### 2.2 Reducer Phase:
1. Read context. Each Combiner will receive all the values corresponding to a key.
2. Iterate over the context values which is list of taxiIn and taxiOut values for the airport.
3. Calculate sum of all the values and total number of values.
4. Calculate average taxi = sum / number of values.
5. Create a class AirportTaxiTime with airportname and avgerage taxi time as instance variables and implement interface Comparable
6. Sort based on average taxi time in the compareTo method.
7. Create 2 Treesets one for airports with highest taxi and another for airports with lowest taxi.
8. We want just top3 and bottom 3 values so remove others using pollFirst and pollLast methods of Treeset.
9. Write the output to context.
## 3 Find most common reason for cancellation of flight
### 3.1 Mapper Phase:
1. Read input files line by line.
2. Split line based on comma and store all fields in an array
3. Fetch the values for fields corresponding to cancellationCode column
4. If cancellationCode corresponds to either A, B, C or D
5. Write to context<cancellationCode, 1>
### 3.2 Reducer Phase:
1. Read context. Each Combiner will receive all the values corresponding to a key.
2. Iterate over the context values and Calculate sum of all the values.
3. Create a class with cancelCode and count as instance variables and implement interface Comparable.
4. Add the class objects to Treeset in order to maintain a sorted order.
5. Write the topmost reason to context

# Commands to run Oozie workflow :

1. Copy input data to hdfs
  ` hadoop fs -put /input inputData `
2. To run oozie workflow
  ` oozie job -oozie http://localhost:11000/oozie -config job.properties -run `
3. To check status of the job with generated job id
  ` oozie job -oozie http://localhost:11000/oozie -info job_id `

# Steps to install Apache oozie 
1. download oozie
` wget http://archive.apache.org/dist/oozie/4.1.0/oozie-4.1.0.tar.gz `
2. untar
` tar -zxf oozie-4.1.0.tar.gz `
3. install maven
` sudo apt-get install maven `
4. edit pom.xml
: make hadoop version as 2.3.0 

5. distro
` mvn clean package assembly:single -P hadoop-2 -DskipTests `

6. move folder 
` cd to oozie-4.1.0/distro/target `
` sudo mv oozie-4.1.0 /home/ubuntu/oozie-4.1.0 `

7. copy jar to libext
` cd /home/ubuntu/oozie-4.1.0/oozie-4.1.0 `
` mkdir libext `
` cp -R /home/ubuntu/oozie-4.1.0/oozie-4.1.0/hadooplibs/hadoop-2/target/hadooplibs/hadooplib-2.3.0.oozie-4.1.0/* libext `

` curl -O http://archive.cloudera.com/gplextras/misc/ext-2.2.zip `

8. prepare war
` sudo apt-get install zip `
` ./bin/oozie-setup.sh prepare-war `

9. Edit core-site.xml

` <property> 
         <name>fs.default.name</name> 
         <value>hdfs://ec2-52-91-190-208.compute-1.amazonaws.com:9000</value> 
     </property> 
  <property> 
    <name>hadoop.proxyuser.ubuntu.hosts</name> 
    <value>ec2-52-91-190-208.compute-1.amazonaws.com.com</value> 
  </property> 
  <property> 
    <name>hadoop.proxyuser.ubuntu.groups</name> 
    <value>ubuntu</value> 
  </property>  ` 
  
10.  upload sharelib to hdfs
 ` ./bin/oozie-setup.sh sharelib create -fs hdfs://localhost:9000 `
 ` ./bin/oozie-setup.sh sharelib create fs -hdfs://ec2-52-91-190-208.compute-1.amazonaws.com:54310 `

11. Execute oozie: 
` ./bin/ooziedb.sh create -sqlfile oozie.sql ñrun `
` ./bin/oozied.sh start `
` ./bin/oozied.sh run `
