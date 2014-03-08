package pl.jojczykp.bookstore.controllers;

import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import pl.jojczykp.bookstore.assemblers.BookAssembler;
import pl.jojczykp.bookstore.commands.BooksCommand;
import pl.jojczykp.bookstore.repositories.BooksRepository;
import pl.jojczykp.bookstore.validators.BooksUpdateValidator;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static pl.jojczykp.bookstore.controllers.BooksConsts.BOOKS_COMMAND;
import static pl.jojczykp.bookstore.controllers.BooksConsts.URL_ACTION_READ;
import static pl.jojczykp.bookstore.controllers.BooksConsts.URL_ACTION_UPDATE;

@Controller
public class BooksControllerUpdate {

	@Autowired private BooksUpdateValidator booksUpdateValidator;
	@Autowired private BooksRepository booksRepository;
	@Autowired private BookAssembler bookAssembler;

	@RequestMapping(value = URL_ACTION_UPDATE, method = POST)
	public RedirectView update(
			@ModelAttribute(BOOKS_COMMAND) BooksCommand booksCommand,
			RedirectAttributes redirectAttributes,
			BindingResult bindingResult)
	{
		booksUpdateValidator.validate(booksCommand, bindingResult);
		if (bindingResult.hasErrors()) {
			processWhenCommandInvalid(booksCommand, bindingResult);
		} else {
			processWhenCommandValid(booksCommand);
		}

		return redirectToRead(booksCommand, redirectAttributes);

	}

	private void processWhenCommandInvalid(BooksCommand booksCommand, BindingResult bindingResult) {
		for(ObjectError error: bindingResult.getAllErrors()) {
			booksCommand.getMessages().addErrors(error.getDefaultMessage());
		}
	}

	private void processWhenCommandValid(BooksCommand booksCommand) {
		try {
			booksRepository.update(bookAssembler.toDomain(booksCommand.getUpdatedBook()));
			booksCommand.getMessages().addInfos("Object updated.");
		} catch (StaleObjectStateException e) {
			booksCommand.getMessages().addWarns(
					"Object updated or deleted by another user. Please try again with actual data.");
		}
	}

	private RedirectView redirectToRead(BooksCommand booksCommand, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(BOOKS_COMMAND, booksCommand);
		return new RedirectView(URL_ACTION_READ);
	}

}