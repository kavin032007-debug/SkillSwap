package com.skillswap.repository;

import com.skillswap.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing persistent {@link Member} entities.
 */
public interface MemberRepository {

    /**
     * Saves or updates a member.
     */
    Member save(Member member);

    /**
     * Finds a member by unique ID.
     */
    Optional<Member> findById(String id);

    /**
     * Finds a member by name (case-insensitive).
     */
    Optional<Member> findByName(String name);

    /**
     * Returns all registered members.
     */
    List<Member> findAll();

    /**
     * Removes a member by ID.
     */
    boolean deleteById(String id);

    /**
     * Returns total count of registered members.
     */
    int count();
}
