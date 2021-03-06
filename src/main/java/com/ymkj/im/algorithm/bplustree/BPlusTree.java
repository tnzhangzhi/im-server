package com.ymkj.im.algorithm.bplustree;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.*;

//内存版本
public class BPlusTree {


    private int order = 8;
    private int maxKey = 7; //order-1;
    private int minKey = 3; //Math.ceil(order/2)-1;
    Node root;
    Node leafHead;
    

    public BPlusTree(){
        root = new LeafNode(true);
    }

    public void insert(Long key,String value){
        //从根节点向下查找
        //如果是叶子节点，直接查找合适位置插入，如果不是叶子节点，向下查找到对应的叶子节点插入
        //刚开始只有一个节点的时候，根节点即是内部节点也是叶子节点
        if(root.isLeaf()){
            insert((LeafNode) root,key,value);
        }else{
            Node node = getLeaf((TreeNode) root,key);
            while(!node.isLeaf()){
                node = getLeaf((TreeNode)node,key);
            }
            insert((LeafNode)node,key,value);
        }
    }

    private Node getLeaf(TreeNode node,Long key) {
        int keyIndex = caculateKeyIndex(node.getKeys(),key);
        return node.getNodes().get(keyIndex);
    }

    public void insert(LeafNode leafNode,Long key,String value){
        int index = caculateKeyIndex(leafNode.getKeys(),key);
        leafNode.addKey(index,key);
        leafNode.addValue(index,value);
        updateNode(leafNode);
    }

    private void updateNode(Node node) {
        if(isFull(node)){
            TreeNode parent = (TreeNode) node.getParent();
            if(parent == null){
                parent = new TreeNode();
                root = parent;
            }
            if(node.isLeaf()) {
                //当前节点作为左节点，新建一个右节点
                LeafNode right = new LeafNode(false);
                right.setParent(parent);
                node.setParent(parent);
                int nodeIndex = parent.getNodeIndex(node);
                int keyIndex = (node.getKeys().size() - 1) / 2;
                Long key = node.getKeys().get(keyIndex);

                for (int i = keyIndex; i < node.getKeys().size(); i++) {
                    right.getKeys().add(node.getKeys().remove(i));
                    right.getValues().add(((LeafNode) node).getValues().remove(i));
                    i--;
                }

                if (nodeIndex >= 0) {
                    parent.getNodes().add(nodeIndex + 1, right);
                } else {
                    parent.getNodes().add(node);
                    parent.getNodes().add(right);
                }
                parent.addKey(nodeIndex < 0 ? 0 : nodeIndex, key);
            }else{
                TreeNode right = new TreeNode();
                right.setParent(parent);
                node.setParent(parent);
                int nodeIndex = parent.getNodeIndex(node);
                int keyIndex = (node.getKeys().size() - 1) / 2;
                Long key = node.getKeys().get(keyIndex);

                for (int i = keyIndex; i < node.getKeys().size(); i++) {
                    right.getKeys().add(node.getKeys().remove(i));
                    i--;
                }
                right.getKeys().remove(0);
                for (int i = keyIndex+1; i < ((TreeNode)node).getNodes().size(); i++) {
                    Node temp =  ((TreeNode)node).getNodes().remove(i);
                    temp.setParent(right);
                    right.getNodes().add(temp);
                    i--;
                }

                if (nodeIndex >= 0) {
                    parent.getNodes().add(nodeIndex + 1, right);
                } else {
                    parent.getNodes().add(node);
                    parent.getNodes().add(right);
                }
                parent.addKey(nodeIndex < 0 ? 0 : nodeIndex, key);
            }
            updateNode(parent);
        }
    }

    private static int caculateKeyIndex(LinkedList<Long> keys,Long key) {
        if(keys.size()==0){
            return 0;
        }
        int start = 0;
        int end = keys.size();

        while((end-start) >0){
            int index = (end+start)/2;
            if(key > keys.get(index)){
                start = index+1;
            }else{
                end = index;
            }
        }

        return end;
    }


    public boolean isFull(Node node){
        if(node.getCapacity()>maxKey){
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree();
        LinkedList<Long> list = new LinkedList<>();
        int[] l = new int[]{8309, 2914, 51, 2622, 1561, 1558, 5485, 7880, 1083, 8974, 3784, 4967, 2128, 5594, 2937, 3242, 1222, 7820, 8848, 7841, 3514, 9455, 8339, 4362, 4225, 7925, 2208, 9137};

        for(int i=0;i<1024;i++) {
            long key = new Random().nextInt(10000);
            while(list.contains(key)){
                key = new Random().nextInt(10000);
            }
            list.add(key);
            System.out.println(list);
            tree.insert(key,key+"");
        }
        printTree2(tree);
    }

    private static void printTree2(BPlusTree tree) {
        Node node = tree.root;
        Map map = new HashMap();
        System.out.println(JSON.toJSONString(node, SerializerFeature.DisableCircularReferenceDetect));

    }

    private static Map printNode2(Node node,Map map) {
        map.put("name",node.getKeys());
        if(!node.isLeaf()){
            TreeNode t = (TreeNode) node;
            Map[] a = new HashMap[t.getNodes().size()];
            for(int i=0;i<t.getNodes().size();i++){
                a[i] = printNode2(t.getNodes().get(i),map);
            }
            map.put("children",a);
        }
        return map;
    }

    private static void printTree(BPlusTree tree) {
        Node node = tree.root;
        printNode(node);

    }

    private static void printNode(Node node) {
        if(node.isLeaf()){
            LeafNode leaf = (LeafNode) node;
            System.out.println("##########################");
            System.out.println(leaf.getKeys());
            System.out.println(leaf.getValues());
            System.out.println("########################");
        }else{
            TreeNode treeNode = (TreeNode) node;
            System.out.println("***********************");
            System.out.println(node.getKeys());
            System.out.println("***********************");
            LinkedList<Node> nodeList = treeNode.getNodes();
            for(int i=0;i<nodeList.size();i++){
                printNode(nodeList.get(i));
            }
        }
    }
}
