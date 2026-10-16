package com.skillswap.repository;

import com.skillswap.model.SwapProposal;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing persistent {@link SwapProposal} entities.
 */
public interface ProposalRepository {

    /**
     * Saves or updates a swap proposal.
     */
    SwapProposal save(SwapProposal proposal);

    /**
     * Finds a proposal by unique ID.
     */
    Optional<SwapProposal> findById(String id);

    /**
     * Finds all proposals where the specified member is a participant.
     */
    List<SwapProposal> findByMemberId(String memberId);

    /**
     * Finds active or pending proposals for a given member.
     */
    List<SwapProposal> findActiveOrPendingByMemberId(String memberId);

    /**
     * Returns all recorded proposals.
     */
    List<SwapProposal> findAll();

    /**
     * Deletes a proposal by ID.
     */
    boolean deleteById(String id);
}
