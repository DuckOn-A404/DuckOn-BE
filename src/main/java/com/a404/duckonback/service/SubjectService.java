package com.a404.duckonback.service;

import com.a404.duckonback.dto.SubjectDTO;
import com.a404.duckonback.entity.Subject;
import com.a404.duckonback.entity.SubjectFollow;
import com.a404.duckonback.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

public interface SubjectService {

    Subject findById(Long subjectId);
    List<Long> findAllSubjectIdByUserId(Long id);
    SubjectDetailDTO getSubjectDetail(Long userId, Long subjectId);
    List<SubjectDTO> searchSubjects(String keyword);
    List<SubjectDTO> getRandomSubjects(int size);
    Subject createSubject(AdminSubjectRequestDTO dto);
    Subject updateSubject(Long subjectId, AdminSubjectRequestDTO dto);
    Subject patchSubject(Long subjectId, AdminSubjectPatchDTO dto);
    String findSlugById(Long subjectId);
    String slugify(String s);
    Page<SubjectDTO> getSubjects(Pageable pageable, String sort, String order, String keyword);
    Page<SubjectDTO> getSubjects(Pageable pageable);
}
