package com.microsoft.example;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    public static int consume(String brokers, String groupId, String topicName) {
        // Create a consumer
        KafkaConsumer<String, String> consumer;
        // Configure the consumer
        Properties properties = new Properties();
        // Point it to the brokers
        properties.setProperty("bootstrap.servers", brokers);
        // Set the consumer group (all consumers must belong to a group).
        properties.setProperty("group.id", groupId);
        // Set how to serialize key/value pairs
        properties.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        // specify the protocol for Domain Joined TLS Encrypted clusters
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        properties.setProperty("ssl.mechanism", "GSSAPI");
        properties.setProperty("sasl.kerberos.service.name", "kafka");
        // specifiy the Truststore location and password
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/sshuser/ssl/kafka.client.truststore.jks");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "MyClientPassword123");
        // specifiy the Keystore location and password
        properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,"/home/sshuser/ssl/kafka.client.keystore.jks");
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "MyClientPassword123");
        // specifiy the key password
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "MyClientPassword123");
        // When a group is first created, it has no offset stored to start reading from. This tells it to start
        // with the earliest record in the stream.
        properties.setProperty("auto.offset.reset","earliest");

        consumer = new KafkaConsumer<>(properties);

        // Subscribe to the 'test' topic
        consumer.subscribe(Arrays.asList(topicName));

        // Loop until ctrl + c
        int count = 0;
        while(true) {
            // Poll for records
            ConsumerRecords<String, String> records = consumer.poll(200);
            // Did we get any?
            if (records.count() == 0) {
                // timeout/nothing to read
            } else {
                // Yes, loop over records
                for(ConsumerRecord<String, String> record: records) {
                    // Display record and count
                    count += 1;
                    logger.info( count + ": " + record.value());
                }
            }
        }
    }
}
