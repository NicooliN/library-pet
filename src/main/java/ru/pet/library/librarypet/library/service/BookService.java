package ru.pet.library.librarypet.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import ru.pet.library.librarypet.library.constants.Errors;
import ru.pet.library.librarypet.library.dto.BookDTO;
import ru.pet.library.librarypet.library.dto.BookSearchDTO;
import ru.pet.library.librarypet.library.dto.BookWithAuthorsDTO;
import ru.pet.library.librarypet.library.exception.MyDeleteException;
import ru.pet.library.librarypet.library.mapper.BookMapper;
import ru.pet.library.librarypet.library.mapper.BookWithAuthorsMapper;
import ru.pet.library.librarypet.library.model.Book;
import ru.pet.library.librarypet.library.repository.BookRepository;
import ru.pet.library.librarypet.library.utils.FileHelper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookService
        extends GenericService<Book, BookDTO> {

    private final BookWithAuthorsMapper bookWithAuthorsMapper;
    private final BookRepository bookRepository;

    protected BookService (BookRepository bookRepository,
                        BookMapper bookMapper,
                           BookWithAuthorsMapper bookWithAuthorsMapper) {
      super(bookRepository, bookMapper);
      this.bookWithAuthorsMapper = bookWithAuthorsMapper;
      this.bookRepository = bookRepository;
    }


    public Page<BookWithAuthorsDTO> getAllBooksWithAuthors(Pageable pageable) {
        Page<Book> booksPaginated = bookRepository.findAll(pageable);
        List<BookWithAuthorsDTO> result = bookWithAuthorsMapper.toDtos(booksPaginated.getContent());
        return new PageImpl<>(result, pageable, booksPaginated.getTotalElements());
    }

    public Page<BookWithAuthorsDTO> findBooks(BookSearchDTO bookSearchDTO,
                                              Pageable pageable) {
        String genre = bookSearchDTO.getGenre() != null ? String.valueOf(bookSearchDTO.getGenre().ordinal()) : null;
        Page<Book> booksPaginated = bookRepository.searchBooks(genre,
                bookSearchDTO.getBookTitle(),
                bookSearchDTO.getAuthorFio(),
                pageable
        );

        List<BookWithAuthorsDTO> result = bookWithAuthorsMapper.toDtos(booksPaginated.getContent());
        return new PageImpl<>(result, pageable, booksPaginated.getTotalElements());
    }
    public BookWithAuthorsDTO getBookWithAuthors(Long id) {
        return bookWithAuthorsMapper.toDto(genericMapper.toEntity(super.getOne(id)));
    }

    public BookDTO create(final BookDTO object,
                          MultipartFile file) {
        String fileName = FileHelper.createFile(file);
        object.setOnlineCopyPath(fileName);
        object.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        object.setCreatedWhen(LocalDateTime.now());
        return genericMapper.toDto(genericRepository.save(genericMapper.toEntity(object)));
    }

    public BookDTO update(final BookDTO object,
                          MultipartFile file) {
        String fileName = FileHelper.createFile(file);
        object.setOnlineCopyPath(fileName);
        return genericMapper.toDto(genericRepository.save(genericMapper.toEntity(object)));
    }

    @Override
    public void delete(Long id) throws MyDeleteException {
        Book book = genericRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Книги с заданным ID=" + id + " не существует"));
//        boolean bookCanBeDeleted = repository.findBookByIdAndBookRentInfosReturnedFalseAndIsDeletedFalse(id) == null;
        boolean bookCanBeDeleted = bookRepository.checkBookForDeletion(id);
        if (bookCanBeDeleted) {
            if (book.getOnlineCopyPath() != null && !book.getOnlineCopyPath().isEmpty()) {
                FileHelper.deleteFile(book.getOnlineCopyPath());
            }
            markAsDeleted(book);
            genericRepository.save(book);
        }
        else {
            throw new MyDeleteException(Errors.Books.BOOK_DELETE_ERROR);
        }
    }

    public Page<BookWithAuthorsDTO> getAllNotDeletedBooksWithAuthors(Pageable pageable) {
        Page<Book> booksPaginated = bookRepository.findAllByIsDeletedFalse(pageable);
        List<BookWithAuthorsDTO> result = bookWithAuthorsMapper.toDtos(booksPaginated.getContent());
        return new PageImpl<>(result, pageable, booksPaginated.getTotalElements());
    }

    public void restore(Long objectId) {
        Book book = genericRepository.findById(objectId).orElseThrow(
                () -> new NotFoundException("Книги с заданным ID=" + objectId + " не существует"));
        unMarkAsDeleted(book);
        genericRepository.save(book);
    }

}
