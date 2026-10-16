package com.skillswap.service;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.util.SecurityUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles member authentication, PIN hashing, credential verification, and registration.
 */
public class AuthService {
    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Registers a new student member with hashed PIN credentials.
     */
    public synchronized Member register(String name, String rawPin, String contact, String bio) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (rawPin == null || rawPin.trim().length() < 4) {
            throw new IllegalArgumentException("PIN must be at least 4 digits");
        }
        if (memberRepository.findByName(name.trim()).isPresent()) {
            throw new IllegalArgumentException("A member with the name '" + name.trim() + "' already exists");
        }

        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPin(rawPin.trim(), salt);
        String id = "mem-" + UUID.randomUUID().toString().substring(0, 8);

        Member member = new Member(id, name.trim(), hash, salt, contact != null ? contact.trim() : "");
        if (bio != null && !bio.trim().isEmpty()) {
            member.setBio(bio.trim());
        }

        return memberRepository.save(member);
    }

    /**
     * Authenticates a member by name and raw PIN.
     */
    public Optional<Member> authenticate(String name, String rawPin) {
        if (name == null || rawPin == null) {
            return Optional.empty();
        }
        Optional<Member> opt = memberRepository.findByName(name.trim());
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Member member = opt.get();
        if (SecurityUtil.verifyPin(rawPin.trim(), member.getPinSalt(), member.getPinHash())) {
            return Optional.of(member);
        }
        return Optional.empty();
    }
}
