package com.hmdp.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    // key CACHE_SHOP_TYPE
    @Resource
    private IShopTypeService shopTypeService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = CACHE_SHOP_TYPE;
        //  1、从Redis中查询商户类型缓存
        String shopTypesJson = stringRedisTemplate.opsForValue().get(key);
        //  2、判断是否存在
        if (StrUtil.isNotBlank(shopTypesJson)) {
            //  3. 存在，直接返回list
//            List<ShopType> typeList = Convert.toList(ShopType.class, shopTypesJson);
            List<ShopType> typeList = JSONUtil.toList(shopTypesJson, ShopType.class);
            return Result.ok(typeList);
        }

        //  4、不存在，查询数据库
        List<ShopType> typeList = shopTypeService
                .query().orderByAsc("sort").list();
        //  5、数据库不存在 返回错误
        if (typeList == null) {
            return Result.fail("店铺类型不存在");
        }

        // 6、数据库存在，将结果写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));
//        stringRedisTemplate.expire(key,CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        //  7、返回list
        return Result.ok(typeList);
    }
}
