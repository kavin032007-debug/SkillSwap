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
 * File-backed persistent implementation of {@link MemberRepository}.
 * Uses thread-safe in-memory caching and flushes changes to a JSON file.
 */
public class FilePersistedMemberRepository implements MemberRepository {
    private final Path storagePath;
    private final Map<String, Member> membersById = new ConcurrentHashMap<>();

    public FilePersistedMemberRepository(Path storagePath) {
        this.storagePath = storagePath;
        load();
    }

    @Override
    public synchronized Member save(Member member) {
        Objects.requireNonNull(member, "Member cannot be null");
        membersById.put(member.getId(), member);
        flush();
        return member;
    }

    @Override
    public Optional<Member> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(membersById.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        if (name == null) return Optional.empty();
        for (Member m : membersById.values()) {
            if (m.getName().equalsIgnoreCase(name.trim())) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(membersById.values());
    }

    @Override
    public synchronized boolean deleteById(String id) {
        if (id == null) return false;
        Member removed = membersById.remove(id);
        if (removed != null) {
            flush();
            return true;
        }
        return false;
    }

    @Override
    public int count() {
        return membersById.size();
    }

    public synchronized void clear() {
        membersById.clear();
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
                    Member m = deserializeMember(map);
                    if (m != null) {
                        membersById.put(m.getId(), m);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: failed to load members from " + storagePath + ": " + e.getMessage());
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
            for (Member m : membersById.values()) {
                serialized.add(serializeMember(m));
            }
            String json = JsonUtil.toJson(serialized);
            Files.writeString(storagePath, json);
        } catch (IOException e) {
            System.err.println("Warning: failed to write members to " + storagePath + ": " + e.getMessage());
        }
    }

    private Map<String, Object> serializeMember(Member m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("pinHash", m.getPinHash());
        map.put("pinSalt", m.getPinSalt());
        map.put("contact", m.getContact());
        map.put("bio", m.getBio());
        map.put("weeklyAvailabilityHours", m.getWeeklyAvailabilityHours());
        map.put("availabilityNote", m.getAvailabilityNote());
        map.put("lockedInProposalId", m.getLockedInProposalId());

        List<Map<String, Object>> teachesList = new ArrayList<>();
        for (Skill s : m.getTeaches()) {
            teachesList.add(serializeSkill(s));
        }
        map.put("teaches", teachesList);

        List<Map<String, Object>> wantsList = new ArrayList<>();
        for (Skill s : m.getWants()) {
            wantsList.add(serializeSkill(s));
        }
        map.put("wants", wantsList);

        List<Map<String, Object>> ratingsList = new ArrayList<>();
        for (Rating r : m.getRatings()) {
            Map<String, Object> rMap = new LinkedHashMap<>();
            rMap.put("fromMemberId", r.getFromMemberId());
            rMap.put("fromMemberName", r.getFromMemberName());
            rMap.put("score", r.getScore());
            rMap.put("feedback", r.getFeedback());
            rMap.put("timestamp", r.getTimestamp());
            ratingsList.add(rMap);
        }
        map.put("ratings", ratingsList);

        return map;
    }

    private Map<String, Object> serializeSkill(Skill s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", s.getName());
        map.put("category", s.getCategory().name());
        map.put("level", s.getLevel().name());
        return map;
    }

    @SuppressWarnings("unchecked")
    private Member deserializeMember(Map<?, ?> map) {
        String id = (String) map.get("id");
        String name = (String) map.get("name");
        String pinHash = (String) map.get("pinHash");
        String pinSalt = (String) map.get("pinSalt");
        String contact = (String) map.get("contact");
        if (id == null || name == null) return null;

        Member member = new Member(id, name, pinHash, pinSalt, contact);
        if (map.containsKey("bio")) {
            member.setBio((String) map.get("bio"));
        }
        if (map.containsKey("weeklyAvailabilityHours")) {
            Object h = map.get("weeklyAvailabilityHours");
            if (h instanceof Number n) {
                member.setWeeklyAvailabilityHours(n.intValue());
            }
        }
        if (map.containsKey("availabilityNote")) {
            member.setAvailabilityNote((String) map.get("availabilityNote"));
        }
        if (map.containsKey("lockedInProposalId")) {
            member.lockInProposal((String) map.get("lockedInProposalId"));
        }

        if (map.get("teaches") instanceof List<?> tList) {
            for (Object item : tList) {
                if (item instanceof Map<?, ?> sMap) {
                    member.addTeaches(deserializeSkill(sMap));
                }
            }
        }
        if (map.get("wants") instanceof List<?> wList) {
            for (Object item : wList) {
                if (item instanceof Map<?, ?> sMap) {
                    member.addWants(deserializeSkill(sMap));
                }
            }
        }
        if (map.get("ratings") instanceof List<?> rList) {
            for (Object item : rList) {
                if (item instanceof Map<?, ?> rMap) {
                    String fromId = (String) rMap.get("fromMemberId");
                    String fromName = (String) rMap.get("fromMemberName");
                    int score = ((Number) rMap.get("score")).intValue();
                    String feedback = (String) rMap.get("feedback");
                    long timestamp = ((Number) rMap.get("timestamp")).longValue();
                    member.addRating(new Rating(fromId, fromName, score, feedback, timestamp));
                }
            }
        }
        return member;
    }

    private Skill deserializeSkill(Map<?, ?> map) {
        String name = (String) map.get("name");
        SkillCategory cat = SkillCategory.fromString((String) map.get("category"));
        SkillLevel lvl = SkillLevel.fromString((String) map.get("level"));
        return new Skill(name, cat, lvl);
    }
}
