package com.microsoft.example;

import java.io.IOException;
import java.util.UUID;
import java.io.PrintWriter;
import java.io.File;
import java.lang.Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Handle starting producer or consumer
public class Run {
    private static final Logger logger = LoggerFactory.getLogger(Run.class);
    public static void main(String[] args) throws IOException {
        if(args.length < 3) {
            usage();
        }
        // Get the brokers
        String brokers = args[2];
        String topicName = args[1];
        switch(args[0].toLowerCase()) {
            case "producer":
                Producer.produce(brokers, topicName);
                break;
            case "consumer":
                // Either a groupId was passed in, or we need a random one
                String groupId;
                if(args.length == 4) {
                    groupId = args[3];
                } else {
                    groupId = UUID.randomUUID().toString();
                }
                Consumer.consume(brokers, groupId, topicName);
                break;
            case "describe":
                AdminClientWrapper.describeTopics(brokers, topicName);
                break;
            case "create":
                AdminClientWrapper.createTopics(brokers, topicName);
                break;
            case "delete":
                AdminClientWrapper.deleteTopics(brokers, topicName);
                break;
            default:
                usage();
        }
        System.exit(0);
    }
    // Display usage
    public static void usage() {
        logger.info("Usage: \n kafka-example.jar <producer|consumer|describe|create|delete> <topicName> brokerhosts [groupid]");
        System.exit(1);
    }
}
