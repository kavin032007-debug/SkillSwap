package com.skillswap.repository;

import com.skillswap.model.*;
import com.skillswap.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-backed persistent implementation of {@link ProposalRepository}.
 * Resolves member references using the associated {@link MemberRepository}.
 */
public class FilePersistedProposalRepository implements ProposalRepository {
    private final Path storagePath;
    private final MemberRepository memberRepository;
    private final Map<String, SwapProposal> proposalsById = new ConcurrentHashMap<>();

    public FilePersistedProposalRepository(Path storagePath, MemberRepository memberRepository) {
        this.storagePath = storagePath;
        this.memberRepository = memberRepository;
        load();
    }

    @Override
    public synchronized SwapProposal save(SwapProposal proposal) {
        Objects.requireNonNull(proposal, "Proposal cannot be null");
        proposalsById.put(proposal.getId(), proposal);
        flush();
        return proposal;
    }

    @Override
    public Optional<SwapProposal> findById(String id) {
        if (id == null) return Optional.empty();
        SwapProposal p = proposalsById.get(id);
        if (p != null) {
            p.checkExpiration();
        }
        return Optional.ofNullable(p);
    }

    @Override
    public List<SwapProposal> findByMemberId(String memberId) {
        if (memberId == null) return Collections.emptyList();
        List<SwapProposal> list = new ArrayList<>();
        for (SwapProposal p : proposalsById.values()) {
            p.checkExpiration();
            if (p.getCycle().containsMemberId(memberId)) {
                list.add(p);
            }
        }
        list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return list;
    }

    @Override
    public List<SwapProposal> findActiveOrPendingByMemberId(String memberId) {
        if (memberId == null) return Collections.emptyList();
        List<SwapProposal> list = new ArrayList<>();
        for (SwapProposal p : proposalsById.values()) {
            p.checkExpiration();
            ProposalStatus status = p.getStatus();
            if ((status == ProposalStatus.PENDING || status == ProposalStatus.ACTIVE) &&
                p.getCycle().containsMemberId(memberId)) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public List<SwapProposal> findAll() {
        for (SwapProposal p : proposalsById.values()) {
            p.checkExpiration();
        }
        List<SwapProposal> list = new ArrayList<>(proposalsById.values());
        list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return list;
    }

    @Override
    public synchronized boolean deleteById(String id) {
        if (id == null) return false;
        SwapProposal removed = proposalsById.remove(id);
        if (removed != null) {
            flush();
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        proposalsById.clear();
        flush();
    }

    private void load() {
        if (storagePath == null || !Files.exists(storagePath)) {
            return;
        }
        try {
            String json = Files.readString(storagePath);
            List<Object> list = JsonUtil.parseList(json);
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    SwapProposal p = deserializeProposal(map);
                    if (p != null) {
                        proposalsById.put(p.getId(), p);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: failed to load proposals from " + storagePath + ": " + e.getMessage());
        }
    }

    private synchronized void flush() {
        if (storagePath == null) return;
        try {
            File parent = storagePath.toFile().getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            List<Map<String, Object>> serialized = new ArrayList<>();
            for (SwapProposal p : proposalsById.values()) {
                serialized.add(serializeProposal(p));
            }
            String json = JsonUtil.toJson(serialized);
            Files.writeString(storagePath, json);
        } catch (IOException e) {
            System.err.println("Warning: failed to write proposals to " + storagePath + ": " + e.getMessage());
        }
    }

    private Map<String, Object> serializeProposal(SwapProposal p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("initiatorMemberId", p.getInitiatorMemberId());
        map.put("status", p.getStatus().name());
        map.put("createdAt", p.getCreatedAt());
        map.put("expiresAt", p.getExpiresAt());
        map.put("completedAt", p.getCompletedAt());

        Map<String, Object> responses = new LinkedHashMap<>();
        for (Map.Entry<String, ResponseStatus> entry : p.getMemberResponses().entrySet()) {
            responses.put(entry.getKey(), entry.getValue().name());
        }
        map.put("memberResponses", responses);

        List<Map<String, Object>> links = new ArrayList<>();
        for (SwapLink link : p.getCycle().getLinks()) {
            Map<String, Object> lMap = new LinkedHashMap<>();
            lMap.put("learnerId", link.getLearner().getId());
            lMap.put("teacherId", link.getTeacher().getId());
            lMap.put("skillName", link.getSkill().getName());
            lMap.put("skillCategory", link.getSkill().getCategory().name());
            lMap.put("skillLevel", link.getSkill().getLevel().name());
            links.add(lMap);
        }
        map.put("links", links);

        return map;
    }

    private SwapProposal deserializeProposal(Map<?, ?> map) {
        String id = (String) map.get("id");
        String initiatorId = (String) map.get("initiatorMemberId");
        String statusStr = (String) map.get("status");
        ProposalStatus status = ProposalStatus.valueOf(statusStr != null ? statusStr : "PENDING");
        long createdAt = ((Number) map.get("createdAt")).longValue();
        long expiresAt = ((Number) map.get("expiresAt")).longValue();
        Long completedAt = map.get("completedAt") != null ? ((Number) map.get("completedAt")).longValue() : null;

        Map<String, ResponseStatus> responses = new LinkedHashMap<>();
        if (map.get("memberResponses") instanceof Map<?, ?> rMap) {
            for (Map.Entry<?, ?> e : rMap.entrySet()) {
                responses.put((String) e.getKey(), ResponseStatus.valueOf((String) e.getValue()));
            }
        }

        List<SwapLink> links = new ArrayList<>();
        if (map.get("links") instanceof List<?> lList) {
            for (Object item : lList) {
                if (item instanceof Map<?, ?> lMap) {
                    String learnerId = (String) lMap.get("learnerId");
                    String teacherId = (String) lMap.get("teacherId");
                    String skillName = (String) lMap.get("skillName");
                    SkillCategory cat = SkillCategory.fromString((String) lMap.get("skillCategory"));
                    SkillLevel lvl = SkillLevel.fromString((String) lMap.get("skillLevel"));

                    Member learner = memberRepository.findById(learnerId).orElse(null);
                    Member teacher = memberRepository.findById(teacherId).orElse(null);
                    if (learner == null || teacher == null) {
                        return null; // Missing member reference
                    }
                    links.add(new SwapLink(learner, teacher, new Skill(skillName, cat, lvl)));
                }
            }
        }

        if (links.size() < 2) {
            return null;
        }

        SwapCycle cycle = new SwapCycle(links);
        return new SwapProposal(id, cycle, initiatorId, responses, status, createdAt, expiresAt, completedAt);
    }
}
