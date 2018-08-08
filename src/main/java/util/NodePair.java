package util;

import dxf.section.entities.DxfDimension;

public class NodePair {
	private RoundNumber n1;
	private RoundNumber n2;

	public NodePair(double v1, double v2) {
		this(new RoundNumber(v1), new RoundNumber(v2));
	}

	public NodePair(DxfDimension dim) {
		this(dim.getCheckPoint1(), dim.getCheckPoint2());
	}

	public NodePair(RoundNumber n1, RoundNumber n2) {
		this.n1 = n1;
		this.n2 = n2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.hashCode() == obj.hashCode()) {
			if (obj instanceof NodePair) {
				NodePair pair = (NodePair) obj;
				if ((this.n1.equals(pair.n1) && this.n2.equals(pair.n2)) || (this.n1.equals(pair.n2) && this.n2.equals(pair.n1))) {
					return true;
				}
			}
		}
		return false;
	}

	public RoundNumber getValue1() {
		return this.n1;
	}

	public RoundNumber getValue2() {
		return this.n2;
	}

	@Override
	public int hashCode() {
		return this.n1.hashCode() + this.n2.hashCode();
	}
}
