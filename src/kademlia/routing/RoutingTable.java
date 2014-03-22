package kademlia.routing;

import java.util.ArrayList;
import java.util.List;
import kademlia.node.Node;
import kademlia.node.NodeId;

/**
 * Implementation of a Kademlia routing table
 *
 * @author Joshua Kissoon
 * @created 20140215
 *
 * @todo Make the KadBucket represent the Bucket interface
 * @todo Change the code to reflect the bucket interface and not the specific KadBucket implementation
 */
public class RoutingTable
{

    private final Node localNode;  // The current node
    private transient KadBucket[] buckets;

    
    {
        buckets = new KadBucket[NodeId.ID_LENGTH];  // 160 buckets; 1 for each level in the tree
    }

    public RoutingTable(Node localNode)
    {
        this.localNode = localNode;

        /* Initialize all of the buckets to a specific depth */
        this.initialize();

        /* Insert the local node */
        this.insert(localNode);
    }

    /**
     * Initialize the RoutingTable to it's default state
     */
    public final void initialize()
    {
        this.buckets = new KadBucket[NodeId.ID_LENGTH];
        for (int i = 0; i < NodeId.ID_LENGTH; i++)
        {
            buckets[i] = new KadBucket(i);
        }
    }

    /**
     * Adds a new node to the routing table based on how far it is from the LocalNode.
     *
     * @param n The contact to add
     */
    public final void insert(Node n)
    {
        this.buckets[this.getBucketId(n.getNodeId())].insert(n);
    }

    /**
     * Remove a node from the routing table.
     *
     * @param n The node to remove
     */
    public final void remove(Node n)
    {
        int bucketId = this.getBucketId(n.getNodeId());

        /* If the bucket has the contact, remove it */
        if (this.buckets[bucketId].containNode(n))
        {
            this.buckets[bucketId].removeNode(n);
        }
    }

    /**
     * Compute the bucket ID in which a given node should be placed; the bucketId is computed based on how far the node is away from the Local Node.
     *
     * @param nid The NodeId for which we want to find which bucket it belong to
     *
     * @return Integer The bucket ID in which the given node should be placed.
     */
    public final int getBucketId(NodeId nid)
    {
        int bId = this.localNode.getNodeId().getDistance(nid) - 1;

        /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
        return bId < 0 ? 0 : bId;
    }

    /**
     * Find the closest set of contacts to a given NodeId
     *
     * @param target The NodeId to find contacts close to
     * @param num    The number of contacts to find
     *
     * @return List A List of contacts closest to target
     */
    public final List<Node> findClosest(NodeId target, int num)
    {
        List<Node> closest = new ArrayList<>(num);

        /* Get the bucket index to search for closest from */
        int bucketIndex = this.getBucketId(target);

        /* Add the contacts from this bucket to the return contacts */
        for (Node c : this.buckets[bucketIndex].getNodes())
        {
            if (closest.size() < num)
            {
                closest.add(c);
            }
            else
            {
                break;
            }
        }

        if (closest.size() >= num)
        {
            return closest;
        }

        /* If we still need more nodes, we add from buckets on either side of the closest bucket */
        for (int i = 1; ((bucketIndex - i) >= 0 || (bucketIndex + i) < NodeId.ID_LENGTH); i++)
        {
            /* Check the bucket on the left side */
            if (bucketIndex - i > 0)
            {
                for (Node c : this.buckets[bucketIndex - i].getNodes())
                {
                    if (closest.size() < num)
                    {
                        closest.add(c);
                    }
                    else
                    {
                        break;
                    }
                }
            }

            /* Check the bucket on the right side */
            if (bucketIndex + i < NodeId.ID_LENGTH)
            {
                for (Node c : this.buckets[bucketIndex + i].getNodes())
                {
                    if (closest.size() < num)
                    {
                        closest.add(c);
                    }
                    else
                    {
                        break;
                    }
                }
            }

            /* If we have enough contacts, then stop adding */
            if (closest.size() >= num)
            {
                break;
            }
        }

        return closest;
    }

    /**
     * @return List A List of all Nodes in this RoutingTable
     */
    public final List getAllNodes()
    {
        List<Node> nodes = new ArrayList<>();

        for (KadBucket b : this.buckets)
        {
            nodes.addAll(b.getNodes());
        }

        return nodes;
    }

    /**
     * @return Bucket[] The buckets in this Kad Instance
     */
    public final KadBucket[] getBuckets()
    {
        return this.buckets;
    }

    /**
     * Set the KadBuckets of this routing table, mainly used when retrieving saved state
     *
     * @param buckets
     */
    public final void setBuckets(KadBucket[] buckets)
    {
        this.buckets = buckets;
    }

    @Override
    public final String toString()
    {
        StringBuilder sb = new StringBuilder("\nPrinting Routing Table Started ***************** \n");
        for (KadBucket b : this.buckets)
        {
            if (b.numNodes() > 0)
            {
                sb.append("# nodes in Bucket with depth ");
                sb.append(b.getDepth());
                sb.append(": ");
                sb.append(b.numNodes());
                sb.append("\n");
                sb.append(b.toString());
                sb.append("\n");
            }
        }
        sb.append("Printing Routing Table Ended ******************** ");

        return sb.toString();
    }

}
