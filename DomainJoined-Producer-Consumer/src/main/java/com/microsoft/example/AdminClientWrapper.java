package com.microsoft.example;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;


public class AdminClientWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AdminClientWrapper.class);

    public static Properties getProperties(String brokers) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        // Set how to serialize key/value pairs
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		// specify the protocol for Domain Joined clusters
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
		
        return properties;
    }

    public static void describeTopics(String brokers, String topicName) throws IOException {
        // Set properties used to configure admin client
        Properties properties = getProperties(brokers);

        try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
            // Make async call to describe the topic.
            final DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
            TopicDescription description = describeTopicsResult.values().get(topicName).get();
            logger.info(description.toString());
        } catch (Exception exception) {
            logger.error("Describe Topic denied: ", exception);
        }
    }

    public static void deleteTopics(String brokers, String topicName) throws IOException {
        // Set properties used to configure admin client
        Properties properties = getProperties(brokers);

        try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
            final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singleton(topicName));
            deleteTopicsResult.values().get(topicName).get();
            logger.info("Topic " + topicName + " deleted");
        } catch (Exception exception) {
            logger.error("Delete Topic denied: ", exception);
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
            logger.info("Topic " + topicName + " created");
        } catch (Exception exception) {
            logger.error("Create Topics denied: ", exception);
        }
    }
}
