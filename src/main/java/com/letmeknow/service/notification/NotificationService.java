package com.letmeknow.service.notification;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.letmeknow.domain.Board;
import com.letmeknow.domain.member.Member;
import com.letmeknow.domain.notification.Subscription;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.notification.DeviceTokenErrorMessage;
import com.letmeknow.exception.BoardException;
import com.letmeknow.exception.auth.jwt.NoSuchDeviceTokenException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.notification.NotificationException;
import com.letmeknow.config.firebase.FirebaseConfig;
import com.letmeknow.message.MessageMaker;
import com.letmeknow.repository.BoardRepository;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.notification.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.letmeknow.message.reason.BoardReason.BOARD;
import static com.letmeknow.message.Message.ALREADY;
import static com.letmeknow.message.reason.SubscriptionReason.SUBSCRIBED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
    private final MemberRepository memberRepository;
    private final FirebaseConfig firebaseConfig;
    private final BoardRepository boardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageMaker messageMaker;

    public void whenMemberLogIn_AddFCMSubscription(String email, String deviceToken) throws NoSuchMemberException, NoSuchDeviceTokenException {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        String existingDeviceToken = member.getDeviceTokens().stream()
            .filter(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))
            .map(deviceToken1 -> deviceToken1.getDeviceToken())
            .findFirst()
            .orElseThrow(() -> new NoSuchDeviceTokenException(DeviceTokenErrorMessage.NO_SUCH_DEVICE_TOKEN.getMessage()));

        // 회원의 구독 테이블을 찾아 FCM에 해당 기기에 사용자의 구독을 전부 추가한다.
        member.getSubscriptions().forEach(subscription -> {
            try {
                firebaseConfig.firebaseMessaging().subscribeToTopicAsync(List.of(existingDeviceToken), String.valueOf(subscription.getBoard().getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void whenMemberLogOut_UnsubscribeFCM_RemoveDeviceToken(String email, String deviceToken) throws NoSuchMemberException, NoSuchDeviceTokenException {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        String existingDeviceToken = member.getDeviceTokens().stream()
            .filter(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))
            .map(deviceToken1 -> deviceToken1.getDeviceToken())
            .findFirst()
            .orElseThrow(() -> new NoSuchDeviceTokenException(DeviceTokenErrorMessage.NO_SUCH_DEVICE_TOKEN.getMessage()));

        // 회원의 구독 테이블을 찾아 FCM에 구독을 취소한다.
        member.getSubscriptions().forEach(subscription -> {
            try {
                firebaseConfig.firebaseMessaging().unsubscribeFromTopicAsync(List.of(existingDeviceToken), String.valueOf(subscription.getBoard().getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // deviceToken을 삭제한다.
        member.removeDeviceToken(existingDeviceToken);
    }

    @Transactional
    public boolean subscribe(String email, Long boardId) throws NoSuchMemberException, NotificationException {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 구독 정보가 이미 있는지 확인
        boolean empty = subscriptionRepository.findOneByMemberIdAndBoardId(member.getId(), boardId).isEmpty();

        // 구독 정보가 이미 있으면
        if (!empty) {
            throw new NotificationException(messageMaker.add(ALREADY).add(SUBSCRIBED).add(BOARD).toString());
        }

        // 구독 정보를 DB에 저장한다.
        Board board = boardRepository.findOneById(boardId)
            .orElseThrow(() -> new BoardException("해당하는 게시판이 없습니다."));

        Subscription newSubscription = Subscription.builder()
                                            .member(member)
                                            .board(board)
                                    .build();

        subscriptionRepository.save(newSubscription);

        // 테스트: 새로운 구독 정보를 저장할 때, DB에 두번 찌르는지 확인할 것.

        // 회원의 모든 토큰을 찾아 FCM에 구독을 추가한다.
        List<String> deviceTokens = member.getDeviceTokens().stream()
            .map((deviceToken1 -> deviceToken1.getDeviceToken()))
            .collect(Collectors.toList());

        try {
            TopicManagementResponse response = firebaseConfig.firebaseMessaging().subscribeToTopic(deviceTokens, String.valueOf(boardId));

            return response.getErrors().isEmpty();
        } catch (IOException | FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void unsubscribe(String email, String deviceToken, Long boardId) throws NoSuchMemberException {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 회원 id와 게시판 id로 구독 정보를 찾는다.
        subscriptionRepository.findOneByMemberIdAndBoardId(member.getId(), boardId)
            .ifPresent(subscription -> {
                // 구독 정보를 DB에서 삭제한다.
                subscription.removeSubscription();

                // 회원의 모든 토큰을 찾아 FCM에 구독을 취소한다.
                List<String> deviceTokens = member.getDeviceTokens().stream()
                    .map((deviceToken1 -> deviceToken1.getDeviceToken()))
                    .collect(Collectors.toList());

                try {
                    firebaseConfig.firebaseMessaging().unsubscribeFromTopicAsync(deviceTokens, String.valueOf(boardId));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }
}
