/*
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.service.saveandrestore.persistence.dao;

import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Configuration;
import org.phoebus.applications.saveandrestore.model.ConfigurationData;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.Snapshot;
import org.phoebus.applications.saveandrestore.model.SnapshotData;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.applications.saveandrestore.model.Tag;

import java.util.List;

/**
 * @author georgweiss Created 11 Mar 2019
 */
public interface NodeDAO {

	/**
	 * Retrieves all child nodes of the specified node.
	 * @param uniqueNodeId The unique id of the node
	 * @return A potentially empty list of child {@link Node}s.
	 */
	List<Node> getChildNodes(String uniqueNodeId);

	/**
	 * Retrieve the node identified by the unique node id
	 *
	 * @param uniqueNodeId
	 *            The unique node id
	 * @return A {@link Node} object
	 */
	Node getNode(String uniqueNodeId);

	/**
	 * Deletes a {@link Node}, folder or configuration. If the node is a folder, the
	 * entire sub-tree of the folder is deleted, including the snapshots associated
	 * with configurations in the sub-tree.
	 *
	 * @param nodeId
	 *            The unique id of the node to delete.
	 *
	 */
	void deleteNode(String nodeId);

	/**
	 * Creates a new node in the tree.
	 *
	 * @param parentNodeId
	 *            The unique id of the parent node in which to create the new {@link Node}.
	 * @param node
	 *            The new {@link Node} to create
	 * @return The created node.
	 *
	 */
	Node createNode(String parentNodeId, Node node);


	Node getParentNode(String uniqueNodeId);

	/**
	 * Moves {@link Node}s (folder or config) to a new parent node.
	 *
	 * @param nodeIds List of unique node ids subject to move
	 * @param targetId Unique id of new parent node
	 * @param userName
	 *            The (account) name of the user performing the operation.
	 * @return The target {@link Node} object that is the new parent of the moved source {@link Node}
	 */
	Node moveNodes(List<String> nodeIds, String targetId, String userName);

	/**
	 * Copies {@link Node}s (folder or config) to some parent node.
	 *
	 * @param nodeIds List of unique node ids subject to move
	 * @param targetId Unique id of target node
	 * @param userName
	 *            The (account) name of the user performing the operation.
	 * @return The target {@link Node} object that is the new parent of the moved source {@link Node}
	 */
	Node copyNodes(List<String> nodeIds, String targetId, String userName);


	/**
	 * Convenience method
	 *
	 * @return The root {@link Node} of the tree structure.
	 */
	Node getRootNode();

	/**
	 * Get snapshots for the specified configuration id.
	 *
	 * @param uniqueNodeId
	 *            The database unique id of the configuration (i.e. the snapshots'
	 *            parent node) see {@link Node#getUniqueId()}
	 * @return A list of snapshot {@link Node} objects associated with the specified
	 *         configuration id. Snapshots that have not yet been committed (=saved
	 *         with comment) are not included.
	 */
	public List<Node> getSnapshots(String uniqueNodeId);

	/**
	 * Get a snapshot.
	 *
	 * @param uniqueNodeId
	 *            The database unique id of the snapshot, see
	 *            {@link Node#getUniqueId()}.
	 * @return A {@link Node} object. <code>null</code> is returned if there is
	 *         no snapshot corresponding to the specified snapshot id, or if
	 *         <code>committedOnly=true</code> and for a snapshot with matching id
	 *         that has not been committed.
	 */
	Node getSnapshotNode(String uniqueNodeId);

	@Deprecated
	Node saveSnapshot(String parentsUniqueId, List<SnapshotItem> snapshotItems, String snapshotName, String comment, String userName);


	Snapshot saveSnapshot(String parentNodeId, Snapshot snapshot);

	List<ConfigPv> getConfigPvs(String configUniqueId);

	List<SnapshotItem> getSnapshotItems(String snapshotUniqueId);

	/**
	 * Updates a {@link Node} with respect to name or properties, or both. Node type cannot
	 * be changed, of course.
	 * @param nodeToUpdate The {@link Node} subject to update.
	 * @param customTimeForMigration A boolean for setting created time manually for migration.
	 * @return The {@link Node} object as read from the persistence implementation.
	 */
	Node updateNode(Node nodeToUpdate, boolean customTimeForMigration);

	List<Tag> getAllTags();

	List<Tag> getTags(String uniqueSnapshotId);

	List<Node> getAllSnapshots();

	/**
	 * Given a file path like /node1/node2/nodeX, find matching node(s). Since a folder node may
	 * contain a folder node and a save set node with the same name, the returned list may
	 * contain one or two {@link Node} objects, or will be <code>null</code> if the path does not correspond
	 * to an existing node.
	 * @param path A non-null "file path" that must start with a forward slash, otherwise an empty list
	 *            is returned. Search will start at the
	 *             tree root, i.e. the top level folder named "Save &amp; Restore Root".
	 * @return A {@link List	} one or two elements, or <code>null</code>.
	 */
	List<Node> getFromPath(String path);

	/**
	 * Given an unique node id, find the full path of the node matching the node id. The
	 * returned string will start with a forward slash and omit the name of the top level root
	 * node named "Save &amp; Restore Root". If the specified node id does not exist, <code>null</code>
	 * is returned.
	 * @param uniqueNodeId Unique id of a {@link Node}.
	 * @return Full path of the {@link Node} if found, otherwise <code>null</code>.
	 */
	String getFullPath(String uniqueNodeId);

	Configuration createConfiguration(String parentNodeId, Configuration configuration);

	ConfigurationData getConfigurationData(String uniqueId);

	/**
	 * Updates an existing {@link ConfigurationData}. In practice an overwrite operation as for instance
	 * the {@link ConfigurationData#getPvList()} may contain both added and removed elements compared to
	 * the persisted object.
	 * @param configuration The object to be updated
	 * @return The updated {@link ConfigurationData}
	 */
	Configuration updateConfiguration(Configuration configuration);

	SnapshotData getSnapshotData(String uniqueId);

	boolean isMoveOrCopyAllowed(List<Node> nodesToMove, Node targetNode);

	Node findParentFromPathElements(Node node, String[] pathElements, int depth);

}
