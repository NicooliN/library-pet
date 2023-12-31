package ru.pet.library.librarypet.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.pet.library.librarypet.library.model.Book;

import java.util.List;

@Repository
public interface BookRepository
        extends GenericRepository<Book>{

    @Query(nativeQuery = true,
            value = """
                 select distinct b.*
                 from books b
                 left join books_authors ba on b.id = ba.book_id
                 join authors a on a.id = ba.author_id
                 where b.title ilike '%' || coalesce(:title, '%') || '%'
                 and cast(b.genre as char) like coalesce(:genre,'%')
                 and a.fio ilike '%' || :fio || '%'
                  and b.is_deleted = false
                      """)
    Page<Book> searchBooks(@Param(value = "genre") String genre,
                           @Param(value = "title") String title,
                           @Param(value = "fio") String fio,
                           Pageable pageable);

    Book findBookByIdAndBookRentInfosReturnedFalseAndIsDeletedFalse(final Long id);

    @Query("""
          select case when count(b) > 0 then false else true end
          from Book b join BookRentInfo bri on b.id = bri.book.id
          where b.id = :id and bri.returned = false
          """)
    boolean checkBookForDeletion(final Long id);

    Page<Book> findAllByIsDeletedFalse(Pageable pageable);
}
