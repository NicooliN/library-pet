package ru.pet.library.librarypet.library.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE;

@Controller
@Slf4j
public class MyErrorController
        implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest httpServletRequest,
                              Model model) {
        log.error("Случилась беда! Ошибка {}",
                httpServletRequest.getAttribute(ERROR_STATUS_CODE));
        model.addAttribute("exception",
                "Ошибка " + httpServletRequest.getAttribute(ERROR_STATUS_CODE) + " в маппинге " +
                        httpServletRequest.getAttribute(ERROR_REQUEST_URI));
        return "error";
    }
}
