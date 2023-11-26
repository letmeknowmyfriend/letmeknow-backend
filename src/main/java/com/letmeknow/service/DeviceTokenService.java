package com.letmeknow.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.auth.repository.devicetoken.DeviceTokenRepository;
import com.letmeknow.entity.member.Member;
import com.letmeknow.message.messages.NotificationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.letmeknow.message.messages.Messages.NOT_FOUND;
import static com.letmeknow.message.messages.NotificationMessages.FCM;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeviceTokenService {
    public static final String DEVICE_TOKEN = "DeviceToken";
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * 회원의 DeviceToken가 DB에 저장되어 있는지 확인하고, 없으면 저장한다.
     * @param deviceToken
     * @param member
     */
    @Transactional
    public DeviceToken storeDeviceToken(String deviceToken, Member member) {
        // DB에 동일한 DeviceToken이 있는지 확인한다.
        List<DeviceToken> duplicatedDeviceTokens = member.getDeviceTokens().stream()
            .filter(deviceTokenEntity -> deviceTokenEntity.getDeviceToken().equals(deviceToken))
            .collect(Collectors.toList());

        // 동일한 DeviceToken이 없으면,
        if (duplicatedDeviceTokens.isEmpty()) {
            // 회원의 DeviceToken을 저장한다.
            DeviceToken newDeviceTokenEntity = DeviceToken.builder()
                .member(member)
                .deviceToken(deviceToken)
                .build();

            deviceTokenRepository.save(newDeviceTokenEntity);

            return newDeviceTokenEntity;
        }
        // 동일한 DeviceToken이 있으면,
        else {
            // DB에 있는 DeviceToken을 반환한다.
            return duplicatedDeviceTokens.get(0);
        }
    }

    public String extractDeviceTokenFromHeader(HttpServletRequest request) throws IllegalArgumentException {
        // 제발 들어올 때부터 검증하자
        String deviceToken = Optional.ofNullable(request.getHeader(DEVICE_TOKEN))
            .orElseThrow(() -> new IllegalArgumentException(new StringBuffer().append(FCM.getMessage()).append(NotificationMessages.DEVICE_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString()));

        // deviceToken 값 검증
        if (deviceToken.isBlank()) {
            throw new IllegalArgumentException(new StringBuffer().append(NotificationMessages.DEVICE_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }

        return deviceToken;
    }

    public void validateAndExtractDeviceToken(String deviceToken) throws IllegalArgumentException {
        try {
            FirebaseMessaging.getInstance().send(Message.builder().setToken(deviceToken).build());
        }
        catch (FirebaseMessagingException e) {
            throw new IllegalArgumentException(new StringBuffer().append(NotificationMessages.DEVICE_TOKEN.getMessage()).append(NOT_FOUND.getMessage()).toString());
        }
    }
}
