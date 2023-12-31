package ru.pet.library.librarypet.library.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.pet.library.librarypet.library.dto.BookRentInfoDTO;
import ru.pet.library.librarypet.library.service.BookRentInfoService;
import ru.pet.library.librarypet.library.service.BookService;
import ru.pet.library.librarypet.library.service.userdetails.CustomUserDetails;

@Controller
@Slf4j

@RequestMapping("/rent")
public class BookRentInfoController {
    private final BookRentInfoService bookRentInfoService;
    private final BookService bookService;

    public BookRentInfoController(BookRentInfoService bookRentInfoService, BookService bookService) {
        this.bookRentInfoService = bookRentInfoService;
        this.bookService = bookService;
    }
    
    @GetMapping("/book/{bookId}")
    public String rentBook(@PathVariable Long bookId,
                           Model model) {
        model.addAttribute("book", bookService.getOne(bookId));
        return "userBooks/rentBook";
    }
    
    @PostMapping("/book")
    public String rentBook(@ModelAttribute("rentBookForm") BookRentInfoDTO rentBookDTO) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        rentBookDTO.setUserId(Long.valueOf(customUserDetails.getUserId()));
        bookRentInfoService.rentBook(rentBookDTO);
        return "redirect:/rent/user-books/" + customUserDetails.getUserId();
    }
    
    @GetMapping("/user-books/{id}")
    public String userBooks(@RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "size", defaultValue = "5") int pageSize,
                            @PathVariable Long id,
                            Model model) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        Page<BookRentInfoDTO> rentInfoDTOPage = bookRentInfoService.listAll(pageRequest);
        model.addAttribute("rentBooks", rentInfoDTOPage);
        return "userBooks/viewAllUserBooks";
    }

    @GetMapping("/return-book/{id}")
    public String returnBook(@PathVariable Long id) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookRentInfoService.returnBook(id);
        return "redirect:/rent/user-books/" + customUserDetails.getUserId();
    }
}
