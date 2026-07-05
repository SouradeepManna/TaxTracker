package com.taxtracker.service;

import com.taxtracker.dto.UploadResponse;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.entity.UploadedDocumentEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.repository.UploadedDocumentRepository;
import com.taxtracker.service.impl.DocumentUploadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceImplTest {

    @Mock private UploadedDocumentRepository uploadedDocumentRepository;
    @Mock private Form90CRepository form90CRepository;
    @InjectMocks private DocumentUploadServiceImpl documentUploadService;

    @Test
    void upload_succeedsForValidPdf() throws TaxTrackerException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.pdf", "application/pdf", "dummy".getBytes());
        UploadedDocumentEntity saved = new UploadedDocumentEntity();
        saved.setDocumentId(1L);
        saved.setFileName("proof.pdf");
        when(uploadedDocumentRepository.save(any(UploadedDocumentEntity.class))).thenReturn(saved);
        Form90CEntity form = new Form90CEntity();
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.of(form));

        UploadResponse res = documentUploadService.upload("prajwal@taxtracker.com", file);

        assertEquals(1L, res.getDocumentId());
        assertEquals("proof.pdf", res.getFileName());
        assertEquals("proof.pdf", form.getDocumentName());
        verify(form90CRepository).save(form);
    }

    @Test
    void upload_succeedsForValidJpgWithNoForm() throws TaxTrackerException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "scan.jpg", "image/jpeg", "img".getBytes());
        UploadedDocumentEntity saved = new UploadedDocumentEntity();
        saved.setDocumentId(2L);
        saved.setFileName("scan.jpg");
        when(uploadedDocumentRepository.save(any(UploadedDocumentEntity.class))).thenReturn(saved);
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.empty());

        UploadResponse res = documentUploadService.upload("prajwal@taxtracker.com", file);

        assertEquals(2L, res.getDocumentId());
        verify(form90CRepository, never()).save(any());
    }

    @Test
    void upload_throwsWhenEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> documentUploadService.upload("prajwal@taxtracker.com", file));
        assertEquals("app.message.file.not.uploaded", ex.getMessage());
    }

    @Test
    void upload_throwsWhenNull() {
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> documentUploadService.upload("prajwal@taxtracker.com", null));
        assertEquals("app.message.file.not.uploaded", ex.getMessage());
    }

    @Test
    void upload_throwsWhenTooLarge() {
        byte[] big = new byte[(int) (2L * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", big);
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> documentUploadService.upload("prajwal@taxtracker.com", file));
        assertEquals("app.message.file.size.exceeded", ex.getMessage());
    }

    @Test
    void upload_throwsWhenInvalidType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "hello".getBytes());
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> documentUploadService.upload("prajwal@taxtracker.com", file));
        assertEquals("app.message.file.invalid.format", ex.getMessage());
    }

    @Test
    void upload_throwsWhenExtensionMismatch() {
        // content type ok but extension wrong
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.txt", "application/pdf", "data".getBytes());
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> documentUploadService.upload("prajwal@taxtracker.com", file));
        assertEquals("app.message.file.invalid.format", ex.getMessage());
    }
}
