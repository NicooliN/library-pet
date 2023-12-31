package ru.pet.library.librarypet.library.mapper;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Component;
import ru.pet.library.librarypet.library.dto.BookDTO;
import ru.pet.library.librarypet.library.model.Book;
import ru.pet.library.librarypet.library.model.GenericModel;
import ru.pet.library.librarypet.library.repository.AuthorRepository;
import ru.pet.library.librarypet.library.utils.DateFormatter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMapper
        extends GenericMapper<Book, BookDTO> {

    private final AuthorRepository authorRepository;

    protected BookMapper(ModelMapper modelMapper,
                         AuthorRepository authorRepository) {

        super(modelMapper, Book.class, BookDTO.class);
        this.authorRepository = authorRepository;
    }
    @PostConstruct
    public void setupMapper() {
        modelMapper.createTypeMap(Book.class, BookDTO.class)
                .addMappings(m -> m.skip(BookDTO::setAuthorsIds)).setPostConverter(toDTOConverter());
        modelMapper.createTypeMap(BookDTO.class, Book.class)
                .addMappings(m -> m.skip(Book::setAuthors)).setPostConverter(toEntityConverter())
                .addMappings(m -> m.skip(Book::setPublishDate)).setPostConverter(toEntityConverter());
    }

    @Override
    protected void mapSpecificFields(BookDTO source, Book destination) {
            if(!Objects.isNull(source.getAuthorsIds())) {
                destination.setAuthors(new HashSet<>(authorRepository.findAllById(source.getAuthorsIds())));
            }
            else {
                destination.setAuthors(Collections.emptySet());
            }
        destination.setPublishDate(DateFormatter.formatStringToDate(source.getPublishDate()));
    }

    @Override
    protected void mapSpecificFields(Book source, BookDTO destination) {
        destination.setAuthorsIds(getIds(source));
    }
    @Override
    protected Set<Long> getIds(Book entity) {
        return Objects.isNull(entity) || Objects.isNull(entity.getAuthors())
                ? Collections.emptySet()
                : entity.getAuthors().stream()
                .map(GenericModel::getId)
                .collect(Collectors.toSet());
    }
}
