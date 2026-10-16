package com.skillswap.service;

import com.skillswap.model.*;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.util.SecurityUtil;

/**
 * Seeds rich demo data with 12+ students and overlapping skills,
 * ensuring immediate 2-way, 3-way (Asha-Ravi-Meena), and 4-way barter swaps.
 */
public class DemoDataSeeder {

    public static void seedIfEmpty(MemberRepository memberRepo, ProposalRepository proposalRepo) {
        if (memberRepo.count() > 0) {
            return;
        }
        reseed(memberRepo, proposalRepo);
    }

    public static void reseed(MemberRepository memberRepo, ProposalRepository proposalRepo) {
        // Clear existing
        if (memberRepo instanceof com.skillswap.repository.FilePersistedMemberRepository fpmr) {
            fpmr.clear();
        }
        if (proposalRepo instanceof com.skillswap.repository.FilePersistedProposalRepository fppr) {
            fppr.clear();
        }

        String pin = "1234";

        // 1. Asha (Core 3-Way: Asha -> Ravi -> Meena -> Asha; also 2-Way with Carlos)
        Member asha = createMember("mem-asha", "Asha", pin, "asha@campus.edu",
                "Computer Science sophomore passionate about acoustic guitar and clean code.", 6, "Weekends & Evenings");
        asha.addTeaches(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.ADVANCED));
        asha.addTeaches(new Skill("Python", SkillCategory.PROGRAMMING, SkillLevel.INTERMEDIATE));
        asha.addWants(new Skill("Excel", SkillCategory.BUSINESS, SkillLevel.BEGINNER));
        asha.addWants(new Skill("UI Design", SkillCategory.DESIGN, SkillLevel.BEGINNER));
        asha.addRating(new Rating("mem-carlos", "Carlos", 5, "Incredible guitar teacher!", System.currentTimeMillis() - 86400000L));
        memberRepo.save(asha);

        // 2. Ravi
        Member ravi = createMember("mem-ravi", "Ravi", pin, "ravi@campus.edu",
                "Finance major who loves spreadsheets, automation, and outdoor photography.", 8, "Tuesday & Thursday evenings");
        ravi.addTeaches(new Skill("Excel", SkillCategory.BUSINESS, SkillLevel.ADVANCED));
        ravi.addTeaches(new Skill("Data Analysis", SkillCategory.ACADEMICS, SkillLevel.INTERMEDIATE));
        ravi.addWants(new Skill("Photography", SkillCategory.DESIGN, SkillLevel.BEGINNER));
        ravi.addWants(new Skill("Spanish", SkillCategory.LANGUAGE, SkillLevel.BEGINNER));
        ravi.addRating(new Rating("mem-asha", "Asha", 5, "Turned complex VLOOKUP into child's play.", System.currentTimeMillis() - 172800000L));
        memberRepo.save(ravi);

