package com.socialnetwork.parrot.core.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadServiceInterface {
    String upload(MultipartFile file, String fileName);
}
