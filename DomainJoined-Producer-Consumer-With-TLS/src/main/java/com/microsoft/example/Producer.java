package com.microsoft.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.SslConfigs;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.Properties;
import java.util.Random;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer
{
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    public static void produce(String brokers, String topicName) throws IOException
    {

        // Set properties used to configure the producer
        Properties properties = new Properties();
        // Set the brokers (bootstrap servers)
        properties.setProperty("bootstrap.servers", brokers);
        // Set how to serialize key/value pairs
        properties.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        // specify the protocol for Domain Joined TLS Encrypted clusters
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        properties.setProperty("ssl.mechanism", "GSSAPI");
        properties.setProperty("sasl.kerberos.service.name", "kafka");
        // specify the Truststore location and password
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/sshuser/ssl/kafka.client.truststore.jks");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "MyClientPassword123");
        // specify the Keystore location and password
        properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,"/home/sshuser/ssl/kafka.client.keystore.jks");
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "MyClientPassword123");
        // specify the key password
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "MyClientPassword123");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // So we can generate random sentences
        Random random = new Random();
        String[] sentences = new String[] {
                "the cow jumped over the moon",
                "an apple a day keeps the doctor away",
                "four score and seven years ago",
                "snow white and the seven dwarfs",
                "i am at two with nature"
        };

        String progressAnimation = "|/-\\";
        int numberOfMessages = 100;
        // Produce a bunch of records
        for(int i = 0; i < numberOfMessages; i++) {
            // Pick a sentence at random
            String sentence = sentences[random.nextInt(sentences.length)];
            // Send the sentence to the test topic
            try {
                producer.send(new ProducerRecord<String, String>(topicName, sentence)).get();
            } catch (Exception exception) {
                logger.error("Exception while producing messages: ", exception);
                throw new IOException(exception.toString());
            }
            String progressBar = "\r" + progressAnimation.charAt(i % progressAnimation.length()) + " " + i;
            System.out.write(progressBar.getBytes());
        }
        logger.info("Produced Messages: " + numberOfMessages);
    }
}
