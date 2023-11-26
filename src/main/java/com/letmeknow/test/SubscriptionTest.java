//package com.letmeknow.test;
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessagingException;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Test {
//    @EventListener(ApplicationReadyEvent.class)
//    public void test() throws FirebaseMessagingException {
//        // The topic name can be optionally prefixed with "/topics/".
//        String topic = "1";
//
//        // See documentation on defining a message payload.
//        Message message = Message.builder()
//            .setNotification(Notification.builder()
//                .setTitle("This is Cha Cha Testing!")
//                .setBody("Check one two three!")
//                .build())
//            .setTopic(topic)
//            .build();
//
//        // Send a message to the devices subscribed to the provided topic.
//        String response = FirebaseMessaging.getInstance().send(message);
//        // Response is a message ID string.
//        System.out.println("Successfully sent message: " + response);
//
//    }
//}
