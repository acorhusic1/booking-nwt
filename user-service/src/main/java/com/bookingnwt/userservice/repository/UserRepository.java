package com.bookingnwt.userservice.repository;

import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Custom JPQL query — search users by role and/or active status.
     * Both parameters are optional: if null, that filter is skipped.
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> searchByRoleAndStatus(
            @Param("role") UserRole role,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(value = "User.withDetails", type = org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") Long id);
}
