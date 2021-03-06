package pl.jojczykp.bookstore.repositories;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.jojczykp.bookstore.entities.Book;
import pl.jojczykp.bookstore.utils.PageSorterColumn;
import pl.jojczykp.bookstore.utils.PageSorterDirection;

import java.util.List;

import static com.google.common.primitives.Ints.checkedCast;
import static java.util.Collections.emptyList;
import static org.hibernate.criterion.Projections.rowCount;
import static pl.jojczykp.bookstore.utils.PageSorter.orderBy;
import static pl.jojczykp.bookstore.utils.SuppressUnchecked.suppressUnchecked;

@Repository
@Transactional
public class BooksRepository {

	@Autowired private SessionFactory sessionFactory;

	public int create(Book book) {
		return (int) getCurrentSession().save(book);
	}

	public Book find(int id) {
		return (Book) getCurrentSession().get(Book.class, id);
	}

	public List<Book> read(int offset, int size, PageSorterColumn sortColumn, PageSorterDirection sortDirection) {
		if (size <= 0) {
			return emptyList();
		} else {
			return readWithPositiveSize(offset, size, sortColumn, sortDirection);
		}
	}

	private List<Book> readWithPositiveSize(int offset, int size,
											PageSorterColumn sortColumn, PageSorterDirection sortDirection) {
		Criteria criteria = getCurrentSession().createCriteria(Book.class);
		criteria.setFirstResult(offset);
		criteria.setMaxResults(size);
		criteria.addOrder(orderBy(sortColumn, sortDirection));

		return suppressUnchecked(criteria.list());
	}

	public void update(Book updated) {
		Book existing = (Book) getCurrentSession().load(Book.class, updated.getId());
		updated.setBookFile(existing.getBookFile());
		getCurrentSession().merge(updated);
	}

	public void delete(int id) {
		Book book = (Book) getCurrentSession().load(Book.class, id);
		getCurrentSession().delete(book);
	}

	public int totalCount() {
		Long result = (Long) getCurrentSession().createCriteria(Book.class).setProjection(rowCount()).uniqueResult();
		return checkedCast(result);
	}

	private Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

}
