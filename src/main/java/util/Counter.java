/**
 * 
 */
package util;

/**
 * 数を数えるためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.03
 */
public class Counter {
	private int count;

	public Counter() {
		this.count = 0;
	}

	public Counter(int count) {
		this.count = count;
	}

	public void down() {
		this.count--;
	}

	public void down(int count) {
		this.count -= count;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.hashCode() == this.hashCode();
	}

	public int getCount() {
		return this.count;
	}

	@Override
	public int hashCode() {
		return this.count;
	}

	@Override
	public String toString() {
		return Integer.toString(this.count);
	}

	public void up() {
		this.count++;
	}

	public void up(int count) {
		this.count += count;
	}
}
