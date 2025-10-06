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

    /** names 컬렉션이 이미 로드되어 있으면 in-memory, 아니면 repo 조회로 */
    public String resolve(Subject subject, String preferredLocale) {
        Set<SubjectName> loaded = subject.getNames();
        boolean hasLoaded = loaded != null && !loaded.isEmpty();
        String pref = (preferredLocale == null || preferredLocale.isBlank()) ? "ko" : preferredLocale.toLowerCase();

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
        return names.stream()
            .filter(n -> n.getNameType() == type && n.getLocaleTag().equalsIgnoreCase(locale))
            .sorted(Comparator.<SubjectName>comparingInt(n -> n.isPrimary() ? 0 : 1)
                .thenComparing(SubjectName::getPriority).reversed())
            .map(SubjectName::getName)
            .findFirst();
    }

    private Optional<String> pickAny(Set<SubjectName> names, SubjectNameType type) {
        return names.stream()
            .filter(n -> n.getNameType() == type)
            .sorted(Comparator.<SubjectName>comparingInt(n -> n.isPrimary() ? 0 : 1)
                .thenComparing(SubjectName::getPriority).reversed())
            .map(SubjectName::getName)
            .findFirst();
    }
}