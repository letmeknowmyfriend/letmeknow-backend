package com.letmeknow.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.letmeknow.auth.repository.devicetoken.DeviceTokenRepository;
import com.letmeknow.entity.Board;
import com.letmeknow.entity.member.Member;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.entity.notification.Subscription;
import com.letmeknow.exception.NoSuchBoardException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.subscription.SubscriptionException;
import com.letmeknow.repository.board.BoardRepository;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.notification.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.message.messages.BoardMessages.BOARD;
import static com.letmeknow.message.messages.MemberMessage.CONSENT_TO_PUSH_NOTIFICATION;
import static com.letmeknow.message.messages.Messages.*;
import static com.letmeknow.message.messages.NotificationMessages.FCM;
import static com.letmeknow.message.messages.SubscriptionMessages.SUBSCRIPTION;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * 회원이 구독한 모든 토픽에 구독한다.
     * @param deviceToken
     * @param member
     */
    @Transactional
    public void subscribeToAllTopics(String deviceToken, Member member) throws IllegalArgumentException, SubscriptionException {
        // 회원이 구독에 동의했는지 확인한다.
        if (!member.getConsentToReceivePushNotification()) {
            return;
        }

        // 회원이 구독한 모든 토픽에 구독한다.
        for (Subscription subscription : member.getSubscriptions()) {
            String boardId = String.valueOf(subscription.getBoard().getId());
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(List.of(deviceToken), boardId);

                // 구독 실패하면,
                if (!response.getErrors().isEmpty()) {
                    // 해당 Device Token와 연결된 Refresh Token 삭제
                    deviceTokenRepository.deleteByDeviceToken(deviceToken);
                }
            }
            catch (FirebaseMessagingException e) {
                log.warn(e.getMessage());
                throw new SubscriptionException(new StringBuffer().append(SUBSCRIPTION.getMessage()).append(FAIL.getMessage()).toString());
            }
        }
    }

    @Transactional
    public void unsubscribeFromAllTopics(String deviceToken, Member member) {
        // 회원이 구독한 모든 토픽을 구독 해제한다.
        for (Subscription subscription : member.getSubscriptions()) {
            String boardId = String.valueOf(subscription.getBoard().getId());
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(deviceToken), boardId);

                // 구독 해제 실패하면,
                if (!response.getErrors().isEmpty()) {
                    // 해당 Device Token와 연결된 Refresh Token 삭제
                    deviceTokenRepository.deleteByDeviceToken(deviceToken);
                }
            }
            // 하나 구독 해제 실패해도, 계속 진행
            catch (FirebaseMessagingException e) {
                log.warn(e.getMessage());
            }
        }
    }

    @Transactional
    public void subscribeToTopic(String email, String boardId) throws NoSuchBoardException, NoSuchMemberException, SubscriptionException {
        // boardId가 존재하는지 확인
        Board board = boardRepository.findById(Long.parseLong(boardId))
            .orElseThrow(() -> new NoSuchBoardException(new StringBuffer().append(SUCH).append(BOARD).append(NOT_EXISTS).toString()));

        // memberWithSubscriptionAndDeviceToken
        Member member = memberRepository.findNotDeletedByEmailWithSubscriptionAndDeviceToken(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 회원이 구독에 동의했는지 확인한다.
        if (!member.getConsentToReceivePushNotification()) {
            throw new IllegalArgumentException(new StringBuffer().append(CONSENT_TO_PUSH_NOTIFICATION.getMessage()).append(NEED_TO.getMessage()).toString());
        }

        // 회원이 이미 구독한 토픽이 아니면
        if (!member.getSubscriptions().stream().anyMatch(subscription -> subscription.getBoard().getId().equals(Long.valueOf(boardId)))) {
            // 회원의 구독에 추가
            Subscription subscription = Subscription.builder()
                .member(member)
                .board(board)
                .build();

            subscriptionRepository.save(subscription);
        }

        // 회원이 갖고있는 deviceToken들에게 구독을 추가한다.
        List<String> deviceTokens = member.getDeviceTokens().stream()
            .map(DeviceToken::getDeviceToken)
            .collect(Collectors.toList());

        deviceTokens.stream().forEach(deviceToken -> {
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(List.of(deviceToken), boardId);

                if (response.getFailureCount() > 0) {
                    // 해당 Device Token와 연결된 Refresh Token 삭제
                    deviceTokenRepository.deleteByDeviceToken(deviceToken);
                }
            }
            catch (FirebaseMessagingException e) {
                throw new SubscriptionException(new StringBuffer().append(FCM.getMessage()).append(SUBSCRIPTION.getMessage()).append(FAIL.getMessage()).toString());
            }
        });
    }

    @Transactional
    public void unsubscribeFromTopic(String email, Long boardId) throws NoSuchMemberException, IllegalArgumentException, SubscriptionException {
        // memberWithSubscriptionAndDeviceToken
        Member member = memberRepository.findNotDeletedByEmailWithSubscriptionAndDeviceToken(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        // 회원이 갖고있는 deviceToken들에게 구독을 삭제한다.
        List<String> deviceTokens = member.getDeviceTokens().stream()
            .map(DeviceToken::getDeviceToken)
            .collect(Collectors.toList());

        deviceTokens.stream().forEach(deviceToken -> {
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(deviceToken), String.valueOf(boardId));

                if (response.getFailureCount() > 0) {
                    // 해당 Device Token와 연결된 Refresh Token 삭제
                    deviceTokenRepository.deleteByDeviceToken(deviceToken);
                }
            }
            catch (FirebaseMessagingException e) {
                throw new SubscriptionException(new StringBuffer().append(FCM.getMessage()).append(SUBSCRIPTION.getMessage()).append(FAIL.getMessage()).toString());
            }
        });

        // 회원의 구독을 삭제
        subscriptionRepository.deleteByMemberIdAndBoardId(member.getId(), boardId);
    }
}
