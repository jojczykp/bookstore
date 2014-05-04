/*
 * Copyright (C) 2013-2014 Paweł Jojczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

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
import pl.jojczykp.bookstore.commands.ListBooksCommand;
import pl.jojczykp.bookstore.commands.UpdateBookCommand;
import pl.jojczykp.bookstore.repositories.BooksRepository;
import pl.jojczykp.bookstore.validators.BooksUpdateValidator;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static pl.jojczykp.bookstore.controllers.BooksConsts.LIST_BOOKS_COMMAND;
import static pl.jojczykp.bookstore.controllers.BooksConsts.UPDATE_BOOK_COMMAND;
import static pl.jojczykp.bookstore.controllers.BooksConsts.URL_ACTION_LIST;
import static pl.jojczykp.bookstore.controllers.BooksConsts.URL_ACTION_UPDATE;

@Controller
public class BooksControllerUpdate {

	@Autowired private BooksUpdateValidator booksUpdateValidator;
	@Autowired private BooksRepository booksRepository;
	@Autowired private BookAssembler bookAssembler;

	@RequestMapping(value = URL_ACTION_UPDATE, method = POST)
	public RedirectView update(
			@ModelAttribute(UPDATE_BOOK_COMMAND) UpdateBookCommand updateBookCommand,
			RedirectAttributes redirectAttributes,
			BindingResult bindingResult)
	{
		booksUpdateValidator.validate(updateBookCommand, bindingResult);

		ListBooksCommand listBooksCommand;
		if (bindingResult.hasErrors()) {
			listBooksCommand = processWhenCommandInvalid(updateBookCommand, bindingResult);
		} else {
			listBooksCommand = processWhenCommandValid(updateBookCommand);
		}

		return redirectToRead(listBooksCommand, redirectAttributes);

	}

	private ListBooksCommand processWhenCommandInvalid(
											UpdateBookCommand updateBookCommand, BindingResult bindingResult) {
		ListBooksCommand listBooksCommand = new ListBooksCommand();
		listBooksCommand.setPager(updateBookCommand.getPager());

		for(ObjectError error: bindingResult.getAllErrors()) {
			listBooksCommand.getMessages().addErrors(error.getDefaultMessage());
		}

		return listBooksCommand;
	}

	private ListBooksCommand processWhenCommandValid(UpdateBookCommand updateBookCommand) {
		ListBooksCommand listBooksCommand = new ListBooksCommand();
		listBooksCommand.setPager(updateBookCommand.getPager());

		try {
			booksRepository.update(bookAssembler.toDomain(updateBookCommand));
			listBooksCommand.getMessages().addInfos("Object updated.");
		} catch (StaleObjectStateException e) {
			listBooksCommand.getMessages().addWarns(
					"Object updated or deleted by another user. Please try again with actual data.");
		}

		return listBooksCommand;
	}

	private RedirectView redirectToRead(ListBooksCommand listBooksCommand, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(LIST_BOOKS_COMMAND, listBooksCommand);
		return new RedirectView(URL_ACTION_LIST);
	}

}
