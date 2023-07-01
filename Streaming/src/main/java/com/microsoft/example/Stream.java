package com.microsoft.example;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class Stream
{
    public static void main( String[] args ) {
        Properties streamsConfig = new Properties();
        String applicationID = "wordcount-example";
        String inputTopic = "test";
        String outputTopic = "wordcounts";
        // The name must be unique on the Kafka cluster
        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        // Brokers
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        // Zookeeper
        //streamsConfig.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, args[1]);
        // SerDes for key and values
        streamsConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sentences = builder.stream(inputTopic);
        KTable<String, Long> wordCounts = sentences
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .groupBy((key,word) -> word)
                .count();
        wordCounts.toStream()
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamsConfig);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
