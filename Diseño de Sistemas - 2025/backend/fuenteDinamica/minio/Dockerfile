FROM minio/minio

ENV MINIO_ROOT_USER=admin
ENV MINIO_ROOT_PASSWORD=admin123

EXPOSE 9000

ENTRYPOINT ["minio"]
CMD ["server", "/data"]
