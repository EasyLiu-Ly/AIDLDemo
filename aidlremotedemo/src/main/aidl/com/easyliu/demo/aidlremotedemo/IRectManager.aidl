// IRectManager.aidl
package com.easyliu.demo.aidlremotedemo;

interface IRectManager {
   List<Rect> getRectList();
   void addRect(in Rect rect);//标注参数的方向
}
