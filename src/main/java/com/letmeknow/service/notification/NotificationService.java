package com.letmeknow.service.notification;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.letmeknow.domain.Board;
import com.letmeknow.domain.member.Member;
import com.letmeknow.domain.notification.DeviceToken;
import com.letmeknow.domain.notification.Subscription;
import com.letmeknow.enumstorage.errormessage.member.MemberErrorMessage;
import com.letmeknow.enumstorage.errormessage.notification.SubscriptionErrorMessage;
import com.letmeknow.exception.BoardException;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.exception.notification.SubscriptionException;
import com.letmeknow.config.firebase.FirebaseConfig;
import com.letmeknow.repository.BoardRepository;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.notification.DeviceTokenRepository;
import com.letmeknow.repository.notification.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
    private final MemberRepository memberRepository;
    private final FirebaseConfig firebaseConfig;
    private final DeviceTokenRepository deviceTokenRepository;
    private final BoardRepository boardRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void whenMemberLogIn_AddDeviceToken_AddFCMSubscription(String email, String deviceToken) {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 회원과 관련된 토큰 테이블에 토큰이 있는지 확인하고 없으면 추가한다.
        DeviceToken newDeviceToken = getDeviceToken_AddIfNotExists(deviceToken, member);

        // 회원의 구독 테이블을 찾아 FCM에 해당 기기에 사용자의 구독을 전부 추가한다.
        member.getSubscriptions().forEach(subscription -> {
            try {
                firebaseConfig.firebaseMessaging().subscribeToTopicAsync(member.getDeviceTokens().stream()
                    .map((deviceToken1 -> deviceToken1.getDeviceToken()))
                    .collect(Collectors.toList()),
                    String.valueOf(subscription.getBoard().getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        deviceTokenRepository.save(newDeviceToken);
        memberRepository.save(member);
    }

    @Transactional
    public void whenMemberLogOut_DeleteDeviceToken_UnsubscribeFCM(String email, String deviceToken) {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 회원과 관련된 토큰 테이블에 토큰이 있는지 확인하고 있으면 삭제한다.
        Set<DeviceToken> deviceTokens = member.getDeviceTokens();
        deviceTokens.forEach(
            deviceToken1 -> {
                if (deviceToken1.getDeviceToken().equals(deviceToken)) {
                    member.removeDeviceToken(deviceToken1);
                }
            }
        );

        // 회원의 구독 테이블을 찾아 FCM에 구독을 취소한다.
        member.getSubscriptions().forEach(subscription -> {
            try {
                firebaseConfig.firebaseMessaging().unsubscribeFromTopicAsync(member.getDeviceTokens().stream()
                    .map((deviceToken1 -> deviceToken1.getDeviceToken()))
                    .collect(Collectors.toList()),
                    String.valueOf(subscription.getBoard().getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        memberRepository.save(member);
    }

    @Transactional
    public void subscribe(String email, String deviceToken, Long boardId) {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 회원과 관련된 토큰 테이블에 토큰이 있는지 확인하고 없으면 추가한다.
        DeviceToken newDeviceToken = getDeviceToken_AddIfNotExists(deviceToken, member);

        // 구독 정보가 이미 있는지 확인
        subscriptionRepository.findOneByMemberIdAndBoardId(member.getId(), boardId)
            .ifPresent(subscription -> {
                throw new SubscriptionException(SubscriptionErrorMessage.ALREADY_SUBSCRIBED.getMessage());
            });

        // 구독 정보를 DB에 저장한다.
        Board board = boardRepository.findOneById(boardId)
            .orElseThrow(() -> new BoardException("해당하는 게시판이 없습니다."));

        Subscription newSubscription = Subscription.builder()
                                            .member(member)
                                            .board(board)
                                    .build();

        // 테스트: 새로운 구독 정보를 저장할 때, DB에 두번 찌르는지 확인할 것.

        // 회원의 모든 토큰을 찾아 FCM에 구독을 추가한다.
        List<String> deviceTokens = member.getDeviceTokens().stream()
            .map((deviceToken1 -> deviceToken1.getDeviceToken()))
            .collect(Collectors.toList());

        try {
            TopicManagementResponse response = firebaseConfig.firebaseMessaging().subscribeToTopic(deviceTokens, String.valueOf(boardId));
            System.out.println(response.getSuccessCount());
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 기기 토큰이 새로 추가되었으면 저장한다.
        deviceTokenRepository.save(newDeviceToken);
        subscriptionRepository.save(newSubscription);
        memberRepository.save(member);
    }

    @Transactional
    public void unsubscribe(String email, String deviceToken, Long boardId) {
        // 회원을 DeviceToken과 Subscription을 fetchJoin하여 찾는다.
        Member member = memberRepository.findNotDeletedByEmailAndDeviceTokenAndSubscription(email)
            .orElseThrow(() -> new NoSuchMemberException(MemberErrorMessage.NO_SUCH_MEMBER_WITH_THAT_EMAIL.getMessage()));

        // 회원과 관련된 토큰 테이블에 토큰이 있는지 확인하고 없으면 추가한다.
        DeviceToken newDeviceToken = getDeviceToken_AddIfNotExists(deviceToken, member);

        // 회원 id와 게시판 id로 구독 정보를 찾는다.
        subscriptionRepository.findOneByMemberIdAndBoardId(member.getId(), boardId)
            .ifPresentOrElse(subscription -> {
                // 구독 정보를 DB에서 삭제한다.
                subscriptionRepository.delete(subscription);

                // 회원의 모든 토큰을 찾아 FCM에 구독을 취소한다.
                List<String> deviceTokens = member.getDeviceTokens().stream()
                    .map((deviceToken1 -> deviceToken1.getDeviceToken()))
                    .collect(Collectors.toList());

                try {
                    firebaseConfig.firebaseMessaging().unsubscribeFromTopicAsync(deviceTokens, String.valueOf(boardId));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            },
                // 구독 정보가 없으면 예외 발생
                () -> {
                throw new SubscriptionException(SubscriptionErrorMessage.NO_SUCH_SUBSCRIPTION.getMessage());
            }
        );

        // 기기 토큰이 새로 추가되었으면 저장한다.
        deviceTokenRepository.save(newDeviceToken);
        memberRepository.save(member);
    }

    private DeviceToken getDeviceToken_AddIfNotExists(String deviceToken, Member member) {
        // 회원과 관련된 토큰 테이블에 토큰이 있는지 확인하고 없으면 추가한다.
        List<DeviceToken> matchingDeviceToken = member.getDeviceTokens().stream()
            .filter(deviceToken1 -> deviceToken1.getDeviceToken().equals(deviceToken))
            .collect(Collectors.toList());

        // 없으면 추가한다.
        if (matchingDeviceToken.isEmpty()) {
            DeviceToken newDeviceToken = DeviceToken.builder()
                        .member(member)
                        .deviceToken(deviceToken)
                    .build();

            return newDeviceToken;
        }
        // 있으면
        else {
            return matchingDeviceToken.get(0);
        }
    }
}
