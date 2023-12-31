package com.blackshoe.moongklheremobileapi.repository;

import com.blackshoe.moongklheremobileapi.entity.Post;
import com.blackshoe.moongklheremobileapi.entity.User;
import com.blackshoe.moongklheremobileapi.entity.View;
import org.reactivestreams.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViewRepository extends JpaRepository<View, UUID> {
    @Query("SELECT v FROM View v WHERE v.post = ?1 AND v.user = ?2")
    Optional<View> findByPostAndUser(Post post, User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM View v WHERE v.user = :user")
    void deleteAllByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM View v WHERE v.post = :post")
    void deleteAllByPost(Post post);
}
