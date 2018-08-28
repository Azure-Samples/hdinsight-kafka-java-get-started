package com.microsoft.example;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.NewTopic;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.CommonClientConfigs;


import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.Properties;
import java.util.Random;
import java.io.IOException;


public class AdminClientWrapper {

    public static Properties getProperties(String brokers) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        // Set how to serialize key/value pairs
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		// specify the protocol for Domain Joined clusters
        //properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
		
        return properties;
    }

    public static void describeTopics(String brokers, String topicName) throws IOException {
        // Set properties used to configure admin client
        Properties properties = getProperties(brokers);

        try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
            // Make async call to describe the topic.
            final DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));

            TopicDescription description = describeTopicsResult.values().get(topicName).get();
            System.out.print(description.toString());
        } catch (Exception e) {
            System.out.print("Describe denied\n");
            System.out.print(e.getMessage());
            //throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void deleteTopics(String brokers, String topicName) throws IOException {
        // Set properties used to configure admin client
        Properties properties = getProperties(brokers);

        try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
            final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singleton(topicName));
            deleteTopicsResult.values().get(topicName).get();
            System.out.print("Topic " + topicName + " deleted");
        } catch (Exception e) {
            System.out.print("Delete Topics denied\n");
            System.out.print(e.getMessage());
            //throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void createTopics(String brokers, String topicName) throws IOException {
        // Set properties used to configure admin client
        Properties properties = getProperties(brokers);

        try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
            int numPartitions = 8;
            short replicationFactor = (short)3;
            final NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);

            final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
            createTopicsResult.values().get(topicName).get();
            System.out.print("Topic " + topicName + " created");
        } catch (Exception e) {
            System.out.print("Create Topics denied\n");
            System.out.print(e.getMessage());
            //throw new RuntimeException(e.getMessage(), e);
        }
    }
}
