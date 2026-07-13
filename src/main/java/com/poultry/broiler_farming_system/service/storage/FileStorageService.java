package com.poultry.broiler_farming_system.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // Validates the file is an image, writes it under app.upload.dir/subDirectory
    // with a generated unique filename, and returns the public "/uploads/..." URL.
    String store(MultipartFile file, String subDirectory, String filenamePrefix);

    // Validates the file is a PDF, writes it under app.upload.dir/subDirectory
    // with a generated unique filename, and returns the public "/uploads/..." URL.
    String storeDocument(MultipartFile file, String subDirectory, String filenamePrefix);

    // Best-effort delete of a previously stored file, given the public URL
    // store()/storeDocument() returned. Missing/foreign files are silently ignored.
    void delete(String publicUrl);
}
