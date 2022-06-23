package com.example.chitchat.Tools;

/**
 * These messages are sent over the network for terminal and broker communication and synchronization
 */
public enum Messages {
    REGISTER,
    GET_BROKER_LIST,
    GET_ID_LIST,
    SENDING_NICK_NAME,
    PUSH_MESSAGE,
    PUSH,
    PULL,
    UNSUBSCRIBE,
    NO_SUCH_TOPIC,
    PUSH_STORY,
    SENDING_BROKER_LIST,
    SHOW_CONVERSATION_DATA,
    SENDING_ID_LIST,
    NOTIFY,
    RECEIVED_CHUNK,
    WAITING_FOR_ACK,
    RECEIVED_BROKER_LIST,
    GET_TOPIC_LIST,
    SENDING_TOPIC_LIST,
    PUSH_FILE,
    I_AM_THE_CORRECT_BROKER,
    I_AM_NOT_THE_CORRECT_BROKER,
    ACK,
    CLOSE_CONNECTION,
    FINISHED_COMMUNICATION_BETWEEN_BROKERS,
    RECEIVE_PULL_MESSAGE,
    NEW_TOPIC,
    FINISHED_OPERATION
}

