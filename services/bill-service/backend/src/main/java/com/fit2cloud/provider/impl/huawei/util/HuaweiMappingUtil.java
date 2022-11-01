package com.fit2cloud.provider.impl.huawei.util;

import com.fit2cloud.common.constants.PlatformConstants;
import com.fit2cloud.common.provider.util.CommonUtil;
import com.fit2cloud.es.entity.CloudBill;
import com.fit2cloud.provider.constants.BillModeConstants;
import com.huaweicloud.sdk.bss.v2.model.ResFeeRecordV2;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code @Author:张少虎}
 * {@code @Date: 2022/10/17  5:56 PM}
 * {@code @Version 1.0}
 * {@code @注释: }
 */
public class HuaweiMappingUtil {


    /**
     * 将华为云账单对象转换为系统账单对象
     *
     * @param item 华为云账单对象
     * @return 系统账单对象
     */
    public static CloudBill toCloudBill(ResFeeRecordV2 item) {
        CloudBill cloudBill = new CloudBill();
        cloudBill.setId(UUID.randomUUID().toString().replace("-", ""));
        cloudBill.setProvider(PlatformConstants.fit2cloud_huawei_platform.name());
        cloudBill.setRegionId(item.getRegion());
        cloudBill.setRegionName(item.getRegionName());
        cloudBill.setProjectId(item.getEnterpriseProjectId());
        cloudBill.setProjectName(item.getEnterpriseProjectName());
        cloudBill.setBillMode(toBillMode(item.getChargeMode()));
        cloudBill.setReousrceId(item.getResourceId());
        cloudBill.setResourceName(item.getResourceName());
        cloudBill.setProductId(item.getProductId());
        cloudBill.setProductName(item.getProductName());
        cloudBill.setTags(toTags(item.getResourceTag()));
        cloudBill.setTotalCost(BigDecimal.valueOf(item.getOfficialAmount()));
        cloudBill.setRealTotalCost(BigDecimal.valueOf(item.getAmount()));
        //2022-04-30T14:00:00Z
        cloudBill.setUsageStartDate(CommonUtil.getLocalDateTime(item.getEffectiveTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        cloudBill.setUsageEndDate(CommonUtil.getLocalDateTime(item.getExpireTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        cloudBill.setBillingCycle(CommonUtil.getLocalDateTime(item.getBillDate(), "yyyy-MM-dd"));
        cloudBill.setPayAccountId(item.getCustomerId());
        return cloudBill;

    }

    /**
     * 计费模式。
     * <p>
     * 1：包年/包月
     * 3：按需
     * 10：预留实例
     * 将华为云的计费模式转换为云管计费模式
     *
     * @param chargeMode 华为云计费模式
     * @return 云管计费模式
     */
    private static String toBillMode(String chargeMode) {
        if (chargeMode.equals("1")) {
            return BillModeConstants.MONTHLY.name();
        } else if (chargeMode.equals("3")) {
            return BillModeConstants.ON_DEMAND.name();
        } else {
            return BillModeConstants.OTHER.name();
        }
    }

    /**
     * 根据标签字符串获取标签
     *
     * @param tag 标签字符串
     * @return 标签对象
     */
    private static Map<String, Object> toTags(String tag) {
        //"resource_tag":"billing:;"
        if (StringUtils.isEmpty(tag)) {
            return new HashMap<>();
        } else {
            String[] split = tag.split(";");
            return Arrays.stream(split).flatMap(t -> {
                String[] tagSplit = t.split(":");
                HashMap<String, Object> tags = new HashMap<>();
                if (tagSplit.length == 1) {
                    tags.put(tagSplit[0], "");
                }
                if (tagSplit.length == 2) {
                    tags.put(tagSplit[0], tagSplit[1]);
                }
                return tags.entrySet().stream();
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
