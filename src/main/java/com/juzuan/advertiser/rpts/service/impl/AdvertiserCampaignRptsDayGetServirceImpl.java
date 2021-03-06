package com.juzuan.advertiser.rpts.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.juzuan.advertiser.rpts.mapper.AdvertiserCampaignRptsDayGetMapper;
import com.juzuan.advertiser.rpts.mapper.CampaignListMapper;
import com.juzuan.advertiser.rpts.mapper.RequestMapper;
import com.juzuan.advertiser.rpts.mapper.TaobaoAuthorizeUserMapper;
import com.juzuan.advertiser.rpts.model.*;
import com.juzuan.advertiser.rpts.service.AdvertiserCampaignRptsDayGetService;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ZuanshiAdvertiserCampaignRptsDayGetRequest;
import com.taobao.api.response.ZuanshiAdvertiserCampaignRptsDayGetResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
@Service
public class AdvertiserCampaignRptsDayGetServirceImpl implements AdvertiserCampaignRptsDayGetService {
    private static String appkey="25139411";
    private static String url ="https://eco.taobao.com/router/rest";
    private static String secret="ccd188d30d3731df6d176ba8a2151765";
    @Autowired
    private CampaignListMapper campaignListMapper;
    @Autowired
    private TaobaoAuthorizeUserMapper taobaoAuthorizeUserMapper;
    @Autowired
    private AdvertiserCampaignRptsDayGetMapper advertiserCampaignRptsDayGetMapper;
    @Autowired
    private RequestMapper requestMapper;
    //定时更新，每天3:00
    @Scheduled( cron = "0 0 3 * * ? ")
    public String getAdvertiserCampaignRptsDay(){
        //时间格式化
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        //获取系统当前时间
        String time= sdf.format(new java.util.Date());
        //获取系统前一天时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        String yestime = sdf.format(calendar.getTime());
        List<Request> requests = requestMapper.selectAllRequest();
        for (Request request:requests) {
            List<CampaignList> campaignLists = campaignListMapper.selectAllCampaign();
            for (CampaignList campaignList : campaignLists) {
                Long id = campaignList.getCampaignId();
                TaobaoAuthorizeUser taobaoAuthorizeUser = taobaoAuthorizeUserMapper.slectByUserId(campaignList.getTaobaoUserId());
                String sessionKey = taobaoAuthorizeUser.getAccessToken();
                TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
                ZuanshiAdvertiserCampaignRptsDayGetRequest req = new ZuanshiAdvertiserCampaignRptsDayGetRequest();
                //req.setStartTime("2018-11-09");
                req.setStartTime(yestime);
                //req.setEndTime("2018-11-22");
                req.setEndTime(time);
                req.setCampaignId(id);
                req.setEffect(request.getEffect());
                req.setCampaignModel(request.getCampaignModel());
                req.setEffectType(request.getEffectType());
                ZuanshiAdvertiserCampaignRptsDayGetResponse rsp = null;
                try {
                    rsp = client.execute(req, sessionKey);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                System.out.println("计划分日列表"+rsp.getBody());
                JSONObject one = JSON.parseObject(rsp.getBody());
                JSONObject onee = one.getJSONObject("zuanshi_advertiser_campaign_rpts_day_get_response");
                System.out.println(taobaoAuthorizeUser.getTaobaoUserId() + "  " + campaignList.getCampaignId() + "  " + onee.toString());
                JSONObject two = JSON.parseObject(onee.toString());
                JSONObject twoo = two.getJSONObject("campaign_offline_rpt_days_list");
                if (twoo.size() != 0) {
                    JSONArray three = twoo.getJSONArray("data");
                    System.out.println("目标数组  " + three.toString());
                    for (Object ob : three.toArray()) {
                        System.out.println("遍历目标数组" + ob.toString());
                    }
                    List<AdvertiserCampaignRptsDayGet> advertiserCampaignRptsDayGets = JSONObject.parseArray(three.toString(), AdvertiserCampaignRptsDayGet.class);
                    //遍历json数据属性对象
                    for (AdvertiserCampaignRptsDayGet advertiserCampaignRptsDayGet : advertiserCampaignRptsDayGets) {
                        System.out.println("遍历对象数组  " + advertiserCampaignRptsDayGet.toString());
                        //插入计算的属性值
                        if (advertiserCampaignRptsDayGet.getAdPv() == null) {
                            advertiserCampaignRptsDayGet.setAdPv("0");
                        }
                        //advertiserCampaignRptsDayGet.setCartNum(advertiserCampaignRptsDayGet.getCartNum()==null?"0":advertiserCampaignRptsDayGet.getCartNum());
                        if (advertiserCampaignRptsDayGet.getAlipayInshopAmt() == null) {
                            advertiserCampaignRptsDayGet.setAlipayInshopAmt("0");
                        }
                        if (advertiserCampaignRptsDayGet.getAlipayInShopNum() == null) {
                            advertiserCampaignRptsDayGet.setAlipayInShopNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getAvgAccessPageNum() == null) {
                            advertiserCampaignRptsDayGet.setAvgAccessPageNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getCampaignId() == null) {
                            advertiserCampaignRptsDayGet.setCampaignId(String.valueOf(campaignList.getCampaignId()));
                        }
                        if (advertiserCampaignRptsDayGet.getCampaignName() == null) {
                            advertiserCampaignRptsDayGet.setCampaignName(campaignList.getCampaignName());
                        }
                        if (advertiserCampaignRptsDayGet.getCartNum() == null) {
                            advertiserCampaignRptsDayGet.setCartNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getAvgAccessTime() == null) {
                            advertiserCampaignRptsDayGet.setAvgAccessTime("0");
                        }
                        if (advertiserCampaignRptsDayGet.getCharge() == null) {
                            advertiserCampaignRptsDayGet.setCharge("0");
                        }
                        if (advertiserCampaignRptsDayGet.getClick() == null) {
                            advertiserCampaignRptsDayGet.setClick("0");
                        }
                        if (advertiserCampaignRptsDayGet.getCtr() == null) {
                            advertiserCampaignRptsDayGet.setCtr("0");
                        }
                        if (advertiserCampaignRptsDayGet.getCvr() == null) {
                            advertiserCampaignRptsDayGet.setCvr("0");
                        }
                        if (advertiserCampaignRptsDayGet.getDeepInshopUv() == null) {
                            advertiserCampaignRptsDayGet.setDeepInshopUv("0");
                        }
                        if (advertiserCampaignRptsDayGet.getUv() == null) {
                            advertiserCampaignRptsDayGet.setUv("0");
                        }
                        if (advertiserCampaignRptsDayGet.getRoi() == null) {
                            advertiserCampaignRptsDayGet.setRoi("0");
                        }
                        if (advertiserCampaignRptsDayGet.getDirShopColNum() == null) {
                            advertiserCampaignRptsDayGet.setDirShopColNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getEcpc() == null) {
                            advertiserCampaignRptsDayGet.setEcpc("0");
                        }
                        if (advertiserCampaignRptsDayGet.getEcpm() == null) {
                            advertiserCampaignRptsDayGet.setEcpm("0");
                        }
                        if (advertiserCampaignRptsDayGet.getGmvInshopAmt() == null) {
                            advertiserCampaignRptsDayGet.setGmvInshopAmt("0");
                        }
                        if (advertiserCampaignRptsDayGet.getGmvInshopNum() == null) {
                            advertiserCampaignRptsDayGet.setGmvInshopNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getCartNum() == null) {
                            advertiserCampaignRptsDayGet.setCartNum("0");
                        }
                        if (advertiserCampaignRptsDayGet.getInshopItemColNum() == null) {
                            advertiserCampaignRptsDayGet.setInshopItemColNum("0");
                        }
                        advertiserCampaignRptsDayGet.setTaobaoUserId(taobaoAuthorizeUser.getTaobaoUserId());
                        advertiserCampaignRptsDayGet.setEffect(request.getEffect());
                        advertiserCampaignRptsDayGet.setCampaignModel(request.getCampaignModel());
                        advertiserCampaignRptsDayGet.setEffectType(request.getEffectType());
                        if (Double.parseDouble(advertiserCampaignRptsDayGet.getClick()) == 0) {
                            advertiserCampaignRptsDayGet.setCommodityPurchaseRate("0");//点击量为零
                            advertiserCampaignRptsDayGet.setCommodityCollectionRate("0");
                            advertiserCampaignRptsDayGet.setTotalCollectionRate("0");
                        } else {
                            advertiserCampaignRptsDayGet.setCommodityPurchaseRate(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getCartNum()) / Double.parseDouble(advertiserCampaignRptsDayGet.getClick())));//加购率=添加购物车量/点击量
                            advertiserCampaignRptsDayGet.setCommodityCollectionRate(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getInshopItemColNum()) / Double.parseDouble(advertiserCampaignRptsDayGet.getClick())));//收藏率=收藏宝贝量/点击量
                            advertiserCampaignRptsDayGet.setTotalCollectionRate(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getClick()) / Double.parseDouble(advertiserCampaignRptsDayGet.getClick())));//总收藏加购率=（收藏宝贝量+收藏店铺量+添加购物车量）/点击量

                        }
                        Double collectionAndBuy = Double.parseDouble(advertiserCampaignRptsDayGet.getDirShopColNum()) + Double.parseDouble(advertiserCampaignRptsDayGet.getInshopItemColNum()) + Double.parseDouble(advertiserCampaignRptsDayGet.getCartNum());
                        if (collectionAndBuy == 0) {
                            advertiserCampaignRptsDayGet.setTotalCollectionPlusCost("0");
                        } else {
                            advertiserCampaignRptsDayGet.setTotalCollectionPlusCost(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getCharge()) / collectionAndBuy));//总收藏加购成本=消耗/（收藏宝贝量+收藏店铺量+添加购物车量 )
                        }
                        if (Double.parseDouble(advertiserCampaignRptsDayGet.getInshopItemColNum()) == 0) {
                            advertiserCampaignRptsDayGet.setCommodityCollectionCost("0");
                        } else {
                            advertiserCampaignRptsDayGet.setCommodityCollectionCost(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getCharge()) / Double.parseDouble(advertiserCampaignRptsDayGet.getInshopItemColNum())));//收藏成本=消耗/收藏宝贝量
                        }
                        if (Double.parseDouble(advertiserCampaignRptsDayGet.getCartNum()) == 0) {
                            advertiserCampaignRptsDayGet.setCommodityPlusCost("0");
                        } else {
                            advertiserCampaignRptsDayGet.setCommodityPlusCost(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getCharge()) / Double.parseDouble(advertiserCampaignRptsDayGet.getCartNum())));//加购成本=消耗/添加购物车量
                        }
                        if (Double.parseDouble(advertiserCampaignRptsDayGet.getUv()) == 0) {
                            advertiserCampaignRptsDayGet.setAverageUvValue("0");
                        } else {
                            advertiserCampaignRptsDayGet.setAverageUvValue(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getAlipayInshopAmt()) / Double.parseDouble(advertiserCampaignRptsDayGet.getUv())));//平均访客价值 (average_uv_value) = 成交订单金额/访客

                        }
                        if (Double.parseDouble(advertiserCampaignRptsDayGet.getAlipayInShopNum()) == 0) {
                            advertiserCampaignRptsDayGet.setOrderAverageAmount("0");
                            advertiserCampaignRptsDayGet.setAverageCostOfOrder("0");

                        } else {
                            advertiserCampaignRptsDayGet.setAverageCostOfOrder(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getCharge()) / Double.parseDouble(advertiserCampaignRptsDayGet.getAlipayInShopNum())));//订单平均成本(average_cost_of_order)订单平均成本 = 消耗/成交订单量

                            advertiserCampaignRptsDayGet.setOrderAverageAmount(String.valueOf(Double.parseDouble(advertiserCampaignRptsDayGet.getAlipayInshopAmt()) / Double.parseDouble(advertiserCampaignRptsDayGet.getAlipayInShopNum())));//订单平均金额(order_average_amount)订单平均金额 = 成交订单金额/成交订单量
                        }

                        advertiserCampaignRptsDayGetMapper.insert(advertiserCampaignRptsDayGet);
                    }
                } else {
                    System.out.println("没有获取的信息");
                    continue;
                }
            }
        }
        return "";
    }
}
