package com.agent.platform.knowledge.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档处理服务
 */
@Slf4j
@Service
public class DocumentProcessorService {

    /**
     * 解析文档
     */
    public Document parseDocument(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        DocumentParser parser = getDocumentParser(contentType, filename);
        
        try (InputStream inputStream = file.getInputStream()) {
            return parser.parse(inputStream);
        }
    }

    /**
     * 分割文档
     */
    public List<TextSegment> splitDocument(Document document, int chunkSize, int chunkOverlap) {
        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
        return splitter.split(document);
    }

    /**
     * 根据文件类型获取解析器
     */
    private DocumentParser getDocumentParser(String contentType, String filename) {
        if (contentType == null && filename != null) {
            contentType = getContentTypeFromFilename(filename);
        }

        return switch (contentType) {
            case "application/pdf" -> new ApachePdfBoxDocumentParser();
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword" -> new ApachePoiDocumentParser();
            case "text/plain", "text/markdown" -> new TextDocumentParser();
            default -> {
                log.warn("未知文件类型: {}, 使用默认文本解析器", contentType);
                yield new TextDocumentParser();
            }
        };
    }

    /**
     * 根据文件名获取内容类型
     */
    private String getContentTypeFromFilename(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc" -> "application/msword";
            case "txt" -> "text/plain";
            case "md", "markdown" -> "text/markdown";
            default -> "text/plain";
        };
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
     * 检查文件类型是否支持
     */
    public boolean isSupportedFileType(String contentType, String filename) {
        if (contentType == null && filename != null) {
            contentType = getContentTypeFromFilename(filename);
        }

        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/msword") ||
            contentType.equals("text/plain") ||
            contentType.equals("text/markdown")
        );
    }

    /**
     * 估算文本token数量
     */
    public int estimateTokenCount(String text) {
        // 简单估算：英文约4个字符=1个token，中文约1.5个字符=1个token
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseCount = 0;
        int englishCount = 0;
        
        for (char c : text.toCharArray()) {
            if (c >= 0x4e00 && c <= 0x9fff) {
                chineseCount++;
            } else {
                englishCount++;
            }
        }
        
        return (int) (chineseCount / 1.5 + englishCount / 4.0);
    }
}