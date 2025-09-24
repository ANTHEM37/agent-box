package com.agent.platform.knowledge.service;

import com.agent.platform.common.exception.BusinessException;
import com.agent.platform.knowledge.dto.DocumentUploadResponse;
import com.agent.platform.knowledge.entity.Document;
import com.agent.platform.knowledge.entity.DocumentChunk;
import com.agent.platform.knowledge.entity.KnowledgeBase;
import com.agent.platform.knowledge.repository.DocumentChunkRepository;
import com.agent.platform.knowledge.repository.DocumentRepository;
import com.agent.platform.user.entity.User;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentProcessorService documentProcessorService;
    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseService knowledgeBaseService;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传文档
     */
    @Transactional
    public DocumentUploadResponse uploadDocument(User user, Long knowledgeBaseId, MultipartFile file) {
        // 验证知识库权限
        KnowledgeBase knowledgeBase = knowledgeBaseService.findKnowledgeBaseByUserAndId(user, knowledgeBaseId);

        // 验证文件
        validateFile(file);

        // 检查文件名是否已存在
        String originalFilename = file.getOriginalFilename();
        if (documentRepository.findByKnowledgeBaseAndFilename(knowledgeBase, originalFilename).isPresent()) {
            throw new BusinessException("文件名已存在: " + originalFilename);
        }

        try {
            // 保存文件
            String savedFilename = saveFile(file);

            // 创建文档记录
            Document document = new Document();
            document.setKnowledgeBase(knowledgeBase);
            document.setFilename(savedFilename);
            document.setOriginalFilename(originalFilename);
            document.setFilePath(uploadPath + "/" + savedFilename);
            document.setFileSize(file.getSize());
            document.setFileType(getFileExtension(originalFilename));
            document.setMimeType(file.getContentType());
            document.setStatus(Document.DocumentStatus.UPLOADED);

            document = documentRepository.save(document);

            log.info("用户 {} 上传文档: {} 到知识库: {}", 
                user.getUsername(), originalFilename, knowledgeBase.getName());

            return DocumentUploadResponse.from(document);

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 处理文档（解析、分块、向量化）
     */
    @Transactional
    public void processDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BusinessException("文档不存在"));

        if (document.getStatus() != Document.DocumentStatus.UPLOADED) {
            log.warn("文档状态不正确，无法处理: {}", document.getStatus());
            return;
        }

        try {
            // 更新状态为处理中
            document.setStatus(Document.DocumentStatus.PROCESSING);
            documentRepository.save(document);

            // 读取文件
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BusinessException("文件不存在: " + document.getFilePath());
            }

            // 解析文档
            dev.langchain4j.data.document.Document langchainDoc = 
                documentProcessorService.parseDocument(createMultipartFile(filePath));

            // 分割文档
            KnowledgeBase kb = document.getKnowledgeBase();
            List<TextSegment> segments = documentProcessorService.splitDocument(
                langchainDoc, kb.getChunkSize(), kb.getChunkOverlap());

            // 向量化并存储
            List<String> vectorIds = vectorStoreService.addTextSegments(segments);

            // 保存文档分块
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                String vectorId = vectorIds.get(i);

                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocument(document);
                chunk.setChunkIndex(i);
                chunk.setContent(segment.text());
                chunk.setTokenCount(documentProcessorService.estimateTokenCount(segment.text()));
                chunk.setVectorId(vectorId);
                
                documentChunkRepository.save(chunk);
            }

            // 更新文档统计信息
            int totalTokens = documentChunkRepository.sumTokenCountByDocument(document);
            document.setChunkCount(segments.size());
            document.setTokenCount(totalTokens);
            document.setStatus(Document.DocumentStatus.PROCESSED);
            documentRepository.save(document);

            // 更新知识库统计信息
            updateKnowledgeBaseStats(kb);

            log.info("文档处理完成: {}, 分块数: {}, Token数: {}", 
                document.getOriginalFilename(), segments.size(), totalTokens);

        } catch (Exception e) {
            log.error("文档处理失败: {}", document.getOriginalFilename(), e);
            
            // 更新错误状态
            document.setStatus(Document.DocumentStatus.ERROR);
            document.setProcessingError(e.getMessage());
            documentRepository.save(document);
            
            throw new BusinessException("文档处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库的文档列表
     */
    public Page<DocumentUploadResponse> getDocuments(User user, Long knowledgeBaseId, Pageable pageable) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.findKnowledgeBaseByUserAndId(user, knowledgeBaseId);
        
        Page<Document> documents = documentRepository.findByKnowledgeBaseOrderByCreatedAtDesc(
            knowledgeBase, pageable);
        
        return documents.map(DocumentUploadResponse::from);
    }

    /**
     * 删除文档
     */
    @Transactional
    public void deleteDocument(User user, Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BusinessException("文档不存在"));

        // 验证权限
        if (!document.getKnowledgeBase().getUser().getId().equals(user.getId())) {
            throw new BusinessException("无权限删除此文档");
        }

        try {
            // 删除向量数据
            List<DocumentChunk> chunks = documentChunkRepository.findByDocumentOrderByChunkIndex(document);
            List<String> vectorIds = chunks.stream()
                .map(DocumentChunk::getVectorId)
                .toList();
            
            if (!vectorIds.isEmpty()) {
                vectorStoreService.removeByVectorIds(vectorIds);
            }

            // 删除文档分块
            documentChunkRepository.deleteByDocument(document);

            // 删除文件
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // 删除文档记录
            documentRepository.delete(document);

            // 更新知识库统计信息
            updateKnowledgeBaseStats(document.getKnowledgeBase());

            log.info("用户 {} 删除文档: {}", user.getUsername(), document.getOriginalFilename());

        } catch (Exception e) {
            log.error("删除文档失败: {}", document.getOriginalFilename(), e);
            throw new BusinessException("删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        // 检查文件大小（限制为10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // 检查文件类型
        if (!documentProcessorService.isSupportedFileType(file.getContentType(), originalFilename)) {
            throw new BusinessException("不支持的文件类型，支持的格式：PDF、Word、TXT、Markdown");
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String savedFilename = timestamp + "_" + uniqueId + "." + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(savedFilename);
        Files.copy(file.getInputStream(), filePath);

        return savedFilename;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 更新知识库统计信息
     */
    private void updateKnowledgeBaseStats(KnowledgeBase knowledgeBase) {
        long documentCount = documentRepository.countByKnowledgeBaseAndStatus(
            knowledgeBase, Document.DocumentStatus.PROCESSED);
        Long totalTokens = documentRepository.sumTokenCountByKnowledgeBase(knowledgeBase);
        
        knowledgeBaseService.updateKnowledgeBaseStats(
            knowledgeBase.getId(), (int) documentCount, totalTokens != null ? totalTokens : 0L);
    }

    /**
     * 创建MultipartFile（用于处理已保存的文件）
     */
    private MultipartFile createMultipartFile(Path filePath) throws IOException {
        // 这里需要实现一个简单的MultipartFile包装器
        // 为了简化，这里返回null，实际使用时需要实现
        return null;
    }
}