        // 3. Meena
        Member meena = createMember("mem-meena", "Meena", pin, "meena@campus.edu",
                "Visual Arts student and native Spanish speaker who wants to pick up acoustic guitar.", 5, "Flexible afternoons");
        meena.addTeaches(new Skill("Photography", SkillCategory.DESIGN, SkillLevel.ADVANCED));
        meena.addTeaches(new Skill("Spanish", SkillCategory.LANGUAGE, SkillLevel.INTERMEDIATE));
        meena.addWants(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.BEGINNER));
        meena.addWants(new Skill("Chess", SkillCategory.LIFESTYLE, SkillLevel.BEGINNER));
        meena.addRating(new Rating("mem-ravi", "Ravi", 5, "Amazing composition techniques!", System.currentTimeMillis() - 250000000L));
        memberRepo.save(meena);

        // 4. Carlos (Direct 2-way with Asha: UI Design <-> Python)
        Member carlos = createMember("mem-carlos", "Carlos", pin, "carlos@campus.edu",
                "Product design junior looking to code my own UI prototypes in Python.", 6, "Friday & Saturday");
        carlos.addTeaches(new Skill("UI Design", SkillCategory.DESIGN, SkillLevel.ADVANCED));
        carlos.addTeaches(new Skill("Figma", SkillCategory.DESIGN, SkillLevel.INTERMEDIATE));
        carlos.addWants(new Skill("Python", SkillCategory.PROGRAMMING, SkillLevel.BEGINNER));
        memberRepo.save(carlos);

        // 5. Diana
        Member diana = createMember("mem-diana", "Diana", pin, "diana@campus.edu",
                "Literature enthusiast and bilingual tutor wanting to analyze literary data.", 4, "Wednesday & Friday");
        diana.addTeaches(new Skill("French", SkillCategory.LANGUAGE, SkillLevel.ADVANCED));
        diana.addTeaches(new Skill("Creative Writing", SkillCategory.ARTS_CRAFTS, SkillLevel.INTERMEDIATE));
        diana.addWants(new Skill("Data Analysis", SkillCategory.ACADEMICS, SkillLevel.BEGINNER));
        memberRepo.save(diana);

        // 6. Evan
        Member evan = createMember("mem-evan", "Evan", pin, "evan@campus.edu",
                "Competitive chess player and math tutor eager to converse in French.", 7, "Weekends");
        evan.addTeaches(new Skill("Chess", SkillCategory.LIFESTYLE, SkillLevel.ADVANCED));
        evan.addTeaches(new Skill("Calculus", SkillCategory.ACADEMICS, SkillLevel.INTERMEDIATE));
        evan.addWants(new Skill("French", SkillCategory.LANGUAGE, SkillLevel.BEGINNER));
        memberRepo.save(evan);

        // 7. Fatima
        Member fatima = createMember("mem-fatima", "Fatima", pin, "fatima@campus.edu",
                "Debate team captain coaching public speaking, wanting to learn digital illustration.", 5, "Monday evenings");
        fatima.addTeaches(new Skill("Public Speaking", SkillCategory.BUSINESS, SkillLevel.ADVANCED));
        fatima.addTeaches(new Skill("Marketing", SkillCategory.BUSINESS, SkillLevel.INTERMEDIATE));
        fatima.addWants(new Skill("UI Design", SkillCategory.DESIGN, SkillLevel.BEGINNER));
        memberRepo.save(fatima);

        // 8. George
        Member george = createMember("mem-george", "George", pin, "george@campus.edu",
                "AI researcher who wants to overcome stage fright and present research confidently.", 6, "Weekdays after 6pm");
        george.addTeaches(new Skill("Machine Learning", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        george.addTeaches(new Skill("Java", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        george.addWants(new Skill("Public Speaking", SkillCategory.BUSINESS, SkillLevel.BEGINNER));
        memberRepo.save(george);

        // 9. Hannah
        Member hannah = createMember("mem-hannah", "Hannah", pin, "hannah@campus.edu",
                "Conservatory pianist fascinated by neural networks and generative sound synthesis.", 4, "Sunday afternoons");
        hannah.addTeaches(new Skill("Piano", SkillCategory.MUSIC, SkillLevel.ADVANCED));
        hannah.addTeaches(new Skill("Music Theory", SkillCategory.MUSIC, SkillLevel.INTERMEDIATE));
        hannah.addWants(new Skill("Machine Learning", SkillCategory.PROGRAMMING, SkillLevel.BEGINNER));
        memberRepo.save(hannah);

        // 10. Ian
        Member ian = createMember("mem-ian", "Ian", pin, "ian@campus.edu",
                "Culinary enthusiast offering artisanal baking and nutrition coaching, wanting to play piano.", 5, "Weekends");
        ian.addTeaches(new Skill("Cooking & Baking", SkillCategory.LIFESTYLE, SkillLevel.ADVANCED));
        ian.addTeaches(new Skill("Nutrition", SkillCategory.FITNESS_WELLNESS, SkillLevel.INTERMEDIATE));
        ian.addWants(new Skill("Piano", SkillCategory.MUSIC, SkillLevel.BEGINNER));
        memberRepo.save(ian);

        // 11. Jin
        Member jin = createMember("mem-jin", "Jin", pin, "jin@campus.edu",
                "Motion designer and YouTuber who wants to master healthy meal prep on a budget.", 6, "Flexible");
        jin.addTeaches(new Skill("Video Editing", SkillCategory.DESIGN, SkillLevel.ADVANCED));
        jin.addTeaches(new Skill("Animation", SkillCategory.DESIGN, SkillLevel.INTERMEDIATE));
        jin.addWants(new Skill("Cooking & Baking", SkillCategory.LIFESTYLE, SkillLevel.BEGINNER));
        memberRepo.save(jin);

        // 12. Kira
        Member kira = createMember("mem-kira", "Kira", pin, "kira@campus.edu",
                "Certified yoga instructor aiming to build a high-quality video channel.", 7, "Morning slots");
        kira.addTeaches(new Skill("Yoga & Meditation", SkillCategory.FITNESS_WELLNESS, SkillLevel.ADVANCED));
        kira.addWants(new Skill("Video Editing", SkillCategory.DESIGN, SkillLevel.BEGINNER));
        memberRepo.save(kira);
    }

    private static Member createMember(String id, String name, String pin, String contact, String bio, int hours, String note) {
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashPin(pin, salt);
        Member m = new Member(id, name, hash, salt, contact);
        m.setBio(bio);
        m.setWeeklyAvailabilityHours(hours);
        m.setAvailabilityNote(note);
        return m;
    }
}
