package com.source3g.platform.utils;

/**
 * Created by Administrator on 9/19/2017.
 */

import com.source3g.platform.dto.PosInfo;
import com.source3g.platform.dto.Position;

import java.util.ArrayList;
import java.util.List;

public class AStar {

    private PosInfo[][] mapArr;

    public static final int STEP = 10;

    private ArrayList<Position> openList = new ArrayList<Position>();
    private ArrayList<Position> closeList = new ArrayList<Position>();

    public Position findMinFNodeInOpneList() {
        Position tempNode = openList.get(0);
        for (Position node : openList) {
            if (node.F < tempNode.F) {
                tempNode = node;
            }
        }
        return tempNode;
    }

    public AStar(PosInfo[][] mapArr){
        this.mapArr = mapArr;
    }

    public ArrayList<Position> findNeighborNodes(Position currentNode) {
        ArrayList<Position> arrayList = new ArrayList<Position>();
        // 只考虑上下左右，不考虑斜对角
        int topX = currentNode.x;
        int topY = currentNode.y - 1;
        if (canReach(topX, topY) && !exists(closeList, topX, topY)) {
            arrayList.add(new Position(topX, topY));
        }
        int bottomX = currentNode.x;
        int bottomY = currentNode.y + 1;
        if (canReach(bottomX, bottomY) && !exists(closeList, bottomX, bottomY)) {
            arrayList.add(new Position(bottomX, bottomY));
        }
        int leftX = currentNode.x - 1;
        int leftY = currentNode.y;
        if (canReach(leftX, leftY) && !exists(closeList, leftX, leftY)) {
            arrayList.add(new Position(leftX, leftY));
        }
        int rightX = currentNode.x + 1;
        int rightY = currentNode.y;
        if (canReach(rightX, rightY) && !exists(closeList, rightX, rightY)) {
            arrayList.add(new Position(rightX, rightY));
        }
        return arrayList;
    }

    public boolean canReach(int x, int y) {
        if (x >= 0 && x < mapArr.length && y >= 0 && y < mapArr[0].length) {
            return !mapArr[x][y].isBlock();
        }
        return false;
    }

    public Position findPath(Position startNode, Position endNode) {

        // 把起点加入 open list
        openList.add(startNode);

        while (openList.size() > 0) {
            // 遍历 open list ，查找 F值最小的节点，把它作为当前要处理的节点
            Position currentNode = findMinFNodeInOpneList();
            // 从open list中移除
            openList.remove(currentNode);
            // 把这个节点移到 close list
            closeList.add(currentNode);

            ArrayList<Position> neighborNodes = findNeighborNodes(currentNode);
            for (Position node : neighborNodes) {
                if (exists(openList, node)) {
                    foundPoint(currentNode, node);
                } else {
                    notFoundPoint(currentNode, endNode, node);
                }
            }
            if (find(openList, endNode) != null) {
                return find(openList, endNode);
            }
        }

        return find(openList, endNode);
    }

    private void foundPoint(Position tempStart, Position node) {
        int G = calcG(tempStart, node);
        if (G < node.G) {
            node.parent = tempStart;
            node.G = G;
            node.calcF();
        }
    }

    private void notFoundPoint(Position tempStart, Position end, Position node) {
        node.parent = tempStart;
        node.G = calcG(tempStart, node);
        node.H = calcH(end, node);
        node.calcF();
        openList.add(node);
    }

    private int calcG(Position start, Position node) {
        int G = STEP;
        int parentG = node.parent != null ? node.parent.G : 0;
        return G + parentG;
    }

    private int calcH(Position end, Position node) {
        int step = Math.abs(node.x - end.x) + Math.abs(node.y - end.y);
        return step * STEP;
    }


    public static Position find(List<Position> nodes, Position point) {
        for (Position n : nodes)
            if ((n.x == point.x) && (n.y == point.y)) {
                return n;
            }
        return null;
    }

    public static boolean exists(List<Position> nodes, Position node) {
        for (Position n : nodes) {
            if ((n.x == node.x) && (n.y == node.y)) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(List<Position> nodes, int x, int y) {
        for (Position n : nodes) {
            if ((n.x == x) && (n.y == y)) {
                return true;
            }
        }
        return false;
    }

}
