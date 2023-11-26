//package com.letmeknow.repository.store;
//
//import com.querydsl.core.types.Projections;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import com.letmeknow.dto.item.ItemCreationDto;
//import com.letmeknow.dto.store.StoreDto;
//import com.letmeknow.dto.order.OrderDto;
//
//import javax.persistence.EntityManager;
//import java.util.Map;
//import java.util.Optional;
//
//import static com.querydsl.core.group.GroupBy.groupBy;
//import static com.querydsl.core.group.GroupBy.list;
//import static com.letmeknow.domain.QStore.store;
//import static com.letmeknow.domain.QOrder.order;
//import static com.letmeknow.domain.QItem.item;
//
//@RequiredArgsConstructor
//public class StoreRepositoryImpl implements StoreRepositoryQueryDsl {
//    private final EntityManager em;
//
//    @Override
//    public Optional<StoreDto> findStoreDtoById(long id) {
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
//
//        //write code 1:N relation Dto using QueryDsl Projections
//        return Optional.ofNullable(
//                queryFactory
//                        .select(Projections.constructor(StoreDto.class,
//                                store.id,
//                                store.member.id,
//                                store.name,
//                                store.address,
//                                store.storeStatus,
//                                store.orders,
//                                store.items,
//                                store.tableNumbers
//                        ))
//                        .from(store)
//                        .join(store.orders, order)
//                        .where(store.id.eq(id))
//                        .fetchOne()
//        );
//
//        Map<Long, StoreDto> transform = queryFactory.from(store)
//                .leftJoin(store.orders, order)
//                .on(store.id.eq(order.store.id))
//                .leftJoin(store.items, item)
//                .on(store.id.eq(item.store.id))
//                .where(store.id.eq(id))
//                .transform(groupBy(store.id)
//                        .as(
//                                Projections.fields(StoreDto.class,
//                                        store.id.as("id"),
//                                        store.member.id.as("memberId"),
//                                        store.name.as("name"),
//                                        store.address.as("address"),
//                                        store.storeStatus.as("storeStatus"),
//                                        list(Projections.fields(OrderDto.class,
//                                                order.id.as("id")
//                                        )).as("orders"),
//                                        list(Projections.fields(ItemCreationDto.class,
//                                                item.id.as("id")
//                                        )).as("items"),
//                                        store.tableNumbers.as("tableNumbers")
//                                )));
//
//        return Optional.ofNullable(transform.get(id));
//    }
//}
