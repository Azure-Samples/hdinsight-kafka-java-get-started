---
page_type: sample
languages:
- java
products:
  - azure
  - azure-hdinsight
description: "The examples in this repository demonstrate how to use the Kafka Consumer, Producer, and Streaming APIs with a Kafka on HDInsight cluster."
urlFragment: hdinsight-kafka-java-get-started
---

# Java-based example of using the Kafka Consumer, Producer, and Streaming APIs

The examples in this repository demonstrate how to use the Kafka Consumer, Producer, and Streaming APIs with a Kafka on HDInsight cluster.

There are two projects included in this repository:

* Producer-Consumer: This contains a producer and consumer that use a Kafka topic named `test`.

* Streaming: This contains an application that uses the Kafka streaming API (in Kafka 0.10.0 or higher) that reads data from the `test` topic, splits the data into words, and writes a count of words into the `wordcounts` topic.

NOTE: This both projects assume Kafka 0.10.0, which is available with Kafka on HDInsight cluster version 3.6.

## Producer and Consumer

To run the consumer and producer example, use the following steps:

1. Fork/Clone the repository to your development environment.

2. Install Java JDK 8 or higher. This was tested with Oracle Java 8, but should work under things like OpenJDK as well.

3. Install [Maven](http://maven.apache.org/).

4. Assuming Java and Maven are both in the path, and everything is configured fine for JAVA_HOME, use the following commands to build the consumer and producer example:

        cd Producer-Consumer
        mvn clean package
    
    A file named `kafka-producer-consumer-1.0-SNAPSHOT.jar` is now available in the `target` directory.

5. Use SCP to upload the file to the Kafka cluster:

        scp ./target/kafka-producer-consumer-1.0-SNAPSHOT.jar SSHUSER@CLUSTERNAME-ssh.azurehdinsight.net:kafka-producer-consumer.jar
   
    Replace **SSHUSER** with the SSH user for your cluster, and replace **CLUSTERNAME** with the name of your cluster. When prompted enter the password for the SSH user.

6. Use SSH to connect to the cluster:

        ssh USERNAME@CLUSTERNAME

7. Use the following commands in the SSH session to get the Zookeeper hosts and Kafka brokers for the cluster. You need this information when working with Kafka. Note that JQ is also installed, as it makes it easier to parse the JSON returned from Ambari. Replace __PASSWORD__ with the login (admin) password for the cluster. Replace __KAFKANAME__ with the name of the Kafka on HDInsight cluster.

        sudo apt -y install jq
        export KAFKAZKHOSTS=`curl -sS -u admin:$PASSWORD -G https://$CLUSTERNAME.azurehdinsight.net/api/v1/clusters/$CLUSTERNAME/services/ZOOKEEPER/components/ZOOKEEPER_SERVER | jq -r '["\(.host_components[].HostRoles.host_name):2181"] | join(",")' | cut -d',' -f1,2`

        export KAFKABROKERS=`curl -sS -u admin:$PASSWORD -G https://$CLUSTERNAME.azurehdinsight.net/api/v1/clusters/$CLUSTERNAME/services/KAFKA/components/KAFKA_BROKER | jq -r '["\(.host_components[].HostRoles.host_name):9092"] | join(",")' | cut -d',' -f1,2`

8. Use the following to verify that the environment variables have been correctly populated:

        echo '$KAFKAZKHOSTS='$KAFKAZKHOSTS
        echo '$KAFKABROKERS='$KAFKABROKERS

    The following is an example of the contents of `$KAFKAZKHOSTS`:
   
        zk0-kafka.eahjefxxp1netdbyklgqj5y1ud.ex.internal.cloudapp.net:2181,zk2-kafka.eahjefxxp1netdbyklgqj5y1ud.ex.internal.cloudapp.net:2181
   
    The following is an example of the contents of `$KAFKABROKERS`:
   
        wn1-kafka.eahjefxxp1netdbyklgqj5y1ud.cx.internal.cloudapp.net:9092,wn0-kafka.eahjefxxp1netdbyklgqj5y1ud.cx.internal.cloudapp.net:9092

    NOTE: This information may change as you perform scaling operations on the cluster, as this adds and removes worker nodes. You should always retrieve the Zookeeper and Broker information before working with Kafka.
    
    IMPORTANT: You don't have to provide all broker or Zookeeper nodes. A connection to one broker or Zookeeper node can be used to learn about the others. In this example, the list of hosts is trimmed to two entries.

9. This example uses a topic named `test`. Use the following to create this topic:

        /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --replication-factor 2 --partitions 8 --topic test --zookeeper $KAFKAZKHOSTS

10. Use the producer-consumer example to write records to the topic:
   
        java -jar kafka-producer-consumer.jar producer test $KAFKABROKERS
    
    A counter displays how many records have been written.

11. Use the producer-consumer to read the records that were just written:

        java -jar kafka-producer-consumer.jar consumer test $KAFKABROKERS
    
    This returns a list of the random sentences, along with a count of how many are read.

## Streaming

NOTE: The streaming example expects that you have already setup the `test` topic from the previous section.

1. On your development environment, change to the `Streaming` directory and use the following to create a jar for this project:

        mvn clean package
    
2. Use SCP to copy the `kafka-streaming-1.0-SNAPSHOT.jar` file to your HDInsight cluster:
   
        scp ./target/kafka-streaming-1.0-SNAPSHOT.jar SSHUSER@CLUSTERNAME-ssh.azurehdinsight.net:kafka-streaming.jar
   
    Replace **SSHUSER** with the SSH user for your cluster, and replace **CLUSTERNAME** with the name of your cluster. When prompted enter the password for the SSH user.

3. Once the file has been uploaded, return to the SSH connection to your HDInsight cluster and use the following commands to create the `wordcounts` and `wordcount-example-Counts-changelog` topics:

        /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --replication-factor 2 --partitions 8 --topic wordcounts --zookeeper $KAFKAZKHOSTS
        /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --replication-factor 2 --partitions 8 --topic wordcount-example-Counts-changelog --zookeeper $KAFKAZKHOSTS

4. Use the following command to start the streaming process in the background:

        java -jar kafka-streaming.jar $KAFKABROKERS 2>/dev/null &

4. While it is running, use the producer to send messages to the `test` topic:

        java -jar kafka-producer-consumer.jar producer test $KAFKABROKERS &>/dev/null &

6. Use the following to view the output that is written to the `wordcounts` topic:
   
        /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --bootstrap-server $KAFKABROKERS --topic wordcounts --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
   
    NOTE: You have to tell the consumer to print the key (which contains the word value) and the deserializer to use for the key and value in order to view the data.
   
    The output is similar to the following:
   
        dwarfs  13635
        ago     13664
        snow    13636
        dwarfs  13636
        ago     13665
        a       13803
        ago     13666
        a       13804
        ago     13667
        ago     13668
        jumped  13640
        jumped  13641
        a       13805
        snow    13637

7. Use __Ctrl + C__ to exit the consumer, then use the `fg` command to bring the streaming background task to the foreground. Use __Ctrl + C__ to exit it also.
