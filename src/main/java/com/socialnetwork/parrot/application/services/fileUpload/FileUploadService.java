package com.socialnetwork.parrot.application.services.fileUpload;

import com.socialnetwork.parrot.core.services.interfaces.FileUploadServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService implements FileUploadServiceInterface {
    @Autowired
    private AwsService _awsService;

    public String upload(MultipartFile file, String fileName) {
        String fileUri = "";

        try {
            fileUri = _awsService.upload(file, fileName);
        } catch (Exception e) {
            return null;
        }

        return fileUri;
    }
}
