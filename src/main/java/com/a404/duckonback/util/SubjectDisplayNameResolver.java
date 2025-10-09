package com.a404.duckonback.util;

import com.a404.duckonback.entity.Subject;
import com.a404.duckonback.entity.SubjectName;
import com.a404.duckonback.enums.SubjectNameType;
import com.a404.duckonback.repository.SubjectNameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SubjectDisplayNameResolver {

    private final SubjectNameRepository subjectNameRepository;

    /** 정렬 정책: PRIMARY 먼저(true 먼저) → priority 작은 값 우선(ASC) */
    private static final Comparator<SubjectName> DISPLAY_ORDER =
        Comparator.comparing(SubjectName::isPrimary, Comparator.reverseOrder())
            .thenComparingInt(SubjectName::getPriority);

    /** names 컬렉션이 이미 로드되어 있으면 in-memory, 아니면 repo 조회로 */
    public String resolve(Subject subject, String preferredLocale) {
        Set<SubjectName> loaded = subject.getNames();
        boolean hasLoaded = loaded != null && !loaded.isEmpty();

        String pref = (preferredLocale == null || preferredLocale.isBlank())
            ? "ko"
            : preferredLocale.toLowerCase();

        // 1) preferred OFFICIAL
        if (hasLoaded) {
            Optional<String> s = pick(loaded, pref, SubjectNameType.OFFICIAL);
            if (s.isPresent()) return s.get();
        } else {
            var s = subjectNameRepository.pickOfficialName(subject.getId(), pref);
            if (s.isPresent()) return s.get();
        }

        // 2) native OFFICIAL
        if (hasLoaded) {
            Optional<String> s = pick(loaded, subject.getNativeLocale(), SubjectNameType.OFFICIAL);
            if (s.isPresent()) return s.get();
        } else {
            var s = subjectNameRepository.pickOfficialName(subject.getId(), subject.getNativeLocale());
            if (s.isPresent()) return s.get();
        }

        // 3) any OFFICIAL
        if (hasLoaded) {
            Optional<String> s = pickAny(loaded, SubjectNameType.OFFICIAL);
            if (s.isPresent()) return s.get();
        } else {
            var s = subjectNameRepository.pickAnyByType(subject.getId(), SubjectNameType.OFFICIAL);
            if (s.isPresent()) return s.get();
        }

        // 4) fallback (ROMANIZED → TRANSLATED → ALIAS)
        for (SubjectNameType t : new SubjectNameType[]{SubjectNameType.ROMANIZED, SubjectNameType.TRANSLATED, SubjectNameType.ALIAS}) {
            if (hasLoaded) {
                Optional<String> s = pickAny(loaded, t);
                if (s.isPresent()) return s.get();
            } else {
                var s = subjectNameRepository.pickAnyByType(subject.getId(), t);
                if (s.isPresent()) return s.get();
            }
        }
        return subject.getSlug(); // 최후 보루
    }

    private Optional<String> pick(Set<SubjectName> names, String locale, SubjectNameType type) {
        final String pref = locale == null ? "" : locale.toLowerCase();
        return names.stream()
            .filter(n -> n.getNameType() == type && localeMatches(n.getLocaleTag(), pref))
            .sorted(DISPLAY_ORDER)
            .map(SubjectName::getName)
            .findFirst();
    }

    private Optional<String> pickAny(Set<SubjectName> names, SubjectNameType type) {
        return names.stream()
            .filter(n -> n.getNameType() == type)
            .sorted(DISPLAY_ORDER)
            .map(SubjectName::getName)
            .findFirst();
    }

    /** ko vs ko-KR 같은 태그도 매칭되도록 느슨하게 비교 */
    private static boolean localeMatches(String candidate, String preferred) {
        if (candidate == null || preferred == null || preferred.isBlank()) return false;
        String c = candidate.toLowerCase();
        return c.equals(preferred) || c.startsWith(preferred + "-") || preferred.startsWith(c + "-");
    }
}
