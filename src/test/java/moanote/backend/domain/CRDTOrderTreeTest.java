package moanote.backend.domain;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class CRDTOrderTreeTest {


//  @Test
//  void testInsertNoConflict() {
//    CRDTOrderTree tree = new CRDTOrderTree();
//    tree.insert("1", "1", "root", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("2", "2", "1", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("3", "3", "1", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("4", "4", "2", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("5", "5", "2", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("6", "6", "3", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("7", "7", "3", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("8", "8", "4", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("9", "9", "4", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("10", "10", "5", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("11", "11", "5", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("12", "12", "6", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("13", "13", "6", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("14", "14", "7", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("15", "15", "7", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("16", "16", "8", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("17", "17", "8", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("18", "18", "9", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("19", "19", "9", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("20", "20", "10", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("21", "21", "10", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("22", "22", "11", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("23", "23", "11", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("24", "24", "12", CRDTOrderTreeNode.Side.LEFT);
//    tree.insert("25", "25", "12", CRDTOrderTreeNode.Side.RIGHT);
//    tree.insert("26", "26", "13", CRDTOrderTreeNode.Side.LEFT);
//
//    for (int i = 1; i <= 26; i++) {
//      tree.update(Integer.toString(i),
//          new LWWRegister(Integer.toString(i), 1, Integer.toString(i) + "\n"));
//    }
//  }

}