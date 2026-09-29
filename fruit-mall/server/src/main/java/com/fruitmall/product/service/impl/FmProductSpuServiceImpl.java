package com.fruitmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitmall.common.enums.ProductStatusEnum;
import com.fruitmall.common.enums.SkuStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.product.domain.FmCategory;
import com.fruitmall.product.domain.FmProductAttrDef;
import com.fruitmall.product.domain.FmProductAttrValue;
import com.fruitmall.product.domain.FmProductImage;
import com.fruitmall.product.domain.FmProductSku;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.dto.AttrValueSaveDTO;
import com.fruitmall.product.dto.ProductSaveDTO;
import com.fruitmall.product.dto.SkuSaveDTO;
import com.fruitmall.product.mapper.FmCategoryMapper;
import com.fruitmall.product.mapper.FmProductAttrDefMapper;
import com.fruitmall.product.mapper.FmProductAttrValueMapper;
import com.fruitmall.product.mapper.FmProductImageMapper;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import com.fruitmall.product.mapper.FmProductSpuMapper;
import com.fruitmall.product.query.ProductQuery;
import com.fruitmall.product.service.IFmProductSpuService;
import com.fruitmall.product.vo.ProductDetailVO;
import com.fruitmall.product.vo.ProductListVO;
import com.fruitmall.product.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 商品服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmProductSpuServiceImpl extends ServiceImpl<FmProductSpuMapper, FmProductSpu>
        implements IFmProductSpuService {

    /** 图片类型：主图 */
    private static final int IMAGE_TYPE_MAIN = 10;

    /** 图片类型：详情图 */
    private static final int IMAGE_TYPE_DETAIL = 20;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FmProductSkuMapper skuMapper;
    private final FmProductAttrDefMapper attrDefMapper;
    private final FmProductAttrValueMapper attrValueMapper;
    private final FmProductImageMapper imageMapper;
    private final FmCategoryMapper categoryMapper;

    @Override
    public PageResult<ProductListVO> pageProducts(ProductQuery query) {
        IPage<ProductListVO> page = baseMapper.selectProductPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public ProductDetailVO getDetail(Long spuId) {
        FmProductSpu spu = this.getById(spuId);
        if (spu == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(spu.getId());
        vo.setSpuCode(spu.getSpuCode());
        vo.setSpuName(spu.getSpuName());
        vo.setSubtitle(spu.getSubtitle());
        vo.setCategoryId(spu.getCategoryId());
        vo.setOriginPlace(spu.getOriginPlace());
        vo.setSeasonMonths(spu.getSeasonMonths());
        vo.setStorageCondition(spu.getStorageCondition());
        vo.setShelfLifeDays(spu.getShelfLifeDays());
        vo.setUnit(spu.getUnit());
        vo.setMainImage(spu.getMainImage());
        vo.setDetail(spu.getDetail());
        vo.setSalesCount(spu.getSalesCount());
        vo.setStatus(spu.getStatus());
        vo.setSort(spu.getSort());
        vo.setRemark(spu.getRemark());
        vo.setCreateTime(spu.getCreateTime());

        FmCategory category = categoryMapper.selectById(spu.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getCategoryName());
        vo.setSkus(listSkus(spuId));
        vo.setAttrs(attrValueMapper.selectAttrsBySpuId(spuId));
        vo.setDetailImages(imageMapper.selectList(Wrappers.<FmProductImage>lambdaQuery()
                        .eq(FmProductImage::getSpuId, spuId)
                        .eq(FmProductImage::getImageType, IMAGE_TYPE_DETAIL)
                        .orderByAsc(FmProductImage::getSort))
                .stream().map(FmProductImage::getImageUrl).toList());
        return vo;
    }

    @Override
    public List<ProductSkuVO> listSkus(Long spuId) {
        List<FmProductSku> skus = skuMapper.selectList(Wrappers.<FmProductSku>lambdaQuery()
                .eq(FmProductSku::getSpuId, spuId)
                .orderByAsc(FmProductSku::getPrice));
        List<ProductSkuVO> list = new ArrayList<>();
        for (FmProductSku sku : skus) {
            list.add(toSkuVO(sku));
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createProduct(ProductSaveDTO dto) {
        checkCategory(dto.getCategoryId());
        checkSpuCode(dto.getSpuCode(), null);
        checkSkuCodes(dto.getSkus());

        FmProductSpu spu = new FmProductSpu();
        fillSpuFields(spu, dto);
        // 新建商品一律先落草稿，属性与规格补齐后再上架
        spu.setStatus(ProductStatusEnum.DRAFT.getCode());
        spu.setSalesCount(0);
        try {
            this.save(spu);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "商品编码已存在：" + dto.getSpuCode());
        }

        saveSkus(spu.getId(), dto.getSkus(), List.of());
        saveAttrs(spu.getId(), dto.getAttrs());
        saveImages(spu.getId(), dto.getMainImage(), dto.getDetailImages());
        log.info("商品建档完成：spuId={}, spuCode={}", spu.getId(), spu.getSpuCode());
        return spu.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProduct(Long id, ProductSaveDTO dto) {
        FmProductSpu exists = this.getById(id);
        if (exists == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        checkCategory(dto.getCategoryId());
        checkSpuCode(dto.getSpuCode(), id);
        checkSkuCodes(dto.getSkus());

        FmProductSpu update = new FmProductSpu();
        update.setId(id);
        fillSpuFields(update, dto);
        try {
            this.updateById(update);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "商品编码已存在：" + dto.getSpuCode());
        }

        List<FmProductSku> existsSkus = skuMapper.selectList(
                Wrappers.<FmProductSku>lambdaQuery().eq(FmProductSku::getSpuId, id));
        saveSkus(id, dto.getSkus(), existsSkus);
        // 属性与图片没有外部引用，整体替换（物理删除，避免旧行占用唯一索引）
        attrValueMapper.deleteBySpuId(id);
        saveAttrs(id, dto.getAttrs());
        imageMapper.deleteBySpuId(id);
        saveImages(id, dto.getMainImage(), dto.getDetailImages());
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        FmProductSpu spu = this.getById(id);
        if (spu == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (ProductStatusEnum.ON_SALE.getCode().equals(status)) {
            checkForOnSale(spu);
        }
        FmProductSpu update = new FmProductSpu();
        update.setId(id);
        update.setStatus(status);
        this.updateById(update);
        log.info("商品状态变更：spuId={}, {} -> {}", id, spu.getStatus(), status);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProduct(Long id) {
        FmProductSpu spu = this.getById(id);
        if (spu == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        this.removeById(id);
        skuMapper.delete(Wrappers.<FmProductSku>lambdaQuery().eq(FmProductSku::getSpuId, id));
        attrValueMapper.deleteBySpuId(id);
        imageMapper.deleteBySpuId(id);
    }

    /**
     * 上架校验，对应一致性规则 R-01。
     */
    private void checkForOnSale(FmProductSpu spu) {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(spu.getMainImage())) {
            missing.add("主图");
        }
        List<FmProductSku> skus = skuMapper.selectList(Wrappers.<FmProductSku>lambdaQuery()
                .eq(FmProductSku::getSpuId, spu.getId())
                .eq(FmProductSku::getStatus, SkuStatusEnum.ENABLED.getCode()));
        if (skus.isEmpty()) {
            missing.add("至少一个启用状态的规格");
        }
        if (skus.stream().anyMatch(sku -> sku.getPrice() == null || sku.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            missing.add("规格售价必须大于 0");
        }
        boolean hasStock = skus.stream().anyMatch(sku ->
                availableStock(sku) > 0);
        if (!hasStock) {
            missing.add("可售库存必须大于 0");
        }

        Set<Long> filledAttrIds = attrValueMapper.selectList(Wrappers.<FmProductAttrValue>lambdaQuery()
                        .eq(FmProductAttrValue::getSpuId, spu.getId()))
                .stream().map(FmProductAttrValue::getAttrDefId).collect(Collectors.toSet());
        List<FmProductAttrDef> requiredDefs = attrDefMapper.selectList(
                Wrappers.<FmProductAttrDef>lambdaQuery()
                        .eq(FmProductAttrDef::getRequired, 1)
                        .eq(FmProductAttrDef::getStatus, 10));
        List<String> missingAttrs = requiredDefs.stream()
                .filter(def -> !filledAttrIds.contains(def.getId()))
                .map(FmProductAttrDef::getAttrName)
                .toList();
        if (!missingAttrs.isEmpty()) {
            missing.add("必填特色属性：" + String.join("、", missingAttrs));
        }

        if (!missing.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上架失败，请先补齐：" + String.join("；", missing));
        }
    }

    private void checkCategory(Long categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类不存在");
        }
    }

    private void checkSpuCode(String spuCode, Long excludeId) {
        Long count = baseMapper.selectCount(Wrappers.<FmProductSpu>lambdaQuery()
                .eq(FmProductSpu::getSpuCode, spuCode)
                .ne(excludeId != null, FmProductSpu::getId, excludeId));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.CONFLICT, "商品编码已存在：" + spuCode);
        }
    }

    private void checkSkuCodes(List<SkuSaveDTO> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "至少填写一个商品规格");
        }
        Set<String> codes = new HashSet<>();
        for (SkuSaveDTO sku : skus) {
            if (!codes.add(sku.getSkuCode())) {
                throw new BizException(ResultCode.BAD_REQUEST, "规格编码在同一商品内重复：" + sku.getSkuCode());
            }
        }
    }

    private void fillSpuFields(FmProductSpu spu, ProductSaveDTO dto) {
        spu.setSpuCode(dto.getSpuCode());
        spu.setCategoryId(dto.getCategoryId());
        spu.setSpuName(dto.getSpuName());
        spu.setSubtitle(dto.getSubtitle());
        spu.setOriginPlace(dto.getOriginPlace());
        spu.setSeasonMonths(dto.getSeasonMonths());
        spu.setStorageCondition(dto.getStorageCondition());
        spu.setShelfLifeDays(dto.getShelfLifeDays());
        spu.setUnit(dto.getUnit());
        spu.setMainImage(dto.getMainImage());
        spu.setDetail(dto.getDetail());
        spu.setSort(dto.getSort() == null ? 0 : dto.getSort());
        spu.setRemark(dto.getRemark());
    }

    /**
     * 保存规格：先按 id 匹配，未带 id 时按规格编码匹配（便于前端编辑页直接回填提交），
     * 仍匹配不到才作为新规格插入；
     * 请求中未出现的旧规格标记为停用而不是删除，避免破坏历史订单的规格引用。
     */
    private void saveSkus(Long spuId, List<SkuSaveDTO> skus, List<FmProductSku> existsSkus) {
        Set<Long> keepIds = new HashSet<>();
        for (SkuSaveDTO dto : skus) {
            String specJson = normalizeJson(dto.getSpecJson());
            FmProductSku exists = matchExistingSku(dto, existsSkus);
            if (exists != null) {
                FmProductSku update = new FmProductSku();
                update.setId(exists.getId());
                fillSkuFields(update, dto, specJson);
                try {
                    skuMapper.updateById(update);
                } catch (DuplicateKeyException e) {
                    throw new BizException(ResultCode.CONFLICT,
                            "规格编码已被其他商品的规格占用：" + dto.getSkuCode());
                }
                keepIds.add(exists.getId());
            } else {
                FmProductSku sku = new FmProductSku();
                sku.setSpuId(spuId);
                sku.setLockedStock(0);
                sku.setSalesCount(0);
                sku.setVersion(0);
                fillSkuFields(sku, dto, specJson);
                try {
                    skuMapper.insert(sku);
                } catch (DuplicateKeyException e) {
                    throw new BizException(ResultCode.CONFLICT, "规格编码已存在：" + dto.getSkuCode());
                }
                keepIds.add(sku.getId());
            }
        }
        for (FmProductSku exists : existsSkus) {
            if (!keepIds.contains(exists.getId())
                    && !SkuStatusEnum.DISABLED.getCode().equals(exists.getStatus())) {
                FmProductSku disable = new FmProductSku();
                disable.setId(exists.getId());
                disable.setStatus(SkuStatusEnum.DISABLED.getCode());
                skuMapper.updateById(disable);
            }
        }
    }

    /**
     * 匹配已有规格：优先用 id；未带 id 时按规格编码匹配。
     * 带了 id 却匹配不到说明前端传了脏数据，直接报错而不是静默新增。
     */
    private FmProductSku matchExistingSku(SkuSaveDTO dto, List<FmProductSku> existsSkus) {
        if (dto.getId() != null) {
            return existsSkus.stream()
                    .filter(sku -> sku.getId().equals(dto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ResultCode.BAD_REQUEST,
                            "规格不存在：" + dto.getId()));
        }
        return existsSkus.stream()
                .filter(sku -> sku.getSkuCode().equals(dto.getSkuCode()))
                .findFirst()
                .orElse(null);
    }

    private void fillSkuFields(FmProductSku sku, SkuSaveDTO dto, String specJson) {
        sku.setSkuCode(dto.getSkuCode());
        sku.setSpecName(dto.getSpecName());
        sku.setSpecJson(specJson);
        sku.setImage(dto.getImage());
        sku.setOriginalPrice(dto.getOriginalPrice());
        sku.setPrice(dto.getPrice());
        sku.setStock(dto.getStock());
        sku.setWarnStock(dto.getWarnStock() == null ? 0 : dto.getWarnStock());
        sku.setStatus(dto.getStatus() == null ? SkuStatusEnum.ENABLED.getCode() : dto.getStatus());
    }

    private void saveAttrs(Long spuId, List<AttrValueSaveDTO> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return;
        }
        for (AttrValueSaveDTO dto : attrs) {
            FmProductAttrValue value = new FmProductAttrValue();
            value.setSpuId(spuId);
            value.setAttrDefId(dto.getAttrDefId());
            value.setAttrValue(dto.getAttrValue());
            value.setNumValue(dto.getNumValue());
            attrValueMapper.insert(value);
        }
    }

    private void saveImages(Long spuId, String mainImage, List<String> detailImages) {
        int sort = 0;
        if (StringUtils.hasText(mainImage)) {
            insertImage(spuId, mainImage, IMAGE_TYPE_MAIN, sort++);
        }
        if (detailImages != null) {
            for (String url : detailImages) {
                if (StringUtils.hasText(url)) {
                    insertImage(spuId, url, IMAGE_TYPE_DETAIL, sort++);
                }
            }
        }
    }

    private void insertImage(Long spuId, String url, int imageType, int sort) {
        FmProductImage image = new FmProductImage();
        image.setSpuId(spuId);
        image.setImageUrl(url);
        image.setImageType(imageType);
        image.setSort(sort);
        imageMapper.insert(image);
    }

    /**
     * 规格键值必须是合法 JSON（数据库列类型为 JSON），非法时给出明确的参数错误而不是 500。
     */
    private String normalizeJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json).toString();
        } catch (Exception e) {
            throw new BizException(ResultCode.BAD_REQUEST, "规格键值不是合法的 JSON：" + json);
        }
    }

    private int availableStock(FmProductSku sku) {
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        int locked = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        return stock - locked;
    }

    private ProductSkuVO toSkuVO(FmProductSku sku) {
        ProductSkuVO vo = new ProductSkuVO();
        vo.setId(sku.getId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpecName(sku.getSpecName());
        vo.setSpecJson(sku.getSpecJson());
        vo.setImage(sku.getImage());
        vo.setOriginalPrice(sku.getOriginalPrice());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setLockedStock(sku.getLockedStock());
        vo.setAvailableStock(availableStock(sku));
        vo.setStatus(sku.getStatus());
        return vo;
    }
}
