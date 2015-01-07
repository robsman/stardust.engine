package org.eclipse.stardust.engine.extensions.camel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;

public class LoadPartitionsAction implements Action<List<String>> {

	@SuppressWarnings("rawtypes")
	public List<String> execute() {

		List<String> partitions = new ArrayList<String>();

		for (Iterator i = AuditTrailPartitionBean.findAll(); i.hasNext();) {
			AuditTrailPartitionBean p = (AuditTrailPartitionBean) i.next();
			partitions.add(p.getId());
		}

		return partitions;
	}
}
