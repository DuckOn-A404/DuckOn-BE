package com.a404.duckonback.support;

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
        boolean available = loaded != null && !loaded.isEmpty();

        // 1) preferred OFFICIAL
        Optional<String> s1 = available
            ? pick(loaded, preferredLocale, SubjectNameType.OFFICIAL)
            : subjectNameRepository.pickOfficialName(subject.getId(), preferredLocale);
        if (s1.isPresent()) return s1.get();

        // 2) native OFFICIAL
        Optional<String> s2 = available
            ? pick(loaded, subject.getNativeLocale(), SubjectNameType.OFFICIAL)
            : subjectNameRepository.pickOfficialName(subject.getId(), subject.getNativeLocale());
        if (s2.isPresent()) return s2.get();

        // 3) any OFFICIAL
        Optional<String> s3 = available
            ? pickAny(loaded, SubjectNameType.OFFICIAL)
            : subjectNameRepository.pickAnyByType(subject.getId(), SubjectNameType.OFFICIAL);
        if (s3.isPresent()) return s3.get();

        // 4) fallback order
        for (SubjectNameType t : new SubjectNameType[]{SubjectNameType.ROMANIZED, SubjectNameType.TRANSLATED, SubjectNameType.ALIAS}) {
            Optional<String> s = available
                ? pickAny(loaded, t)
                : subjectNameRepository.pickAnyByType(subject.getId(), t);
            if (s.isPresent()) return s.get();
        }
        // 마지막 최후의 수단
        return "Unknown";
    }

    private Optional<String> pick(Set<SubjectName> names, String locale, SubjectNameType type) {
        return names.stream()
            .filter(n -> n.getLocaleTag().equalsIgnoreCase(locale) && n.getNameType() == type)
            .sorted(Comparator.<SubjectName>comparingInt(n -> n.isPrimary() ? 0 : 1)
                .thenComparingInt(n -> n.getPriority()))
            .map(SubjectName::getName)
            .findFirst();
    }

    private Optional<String> pickAny(Set<SubjectName> names, SubjectNameType type) {
        return names.stream()
            .filter(n -> n.getNameType() == type)
            .sorted(Comparator.<SubjectName>comparingInt(n -> n.isPrimary() ? 0 : 1)
                .thenComparingInt(n -> n.getPriority()))
            .map(SubjectName::getName)
            .findFirst();
    }
}
