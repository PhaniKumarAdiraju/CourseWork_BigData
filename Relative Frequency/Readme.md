# Calculate Relative Frequency of 100K Wiki Text 

In this assignment, I worked on a set of 100,000 Wikipedia documents: 100KWikiText.txt, in which each line consists of the plain text extracted from an individual Wikipedia document. Worked on the following using AWS instances:

1. Configured and ran the latest release of Apache Hadoop in a pseudo-distributed mode.
2. Developed a MapReduce-based approach in Hadoop system to compute the relative frequencies of each word that occurs in all the documents in 100KWikiText.txt, and output the top 100 word pairs sorted in a decreasing order of relative frequency. 
3. Repeat the above steps using at least 2 VM instances in Hadoop system running in a fully-distributed mode.

# Input: 100KWikiText.txt 
# Output: Top 100 Wordpairs with their relative frequency.

# Algorithm Steps:
1.	Create a mapper, combiner and reducer
2.	Mapper will form keys and values of paired words(wordA wordB, count) and single words(wordA &)
3.	Combiner will count the number of occurrences of each pair and send it to reducer
4.	In reducer,  I am checking if the key is a single word or word pair. 
	a.	If itís a single word then:
		i.	I will check if the key is equal to current word. If so, then count the total number of occurrences
		ii.	If not, then I will set it as current word and count the occurrences of this particular word.
	b.	If itís a pair then I am counting the total occurrences of pair and calculating the relative frequencies. (I am ignoring pairs which have appeared only once)
	c.	I am adding the relative frequency, word Pair and first word to a TreeSet. I have written a custom comparator for this Treeset to arrange the objects in decreasing order of Relative Frequency. 
	d.	I am checking the size of Treeset and if itís greater than 100, I will remove the last element. Since, objects are sorted in descending order the least relative frequency pair will be removed.  
	e.	At the end of reducer, I will have Top 100 word pairs with relative frequencies. 
5.	In the main method, I am printing TreeSet to a file. 


# Execution in Pseudo Distributed Mode

1) Format The NameNode:
 $ /bin/Hadoop namenode -format

2) Start Hadoop Services:
 $ /sbin/start-all.sh

3) Put the Text File and jar in a folder on the HDFS:
 $ hadoop fs -mkdir /home/ec2-user/Practice
 $ hadoop fs -put 100KWikiText.txt /home/ ec2-user/Practice
 $ hadoop fs -put paircount4.jar /home/ ec2-user/Practice

4) Execute the jar file:
 $ /home/ec2-user/hadoop-2.7.3/bin/hadoop jar paircount4.jar PairCount1  100KWikiText.txt PCwiki4

5) View the output
 $ hadoop fs -cat /home/ec2-user/Practice/PCwiki4 /top100.txt

# Execution in Fully Distributed Mode


1) Create Another Slave EC2 instance on AWS and install hadoop on all the nodes
 $ tar -xzvf hadoop-2.7.2.tar.gz

2) Configure the hadoop/etc/hadoop/core-site.xml:
<configuration>
<property>
<name>fs.defaultFS</name> <value>hdfs://master:8020/</value>
</property>
<property>
<name>hadoop.temp.dir</name>
<value>$HOME/hdfs</value>
</property>
</configuration>

3) Configure the hadoop/etc/hadoop/hdfs-site.xml:
change the value based on number of nodes
<configuration>
<property>
<name>dfs.replication</name> <value>3</value>
</property>
<property>
<name>dfs.permissions</name>
<value>false</value>
</property> </configuration>

4) Configure the hadoop/etc/hadoop/mapred-site.xml:
<configuration>
<property>
<name>mapred.job.tracker</name>
<value>hdfs://master:8021</value>
</property>
</configuration>

5) For runnning the code, repeat the steps for Pseudo Distributed Mode
