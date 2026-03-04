package ar.utn.ba.ddsi.services.impl;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioService {

  private final MinioClient minioClient;
  private final String bucketName;
  private final String publicBaseUrl;

  public MinioService(
      @Value("${minio.endpoint}") String endpoint,
      @Value("${minio.access-key}") String accessKey,
      @Value("${minio.secret-key}") String secretKey,
      @Value("${minio.bucket}") String bucketName,
      @Value("${minio.public-base-url}") String publicBaseUrl
  ) {
    this.minioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();

    this.bucketName = bucketName;
    this.publicBaseUrl = publicBaseUrl;
  }

  /**
   * Se ejecuta automáticamente cuando se inicializa el servicio.
   * Si el bucket no existe, lo crea.
   */
  @PostConstruct
  public void ensureBucketExists() {
    try {
      boolean exists = minioClient.bucketExists(
          BucketExistsArgs.builder()
              .bucket(bucketName)
              .build()
      );

      if (!exists) {
        minioClient.makeBucket(
            MakeBucketArgs.builder()
                .bucket(bucketName)
                .build()
        );

        System.out.println("✔ Bucket creado automáticamente: " + bucketName);
      } else {
        System.out.println("✔ Bucket ya existe: " + bucketName);
      }

    } catch (Exception e) {
      throw new RuntimeException("❌ Error verificando/creando bucket en MinIO", e);
    }
  }

  /**
   * Sube un archivo al bucket de MinIO.
   */
  public String upload(MultipartFile file) {
    try {
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build()
      );

      // URL pública para que el front pueda acceder
      return publicBaseUrl + "/" + bucketName + "/" + fileName;

    } catch (Exception e) {
      throw new RuntimeException("❌ Error subiendo archivo a MinIO", e);
    }
  }
}