package com.topfilmesbrasil.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.topfilmesbrasil.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);
    
    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;
    
    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;
    
    @Value("${cloud.aws.s3.bucket:}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static:us-east-1}")
    private String region;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    private AmazonS3 s3Client;
    
    private AmazonS3 getS3Client() {
        if (s3Client == null && isS3Enabled()) {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(Regions.fromName(region))
                    .build();
        }
        return s3Client;
    }
    
    @Override
    public boolean isS3Enabled() {
        return "production".equalsIgnoreCase(activeProfile) 
                && accessKey != null && !accessKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty()
                && bucketName != null && !bucketName.isEmpty();
    }
    
    @Override
    public String uploadFile(MultipartFile multipartFile) {
        if (!isS3Enabled()) {
            throw new RuntimeException("S3 não está configurado. Use em ambiente de produção.");
        }
        
        String fileName = generateFileName(multipartFile);
        
        try {
            File file = convertMultiPartFileToFile(multipartFile);
            uploadFileToS3(fileName, file);
            file.delete(); // Remove arquivo temporário
            return getFileUrl(fileName);
        } catch (Exception e) {
            logger.error("Erro ao fazer upload para S3: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (!isS3Enabled()) {
            logger.warn("S3 não está configurado. Ignorando deleção.");
            return;
        }
        
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            getS3Client().deleteObject(new DeleteObjectRequest(bucketName, fileName));
            logger.info("Arquivo deletado do S3: {}", fileName);
        } catch (Exception e) {
            logger.error("Erro ao deletar arquivo do S3: {}", e.getMessage(), e);
        }
    }
    
    private String generateFileName(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "uploads/" + UUID.randomUUID().toString() + extension;
    }
    
    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    
    private void uploadFileToS3(String fileName, File file) {
        getS3Client().putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        logger.info("Arquivo enviado para S3: {}", fileName);
    }
    
    private String getFileUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }
    
    private String extractFileNameFromUrl(String fileUrl) {
        // Extrai o nome do arquivo da URL
        // Ex: https://bucket.s3.region.amazonaws.com/uploads/file.jpg -> uploads/file.jpg
        String[] parts = fileUrl.split(".com/");
        if (parts.length > 1) {
            return parts[1];
        }
        return fileUrl;
    }
}

