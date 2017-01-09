package com.seaky.hamster.core.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDto {

  private List<Object> datas;

  private Map<Object, Object> maps = new HashMap<>();


  public List<Object> getDatas() {
    return datas;
  }

  public void setDatas(List<Object> datas) {
    this.datas = datas;
  }

  public Map<Object, Object> getMaps() {
    return maps;
  }

  public void setMaps(Map<Object, Object> maps) {
    this.maps = maps;
  }



  public int getExtdata() {
    return extdata;
  }

  public void setExtdata(int extdata) {
    this.extdata = extdata;
  }



  private int extdata;
  //
  // public int getExtdata() {
  // return extdata;
  // }
  //
  // public void setExtdata(int extdata) {
  // this.extdata = extdata;
  // }


}
