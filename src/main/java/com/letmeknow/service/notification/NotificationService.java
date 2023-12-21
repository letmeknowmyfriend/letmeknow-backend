package com.letmeknow.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.letmeknow.dto.NotificationDtoWithBoardViewUrlAndArticleDto;
import com.letmeknow.entity.Article;
import com.letmeknow.entity.Board;
import com.letmeknow.entity.notification.Notification;
import com.letmeknow.entity.notification.Subscription;
import com.letmeknow.exception.member.NoSuchMemberException;
import com.letmeknow.repository.member.MemberRepository;
import com.letmeknow.repository.notification.NotificationRepository;
import com.letmeknow.repository.notification.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.auth.messages.MemberMessages.MEMBER;
import static com.letmeknow.message.messages.Messages.NOT_EXISTS;
import static com.letmeknow.message.messages.Messages.SUCH;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    public List<NotificationDtoWithBoardViewUrlAndArticleDto> findWithNoOffset(Long lastId, Long pageSize, String email) throws NoSuchMemberException {
        Long memberId = memberRepository.findNotDeletedIdByEmail(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        List<Notification> withNoOffset = notificationRepository.findByNoOffsetWithArticle(lastId, pageSize, memberId);

        List<NotificationDtoWithBoardViewUrlAndArticleDto> collect = withNoOffset.stream()
            .map(Notification::toDtoWithBoardViewUrlAndArticleDto)
            .collect(Collectors.toList());

        return collect;
    }

    @Transactional
    public void readNotification(Long notificationId, String email) throws NoSuchMemberException {
        Long memberId = memberRepository.findNotDeletedIdByEmail(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        notificationRepository.readNotification(notificationId, memberId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, String email) throws NoSuchMemberException {
        Long memberId = memberRepository.findNotDeletedIdByEmail(email)
            .orElseThrow(() -> new NoSuchMemberException(new StringBuffer().append(SUCH.getMessage()).append(MEMBER.getMessage()).append(NOT_EXISTS.getMessage()).toString()));

        notificationRepository.deleteNotification(notificationId, memberId);
    }

    @Transactional
    public void saveAndSendNotifications(Board board, List<Article> newArticles) {
        // 게시글 id를 가지고 있는 구독 정보를 찾는다.
        List<Subscription> subscriptions = subscriptionRepository.findByBoardId(board.getId());

        List<Notification> newNotifications = new ArrayList<>();

        // 해당 게시판을 구독한 회원들에게 Notification를 생성한다.
        for (Subscription subscription : subscriptions) {
            for (Article article : newArticles) {
                Notification notification = Notification.builder()
                    .memberId(subscription.getMember().getId())
                    .board(board)
                    .article(article)
                    .build();

                newNotifications.add(notification);
            }
        }

        // 생성한 Notification을 저장한다.
        notificationRepository.saveAll(newNotifications);

        // 푸시 알림 보내기
        try {
        FirebaseMessaging.getInstance().send(Message.builder()
            .setNotification(com.google.firebase.messaging.Notification.builder()
                .setTitle(board.getBoardName() + "에 " + newArticles.size() + "개의 새로운 글이 올라왔습니다!")
                .setBody("확인해보세요!")
                .build())
            .setTopic(String.valueOf(board.getId()))
            .build());
        } catch (FirebaseMessagingException e) {
            // 메시지 못 보내도 크롤링은 진행되어야 하므로 로그만 남긴다.
            log.warn(e.getMessage());
        }
    }
}
