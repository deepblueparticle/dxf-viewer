/**
 * 
 */
package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author fujiwara
 * @param <E>
 * 
 */
public class ListSet<E> implements List<E> {
	List<E> list;
	Map<E, Integer> map;

	public ListSet() {
		this.list = new ArrayList<E>();
		this.map = new HashMap<E, Integer>();
	}

	public boolean add(E entry) {
		Integer index = this.map.get(entry);
		if (index == null) {
			index = this.list.size();
			this.map.put(entry, index);
			this.list.add(entry);
		}
		return true;
	}

	/*
	 * @see java.util.List#add(int, java.lang.Object)
	 */

	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */

	public boolean addAll(Collection<? extends E> c) {
		for (E entry : c) {
			this.add(entry);
		}
		return true;
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */

	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Collection#clear()
	 */

	public void clear() {
		this.list.clear();
		this.map.clear();
	}

	/**
	 * @see java.util.Collection#contains(java.lang.Object)
	 */

	public boolean contains(Object o) {
		return this.map.containsKey(o);
	}

	/**
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */

	public boolean containsAll(Collection<?> c) {
		boolean flag = true;
		for (Object obj : c) {
			flag &= this.contains(obj);
		}
		return flag;
	}

	/**
	 * @see java.util.List#get(int)
	 */

	public E get(int index) {
		return this.list.get(index);
	}

	public E get(Object obj) {
		Integer index = this.map.get(obj);
		E entry = null;
		if (index != null) {
			entry = this.list.get(index);
		}
		return entry;
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */

	public int indexOf(Object o) {
		Integer index = this.map.get(o);
		if (index == null) {
			return -1;
		} else {
			return index;
		}
	}

	/**
	 * @see java.util.Collection#isEmpty()
	 */

	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	/**
	 * @see java.util.Collection#iterator()
	 */

	public Iterator<E> iterator() {
		return this.list.iterator();
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */

	public int lastIndexOf(Object o) {
		return this.indexOf(o);
	}

	/**
	 * @see java.util.List#listIterator()
	 */

	public ListIterator<E> listIterator() {
		return this.list.listIterator();
	}

	/**
	 * @see java.util.List#listIterator(int)
	 */

	public ListIterator<E> listIterator(int index) {
		return this.list.listIterator();
	}

	/**
	 * @see java.util.List#remove(int)
	 */

	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Collection#remove(java.lang.Object)
	 */

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */

	public boolean removeAll(Collection<?> c) {
		boolean flag = true;
		for (Object obj : c) {
			flag &= this.remove(obj);
		}
		return flag;
	}

	/**
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */

	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Collection#size()
	 */

	public int size() {
		return this.list.size();
	}

	/**
	 * @see java.util.List#subList(int, int)
	 */

	public List<E> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}

	/**
	 * @see java.util.Collection#toArray()
	 */

	public Object[] toArray() {
		return this.list.toArray();
	}

	/**
	 * @see java.util.Collection#toArray(T[])
	 */

	public <T> T[] toArray(T[] a) {
		return this.list.toArray(a);
	}

}
