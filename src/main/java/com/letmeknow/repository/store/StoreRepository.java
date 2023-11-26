package com.letmeknow.repository.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import com.letmeknow.entity.Store;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryQueryDsl {

    Optional<Store> findStoreByName(@Param("storeName") String storeName);

    List<Store> findAllStoreByMemberId(long memberId);

    List<Store> findStoreByMemberId(long memberId);

    Optional<Store> findStoreByIdAndMemberId(long id, long memberId);

}
