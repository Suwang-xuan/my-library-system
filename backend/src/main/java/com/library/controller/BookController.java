package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.Book;
import com.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @GetMapping("/list")
    public CommonResult<Map<String, Object>> getBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bookNo,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publisher) {

        Map<String, Object> query = new java.util.HashMap<>();
        if (bookNo != null && !bookNo.isEmpty()) query.put("bookNo", bookNo);
        if (bookName != null && !bookName.isEmpty()) query.put("bookName", bookName);
        if (author != null && !author.isEmpty()) query.put("author", author);
        if (categoryId != null) query.put("categoryId", categoryId);
        if (publisher != null && !publisher.isEmpty()) query.put("publisher", publisher);

        Map<String, Object> result = bookService.getBookList(pageNum, pageSize, query);
        return CommonResult.success(result);
    }

    @GetMapping("/{bookId}")
    public CommonResult<Book> getBookById(@PathVariable Long bookId) {
        Book book = bookService.getBookById(bookId);
        return CommonResult.success(book);
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addBook(@RequestBody Book book) {
        boolean result = bookService.saveBook(book);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateBook(@RequestBody Book book) {
        boolean result = bookService.updateBook(book);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @DeleteMapping("/delete/{bookId}")
    public CommonResult<Boolean> deleteBook(@PathVariable Long bookId) {
        boolean result = bookService.deleteBook(bookId);
        return result ? CommonResult.success("删除成功", true) : CommonResult.error("删除失败");
    }

    @DeleteMapping("/batch-delete")
    public CommonResult<Boolean> batchDeleteBooks(@RequestBody List<Long> bookIds) {
        boolean result = bookService.batchDeleteBooks(bookIds);
        return result ? CommonResult.success("批量删除成功", true) : CommonResult.error("批量删除失败");
    }

    @GetMapping("/hot")
    public CommonResult<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") Integer limit) {
        List<Book> hotBooks = bookService.getHotBooks(limit);
        return CommonResult.success(hotBooks);
    }

    @GetMapping("/low-stock")
    public CommonResult<List<Book>> getLowStockBooks(@RequestParam(defaultValue = "5") Integer threshold) {
        List<Book> lowStockBooks = bookService.getLowStockBooks(threshold);
        return CommonResult.success(lowStockBooks);
    }

    @GetMapping("/stats")
    public CommonResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalBooks", bookService.getTotalBookCount());
        stats.put("totalStock", bookService.getTotalStock());
        return CommonResult.success(stats);
    }

    /**
     * 上传图书封面
     * @param file 封面图片文件
     * @param bookId 图书ID
     * @return 封面URL
     */
    @PostMapping("/upload-cover")
    public CommonResult<Map<String, String>> uploadCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookId") Long bookId) {
        
        logger.info("收到封面上传请求, bookId: {}", bookId);
        
        if (file.isEmpty()) {
            logger.warn("上传文件为空");
            return CommonResult.error("请选择要上传的文件");
        }

        try {
            // 生成唯一文件名，避免冲突
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String newFileName = "cover_" + bookId + "_" + System.currentTimeMillis() + ext;
            
            // 确保上传目录存在
            Path uploadDir = Paths.get("uploads/covers");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("创建上传目录成功: {}", uploadDir.toString());
            }
            
            // 保存文件
            Path targetPath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("文件保存成功: {}", targetPath.toString());
            
            // 构建封面URL
            String coverUrl = "/api/file/cover/" + newFileName;
            
            // 更新图书封面
            Book book = bookService.getBookById(bookId);
            book.setCoverUrl(coverUrl);
            bookService.updateBook(book);
            
            Map<String, String> result = new HashMap<>();
            result.put("coverUrl", coverUrl);
            result.put("fileName", newFileName);
            
            logger.info("图书 {} 封面上传成功: {}", bookId, coverUrl);
            return CommonResult.success("封面上传成功", result);
            
        } catch (IOException e) {
            logger.error("封面上传失败", e);
            return CommonResult.error("封面上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("封面上传发生异常", e);
            return CommonResult.error("封面上传失败: " + e.getMessage());
        }
    }

}
