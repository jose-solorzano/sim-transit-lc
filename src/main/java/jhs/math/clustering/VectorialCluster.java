package jhs.math.clustering;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jhs.math.common.ItemUtil;
import jhs.math.common.VectorialItem;
import jhs.math.util.MathUtil;

public class VectorialCluster<T extends VectorialItem> implements Iterable<T>, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	protected final double[] clusterPosition;
	private final List<Member<T>> members = new ArrayList<Member<T>>();
	
	public VectorialCluster(double[] clusterPosition) {
		this.clusterPosition = clusterPosition;
	}

	public List<T> getItems() {
		List<T> list = new ArrayList<T>();
		for(Member<T> member : this.members) {
			list.add(member.item);
		}
		return list;
	}
	
	public final boolean isEmpty() {
		return this.members.isEmpty();
	}
	
	public final Iterator<T> iterator() {
		return this.getItems().iterator();
	}

	private final void addMember(T item, double distanceSq) {
		this.members.add(new Member<T>(item, distanceSq));
	}

	public final void addMember(T item) {
		double distanceSq = this.distanceSquared(item);
		this.members.add(new Member<T>(item, distanceSq));
	}
	
	private final double distanceSquared(T item) {
		return MathUtil.euclideanDistanceSquared(item.getPosition(), this.clusterPosition);
	}
	
	public double[] getClusterPosition() {
		return this.clusterPosition;
	}
	
	public final double[] calculateMeanPositionOfMembers() {
		return ItemUtil.meanPosition(this.getItems());
	}
	
	public final double calculateWcss() {
		double sum = 0;
		for(Member<T> member : this.members) {
			sum += member.distanceSq;
		}
		return sum;
	}

	public final int size() {
		return this.members.size();
	}
	
	public static <T extends VectorialItem> double calculateWcss(VectorialCluster<T>[] clusters) {
		double sum = 0;
		for(VectorialCluster<T> cluster : clusters) {
			sum += cluster.calculateWcss();
		}
		return sum;
	}

	public static <T extends VectorialItem> FoundCluster<T> findClosestClusterImpl(VectorialCluster<T>[] clusters, T item) {		
		double minDistanceSq = Double.POSITIVE_INFINITY;
		VectorialCluster<T> closestCluster = null;
		double[] itemPosition = item.getPosition();
		for(VectorialCluster<T> cluster : clusters) {
			double distanceSq = MathUtil.euclideanDistanceSquared(itemPosition, cluster.getClusterPosition());
			if(distanceSq < minDistanceSq) {
				minDistanceSq = distanceSq;
				closestCluster = cluster;
			}
		}
		return new FoundCluster<T>(closestCluster, minDistanceSq);
	}
	
	public static <T extends VectorialItem> void populateClusters(List<? extends T> items, VectorialCluster<T>[] clusters){
		for(T item : items) {
			FoundCluster<T> ccinfo = VectorialCluster.findClosestClusterImpl(clusters, item);
			VectorialCluster<T> closestCluster = ccinfo.cluster;
			if(closestCluster != null) {
				closestCluster.addMember(item, ccinfo.distanceSq);
			}
		}
	}
	
	private static class FoundCluster<T extends VectorialItem> {
		private final VectorialCluster<T> cluster;
		private final double distanceSq;
		
		public FoundCluster(VectorialCluster<T> cluster, double distanceSq) {
			super();
			this.cluster = cluster;
			this.distanceSq = distanceSq;
		}
	}
	
	private static class Member<T> {
		private final T item;
		private final double distanceSq;
		
		public Member(T item, double distanceSq) {
			super();
			this.item = item;
			this.distanceSq = distanceSq;
		}
	}
}
