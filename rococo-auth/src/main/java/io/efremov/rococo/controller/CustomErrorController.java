package io.efremov.rococo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  private final String frontUri;

  public CustomErrorController(@Value("${rococo-front.base-uri}") String frontUri) {
    this.frontUri = frontUri;
  }

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object codeAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int statusCode = codeAttribute instanceof Integer integer
        ? integer
        : HttpStatus.INTERNAL_SERVER_ERROR.value();

    HttpStatus httpStatus = HttpStatus.resolve(statusCode);
    if (httpStatus == null) {
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    Object messageAttr = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    String message = messageAttr != null
        ? messageAttr.toString()
        : null;

    Object pathAttr = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    String path = pathAttr != null
        ? pathAttr.toString()
        : request.getRequestURI();

    model.addAttribute("status", statusCode);
    model.addAttribute("error", httpStatus.getReasonPhrase());
    model.addAttribute("message", message);
    model.addAttribute("path", path);
    model.addAttribute("frontUri", frontUri);

    return "error";
  }
}
