package com.letmeknow.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class FirebaseConfig {
    @Bean
    public GoogleCredentials getGoogleCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new FileInputStream("./letmeknow-bdaef-firebase-adminsdk-ogpdk-7f7f7325ab.json"))
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(getGoogleCredentials())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        return FirebaseMessaging.getInstance(firebaseApp());
    }
}
