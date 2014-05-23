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

package pl.jojczykp.bookstore.controllers.books;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import pl.jojczykp.bookstore.commands.books.ChangePagerCommand;
import pl.jojczykp.bookstore.commands.books.DisplayBooksCommand;
import pl.jojczykp.bookstore.validators.ChangePagerValidator;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static pl.jojczykp.bookstore.consts.BooksConsts.DISPLAY_BOOKS_COMMAND;
import static pl.jojczykp.bookstore.consts.BooksConsts.CHANGE_PAGER_COMMAND;
import static pl.jojczykp.bookstore.consts.BooksConsts.URL_ACTION_GO_TO_PAGE;
import static pl.jojczykp.bookstore.consts.BooksConsts.URL_ACTION_LIST;
import static pl.jojczykp.bookstore.consts.BooksConsts.URL_ACTION_SET_PAGE_SIZE;
import static pl.jojczykp.bookstore.consts.BooksConsts.URL_ACTION_SORT;

@Controller
public class ChangeBooksPagerController {

	@Autowired private ChangePagerValidator changePagerValidator;

	@Value("${view.books.defaultPageSize}") private int defaultPageSize;

	@RequestMapping(value = URL_ACTION_SORT, method = POST)
	public RedirectView sort(
			@ModelAttribute(CHANGE_PAGER_COMMAND) ChangePagerCommand changePagerCommand,
			RedirectAttributes redirectAttributes)
	{
		DisplayBooksCommand displayBooksCommand = new DisplayBooksCommand();
		displayBooksCommand.setPager(changePagerCommand.getPager());

		return redirect(displayBooksCommand, redirectAttributes);
	}

	@RequestMapping(value = URL_ACTION_SET_PAGE_SIZE, method = POST)
	public RedirectView setPageSize(
			@ModelAttribute(CHANGE_PAGER_COMMAND) ChangePagerCommand changePagerCommand,
			RedirectAttributes redirectAttributes,
			BindingResult bindingResult)
	{
		changePagerValidator.validate(changePagerCommand, bindingResult);

		DisplayBooksCommand displayBooksCommand;
		if (bindingResult.hasErrors()) {
			displayBooksCommand = processWhenSetPageSizeCommandInvalid(changePagerCommand, bindingResult);
		} else {
			displayBooksCommand = processWhenSetPageSizeCommandValid(changePagerCommand);
		}

		return redirect(displayBooksCommand, redirectAttributes);
	}

	private DisplayBooksCommand processWhenSetPageSizeCommandInvalid(
							ChangePagerCommand changePagerCommand, BindingResult bindingResult) {
		DisplayBooksCommand displayBooksCommand = new DisplayBooksCommand();
		displayBooksCommand.setPager(changePagerCommand.getPager());

		displayBooksCommand.getPager().setPageSize(defaultPageSize);
		for(ObjectError error: bindingResult.getAllErrors()) {
			displayBooksCommand.getMessages().addErrors(error.getDefaultMessage());
		}

		return displayBooksCommand;
	}

	private DisplayBooksCommand processWhenSetPageSizeCommandValid(ChangePagerCommand changePagerCommand) {
		DisplayBooksCommand displayBooksCommand = new DisplayBooksCommand();
		displayBooksCommand.setPager(changePagerCommand.getPager());

		displayBooksCommand.getMessages().addInfos("Page size changed.");

		return displayBooksCommand;
	}

	@RequestMapping(value = URL_ACTION_GO_TO_PAGE, method = POST)
	public RedirectView goToPage(
			@ModelAttribute(CHANGE_PAGER_COMMAND) ChangePagerCommand changePagerCommand,
			RedirectAttributes redirectAttributes)
	{
		DisplayBooksCommand displayBooksCommand = new DisplayBooksCommand();
		displayBooksCommand.setPager(changePagerCommand.getPager());

		return redirect(displayBooksCommand, redirectAttributes);
	}

	private RedirectView redirect(DisplayBooksCommand displayBooksCommand, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(DISPLAY_BOOKS_COMMAND, displayBooksCommand);
		return new RedirectView(URL_ACTION_LIST);
	}

}